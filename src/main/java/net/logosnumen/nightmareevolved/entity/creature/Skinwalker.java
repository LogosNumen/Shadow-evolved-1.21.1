package net.logosnumen.nightmareevolved.entity.creature;

import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A Skinwalker's vessel: the body of an Awakened whose soul core has gone to darkness, worn by a great abomination that
 * knows everything the body knew. It walks and looks like any traveller of the Dream Realm, and wears a traveller's
 * name, until it finds someone alone. Then it shows what it is.
 */
public final class Skinwalker extends DreamCreature {
    private static final EntityDataAccessor<Boolean> REVEALED = SynchedEntityData.defineId(Skinwalker.class, EntityDataSerializers.BOOLEAN);
    /** How many travellers' names it may wear (lang keys {@code creature.nightmare_evolved.skinwalker.name.N}). */
    private static final int NAMES = 12;
    /** How near a soul must be, and how far from any other, for it to show itself. */
    private static final double NEAR = 6.0;
    private static final double ALONE = 24.0;

    public Skinwalker(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(REVEALED, false);
    }

    @Override
    public boolean roused() {
        return this.entityData.get(REVEALED);
    }

    @Override
    protected void addGoals() {
        super.addGoals();
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal);
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::alone));
    }

    /** Whether a soul is near and has no one else near it. */
    private boolean alone(LivingEntity player) {
        if (player.distanceToSqr(this) > NEAR * NEAR) {
            return false;
        }

        return this.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(ALONE), other -> other != player && !other.isSpectator()).isEmpty();
    }

    private void reveal() {
        if (this.roused()) {
            return;
        }

        this.entityData.set(REVEALED, true);
        this.setCustomName(Component.translatable("entity.nightmare_evolved.skinwalker"));
        this.playSound(SoundEvents.WARDEN_ROAR, 0.8F, 1.6F);
        Vfx.smoke((ServerLevel)this.level(), this.position().add(0.0, 1.2, 0.0), Vfx.Palette.SHADOW.deep, 30, 0.5);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.getTarget() != null) {
            this.reveal();
        }

        if (this.tickCount == 1 && !this.hasCustomName()) {
            this.setCustomName(Component.translatable("creature.nightmare_evolved.skinwalker.name." + this.random.nextInt(NAMES)));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.reveal();
        }

        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Revealed", this.roused());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(REVEALED, tag.getBoolean("Revealed"));
    }
}
