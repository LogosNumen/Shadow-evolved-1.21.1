package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaHoardEntity;
import net.minecraft.client.renderer.LightTexture;
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

/** A Sovereign's store, hanging over the water: an old vault, turning slowly, lit from within. */
public class SeaHoardRenderer extends EntityRenderer<SeaHoardEntity> {
    private static final ItemStack VAULT = new ItemStack(Items.VAULT);
    private final ItemRenderer items;

    public SeaHoardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.items = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SeaHoardEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float time = entity.tickCount + partialTick;
        pose.pushPose();
        pose.translate(0.0F, 0.5F + Mth.sin(time * 0.04F) * 0.1F, 0.0F);
        pose.mulPose(Axis.YP.rotation(time * 0.01F));
        pose.scale(1.4F, 1.4F, 1.4F);
        this.items.renderStatic(VAULT, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaHoardEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
