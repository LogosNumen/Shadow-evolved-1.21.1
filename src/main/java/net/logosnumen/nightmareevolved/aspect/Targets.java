package net.logosnumen.nightmareevolved.aspect;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Finding what an ability strikes, and striking it, so every ability treats friend and foe alike. */
public final class Targets {
    /** How far outside a creature's body the crosshair may fall and still count as looking at it. */
    private static final double AIM_MARGIN = 0.35;

    private Targets() {
    }

    /**
     * A foe of the player's: monsters, Nightmare Creatures, and anything already hunting them. Never another player,
     * and never a creature bound to the player.
     */
    public static boolean foe(LivingEntity entity, ServerPlayer player) {
        return entity != player && entity.isAlive() && !(entity instanceof Player) && !Bound.servesOrAllied(entity, player)
            && (entity instanceof Enemy || Creatures.ofNightmare(entity) || entity instanceof Mob mob && mob.getTarget() == player);
    }

    /** An ally of the player's: other players nearby who are not attacking them, and creatures bound to them. */
    public static boolean ally(LivingEntity entity, ServerPlayer player) {
        return entity.isAlive() && (entity == player || entity instanceof Player other && !other.isSpectator() || Bound.servesOrAllied(entity, player));
    }

    public static List<LivingEntity> near(ServerLevel level, ServerPlayer player, Vec3 centre, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(radius),
            e -> foe(e, player) && e.position().distanceTo(centre) <= radius + e.getBbWidth() * 0.5);
    }

    public static List<LivingEntity> alliesNear(ServerLevel level, ServerPlayer player, Vec3 centre, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(radius),
            e -> ally(e, player) && e.position().distanceTo(centre) <= radius);
    }

    /** Foes within a cone in front of the player: {@code halfAngle} degrees either side of where they look. */
    public static List<LivingEntity> cone(ServerLevel level, ServerPlayer player, double reach, double halfAngle) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double cos = Math.cos(Math.toRadians(halfAngle));
        return near(level, player, eye, reach).stream().filter(e -> {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            return to.lengthSqr() < 1.0E-4 || to.normalize().dot(look) >= cos;
        }).toList();
    }

    /** Foes along a straight line from the eye, within {@code width} of it. */
    public static List<LivingEntity> line(ServerLevel level, ServerPlayer player, double length, double width) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        return near(level, player, eye.add(look.scale(length * 0.5)), length * 0.5 + width).stream().filter(e -> {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(look);
            return along >= 0.0 && along <= length && to.subtract(look.scale(along)).length() <= width + e.getBbWidth() * 0.5;
        }).toList();
    }

    public static List<LivingEntity> nearest(ServerLevel level, ServerPlayer player, double radius, int count) {
        return near(level, player, player.position(), radius).stream()
            .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .limit(count)
            .toList();
    }

    /**
     * The living thing under the player's crosshair within {@code range}, if the view to it is clear. The player's
     * own summoned and bound creatures are looked past, so they never stand between an ability and its mark.
     */
    @Nullable
    public static LivingEntity looked(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = block.getLocation();
        AABB box = player.getBoundingBox().expandTowards(limit.subtract(eye)).inflate(1.0);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, box,
            e -> e != player && e.isAlive() && EntitySelector.NO_SPECTATORS.test(e) && !Bound.servesOrAllied(e, player))) {
            // A little wider than the body itself: an ability's aim is forgiving where a sword's is not.
            AABB body = candidate.getBoundingBox().inflate(AIM_MARGIN);
            Vec3 at = body.contains(eye) ? eye : body.clip(eye, limit).orElse(null);
            if (at != null && eye.distanceToSqr(at) < bestDistance) {
                bestDistance = eye.distanceToSqr(at);
                best = candidate;
            }
        }

        return best;
    }

    /** Where the player's gaze meets a block (or the end of {@code range}). */
    public static BlockHitResult lookedBlock(ServerPlayer player, double range, ClipContext.Fluid fluids) {
        Vec3 eye = player.getEyePosition();
        return player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(range)), ClipContext.Block.COLLIDER, fluids, player));
    }

    /** Hurts a foe as the player's own blow, so ranks and armour count. */
    public static boolean hit(ServerLevel level, ServerPlayer player, LivingEntity target, float damage) {
        return target.hurt(level.damageSources().playerAttack(player), damage);
    }

    /** Hurts a foe's soul directly: armour does not help. */
    public static boolean soulHit(ServerLevel level, ServerPlayer player, LivingEntity target, float damage) {
        return target.hurt(level.damageSources().source(DamageTypes.INDIRECT_MAGIC, player, player), damage);
    }
}
