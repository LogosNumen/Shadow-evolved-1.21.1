package net.logosnumen.nightmareevolved.dream.gate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
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
 * The Gateways that have come and gone since the world began: those left where Seeds of Nightmare were destroyed, those
 * that have been destroyed themselves, and the lords their Citadels are bound to; with the oaths Saints have sworn to
 * Sovereigns, and which lords were last seen as Sovereigns. Kept with the overworld's data.
 */
public final class GatewayRecords extends SavedData {
    private static final String NAME = "nightmare_evolved_gateways";
    private final List<Gateways.Gateway> born = new ArrayList<>();
    private final Set<String> destroyed = new HashSet<>();
    private final Map<String, Lord> lords = new HashMap<>();
    private final Map<UUID, Oath> oaths = new HashMap<>();
    private final Set<UUID> sovereigns = new HashSet<>();

    /** The soul a Citadel is bound to. */
    public record Lord(UUID id, String name) {
    }

    /** A Saint's oath of fealty: who swore it, and the Sovereign they serve. */
    public record Oath(String saint, Lord liege) {
    }

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
                GatewayBlock.Form form = GatewayBlock.Form.byName(g.getString("Form"));
                String citadel = g.getString("Citadel");
                records.born.add(new Gateways.Gateway(g.getString("Id"), region, pos, form == null ? GatewayBlock.Form.HEART : form,
                    citadel.isEmpty() ? null : citadel, g.getBoolean("Great")));
            }
        }

        for (Tag t : tag.getList("Destroyed", Tag.TAG_STRING)) {
            records.destroyed.add(t.getAsString());
        }

        CompoundTag lords = tag.getCompound("Lords");
        for (String citadel : lords.getAllKeys()) {
            CompoundTag lord = lords.getCompound(citadel);
            if (lord.hasUUID("Id")) {
                records.lords.put(citadel, new Lord(lord.getUUID("Id"), lord.getString("Name")));
            }
        }

        for (Tag t : tag.getList("Oaths", Tag.TAG_COMPOUND)) {
            CompoundTag o = (CompoundTag)t;
            if (o.hasUUID("Saint") && o.hasUUID("Liege")) {
                records.oaths.put(o.getUUID("Saint"), new Oath(o.getString("SaintName"), new Lord(o.getUUID("Liege"), o.getString("LiegeName"))));
            }
        }

        for (Tag t : tag.getList("Sovereigns", Tag.TAG_INT_ARRAY)) {
            records.sovereigns.add(NbtUtils.loadUUID(t));
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
            g.putString("Form", gateway.form().getSerializedName());
            if (gateway.citadel() != null) {
                g.putString("Citadel", gateway.citadel());
            }

            g.putBoolean("Great", gateway.great());
            born.add(g);
        }

        tag.put("Born", born);
        ListTag destroyed = new ListTag();
        this.destroyed.forEach(id -> destroyed.add(StringTag.valueOf(id)));
        tag.put("Destroyed", destroyed);
        CompoundTag lords = new CompoundTag();
        this.lords.forEach((citadel, lord) -> {
            CompoundTag l = new CompoundTag();
            l.putUUID("Id", lord.id());
            l.putString("Name", lord.name());
            lords.put(citadel, l);
        });
        tag.put("Lords", lords);
        ListTag oaths = new ListTag();
        this.oaths.forEach((saint, oath) -> {
            CompoundTag o = new CompoundTag();
            o.putUUID("Saint", saint);
            o.putString("SaintName", oath.saint());
            o.putUUID("Liege", oath.liege().id());
            o.putString("LiegeName", oath.liege().name());
            oaths.add(o);
        });
        tag.put("Oaths", oaths);
        ListTag sovereigns = new ListTag();
        this.sovereigns.forEach(id -> sovereigns.add(NbtUtils.createUUID(id)));
        tag.put("Sovereigns", sovereigns);
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

    @Nullable
    public Lord lord(String citadel) {
        return this.lords.get(citadel);
    }

    public void bind(String citadel, UUID id, String name) {
        this.lords.put(citadel, new Lord(id, name));
        this.setDirty();
    }

    /** Frees a Citadel, if it is bound to this soul. */
    public void release(String citadel, UUID id) {
        Lord lord = this.lords.get(citadel);
        if (lord != null && lord.id().equals(id)) {
            this.lords.remove(citadel);
            this.setDirty();
        }
    }

    public boolean destroyed(String id) {
        return this.destroyed.contains(id);
    }

    public void setDestroyed(String id, boolean destroyed) {
        if (destroyed ? this.destroyed.add(id) : this.destroyed.remove(id)) {
            this.setDirty();
        }
    }

    /** The oath a Saint has sworn, or null if they serve no one. */
    @Nullable
    public Oath oath(UUID saint) {
        return this.oaths.get(saint);
    }

    public void swear(UUID saint, String saintName, Lord liege) {
        this.oaths.put(saint, new Oath(saintName, liege));
        this.setDirty();
    }

    /** Ends a Saint's oath; true if there was one. */
    public boolean renounce(UUID saint) {
        boolean had = this.oaths.remove(saint) != null;
        if (had) {
            this.setDirty();
        }

        return had;
    }

    /** The Saints sworn to a Sovereign, by their ids. */
    public Map<UUID, Oath> vassals(UUID sovereign) {
        Map<UUID, Oath> vassals = new HashMap<>();
        this.oaths.forEach((saint, oath) -> {
            if (oath.liege().id().equals(sovereign)) {
                vassals.put(saint, oath);
            }
        });
        return vassals;
    }

    /** Whether a soul was a Sovereign when last seen, for the lords of Citadels who are not here to ask. */
    public boolean sovereign(UUID id) {
        return this.sovereigns.contains(id);
    }

    public void noteSovereign(UUID id, boolean sovereign) {
        if (sovereign ? this.sovereigns.add(id) : this.sovereigns.remove(id)) {
            this.setDirty();
        }
    }
}
