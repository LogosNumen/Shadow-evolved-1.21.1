package net.logosnumen.nightmareevolved.soul;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Everything the Spell knows about one player's soul: rank, Aspect, essence, how saturated the soul cores are, how many
 * trials it has passed, ability cooldowns, and belongings held safe through a death inside a Nightmare. Saved with the
 * player and carried through death.
 */
public final class SoulData implements INBTSerializable<CompoundTag> {
    private Rank rank = Rank.ASPIRANT;
    private String aspect = "";
    private AspectTier aspectTier = AspectTier.DORMANT;
    private double essence;
    private double saturation;
    private int trials;
    private boolean touched;
    private long retryAt;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private ListTag stash = new ListTag();
    private int stashedExperience;
    private CompoundTag returnPoint = new CompoundTag();
    private boolean dirty = true;

    public Rank rank() {
        return this.rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
        this.changed();
    }

    public String aspect() {
        return this.aspect;
    }

    public boolean hasAspect() {
        return !this.aspect.isEmpty();
    }

    public void setAspect(String aspect, AspectTier tier) {
        this.aspect = aspect;
        this.aspectTier = tier;
        this.changed();
    }

    public AspectTier aspectTier() {
        return this.aspectTier;
    }

    public double essence() {
        return this.essence;
    }

    public double maxEssence() {
        return SoulRules.maxEssence(this.rank, this.aspectTier);
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

    public double saturation() {
        return this.saturation;
    }

    public double capacity() {
        return SoulRules.capacity(this.rank);
    }

    public void setSaturation(double saturation) {
        this.saturation = Math.max(0.0, Math.min(this.capacity(), saturation));
        this.changed();
    }

    /** Whether every soul core is full, so the soul can face its next trial. */
    public boolean saturated() {
        return this.capacity() > 0.0 && this.saturation >= this.capacity() - 1.0E-6;
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

    public void changed() {
        this.dirty = true;
    }

    /** True once after any change, for syncing to the client. */
    public boolean takeDirty() {
        boolean was = this.dirty;
        this.dirty = false;
        return was;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Rank", this.rank.id());
        tag.putString("Aspect", this.aspect);
        tag.putInt("AspectTier", this.aspectTier.ordinal());
        tag.putDouble("Essence", this.essence);
        tag.putDouble("Saturation", this.saturation);
        tag.putInt("Trials", this.trials);
        tag.putBoolean("Touched", this.touched);
        tag.putLong("RetryAt", this.retryAt);
        CompoundTag cooldowns = new CompoundTag();
        this.cooldowns.forEach(cooldowns::putLong);
        tag.put("Cooldowns", cooldowns);
        tag.put("Stash", this.stash);
        tag.putInt("StashedExperience", this.stashedExperience);
        tag.put("Return", this.returnPoint);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.rank = Rank.SLEEPER;
        for (Rank r : Rank.values()) {
            if (r.id().equals(tag.getString("Rank"))) {
                this.rank = r;
            }
        }

        if (!tag.contains("Rank")) {
            this.rank = Rank.ASPIRANT;
        }

        this.aspect = tag.getString("Aspect");
        this.aspectTier = AspectTier.byOrdinal(tag.getInt("AspectTier"));
        this.essence = tag.getDouble("Essence");
        this.saturation = tag.getDouble("Saturation");
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
        this.dirty = true;
    }
}
