package net.logosnumen.nightmareevolved.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.logosnumen.nightmareevolved.vfx.TintedParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

/**
 * The ability effect particles. Each kind moves and fades in its own way, and all take their colour from the options.
 * Everything but smoke is drawn as light: added onto the scene rather than painted over it, so overlapping particles
 * brighten each other and glow against the dark. Sparks stretch along their flight into streaks, and the flat kinds
 * (wave rings, halos, rune circles and blade arcs) lie in a plane set by the options' yaw and roll instead of facing
 * the camera.
 */
public final class VfxParticle extends TextureSheetParticle {
    private static final ResourceLocation PARTICLE_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");
    /** Additive blending for the glowing kinds. The blend function is put back once particles are drawn. */
    public static final ParticleRenderType GLOW = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, PARTICLE_ATLAS);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "nightmare_evolved:glow";
        }
    };

    public enum Kind {
        /** A soft glowing dot that drifts and fades. */
        MOTE(0.2F, 22, 0.9F, 0.0F, true, false),
        /** A bright fleck that streaks along its path and dies quickly. */
        SPARK(0.1F, 10, 0.86F, 0.03F, true, false),
        /** A cinder that floats upward, flickering. */
        EMBER(0.085F, 30, 0.96F, -0.012F, true, false),
        /** A puff of smoke that swells and thins out; dark ones read as shadow. */
        WISP(0.45F, 34, 0.94F, -0.004F, false, false),
        /** A floating rune that fades in and out. */
        GLYPH(0.2F, 30, 0.9F, -0.003F, true, false),
        /** A ring that grows from a point and fades, facing the viewer: a flash. */
        PULSE(0.25F, 12, 1.0F, 0.0F, true, false),
        /** A tumbling sliver of light, for breaking wards and scales. */
        SHARD(0.09F, 18, 0.9F, 0.04F, true, false),
        /** A wave racing out across the ground; its scale is the radius it reaches. */
        RING(1.0F / 0.9F, 14, 1.0F, 0.0F, true, true),
        /** A circle of light that holds its place; its scale is its radius. */
        HALO(1.0F / 0.88F, 24, 1.0F, 0.0F, true, true),
        /** A turning circle of runes; its scale is its radius. */
        SIGIL(1.0F / 0.95F, 40, 1.0F, 0.0F, true, true),
        /** The arc a blade or a tail cuts, opening toward its yaw; its scale is its reach. */
        SWEEP(1.0F / 0.95F, 7, 1.0F, 0.0F, true, true);

        final float size;
        final int life;
        final float friction;
        final float gravity;
        final boolean bright;
        final boolean flat;

        Kind(float size, int life, float friction, float gravity, boolean bright, boolean flat) {
            this.size = size;
            this.life = life;
            this.friction = friction;
            this.gravity = gravity;
            this.bright = bright;
            this.flat = flat;
        }
    }

    private final Kind kind;
    private final float baseSize;
    private final float baseAlpha;
    private final float tilt;
    private float yaw;
    private float oYaw;

    VfxParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TintedParticleOptions options, SpriteSet sprites, Kind kind) {
        super(level, x, y, z);
        this.kind = kind;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.friction = kind.friction;
        this.gravity = kind.gravity;
        this.hasPhysics = false;
        int colour = options.colour();
        this.rCol = (colour >> 16 & 255) / 255.0F;
        this.gCol = (colour >> 8 & 255) / 255.0F;
        this.bCol = (colour & 255) / 255.0F;
        float jitter = kind.flat ? 1.0F : 0.85F + this.random.nextFloat() * 0.3F;
        this.baseSize = kind.size * options.scale() * jitter;
        this.quadSize = this.baseSize;
        this.lifetime = options.lifetime() > 0 ? options.lifetime() : kind.life + this.random.nextInt(Math.max(1, kind.life / 4));
        this.baseAlpha = kind == Kind.WISP ? 0.7F : 1.0F;
        this.alpha = this.baseAlpha;
        this.roll = kind == Kind.GLYPH || kind == Kind.SHARD ? this.random.nextFloat() * Mth.TWO_PI : 0.0F;
        this.oRoll = this.roll;
        this.yaw = options.yaw();
        this.oYaw = this.yaw;
        this.tilt = options.roll();
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        float t = (float)this.age / this.lifetime;
        this.oRoll = this.roll;
        this.oYaw = this.yaw;
        switch (this.kind) {
            case MOTE -> this.alpha = this.baseAlpha * (t < 0.55F ? 1.0F : 1.0F - (t - 0.55F) / 0.45F);
            case SPARK -> this.alpha = 1.0F - t * t;
            case EMBER -> {
                this.alpha = (1.0F - t) * (0.7F + 0.3F * Mth.sin(this.age * 1.7F));
                this.xd += (this.random.nextFloat() - 0.5F) * 0.004F;
                this.zd += (this.random.nextFloat() - 0.5F) * 0.004F;
            }
            case WISP -> this.alpha = this.baseAlpha * (1.0F - t);
            case GLYPH -> {
                this.alpha = fadeInOut(t, 0.2F, 0.3F);
                this.roll += 0.02F;
            }
            case PULSE -> this.alpha = 1.0F - t;
            case SHARD -> {
                this.alpha = 1.0F - t;
                this.roll += 0.25F;
            }
            case RING -> this.alpha = (float)Math.pow(1.0F - t, 1.4F);
            case HALO -> this.alpha = fadeInOut(t, 0.15F, 0.4F);
            case SIGIL -> {
                this.alpha = fadeInOut(t, 0.2F, 0.3F);
                this.yaw += 0.035F;
            }
            case SWEEP -> this.alpha = 1.0F - t * t;
        }
    }

    private static float fadeInOut(float t, float in, float out) {
        if (t < in) {
            return t / in;
        }

        return t > 1.0F - out ? (1.0F - t) / out : 1.0F;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0F, 1.0F);
        return switch (this.kind) {
            case MOTE -> this.baseSize * (1.0F - 0.4F * t);
            case WISP -> this.baseSize * (1.0F + 0.8F * t);
            case PULSE -> this.baseSize * (1.0F + 4.0F * t);
            case RING -> {
                float out = 1.0F - t;
                yield this.baseSize * (0.12F + 0.88F * (1.0F - out * out * out));
            }
            case HALO -> this.baseSize * (0.92F + 0.08F * t);
            case SIGIL -> this.baseSize * (0.8F + 0.2F * Math.min(1.0F, t / 0.2F));
            case SWEEP -> this.baseSize * (0.88F + 0.17F * t);
            default -> this.baseSize;
        };
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (this.kind.flat) {
            this.renderFlat(buffer, camera, partialTicks);
        } else if (this.kind != Kind.SPARK || !this.renderStreak(buffer, camera, partialTicks)) {
            super.render(buffer, camera, partialTicks);
        }
    }

    /** A quad lying in its own plane, drawn from both sides. */
    private void renderFlat(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - cam.x());
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - cam.y());
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - cam.z());
        float size = this.getQuadSize(partialTicks);
        float yawNow = Mth.lerp(partialTicks, this.oYaw, this.yaw);
        float cos = Mth.cos(yawNow);
        float sin = Mth.sin(yawNow);
        // Forward along the ground for that yaw, and the right hand, tipped up about forward by the roll.
        Vector3f forward = new Vector3f(-sin, 0.0F, cos).mul(size);
        Vector3f side = new Vector3f(-cos * Mth.cos(this.tilt), Mth.sin(this.tilt), -sin * Mth.cos(this.tilt)).mul(size);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        this.corner(buffer, px, py, pz, side, forward, 1.0F, -1.0F, u1, v1, light);
        this.corner(buffer, px, py, pz, side, forward, 1.0F, 1.0F, u1, v0, light);
        this.corner(buffer, px, py, pz, side, forward, -1.0F, 1.0F, u0, v0, light);
        this.corner(buffer, px, py, pz, side, forward, -1.0F, -1.0F, u0, v1, light);
        this.corner(buffer, px, py, pz, side, forward, -1.0F, -1.0F, u0, v1, light);
        this.corner(buffer, px, py, pz, side, forward, -1.0F, 1.0F, u0, v0, light);
        this.corner(buffer, px, py, pz, side, forward, 1.0F, 1.0F, u1, v0, light);
        this.corner(buffer, px, py, pz, side, forward, 1.0F, -1.0F, u1, v1, light);
    }

    /** A spark drawn as a streak along its velocity, longer the faster it flies. False when it is too slow to streak. */
    private boolean renderStreak(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vector3f along = new Vector3f((float)this.xd, (float)this.yd, (float)this.zd);
        float speed = along.length();
        if (speed < 0.002F) {
            return false;
        }

        Vec3 cam = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - cam.x());
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - cam.y());
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - cam.z());
        along.div(speed);
        Vector3f side = new Vector3f(along).cross(px, py, pz);
        if (side.lengthSquared() < 1.0E-6F) {
            return false;
        }

        float size = this.getQuadSize(partialTicks);
        side.normalize().mul(size * 0.6F);
        along.mul(size * (1.5F + Math.min(speed * 25.0F, 5.0F)));
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        this.corner(buffer, px, py, pz, along, side, 1.0F, -1.0F, u1, v1, light);
        this.corner(buffer, px, py, pz, along, side, 1.0F, 1.0F, u1, v0, light);
        this.corner(buffer, px, py, pz, along, side, -1.0F, 1.0F, u0, v0, light);
        this.corner(buffer, px, py, pz, along, side, -1.0F, -1.0F, u0, v1, light);
        return true;
    }

    private void corner(VertexConsumer buffer, float x, float y, float z, Vector3f a, Vector3f b, float da, float db, float u, float v, int light) {
        buffer.addVertex(x + a.x * da + b.x * db, y + a.y * da + b.y * db, z + a.z * da + b.z * db)
            .setUv(u, v)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(light);
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        // A streak reaches well past its centre.
        AABB box = super.getRenderBoundingBox(partialTicks);
        return this.kind == Kind.SPARK ? box.inflate(this.getQuadSize(partialTicks) * 7.0F) : box;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return this.kind.bright ? LightTexture.FULL_BRIGHT : super.getLightColor(partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.kind.bright ? GLOW : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** Puts the usual blend function back after particles are drawn, so the additive one never leaks into other rendering. */
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            RenderSystem.defaultBlendFunc();
        }
    }

    public record Provider(SpriteSet sprites, Kind kind) implements ParticleProvider<TintedParticleOptions> {
        @Override
        public Particle createParticle(TintedParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new VfxParticle(level, x, y, z, xd, yd, zd, options, this.sprites, this.kind);
        }
    }
}
