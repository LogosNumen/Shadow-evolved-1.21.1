package net.logosnumen.nightmareevolved.entity.creature;

import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.life.Wildlife;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The Deathless of the Nightmare Desert, cursed out of death: black skeletons in rusted arms that rise from beneath the
 * dunes when the sun sets and fall on each other in endless battle. They pay outsiders no mind unless struck or crowded,
 * but cannot be got past without a fight. They have no souls and cannot be destroyed: broken, they lie in pieces a
 * while and rise again. At dawn they sink back under the sand.
 */
public final class Deathless extends DreamCreature {
    private static final EntityDataAccessor<Boolean> FALLEN = SynchedEntityData.defineId(Deathless.class, EntityDataSerializers.BOOLEAN);
    /** How long a broken Deathless lies in pieces before it rises again. */
    private static final int RISE = 300;
    private int fallenTicks;

    public Deathless(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
        this.xpReward = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FALLEN, false);
    }

    /** Lying in pieces. */
    @Override
    public boolean roused() {
        return this.entityData.get(FALLEN);
    }

    @Override
    protected void addGoals() {
        super.addGoals();
        this.targetSelector.removeAllGoals(goal -> true);
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, player -> player.distanceToSqr(this) < 25.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Deathless.class, 20, true, false,
            other -> other != this && !((Deathless)other).roused()));
    }

    @Override
    protected boolean canStrike(LivingEntity other) {
        return other instanceof Deathless foe && foe != this && !foe.roused() || super.canStrike(other);
    }

    @Override
    public boolean isImmobile() {
        return this.roused() || super.isImmobile();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.roused() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }

        // It cannot die: a blow that would end it only breaks it apart for a while.
        boolean hurt = super.hurt(source, Math.min(amount, this.getHealth() - 1.0F));
        if (hurt && this.getHealth() <= 1.0F && !this.level().isClientSide) {
            this.entityData.set(FALLEN, true);
            this.fallenTicks = 0;
            this.setTarget(null);
            this.getNavigation().stop();
            this.playSound(SoundEvents.SKELETON_DEATH, 1.0F, 0.6F);
            Vfx.smoke((ServerLevel)this.level(), this.position().add(0.0, 0.5, 0.0), 0xD8D0B8, 20, 0.6);
        }

        return hurt;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel)this.level();
        if (this.roused()) {
            this.setTarget(null);
            if (++this.fallenTicks > RISE) {
                this.entityData.set(FALLEN, false);
                this.setHealth(this.getMaxHealth());
                this.playSound(SoundEvents.SKELETON_AMBIENT, 1.0F, 0.5F);
                Vfx.smoke(level, this.position().add(0.0, 0.5, 0.0), 0xD8D0B8, 30, 0.8);
            }
        }

        // Day: back under the sand, leaving their own wars for the next night. Only a fight with the living keeps one up.
        if (DreamRealm.is(level) && this.tickCount % 40 == 0 && !Wildlife.night(level) && (!(this.getTarget() instanceof Player) || this.roused())) {
            Vfx.smoke(level, this.position().add(0.0, 0.3, 0.0), 0xE8E0C8, 30, 0.8);
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Fallen", this.roused());
        tag.putInt("FallenTicks", this.fallenTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(FALLEN, tag.getBoolean("Fallen"));
        this.fallenTicks = tag.getInt("FallenTicks");
    }
}
