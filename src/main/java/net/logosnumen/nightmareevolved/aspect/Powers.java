package net.logosnumen.nightmareevolved.aspect;

import net.logosnumen.nightmareevolved.colossus.Quake;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** What each Aspect's abilities do. Damage and reach grow with {@code power}: the Aspect's tier and the soul's rank. */
final class Powers {
    private Powers() {
    }

    // ---- Hollow Shade: a soul at home in shadow ------------------------------------------------------------------

    /** Shade Step: through the dark to a point up to ten blocks ahead, leaving smoke behind. */
    static boolean shadeStep(ServerPlayer player, ServerLevel level, double power) {
        Vec3 from = player.position();
        Vec3 look = player.getLookAngle().multiply(1.0, 0.35, 1.0).normalize();
        Vec3 best = null;
        for (double d = 1.0; d <= 10.0; d += 0.5) {
            Vec3 at = from.add(look.scale(d));
            if (level.noCollision(player, player.getBoundingBox().move(at.subtract(from)))) {
                best = at;
            } else {
                break;
            }
        }

        if (best == null || best.distanceTo(from) < 1.5) {
            return false;
        }

        Vfx.smoke(level, from.add(0.0, 1.0, 0.0), Vfx.Palette.SHADOW.deep, 12, 0.5);
        Vfx.trail(level, from, best, Vfx.Palette.SHADOW);
        player.teleportTo(best.x, best.y, best.z);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 0, false, false));
        Vfx.smoke(level, best.add(0.0, 1.0, 0.0), Vfx.Palette.SHADOW.deep, 10, 0.5);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 0.6F);
        return true;
    }

    /** Umbral Grasp: shadows reach out across a cone ahead, seize what they find and hold it fast. */
    static boolean umbralGrasp(ServerPlayer player, ServerLevel level, double power) {
        Vfx.cone(level, player.getEyePosition(), player.getLookAngle(), 7.0, 35.0, Vfx.Palette.SHADOW, 40, false);
        for (LivingEntity target : Targets.cone(level, player, 7.0, 35.0)) {
            Targets.hit(level, player, target, (float)(4.0 * power));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
            Vfx.smoke(level, target.position().add(0.0, 0.3, 0.0), Vfx.Palette.SHADOW.deep, 6, 0.4);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.6F, 1.4F);
        return true;
    }

    // ---- Ember Herald: a soul that burns ---------------------------------------------------------------------------

    /** Cinder Lash: a whip of embers across everything in front, setting it alight. */
    static boolean cinderLash(ServerPlayer player, ServerLevel level, double power) {
        Vfx.slash(level, player, 2.6, Vfx.Palette.FIRE, 140.0);
        for (LivingEntity target : Targets.cone(level, player, 4.5, 70.0)) {
            Targets.hit(level, player, target, (float)(5.0 * power));
            target.igniteForSeconds(4.0F);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    /** Dawn Pyre: marks a place with a circle of fire, which a breath later erupts into a pillar of flame. */
    static boolean dawnPyre(ServerPlayer player, ServerLevel level, double power) {
        Vec3 eye = player.getEyePosition();
        HitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(24.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 at = hit.getLocation();
        Vec3 ground = new Vec3(at.x, Quake.groundAt(level, at.x, at.y + 1.0, at.z), at.z);
        Vfx.sigil(level, ground, 3.0, Vfx.Palette.FIRE, 20);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 0.7F);
        Vfx.later(15, () -> {
            Vfx.pillar(level, ground, 8.0, Vfx.Palette.FIRE);
            Vfx.shockwave(level, ground, 3.5, Vfx.Palette.FIRE);
            for (LivingEntity target : Targets.near(level, player, ground, 3.0)) {
                Targets.hit(level, player, target, (float)(10.0 * power));
                target.igniteForSeconds(5.0F);
                target.push(0.0, 0.6, 0.0);
                target.hurtMarked = true;
            }

            level.playSound(null, BlockPos.containing(ground), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        });
        return true;
    }

    // ---- Iron Penitent: a soul that endures -----------------------------------------------------------------------

    /** Penitent's Guard: braces body and soul; for a while blows land softly and a shell of essence soaks the rest. */
    static boolean penitentsGuard(ServerPlayer player, ServerLevel level, double power) {
        int seconds = 6 + (int)Math.round(2.0 * power);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, seconds * 20, power >= 2.0 ? 2 : 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, seconds * 20, Math.min(4, (int)power), false, true));
        Vfx.dome(level, player.position(), 1.8, Vfx.Palette.STEEL, 48);
        Vfx.ring(level, player.position().add(0.0, 0.1, 0.0), 1.6, Vfx.Palette.STEEL, 32);
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
        return true;
    }

    /** Shackle Break: tears free of whatever slows or binds them and brings both fists down, throwing back all near. */
    static boolean shackleBreak(ServerPlayer player, ServerLevel level, double power) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
        Vec3 at = player.position();
        for (LivingEntity target : Targets.near(level, player, at, 4.5)) {
            Vec3 away = target.position().subtract(at).multiply(1.0, 0.0, 1.0);
            Vec3 dir = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize();
            Targets.hit(level, player, target, (float)(7.0 * power));
            target.push(dir.x * 0.8, 0.25, dir.z * 0.8);
            target.hurtMarked = true;
        }

        Vfx.shockwave(level, at, 4.5, Vfx.Palette.STEEL);
        Quake.debris(level, at, 3.5, 30);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.5F);
        level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    // ---- Tide Weaver: a soul that moves like water ----------------------------------------------------------------

    /** Undertow: a current that drags every foe nearby in toward the weaver and leaves them stumbling. */
    static boolean undertow(ServerPlayer player, ServerLevel level, double power) {
        Vec3 at = player.position();
        var caught = Targets.near(level, player, at, 8.0);
        if (caught.isEmpty()) {
            return false;
        }

        Vfx.vortex(level, at, 6.0, 1.5, Vfx.Palette.FROST, 0.0, 0);
        for (LivingEntity target : caught) {
            // The blow first, so its knockback (away from the weaver) doesn't undo the current's pull.
            Targets.hit(level, player, target, (float)(2.0 * power));
            Vec3 pull = at.subtract(target.position()).multiply(1.0, 0.0, 1.0);
            double d = pull.length();
            if (d > 1.5) {
                Vec3 v = pull.scale(Math.min(1.4, d * 0.22) / d);
                target.setDeltaMovement(v.x, 0.2, v.z);
                target.hurtMarked = true;
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
            Vfx.trail(level, target.position(), at, Vfx.Palette.FROST);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    /** Riptide Lance: water drawn into a spear and hurled straight ahead, through everything in its path. */
    static boolean riptideLance(ServerPlayer player, ServerLevel level, double power) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vfx.beam(level, eye.add(look.scale(0.8)), eye.add(look.scale(16.0)), Vfx.Palette.FROST, 0.35);
        for (LivingEntity target : Targets.line(level, player, 16.0, 1.2)) {
            Targets.hit(level, player, target, (float)(8.0 * power));
            target.push(look.x * 0.8, 0.25, look.z * 0.8);
            target.hurtMarked = true;
            Vfx.burst(level, target.getBoundingBox().getCenter(), Vfx.Palette.FROST, 10, 0.25);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
        return true;
    }

    // ---- Rune Scribe: a soul that writes the world -----------------------------------------------------------------

    /** Warding Sigil: writes a circle at their feet that, for six seconds, mends the scribe and sears foes within. */
    static boolean wardingSigil(ServerPlayer player, ServerLevel level, double power) {
        Vec3 at = new Vec3(player.getX(), Quake.groundAt(level, player.getX(), player.getY(), player.getZ()), player.getZ());
        Vfx.sigil(level, at, 3.5, Vfx.Palette.RUNE, 120);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
        Vfx.over(120, tick -> {
            if (tick % 20 != 0 || !player.isAlive() || player.level() != level) {
                return;
            }

            if (player.position().distanceTo(at) <= 3.5) {
                player.heal((float)(1.0 + power));
            }

            for (LivingEntity target : Targets.near(level, player, at, 3.5)) {
                Targets.hit(level, player, target, (float)(2.0 * power));
                Vfx.glyph(level, target.getBoundingBox().getCenter(), Vfx.Palette.RUNE.bright);
            }

            Vfx.glyphs(level, at.add(0.0, 0.2, 0.0), 3.0, Vfx.Palette.RUNE, 6);
        });
        return true;
    }

    /** Glyph Volley: a handful of burning glyphs, each finding its own foe among the nearest. */
    static boolean glyphVolley(ServerPlayer player, ServerLevel level, double power) {
        var targets = Targets.nearest(level, player, 16.0, 3 + (int)power);
        if (targets.isEmpty()) {
            return false;
        }

        Vec3 from = player.getEyePosition().add(0.0, 0.3, 0.0);
        for (int i = 0; i < targets.size(); i++) {
            LivingEntity target = targets.get(i);
            Vfx.later(2 + i * 3, () -> {
                if (!target.isAlive()) {
                    return;
                }

                Vec3 to = target.getBoundingBox().getCenter();
                Vfx.beam(level, from, to, Vfx.Palette.RUNE, 0.4);
                Targets.hit(level, player, target, (float)(5.0 * power));
                level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 1.6F);
            });
        }

        Vfx.glyphs(level, from, 1.2, Vfx.Palette.RUNE, 8);
        return true;
    }
}
