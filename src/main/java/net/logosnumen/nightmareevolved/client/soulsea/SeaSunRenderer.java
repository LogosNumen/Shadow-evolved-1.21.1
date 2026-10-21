package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaSunEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws a soul core as a sun: layered, camera-facing glows, a wide halo, the sun itself in its Aspect's colour, and a
 * white heart that only shows as it fills. Additive blending makes it read as light. Sunny's Shadow Core is the one
 * sun that gives none: a black disc ringed with faint violet. Mordret's is pale, with a turning ring of marks.
 */
public class SeaSunRenderer extends EntityRenderer<SeaSunEntity> {
    private static final ResourceLocation GLOW = NightmareEvolved.id("textures/environment/soul_core.png");
    private static final ResourceLocation MARKS = NightmareEvolved.id("textures/environment/sun_marks.png");
    private static final RenderType LIGHT = RenderType.eyes(GLOW);
    private static final RenderType DARK = RenderType.entityTranslucent(GLOW);
    private static final RenderType RING = RenderType.eyes(MARKS);

    public SeaSunRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SeaSunEntity sun, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float fill = sun.fill();
        float time = sun.tickCount + partialTick;
        float bob = Mth.sin(time * 0.03F + sun.index() * 1.7F) * 0.25F;
        float pulse = 1.0F + 0.04F * Mth.sin(time * 0.07F + sun.index());
        float radius = (2.0F + 1.3F * fill) * pulse;
        int colour = sun.colour();
        float[] tint = {(colour >> 16 & 255) / 255.0F, (colour >> 8 & 255) / 255.0F, (colour & 255) / 255.0F};
        float strength = 0.6F + 0.4F * fill;
        pose.pushPose();
        pose.translate(0.0F, sun.getBbHeight() / 2.0F + bob, 0.0F);
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose at = pose.last();
        switch (sun.look()) {
            case "shadow" -> {
                // A black sun: transparent darkness with no source of light, a thin violet ring about it.
                VertexConsumer dark = buffers.getBuffer(DARK);
                quad(dark, at, radius * 1.15F, new float[] {0.0F, 0.0F, 0.0F}, 0.92F, false);
                VertexConsumer rim = buffers.getBuffer(LIGHT);
                quad(rim, at, radius * 1.7F, new float[] {0.5F, 0.34F, 0.95F}, 0.35F + 0.2F * fill, true);
            }
            case "light" -> {
                VertexConsumer glow = buffers.getBuffer(LIGHT);
                quad(glow, at, radius * 3.4F, new float[] {1.0F, 0.85F, 0.55F}, strength * 0.4F, true);
                quad(glow, at, radius * 1.3F, new float[] {1.0F, 0.97F, 0.88F}, strength, true);
                quad(glow, at, radius * 0.6F, new float[] {1.0F, 1.0F, 1.0F}, 0.6F + 0.4F * fill, true);
            }
            case "nothing" -> {
                VertexConsumer glow = buffers.getBuffer(LIGHT);
                quad(glow, at, radius * 2.2F, new float[] {0.78F, 0.8F, 0.85F}, strength * 0.25F, true);
                quad(glow, at, radius, new float[] {0.9F, 0.92F, 0.95F}, strength * 0.7F, true);
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotation(time * 0.01F));
                quad(buffers.getBuffer(RING), pose.last(), radius * 1.5F, new float[] {0.85F, 0.88F, 0.95F}, 0.5F, true);
                pose.popPose();
            }
            default -> {
                VertexConsumer glow = buffers.getBuffer(LIGHT);
                quad(glow, at, radius * 2.6F, tint, strength * 0.35F, true);
                quad(glow, at, radius, tint, strength, true);
                quad(glow, at, radius * 0.42F, new float[] {1.0F, 1.0F, 1.0F}, 0.25F + 0.7F * fill, true);
            }
        }

        pose.popPose();
        super.render(sun, yaw, partialTick, pose, buffers, light);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float size, float[] tint, float alpha, boolean additive) {
        if (alpha <= 0.01F) {
            return;
        }

        // Additive light is fainter for darker colours, so its strength scales the colour itself.
        float r = additive ? tint[0] * alpha : tint[0];
        float g = additive ? tint[1] * alpha : tint[1];
        float b = additive ? tint[2] * alpha : tint[2];
        float a = additive ? 1.0F : alpha;
        vertex(consumer, pose, -size, -size, 0.0F, 1.0F, r, g, b, a);
        vertex(consumer, pose, size, -size, 1.0F, 1.0F, r, g, b, a);
        vertex(consumer, pose, size, size, 1.0F, 0.0F, r, g, b, a);
        vertex(consumer, pose, -size, size, 0.0F, 0.0F, r, g, b, a);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v, float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, 0.0F).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaSunEntity sun) {
        return GLOW;
    }
}
