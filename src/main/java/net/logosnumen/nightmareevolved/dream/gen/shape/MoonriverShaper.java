package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Moonriver Plain: a bleak, rocky land under a distant sun, cold, with a little snow. Deep canyons cut it, rushing
 * rivers at their bottoms, and the stone bridges once built over them have mostly fallen. Ancient ruins stand above the
 * ground like forlorn obelisks. At night three moons light it.
 */
public final class MoonriverShaper implements Shaper {
    public static final int RIVER = 46;
    private static final int RUIN_CELL = 96;
    private static final int BRIDGE_CELL = 260;
    private final long seed;
    private final Noise plain;
    private final Noise rocks;
    private final Noise canyon;
    private final Noise cover;
    private final BlockState[] surface = {
        Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(),
        Blocks.STONE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState()
    };
    private final BlockState[] ruin = {
        Blocks.STONE_BRICKS.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
        Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
    };

    public MoonriverShaper(long seed) {
        this.seed = seed;
        this.plain = new Noise(Noise.mix(seed, "moonriver.plain"), 3);
        this.rocks = new Noise(Noise.mix(seed, "moonriver.rocks"), 2);
        this.canyon = new Noise(Noise.mix(seed, "moonriver.canyon"), 2);
        this.cover = new Noise(Noise.mix(seed, "moonriver.cover"), 1);
    }

    /** How deep the canyon cuts at a place, 0 outside one. */
    private double canyonDepth(int x, int z) {
        double c = Math.abs(this.canyon.at(x / 620.0, z / 620.0));
        if (c > 0.036) {
            return 0.0;
        }

        return 44.0 * Math.pow(1.0 - c / 0.036, 0.35);
    }

    @Override
    public double ground(int x, int z, double edge) {
        double rocky = Math.max(0.0, this.rocks.at(x / 30.0, z / 30.0)) * 4.0;
        double land = 86.0 + 5.0 * this.plain.at(x / 190.0, z / 190.0) + 2.5 * this.plain.at(x / 47.0, 9.0, z / 47.0) + rocky;
        return land - this.canyonDepth(x, z) * Noise.smooth(edge / 64.0);
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        Ground.stone(column, top);
        BlockState cover = this.surface[(int)(this.cover.unit(x / 9.0, z / 9.0) * (this.surface.length - 0.01))];
        column.set(top, cover);
        double depth = this.canyonDepth(x, z);
        if (depth > 26.0 && top < RIVER) {
            column.flood(top + 1, RIVER, Ground.WATER);
        } else if (depth < 4.0 && Noise.hash(this.seed, x, z, 51) < 0.22) {
            column.set(top + 1, Blocks.SNOW.defaultBlockState());
        }

        this.obelisks(column, top);
        this.bridges(column, depth);
    }

    /** Ruins standing like obelisks: square shafts, narrowing toward a broken top. */
    private void obelisks(Column column, int top) {
        int cx = Math.floorDiv(column.x, RUIN_CELL);
        int cz = Math.floorDiv(column.z, RUIN_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int i = cx + dx;
                int j = cz + dz;
                if (Noise.hash(this.seed, i, j, 52) > 0.3) {
                    continue;
                }

                int ox = i * RUIN_CELL + 20 + (int)(Noise.hash(this.seed, i, j, 53) * (RUIN_CELL - 40));
                int oz = j * RUIN_CELL + 20 + (int)(Noise.hash(this.seed, i, j, 54) * (RUIN_CELL - 40));
                int half = 1 + (int)(Noise.hash(this.seed, i, j, 55) * 2);
                int tall = 12 + (int)(Noise.hash(this.seed, i, j, 56) * 18);
                int ux = Math.abs(column.x - ox);
                int uz = Math.abs(column.z - oz);
                if (ux > half || uz > half) {
                    continue;
                }

                int base = Mth.floor(this.ground(ox, oz, 64.0));
                int broken = tall - (int)(Noise.hash(this.seed, column.x, column.z, 57) * 4);
                for (int y = -2; y <= broken; y++) {
                    int narrow = y > tall * 2 / 3 && half > 0 ? 1 : 0;
                    if (ux <= half - narrow && uz <= half - narrow) {
                        column.set(base + y, this.ruin[(int)(Noise.hash(this.seed, column.x + y, column.z, 58) * this.ruin.length)]);
                    }
                }
            }
        }
    }

    /** The old bridges over the canyons: decks at the rim's height, with gaps where they fell. */
    private void bridges(Column column, double depth) {
        if (depth < 10.0) {
            return;
        }

        int cx = Math.floorDiv(column.x, BRIDGE_CELL);
        int cz = Math.floorDiv(column.z, BRIDGE_CELL);
        boolean alongX = Noise.hash(this.seed, cx, cz, 61) < 0.5;
        int line = (alongX ? cz : cx) * BRIDGE_CELL + BRIDGE_CELL / 2 + (int)(Noise.hash(this.seed, cx, cz, 62) * 60 - 30);
        int off = alongX ? column.z - line : column.x - line;
        if (Math.abs(off) > 2) {
            return;
        }

        int along = alongX ? column.x : column.z;
        if (Noise.hash(this.seed, along >> 3, line, 63) < 0.35) {
            // Fallen here.
            return;
        }

        int deck = 84 + (int)(3.0 * this.plain.at(column.x / 190.0, column.z / 190.0));
        BlockState stone = this.ruin[(int)(Noise.hash(this.seed, column.x, column.z, 64) * this.ruin.length)];
        column.set(deck, stone);
        if (Math.abs(off) == 2) {
            column.set(deck + 1, Blocks.STONE_BRICK_WALL.defaultBlockState());
        }

        if (Math.floorMod(along, 18) < 2) {
            // A pier under the deck.
            column.fill(Mth.floor(deck - depth), deck - 1, stone);
        }
    }
}
