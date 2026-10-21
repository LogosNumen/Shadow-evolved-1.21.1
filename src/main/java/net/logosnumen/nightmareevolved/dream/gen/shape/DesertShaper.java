package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.logosnumen.nightmareevolved.dream.gen.Trees;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Nightmare Desert, the Battlefield of the Gods: flawless white dunes under a blinding sun, with inky black ruins
 * jutting out of the sand like bones, and here and there a tree with scarlet leaves and pale fruit.
 */
public final class DesertShaper implements Shaper {
    private static final int RUIN_CELL = 80;
    private final long seed;
    private final Noise swell;
    private final Noise dunes;
    private final Noise ripples;
    private final BlockState sand;
    private final BlockState[] ruin = {
        Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),
        Blocks.BLACKSTONE.defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState()
    };

    public DesertShaper(long seed) {
        this.seed = seed;
        this.swell = new Noise(Noise.mix(seed, "desert.swell"), 2);
        this.dunes = new Noise(Noise.mix(seed, "desert.dunes"), 3);
        this.ripples = new Noise(Noise.mix(seed, "desert.ripples"), 1);
        this.sand = DreamBlocks.PALE_SAND.get().defaultBlockState();
    }

    @Override
    public double ground(int x, int z, double edge) {
        double crest = Math.pow(this.dunes.unit(x / 96.0 + z / 300.0, z / 42.0), 1.7);
        return 69.0 + 7.0 * this.swell.at(x / 430.0, z / 430.0) + 12.0 * crest + 0.6 * this.ripples.at(x / 6.0, z / 3.0);
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int top = Mth.floor(height);
        Ground.stone(column, top);
        column.fill(top - 9, top - 6, Blocks.CALCITE.defaultBlockState());
        column.fill(top - 5, top, this.sand);
        this.ruins(column, top);
        Trees.paint(column, 640, this::scarletTree);
    }

    /** Black ruins half sunk in the sand: leaning pillars, broken walls, and now and then an arch. */
    private void ruins(Column column, int top) {
        int cx = Math.floorDiv(column.x, RUIN_CELL);
        int cz = Math.floorDiv(column.z, RUIN_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int i = cx + dx;
                int j = cz + dz;
                if (Noise.hash(this.seed, i, j, 31) > 0.3) {
                    continue;
                }

                double ox = i * RUIN_CELL + RUIN_CELL * (0.2 + 0.6 * Noise.hash(this.seed, i, j, 32));
                double oz = j * RUIN_CELL + RUIN_CELL * (0.2 + 0.6 * Noise.hash(this.seed, i, j, 33));
                int kind = (int)(Noise.hash(this.seed, i, j, 34) * 3);
                double angle = Noise.hash(this.seed, i, j, 35) * Math.PI;
                int tall = 6 + (int)(Noise.hash(this.seed, i, j, 36) * 18);
                double u = (column.x - ox) * Math.cos(angle) + (column.z - oz) * Math.sin(angle);
                double v = -(column.x - ox) * Math.sin(angle) + (column.z - oz) * Math.cos(angle);
                BlockState stone = this.ruin[(int)(Noise.hash(this.seed, column.x, column.z, 37) * this.ruin.length)];
                int base = top - 3;
                switch (kind) {
                    case 0 -> {
                        // A leaning pillar: it drifts sideways as it rises.
                        double lean = (Noise.hash(this.seed, i, j, 38) - 0.5) * 0.5;
                        for (int y = 0; y <= tall; y++) {
                            double pu = u - lean * y;
                            if (pu * pu + v * v <= 2.3) {
                                column.set(base + y, stone);
                            }
                        }
                    }
                    case 1 -> {
                        // A broken wall with a ragged top.
                        if (Math.abs(v) <= 1.0 && Math.abs(u) <= 9.0 + tall * 0.4) {
                            int h = (int)(tall * 0.6 * (0.4 + 0.6 * Noise.hash(this.seed, (int)Math.floor(u), j, 39)));
                            column.fill(base, top + h, stone);
                        }
                    }
                    default -> {
                        // An arch: two legs and a curve between them.
                        double span = 5.0 + tall * 0.3;
                        if (Math.abs(v) <= 1.5 && Math.abs(u) <= span + 1.5) {
                            double inner = Math.sqrt(Math.max(0.0, span * span - u * u));
                            int crown = top + (int)(span + 2);
                            if (Math.abs(u) >= span - 1.5) {
                                column.fill(base, crown, stone);
                            } else {
                                column.fill(top + (int)inner, crown, stone);
                            }
                        }
                    }
                }
            }
        }
    }

    /** The rare scarlet-leaved tree, its branches hung with pale fruit. */
    private Trees.Tree scarletTree(int i, int j) {
        if (Noise.hash(this.seed, i, j, 41) > 0.35) {
            return null;
        }

        int x = i * 640 + 160 + (int)(Noise.hash(this.seed, i, j, 42) * 320);
        int z = j * 640 + 160 + (int)(Noise.hash(this.seed, i, j, 43) * 320);
        int base = Mth.floor(this.ground(x, z, 0.0)) + 1;
        return new Trees.Tree(x, z, base, 9, 5, DreamBlocks.BLACK_BARK_LOG.get().defaultBlockState(),
            DreamBlocks.BLOOD_LEAVES.get().defaultBlockState(), Trees.Crown.ROUND);
    }
}
