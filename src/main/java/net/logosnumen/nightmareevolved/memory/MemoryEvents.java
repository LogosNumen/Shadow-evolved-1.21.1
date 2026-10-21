package net.logosnumen.nightmareevolved.memory;

import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.flaw.FlawEvents;
import net.logosnumen.nightmareevolved.network.SeaPayload;
import net.logosnumen.nightmareevolved.network.WornPayload;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * When Memories act on their own: the Spell's rewards for kills, losing Memories to death (or being kept from it by
 * the Undying Chain), mending what rests in the soul, shields that remember what they stopped, worn Memories used
 * with an open hand, a charm that turns a long silence into a command, a mask that hides a face, and a dagger that
 * keeps its wielder from making a sound.
 */
public final class MemoryEvents {
    /** How long a Stifled Scream's master must keep silent before a word of theirs becomes a command. */
    private static final long GREAT_SILENCE = 6000L;

    private MemoryEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) {
            return;
        }

        if (player.tickCount % 20 == 0) {
            SoulMemories.refreshDawn(player);
        }

        if (player.tickCount % 400 == 0) {
            SoulMemories.mend(player);
        }

        if (Soul.of(player).takeSeaDirty()) {
            PacketDistributor.sendToPlayer(player, SeaPayload.of(Soul.of(player)));
        }
    }

    /** The Spell's reward for a kill. */
    public static void onKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer killer && !(event.getEntity() instanceof ServerPlayer)) {
            SoulMemories.onKill(killer, event.getEntity());
        }
    }

    /** A death: the Undying Chain may refuse it; if not, the soul's Memories may go with its owner. */
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (SoulMemories.spared(player, event.getSource())) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            return;
        }

        SoulMemories.onDeath(player);
    }

    /** Taking up or putting down a Memory changes the body. */
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
            && (SoulMemories.data(event.getFrom()) != null || SoulMemories.data(event.getTo()) != null)) {
            Soul.applyStats(player);
        }
    }

    /** A Memory shield remembers what it stopped. */
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getBlocked()) {
            return;
        }

        InteractionHand hand = player.getUsedItemHand();
        MemoryData data = SoulMemories.mine(player, player.getItemInHand(hand));
        if (data == null) {
            return;
        }

        Worn w = new Worn(player, Soul.of(player), player.getItemInHand(hand), data, hand == InteractionHand.MAIN_HAND ? Worn.Slot.MAIN_HAND : Worn.Slot.OFF_HAND);
        float blocked = event.getBlockedDamage();
        for (String id : data.enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null) {
                blocked = enchant.blocked(w, event.getDamageSource(), blocked);
            }
        }

        event.setBlockedDamage(blocked);
    }

    /** With an open hand and crouching, a worn Memory acts on whoever is touched: the Shroud's mind link. */
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getHand() == InteractionHand.MAIN_HAND && player.isShiftKeyDown()
            && player.getMainHandItem().isEmpty() && event.getTarget() instanceof LivingEntity target && SoulMemories.useWornOn(player, target)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    /** Stifled Scream: speaking breaks the silence; after a great silence, the word spoken is a command. */
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        SoulData soul = Soul.of(player);
        for (MemoryRecord record : soul.memories()) {
            MemoryData data = record.data();
            if (!record.pieces.get(0).out || !data.has(Enchant.WORD_OF_POWER)) {
                continue;
            }

            CompoundTag state = record.pieces.get(0).stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            long now = player.level().getGameTime();
            if (now - state.getLong("Spoke") >= GREAT_SILENCE) {
                for (LivingEntity foe : Targets.near(player.serverLevel(), player, player.position(), 16.0)) {
                    Kit.quiet(foe, MobEffects.MOVEMENT_SLOWDOWN, 60, 6);
                    Kit.quiet(foe, MobEffects.WEAKNESS, 120, 1);
                    if (foe instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                }

                Vfx.shockwave(player.serverLevel(), player.position(), 16.0, Vfx.Palette.SHADOW);
                Kit.sound(player.serverLevel(), player.position(), SoundEvents.WARDEN_ROAR, 0.8F, 1.5F);
            }

            state.putLong("Spoke", now);
            record.pieces.get(0).stack.set(DataComponents.CUSTOM_DATA, CustomData.of(state));
        }
    }

    /** Weaver's Mask hides who wears it. */
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && SoulMemories.wears(player, Enchant.MANTLE_OF_LIES)) {
            event.setDisplayname(Component.translatable("memory.nightmare_evolved.weavers_mask.stranger"));
        }
    }

    public static void onTabName(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && SoulMemories.wears(player, Enchant.MANTLE_OF_LIES)) {
            event.setDisplayName(Component.translatable("memory.nightmare_evolved.weavers_mask.stranger"));
        }
    }

    /** Whether a player's name should be hidden over their head: the Overlooked, and whoever wears the Weaver's Mask. */
    public static void refreshHidden(ServerPlayer player) {
        FlawEvents.hideName(player, Flaw.OVERLOOKED.id().equals(Soul.of(player).flaw()) || SoulMemories.wears(player, Enchant.MANTLE_OF_LIES));
    }

    /** The Silent Dagger: nothing its wielder does makes a sound anything can hear. */
    public static void onGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof ServerPlayer player) {
            MemoryData held = SoulMemories.mine(player, player.getMainHandItem());
            if (held != null && held.has(Enchant.HUSH)) {
                event.setCanceled(true);
            }
        }
    }

    /** Someone comes into sight of a player: show them what that player wears. */
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer tracker) {
            PacketDistributor.sendToPlayer(tracker, WornPayload.of(target));
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WornPayload.broadcast(player);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SoulMemories.forget(event.getEntity().getUUID());
    }
}
