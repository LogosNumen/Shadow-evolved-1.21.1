package net.logosnumen.nightmareevolved.entity.creature;

import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Whatever a creature spits, throws or looses at its prey: venom, a bone spear, a bolt of light. It flies straight,
 * trailing its colour, and is spent on whatever it strikes.
 */
public final class CreatureBolt extends Projectile {
    private static final EntityDataAccessor<Integer> COLOUR = SynchedEntityData.defineId(CreatureBolt.class, EntityDataSerializers.INT);
    private static final int LIFE = 80;
    private float damage = 4.0F;
    private int life;

    public CreatureBolt(EntityType<? extends CreatureBolt> type, Level level) {
        super(type, level);
    }

    /** Looses a bolt from a creature at its target. */
    public static void shoot(LivingEntity shooter, LivingEntity target, float damage, Vfx.Palette palette, double speed) {
        CreatureBolt bolt = new CreatureBolt(CreatureTypes.BOLT.get(), shooter.level());
        bolt.setOwner(shooter);
        bolt.damage = damage;
        bolt.entityData.set(COLOUR, palette.main);
        Vec3 from = shooter.getEyePosition().add(Vec3.directionFromRotation(0.0F, shooter.getYRot()).scale(shooter.getBbWidth() * 0.6));
        bolt.setPos(from);
        Vec3 to = target.getEyePosition().subtract(0.0, target.getBbHeight() * 0.25, 0.0).subtract(from);
        bolt.shoot(to.x, to.y, to.z, (float)speed, 1.5F);
        shooter.level().addFreshEntity(bolt);
    }

    public int colour() {
        return this.entityData.get(COLOUR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOUR, 0xFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !this.level().isClientSide) {
            this.onHit(hit);
            if (this.isRemoved()) {
                return;
            }
        }

        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.updateRotation();
        if (this.level() instanceof ServerLevel level) {
            Vfx.mote(level, this.position(), this.colour(), 0.0);
            if (++this.life > LIFE) {
                this.discard();
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = this.getOwner();
        if (owner instanceof DreamCreature shooter && target instanceof DreamCreature kin && kin.kind() == shooter.kind()) {
            return false;
        }

        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity owner = this.getOwner();
        result.getEntity().hurt(this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null), this.damage);
        if (owner instanceof DreamCreature creature && result.getEntity() instanceof LivingEntity hit) {
            creature.afflictFromShot(hit);
        }

        this.spend();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.spend();
    }

    private void spend() {
        if (this.level() instanceof ServerLevel level) {
            Vfx.burst(level, this.position(), Vfx.Palette.SHADOW, 6, 0.08);
        }

        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
        tag.putInt("Colour", this.colour());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        this.entityData.set(COLOUR, tag.getInt("Colour"));
    }
}
