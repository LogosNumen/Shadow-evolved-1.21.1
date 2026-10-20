package net.logosnumen.nightmareevolved.entity.colossus;

import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.colossus.ColossusAttack;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
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
import net.minecraft.world.phys.Vec3;

/**
 * The Chainbound Colossus: an armoured giant with nothing but dark inside its armour and two lights behind its visor,
 * wrapped in the chains that once held it. It carries a greatsword, a warhammer or a halberd, and sometimes a shield.
 * It cleaves a furrow through the ground in front of it, sweeps its weapon in a half circle, hooks distant prey with a
 * chain and drags it in, and drives its weapon into the earth so chains burst up all around. Its armour may be iron,
 * bronze, blackened, gilded or blood-red.
 */
public class ChainboundColossusEntity extends ColossusEntity {
    public static final int GREATSWORD = 0;
    public static final int WARHAMMER = 1;
    public static final int HALBERD = 2;
    private static final List<ColossusAttack> ATTACKS = List.of(
        ColossusAttack.of("cleave", 40, 0.0, 10.0, 70, 4, 24),
        ColossusAttack.of("sweep", 30, 0.0, 9.5, 55, 4, 16),
        ColossusAttack.of("hook", 34, 8.0, 28.0, 100, 3, 14),
        ColossusAttack.of("stab", 44, 0.0, 10.0, 120, 2, 26),
        ColossusAttack.of("bash", 24, 0.0, 5.5, 50, 4, 12)
    );
    private static final Vfx.Palette[] PALETTES = {Vfx.Palette.STEEL, Vfx.Palette.EARTH, Vfx.Palette.SHADOW, Vfx.Palette.LIGHT, Vfx.Palette.BLOOD};

    public ChainboundColossusEntity(EntityType<? extends ColossusEntity> type, Level level) {
        super(type, level, ColossusKind.CHAINBOUND);
    }

    public static int weapon(ColossusVariant variant) {
        return variant.pick("weapon", 3);
    }

    public static boolean shield(ColossusVariant variant) {
        return variant.chance("shield", 0.35F);
    }

    @Override
    public List<ColossusAttack> attacks() {
        return ATTACKS;
    }

    @Override
    protected int weight(ColossusAttack attack) {
        return "bash".equals(attack.id()) && !shield(this.variant()) ? 0 : attack.weight();
    }

    @Override
    public Vfx.Palette palette() {
        return PALETTES[this.variant().palette()];
    }


    @Override
    @Nullable
    protected String rageAttack() {
        return "stab";
    }

    /** A halberd reaches further than a sword or hammer. */
    private double reach() {
        return (weapon(this.variant()) == HALBERD ? 1.2 : 1.0) * this.getScale();
    }

    @Override
    protected void windup(ServerLevel level, ColossusAttack attack, @Nullable LivingEntity target) {
        switch (attack.id()) {
            case "cleave", "stab" -> {
                this.sound(level, SoundEvents.CHAIN_BREAK, 3.0F, 0.5F);
                this.sound(level, SoundEvents.WITHER_SKELETON_AMBIENT, 2.5F, 0.5F);
            }
            case "hook" -> this.sound(level, SoundEvents.CHAIN_PLACE, 3.0F, 0.6F);
            case "bash" -> this.sound(level, SoundEvents.SHIELD_BLOCK, 3.0F, 0.5F);
            default -> {
            }
        }
    }

    @Override
    protected void impact(ServerLevel level, ColossusAttack attack, int which, @Nullable LivingEntity target) {
        double s = this.getScale();
        double reach = this.reach();
        switch (attack.id()) {
            case "cleave" -> {
                Quake.line(level, this, this.ahead(1.0), this.facing(), 11.0 * reach, 3.0 * s, this.blow(1.4F), 0.6, this.palette());
                Quake.spikeLine(level, this, this.ahead(2.0), this.facing(), 6, 1.6 * reach, Blocks.CHAIN.defaultBlockState(), this.blow(0.5F), this.palette());
                this.sound(level, SoundEvents.ANVIL_LAND, 2.5F, 0.5F);
            }
            case "sweep" -> Quake.arc(level, this, 10.0 * reach, 180.0, this.blow(1.1F), 1.8, 0.4, 3.0 * s, this.palette());
            case "hook" -> {
                if (target != null) {
                    Vec3 hand = this.position().add(this.facing().scale(1.5 * s)).add(0.0, 5.0 * s, 0.0);
                    Vfx.beam(level, hand, target.getEyePosition(), Vfx.Palette.STEEL, 0.4);
                    Vec3 pull = this.position().subtract(target.position()).normalize().scale(2.2);
                    Quake.hit(level, this, target, this.blow(0.5F), new Vec3(pull.x, 0.5, pull.z));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    this.sound(level, SoundEvents.CHAIN_HIT, 3.0F, 0.5F);
                }
            }
            case "stab" -> {
                Quake.slam(level, this, this.ahead(2.0), 7.0 * s, this.blow(1.2F), 1.0, this.palette());
                Quake.spikeRing(level, this, this.position(), 4.5 * s, 10, Blocks.CHAIN.defaultBlockState(), this.blow(0.5F), this.palette());
                Vfx.later(5, () -> Quake.spikeRing(level, this, this.position(), 8.5 * s, 18, Blocks.CHAIN.defaultBlockState(), this.blow(0.5F), this.palette()));
            }
            case "bash" -> Quake.arc(level, this, 5.5 * s, 70.0, this.blow(0.8F), 2.6, 0.5, 3.0 * s, this.palette());
            default -> {
            }
        }
    }

    @Override
    protected SoundEvent stepSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }
}
