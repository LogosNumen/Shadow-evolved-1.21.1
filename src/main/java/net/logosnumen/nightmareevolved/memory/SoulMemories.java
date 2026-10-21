package net.logosnumen.nightmareevolved.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Summons;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.core.Scheduler;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.entity.memory.ThrownMemoryEntity;
import net.logosnumen.nightmareevolved.entity.memory.WalkingCofferEntity;
import net.logosnumen.nightmareevolved.entity.soul.SoulConstructEntity;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.memory.item.MemoryArmorItem;
import net.logosnumen.nightmareevolved.network.WornPayload;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulHooks;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Everything a soul does with its Memories: receiving them from the Spell, summoning them out of essence and dismissing
 * them back into it, mending them while they rest, losing them, handing them to someone else, and the enchantments
 * that only work while a Memory is summoned and carried.
 *
 * <p>As in the novel: summoning costs essence, which returns to the core when the Memory is dismissed, unless the
 * Memory is too far away; a resting Memory slowly mends; a Memory is destroyed when its owner dies (the seven Shard
 * Memories excepted, which outlive anyone), and can only change hands by touch.
 */
public final class SoulMemories {
    /** The most Memories one soul can hold. */
    public static final int MAX = 120;
    /** How far away a Memory can be when it is dismissed and still give its essence back. */
    public static final double REACH = 32.0;
    /** The moment a Memory takes to gather into matter. */
    public static final int GATHER = 10;
    private static final String BOW = "nightmare_evolved_bow";
    private static final String ARROW = "nightmare_evolved_arrow";
    private static final String VENOM = "nightmare_evolved_venom";
    private static final String OATH = "nightmare_evolved_oath_keys";
    /** Who stands in the light of a Dawn Shard (1) or the Crown of Dawn (2), refreshed each second. */
    private static final Map<UUID, Integer> DAWN = new HashMap<>();

    private SoulMemories() {
    }

    @Nullable
    public static MemoryData data(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(MemoryItems.DATA.get());
    }

    /** The Memory data of a stack, if it is one of this player's own Memories. */
    @Nullable
    public static MemoryData mine(Player player, ItemStack stack) {
        MemoryData data = data(stack);
        return data != null && player.getUUID().equals(data.owner()) ? data : null;
    }

    // Numbers.

    /** The essence a Memory costs to summon whole: more for higher Ranks and Tiers, much more above its owner's Rank. */
    public static double cost(MemoryData data, SoulData soul) {
        double base = 1.5 * Math.pow(1 + data.rank(), 1.6) * (1.0 + 0.25 * (data.tier() - 1));
        int above = Math.max(0, data.rank() - Math.max(0, soul.rank().ladder));
        return base * Math.pow(1.5, above);
    }

    /** How strongly a Memory's enchantments work: its Rank (raised by a Dawn Shard nearby) and its Tier. */
    public static double power(ServerPlayer player, MemoryData data) {
        double rank = data.rank() + dawnBoost(DAWN.getOrDefault(player.getUUID(), 0), data.rank()) + armament(player, data);
        return (1.0 + 0.35 * rank) * (1.0 + 0.06 * (data.tier() - 1));
    }

    /** How far the Dawn lifts a Memory of a Rank: almost a Rank for an Awakened one under the Shard, more under the Crown. */
    public static double dawnBoost(int dawn, int rank) {
        if (dawn <= 0) {
            return 0.0;
        }

        double[] shard = {1.0, 0.9, 0.5, 0.25, 0.1, 0.1, 0.1};
        double[] crown = {1.8, 1.6, 1.0, 1.0, 0.4, 0.2, 0.2};
        return (dawn >= 2 ? crown : shard)[Math.max(0, Math.min(6, rank))];
    }

    /** The Mantle of the Underworld takes in a charm and strengthens it. */
    private static double armament(ServerPlayer player, MemoryData data) {
        MemoryDef def = Memories.byId(data.def());
        if (def == null || def.kind != MemoryDef.Kind.CHARM) {
            return 0.0;
        }

        MemoryData chest = mine(player, player.getItemBySlot(EquipmentSlot.CHEST));
        return chest != null && chest.has(Enchant.UNDERWORLD_ARMAMENT) ? 0.5 : 0.0;
    }

    /** Refreshes who stands in the light of a Dawn Shard or the Crown of Dawn. Once a second. */
    public static void refreshDawn(ServerPlayer player) {
        int best = 0;
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other.distanceToSqr(player) > 48.0 * 48.0) {
                continue;
            }

            for (MemoryRecord record : Soul.of(other).memories()) {
                MemoryData data = record.data();
                if (record.anyOut() && data.has(Enchant.DAWN)) {
                    best = Math.max(best, data.rank() >= 4 ? 2 : 1);
                }
            }
        }

        if (best > 0) {
            DAWN.put(player.getUUID(), best);
        } else {
            DAWN.remove(player.getUUID());
        }
    }

    public static void forget(UUID player) {
        DAWN.remove(player);
    }

    // Receiving.

    /** A Memory's name as the Spell says it, coloured by its Rank, showing the Memory itself when hovered. */
    public static MutableComponent title(ItemStack stack) {
        MemoryData data = data(stack);
        MutableComponent name = Component.literal("[").append(data != null ? Memories.name(data) : stack.getHoverName()).append("]");
        name.withStyle(net.logosnumen.nightmareevolved.soul.Tier.byOrdinal(data != null ? data.rank() : 0).colour);
        return name.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
    }

    /**
     * Puts a Memory into a soul. Only a soul with a core can hold Memories. Returns the record, or null if the soul
     * couldn't take it.
     */
    @Nullable
    public static MemoryRecord grant(ServerPlayer player, MemoryData data, boolean announce) {
        SoulData soul = Soul.of(player);
        MemoryDef def = Memories.byId(data.def());
        if (def == null || soul.rank() == Rank.ASPIRANT) {
            return null;
        }

        if (soul.memories().size() >= MAX) {
            Spell.say(player, "spell.nightmare_evolved.memory_overflow");
            return null;
        }

        MemoryData owned = data.withOwner(player.getUUID());
        List<MemoryRecord.Piece> pieces = new ArrayList<>();
        for (int i = 0; i < def.pieces(); i++) {
            pieces.add(new MemoryRecord.Piece(MemoryItems.stack(def, owned, i), false, 0));
        }

        MemoryRecord record = new MemoryRecord(owned.id(), pieces);
        soul.memories().add(record);
        soul.seaChanged();
        if (announce) {
            Spell.say(player, "spell.nightmare_evolved.memory_received", title(pieces.get(0).stack));
        }

        checkOathKeys(player, soul);
        return record;
    }

    /** A named Memory, as the novel has it. */
    @Nullable
    public static MemoryRecord grant(ServerPlayer player, MemoryDef def, boolean announce) {
        return grant(player, Memories.data(def, player.getUUID()), announce);
    }

    /**
     * The Spell's reward for a kill: sometimes a Memory, more often for rarer prey and always for a guardian, though
     * it grows stingier the higher the prey's Rank. A creature the novel names leaves its own Memory the first time.
     */
    public static void onKill(ServerPlayer player, LivingEntity victim) {
        SoulData soul = Soul.of(player);
        if (soul.rank() == Rank.ASPIRANT || !Creatures.feedsSouls(victim) || Bound.bound(victim) || player.level().isClientSide) {
            return;
        }

        boolean nightmare = Creatures.ofNightmare(victim);
        boolean guardian = nightmare && Creatures.profile(victim).guardian();
        int rank = Creatures.rankOf(victim).ordinal();
        int cls = Creatures.classOf(victim).ordinal();
        double chance = guardian ? 1.0 : nightmare ? (0.06 + 0.03 * cls) / (1.0 + 0.25 * rank) : 0.012;
        if (player.getRandom().nextDouble() >= chance) {
            return;
        }

        MemoryDef canon = Memories.leftBy(victim.getPersistentData().getString("nightmare_evolved_lore"));
        if (canon != null && soul.memories().stream().noneMatch(r -> canon.id.equals(r.data().def()))) {
            grant(player, canon, true);
            return;
        }

        grant(player, Memories.weave(victim, player.getUUID(), player.getRandom()), true);
    }

    /**
     * The end of a First Nightmare: the Spell gives a Memory woven from the trial's guardian, and often one of the
     * small Memories the novel's Sleepers carry out of theirs (a bottle that never runs dry, a silver bell, a plain
     * silver sword, a golden rope).
     */
    public static void firstNightmare(ServerPlayer player, Family family, int rank, int tier) {
        grant(player, Memories.weave(family, rank, tier, player.getUUID(), player.getRandom()), true);
        if (player.getRandom().nextFloat() < 0.5F) {
            SoulData soul = Soul.of(player);
            List<MemoryDef> left = Memories.FIRST_NIGHTMARE.stream().filter(def -> soul.memories().stream().noneMatch(r -> def.id.equals(r.data().def())))
                .toList();
            if (!left.isEmpty()) {
                grant(player, left.get(player.getRandom().nextInt(left.size())), true);
            }
        }
    }

    /** Once all seven Shard Memories rest in one soul, the Oath Keys take shape from them. */
    private static void checkOathKeys(ServerPlayer player, SoulData soul) {
        if (player.getPersistentData().getBoolean(OATH)) {
            return;
        }

        for (MemoryDef shard : Memories.SHARDS) {
            if (soul.memories().stream().noneMatch(r -> shard.id.equals(r.data().def()))) {
                return;
            }
        }

        player.getPersistentData().putBoolean(OATH, true);
        ItemStack keys = new ItemStack(MemoryItems.OATH_KEY.get());
        if (!player.getInventory().add(keys)) {
            player.drop(keys, false);
        }

        Spell.say(player, "spell.nightmare_evolved.oath_keys");
        Vfx.burst(player.serverLevel(), Vfx.chest(player), Vfx.Palette.LIGHT, 40, 0.3);
    }

    // Summoning and dismissing.

    /** Summons the pieces of a Memory picked by {@code mask} (bit i for piece i). Returns whether it began. */
    public static boolean summon(ServerPlayer player, UUID id, int mask) {
        SoulData soul = Soul.of(player);
        MemoryRecord record = soul.memory(id);
        MemoryDef def = record == null ? null : record.def();
        if (record == null || def == null || !player.isAlive()) {
            return false;
        }

        List<Integer> wanted = new ArrayList<>();
        for (int i = 0; i < record.pieces.size(); i++) {
            if ((mask & 1 << i) != 0 && !record.pieces.get(i).out) {
                wanted.add(i);
            }
        }

        if (wanted.isEmpty()) {
            return false;
        }

        if ((def.carry == MemoryDef.Carry.HAND || def.carry == MemoryDef.Carry.CONSUMED) && !room(player)) {
            tell(player, "no_room");
            return false;
        }

        MemoryData data = record.data();
        double cost = cost(data, soul) * wanted.size() / record.pieces.size();
        if (!soul.spend(cost)) {
            tell(player, "no_essence_summon", Memories.name(data));
            return false;
        }

        for (int i : wanted) {
            MemoryRecord.Piece piece = record.pieces.get(i);
            piece.out = true;
            piece.nonce++;
        }

        soul.seaChanged();
        boolean instant = data.enchants().stream().map(Enchant::byId).anyMatch(e -> e != null && e.instant());
        if (instant) {
            materialize(player, record, wanted, cost);
        } else {
            ServerLevel level = player.serverLevel();
            Vfx.over(GATHER, t -> Vfx.mote(level, hand(player), def.palette.main, 0.02));
            Scheduler.schedule(() -> materialize(player, record, wanted, cost), GATHER);
        }

        return true;
    }

    private static Vec3 hand(Player player) {
        return player.getEyePosition().add(player.getLookAngle().scale(0.6)).add(0.0, -0.4, 0.0);
    }

    private static boolean room(Player player) {
        return player.getMainHandItem().isEmpty() || player.getInventory().getFreeSlot() >= 0;
    }

    /** The summoned pieces take form: in hand, on the body, or (for what is worn without a slot) simply on. */
    private static void materialize(ServerPlayer player, MemoryRecord record, List<Integer> pieces, double cost) {
        SoulData soul = Soul.of(player);
        MemoryDef def = record.def();
        if (def == null || player.isRemoved() || !player.isAlive() || !soul.memories().contains(record)) {
            pieces.forEach(i -> record.pieces.get(i).out = false);
            soul.setEssence(soul.essence() + cost);
            soul.seaChanged();
            return;
        }

        for (int i : pieces) {
            MemoryRecord.Piece piece = record.pieces.get(i);
            if (!piece.out) {
                continue;
            }

            ItemStack copy = piece.stack.copyWithCount(1);
            MemoryData data = data(copy);
            if (data != null) {
                copy.set(MemoryItems.DATA.get(), data.withNonce(piece.nonce).withOwner(player.getUUID()));
            }

            switch (def.carry) {
                case ARMOR -> {
                    EquipmentSlot slot = def.layers.get(i);
                    ItemStack worn = player.getItemBySlot(slot);
                    if (!worn.isEmpty()) {
                        MemoryData other = mine(player, worn);
                        if (other != null) {
                            dismiss(player, other.id(), -1, true);
                        } else if (!player.getInventory().add(worn.copy())) {
                            player.drop(worn.copy(), false);
                        }
                    }

                    player.setItemSlot(slot, copy);
                }
                case WORN -> {
                }
                default -> {
                    if (player.getMainHandItem().isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, copy);
                    } else if (def.shape == MemoryDef.Shape.SHIELD && player.getOffhandItem().isEmpty()) {
                        player.setItemInHand(InteractionHand.OFF_HAND, copy);
                    } else if (!player.getInventory().add(copy)) {
                        piece.out = false;
                        soul.setEssence(soul.essence() + cost / pieces.size());
                        tell(player, "no_room");
                    }
                }
            }
        }

        ServerLevel level = player.serverLevel();
        Vfx.burst(level, hand(player), def.palette, 10, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.4F);
        soul.seaChanged();
        changedOn(player, record);
    }

    /** Dismisses the pieces of a Memory picked by {@code mask} (-1 for all). Their essence comes back if they are near. */
    public static void dismiss(ServerPlayer player, UUID id, int mask, boolean quiet) {
        SoulData soul = Soul.of(player);
        MemoryRecord record = soul.memory(id);
        MemoryDef def = record == null ? null : record.def();
        if (record == null || def == null) {
            return;
        }

        MemoryData data = record.data();
        double share = cost(data, soul) / record.pieces.size();
        boolean tethered = data.enchants().stream().map(Enchant::byId).anyMatch(e -> e != null && e.tethered());
        Vec3 at = player.position();
        for (int i = 0; i < record.pieces.size(); i++) {
            MemoryRecord.Piece piece = record.pieces.get(i);
            if ((mask & 1 << i) == 0 || !piece.out) {
                continue;
            }

            boolean near = def.carry == MemoryDef.Carry.WORN;
            ItemStack found = take(player, id, def, i);
            if (found.isEmpty() && !near) {
                Found loose = takeLoose(player, id, def, i);
                found = loose.stack;
                at = loose.at != null ? loose.at : at;
            } else if (!found.isEmpty()) {
                at = player.position();
            }

            near |= !found.isEmpty();
            if (!found.isEmpty()) {
                MemoryData was = data(found);
                piece.stack = found.copyWithCount(1);
                if (was != null) {
                    piece.stack.set(MemoryItems.DATA.get(), was.withOwner(player.getUUID()));
                }
            }

            Worn worn = new Worn(player, soul, piece.stack, data(piece.stack) != null ? data(piece.stack) : data, Worn.Slot.WORN);
            for (String e : data.enchants()) {
                Enchant enchant = Enchant.byId(e);
                if (enchant != null) {
                    enchant.dismissed(worn, at);
                }
            }

            piece.out = false;
            piece.nonce++;
            if (near || tethered) {
                soul.setEssence(soul.essence() + share);
            }
        }

        if (!quiet) {
            Vfx.burst(player.serverLevel(), hand(player), def.palette, 8, 0.05);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.2F);
        }

        soul.seaChanged();
        changedOn(player, record);
    }

    /** Sends every summoned Memory back into the soul. */
    public static void dismissAll(ServerPlayer player, boolean quiet) {
        for (MemoryRecord record : List.copyOf(Soul.of(player).memories())) {
            if (record.anyOut()) {
                dismiss(player, record.id, -1, quiet);
            }
        }
    }

    /** Body changes and names follow what is summoned. */
    private static void changedOn(ServerPlayer player, MemoryRecord record) {
        Soul.applyStats(player);
        MemoryDef def = record.def();
        if (def != null && def.carry == MemoryDef.Carry.WORN) {
            WornPayload.broadcast(player);
        }

        if (record.data().has(Enchant.MANTLE_OF_LIES)) {
            player.refreshDisplayName();
            player.refreshTabListName();
            MemoryEvents.refreshHidden(player);
        }
    }

    /** Removes this player's copy of a Memory's piece from wherever they carry it, returning it (or empty). */
    private static ItemStack take(ServerPlayer player, UUID id, MemoryDef def, int piece) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (is(stack, id, def, piece)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                return stack;
            }
        }

        ItemStack carried = player.containerMenu.getCarried();
        if (is(carried, id, def, piece)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
            return carried;
        }

        return ItemStack.EMPTY;
    }

    private static boolean is(ItemStack stack, UUID id, MemoryDef def, int piece) {
        MemoryData data = data(stack);
        return data != null && data.id().equals(id) && (!def.armor() || MemoryItems.layerOf(stack) == piece);
    }

    private record Found(ItemStack stack, @Nullable Vec3 at) {
    }

    /** Finds a loose copy near its owner (lying on the ground, thrown, or walking about) and takes it back. */
    private static Found takeLoose(ServerPlayer player, UUID id, MemoryDef def, int piece) {
        AABB area = player.getBoundingBox().inflate(REACH);
        for (Entity entity : player.serverLevel().getEntities((Entity)null, area, e -> e instanceof ItemEntity || e instanceof ThrownMemoryEntity
            || e instanceof WalkingCofferEntity)) {
            ItemStack stack = entity instanceof ItemEntity item ? item.getItem() : entity instanceof ThrownMemoryEntity thrown ? thrown.getItem()
                : ((WalkingCofferEntity)entity).getItem();
            if (is(stack, id, def, piece)) {
                Vec3 at = entity.position();
                ItemStack copy = stack.copy();
                entity.discard();
                return new Found(copy, at);
            }
        }

        return new Found(ItemStack.EMPTY, null);
    }

    // Copies in the world.

    /**
     * Checks a Memory carried in an inventory: an ownerless one binds to an Awakened who picks it up; a copy from an
     * earlier summoning dissolves.
     */
    public static void checkCopy(ItemStack stack, Entity holder) {
        MemoryData data = data(stack);
        if (data == null || !(holder.level() instanceof ServerLevel level)) {
            return;
        }

        if (!data.owned()) {
            if (holder instanceof ServerPlayer player && Soul.of(player).rank() != Rank.ASPIRANT) {
                claim(player, stack, data);
            }

            return;
        }

        if (stale(level, stack, data)) {
            Vfx.burst(level, holder.position().add(0.0, 1.0, 0.0), Vfx.Palette.SOUL, 6, 0.05);
            stack.setCount(0);
        }
    }

    /** The same for a Memory lying on the ground. Returns true if it dissolved. */
    public static boolean checkLoose(ItemStack stack, Entity entity) {
        MemoryData data = data(stack);
        if (data == null || !data.owned() || !(entity.level() instanceof ServerLevel level) || !stale(level, stack, data)) {
            return false;
        }

        Vfx.burst(level, entity.position(), Vfx.Palette.SOUL, 6, 0.05);
        entity.discard();
        return true;
    }

    /** Whether a copy belongs to an earlier summoning, or to a Memory its owner no longer has. Unknown if they're away. */
    public static boolean stale(ServerLevel level, ItemStack stack, MemoryData data) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(data.owner());
        if (owner == null) {
            return false;
        }

        MemoryRecord record = Soul.of(owner).memory(data.id());
        if (record == null) {
            return true;
        }

        int index = MemoryItems.layerOf(stack);
        if (index < 0 || index >= record.pieces.size()) {
            index = 0;
        }

        MemoryRecord.Piece piece = record.pieces.get(index);
        return !piece.out || piece.nonce != data.nonce();
    }

    /** An ownerless Memory (a Shard Memory that outlived its owner) binds to the Awakened who takes it up. */
    private static void claim(ServerPlayer player, ItemStack stack, MemoryData data) {
        SoulData soul = Soul.of(player);
        if (soul.memory(data.id()) != null || soul.memories().size() >= MAX) {
            return;
        }

        MemoryDef def = Memories.byId(data.def());
        if (def == null) {
            return;
        }

        MemoryData owned = data.withOwner(player.getUUID()).withNonce(1);
        stack.set(MemoryItems.DATA.get(), owned);
        // What is worn without a slot goes into the soul at once; anything else stays in hand, summoned.
        boolean held = def.carry != MemoryDef.Carry.WORN;
        int layer = Math.max(0, MemoryItems.layerOf(stack));
        List<MemoryRecord.Piece> pieces = new ArrayList<>();
        for (int i = 0; i < def.pieces(); i++) {
            pieces.add(new MemoryRecord.Piece(i == layer ? stack.copyWithCount(1) : MemoryItems.stack(def, owned, i), held && i == layer, 1));
        }

        if (!held) {
            stack.setCount(0);
        }

        soul.memories().add(new MemoryRecord(owned.id(), pieces));
        soul.seaChanged();
        Spell.say(player, "spell.nightmare_evolved.memory_claimed", title(stack));
        checkOathKeys(player, soul);
    }

    /** A summoned Memory broke: it is destroyed (an armour layer alone, if others remain). */
    public static void broken(ItemStack stack, @Nullable LivingEntity holder) {
        MemoryData data = data(stack);
        if (data == null || holder == null || !(holder.level() instanceof ServerLevel level)) {
            return;
        }

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(data.owner());
        if (owner == null) {
            return;
        }

        SoulData soul = Soul.of(owner);
        MemoryRecord record = soul.memory(data.id());
        if (record == null) {
            return;
        }

        int index = MemoryItems.layerOf(stack);
        if (record.pieces.size() > 1 && index >= 0 && index < record.pieces.size()) {
            record.pieces.remove(index);
            soul.seaChanged();
            Spell.say(owner, "spell.nightmare_evolved.memory_layer_destroyed", title(stack));
            return;
        }

        destroy(owner, data.id(), false);
    }

    /** Destroys a Memory outright, wherever its pieces are. */
    public static void destroy(ServerPlayer player, UUID id, boolean quiet) {
        SoulData soul = Soul.of(player);
        MemoryRecord record = soul.memory(id);
        if (record == null) {
            return;
        }

        MemoryDef def = record.def();
        for (int i = 0; i < record.pieces.size() && def != null; i++) {
            if (record.pieces.get(i).out) {
                take(player, id, def, i);
            }
        }

        soul.memories().remove(record);
        soul.seaChanged();
        if (!quiet) {
            Spell.say(player, "spell.nightmare_evolved.memory_destroyed", title(record.pieces.get(0).stack));
        }

        changedOn(player, record);
    }

    /** Mends what rests in the soul: a twentieth of a piece's wear every twenty seconds. */
    public static void mend(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        for (MemoryRecord record : soul.memories()) {
            for (MemoryRecord.Piece piece : record.pieces) {
                if (!piece.out && piece.stack.isDamaged()) {
                    piece.stack.setDamageValue(Math.max(0, piece.stack.getDamageValue() - Math.max(1, piece.stack.getMaxDamage() / 20)));
                    soul.seaChanged();
                }
            }

            if (!record.anyOut() && record.data().has(Enchant.CURSED)) {
                // The jade sword's whispers fade while it rests.
                CompoundTag state = record.pieces.get(0).stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                if (state.getInt("Madness") > 0) {
                    state.putInt("Madness", Math.max(0, state.getInt("Madness") - 5));
                    record.pieces.get(0).stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.of(state));
                }
            }
        }
    }

    /**
     * A soul's owner dies. Outside a Nightmare, as in the novel, its Memories are destroyed with it, unless the
     * {@code keepSoulMemories} game rule is on; a Shard Memory outlives its owner, ownerless, to be taken up by someone
     * else. Inside a Nightmare, a death is a failed trial, and nothing is lost.
     */
    public static void onDeath(ServerPlayer player) {
        if (Nightmares.isNightmare(player.level())) {
            return;
        }

        SoulData soul = Soul.of(player);
        if (player.serverLevel().getGameRules().getBoolean(MemoryItems.KEEP_MEMORIES)) {
            dismissAll(player, true);
            return;
        }

        if (soul.memories().isEmpty()) {
            return;
        }

        for (MemoryRecord record : List.copyOf(soul.memories())) {
            MemoryDef def = record.def();
            if (def == null) {
                continue;
            }

            for (int i = 0; i < record.pieces.size(); i++) {
                MemoryRecord.Piece piece = record.pieces.get(i);
                ItemStack carried = piece.out ? take(player, record.id, def, i) : ItemStack.EMPTY;
                if (def.shard) {
                    ItemStack left = !carried.isEmpty() ? carried : piece.stack.copyWithCount(1);
                    MemoryData data = data(left);
                    if (data != null) {
                        left.set(MemoryItems.DATA.get(), data.withOwner(MemoryData.NOBODY).withNonce(0));
                    }

                    player.drop(left, true, false);
                }
            }
        }

        soul.memories().clear();
        soul.seaChanged();
        Spell.say(player, "spell.nightmare_evolved.memories_lost");
    }

    // What is summoned, at work.

    /** The player's own Memories that are summoned and carried: in hand, on the body, or worn. */
    public static List<Worn> active(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        List<Worn> out = new ArrayList<>();
        add(out, player, soul, player.getMainHandItem(), Worn.Slot.MAIN_HAND);
        add(out, player, soul, player.getOffhandItem(), Worn.Slot.OFF_HAND);
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof MemoryArmorItem armor && armor.getEquipmentSlot() == slot) {
                add(out, player, soul, stack, Worn.Slot.ARMOR);
            }
        }

        for (MemoryRecord record : soul.memories()) {
            MemoryDef def = record.def();
            if (def != null && def.carry == MemoryDef.Carry.WORN && record.pieces.get(0).out) {
                out.add(new Worn(player, soul, record.pieces.get(0).stack, record.data(), Worn.Slot.WORN));
            }
        }

        return out;
    }

    private static void add(List<Worn> out, ServerPlayer player, SoulData soul, ItemStack stack, Worn.Slot slot) {
        MemoryData data = mine(player, stack);
        if (data != null) {
            out.add(new Worn(player, soul, stack, data, slot));
        }
    }

    /** Whether one of the player's summoned Memories carries an enchantment. */
    public static boolean wears(ServerPlayer player, Enchant enchant) {
        return active(player).stream().anyMatch(w -> w.data().has(enchant));
    }

    /**
     * The hooks a soul acts through: its Flaw and Attributes, then every enchantment of every Memory it has summoned
     * and carries. A Weaver's Mask turns the Flaw against itself, so it drops out while the mask is worn.
     */
    public static List<SoulHooks> hooks(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        List<Worn> active = active(player);
        boolean masked = active.stream().anyMatch(w -> w.data().has(Enchant.SIMPLE_TRICK));
        List<SoulHooks> out = new ArrayList<>();
        for (SoulHooks trait : Soul.traits(soul)) {
            if (!(masked && trait instanceof Flaw)) {
                out.add(trait);
            }
        }

        for (Worn w : active) {
            for (String id : w.data().enchants()) {
                Enchant enchant = Enchant.byId(id);
                if (enchant != null) {
                    out.add(new Active(w, enchant));
                }
            }
        }

        return out;
    }

    /** An enchantment at work, speaking the soul's language of hooks. */
    private record Active(Worn w, Enchant enchant) implements SoulHooks {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            this.enchant.tick(this.w, age);
        }

        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            this.enchant.modifiers(this.w, out);
        }

        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return this.enchant.dealt(this.w, target, source, amount);
        }

        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return this.enchant.taken(this.w, source, amount);
        }

        @Override
        public void killed(ServerPlayer player, SoulData soul, LivingEntity victim) {
            this.enchant.killed(this.w, victim);
        }

        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return this.enchant.regen(this.w);
        }

        @Override
        public double cost(ServerPlayer player, SoulData soul, Ability ability) {
            return this.enchant.cost(this.w);
        }

        @Override
        public double cooldown(ServerPlayer player, SoulData soul, Ability ability) {
            return this.enchant.cooldown(this.w);
        }
    }

    /** The Memories that loosed a projectile (the bow, and the arrow if it was a Memory), as they strike. */
    public static List<Worn> fired(ServerPlayer player, DamageSource source) {
        List<Worn> out = new ArrayList<>();
        if (!(source.getDirectEntity() instanceof Projectile projectile)) {
            return out;
        }

        SoulData soul = Soul.of(player);
        for (String key : new String[] {BOW, ARROW}) {
            CompoundTag tag = projectile.getPersistentData();
            if (tag.hasUUID(key)) {
                MemoryRecord record = soul.memory(tag.getUUID(key));
                if (record != null) {
                    out.add(new Worn(player, soul, record.pieces.get(0).stack, record.data(), Worn.Slot.FIRED));
                }
            }
        }

        return out;
    }

    /**
     * What a blow gains from the Memories behind it, beyond what carried Memories add through the soul's hooks: the
     * enchantments of the Memory that loosed a shot, the Dawn's lift on a Memory weapon, a venomed blade, and the
     * obliterating shot of Morgan's Warbow.
     */
    public static float strike(ServerPlayer attacker, LivingEntity victim, DamageSource source, float amount) {
        for (Worn w : fired(attacker, source)) {
            for (String id : w.data().enchants()) {
                Enchant enchant = Enchant.byId(id);
                if (enchant != null) {
                    amount = enchant.dealt(w, victim, source, amount);
                }
            }
        }

        if (source.getDirectEntity() == attacker) {
            MemoryData held = mine(attacker, attacker.getMainHandItem());
            double boost = held == null ? 0.0 : dawnBoost(DAWN.getOrDefault(attacker.getUUID(), 0), held.rank());
            if (boost > 0.0) {
                amount *= (float)((1.0 + 0.35 * (held.rank() + boost)) / (1.0 + 0.35 * held.rank()));
            }

            if (venomed(attacker)) {
                Kit.quiet(victim, net.minecraft.world.effect.MobEffects.POISON, 100, 1);
            }
        }

        if (source.getDirectEntity() instanceof Projectile projectile && projectile.getPersistentData().getBoolean("nightmare_evolved_obliterate")) {
            ServerLevel level = attacker.serverLevel();
            for (LivingEntity near : net.logosnumen.nightmareevolved.aspect.Targets.near(level, attacker, victim.position(), 3.5)) {
                if (near != victim) {
                    net.logosnumen.nightmareevolved.aspect.Targets.hit(level, attacker, near, amount * 0.5F);
                }
            }

            Kit.shove(victim, projectile.position().subtract(projectile.getDeltaMovement()), 2.0, 0.4);
            Vfx.shockwave(level, victim.position(), 3.5, Vfx.Palette.STEEL);
        }

        return amount;
    }

    public static boolean firedByMemory(DamageSource source) {
        return source.getDirectEntity() instanceof Projectile p && (p.getPersistentData().hasUUID(BOW) || p.getPersistentData().hasUUID(ARROW));
    }

    /** Marks a projectile as loosed by a Memory bow, or as a Memory arrow. */
    public static void markFired(Projectile projectile, MemoryData data, boolean arrow) {
        projectile.getPersistentData().putUUID(arrow ? ARROW : BOW, data.id());
    }

    // Helpers the enchantments use.

    public static void later(Runnable work, int ticks) {
        Scheduler.schedule(work, ticks);
    }

    public static void tell(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("memory.nightmare_evolved." + key, args), true);
    }

    /** Coats the player's next blows in venom. */
    public static void coat(ServerPlayer player, int hits) {
        player.getPersistentData().putInt(VENOM, hits);
    }

    /** Spends one venomed blow, if the blade is coated. */
    public static boolean venomed(ServerPlayer player) {
        int hits = player.getPersistentData().getInt(VENOM);
        if (hits <= 0) {
            return false;
        }

        player.getPersistentData().putInt(VENOM, hits - 1);
        return true;
    }

    /** Cruel Sight changes between sword and spear. */
    public static void reshape(Worn w, InteractionHand hand) {
        ItemStack held = w.player().getItemInHand(hand);
        ItemStack other = MemoryItems.otherForm(held);
        if (other.isEmpty()) {
            return;
        }

        w.player().setItemInHand(hand, other);
        Kit.sound(w.level(), w.player().position(), SoundEvents.ARMOR_EQUIP_CHAIN, 1.0F, 1.4F);
        Vfx.shards(w.level(), hand(w.player()), Vfx.Palette.FROST, 10);
    }

    /** Dark Shaper's next shape for a block: full, stair, slab, wall, and round again. Null if it has no others. */
    @Nullable
    public static Block reshapeBlock(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        String path = key.getPath();
        String stem = path;
        for (String suffix : new String[] {"_stairs", "_slab", "_wall"}) {
            if (path.endsWith(suffix)) {
                stem = path.substring(0, path.length() - suffix.length());
            }
        }

        List<String> stems = new ArrayList<>(List.of(stem));
        if (stem.endsWith("s")) {
            stems.add(stem.substring(0, stem.length() - 1));
        }

        for (String tail : new String[] {"_planks", "_block"}) {
            if (stem.endsWith(tail)) {
                stems.add(stem.substring(0, stem.length() - tail.length()));
            }
        }

        Block full = find(key.getNamespace(), stem, stem + "s", stem + "_planks", stem + "_block");
        List<Block> forms = new ArrayList<>();
        if (full != null) {
            forms.add(full);
        }

        for (String suffix : new String[] {"_stairs", "_slab", "_wall"}) {
            for (String s : stems) {
                Block form = find(key.getNamespace(), s + suffix);
                if (form != null && !forms.contains(form)) {
                    forms.add(form);
                    break;
                }
            }
        }

        int at = forms.indexOf(block);
        return forms.size() < 2 || at < 0 ? null : forms.get((at + 1) % forms.size());
    }

    @Nullable
    private static Block find(String namespace, String... paths) {
        for (String path : paths) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                Block block = BuiltInRegistries.BLOCK.get(id);
                if (block != Blocks.AIR) {
                    return block;
                }
            }
        }

        return null;
    }

    /** The Beast Locket takes in a creature, if it is no stronger than the locket can hold. */
    public static boolean keepInLocket(Worn w, Mob mob) {
        CompoundTag state = w.state();
        ListTag kept = state.getList("Beings", Tag.TAG_COMPOUND);
        if (kept.size() >= 8 || mob.getMaxHealth() > 40.0 * w.power() || Bound.bound(mob) && !Bound.servesOrAllied(mob, w.player())) {
            tell(w.player(), "beast_locket.refused");
            return false;
        }

        CompoundTag being = new CompoundTag();
        if (!mob.save(being)) {
            return false;
        }

        kept.add(being);
        state.put("Beings", kept);
        w.setState(state);
        Vfx.trail(w.level(), mob.getBoundingBox().getCenter(), hand(w.player()), Vfx.Palette.EARTH);
        mob.discard();
        tell(w.player(), "beast_locket.kept", kept.size());
        return true;
    }

    /** The Beast Locket lets out all it keeps, near its owner. */
    public static boolean freeFromLocket(Worn w) {
        CompoundTag state = w.state();
        ListTag kept = state.getList("Beings", Tag.TAG_COMPOUND);
        if (kept.isEmpty()) {
            return false;
        }

        ServerLevel level = w.level();
        for (int i = 0; i < kept.size(); i++) {
            CompoundTag being = kept.getCompound(i);
            double angle = i * Math.PI * 2.0 / kept.size();
            Vec3 at = w.player().position().add(Math.cos(angle) * 2.5, 0.0, Math.sin(angle) * 2.5);
            Entity entity = EntityType.loadEntityRecursive(being, level, e -> {
                e.moveTo(at.x, at.y, at.z, e.getYRot(), e.getXRot());
                return e;
            });
            if (entity != null) {
                entity.setUUID(UUID.randomUUID());
                level.addFreshEntity(entity);
                Vfx.burst(level, entity.position().add(0.0, 0.5, 0.0), Vfx.Palette.EARTH, 10, 0.1);
            }
        }

        state.put("Beings", new ListTag());
        w.setState(state);
        return true;
    }

    /** The Shadow Lantern sets the shadows it gathered loose, to fight for its owner for half a minute. */
    public static void releaseShadows(Worn w, int count) {
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2.0 / count;
            Vec3 at = w.player().position().add(Math.cos(angle) * 2.0, 0.0, Math.sin(angle) * 2.0);
            Summons.construct(w.player(), SoulConstructEntity.Look.SHADOW, at, 12.0 * w.power(), 3.0 * w.power(), 600, "lantern");
        }

        Vfx.shockwave(w.level(), w.player().position(), 4.0, Vfx.Palette.SHADOW);
    }

    /** A lineage Memory is consumed, and its lineage becomes part of whoever consumed it. */
    public static boolean consumeLineage(Worn w, List<SoulAttribute> lineage) {
        SoulData soul = w.soul();
        for (SoulAttribute attribute : lineage) {
            if (!soul.hasAttribute(attribute.id())) {
                soul.addAttribute(attribute.id());
                destroy(w.player(), w.data().id(), true);
                Soul.applyStats(w.player());
                Spell.say(w.player(), "spell.nightmare_evolved.lineage", attribute.title());
                Vfx.burst(w.level(), Vfx.chest(w.player()), Vfx.Palette.BLOOD, 30, 0.2);
                return true;
            }
        }

        tell(w.player(), "lineage_full");
        return false;
    }

    /**
     * A soulbound relic is bound: the Memory is gone, and becomes an Attribute of its owner's. Asks once to be sure.
     */
    public static boolean bindRelic(Worn w, String attribute) {
        CompoundTag state = w.state();
        long now = w.level().getGameTime();
        if (now - state.getLong("BindAsked") > 200L) {
            state.putLong("BindAsked", now);
            w.setState(state);
            tell(w.player(), "relic_confirm", Memories.name(w.data()));
            return true;
        }

        SoulData soul = w.soul();
        destroy(w.player(), w.data().id(), true);
        soul.addAttribute(attribute);
        Soul.evolveAttributes(soul);
        Soul.applyStats(w.player());
        Spell.say(w.player(), "spell.nightmare_evolved.relic_bound", Memories.name(w.data()));
        Vfx.burst(w.level(), Vfx.chest(w.player()), Vfx.Palette.EARTH, 40, 0.3);
        return true;
    }

    /**
     * Hands a Memory to another Awakened, which takes touch: the giver holds it out and the taker is within reach.
     * Everything summoned of it returns to the giver's soul first, and the Memory goes to the taker's soul whole.
     */
    public static boolean bestow(ServerPlayer giver, ServerPlayer taker, MemoryData data) {
        SoulData from = Soul.of(giver);
        SoulData to = Soul.of(taker);
        MemoryRecord record = from.memory(data.id());
        if (record == null || to.rank() == Rank.ASPIRANT || to.memories().size() >= MAX || giver.distanceToSqr(taker) > 4.0 * 4.0) {
            tell(giver, "bestow_refused", taker.getDisplayName());
            return false;
        }

        dismiss(giver, data.id(), -1, true);
        from.memories().remove(record);
        for (MemoryRecord.Piece piece : record.pieces) {
            MemoryData pieceData = data(piece.stack);
            if (pieceData != null) {
                piece.stack.set(MemoryItems.DATA.get(), pieceData.withOwner(taker.getUUID()));
            }

            piece.out = false;
        }

        to.memories().add(record);
        from.seaChanged();
        to.seaChanged();
        Spell.say(giver, "spell.nightmare_evolved.memory_given", title(record.pieces.get(0).stack), taker.getDisplayName());
        Spell.say(taker, "spell.nightmare_evolved.memory_taken", title(record.pieces.get(0).stack), giver.getDisplayName());
        Vfx.trail(giver.serverLevel(), Vfx.chest(giver), Vfx.chest(taker), record.def() != null ? record.def().palette : Vfx.Palette.SOUL);
        checkOathKeys(taker, to);
        return true;
    }

    /** A worn Memory's enchantments, used on a creature with an empty hand (the Shroud's mind link, for one). */
    public static boolean useWornOn(ServerPlayer player, LivingEntity target) {
        for (Worn w : active(player)) {
            if (w.slot() == Worn.Slot.WORN || w.slot() == Worn.Slot.ARMOR) {
                for (String id : w.data().enchants()) {
                    Enchant enchant = Enchant.byId(id);
                    if (enchant != null && enchant.useOnEntity(w, target)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Whether the player's Memories keep them alive through a killing blow. */
    public static boolean spared(ServerPlayer player, DamageSource source) {
        for (Worn w : active(player)) {
            for (String id : w.data().enchants()) {
                Enchant enchant = Enchant.byId(id);
                if (enchant != null && enchant.spares(w, source)) {
                    return true;
                }
            }
        }

        return false;
    }
}
