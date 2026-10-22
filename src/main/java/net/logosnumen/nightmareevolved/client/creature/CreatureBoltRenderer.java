package net.logosnumen.nightmareevolved.client.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.entity.creature.CreatureBolt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** A creature's bolt: a small glow of its colour, with a white-hot heart, always facing the viewer. */
public class CreatureBoltRenderer extends EntityRenderer<CreatureBolt> {
    private static final ResourceLocation GLOW = NightmareEvolved.id("textures/environment/soul_core.png");
    private static final RenderType LIGHT = RenderType.eyes(GLOW);

    public CreatureBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(CreatureBolt bolt, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        int colour = bolt.colour();
        float r = (colour >> 16 & 255) / 255.0F;
        float g = (colour >> 8 & 255) / 255.0F;
        float b = (colour & 255) / 255.0F;
        pose.pushPose();
        pose.translate(0.0F, bolt.getBbHeight() / 2.0F, 0.0F);
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        VertexConsumer consumer = buffers.getBuffer(LIGHT);
        quad(consumer, pose.last(), 0.55F, r * 0.7F, g * 0.7F, b * 0.7F);
        quad(consumer, pose.last(), 0.22F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(bolt, yaw, partial, pose, buffers, light);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float size, float r, float g, float b) {
        vertex(consumer, pose, -size, -size, 0.0F, 1.0F, r, g, b);
        vertex(consumer, pose, size, -size, 1.0F, 1.0F, r, g, b);
        vertex(consumer, pose, size, size, 1.0F, 0.0F, r, g, b);
        vertex(consumer, pose, -size, size, 0.0F, 0.0F, r, g, b);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v, float r, float g, float b) {
        consumer.addVertex(pose, x, y, 0.0F).setColor(r, g, b, 1.0F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CreatureBolt bolt) {
        return GLOW;
    }
}
