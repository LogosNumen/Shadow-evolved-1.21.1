package net.logosnumen.nightmareevolved.mixin;

import net.logosnumen.nightmareevolved.client.anim.ClientPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lets body animations and flight pose a humanoid once its ordinary posing is done. */
@Mixin(HumanoidModel.class)
public abstract class HumanoidPoseMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void dots$pose(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ClientPoses.pose((HumanoidModel<?>)(Object)this, entity, ageInTicks);
    }
}
