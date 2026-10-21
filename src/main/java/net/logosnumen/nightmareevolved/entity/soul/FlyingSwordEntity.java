package net.logosnumen.nightmareevolved.entity.soul;

import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A sword moved by will alone, as the King of Swords moves his: it seeks its master's foes, cuts them, wheels away and
 * comes back for another pass, and circles its master when there is nothing to fight. It passes through stone, and
 * falls apart into light when its time is up.
 */
public final class FlyingSwordEntity extends Projectile {
    private int life = 200;
    private float damage = 6.0F;
    private int rest;
    private float orbit;

    public FlyingSwordEntity(EntityType<? extends FlyingSwordEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setup(ServerPlayer owner, float damage, int life, float orbit) {
        this.setOwner(owner);
        this.damage = damage;
        this.life = life;
        this.orbit = orbit;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        if (--this.life <= 0 || !(this.getOwner() instanceof ServerPlayer owner) || !owner.isAlive() || owner.level() != level) {
            Vfx.burst(level, this.position(), Vfx.Palette.STEEL, 8, 0.15);
            this.discard();
            return;
        }

        if (this.rest > 0) {
            this.rest--;
        }

        LivingEntity target = this.seek(level, owner);
        Vec3 goal;
        if (target != null && this.rest == 0) {
            goal = target.getBoundingBox().getCenter();
        } else {
            double angle = (this.tickCount * 0.12) + this.orbit;
            goal = owner.position().add(Math.cos(angle) * 2.0, 1.6 + Math.sin(this.tickCount * 0.07 + this.orbit) * 0.3, Math.sin(angle) * 2.0);
        }

        Vec3 to = goal.subtract(this.position());
        double speed = target != null && this.rest == 0 ? 1.1 : 0.6;
        Vec3 velocity = this.getDeltaMovement().scale(0.7).add(to.normalize().scale(speed * 0.45));
        if (velocity.length() > speed) {
            velocity = velocity.normalize().scale(speed);
        }

        this.setDeltaMovement(velocity);
        this.setPos(this.position().add(velocity));
        this.updateRotation();
        if (target != null && this.rest == 0 && this.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
            target.invulnerableTime = 0;
            if (Targets.hit(level, owner, target, this.damage)) {
                level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7F, 1.4F);
                Vfx.spark(level, this.position(), Vfx.Palette.STEEL.bright, velocity.scale(-0.3));
            }

            this.rest = 12;
            this.setDeltaMovement(velocity.scale(-0.8).add(0.0, 0.4, 0.0));
        }

        if (this.tickCount % 2 == 0) {
            Vfx.mote(level, this.position(), Vfx.Palette.STEEL.main, 0.0);
        }
    }

    private LivingEntity seek(ServerLevel level, ServerPlayer owner) {
        List<LivingEntity> foes = level.getEntitiesOfClass(LivingEntity.class, new AABB(owner.blockPosition()).inflate(20.0), e -> Targets.foe(e, owner));
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity foe : foes) {
            double d = foe.distanceToSqr(this);
            if (d < bestDistance) {
                bestDistance = d;
                best = foe;
            }
        }

        return best;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", this.life);
        tag.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.life = tag.getInt("Life");
        this.damage = tag.getFloat("Damage");
    }
}
