package net.logosnumen.nightmareevolved.aspect;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Creatures that fight for a player: shadows and reflections they summon, corpses raised as puppets, minds they have
 * taken. A bound creature never turns on its master or the master's other servants, hunts whatever its master fights,
 * and comes back to them when idle. When its time runs out, a summoned one fades and a taken one is let go.
 */
public final class Bound {
    private Bound() {
    }

    /**
     * Binds a creature to a player for {@code ticks} (or for good, if negative). {@code summoned} creatures fade when
     * released; others simply go back to themselves.
     */
    public static void bind(Mob mob, ServerPlayer owner, int ticks, String kind, boolean summoned) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Owner", owner.getUUID());
        tag.putLong("Until", ticks < 0 ? Long.MAX_VALUE : mob.level().getGameTime() + ticks);
        tag.putString("Kind", kind);
        tag.putBoolean("Summoned", summoned);
        mob.setData(ModAttachments.BOUND, tag);
        mob.setTarget(null);
        mob.setPersistenceRequired();
    }

    public static boolean bound(Entity entity) {
        return entity.hasData(ModAttachments.BOUND) && entity.getData(ModAttachments.BOUND).hasUUID("Owner");
    }

    @Nullable
    public static UUID owner(Entity entity) {
        return bound(entity) ? entity.getData(ModAttachments.BOUND).getUUID("Owner") : null;
    }

    public static String kind(Entity entity) {
        return bound(entity) ? entity.getData(ModAttachments.BOUND).getString("Kind") : "";
    }

    /** Whether a creature serves this player. */
    public static boolean servesOrAllied(LivingEntity entity, ServerPlayer player) {
        return player.getUUID().equals(owner(entity));
    }

    /** The player's bound creatures within {@code radius}, optionally of one kind. */
    public static List<Mob> of(ServerLevel level, ServerPlayer player, double radius, @Nullable String kind) {
        return level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius),
            m -> servesOrAllied(m, player) && (kind == null || kind.equals(kind(m))));
    }

    /** Ends a creature's binding now: a summoned one fades away, a taken one goes back to itself. */
    public static void release(Mob mob) {
        CompoundTag tag = mob.getData(ModAttachments.BOUND);
        boolean summoned = tag.getBoolean("Summoned");
        mob.removeData(ModAttachments.BOUND);
        mob.setTarget(null);
        if (summoned && mob.level() instanceof ServerLevel level) {
            Vfx.smoke(level, mob.getBoundingBox().getCenter(), 0x101018, 12, mob.getBbWidth() * 0.6);
            mob.discard();
        }
    }

    /** A bound creature never takes its master, or its master's other servants, as a target. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        UUID owner = owner(event.getEntity());
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null) {
            return;
        }

        if (owner != null && (owner.equals(target.getUUID()) || owner.equals(owner(target)))) {
            event.setCanceled(true);
            return;
        }

        UUID targetOwner = owner(target);
        if (targetOwner != null && targetOwner.equals(owner(event.getEntity()))) {
            event.setCanceled(true);
        }
    }

    /** Twice a second: lets bound creatures go when their time is up, and points them at their master's fight. */
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level) || mob.tickCount % 10 != 0 || !bound(mob)) {
            return;
        }

        CompoundTag tag = mob.getData(ModAttachments.BOUND);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(tag.getUUID("Owner"));
        if (level.getGameTime() >= tag.getLong("Until") || owner == null && tag.getBoolean("Summoned")) {
            release(mob);
            return;
        }

        if (owner == null || owner.level() != level) {
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || !Targets.foe(target, owner)) {
            LivingEntity next = pick(level, owner, mob);
            mob.setTarget(next);
            if (next == null && mob.distanceToSqr(owner) > 64.0) {
                mob.getNavigation().moveTo(owner, 1.2);
            }
        }

        if (tag.getBoolean("Summoned") && mob.distanceToSqr(owner) > 48.0 * 48.0) {
            mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    @Nullable
    private static LivingEntity pick(ServerLevel level, ServerPlayer owner, Mob mob) {
        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && Targets.foe(attacker, owner) && attacker.distanceToSqr(owner) < 32.0 * 32.0) {
            return attacker;
        }

        LivingEntity struck = owner.getLastHurtMob();
        if (struck != null && struck.isAlive() && Targets.foe(struck, owner) && struck.distanceToSqr(owner) < 32.0 * 32.0) {
            return struck;
        }

        List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, new AABB(mob.blockPosition()).inflate(16.0),
            e -> Targets.foe(e, owner) && e instanceof Mob other && other.getTarget() != null && (other.getTarget() == owner || servesOrAllied(other.getTarget(), owner)));
        return near.isEmpty() ? null : near.get(0);
    }
}
