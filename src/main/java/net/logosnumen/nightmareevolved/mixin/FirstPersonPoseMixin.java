package net.logosnumen.nightmareevolved.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.logosnumen.nightmareevolved.client.anim.ClientPoses;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Moves the first-person hand with the local player's body animations. */
@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonPoseMixin {
    @Unique
    private boolean nightmare_evolved$moved;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void nightmare_evolved$moveHand(
        AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack,
        float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci
    ) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        this.nightmare_evolved$moved = ClientPoses.pushHand(poseStack, player, arm, partialTicks);
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void nightmare_evolved$restoreHand(
        AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack,
        float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci
    ) {
        if (this.nightmare_evolved$moved) {
            poseStack.popPose();
            this.nightmare_evolved$moved = false;
        }
    }
}
