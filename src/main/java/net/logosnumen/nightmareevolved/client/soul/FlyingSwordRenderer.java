package net.logosnumen.nightmareevolved.client.soul;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.entity.soul.FlyingSwordEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Draws a flying sword as a blade pointing the way it flies. */
public class FlyingSwordRenderer extends EntityRenderer<FlyingSwordEntity> {
    private static final ItemStack BLADE = new ItemStack(Items.IRON_SWORD);
    private final ItemRenderer items;

    public FlyingSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.items = context.getItemRenderer();
    }

    @Override
    public void render(FlyingSwordEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) - 45.0F));
        poseStack.scale(1.4F, 1.4F, 1.4F);
        this.items.renderStatic(BLADE, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingSwordEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
