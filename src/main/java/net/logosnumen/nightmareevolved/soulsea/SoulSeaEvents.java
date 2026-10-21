package net.logosnumen.nightmareevolved.soulsea;

import net.logosnumen.nightmareevolved.core.Scheduler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** The rules of a Soul Sea: nothing hurts a soul in it, and nothing can be built, broken or left behind there. */
public final class SoulSeaEvents {
    private SoulSeaEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulSea.tick(player);
        }
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player && SoulSea.in(event.getEntity()) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension().equals(SoulSea.DIMENSION)
            && !(event.getEntity() instanceof Player player && player.isCreative())) {
            event.setCanceled(true);
        }
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension().equals(SoulSea.DIMENSION) && !event.getPlayer().isCreative()) {
            event.setCanceled(true);
        }
    }

    public static void onUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (SoulSea.in(event.getEntity()) && !event.getEntity().isCreative()) {
            event.setCanceled(true);
        }
    }

    public static void onToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        if (SoulSea.in(player) && !player.isCreative()) {
            // Nothing can be left behind in the sea: it goes straight back to its owner.
            player.getInventory().add(event.getEntity().getItem().copy());
            event.setCanceled(true);
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SoulSea.in(player)) {
            // The sea's figures aren't saved with the world; rebuild them once the player's ground is ready.
            Scheduler.schedule(() -> {
                if (!player.hasDisconnected() && SoulSea.in(player)) {
                    SeaPresence.populate(player);
                }
            }, 10);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulSea.forget(player);
            if (SoulSea.in(player)) {
                SeaPresence.clear(player);
            }
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        SoulSea.reset();
    }
}
