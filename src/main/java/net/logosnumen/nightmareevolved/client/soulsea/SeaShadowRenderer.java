package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaShadowEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Sunny's shadow is his own silhouette in solid black. It always faces him and moves when he moves, a beat behind, as
 * shadows do.
 */
public class SeaShadowRenderer extends EntityRenderer<SeaShadowEntity> {
    private static final int BLACK = 0xE00A0A10;
    private final PlayerModel<AbstractClientPlayer> wide;
    private final PlayerModel<AbstractClientPlayer> slim;

    public SeaShadowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.wide = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SeaShadowEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        LocalPlayer owner = Minecraft.getInstance().player;
        if (owner == null) {
            return;
        }

        PlayerSkin skin = owner.getSkin();
        PlayerModel<AbstractClientPlayer> model = skin.model() == PlayerSkin.Model.SLIM ? this.slim : this.wide;
        float facing = (float)(Mth.atan2(owner.getZ() - entity.getZ(), owner.getX() - entity.getX()) * (180.0 / Math.PI)) - 90.0F;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - facing));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);
        model.attackTime = owner.getAttackAnim(partialTick);
        model.crouching = owner.isCrouching();
        model.riding = false;
        model.young = false;
        model.setupAnim(owner, owner.walkAnimation.position(partialTick), owner.walkAnimation.speed(partialTick), owner.tickCount + partialTick, 0.0F, 0.0F);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(skin.texture()));
        model.renderToBuffer(pose, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, BLACK);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaShadowEntity entity) {
        LocalPlayer owner = Minecraft.getInstance().player;
        return owner != null ? owner.getSkin().texture() : ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    }
}
