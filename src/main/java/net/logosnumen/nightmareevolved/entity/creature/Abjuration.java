package net.logosnumen.nightmareevolved.entity.creature;

import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Abjuration, the Cursed Demon of rejection and negation: a mountain of grey flesh overgrown with scarlet moss, a
 * hundred limbs, and eyes that open all over it and know when they are looked at. It denies what strikes it: whatever
 * is shot at it is unmade before it lands, and every third blow simply was never struck. Whoever looks at it, it looks
 * back at.
 */
public final class Abjuration extends DreamCreature {
    private int blows;

    public Abjuration(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
            && (source.is(DamageTypeTags.IS_PROJECTILE) || ++this.blows % 3 == 0)) {
            // Denied: the blow is proclaimed untrue, and so it is.
            Vfx.flash((ServerLevel)this.level(), this.position().add(0.0, this.getBbHeight() * 0.6, 0.0), Vfx.Palette.SHADOW, 2.0F);
            this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.5F, 0.4F);
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 20 != 0) {
            return;
        }

        // Its eyes look back at whoever looks at them.
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32.0), this::canStrike)) {
            Vec3 toward = this.getEyePosition().subtract(player.getEyePosition()).normalize();
            if (player.getViewVector(1.0F).dot(toward) > 0.9) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0), this);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1), this);
                if (this.getTarget() == null) {
                    this.setTarget(player);
                }
            }
        }
    }
}
