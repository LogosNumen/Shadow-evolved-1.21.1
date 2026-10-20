package net.logosnumen.nightmareevolved.entity.colossus;

import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.colossus.Quake;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A boulder torn out of a colossus's own body and thrown: it trails dust and bursts where it lands. */
public final class ColossusBoulderEntity extends ThrowableItemProjectile {
    private float damage = 12.0F;
    private Vfx.Palette palette = Vfx.Palette.EARTH;

    public ColossusBoulderEntity(EntityType<? extends ColossusBoulderEntity> type, Level level) {
        super(type, level);
    }

    public ColossusBoulderEntity(Level level, LivingEntity owner, ItemStack block, float damage, Vfx.Palette palette) {
        super(Colossi.BOULDER.get(), owner, level);
        this.setItem(block);
        this.damage = damage;
        this.palette = palette;
    }

    /** Lobs it from a point so it comes down on (or near) a target, leading it a little. */
    public void lob(Vec3 from, Entity target) {
        Vec3 aim = target.position().add(target.getDeltaMovement().scale(8.0));
        double dx = aim.x - from.x;
        double dz = aim.z - from.z;
        double dy = aim.y - from.y;
        double flight = Math.max(12.0, Math.sqrt(dx * dx + dz * dz) / 1.3);
        double gravity = this.getDefaultGravity();
        this.setPos(from.x, from.y, from.z);
        // Drag takes about a tenth off over the flight; aim that much long.
        this.setDeltaMovement(dx / flight * 1.1, dy / flight + gravity * flight * 0.5, dz / flight * 1.1);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBBLESTONE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof ColossusEntity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.01, 0.0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            Vec3 at = result.getLocation();
            if (this.getOwner() instanceof LivingEntity owner) {
                Quake.slam(level, owner, at, 3.5, this.damage, 0.6, this.palette);
            } else {
                Quake.debris(level, at, 2.0, 16);
            }

            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
        tag.putString("Palette", this.palette.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        try {
            this.palette = Vfx.Palette.valueOf(tag.getString("Palette"));
        } catch (IllegalArgumentException e) {
            this.palette = Vfx.Palette.EARTH;
        }
    }
}
