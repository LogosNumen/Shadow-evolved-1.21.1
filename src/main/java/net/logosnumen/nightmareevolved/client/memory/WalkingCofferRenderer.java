package net.logosnumen.nightmareevolved.client.memory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.entity.memory.WalkingCofferEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

/** The Covetous Coffer walking after its owner: it rocks from side to side as it goes, and its lid chatters. */
public class WalkingCofferRenderer extends EntityRenderer<WalkingCofferEntity> {
    private final ItemRenderer items;

    public WalkingCofferRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.items = context.getItemRenderer();
        this.shadowRadius = 0.45F;
    }

    @Override
    public void render(WalkingCofferEntity coffer, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float walk = coffer.walkAnimation.position(partialTick);
        float speed = Math.min(1.0F, coffer.walkAnimation.speed(partialTick));
        float body = Mth.lerp(partialTick, coffer.yBodyRotO, coffer.yBodyRot);
        pose.pushPose();
        pose.translate(0.0F, 0.45F + Math.abs(Mth.sin(walk * 0.9F)) * 0.12F * speed, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - body));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walk * 0.9F) * 8.0F * speed));
        pose.scale(1.5F, 1.5F, 1.5F);
        this.items.renderStatic(coffer.getItem(), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, coffer.level(), coffer.getId());
        pose.popPose();
        super.render(coffer, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(WalkingCofferEntity coffer) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
