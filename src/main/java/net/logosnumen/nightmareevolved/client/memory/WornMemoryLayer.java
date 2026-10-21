package net.logosnumen.nightmareevolved.client.memory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Set;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * What a player wears from their Memories, drawn on them: a garment hanging from the shoulders like a cloak (Dark
 * Wing's dragonfly wings, the Starlight Shard's white cloak, Ananke's black mantle, a woven cloak), and a crown, circlet
 * or mask on the head.
 */
public class WornMemoryLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    /** Memories worn on the head rather than over the shoulders. */
    private static final Set<String> HEAD = Set.of("weavers_mask", "crown_of_twilight", "crown_of_dawn", "dawn_shard");

    public WornMemoryLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    /** Adds the layer to a player renderer. */
    public static void add(PlayerRenderer renderer) {
        renderer.addLayer(new WornMemoryLayer(renderer));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
        float partialTick, float age, float headYaw, float headPitch) {
        List<String> looks = ClientWorn.of(player.getId());
        if (looks.isEmpty() || player.isInvisible()) {
            return;
        }

        for (String look : looks) {
            String id = look.contains(":") ? look.substring(0, look.indexOf(':')) : look;
            if (HEAD.contains(id)) {
                this.head(pose, buffers, light, player, id);
            } else {
                MemoryDef def = Memories.byId(id);
                if (def != null && def.kind == MemoryDef.Kind.GARMENT) {
                    this.cloak(pose, buffers, light, player, partialTick, look.replace(':', '_'));
                }
            }
        }
    }

    private void head(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, String id) {
        MemoryDef def = Memories.byId(id);
        if (def == null || !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !"weavers_mask".equals(id)) {
            return;
        }

        ItemStack stack = MemoryItems.display(def);
        pose.pushPose();
        this.getParentModel().getHead().translateAndRotate(pose);
        pose.translate(0.0F, -0.25F, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD, light, OverlayTexture.NO_OVERLAY, pose, buffers,
            player.level(), player.getId());
        pose.popPose();
    }

    /** Hangs a garment from the shoulders, swinging as the wearer moves, the way a cape does. */
    private void cloak(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float partialTick, String look) {
        ResourceLocation texture = NightmareEvolved.id("textures/entity/garment/" + look + ".png");
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 0.125F);
        double dx = Mth.lerp(partialTick, player.xCloakO, player.xCloak) - Mth.lerp(partialTick, player.xo, player.getX());
        double dy = Mth.lerp(partialTick, player.yCloakO, player.yCloak) - Mth.lerp(partialTick, player.yo, player.getY());
        double dz = Mth.lerp(partialTick, player.zCloakO, player.zCloak) - Mth.lerp(partialTick, player.zo, player.getZ());
        float body = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double sin = Mth.sin(body * (float)(Math.PI / 180.0));
        double cos = -Mth.cos(body * (float)(Math.PI / 180.0));
        float lift = Mth.clamp((float)dy * 10.0F, -6.0F, 32.0F);
        float swing = Mth.clamp((float)(dx * sin + dz * cos) * 100.0F, 0.0F, 150.0F);
        float sway = Mth.clamp((float)(dx * cos - dz * sin) * 100.0F, -20.0F, 20.0F);
        float bob = Mth.lerp(partialTick, player.oBob, player.bob);
        lift += Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * bob;
        if (player.isCrouching()) {
            lift += 25.0F;
        }

        pose.mulPose(Axis.XP.rotationDegrees(6.0F + swing / 2.0F + lift));
        pose.mulPose(Axis.ZP.rotationDegrees(sway / 2.0F));
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - sway / 2.0F));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(texture));
        this.getParentModel().renderCloak(pose, consumer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
