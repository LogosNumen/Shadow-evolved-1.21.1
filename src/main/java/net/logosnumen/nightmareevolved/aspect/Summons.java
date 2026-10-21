package net.logosnumen.nightmareevolved.aspect;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.entity.soul.FlyingSwordEntity;
import net.logosnumen.nightmareevolved.entity.soul.SoulConstructEntity;
import net.logosnumen.nightmareevolved.entity.soul.SoulEntities;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** Bringing bodies into the world for an Aspect: constructs in the master's shape, flying swords, and puppets. */
public final class Summons {
    private Summons() {
    }

    /** A construct in the master's shape, fighting for them for {@code ticks} (or until dismissed, if negative). */
    public static SoulConstructEntity construct(Ctx ctx, SoulConstructEntity.Look look, Vec3 at, double health, double damage, int ticks, String kind) {
        SoulConstructEntity body = SoulEntities.CONSTRUCT.get().create(ctx.level());
        body.moveTo(at.x, at.y, at.z, ctx.player().getYRot(), 0.0F);
        body.setup(ctx.player().getUUID(), look, health, damage);
        ctx.level().addFreshEntity(body);
        Bound.bind(body, ctx.player(), ticks, kind, true);
        Vfx.burst(ctx.level(), body.getBoundingBox().getCenter(), look.palette, 14, 0.18);
        return body;
    }

    /** Spots in a ring around the player, for summoning several bodies at once. */
    public static Vec3 around(Ctx ctx, int index, int count, double radius) {
        double angle = Math.toRadians(ctx.player().getYRot()) + index * Math.PI * 2.0 / Math.max(1, count);
        return ctx.player().position().add(-Math.sin(angle) * radius, 0.0, Math.cos(angle) * radius);
    }

    public static FlyingSwordEntity sword(Ctx ctx, float damage, int ticks, float orbit) {
        FlyingSwordEntity sword = SoulEntities.FLYING_SWORD.get().create(ctx.level());
        Vec3 at = ctx.player().getEyePosition().add(ctx.player().getLookAngle().scale(0.8));
        sword.moveTo(at.x, at.y, at.z, ctx.player().getYRot(), ctx.player().getXRot());
        sword.setup(ctx.player(), damage, ticks, orbit);
        ctx.level().addFreshEntity(sword);
        return sword;
    }

    /** Dismisses every body of one kind the player has summoned. */
    public static void dismiss(Ctx ctx, String kind) {
        List<Mob> mine = Bound.of(ctx.level(), ctx.player(), 128.0, kind);
        mine.forEach(Bound::release);
    }

    /** The fragments a kill gives a soul that feeds on what it slays: a shard's worth for each of the creature's cores. */
    public static int fragmentsFor(LivingEntity victim, int soulLadder) {
        if (!Creatures.feedsSouls(victim)) {
            return 0;
        }

        return SoulRules.shardValue(Creatures.rankOf(victim).ordinal(), soulLadder) * Creatures.cores(victim);
    }
}
