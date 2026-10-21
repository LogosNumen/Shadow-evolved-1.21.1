package net.logosnumen.nightmareevolved.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * A Memory as its owner's soul keeps it. Most Memories are a single piece; armour has one per layer, each summoned and
 * dismissed on its own. While a piece rests in the soul, the soul holds it (and mends it); while it is out, the soul
 * remembers which summoning the copy in the world belongs to, so any older copy can be told apart and dissolved.
 */
public final class MemoryRecord {
    public final UUID id;
    public final List<Piece> pieces;

    public static final class Piece {
        /** The piece as the soul last held it: its wear, and whatever its enchantments remember. */
        public ItemStack stack;
        /** Whether it is summoned (in the world, or worn). */
        public boolean out;
        /** Which summoning the copy in the world belongs to. */
        public int nonce;

        public Piece(ItemStack stack, boolean out, int nonce) {
            this.stack = stack;
            this.out = out;
            this.nonce = nonce;
        }
    }

    public MemoryRecord(UUID id, List<Piece> pieces) {
        this.id = id;
        this.pieces = pieces;
    }

    public MemoryData data() {
        MemoryData data = SoulMemories.data(this.pieces.get(0).stack);
        return data != null ? data : new MemoryData(this.id, "", "", 0, 1, List.of(), MemoryData.NOBODY, 0);
    }

    @Nullable
    public MemoryDef def() {
        return Memories.byId(this.data().def());
    }

    public boolean anyOut() {
        return this.pieces.stream().anyMatch(p -> p.out);
    }

    public boolean allOut() {
        return this.pieces.stream().allMatch(p -> p.out);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.id);
        ListTag list = new ListTag();
        for (Piece piece : this.pieces) {
            CompoundTag p = new CompoundTag();
            p.put("Stack", piece.stack.save(registries));
            p.putBoolean("Out", piece.out);
            p.putInt("Nonce", piece.nonce);
            list.add(p);
        }

        tag.put("Pieces", list);
        return tag;
    }

    /** A record from its save, or null if its items no longer exist. */
    @Nullable
    public static MemoryRecord load(HolderLookup.Provider registries, CompoundTag tag) {
        if (!tag.hasUUID("Id")) {
            return null;
        }

        List<Piece> pieces = new ArrayList<>();
        for (Tag t : tag.getList("Pieces", Tag.TAG_COMPOUND)) {
            CompoundTag p = (CompoundTag)t;
            ItemStack stack = ItemStack.parse(registries, p.getCompound("Stack")).orElse(ItemStack.EMPTY);
            if (stack.isEmpty() || SoulMemories.data(stack) == null) {
                return null;
            }

            pieces.add(new Piece(stack, p.getBoolean("Out"), p.getInt("Nonce")));
        }

        return pieces.isEmpty() ? null : new MemoryRecord(tag.getUUID("Id"), pieces);
    }
}
