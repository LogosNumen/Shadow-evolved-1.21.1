package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaMemoryEntity;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * A Memory circling over the water as a little light: a soft glow in the colour of its Rank, and the Memory itself
 * turning slowly inside it. One that is summoned leaves only a dim, small echo of itself behind.
 */
public class SeaMemoryRenderer extends EntityRenderer<SeaMemoryEntity> {
    private static final RenderType GLOW = RenderType.eyes(NightmareEvolved.id("textures/environment/soul_core.png"));
    private final ItemRenderer items;

    public SeaMemoryRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.items = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SeaMemoryEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        ItemStack stack = entity.item();
        if (stack.isEmpty()) {
            return;
        }

        float time = entity.tickCount + partialTick;
        boolean out = entity.out();
        MemoryData data = SoulMemories.data(stack);
        Integer colour = Tier.byOrdinal(data == null ? 0 : data.rank()).colour.getColor();
        int rgb = colour == null ? 0xFFFFFF : colour;
        float glow = out ? 0.15F : 0.55F + 0.1F * Mth.sin(time * 0.1F + entity.getId());
        pose.pushPose();
        pose.translate(0.0F, 0.35F, 0.0F);
        pose.pushPose();
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        VertexConsumer consumer = buffers.getBuffer(GLOW);
        float size = out ? 0.4F : 1.1F;
        float r = (rgb >> 16 & 255) / 255.0F * glow;
        float g = (rgb >> 8 & 255) / 255.0F * glow;
        float b = (rgb & 255) / 255.0F * glow;
        PoseStack.Pose at = pose.last();
        consumer.addVertex(at, -size, -size, 0.0F).setColor(r, g, b, 1.0F).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(at, 0, 0, 1);
        consumer.addVertex(at, size, -size, 0.0F).setColor(r, g, b, 1.0F).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(at, 0, 0, 1);
        consumer.addVertex(at, size, size, 0.0F).setColor(r, g, b, 1.0F).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(at, 0, 0, 1);
        consumer.addVertex(at, -size, size, 0.0F).setColor(r, g, b, 1.0F).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(at, 0, 0, 1);
        pose.popPose();
        pose.mulPose(Axis.YP.rotation(time * 0.02F));
        float scale = out ? 0.5F : 1.1F;
        pose.scale(scale, scale, scale);
        this.items.renderStatic(stack, ItemDisplayContext.GROUND, out ? LightTexture.pack(2, 0) : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose,
            buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaMemoryEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
