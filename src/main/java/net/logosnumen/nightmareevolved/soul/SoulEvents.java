package net.logosnumen.nightmareevolved.soul;

import net.logosnumen.nightmareevolved.spell.SpellReplies;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Keeps each soul in step with its body: on joining, respawning and changing worlds, and every tick. */
public final class SoulEvents {
    private SoulEvents() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulData soul = Soul.of(player);
            Soul.applyStats(player);
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
            // Attribute modifiers don't survive a death; the soul does.
            Soul.applyStats(player);
            player.setHealth(player.getMaxHealth());
            Soul.of(player).changed();
        }
    }

    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Soul.of(player).changed();
        }
    }

    /** Essence comes back a little every second, and the client hears about any change. */
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SoulData soul = Soul.of(player);
        if (player.tickCount % 20 == 0 && soul.essence() < soul.maxEssence() && player.isAlive()) {
            soul.setEssence(soul.essence() + SoulRules.regenPerSecond(soul.rank(), soul.aspectTier()));
        }

        if (soul.takeDirty()) {
            Soul.sync(player);
        }
    }
}
