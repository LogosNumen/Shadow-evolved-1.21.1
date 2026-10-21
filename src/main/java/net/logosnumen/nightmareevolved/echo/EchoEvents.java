package net.logosnumen.nightmareevolved.echo;

import java.util.UUID;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Echoes in the world: they leave nothing when slain (they are made of essence), their Attributes act in every blow,
 * a Blood Blossom on their master makes them thirst for blood, Shadows trail darkness, and none stay out once their
 * master leaves the world they were called into.
 */
public final class EchoEvents {
    private EchoEvents() {
    }

    public static void onDrops(LivingDropsEvent event) {
        if (Echoes.isEcho(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    public static void onExperience(LivingExperienceDropEvent event) {
        if (Echoes.isEcho(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob && Echoes.isEcho(mob) && !mob.level().isClientSide) {
            Echoes.slain(mob);
        }

        if (event.getSource().getEntity() instanceof ServerPlayer killer && !(event.getEntity() instanceof ServerPlayer)) {
            Echoes.onKill(killer, event.getEntity());
        }
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        float amount = event.getAmount();
        if (event.getSource().getEntity() instanceof Mob echo && Echoes.isEcho(echo)) {
            for (String id : traits(echo)) {
                EchoTrait trait = EchoTrait.byId(id);
                if (trait != null) {
                    amount = trait.dealt(echo, event.getEntity(), event.getSource(), amount);
                }
            }

            ServerPlayer master = master(echo);
            if (master != null && SoulMemories.wears(master, Enchant.FLOWER_OF_EVIL)) {
                amount *= 1.15F;
                echo.heal(amount * 0.05F);
            }
        }

        if (event.getEntity() instanceof Mob echo && Echoes.isEcho(echo)) {
            for (String id : traits(echo)) {
                EchoTrait trait = EchoTrait.byId(id);
                if (trait != null) {
                    amount = trait.taken(echo, event.getSource(), amount);
                }
            }
        }

        event.setAmount(amount);
    }

    /** Once a second: an Echo's Attributes that act over time, and the darkness a Shadow trails. */
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob echo) || echo.tickCount % 20 != 0 || !(echo.level() instanceof ServerLevel level) || !Echoes.isEcho(echo)) {
            return;
        }

        for (String id : traits(echo)) {
            EchoTrait trait = EchoTrait.byId(id);
            if (trait != null) {
                trait.tick(echo);
            }
        }

        ServerPlayer master = master(echo);
        EchoRecord record = master == null ? null : Soul.of(master).echo(echo.getPersistentData().getUUID(Echoes.TAG));
        if (record == null || !record.out) {
            // An Echo whose master no longer knows it (or is gone) is only essence; it comes apart.
            echo.discard();
            return;
        }

        if (record.shadow) {
            Vfx.smoke(level, echo.getBoundingBox().getCenter(), 0x050508, 3, echo.getBbWidth() * 0.6);
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 400 == 0) {
            Echoes.heal(player);
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Echoes.settle(player);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Echoes.dismissAll(player);
        }
    }

    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (EchoRecord record : Soul.of(player).echoes()) {
                if (record.out) {
                    // The body was left behind in the other world; the Echo returns to the soul, wounds and all.
                    record.out = false;
                    record.body = null;
                    Soul.of(player).seaChanged();
                }
            }
        }
    }

    private static java.util.List<String> traits(Mob echo) {
        ServerPlayer master = master(echo);
        EchoRecord record = master == null ? null : Soul.of(master).echo(echo.getPersistentData().getUUID(Echoes.TAG));
        return record == null ? java.util.List.of() : record.traits;
    }

    private static ServerPlayer master(LivingEntity echo) {
        UUID owner = Bound.owner(echo);
        return owner == null || !(echo.level() instanceof ServerLevel level) ? null : level.getServer().getPlayerList().getPlayer(owner);
    }
}
