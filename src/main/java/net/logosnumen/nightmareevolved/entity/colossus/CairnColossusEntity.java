package net.logosnumen.nightmareevolved.entity.colossus;

import java.util.List;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Cairn Colossus: a giant of piled stone on two legs, its long arms nearly dragging the ground and its chest split
 * by glowing fissures. It smashes both fists down ahead of it, stamps the ground around it, sweeps an arm across its
 * front, and tears boulders out of its own body to throw at whatever keeps its distance. Its colouring decides the
 * stone it is made of and throws: grey stone, basalt, mossy stone, sandstone or obsidian.
 */
public class CairnColossusEntity extends ColossusEntity {
    private static final List<ColossusAttack> ATTACKS = List.of(
        ColossusAttack.of("slam", 40, 0.0, 7.5, 80, 4, 22),
        ColossusAttack.of("stomp", 30, 0.0, 5.0, 60, 3, 16),
        ColossusAttack.of("sweep", 26, 0.0, 8.5, 50, 4, 14),
        ColossusAttack.of("throw", 36, 9.0, 40.0, 110, 3, 22),
        ColossusAttack.of("roar", 40, 0.0, 40.0, 600, 0, 12)
    );
    private static final Vfx.Palette[] PALETTES = {Vfx.Palette.EARTH, Vfx.Palette.FIRE, Vfx.Palette.ACID, Vfx.Palette.LIGHT, Vfx.Palette.SHADOW};
    private static final Block[] STONES = {Blocks.COBBLESTONE, Blocks.BASALT, Blocks.MOSSY_COBBLESTONE, Blocks.SANDSTONE, Blocks.OBSIDIAN};

    public CairnColossusEntity(EntityType<? extends ColossusEntity> type, Level level) {
        super(type, level, ColossusKind.CAIRN);
    }

    @Override
    public List<ColossusAttack> attacks() {
        return ATTACKS;
    }

    @Override
    public Vfx.Palette palette() {
        return PALETTES[this.variant().palette()];
    }


    private Block stone() {
        return STONES[this.variant().palette()];
    }

    @Override
    @Nullable
    protected String rageAttack() {
        return "roar";
    }

    @Override
    protected void windup(ServerLevel level, ColossusAttack attack, @Nullable LivingEntity target) {
        switch (attack.id()) {
            case "slam" -> this.sound(level, SoundEvents.RAVAGER_ATTACK, 2.5F, 0.4F);
            case "sweep" -> this.sound(level, SoundEvents.IRON_GOLEM_ATTACK, 2.5F, 0.4F);
            case "throw" -> {
                this.sound(level, SoundEvents.STONE_BREAK, 2.5F, 0.5F);
                this.sound(level, SoundEvents.GRAVEL_BREAK, 2.5F, 0.5F);
            }
            case "roar" -> {
                this.sound(level, SoundEvents.RAVAGER_ROAR, 4.0F, 0.4F);
                this.sound(level, SoundEvents.WARDEN_ROAR, 2.0F, 0.6F);
            }
            default -> {
            }
        }
    }

    @Override
    protected void impact(ServerLevel level, ColossusAttack attack, int which, @Nullable LivingEntity target) {
        double s = this.getScale();
        switch (attack.id()) {
            case "slam" -> {
                Vec3 at = this.ahead(5.5);
                Quake.slam(level, this, at, 5.0 * s, this.blow(1.3F), 0.9, this.palette());
                Quake.spikeRing(level, this, at, 3.5 * s, 8, this.stone().defaultBlockState(), this.blow(0.5F), this.palette());
            }
            case "stomp" -> Quake.slam(level, this, this.position(), 6.0 * s, this.blow(0.9F), 0.6, this.palette());
            case "sweep" -> Quake.arc(level, this, 9.0 * s, 150.0, this.blow(1.0F), 1.8, 0.4, 2.0 * s, this.palette());
            case "throw" -> {
                if (target != null) {
                    Vec3 hand = this.position().add(this.facing().scale(2.5 * s)).add(0.0, 7.0 * s, 0.0);
                    ColossusBoulderEntity boulder = new ColossusBoulderEntity(level, this, new ItemStack(this.stone()), this.blow(1.1F), this.palette());
                    boulder.lob(hand, target);
                    level.addFreshEntity(boulder);
                    this.sound(level, SoundEvents.RAVAGER_ATTACK, 2.5F, 0.5F);
                }
            }
            case "roar" -> {
                Vfx.shockwave(level, this.position(), 16.0 * s, this.palette());
                for (LivingEntity victim : Quake.victims(level, this, new AABB(this.blockPosition()).inflate(20.0 * s))) {
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
            }
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
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
