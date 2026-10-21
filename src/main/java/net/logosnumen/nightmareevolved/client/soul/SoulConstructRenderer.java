package net.logosnumen.nightmareevolved.client.soul;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.entity.soul.SoulConstructEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * Draws a soul construct in its master's own shape and skin, tinted by what it is made of: near-black for a shadow,
 * silver for a reflection, red for blood, pale gold for light, grey for stone. Shadows, reflections and light are
 * partly see-through.
 */
public class SoulConstructRenderer extends HumanoidMobRenderer<SoulConstructEntity, SoulConstructRenderer.TintedModel> {
    /** A player model that multiplies whatever colour it is drawn with by a tint set just before drawing. */
    public static class TintedModel extends PlayerModel<SoulConstructEntity> {
        private int tint = -1;

        public TintedModel(ModelPart root) {
            super(root, false);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int colour) {
            super.renderToBuffer(poseStack, buffer, light, overlay, FastColor.ARGB32.multiply(colour, this.tint));
        }
    }

    public SoulConstructRenderer(EntityRendererProvider.Context context) {
        super(context, new TintedModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    private static int tint(SoulConstructEntity.Look look) {
        return switch (look) {
            case SHADOW -> 0xD81C1A26;
            case MIRROR -> 0xB8D8E0F0;
            case BLOOD -> 0xE6B01818;
            case LIGHT -> 0x99FFF2C0;
            case STONE -> 0xFF8C847A;
        };
    }

    @Override
    public void render(SoulConstructEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        this.model.tint = tint(entity.look());
        super.render(entity, yaw, partialTick, poseStack, buffers, entity.look() == SoulConstructEntity.Look.LIGHT ? 0xF000F0 : light);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SoulConstructEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(SoulConstructEntity entity) {
        UUID master = entity.master();
        if (master == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }

        PlayerInfo info = Minecraft.getInstance().getConnection() == null ? null : Minecraft.getInstance().getConnection().getPlayerInfo(master);
        return info != null ? info.getSkin().texture() : DefaultPlayerSkin.get(master).texture();
    }
}
