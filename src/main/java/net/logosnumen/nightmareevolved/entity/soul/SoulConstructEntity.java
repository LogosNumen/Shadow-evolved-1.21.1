package net.logosnumen.nightmareevolved.entity.soul;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A body an Aspect makes in its bearer's own shape: one of Sunny's shadow avatars, a reflection of Mordret's, a puppet
 * of blood, a figure of light. It fights for its master (see {@code Bound}), looks like them, tinted by what it is made
 * of, and dissolves when its time is up or it is struck down. It leaves nothing behind.
 */
public class SoulConstructEntity extends PathfinderMob {
    /** What a construct is made of, which decides its tint and the light it leaves when it dissolves. */
    public enum Look {
        SHADOW(Vfx.Palette.SHADOW),
        MIRROR(Vfx.Palette.STEEL),
        BLOOD(Vfx.Palette.BLOOD),
        LIGHT(Vfx.Palette.LIGHT),
        STONE(Vfx.Palette.EARTH);

        public final Vfx.Palette palette;

        Look(Vfx.Palette palette) {
            this.palette = palette;
        }

        public static Look byOrdinal(int ordinal) {
            Look[] all = values();
            return all[Math.max(0, Math.min(all.length - 1, ordinal))];
        }
    }

    private static final EntityDataAccessor<Optional<UUID>> MASTER = SynchedEntityData.defineId(SoulConstructEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Byte> LOOK = SynchedEntityData.defineId(SoulConstructEntity.class, EntityDataSerializers.BYTE);

    public SoulConstructEntity(EntityType<? extends SoulConstructEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.ATTACK_DAMAGE, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MASTER, Optional.empty());
        builder.define(LOOK, (byte)0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Shapes a new construct after its master, made of {@code look}, and sets how strong it is. */
    public void setup(UUID master, Look look, double health, double damage) {
        this.entityData.set(MASTER, Optional.of(master));
        this.entityData.set(LOOK, (byte)look.ordinal());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        this.setHealth((float)health);
    }

    /** Whose shape this construct wears. */
    @Nullable
    public UUID master() {
        return this.entityData.get(MASTER).orElse(null);
    }

    public Look look() {
        return Look.byOrdinal(this.entityData.get(LOOK));
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel level) {
            Vfx.burst(level, this.getBoundingBox().getCenter(), this.look().palette, 16, 0.2);
        }

        super.die(source);
        this.discard();
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID master = this.master();
        if (master != null) {
            tag.putUUID("Master", master);
        }
        tag.putByte("Look", this.entityData.get(LOOK));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Master")) {
            this.entityData.set(MASTER, Optional.of(tag.getUUID("Master")));
        }

        this.entityData.set(LOOK, tag.getByte("Look"));
    }
}
