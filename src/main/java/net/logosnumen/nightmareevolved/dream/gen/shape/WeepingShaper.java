package net.logosnumen.nightmareevolved.dream.gen.shape;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.logosnumen.nightmareevolved.dream.gen.Trees;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Weeping Goddess, in the Song Domain: deep canyons cut a high plateau and run together, and where they overflow
 * the plateau's edge the Great Waterfall pours down into the Lake of Tears. From the lake the River of Tears runs south
 * through green lowlands, down the giant's stairs of Rivergate's locks, to its estuary on the Stormsea's dusky shore.
 * <p>
 * The shape of it is the same in every world, since the citadels are built on it.
 */
public final class WeepingShaper implements Shaper {
    public static final int PLATEAU = 168;
    public static final int CANYON_WATER = 132;
    public static final int LAKE = 100;
    public static final int LOWLAND = 106;
    public static final int COAST = 68;
    public static final int LOCKS_START = 3700;
    public static final int LOCK_LENGTH = 44;
    public static final int LOCKS = 5;
    public static final int LOCKS_END = LOCKS_START + LOCK_LENGTH * LOCKS;
    private static final double LAKE_Z = 2960.0;
    private final long seed;
    private final Noise land;
    private final Noise cover;

    public WeepingShaper(long seed) {
        this.seed = seed;
        this.land = new Noise(Noise.mix(seed, "weeping.land"), 2);
        this.cover = new Noise(Noise.mix(seed, "weeping.cover"), 1);
    }

    public static int fallsX() {
        return Places.WEEPING_GODDESS.x();
    }

    /** Where the plateau ends in the cliff of the Great Waterfall, bending away south on both sides of the falls. */
    public static double cliffZ(double x) {
        double dx = x - fallsX();
        return Places.WEEPING_GODDESS.z() + 0.0009 * dx * dx;
    }

    /** How far a place lies from the middle of the Lake of Tears, as a fraction of its shore: under 1 in it. */
    public static double lake(double x, double z) {
        double dx = (x - fallsX()) / 300.0;
        double dz = (z - LAKE_Z) / 225.0;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** The middle of the River of Tears at a z, south of the lake. */
    public static double riverX(double z) {
        return fallsX() + 90.0 * Math.sin((z - 3180.0) / 260.0);
    }

    /** The water level in the river (or a lock) at a z. */
    public static int riverLevel(double z) {
        if (z < LOCKS_START) {
            return LAKE;
        }

        if (z >= LOCKS_END) {
            return Ground.SEA_LEVEL;
        }

        int step = (int)((z - LOCKS_START) / LOCK_LENGTH);
        return LAKE - Math.round(step * (LAKE - Ground.SEA_LEVEL - 4) / (float)(LOCKS - 1));
    }

    /** How deep a plateau canyon cuts at a place: three canyons run together at the falls. */
    private static double canyon(double x, double z) {
        double best = 0.0;
        double fx = fallsX();
        double fz = Places.WEEPING_GODDESS.z();
        for (int k = 0; k < 3; k++) {
            double angle = Math.toRadians(-135.0 + 45.0 * k);
            double ux = Math.cos(angle);
            double uz = Math.sin(angle);
            double ox = x - fx;
            double oz = z - fz;
            double along = ox * ux + oz * uz;
            if (along < -10.0 || along > 950.0) {
                continue;
            }

            double across = Math.abs(-ox * uz + oz * ux);
            double half = 22.0 - 10.0 * (along / 950.0);
            if (across < half) {
                best = Math.max(best, 1.0 - across / half);
            }
        }

        return best;
    }

    @Override
    public double ground(int x, int z, double edge) {
        double cliff = cliffZ(x);
        double bumps = 4.0 * this.land.at(x / 150.0, z / 150.0);
        if (z < cliff) {
            double c = canyon(x, z);
            double plateau = PLATEAU + bumps;
            return c > 0.0 ? Mth.lerp(Math.pow(c, 0.4), plateau, CANYON_WATER - 4) : plateau;
        }

        if (z < LOCKS_START) {
            double low = LOWLAND + bumps * 0.6;
            double lake = lake(x, z);
            if (lake < 1.0) {
                return LAKE - 30.0 * Noise.smooth((1.0 - lake) * 2.0);
            }

            if (lake < 1.12) {
                return Mth.lerp(Noise.smooth((lake - 1.0) / 0.12), LAKE - 1, low);
            }

            double across = Math.abs(x - riverX(z));
            if (z > LAKE_Z && across < 26.0) {
                return across < 15.0 ? LAKE - 8 : Mth.lerp((across - 15.0) / 11.0, LAKE - 1, low);
            }

            return low;
        }

        double across = Math.abs(x - riverX(z));
        int level = riverLevel(z);
        if (z < LOCKS_END) {
            int step = (z - LOCKS_START) / LOCK_LENGTH;
            double land = LOWLAND - (LOWLAND - COAST) * (step + 1.0) / LOCKS + bumps * 0.4;
            return across < 18.0 ? level - 8 : land;
        }

        double coast = COAST + bumps * 0.4;
        return across < 16.0 ? Ground.SEA_LEVEL - 6 : coast;
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        double cliff = cliffZ(x);
        boolean water = false;
        int level = 0;
        if (z < cliff) {
            if (canyon(x, z) > 0.0 && top < CANYON_WATER) {
                water = true;
                level = CANYON_WATER;
            }
        } else if (lake(x, z) < 1.05 && top < LAKE) {
            water = true;
            level = LAKE;
        } else if (Math.abs(x - riverX(z)) < 18.0 && z > LAKE_Z) {
            level = riverLevel(z);
            water = top < level;
        }

        Ground.lay(column, top, Blocks.DIRT.defaultBlockState(), water ? Blocks.GRAVEL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), 4);
        if (top > PLATEAU - 20) {
            column.set(top, this.cover.at(x / 8.0, z / 8.0) > 0.3 ? Blocks.STONE.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
        }

        if (water) {
            column.flood(top + 1, level, Ground.WATER);
        } else if (column.get(top) != null && column.get(top).is(Blocks.GRASS_BLOCK)) {
            double c = this.cover.at(x / 5.0, z / 5.0);
            if (c > 0.4) {
                column.set(top + 1, Blocks.SHORT_GRASS.defaultBlockState());
            } else if (c < -0.6) {
                column.set(top + 1, Blocks.BLUE_ORCHID.defaultBlockState());
            }

            Trees.paint(column, 16, this::tree);
        }

        this.falls(column, cliff);
    }

    /** The Great Waterfall: where the canyons' rivers spill over the cliff, a sheet of falling water down to the lake. */
    private void falls(Column column, double cliff) {
        int x = column.x;
        int z = column.z;
        if (Math.abs(x - fallsX()) > 24 || z < cliff - 1.0 || z > cliff + 3.0) {
            return;
        }

        BlockState falling = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 8);
        int bottom = Math.max(LAKE, column.topSolid() + 1);
        for (int y = bottom; y <= CANYON_WATER; y++) {
            column.set(y, falling);
        }
    }

    @Nullable
    private Trees.Tree tree(int i, int j) {
        if (Noise.hash(this.seed, i, j, 191) > 0.18) {
            return null;
        }

        int x = i * 16 + 3 + (int)(Noise.hash(this.seed, i, j, 192) * 10);
        int z = j * 16 + 3 + (int)(Noise.hash(this.seed, i, j, 193) * 10);
        if (lake(x, z) < 1.15 || Math.abs(x - riverX(z)) < 30.0 || Math.abs(z - cliffZ(x)) < 12.0) {
            return null;
        }

        int base = Mth.floor(this.ground(x, z, 500.0)) + 1;
        boolean spruce = z < cliffZ(x);
        return new Trees.Tree(x, z, base, 7 + (int)(Noise.hash(this.seed, i, j, 194) * 5), spruce ? 3 : 4,
            spruce ? Blocks.SPRUCE_LOG.defaultBlockState() : Blocks.BIRCH_LOG.defaultBlockState(),
            spruce ? Blocks.SPRUCE_LEAVES.defaultBlockState() : Blocks.BIRCH_LEAVES.defaultBlockState(), spruce ? Trees.Crown.CONE : Trees.Crown.ROUND);
    }
}
