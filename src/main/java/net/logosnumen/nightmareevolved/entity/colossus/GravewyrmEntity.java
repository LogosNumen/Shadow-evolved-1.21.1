package net.logosnumen.nightmareevolved.entity.colossus;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.colossus.ColossusAttack;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.Quake;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Gravewyrm: a serpent of plates and spines as long as a street, which swims through the ground as easily as over
 * it. It lunges and bites, spits a burning spray, thrashes its coils about it, and dives under the ground to come up
 * beneath its prey. Only its head can be struck, and nothing reaches it while it is under the earth. Its hide may be
 * pale, verdant, crimson, ashen or abyssal.
 */
public class GravewyrmEntity extends ColossusEntity {
    private static final EntityDataAccessor<Boolean> DATA_BURROWED = SynchedEntityData.defineId(GravewyrmEntity.class, EntityDataSerializers.BOOLEAN);
    private static final List<ColossusAttack> ATTACKS = List.of(
        ColossusAttack.of("lunge", 28, 3.0, 14.0, 50, 4, 10),
        ColossusAttack.of("spit", 30, 6.0, 24.0, 70, 3, 14),
        ColossusAttack.of("burrow", 64, 9.0, 40.0, 160, 3, 14, 48),
        ColossusAttack.of("thrash", 30, 0.0, 6.0, 60, 3, 15)
    );
    private static final Vfx.Palette[] PALETTES = {Vfx.Palette.SOUL, Vfx.Palette.ACID, Vfx.Palette.BLOOD, Vfx.Palette.EARTH, Vfx.Palette.SHADOW};
    private static final Block[] SPIKES = {Blocks.BONE_BLOCK, Blocks.MOSS_BLOCK, Blocks.NETHER_WART_BLOCK, Blocks.COBBLED_DEEPSLATE, Blocks.SCULK};
    /** On clients: where the head has been, newest first, for the body to follow. */
    public final Deque<Vec3> trail = new ArrayDeque<>();
    private final Set<UUID> bitten = new HashSet<>();

    public GravewyrmEntity(EntityType<? extends ColossusEntity> type, Level level) {
        super(type, level, ColossusKind.GRAVEWYRM);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURROWED, false);
    }

    public boolean burrowed() {
        return this.entityData.get(DATA_BURROWED);
    }

    private void setBurrowed(boolean burrowed) {
        this.entityData.set(DATA_BURROWED, burrowed);
        this.noPhysics = burrowed;
        this.setNoGravity(burrowed);
    }

    /** How many body segments trail it. */
    public int segments() {
        return 12 + this.variant().pick("segments", 9);
    }

    @Override
    public List<ColossusAttack> attacks() {
        return ATTACKS;
    }

    @Override
    public Vfx.Palette palette() {
        return PALETTES[this.variant().palette()];
    }


    @Override
    @Nullable
    protected String rageAttack() {
        return "burrow";
    }

    @Override
    protected double cullingReach() {
        return 14.0 + this.segments() * 0.6;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.burrowed() && !pierces(source) || super.isInvulnerableTo(source);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.trackTrail();
        }
    }

    private void trackTrail() {
        Vec3 here = this.position();
        if (this.trail.isEmpty()) {
            // A newly seen wyrm lies straight out behind its head.
            Vec3 back = Vec3.directionFromRotation(0.0F, this.getYRot()).scale(-0.5);
            for (int i = 0; i < 160; i++) {
                this.trail.addLast(here.add(back.scale(i)));
            }
        }

        if (this.trail.peekFirst().distanceToSqr(here) > 0.04) {
            this.trail.addFirst(here);
            while (this.trail.size() > 400) {
                this.trail.removeLast();
            }
        }
    }

    @Override
    protected void windup(ServerLevel level, ColossusAttack attack, @Nullable LivingEntity target) {
        switch (attack.id()) {
            case "lunge" -> this.sound(level, SoundEvents.RAVAGER_ROAR, 2.5F, 0.7F);
            case "spit" -> this.sound(level, SoundEvents.LLAMA_SPIT, 3.0F, 0.4F);
            case "burrow" -> this.sound(level, SoundEvents.ROOTED_DIRT_BREAK, 3.0F, 0.4F);
            case "thrash" -> this.sound(level, SoundEvents.RAVAGER_ATTACK, 3.0F, 0.5F);
            default -> {
            }
        }

        this.bitten.clear();
    }

    @Override
    protected void actionTick(ServerLevel level, ColossusAttack attack, int tick, @Nullable LivingEntity target) {
        double s = this.getScale();
        if ("lunge".equals(attack.id()) && tick >= 10 && tick <= 17) {
            // The strike: a burst forward, the jaws closing on whatever is in front.
            Vec3 dash = this.facing().scale(tick == 17 ? 0.1 : 1.3);
            this.setDeltaMovement(dash.x, this.getDeltaMovement().y, dash.z);
            for (LivingEntity victim : Quake.victims(level, this, this.getBoundingBox().move(this.facing().scale(1.5 * s)).inflate(0.6))) {
                if (this.bitten.add(victim.getUUID())) {
                    Quake.hit(level, this, victim, this.blow(1.3F), this.facing().scale(1.2).add(0.0, 0.5, 0.0));
                }
            }
        } else if ("burrow".equals(attack.id()) && this.burrowed() && tick < 48) {
            // Under the ground it closes on its prey fast, leaving a churned furrow above it.
            if (target != null) {
                Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
                Vec3 step = to.length() < 0.7 ? to : to.normalize().scale(0.7);
                double x = this.getX() + step.x;
                double z = this.getZ() + step.z;
                this.setPos(x, Quake.groundAt(level, x, this.getY(), z), z);
                this.turnToward(target.getX(), target.getZ(), 30.0F);
            }

            this.setDeltaMovement(Vec3.ZERO);
            if (tick % 2 == 0) {
                Quake.debris(level, this.position(), 1.5 * s, 6);
            }

            if (tick % 8 == 0) {
                this.sound(level, SoundEvents.GRAVEL_BREAK, 2.5F, 0.4F);
            }
        }
    }

    @Override
    protected void impact(ServerLevel level, ColossusAttack attack, int which, @Nullable LivingEntity target) {
        double s = this.getScale();
        switch (attack.id()) {
            case "spit" -> {
                Vec3 mouth = this.position().add(this.facing().scale(2.0 * s)).add(0.0, 2.0 * s, 0.0);
                Vec3 aim = target != null ? target.getEyePosition().subtract(mouth).normalize() : this.facing();
                Vfx.cone(level, mouth, aim, 12.0 * s, 22.0, this.palette(), 60, false);
                for (LivingEntity victim : Quake.victims(level, this, new AABB(mouth, mouth).inflate(12.0 * s))) {
                    Vec3 to = victim.getEyePosition().subtract(mouth);
                    if (to.length() <= 12.0 * s && to.normalize().dot(aim) > Math.cos(Math.toRadians(25.0))) {
                        Quake.hit(level, this, victim, this.blow(0.9F), aim.scale(0.5));
                        victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    }
                }
            }
            case "burrow" -> {
                if (which == 0) {
                    this.setBurrowed(true);
                    Quake.debris(level, this.position(), 3.0 * s, 30);
                    this.sound(level, SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.6F);
                } else {
                    this.setBurrowed(false);
                    Quake.slam(level, this, this.position(), 5.0 * s, this.blow(1.4F), 1.5, this.palette());
                    Quake.spikeRing(level, this, this.position(), 4.0 * s, 10, SPIKES[this.variant().palette()].defaultBlockState(), this.blow(0.6F), this.palette());
                    this.sound(level, SoundEvents.RAVAGER_ROAR, 4.0F, 0.5F);
                }
            }
            case "thrash" -> Quake.slam(level, this, this.position(), 6.5 * s, this.blow(1.0F), 0.9, this.palette());
            default -> {
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // A wyrm saved underground comes back up where it was.
        this.setBurrowed(false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.burrowed() && !this.acting()) {
            this.setBurrowed(false);
        }

        if (this.level().isClientSide && this.burrowed() && this.tickCount % 2 == 0) {
            float a = this.random.nextFloat() * Mth.TWO_PI;
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX() + Mth.cos(a), this.getY() + 0.2, this.getZ() + Mth.sin(a), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected SoundEvent stepSound() {
        return SoundEvents.SAND_STEP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }
}
