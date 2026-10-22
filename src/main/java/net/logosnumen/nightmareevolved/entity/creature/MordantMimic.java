package net.logosnumen.nightmareevolved.entity.creature;

import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The Mordant Mimic of Shipwreck Island: a long chest of dark wood bound with dull metal, its lid flat, large enough to
 * hold a man. It waits as a chest. Open it, strike it or come too close, and it wakes: long, emaciated limbs unfold from
 * it, and it swallows its prey whole into its jagged maw, healing as it feeds. Left alone, it folds itself away again.
 */
public final class MordantMimic extends DreamCreature {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(MordantMimic.class, EntityDataSerializers.BOOLEAN);
    /** How long it stays awake with no one to eat. */
    private static final int SETTLE = 400;
    /** How long it holds what it has swallowed. */
    private static final int HOLD = 50;
    private int idle;
    @Nullable
    private UUID swallowed;
    private int held;

    public MordantMimic(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AWAKE, false);
    }

    @Override
    public boolean roused() {
        return this.entityData.get(AWAKE);
    }

    private void wake() {
        if (!this.roused()) {
            this.entityData.set(AWAKE, true);
            this.setCustomNameVisible(this.hasCustomName());
            this.playSound(SoundEvents.CHEST_OPEN, 1.5F, 0.5F);
        }

        this.idle = 0;
    }

    @Override
    public boolean isImmobile() {
        return !this.roused() || super.isImmobile();
    }

    @Override
    public boolean shouldShowName() {
        return this.roused() && super.shouldShowName();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Trying to open the chest is the last thing anyone does with it.
        if (!this.level().isClientSide) {
            this.wake();
            this.setTarget(player);
            this.forceMove("swallow");
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.wake();
        }

        return hurt;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel)this.level();
        if (this.getTarget() != null) {
            this.wake();
        } else if (this.roused() && this.swallowed == null && ++this.idle > SETTLE) {
            this.entityData.set(AWAKE, false);
            this.playSound(SoundEvents.CHEST_CLOSE, 1.2F, 0.5F);
        }

        if (this.swallowed != null) {
            Entity prey = level.getEntity(this.swallowed);
            if (!(prey instanceof LivingEntity living) || !living.isAlive() || ++this.held > HOLD) {
                this.spitOut(prey);
            } else {
                living.teleportTo(this.getX(), this.getY() + 0.3, this.getZ());
                if (this.held % 10 == 0) {
                    living.hurt(this.damageSources().mobAttack(this), this.blow(0.6F));
                    this.heal(this.blow(0.6F));
                }
            }
        } else if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
        }
    }

    private void spitOut(@Nullable Entity prey) {
        if (prey != null) {
            prey.setPos(this.getX(), this.getY() + 1.2, this.getZ());
            prey.setDeltaMovement(this.getLookAngle().scale(0.8).add(0.0, 0.4, 0.0));
            prey.hurtMarked = true;
        }

        this.swallowed = null;
        this.held = 0;
    }

    @Override
    protected void special(ServerLevel level, Move move, int which, @Nullable LivingEntity target) {
        if ("swallow".equals(move.id()) && target != null && this.swallowed == null && this.distanceToTarget(target) <= move.reach() + 0.5) {
            this.swallowed = target.getUUID();
            this.held = 0;
            Vfx.burst(level, target.position().add(0.0, 1.0, 0.0), Vfx.Palette.BLOOD, 20, 0.2);
            this.playSound(SoundEvents.CHEST_CLOSE, 2.0F, 0.4F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Awake", this.roused());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(AWAKE, tag.getBoolean("Awake"));
    }
}
