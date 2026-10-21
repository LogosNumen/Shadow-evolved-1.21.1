package net.logosnumen.nightmareevolved.echo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * An Echo as its owner's soul keeps it: a frozen copy of a slain creature (its kind, and everything about it at the
 * moment it died), its Rank and class, the Attributes the Spell found in it, how whole it is, and whether it is out.
 * Sunny's Aspect can make an Echo into a Shadow, which, unlike an Echo, grows.
 */
public final class EchoRecord {
    public final UUID id;
    /** The creature's kind, by entity type id. */
    public final String type;
    /** The creature as it was when it died, less where it stood and how hurt it was. */
    public final CompoundTag snapshot;
    public final int rank;
    public final int cls;
    public final List<String> traits;
    /** How whole it is, from 0 (nearly gone) to 1. */
    public float health = 1.0F;
    public boolean out;
    /** The summoned body, while it is out. */
    @Nullable
    public UUID body;
    public boolean shadow;
    /** Until when a badly hurt Shadow sleeps, unable to be called. */
    public long sleepsUntil;

    public EchoRecord(UUID id, String type, CompoundTag snapshot, int rank, int cls, List<String> traits) {
        this.id = id;
        this.type = type;
        this.snapshot = snapshot;
        this.rank = rank;
        this.cls = cls;
        this.traits = new ArrayList<>(traits);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.id);
        tag.putString("Type", this.type);
        tag.put("Snapshot", this.snapshot);
        tag.putInt("Rank", this.rank);
        tag.putInt("Class", this.cls);
        ListTag traits = new ListTag();
        this.traits.forEach(t -> traits.add(StringTag.valueOf(t)));
        tag.put("Traits", traits);
        tag.putFloat("Health", this.health);
        tag.putBoolean("Out", this.out);
        if (this.body != null) {
            tag.putUUID("Body", this.body);
        }

        tag.putBoolean("Shadow", this.shadow);
        tag.putLong("Sleeps", this.sleepsUntil);
        return tag;
    }

    @Nullable
    public static EchoRecord load(CompoundTag tag) {
        if (!tag.hasUUID("Id") || tag.getString("Type").isEmpty()) {
            return null;
        }

        List<String> traits = new ArrayList<>();
        tag.getList("Traits", Tag.TAG_STRING).forEach(t -> traits.add(t.getAsString()));
        EchoRecord record = new EchoRecord(tag.getUUID("Id"), tag.getString("Type"), tag.getCompound("Snapshot"), tag.getInt("Rank"), tag.getInt("Class"), traits);
        record.health = tag.contains("Health") ? tag.getFloat("Health") : 1.0F;
        record.out = tag.getBoolean("Out");
        record.body = tag.hasUUID("Body") ? tag.getUUID("Body") : null;
        record.shadow = tag.getBoolean("Shadow");
        record.sleepsUntil = tag.getLong("Sleeps");
        return record;
    }
}
