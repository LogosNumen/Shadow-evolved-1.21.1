package net.logosnumen.nightmareevolved.entity.colossus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.colossus.ColossusAttack;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.Quake;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Ossuary Behemoth: the bare skeleton of a four-legged beast the size of a hall, still walking, a light burning in
 * the cage of its ribs. It lowers its horns and charges through everything in its path, tosses what is close with its
 * skull, rears and brings its forelegs down, and drives a line of bone spikes up out of the ground toward its prey.
 * Its bones may be bleached, ashen, black, blood-stained or rimed with frost.
 */
public class OssuaryBehemothEntity extends ColossusEntity {
    private static final List<ColossusAttack> ATTACKS = List.of(
        ColossusAttack.of("charge", 52, 8.0, 30.0, 120, 3, 14),
        ColossusAttack.of("gore", 22, 0.0, 6.5, 40, 4, 11),
        ColossusAttack.of("rear", 36, 0.0, 7.5, 80, 3, 22),
        ColossusAttack.of("spikes", 34, 5.0, 26.0, 90, 3, 16),
        ColossusAttack.of("howl", 40, 0.0, 40.0, 600, 0, 12, 20)
    );
    private static final Vfx.Palette[] PALETTES = {Vfx.Palette.SOUL, Vfx.Palette.EARTH, Vfx.Palette.SHADOW, Vfx.Palette.BLOOD, Vfx.Palette.FROST};
    private static final int CHARGE_END = 40;
    private final Set<UUID> trampled = new HashSet<>();

    public OssuaryBehemothEntity(EntityType<? extends ColossusEntity> type, Level level) {
        super(type, level, ColossusKind.OSSUARY);
    }

    @Override
    public List<ColossusAttack> attacks() {
        return ATTACKS;
    }

    @Override
    public Vfx.Palette palette() {
        return PALETTES[this.variant().palette()];
    }


    private BlockState spike() {
        return this.variant().palette() == 4 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BONE_BLOCK.defaultBlockState();
    }

    @Override
    @Nullable
    protected String rageAttack() {
        return "howl";
    }

    @Override
    protected void windup(ServerLevel level, ColossusAttack attack, @Nullable LivingEntity target) {
        switch (attack.id()) {
            case "charge" -> this.sound(level, SoundEvents.RAVAGER_ROAR, 3.0F, 0.5F);
            case "gore" -> this.sound(level, SoundEvents.ZOGLIN_ATTACK, 2.5F, 0.4F);
            case "rear" -> this.sound(level, SoundEvents.SKELETON_HORSE_AMBIENT, 3.0F, 0.4F);
            case "spikes" -> this.sound(level, SoundEvents.BONE_BLOCK_BREAK, 3.0F, 0.4F);
            case "howl" -> {
                this.sound(level, SoundEvents.WOLF_HOWL, 4.0F, 0.35F);
                this.sound(level, SoundEvents.WARDEN_ROAR, 2.0F, 0.8F);
            }
            default -> {
            }
        }

        this.trampled.clear();
    }

    /** The charge: from the first impact tick it runs straight ahead, steering a little, through whatever is there. */
    @Override
    protected void actionTick(ServerLevel level, ColossusAttack attack, int tick, @Nullable LivingEntity target) {
        if (!"charge".equals(attack.id()) || tick < attack.firstImpact() || tick > CHARGE_END) {
            return;
        }

        if (target != null) {
            this.turnToward(target.getX(), target.getZ(), 2.0F);
        }

        double s = this.getScale();
        Vec3 run = this.facing().scale(tick == CHARGE_END ? 0.0 : 0.85);
        this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
        AABB front = this.getBoundingBox().move(this.facing().scale(1.5 * s)).inflate(0.5);
        for (LivingEntity victim : Quake.victims(level, this, front)) {
            if (this.trampled.add(victim.getUUID())) {
                Vec3 side = victim.position().subtract(this.position()).normalize();
                Quake.hit(level, this, victim, this.blow(1.4F), new Vec3(side.x * 1.6 + run.x, 0.8, side.z * 1.6 + run.z));
            }
        }

        if (tick % 2 == 0) {
            Quake.debris(level, this.position(), this.getBbWidth() * 0.6, 6);
        }

        if (tick % 6 == 0) {
            this.sound(level, SoundEvents.RAVAGER_STEP, 3.0F, 0.5F);
        }
    }

    @Override
    protected void impact(ServerLevel level, ColossusAttack attack, int which, @Nullable LivingEntity target) {
        double s = this.getScale();
        switch (attack.id()) {
            case "gore" -> Quake.arc(level, this, 6.5 * s, 100.0, this.blow(1.1F), 0.8, 1.2, 2.5 * s, this.palette());
            case "rear" -> Quake.slam(level, this, this.ahead(4.5), 5.0 * s, this.blow(1.3F), 0.8, this.palette());
            case "spikes" -> {
                Vec3 toward = target != null ? target.position().subtract(this.position()) : this.facing();
                Quake.spikeLine(level, this, this.ahead(2.0), toward, 12, 1.7, this.spike(), this.blow(0.8F), this.palette());
            }
            case "howl" -> {
                Quake.spikeRing(level, this, this.position(), (which == 0 ? 5.0 : 9.0) * s, which == 0 ? 10 : 16, this.spike(), this.blow(0.6F), this.palette());
                if (which == 0) {
                    for (LivingEntity victim : Quake.victims(level, this, new AABB(this.blockPosition()).inflate(20.0 * s))) {
                        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected SoundEvent stepSound() {
        return SoundEvents.RAVAGER_STEP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_HORSE_DEATH;
    }
}
