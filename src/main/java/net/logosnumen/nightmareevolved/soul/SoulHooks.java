package net.logosnumen.nightmareevolved.soul;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * The ways a Flaw or an Attribute can touch a soul. Every method is optional; a Flaw or Attribute overrides only what
 * it changes.
 */
public interface SoulHooks {
    /** Every tick; {@code age} counts the player's ticks, for things that happen only every second or minute. */
    default void tick(ServerPlayer player, SoulData soul, int age) {
    }

    /** Lasting changes to the body, reapplied whenever the soul changes. */
    default void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
    }

    /** A blow the player lands; returns the damage it should do. */
    default float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
        return amount;
    }

    /** A blow the player takes; returns the damage it should do. */
    default float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
        return amount;
    }

    default void killed(ServerPlayer player, SoulData soul, LivingEntity victim) {
    }

    /** How fast essence comes back, as a share of normal. */
    default double regen(ServerPlayer player, SoulData soul) {
        return 1.0;
    }

    /** What an ability costs, as a share of its listed cost. */
    default double cost(ServerPlayer player, SoulData soul, Ability ability) {
        return 1.0;
    }

    /** How long an ability rests after use, as a share of its listed cooldown. */
    default double cooldown(ServerPlayer player, SoulData soul, Ability ability) {
        return 1.0;
    }

    /** Why an ability can't be used right now, or null if it can. */
    @Nullable
    default Component forbids(ServerPlayer player, SoulData soul, Ability ability) {
        return null;
    }

    /** After an ability (or a held ability's second) has been paid for. */
    default void used(ServerPlayer player, SoulData soul, Ability ability) {
    }

    /** How much of a shard's fragments reach the cores, as a share. */
    default double shards(ServerPlayer player, SoulData soul) {
        return 1.0;
    }
}
