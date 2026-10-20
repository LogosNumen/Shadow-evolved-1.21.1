package net.logosnumen.nightmareevolved.aspect;

import java.util.Comparator;
import java.util.List;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Finding what an ability strikes, and striking it, so every ability treats friend and foe alike. */
final class Targets {
    private Targets() {
    }

    /** A foe of the player's: monsters, Nightmare Creatures, and anything already hunting them. Never another player. */
    static boolean foe(LivingEntity entity, ServerPlayer player) {
        return entity != player && entity.isAlive() && !(entity instanceof Player)
            && (entity instanceof Enemy || Creatures.ofNightmare(entity) || entity instanceof Mob mob && mob.getTarget() == player);
    }

    static List<LivingEntity> near(ServerLevel level, ServerPlayer player, Vec3 centre, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(radius),
            e -> foe(e, player) && e.position().distanceTo(centre) <= radius + e.getBbWidth() * 0.5);
    }

    /** Foes within a cone in front of the player: {@code halfAngle} degrees either side of where they look. */
    static List<LivingEntity> cone(ServerLevel level, ServerPlayer player, double reach, double halfAngle) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double cos = Math.cos(Math.toRadians(halfAngle));
        return near(level, player, eye, reach).stream().filter(e -> {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            return to.lengthSqr() < 1.0E-4 || to.normalize().dot(look) >= cos;
        }).toList();
    }

    /** Foes along a straight line from the eye, within {@code width} of it. */
    static List<LivingEntity> line(ServerLevel level, ServerPlayer player, double length, double width) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        return near(level, player, eye.add(look.scale(length * 0.5)), length * 0.5 + width).stream().filter(e -> {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(look);
            return along >= 0.0 && along <= length && to.subtract(look.scale(along)).length() <= width + e.getBbWidth() * 0.5;
        }).toList();
    }

    static List<LivingEntity> nearest(ServerLevel level, ServerPlayer player, double radius, int count) {
        return near(level, player, player.position(), radius).stream()
            .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .limit(count)
            .toList();
    }

    /** Hurts a foe as the player's own blow, so ranks and armour count. */
    static void hit(ServerLevel level, ServerPlayer player, LivingEntity target, float damage) {
        target.hurt(level.damageSources().playerAttack(player), damage);
    }
}
