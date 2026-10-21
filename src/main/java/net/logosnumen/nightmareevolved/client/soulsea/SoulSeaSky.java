package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The Soul Sea's sky: still and empty but for faint stars (fewer in Sunny's sea, none in Mordret's mist, warm in
 * Nephis's light). The soul's own suns hang over the water as figures, close enough to touch.
 */
public class SoulSeaSky extends DimensionSpecialEffects {
    private VertexBuffer stars;

    public SoulSeaSky() {
        super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColour, float brightness) {
        return fogColour;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ,
        Matrix4f modelView, Matrix4f projection) {
        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelView, Camera camera, Matrix4f projection, boolean isFoggy,
        Runnable setupFog) {
        SoulSeaClient.Look look = SoulSeaClient.Look.current();
        if (look.stars <= 0.0F) {
            return true;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelView);
        float time = ticks + partialTick;
        FogRenderer.setupNoFog();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (this.stars == null) {
            this.stars = buildStars();
        }

        float twinkle = (0.5F + 0.06F * Mth.sin(time * 0.013F)) * look.stars;
        boolean warm = look == SoulSeaClient.Look.LIGHT;
        RenderSystem.setShaderColor(twinkle * (warm ? 1.0F : 0.85F), twinkle * (warm ? 0.85F : 0.9F), twinkle * (warm ? 0.55F : 1.0F), twinkle);
        this.stars.bind();
        this.stars.drawWithShader(poseStack.last().pose(), projection, GameRenderer.getPositionShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        setupFog.run();
        return true;
    }

    /** A denser, fainter starfield than the overworld's, so the sea feels deep and still. */
    private static VertexBuffer buildStars() {
        RandomSource random = RandomSource.create(5129L);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 2600; i++) {
            float x = random.nextFloat() * 2.0F - 1.0F;
            float y = random.nextFloat() * 2.0F - 1.0F;
            float z = random.nextFloat() * 2.0F - 1.0F;
            float size = 0.07F + random.nextFloat() * 0.1F;
            float lengthSq = Mth.lengthSquared(x, y, z);
            if (lengthSq > 0.01F && lengthSq < 1.0F) {
                Vector3f dir = new Vector3f(x, y, z).normalize(100.0F);
                float spin = (float)(random.nextDouble() * Math.PI * 2.0);
                Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), dir).rotateZ(spin);
                builder.addVertex(new Vector3f(dir).add(new Vector3f(size, -size, 0.0F).rotate(rotation)));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(size, size, 0.0F).rotate(rotation)));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(-size, size, 0.0F).rotate(rotation)));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(-size, -size, 0.0F).rotate(rotation)));
            }
        }

        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.buildOrThrow());
        VertexBuffer.unbind();
        return buffer;
    }
}
