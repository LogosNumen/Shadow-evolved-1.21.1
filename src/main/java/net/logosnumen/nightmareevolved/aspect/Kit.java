package net.logosnumen.nightmareevolved.aspect;

import java.util.List;
import net.logosnumen.nightmareevolved.anim.Poses;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

/**
 * The common moves abilities are made of, so each Aspect reads as what it does: a blink, a burst around a point, a cone
 * or line of harm, healing, pushes and pulls, buffs, and sounds. Everything is drawn in the Aspect's own colours.
 */
public final class Kit {
    private Kit() {
    }

    public static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, BlockPos.containing(at), sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void sound(ServerLevel level, Vec3 at, Holder<SoundEvent> sound, float volume, float pitch) {
        level.playSound(null, BlockPos.containing(at), sound.value(), SoundSource.PLAYERS, volume, pitch);
    }

    public static void buff(LivingEntity entity, Holder<MobEffect> effect, int seconds, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, seconds * 20, amplifier, false, true));
    }

    /** A quiet buff with no swirling particles: for effects that should feel innate. */
    public static void quiet(LivingEntity entity, Holder<MobEffect> effect, int ticks, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, ticks, amplifier, true, false));
    }

    /** The furthest free spot up to {@code range} blocks along the player's gaze (kept level-ish), or null. */
    public static Vec3 clearSpotAhead(ServerPlayer player, double range) {
        Vec3 from = player.position();
        Vec3 look = player.getLookAngle().multiply(1.0, 0.35, 1.0).normalize();
        Vec3 best = null;
        for (double d = 1.0; d <= range; d += 0.5) {
            Vec3 at = from.add(look.scale(d));
            if (player.level().noCollision(player, player.getBoundingBox().move(at.subtract(from)))) {
                best = at;
            } else {
                break;
            }
        }

        return best;
    }

    /** Moves the player to a spot in a streak of their Aspect's light. */
    public static void blink(Ctx ctx, Vec3 to) {
        ServerPlayer player = ctx.player();
        Vec3 from = player.position();
        Vfx.smoke(ctx.level(), from.add(0.0, 1.0, 0.0), ctx.palette().deep, 10, 0.5);
        Vfx.trail(ctx.level(), from, to, ctx.palette());
        player.teleportTo(to.x, to.y, to.z);
        player.fallDistance = 0.0F;
        Vfx.smoke(ctx.level(), to.add(0.0, 1.0, 0.0), ctx.palette().deep, 10, 0.5);
    }

    /** Harm to every foe within {@code radius} of a point, less at the edge, with a wave racing out. */
    public static List<LivingEntity> nova(Ctx ctx, Vec3 centre, double radius, float damage, boolean soul) {
        List<LivingEntity> caught = Targets.near(ctx.level(), ctx.player(), centre, radius);
        for (LivingEntity target : caught) {
            float falloff = (float)(1.0 - 0.4 * Math.min(1.0, target.position().distanceTo(centre) / radius));
            if (soul) {
                Targets.soulHit(ctx.level(), ctx.player(), target, damage * falloff);
            } else {
                Targets.hit(ctx.level(), ctx.player(), target, damage * falloff);
            }
        }

        Vfx.shockwave(ctx.level(), centre, radius, ctx.palette());
        return caught;
    }

    /** Harm across a cone in front of the player. */
    public static List<LivingEntity> cone(Ctx ctx, double reach, double halfAngle, float damage, boolean soul) {
        List<LivingEntity> caught = Targets.cone(ctx.level(), ctx.player(), reach, halfAngle);
        for (LivingEntity target : caught) {
            if (soul) {
                Targets.soulHit(ctx.level(), ctx.player(), target, damage);
            } else {
                Targets.hit(ctx.level(), ctx.player(), target, damage);
            }
        }

        Vfx.cone(ctx.level(), ctx.player().getEyePosition(), ctx.player().getLookAngle(), reach, halfAngle, ctx.palette(), (int)(reach * 6), false);
        return caught;
    }

    /** Harm along a straight line from the eye, drawn as a beam. */
    public static List<LivingEntity> bolt(Ctx ctx, double length, double width, float damage, boolean soul) {
        Vec3 eye = ctx.player().getEyePosition();
        Vec3 look = ctx.player().getLookAngle();
        List<LivingEntity> caught = Targets.line(ctx.level(), ctx.player(), length, width);
        for (LivingEntity target : caught) {
            if (soul) {
                Targets.soulHit(ctx.level(), ctx.player(), target, damage);
            } else {
                Targets.hit(ctx.level(), ctx.player(), target, damage);
            }
        }

        Vfx.beam(ctx.level(), eye.add(look.scale(0.8)), eye.add(look.scale(length)), ctx.palette(), 0.35);
        return caught;
    }

    public static void heal(ServerLevel level, LivingEntity target, float amount, Vfx.Palette palette) {
        target.heal(amount);
        Vfx.burst(level, target.getBoundingBox().getCenter(), palette, 8, 0.12);
    }

    /** Throws a creature away from a point (or toward it, for a negative strength). */
    public static void shove(LivingEntity target, Vec3 from, double strength, double lift) {
        Vec3 away = target.position().subtract(from).multiply(1.0, 0.0, 1.0);
        Vec3 dir = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize();
        target.setDeltaMovement(dir.x * strength, lift, dir.z * strength);
        target.hurtMarked = true;
    }

    /** Whether a spot lies in shadow: little light reaches it from sky or flame. */
    public static boolean shadowed(ServerLevel level, BlockPos pos) {
        int sky = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        return Math.max(sky, level.getBrightness(LightLayer.BLOCK, pos)) <= 7;
    }

    /** Plays a body animation on the player (as the ability's own clip does when fired). */
    public static void pose(ServerPlayer player, String clip) {
        Poses.play(player, clip);
    }
}
