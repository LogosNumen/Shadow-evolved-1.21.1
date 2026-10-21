package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.UUID;
import net.logosnumen.nightmareevolved.entity.soulsea.VesselEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A body left behind by its soul, in its owner's own shape: seated with its head bowed while the mind is in its Soul
 * Sea, breathing slowly, or lying asleep in bed while the soul dreams.
 */
public class VesselRenderer extends LivingEntityRenderer<VesselEntity, PlayerModel<VesselEntity>> {
    private final PlayerModel<VesselEntity> wide;
    private final PlayerModel<VesselEntity> slim;

    public VesselRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.4F);
        this.wide = this.model;
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    private static PlayerSkin skin(VesselEntity vessel) {
        UUID owner = vessel.owner();
        if (owner == null) {
            return DefaultPlayerSkin.get(net.minecraft.Util.NIL_UUID);
        }

        PlayerInfo info = Minecraft.getInstance().getConnection() == null ? null : Minecraft.getInstance().getConnection().getPlayerInfo(owner);
        return info != null ? info.getSkin() : DefaultPlayerSkin.get(owner);
    }

    @Override
    public void render(VesselEntity vessel, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        PlayerSkin skin = skin(vessel);
        this.model = skin.model() == PlayerSkin.Model.SLIM ? this.slim : this.wide;
        this.model.riding = !vessel.dreaming();
        super.render(vessel, yaw, partialTick, pose, buffers, light);
    }

    @Override
    protected void setupRotations(VesselEntity vessel, PoseStack pose, float bob, float yaw, float partialTick, float scale) {
        super.setupRotations(vessel, pose, bob, yaw, partialTick, scale);
        if (vessel.dreaming()) {
            if (!vessel.isSleeping()) {
                // No bed: asleep where it lay, on the ground.
                pose.translate(0.0F, 0.15F, 0.0F);
                pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
                pose.translate(0.0F, -0.95F, 0.0F);
            }

            return;
        }

        pose.translate(0.0F, -0.6F, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(4.0F + 1.5F * Mth.sin((vessel.tickCount + partialTick) * 0.05F)));
    }

    @Override
    protected RenderType getRenderType(VesselEntity vessel, boolean visible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(vessel));
    }

    @Override
    protected boolean shouldShowName(VesselEntity vessel) {
        return vessel.hasCustomName() && this.entityRenderDispatcher.distanceToSqr(vessel) < 8.0 * 8.0;
    }

    @Override
    public ResourceLocation getTextureLocation(VesselEntity vessel) {
        return skin(vessel).texture();
    }
}
