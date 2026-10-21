package net.logosnumen.nightmareevolved.soul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.echo.EchoRecord;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Everything the Spell knows about one player's soul: its Rank, its soul cores and the fragments saturating them, its
 * Aspect (with the Aspect's Rank), its Flaw and Attributes, essence, which abilities are held active and which
 * Transformation is worn, cooldowns, trials passed, and what a trial holds safe for its return. Saved with the player
 * and kept through death. It also holds the soul's Memories and Echoes, and (for a Sovereign) what it keeps in its
 * Soul Sea.
 */
public final class SoulData implements INBTSerializable<CompoundTag> {
    private Rank rank = Rank.ASPIRANT;
    private String aspect = "";
    private Tier aspectTier = Tier.DORMANT;
    private String flaw = "";
    private final Set<String> attributes = new LinkedHashSet<>();
    private int cores;
    private int fragments;
    private double essence;
    private final Set<String> toggles = new LinkedHashSet<>();
    private String form = "";
    private CompoundTag flawState = new CompoundTag();
    private CompoundTag abilityState = new CompoundTag();
    private int trials;
    private boolean touched;
    private long retryAt;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private ListTag stash = new ListTag();
    private int stashedExperience;
    private CompoundTag returnPoint = new CompoundTag();
    private final List<MemoryRecord> memories = new ArrayList<>();
    private final List<EchoRecord> echoes = new ArrayList<>();
    private ListTag vault = new ListTag();
    private String citadel = "";
    private CompoundTag seaReturn = new CompoundTag();
    private int seaVisits;
    private CompoundTag dream = new CompoundTag();
    private boolean dirty = true;
    private boolean seaDirty = true;

    public Rank rank() {
        return this.rank;
    }

    /** Sets the Rank; a soul that has passed its First Nightmare always holds at least one core. */
    public void setRank(Rank rank) {
        this.rank = rank;
        if (rank == Rank.ASPIRANT) {
            this.cores = 0;
            this.fragments = 0;
        } else if (this.cores < 1) {
            this.cores = 1;
        }

        this.changed();
        this.seaChanged();
    }

    public String aspect() {
        return this.aspect;
    }

    public boolean hasAspect() {
        return !this.aspect.isEmpty();
    }

    public Tier aspectTier() {
        return this.aspectTier;
    }

    public void setAspect(String aspect, Tier tier) {
        this.aspect = aspect;
        this.aspectTier = tier;
        this.toggles.clear();
        this.form = "";
        this.abilityState = new CompoundTag();
        this.changed();
    }

    public String flaw() {
        return this.flaw;
    }

    public void setFlaw(String flaw) {
        this.flaw = flaw;
        this.flawState = new CompoundTag();
        this.changed();
    }

    public List<String> attributes() {
        return new ArrayList<>(this.attributes);
    }

    public boolean hasAttribute(String id) {
        return this.attributes.contains(id);
    }

    public void addAttribute(String id) {
        if (this.attributes.add(id)) {
            this.changed();
        }
    }

    public void removeAttribute(String id) {
        if (this.attributes.remove(id)) {
            this.changed();
        }
    }

    /** Swaps one Attribute for its evolved form, keeping its place in the list. */
    public void replaceAttribute(String from, String to) {
        List<String> list = this.attributes();
        int at = list.indexOf(from);
        if (at < 0) {
            this.addAttribute(to);
            return;
        }

        list.set(at, to);
        this.attributes.clear();
        this.attributes.addAll(list);
        this.changed();
    }

    public void clearAttributes() {
        this.attributes.clear();
        this.changed();
    }

    /** How many soul cores the soul holds: one for any Awakened, up to seven for a Divine Aspect, none for an Aspirant. */
    public int cores() {
        return this.cores;
    }

    public void setCores(int cores) {
        this.cores = Math.max(0, Math.min(SoulRules.MAX_CORES, cores));
        this.fragments = Math.min(this.fragments, this.capacity());
        this.changed();
    }

    public int fragments() {
        return this.fragments;
    }

    public int capacity() {
        return SoulRules.capacity(this.cores);
    }

    public void setFragments(int fragments) {
        this.fragments = Math.max(0, Math.min(this.capacity(), fragments));
        this.changed();
    }

    /** How full the cores are, from 0 to 1. */
    public double saturation() {
        int capacity = this.capacity();
        return capacity <= 0 ? 0.0 : (double)this.fragments / capacity;
    }

    public boolean saturated() {
        return this.capacity() > 0 && this.fragments >= this.capacity();
    }

    public double essence() {
        return this.essence;
    }

    public double maxEssence() {
        return SoulRules.maxEssence(this.rank, this.aspectTier, this.cores, this.saturation());
    }

    public void setEssence(double essence) {
        double clamped = Math.max(0.0, Math.min(this.maxEssence(), essence));
        if (clamped != this.essence) {
            this.essence = clamped;
            this.changed();
        }
    }

    /** Spends essence if there is enough; false (and nothing spent) otherwise. */
    public boolean spend(double amount) {
        if (this.essence + 1.0E-6 < amount) {
            return false;
        }

        this.setEssence(this.essence - amount);
        return true;
    }

    /** Abilities held active (toggled on), by id. */
    public Set<String> toggles() {
        return this.toggles;
    }

    public boolean toggled(String ability) {
        return this.toggles.contains(ability);
    }

    public void setToggled(String ability, boolean on) {
        if (on ? this.toggles.add(ability) : this.toggles.remove(ability)) {
            this.changed();
        }
    }

    /** The Transformation being worn, by ability id, or empty. */
    public String form() {
        return this.form;
    }

    public void setForm(String form) {
        this.form = form;
        this.changed();
    }

    /** Free-form state a Flaw keeps (time lost, growth, oaths, and so on). */
    public CompoundTag flawState() {
        return this.flawState;
    }

    /** Free-form state an Aspect's abilities keep (marks, absorbed enchantments, shadows gathered, and so on). */
    public CompoundTag abilityState() {
        return this.abilityState;
    }

    public int trials() {
        return this.trials;
    }

    public void passTrial() {
        this.trials++;
        this.changed();
    }

    /** Whether the Spell has already found this player. */
    public boolean touched() {
        return this.touched;
    }

    public void touch() {
        this.touched = true;
        this.changed();
    }

    public long retryAt() {
        return this.retryAt;
    }

    public void setRetryAt(long gameTime) {
        this.retryAt = gameTime;
        this.changed();
    }

    public long readyAt(String ability) {
        return this.cooldowns.getOrDefault(ability, 0L);
    }

    public void setReadyAt(String ability, long gameTime) {
        this.cooldowns.put(ability, gameTime);
        this.changed();
    }

    public void clearCooldowns() {
        this.cooldowns.clear();
        this.changed();
    }

    public ListTag stash() {
        return this.stash;
    }

    public void setStash(ListTag stash) {
        this.stash = stash;
    }

    public int stashedExperience() {
        return this.stashedExperience;
    }

    public void setStashedExperience(int experience) {
        this.stashedExperience = experience;
    }

    /** Where a player in a trial came from, to send them back: dimension, position and facing. */
    public CompoundTag returnPoint() {
        return this.returnPoint;
    }

    public void setReturnPoint(CompoundTag point) {
        this.returnPoint = point;
    }

    public boolean hasReturnPoint() {
        return this.returnPoint.contains("Dimension");
    }

    /** The soul's Memories, in the order they came to it. */
    public List<MemoryRecord> memories() {
        return this.memories;
    }

    @Nullable
    public MemoryRecord memory(UUID id) {
        for (MemoryRecord record : this.memories) {
            if (record.id.equals(id)) {
                return record;
            }
        }

        return null;
    }

    /** The soul's Echoes (and, for Shadow Slave, its Shadows), in the order they came to it. */
    public List<EchoRecord> echoes() {
        return this.echoes;
    }

    @Nullable
    public EchoRecord echo(UUID id) {
        for (EchoRecord record : this.echoes) {
            if (record.id.equals(id)) {
                return record;
            }
        }

        return null;
    }

    /** What a Sovereign's Soul Sea holds: plain things, kept in the soul. */
    public ListTag vault() {
        return this.vault;
    }

    public void setVault(ListTag vault) {
        this.vault = vault;
        this.seaChanged();
    }

    /** The Citadel bound to the soul, whose likeness stands in its Soul Sea, or empty. */
    public String citadel() {
        return this.citadel;
    }

    public void setCitadel(String citadel) {
        this.citadel = citadel;
        this.seaChanged();
    }

    /** Where the body was left when the mind went down into the Soul Sea. */
    public CompoundTag seaReturn() {
        return this.seaReturn;
    }

    public void setSeaReturn(CompoundTag point) {
        this.seaReturn = point;
    }

    public int seaVisits() {
        return this.seaVisits;
    }

    public void visitSea() {
        this.seaVisits++;
    }

    /** The soul's journeys into the Dream Realm: its anchor, its body left behind, its belongings on either side. */
    public CompoundTag dream() {
        return this.dream;
    }

    /** Forgets every journey the soul made (for operators starting a soul over). */
    public void resetDream() {
        this.dream = new CompoundTag();
    }

    public void changed() {
        this.dirty = true;
    }

    /** The Memories, Echoes or Soul Sea changed, for the client's view of them. */
    public void seaChanged() {
        this.seaDirty = true;
    }

    public boolean takeSeaDirty() {
        boolean was = this.seaDirty;
        this.seaDirty = false;
        return was;
    }

    /** True once after any change, for syncing to the client. */
    public boolean takeDirty() {
        boolean was = this.dirty;
        this.dirty = false;
        return was;
    }

    private static ListTag strings(Iterable<String> values) {
        ListTag list = new ListTag();
        values.forEach(value -> list.add(StringTag.valueOf(value)));
        return list;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Rank", this.rank.id());
        tag.putString("Aspect", this.aspect);
        tag.putString("AspectTier", this.aspectTier.id());
        tag.putString("Flaw", this.flaw);
        tag.put("Attributes", strings(this.attributes));
        tag.putInt("Cores", this.cores);
        tag.putInt("Fragments", this.fragments);
        tag.putDouble("Essence", this.essence);
        tag.put("Toggles", strings(this.toggles));
        tag.putString("Form", this.form);
        tag.put("FlawState", this.flawState);
        tag.put("AbilityState", this.abilityState);
        tag.putInt("Trials", this.trials);
        tag.putBoolean("Touched", this.touched);
        tag.putLong("RetryAt", this.retryAt);
        CompoundTag cooldowns = new CompoundTag();
        this.cooldowns.forEach(cooldowns::putLong);
        tag.put("Cooldowns", cooldowns);
        tag.put("Stash", this.stash);
        tag.putInt("StashedExperience", this.stashedExperience);
        tag.put("Return", this.returnPoint);
        ListTag memories = new ListTag();
        this.memories.forEach(record -> memories.add(record.save(provider)));
        tag.put("Memories", memories);
        ListTag echoes = new ListTag();
        this.echoes.forEach(record -> echoes.add(record.save()));
        tag.put("Echoes", echoes);
        tag.put("Vault", this.vault);
        tag.putString("Citadel", this.citadel);
        tag.put("SeaReturn", this.seaReturn);
        tag.putInt("SeaVisits", this.seaVisits);
        tag.put("Dream", this.dream);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.rank = tag.contains("Rank") ? Rank.byId(tag.getString("Rank")) : Rank.ASPIRANT;
        this.aspect = tag.getString("Aspect");
        this.aspectTier = Tier.byId(tag.getString("AspectTier"));
        this.flaw = tag.getString("Flaw");
        this.attributes.clear();
        tag.getList("Attributes", Tag.TAG_STRING).forEach(t -> this.attributes.add(t.getAsString()));
        this.cores = tag.getInt("Cores");
        if (this.rank != Rank.ASPIRANT && this.cores < 1) {
            this.cores = 1;
        }

        this.fragments = tag.getInt("Fragments");
        this.essence = tag.getDouble("Essence");
        this.toggles.clear();
        tag.getList("Toggles", Tag.TAG_STRING).forEach(t -> this.toggles.add(t.getAsString()));
        this.form = tag.getString("Form");
        this.flawState = tag.getCompound("FlawState");
        this.abilityState = tag.getCompound("AbilityState");
        this.trials = tag.getInt("Trials");
        this.touched = tag.getBoolean("Touched");
        this.retryAt = tag.getLong("RetryAt");
        this.cooldowns.clear();
        CompoundTag cooldowns = tag.getCompound("Cooldowns");
        for (String key : cooldowns.getAllKeys()) {
            this.cooldowns.put(key, cooldowns.getLong(key));
        }

        this.stash = tag.getList("Stash", Tag.TAG_COMPOUND);
        this.stashedExperience = tag.getInt("StashedExperience");
        this.returnPoint = tag.getCompound("Return");
        this.memories.clear();
        for (Tag t : tag.getList("Memories", Tag.TAG_COMPOUND)) {
            MemoryRecord record = MemoryRecord.load(provider, (CompoundTag)t);
            if (record != null) {
                this.memories.add(record);
            }
        }

        this.echoes.clear();
        for (Tag t : tag.getList("Echoes", Tag.TAG_COMPOUND)) {
            EchoRecord record = EchoRecord.load((CompoundTag)t);
            if (record != null) {
                this.echoes.add(record);
            }
        }

        this.vault = tag.getList("Vault", Tag.TAG_COMPOUND);
        this.citadel = tag.getString("Citadel");
        this.seaReturn = tag.getCompound("SeaReturn");
        this.seaVisits = tag.getInt("SeaVisits");
        this.dream = tag.getCompound("Dream");
        this.dirty = true;
        this.seaDirty = true;
    }
}
