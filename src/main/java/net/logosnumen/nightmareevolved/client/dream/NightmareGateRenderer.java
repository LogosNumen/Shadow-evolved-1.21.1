package net.logosnumen.nightmareevolved.client.dream;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.dream.seed.NightmareGateEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Draws a Nightmare Gate. While it forms it is only a tall, faint warp in the air, a place where the light bends wrongly,
 * that slowly gathers; once open it is a rift of vile darkness, lens-shaped and ragged, its edges burning a dull
 * crimson that never quite settles. It always turns to face whoever looks at it.
 */
public class NightmareGateRenderer extends EntityRenderer<NightmareGateEntity> {
    private static final ResourceLocation CORE = NightmareEvolved.id("textures/entity/nightmare_gate.png");
    private static final ResourceLocation EDGE = NightmareEvolved.id("textures/entity/nightmare_gate_edge.png");
    private static final RenderType DARK = RenderType.entityTranslucent(CORE);
    private static final RenderType BURN = RenderType.eyes(EDGE);
    private static final int SEGMENTS = 24;

    public NightmareGateRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(NightmareGateEntity gate, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float time = gate.tickCount + partialTick;
        float age = gate.clientAge() + partialTick;
        float height = gate.height();
        float width = gate.width();
        Camera camera = this.entityRenderDispatcher.camera;
        Vec3 eye = camera.getPosition();
        float facing = (float)Mth.atan2(eye.x - gate.getX(), eye.z - gate.getZ());
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(facing));
        PoseStack.Pose at = pose.last();
        if (age < NightmareGateEntity.FORMING) {
            float p = age / NightmareGateEntity.FORMING;
            float tall = height * (0.35F + 0.65F * p);
            float flicker = 0.75F + 0.25F * Mth.sin(time * 0.31F) * Mth.sin(time * 0.07F);
            // The warp: two faint, slightly offset sheets of wrongness, shifting against each other.
            VertexConsumer dark = buffers.getBuffer(DARK);
            lens(dark, at, tall, width * 0.35F * (0.4F + 0.6F * p), time, 0.0F, 0.15F, 0.1F, 0.22F, (0.08F + 0.22F * p) * flicker, 0.4F, false);
            lens(dark, at, tall * 0.92F, width * 0.22F * (0.4F + 0.6F * p), time + 40.0F, 0.15F, 0.25F, 0.15F, 0.35F, (0.05F + 0.15F * p) * flicker, 0.6F,
                false);
            if (p > 0.75F) {
                // The first cracks of the tearing.
                float crack = (p - 0.75F) / 0.25F;
                lens(buffers.getBuffer(BURN), at, tall * 0.8F, 0.08F + 0.2F * crack, time, 0.0F, 0.9F, 0.2F, 0.25F, crack * flicker, 1.2F, true);
            }
        } else {
            float open = Math.min(1.0F, (age - NightmareGateEntity.FORMING) / 40.0F);
            float w = width * (0.25F + 0.75F * open);
            float pulse = 1.0F + 0.06F * Mth.sin(time * 0.11F);
            VertexConsumer burn = buffers.getBuffer(BURN);
            lens(burn, at, height * 1.04F, w * 0.95F * pulse, time, 0.0F, 0.72F, 0.05F, 0.1F, 0.75F, 1.0F, true);
            lens(burn, at, height * 1.12F, w * 1.35F * pulse, time * 0.7F, 0.03F, 0.38F, 0.02F, 0.18F, 0.3F, 0.7F, true);
            lens(buffers.getBuffer(DARK), at, height, w * 0.7F, time, 0.01F, 1.0F, 1.0F, 1.0F, 0.97F, 1.0F, false);
        }

        pose.popPose();
        super.render(gate, yaw, partialTick, pose, buffers, light);
    }

    /**
     * A ragged lens from the ground up: its half-width swells to {@code half} at the middle and narrows to points, each
     * segment's edge pushed in or out by a jag that shifts every few ticks.
     */
    private static void lens(VertexConsumer consumer, PoseStack.Pose pose, float height, float half, float time, float depth, float r, float g, float b,
        float alpha, float jag, boolean additive) {
        if (alpha <= 0.01F) {
            return;
        }

        float cr = additive ? r * alpha : r;
        float cg = additive ? g * alpha : g;
        float cb = additive ? b * alpha : b;
        float ca = additive ? 1.0F : alpha;
        float scroll = time * 0.004F;
        int step = (int)(time / 3.0F);
        float lastY = 0.0F;
        float lastLeft = 0.0F;
        float lastRight = 0.0F;
        for (int i = 1; i <= SEGMENTS; i++) {
            float v = i / (float)SEGMENTS;
            float y = v * height;
            float swell = (float)Math.pow(Mth.sin(v * Mth.PI), 0.75);
            float left = i == SEGMENTS ? 0.0F : half * swell * (1.0F + jag * 0.18F * jitter(i, step, 1));
            float right = i == SEGMENTS ? 0.0F : half * swell * (1.0F + jag * 0.18F * jitter(i, step, 2));
            float v0 = (i - 1) / (float)SEGMENTS + scroll;
            float v1 = v + scroll;
            vertex(consumer, pose, -lastLeft, lastY, depth, 0.0F, v0, cr, cg, cb, ca);
            vertex(consumer, pose, lastRight, lastY, depth, 1.0F, v0, cr, cg, cb, ca);
            vertex(consumer, pose, right, y, depth, 1.0F, v1, cr, cg, cb, ca);
            vertex(consumer, pose, -left, y, depth, 0.0F, v1, cr, cg, cb, ca);
            lastY = y;
            lastLeft = left;
            lastRight = right;
        }
    }

    private static float jitter(int i, int step, int side) {
        int h = i * 73856093 ^ step * 19349663 ^ side * 83492791;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return ((h & 0xFFFF) / 65535.0F) * 2.0F - 1.0F;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, float r, float g, float b,
        float a) {
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
            .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public boolean shouldRender(NightmareGateEntity gate, Frustum frustum, double x, double y, double z) {
        return gate.shouldRenderAtSqrDistance(gate.distanceToSqr(x, y, z)) && frustum.isVisible(gate.getBoundingBox().inflate(gate.width(), gate.height(),
            gate.width()));
    }

    @Override
    public ResourceLocation getTextureLocation(NightmareGateEntity gate) {
        return CORE;
    }
}
