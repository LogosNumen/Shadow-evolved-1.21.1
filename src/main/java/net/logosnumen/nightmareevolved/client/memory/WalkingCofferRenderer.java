package net.logosnumen.nightmareevolved.client.memory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.entity.memory.WalkingCofferEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.data.ModelData;

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
        // A coffer wearing a guise passes for the block it copied (the Mendacious enchantment keeps it in the stack).
        CustomData state = coffer.getItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        Block guise = state.contains("Guise") ? BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(state.copyTag().getString("Guise"))) : Blocks.AIR;
        float hop = Math.abs(Mth.sin(walk * 0.9F)) * 0.12F * speed;
        pose.pushPose();
        if (guise != Blocks.AIR) {
            pose.translate(0.0F, hop, 0.0F);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - body));
            pose.translate(-0.5F, 0.0F, -0.5F);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(guise.defaultBlockState(), pose, buffers, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
                null);
        } else {
            pose.translate(0.0F, 0.45F + hop, 0.0F);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - body));
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walk * 0.9F) * 8.0F * speed));
            pose.scale(1.5F, 1.5F, 1.5F);
            this.items.renderStatic(coffer.getItem(), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, coffer.level(), coffer.getId());
        }

        pose.popPose();
        super.render(coffer, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(WalkingCofferEntity coffer) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
