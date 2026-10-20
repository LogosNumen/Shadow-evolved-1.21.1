package net.logosnumen.nightmareevolved.aspect;

import net.logosnumen.nightmareevolved.soul.Rank;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * One of an Aspect's abilities: what rank it wakes at, what it costs in essence, how long before it can be used again
 * (ticks), the body animation it plays, and what it does. {@code power} scales its effect by the Aspect's tier and the
 * soul's rank. Returns false if it could not be used (no target, nowhere to go), so nothing is spent.
 */
public record Ability(String id, Rank unlock, double cost, int cooldown, String clip, Effect effect) {
    @FunctionalInterface
    public interface Effect {
        boolean use(ServerPlayer player, ServerLevel level, double power);
    }
}
