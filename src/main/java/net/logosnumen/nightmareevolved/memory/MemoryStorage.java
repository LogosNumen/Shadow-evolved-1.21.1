package net.logosnumen.nightmareevolved.memory;

import java.util.function.Consumer;
import net.logosnumen.nightmareevolved.entity.memory.MemoryEntities;
import net.logosnumen.nightmareevolved.entity.memory.WalkingCofferEntity;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;

/**
 * Things kept inside Memories that are larger inside than out (Effie's bag, the Bag of Withholding, the Covetous
 * Coffer), and inside a Sovereign's Soul Sea. A Memory can't be put into any of them.
 */
public final class MemoryStorage {
    private MemoryStorage() {
    }

    /** A chest menu whose slots refuse Memories. */
    private static final class Storage extends ChestMenu {
        Storage(MenuType<?> type, int id, Inventory inventory, Container container, int rows) {
            super(type, id, inventory, container, rows);
            for (int i = 0; i < rows * 9; i++) {
                Slot old = this.slots.get(i);
                Slot guarded = new Slot(container, old.getContainerSlot(), old.x, old.y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return SoulMemories.data(stack) == null;
                    }
                };
                guarded.index = i;
                this.slots.set(i, guarded);
            }
        }
    }

    /** A container that writes itself back wherever it lives when it is closed. */
    private static final class Kept extends SimpleContainer {
        private final Consumer<NonNullList<ItemStack>> save;

        Kept(NonNullList<ItemStack> items, Consumer<NonNullList<ItemStack>> save) {
            super(items.toArray(new ItemStack[0]));
            this.save = save;
        }

        @Override
        public void stopOpen(Player player) {
            super.stopOpen(player);
            this.save.accept(this.getItems());
        }
    }

    private static MenuType<?> type(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    private static void show(ServerPlayer player, Component title, NonNullList<ItemStack> items, Consumer<NonNullList<ItemStack>> save) {
        int rows = items.size() / 9;
        Kept container = new Kept(items, save);
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new Storage(type(rows), id, inventory, container, rows), title));
    }

    private static NonNullList<ItemStack> contents(ItemStack holder, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        holder.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }

    private static int round(int size) {
        return Math.max(9, Math.min(54, (size + 8) / 9 * 9));
    }

    /** Opens a storage Memory held in the hand. */
    public static void open(Worn w, int size) {
        int slots = round(size);
        ServerPlayer player = w.player();
        java.util.UUID id = w.data().id();
        show(player, Memories.name(w.data()), contents(w.stack(), slots), items -> {
            ItemContainerContents saved = ItemContainerContents.fromItems(items);
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                MemoryData data = SoulMemories.data(inventory.getItem(slot));
                if (data != null && data.id().equals(id)) {
                    inventory.getItem(slot).set(DataComponents.CONTAINER, saved);
                }
            }

            remember(player, id, saved);
        });
    }

    /** The soul's own copy keeps what the Memory holds too, in case the summoned one is lost far away. */
    private static void remember(ServerPlayer player, java.util.UUID id, ItemContainerContents saved) {
        MemoryRecord record = Soul.of(player).memory(id);
        if (record != null) {
            record.pieces.get(0).stack.set(DataComponents.CONTAINER, saved);
        }
    }

    /** Effie's bag tips everything it holds out onto the ground as it is dismissed. */
    public static void spill(Worn w, Vec3 at) {
        ItemContainerContents held = w.stack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        held.nonEmptyItemsCopy().forEach(stack -> w.level().addFreshEntity(new ItemEntity(w.level(), at.x, at.y + 0.5, at.z, stack)));
        w.stack().set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        MemoryRecord record = w.soul().memory(w.data().id());
        if (record != null) {
            record.pieces.get(0).stack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        }
    }

    /** The Covetous Coffer climbs out of its owner's hands to walk after them. */
    public static boolean walk(Worn w, BlockPos at) {
        if (!w.held()) {
            return false;
        }

        WalkingCofferEntity coffer = MemoryEntities.WALKING_COFFER.get().create(w.level());
        if (coffer == null) {
            return false;
        }

        coffer.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, w.player().getYRot() + 180.0F, 0.0F);
        coffer.setItem(w.stack());
        w.level().addFreshEntity(coffer);
        w.player().setItemInHand(w.slot() == Worn.Slot.MAIN_HAND ? net.minecraft.world.InteractionHand.MAIN_HAND : net.minecraft.world.InteractionHand.OFF_HAND,
            ItemStack.EMPTY);
        Vfx.burst(w.level(), coffer.position().add(0.0, 0.5, 0.0), Vfx.Palette.EARTH, 10, 0.1);
        return true;
    }

    /** Opens the Covetous Coffer where it stands. */
    public static void openCoffer(ServerPlayer player, WalkingCofferEntity coffer) {
        ItemStack held = coffer.getItem();
        MemoryData data = SoulMemories.data(held);
        if (data == null) {
            return;
        }

        int slots = round(27 + 9 * Math.min(3, data.rank()));
        show(player, Memories.name(data), contents(held, slots), items -> {
            ItemContainerContents saved = ItemContainerContents.fromItems(items);
            ItemStack updated = coffer.getItem().copy();
            updated.set(DataComponents.CONTAINER, saved);
            coffer.setItem(updated);
            remember(player, data.id(), saved);
        });
    }

    /** What a Sovereign's Soul Sea can hold: six rows of plain things. */
    public static final int VAULT = 54;

    /** Opens the store of things kept in the soul itself. */
    public static void openVault(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        NonNullList<ItemStack> items = NonNullList.withSize(VAULT, ItemStack.EMPTY);
        for (Tag t : soul.vault()) {
            CompoundTag tag = (CompoundTag)t;
            int slot = tag.getByte("Slot") & 255;
            if (slot < VAULT) {
                items.set(slot, ItemStack.parse(player.registryAccess(), tag.getCompound("Item")).orElse(ItemStack.EMPTY));
            }
        }

        show(player, Component.translatable("soul_sea.nightmare_evolved.vault"), items, saved -> {
            ListTag list = new ListTag();
            for (int i = 0; i < saved.size(); i++) {
                if (!saved.get(i).isEmpty()) {
                    CompoundTag tag = new CompoundTag();
                    tag.putByte("Slot", (byte)i);
                    tag.put("Item", saved.get(i).save(player.registryAccess()));
                    list.add(tag);
                }
            }

            soul.setVault(list);
        });
    }
}
