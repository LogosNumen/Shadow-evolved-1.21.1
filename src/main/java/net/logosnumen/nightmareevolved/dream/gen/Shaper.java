package net.logosnumen.nightmareevolved.dream.gen;

import net.logosnumen.nightmareevolved.dream.RealmMap;

/** Shapes one region's land. Each region has one, made for the world's seed. */
public interface Shaper {
    /**
     * The height of the bare land at a place, for blending with neighbouring regions. Regions with no ground (the open
     * sky of the Chained Isles) answer far below the world.
     *
     * @param edge how far the place lies inside this region, in blocks (0 when the region is only a neighbour here)
     */
    double ground(int x, int z, double edge);

    /**
     * Builds a column where this region has the most say: its ground up to the blended height, and whatever grows,
     * lies or stands on it.
     */
    void build(Column column, double height, RealmMap.Blend blend);
}
