package net.logosnumen.nightmareevolved.soul;

import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.memory.MemoryEvents;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.spell.SpellReplies;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Keeps each soul in step with its body: on joining, respawning and changing worlds; every tick (essence, held
 * abilities, and whatever the Aspect, Flaw and Attributes do over time); and in every blow the player lands or takes,
 * and every kill.
 */
public final class SoulEvents {
    private SoulEvents() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulData soul = Soul.of(player);
            Soul.applyStats(player);
            MemoryEvents.refreshHidden(player);
            player.refreshDisplayName();
            soul.changed();
            if (!soul.touched()) {
                soul.touch();
                SpellReplies.greet(player);
            }

            Soul.sync(player);
        }
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Attribute modifiers and flight don't survive a death; the soul does.
            Soul.applyStats(player);
            player.setHealth(player.getMaxHealth());
            Soul.of(player).changed();
        }
    }

    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Soul.applyStats(player);
            Soul.of(player).changed();
        }
    }

    /** Every tick: abilities, Flaw and Attributes act; once a second essence returns and held abilities are paid for. */
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) {
            return;
        }

        SoulData soul = Soul.of(player);
        int age = player.tickCount;
        for (Ctx ctx : Aspects.inEffect(player)) {
            ctx.ability().hooks.tick(ctx, age);
        }

        List<SoulHooks> hooks = Soul.hooks(player);
        for (SoulHooks trait : hooks) {
            trait.tick(player, soul, age);
        }

        if (age % 20 == 0) {
            Aspects.upkeep(player);
            double share = 1.0;
            for (SoulHooks trait : hooks) {
                share *= trait.regen(player, soul);
            }

            if (share > 0.0 && soul.essence() < soul.maxEssence()) {
                soul.setEssence(soul.essence() + SoulRules.regenPerSecond(soul.maxEssence()) * share);
            }
        }

        if (soul.takeDirty()) {
            Soul.sync(player);
        }
    }

    /**
     * A blow a soul lands passes through its abilities in effect, Flaw and Attributes, and the Memories it carries (or
     * that loosed the shot); so does one it takes.
     */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();
        if (source.getEntity() instanceof ServerPlayer attacker && attacker != victim) {
            SoulData soul = Soul.of(attacker);
            float amount = event.getAmount();
            for (Ctx ctx : Aspects.inEffect(attacker)) {
                amount = ctx.ability().hooks.dealt(ctx, victim, source, amount);
            }

            for (SoulHooks trait : Soul.hooks(attacker)) {
                amount = trait.dealt(attacker, soul, victim, source, amount);
            }

            amount = SoulMemories.strike(attacker, victim, source, amount);
            event.setAmount(Math.max(0.0F, amount));
        }

        if (victim instanceof ServerPlayer player) {
            SoulData soul = Soul.of(player);
            float amount = event.getAmount();
            for (Ctx ctx : Aspects.inEffect(player)) {
                amount = ctx.ability().hooks.taken(ctx, source, amount);
            }

            for (SoulHooks trait : Soul.hooks(player)) {
                amount = trait.taken(player, soul, source, amount);
            }

            event.setAmount(Math.max(0.0F, amount));
        }
    }

    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer killer && killer != event.getEntity()) {
            SoulData soul = Soul.of(killer);
            for (Ctx ctx : Aspects.inEffect(killer)) {
                ctx.ability().hooks.killed(ctx, event.getEntity());
            }

            for (SoulHooks trait : Soul.hooks(killer)) {
                trait.killed(killer, soul, event.getEntity());
            }
        }

        LivingEntity victim = event.getEntity();
        if (!(victim instanceof ServerPlayer) && victim.level() instanceof ServerLevel level) {
            for (ServerPlayer witness : level.players()) {
                if (witness.isAlive() && witness.distanceToSqr(victim) < 24.0 * 24.0) {
                    for (Ctx ctx : Aspects.inEffect(witness)) {
                        ctx.ability().hooks.witnessed(ctx, victim);
                    }
                }
            }
        }

        if (victim instanceof ServerPlayer player) {
            Aspects.stopAll(player);
        }
    }
}
