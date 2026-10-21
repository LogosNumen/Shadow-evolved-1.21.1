package net.logosnumen.nightmareevolved.client.dream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CrimsonSpire;
import net.logosnumen.nightmareevolved.network.RealmPayload;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The Dream Realm's skies, which differ region by region:
 * <ul>
 * <li>The Forgotten Shore: grey by day, with no sun but the Crimson Spire's artificial one burning over the labyrinth;
 * by night a black void without a single star.</li>
 * <li>Godgrave: a grey veil of storm clouds, the same at every hour; there is no night there. Where the veil tears,
 * the white sky shows through.</li>
 * <li>The Hollow Mountains: white mist.</li>
 * <li>The Stormsea: twilight under black clouds, pale stars between them.</li>
 * <li>Everywhere else the ordinary sky, with additions drawn over it: the Chained Isles' Sky Below with stars of its
 * own, the Moonriver Plain's three moons, the Nightmare Desert's blinding sun (see {@link RealmRender}).</li>
 * </ul>
 */
public final class DreamSky extends DimensionSpecialEffects {
    private static final ResourceLocation GLARE = NightmareEvolved.id("textures/environment/glare.png");

    public DreamSky() {
        super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColour, float brightness) {
        if (ClientRealm.sky() == Region.Sky.CLOUDVEIL) {
            return fogColour.multiply(0.9, 0.9, 0.92);
        }

        return fogColour.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return ClientRealm.sky() == Region.Sky.MIST;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ,
        Matrix4f modelView, Matrix4f projection) {
        return switch (ClientRealm.sky()) {
            case SHORE, MIST, CLOUDVEIL, DESERT, STORM, ASHEN -> true;
            default -> false;
        };
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelView, Camera camera, Matrix4f projection, boolean isFoggy,
        Runnable setupFog) {
        Region.Sky sky = ClientRealm.sky();
        float day = daylight(level, partialTick);
        Vec3 tint = level.getSkyColor(camera.getPosition(), partialTick);
        switch (sky) {
            case SHORE -> {
                // A dead grey sky, darkening into a starless void at night.
                dome(modelView, setupFog, (float)tint.x * day, (float)tint.y * day, (float)tint.z * day);
                spireSun(level, modelView, camera, day, partialTick);
                return true;
            }
            case CLOUDVEIL -> {
                dome(modelView, setupFog, 0.56F, 0.57F, 0.61F);
                tears(level, modelView, camera, partialTick);
                return true;
            }
            case MIST -> {
                dome(modelView, setupFog, 0.85F, 0.86F, 0.89F);
                return true;
            }
            case STORM -> {
                float lit = 0.25F + 0.5F * day;
                dome(modelView, setupFog, 0.12F * lit, 0.14F * lit, 0.2F * lit);
                RealmRender.stars(modelView, 0.35F * (1.0F - day * 0.6F), false);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /** How much daylight there is, from 0 at midnight to 1 at noon, the way the ordinary sky reckons it. */
    static float daylight(ClientLevel level, float partialTick) {
        float angle = level.getTimeOfDay(partialTick);
        return Mth.clamp(Mth.cos(angle * Mth.TWO_PI) * 2.0F + 0.5F, 0.0F, 1.0F);
    }

    /** A plain dome of sky in one colour, fogged at the horizon, with a darker floor below it. */
    private static void dome(Matrix4f modelView, Runnable setupFog, float r, float g, float b) {
        setupFog.run();
        RenderSystem.depthMask(false);
        RealmRender.identityModelView(() -> {
            PoseStack pose = new PoseStack();
            pose.mulPose(modelView);
            Matrix4f m = pose.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionShader);
            for (int half = 0; half < 2; half++) {
                float y = half == 0 ? 16.0F : -16.0F;
                float shade = half == 0 ? 1.0F : 0.4F;
                RenderSystem.setShaderColor(r * shade, g * shade, b * shade, 1.0F);
                BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
                builder.addVertex(m, 0.0F, y, 0.0F);
                for (int i = -180; i <= 180; i += 45) {
                    float a = (half == 0 ? 1 : -1) * i * Mth.DEG_TO_RAD;
                    builder.addVertex(m, 512.0F * Mth.cos(a), y, 512.0F * Mth.sin(a));
                }

                BufferUploader.drawWithShader(builder.buildOrThrow());
            }
        });
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    /** The Crimson Spire's artificial sun, burning over the labyrinth wherever on the Shore one stands. */
    private static void spireSun(ClientLevel level, Matrix4f modelView, Camera camera, float day, float partialTick) {
        Vec3 at = camera.getPosition();
        Vector3f toward = new Vector3f((float)(Places.CRIMSON_SPIRE.x() + 0.5 - at.x), (float)(CrimsonSpire.TOP + 14 - at.y),
            (float)(Places.CRIMSON_SPIRE.z() + 0.5 - at.z));
        float distance = toward.length();
        if (distance < 1.0F) {
            return;
        }

        toward.normalize();
        float size = Mth.clamp(7.0F * 90.0F / distance * 3.0F, 1.6F, 22.0F);
        float glow = 0.55F + 0.45F * day;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GLARE);
        RealmRender.identityModelView(() -> {
            PoseStack pose = new PoseStack();
            pose.mulPose(modelView);
            for (int layer = 0; layer < 2; layer++) {
                float s = layer == 0 ? size : size * 3.2F;
                float a = layer == 0 ? glow : glow * 0.35F;
                RenderSystem.setShaderColor(1.0F, 0.42F, 0.22F, a);
                RealmRender.billboard(pose.last().pose(), toward, 90.0F, s, true);
            }
        });
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    /** Where Godgrave's veil is torn, the white sky shows through: a glaring patch overhead. */
    private static void tears(ClientLevel level, Matrix4f modelView, Camera camera, float partialTick) {
        long now = level.getGameTime();
        Vec3 at = camera.getPosition();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GLARE);
        for (RealmPayload.Tear tear : ClientRealm.tears()) {
            float strength = RealmRender.tearStrength(tear, now, partialTick);
            if (strength <= 0.0F) {
                continue;
            }

            Vector3f toward = new Vector3f((float)(tear.x() - at.x), 260.0F, (float)(tear.z() - at.z)).normalize();
            RealmRender.identityModelView(() -> {
                PoseStack pose = new PoseStack();
                pose.mulPose(modelView);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, strength);
                RealmRender.billboard(pose.last().pose(), toward, 90.0F, 30.0F + tear.radius() * 1.5F, true);
            });
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    /** Godgrave never darkens: its light stays at the day's whatever the hour. The Shore's nights are blacker than any. */
    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker, float skyLight, int pixelX,
        int pixelY, Vector3f colours) {
        Region.Sky sky = ClientRealm.sky();
        if (sky != Region.Sky.CLOUDVEIL && sky != Region.Sky.SHORE) {
            return;
        }

        float brightness = LightTexture.getBrightness(level.dimensionType(), pixelY);
        float vr = 0.35F + 0.65F * skyDarken;
        float now = brightness * (skyDarken * 0.95F + 0.05F);
        if (sky == Region.Sky.CLOUDVEIL) {
            float dayR = brightness * 0.92F;
            colours.add((dayR - vr * now) * 0.96F, (dayR - vr * now) * 0.96F, (dayR - now) * 0.96F);
        } else {
            float night = 1.0F - Mth.clamp(skyDarken * 1.6F, 0.0F, 1.0F);
            colours.sub(vr * now * 0.55F * night, vr * now * 0.55F * night, now * 0.55F * night);
        }

        colours.set(Mth.clamp(colours.x, 0.0F, 1.0F), Mth.clamp(colours.y, 0.0F, 1.0F), Mth.clamp(colours.z, 0.0F, 1.0F));
    }

    /** The sun's rotation, as the ordinary sky turns it, for drawing things beside it. */
    static void sunRotation(PoseStack pose, ClientLevel level, float partialTick) {
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360.0F));
    }
}
