package net.logosnumen.nightmareevolved.aspect.series;

import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

/**
 * Jest's Aspect, of the Ascended Rank (the novel never names it): mastery of other hearts. It stokes whatever a
 * creature feels, goads anything into attacking him, turns a foe's fury on another, and as a Saint makes him a horned,
 * goat-legged fiend. It works best when there are two enemies to turn on each other.
 */
public final class Jester {
    public static final Aspect ASPECT = Aspect.of("kindled_passions", Tier.ASCENDED, Vfx.Palette.BLOOD)
        .with(
            Ability.active("kindle", Stage.DORMANT, 6.0, 80, "point", Jester::kindle),
            Ability.active("provoke", Stage.AWAKENED, 10.0, 160, "roar", Jester::provoke),
            Ability.active("redirect", Stage.ASCENDED, 15.0, 200, "cast", Jester::redirect),
            Ability.transformation("fiend_of_wrath", 4.0, "transform_burst", Forms.of(new Forms.Shape(1.4, 16.0, 5.0, 0.1, 4.0, false)))
        )
        .flaw("bare_hands")
        .build();

    private Jester() {
    }

    /** Kindle: whatever the creature feels burns hotter. A hunter turns reckless and careless of its guard; prey panics. */
    private static boolean kindle(Ctx ctx) {
        LivingEntity seen = Targets.looked(ctx.player(), 20.0);
        if (seen == null) {
            return false;
        }

        if (seen instanceof Enemy) {
            Kit.quiet(seen, MobEffects.MOVEMENT_SPEED, 160, 0);
            Kit.quiet(seen, MobEffects.DAMAGE_BOOST, 160, 0);
            Afflictions.afflict(seen, ctx.player(), Afflictions.MARKED, 0.3, 8);
        } else if (seen instanceof PathfinderMob animal) {
            Vec3 away = animal.position().add(animal.position().subtract(ctx.player().position()).normalize().scale(10.0));
            animal.getNavigation().moveTo(away.x, away.y, away.z, 1.6);
        }

        Vfx.embers(ctx.level(), seen.getEyePosition(), Vfx.Palette.BLOOD, 8, 0.4);
        return true;
    }

    /** Provoke: every foe near him is seized by a smothering rage and comes for him, heedless of its guard. */
    private static boolean provoke(Ctx ctx) {
        List<LivingEntity> foes = Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 12.0);
        for (LivingEntity foe : foes) {
            if (foe instanceof Mob mob) {
                mob.setTarget(ctx.player());
            }

            Afflictions.afflict(foe, ctx.player(), Afflictions.MARKED, 0.25, 8);
        }

        Vfx.shockwave(ctx.level(), ctx.player().position(), 12.0, Vfx.Palette.BLOOD);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.RAVAGER_ROAR, 0.8F, 1.3F);
        return !foes.isEmpty();
    }

    /** Redirect: a foe's fury turns from him onto the creature nearest it, and that one answers in kind. */
    private static boolean redirect(Ctx ctx) {
        LivingEntity seen = Targets.looked(ctx.player(), 20.0);
        if (!(seen instanceof Mob mob)) {
            return false;
        }

        LivingEntity other = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : Targets.near(ctx.level(), ctx.player(), mob.position(), 16.0)) {
            double d = candidate.distanceToSqr(mob);
            if (candidate != mob && d < best) {
                best = d;
                other = candidate;
            }
        }

        if (other == null) {
            return false;
        }

        mob.setTarget(other);
        if (other instanceof Mob otherMob) {
            otherMob.setTarget(mob);
        }

        Vfx.beam(ctx.level(), mob.getEyePosition(), other.getEyePosition(), Vfx.Palette.BLOOD, 0.3);
        return true;
    }
}
