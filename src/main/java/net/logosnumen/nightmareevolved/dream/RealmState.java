package net.logosnumen.nightmareevolved.dream;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * What has changed in the Dream Realm for good, world-wide: whether the Dark Sea has been sealed into the Crimson
 * Spire. Kept with the overworld's data.
 */
public final class RealmState extends SavedData {
    private static final String NAME = "nightmare_evolved_dream_realm";
    private boolean darkSeaSealed;

    public static RealmState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(RealmState::new, RealmState::load, null), NAME);
    }

    private static RealmState load(CompoundTag tag, HolderLookup.Provider provider) {
        RealmState state = new RealmState();
        state.darkSeaSealed = tag.getBoolean("DarkSeaSealed");
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("DarkSeaSealed", this.darkSeaSealed);
        return tag;
    }

    public boolean darkSeaSealed() {
        return this.darkSeaSealed;
    }

    public void setDarkSeaSealed(boolean sealed) {
        if (this.darkSeaSealed != sealed) {
            this.darkSeaSealed = sealed;
            this.setDirty();
        }
    }
}
