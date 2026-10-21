package net.logosnumen.nightmareevolved.dream.gate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.logosnumen.nightmareevolved.dream.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The Gateways that have come and gone since the world began: those left where Seeds of Nightmare were destroyed, and
 * those that have been destroyed themselves. Kept with the overworld's data.
 */
public final class GatewayRecords extends SavedData {
    private static final String NAME = "nightmare_evolved_gateways";
    private final List<Gateways.Gateway> born = new ArrayList<>();
    private final Set<String> destroyed = new HashSet<>();

    public static GatewayRecords get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(GatewayRecords::new, GatewayRecords::load, null), NAME);
    }

    private static GatewayRecords load(CompoundTag tag, HolderLookup.Provider provider) {
        GatewayRecords records = new GatewayRecords();
        for (Tag t : tag.getList("Born", Tag.TAG_COMPOUND)) {
            CompoundTag g = (CompoundTag)t;
            Region region = Region.byId(g.getString("Region"));
            BlockPos pos = NbtUtils.readBlockPos(g, "Pos").orElse(null);
            if (region != null && pos != null) {
                records.born.add(new Gateways.Gateway(g.getString("Id"), region, pos, GatewayBlock.Form.POOL, null, false));
            }
        }

        for (Tag t : tag.getList("Destroyed", Tag.TAG_STRING)) {
            records.destroyed.add(t.getAsString());
        }

        return records;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag born = new ListTag();
        for (Gateways.Gateway gateway : this.born) {
            CompoundTag g = new CompoundTag();
            g.putString("Id", gateway.id());
            g.putString("Region", gateway.region().id());
            g.put("Pos", NbtUtils.writeBlockPos(gateway.pos()));
            born.add(g);
        }

        tag.put("Born", born);
        ListTag destroyed = new ListTag();
        this.destroyed.forEach(id -> destroyed.add(StringTag.valueOf(id)));
        tag.put("Destroyed", destroyed);
        return tag;
    }

    public List<Gateways.Gateway> born() {
        return this.born;
    }

    /** A Gateway left in a destroyed Seed's place. */
    public void addBorn(Gateways.Gateway gateway) {
        this.born.removeIf(g -> g.id().equals(gateway.id()));
        this.born.add(gateway);
        this.setDirty();
    }

    public boolean destroyed(String id) {
        return this.destroyed.contains(id);
    }

    public void setDestroyed(String id, boolean destroyed) {
        if (destroyed ? this.destroyed.add(id) : this.destroyed.remove(id)) {
            this.setDirty();
        }
    }
}
