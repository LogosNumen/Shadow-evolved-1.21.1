package net.logosnumen.nightmareevolved.entity.creature;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The Undetectable Forest Maw: nothing at all to see, hear or sense until someone steps where it waits. Then the forest
 * floor gapes open into a maw, and roots reach out of it. Left alone long enough, it closes and is gone again.
 */
public final class ForestMaw extends DreamCreature {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(ForestMaw.class, EntityDataSerializers.BOOLEAN);
    /** How near someone must come before it opens. */
    private static final double NEAR = 3.5;
    /** How long it waits, open and with no one to eat, before it closes. */
    private static final int SETTLE = 200;
    private int idle;

    public ForestMaw(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OPEN, false);
    }

    @Override
    public boolean roused() {
        return this.entityData.get(OPEN);
    }

    @Override
    public int tint() {
        return this.roused() ? 0xFFFFFFFF : 0x00FFFFFF;
    }

    @Override
    public boolean shouldShowName() {
        return this.roused() && super.shouldShowName();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return this.roused() && super.isCurrentlyGlowing();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.roused() ? super.getAmbientSound() : null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.open();
        }

        return hurt;
    }

    private void open() {
        this.idle = 0;
        if (!this.roused()) {
            this.entityData.set(OPEN, true);
            Vfx.burst((ServerLevel)this.level(), this.position().add(0.0, 0.6, 0.0), Vfx.Palette.EARTH, 20, 0.3);
            this.playSound(SoundEvents.ROOTED_DIRT_BREAK, 2.0F, 0.5F);
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 0.7F);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!this.roused()) {
            Player near = this.level().getNearestPlayer(this, NEAR + this.getBbWidth() * 0.5);
            if (near != null && this.canStrike(near)) {
                this.open();
                this.setTarget(near);
            }
        } else if (this.getTarget() == null && ++this.idle > SETTLE) {
            this.entityData.set(OPEN, false);
            this.playSound(SoundEvents.ROOTED_DIRT_PLACE, 1.5F, 0.5F);
        }
    }
}
