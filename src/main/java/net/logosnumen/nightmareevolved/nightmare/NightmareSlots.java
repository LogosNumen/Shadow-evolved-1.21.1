package net.logosnumen.nightmareevolved.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Hands out a fresh patch of the Nightmare's void for each trial, far from every other, and remembers how many. */
public final class NightmareSlots extends SavedData {
    private static final String NAME = "nightmare_evolved_slots";
    private static final int SPACING = 3000;
    private int next;

    private static final Factory<NightmareSlots> FACTORY = new Factory<>(NightmareSlots::new, NightmareSlots::load, null);

    private static NightmareSlots load(CompoundTag tag, HolderLookup.Provider provider) {
        NightmareSlots slots = new NightmareSlots();
        slots.next = tag.getInt("Next");
        return slots;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("Next", this.next);
        return tag;
    }

    /** The middle of a new trial's ground. */
    public static BlockPos claim(MinecraftServer server) {
        NightmareSlots slots = server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
        int slot = slots.next++;
        slots.setDirty();
        return new BlockPos(SPACING * (slot + 1), Arenas.GROUND, 0);
    }
}
