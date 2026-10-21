package net.logosnumen.nightmareevolved.dream.seed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * What the world knows of its Seeds of Nightmare: those someone has found, when each will bloom, which have bloomed
 * (and opened a Gate in the waking world), and which have been conquered. Kept with the overworld's data.
 */
public final class SeedRecords extends SavedData {
    private static final String NAME = "nightmare_evolved_seeds";
    private final Map<String, Seed> seeds = new HashMap<>();

    /** One found Seed. */
    public static final class Seed {
        public final String id;
        public final BlockPos pos;
        public final int category;
        public final Region region;
        public final long found;
        public long bloomAt;
        public boolean bloomed;
        public boolean conquered;

        Seed(String id, BlockPos pos, int category, Region region, long found, long bloomAt) {
            this.id = id;
            this.pos = pos;
            this.category = category;
            this.region = region;
            this.found = found;
            this.bloomAt = bloomAt;
        }
    }

    public static SeedRecords get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(SeedRecords::new, SeedRecords::load, null), NAME);
    }

    private static SeedRecords load(CompoundTag tag, HolderLookup.Provider provider) {
        SeedRecords records = new SeedRecords();
        CompoundTag all = tag.getCompound("Seeds");
        for (String id : all.getAllKeys()) {
            CompoundTag s = all.getCompound(id);
            Region region = Region.byId(s.getString("Region"));
            BlockPos pos = NbtUtils.readBlockPos(s, "Pos").orElse(null);
            if (region == null || pos == null) {
                continue;
            }

            Seed seed = new Seed(id, pos, s.getInt("Category"), region, s.getLong("Found"), s.getLong("BloomAt"));
            seed.bloomed = s.getBoolean("Bloomed");
            seed.conquered = s.getBoolean("Conquered");
            records.seeds.put(id, seed);
        }

        return records;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag all = new CompoundTag();
        for (Seed seed : this.seeds.values()) {
            CompoundTag s = new CompoundTag();
            s.put("Pos", NbtUtils.writeBlockPos(seed.pos));
            s.putInt("Category", seed.category);
            s.putString("Region", seed.region.id());
            s.putLong("Found", seed.found);
            s.putLong("BloomAt", seed.bloomAt);
            s.putBoolean("Bloomed", seed.bloomed);
            s.putBoolean("Conquered", seed.conquered);
            all.put(seed.id, s);
        }

        tag.put("Seeds", all);
        return tag;
    }

    @Nullable
    public Seed get(String id) {
        return this.seeds.get(id);
    }

    public Collection<Seed> all() {
        return this.seeds.values();
    }

    /** Records a Seed as found, if it wasn't: from now its time to bloom runs. */
    public Seed find(SeedSites.Site site, long now, long bloomAt) {
        Seed seed = this.seeds.get(site.id());
        if (seed == null) {
            seed = new Seed(site.id(), site.pos(), site.category(), site.region(), now, bloomAt);
            this.seeds.put(site.id(), seed);
            this.setDirty();
        }

        return seed;
    }

    public void changed() {
        this.setDirty();
    }
}
