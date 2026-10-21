package net.logosnumen.nightmareevolved.soulsea;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Hands every soul a private stretch of the Soul Sea dimension, on a grid far enough apart that no one can ever see
 * another's sea, and remembers what has been raised in each (a Citadel's likeness, an island). Kept with the
 * overworld's saved data.
 */
public final class SeaRegions extends SavedData {
    private static final String ID = "nightmare_evolved_soul_sea";
    /** Distance between region centres, well beyond the farthest render distance. */
    public static final int SPACING = 2048;
    private static final int COLUMNS = 512;
    private static final int OFFSET = 1024;
    private final Map<UUID, Integer> regions = new HashMap<>();
    private final Map<Integer, UUID> owners = new HashMap<>();
    private final Map<Integer, CompoundTag> built = new HashMap<>();
    private int next;

    public static SeaRegions get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(SeaRegions::new, SeaRegions::load), ID);
    }

    public int regionOf(UUID player) {
        Integer existing = this.regions.get(player);
        if (existing != null) {
            return existing;
        }

        int index = this.next++;
        this.regions.put(player, index);
        this.owners.put(index, player);
        this.setDirty();
        return index;
    }

    public Optional<UUID> ownerOf(int index) {
        return Optional.ofNullable(this.owners.get(index));
    }

    /** What has been raised in a region's sea, by feature. */
    public CompoundTag built(int index) {
        return this.built.computeIfAbsent(index, i -> new CompoundTag());
    }

    public void markBuilt(int index, String feature, String value) {
        this.built(index).putString(feature, value);
        this.setDirty();
    }

    public static double centerX(int index) {
        return (double)(index % COLUMNS) * SPACING + OFFSET + 0.5;
    }

    public static double centerZ(int index) {
        return (double)(index / COLUMNS) * SPACING + OFFSET + 0.5;
    }

    /** The region whose centre is nearest a position, or -1 outside the grid. */
    public static int indexAt(double x, double z) {
        int column = (int)Math.round((x - OFFSET - 0.5) / SPACING);
        int row = (int)Math.round((z - OFFSET - 0.5) / SPACING);
        if (column < 0 || row < 0 || column >= COLUMNS) {
            return -1;
        }

        return row * COLUMNS + column;
    }

    private static SeaRegions load(CompoundTag tag, HolderLookup.Provider registries) {
        SeaRegions data = new SeaRegions();
        data.next = tag.getInt("Next");
        CompoundTag regions = tag.getCompound("Regions");
        for (String key : regions.getAllKeys()) {
            try {
                UUID player = UUID.fromString(key);
                int index = regions.getInt(key);
                data.regions.put(player, index);
                data.owners.put(index, player);
                data.next = Math.max(data.next, index + 1);
            } catch (IllegalArgumentException ignored) {
                // An entry spoiled by hand-editing; the soul is given a fresh region.
            }
        }

        CompoundTag built = tag.getCompound("Built");
        for (String key : built.getAllKeys()) {
            try {
                data.built.put(Integer.parseInt(key), built.getCompound(key));
            } catch (NumberFormatException ignored) {
                // As above.
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Next", this.next);
        CompoundTag regions = new CompoundTag();
        this.regions.forEach((player, index) -> regions.putInt(player.toString(), index));
        tag.put("Regions", regions);
        CompoundTag built = new CompoundTag();
        this.built.forEach((index, features) -> built.put(Integer.toString(index), features));
        tag.put("Built", built);
        return tag;
    }
}
