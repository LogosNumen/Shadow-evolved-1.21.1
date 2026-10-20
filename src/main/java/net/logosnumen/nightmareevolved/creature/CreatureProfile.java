package net.logosnumen.nightmareevolved.creature;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Marks a creature as a Nightmare Creature: its rank and class, whether it guards a Nightmare, and which Nightmare (if
 * any) it belongs to. Any mob can carry one, so vanilla creatures can be raised into the Spell's world.
 */
public final class CreatureProfile implements INBTSerializable<CompoundTag> {
    private CreatureRank rank = CreatureRank.DORMANT;
    private CreatureClass creatureClass = CreatureClass.BEAST;
    private boolean guardian;
    private long trial = -1L;
    private boolean present;

    public CreatureRank rank() {
        return this.rank;
    }

    public CreatureClass creatureClass() {
        return this.creatureClass;
    }

    public boolean guardian() {
        return this.guardian;
    }

    public long trial() {
        return this.trial;
    }

    /** Whether this profile has been given out (an empty one means the creature is not of the Nightmare). */
    public boolean present() {
        return this.present;
    }

    public void set(CreatureRank rank, CreatureClass creatureClass, boolean guardian, long trial) {
        this.rank = rank;
        this.creatureClass = creatureClass;
        this.guardian = guardian;
        this.trial = trial;
        this.present = true;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (this.present) {
            tag.putInt("Rank", this.rank.ordinal());
            tag.putInt("Class", this.creatureClass.ordinal());
            tag.putBoolean("Guardian", this.guardian);
            tag.putLong("Trial", this.trial);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.present = tag.contains("Rank");
        if (this.present) {
            this.rank = CreatureRank.values()[Math.max(0, Math.min(CreatureRank.values().length - 1, tag.getInt("Rank")))];
            this.creatureClass = CreatureClass.values()[Math.max(0, Math.min(CreatureClass.values().length - 1, tag.getInt("Class")))];
            this.guardian = tag.getBoolean("Guardian");
            this.trial = tag.getLong("Trial");
        }
    }
}
