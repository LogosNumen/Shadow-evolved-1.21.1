package net.logosnumen.nightmareevolved.dream.gen;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

/**
 * A named place built into the Dream Realm: painted into each column it covers after the land is shaped, so it never
 * depends on which chunk is made first. What blocks alone can't hold (chests and their loot, a Gateway, a Seed of
 * Nightmare) it sets up when its chunks are decorated.
 */
public interface Landmark {
    int minX();

    int maxX();

    int minZ();

    int maxZ();

    /** Paints the landmark into a column inside its bounds. */
    void paint(Column column, RealmTerrain terrain);

    /** Sets up the block entities of the part of the landmark in one chunk. */
    default void decorate(WorldGenLevel level, ChunkPos chunk, RealmTerrain terrain) {
    }

    default boolean covers(int x, int z) {
        return x >= this.minX() && x <= this.maxX() && z >= this.minZ() && z <= this.maxZ();
    }

    default boolean touches(ChunkPos chunk) {
        return chunk.getMaxBlockX() >= this.minX() && chunk.getMinBlockX() <= this.maxX() && chunk.getMaxBlockZ() >= this.minZ()
            && chunk.getMinBlockZ() <= this.maxZ();
    }
}
