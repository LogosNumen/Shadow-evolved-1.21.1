package net.logosnumen.nightmareevolved.dream.gen.shape;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
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
 * Bastion's lands, the heart of the Sword Domain. Around the tranquil lake and its castle lies a picturesque land of
 * meadows and woods; farther out, the ancient forest that Valor burned is a sea of ash, with the charred remains of the
 * titan it was lying in it and human forts standing guard here and there.
 */
public final class BastionShaper implements Shaper {
    /** The Lake of Bones, around the castle. */
    public static final double LAKE = 300.0;
    /** How far the fair land around the lake reaches before the ash begins. */
    public static final double FAIR = 900.0;
    private final long seed;
    private final Noise land;
    private final Noise cover;
    private final Noise remains;

    public BastionShaper(long seed) {
        this.seed = seed;
        this.land = new Noise(Noise.mix(seed, "bastion.land"), 3);
        this.cover = new Noise(Noise.mix(seed, "bastion.cover"), 2);
        this.remains = new Noise(Noise.mix(seed, "bastion.remains"), 2);
    }

    @Override
    public double ground(int x, int z, double edge) {
        double d = Places.BASTION.distance(x, z);
        double plain = 71.0 + 3.5 * this.land.at(x / 260.0, z / 260.0) + 1.0 * this.land.at(x / 50.0, 4.0, z / 50.0);
        if (d < LAKE) {
            return 63.0 - 32.0 * Noise.smooth(1.0 - d / LAKE);
        }

        if (d < LAKE + 70.0) {
            return Mth.lerp(Noise.smooth((d - LAKE) / 70.0), 63.0, plain);
        }

        return plain;
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        double d = Places.BASTION.distance(x, z);
        double fair = 1.0 - Noise.smooth((d - FAIR) / 220.0 + 0.3 * this.cover.at(x / 120.0, z / 120.0));
        if (d < LAKE + 6.0) {
            Ground.lay(column, top, Blocks.GRAVEL.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), 3);
            column.flood(top + 1, Ground.SEA_LEVEL, Ground.WATER);
            return;
        }

        if (fair > 0.5) {
            Ground.lay(column, top, Blocks.DIRT.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState(), 4);
            double c = this.cover.at(x / 6.0, z / 6.0);
            if (c > 0.45) {
                column.set(top + 1, c > 0.62 ? Blocks.CORNFLOWER.defaultBlockState() : Blocks.POPPY.defaultBlockState());
            } else if (c > 0.1) {
                column.set(top + 1, Blocks.SHORT_GRASS.defaultBlockState());
            }

            Trees.paint(column, 14, this::fairTree);
            return;
        }

        Ground.lay(column, top, Blocks.COARSE_DIRT.defaultBlockState(), DreamBlocks.ASH.get().defaultBlockState(), 4);
        column.fill(top - 1, top, DreamBlocks.ASH.get().defaultBlockState());
        int layers = (int)Math.round(1.5 + 2.0 * this.cover.at(x / 11.0, z / 11.0));
        if (layers > 0) {
            column.set(top + 1, DreamBlocks.ASH_LAYER.get().depth(layers));
        }

        // The charred remains of the forest-titan: great burnt limbs half sunk in the ash.
        double limb = Math.abs(this.remains.at(x / 85.0, z / 85.0));
        if (limb < 0.03 && this.remains.at(x / 400.0, 7.0, z / 400.0) > 0.0) {
            int rise = (int)(10.0 * (1.0 - limb / 0.03));
            column.fill(top - 2, top + rise, DreamBlocks.CHARRED_LOG.get().defaultBlockState());
        }

        this.forts(column, top);
    }

    @Nullable
    private Trees.Tree fairTree(int i, int j) {
        if (Noise.hash(this.seed, i, j, 121) > 0.22) {
            return null;
        }

        int x = i * 14 + 3 + (int)(Noise.hash(this.seed, i, j, 122) * 8);
        int z = j * 14 + 3 + (int)(Noise.hash(this.seed, i, j, 123) * 8);
        double d = Places.BASTION.distance(x, z);
        if (d < LAKE + 20.0 || d > FAIR) {
            return null;
        }

        int base = Mth.floor(this.ground(x, z, 500.0)) + 1;
        boolean birch = Noise.hash(this.seed, i, j, 124) < 0.4;
        return new Trees.Tree(x, z, base, 6 + (int)(Noise.hash(this.seed, i, j, 125) * 4), 3,
            birch ? Blocks.BIRCH_LOG.defaultBlockState() : Blocks.OAK_LOG.defaultBlockState(),
            birch ? Blocks.BIRCH_LEAVES.defaultBlockState() : Blocks.OAK_LEAVES.defaultBlockState(), Trees.Crown.ROUND);
    }

    /** Small stone forts in the ash: a square of walls with a tower at one corner. */
    private void forts(Column column, int top) {
        int cell = 520;
        int ci = Math.floorDiv(column.x, cell);
        int cj = Math.floorDiv(column.z, cell);
        if (Noise.hash(this.seed, ci, cj, 131) > 0.45) {
            return;
        }

        int fx = ci * cell + 80 + (int)(Noise.hash(this.seed, ci, cj, 132) * (cell - 160));
        int fz = cj * cell + 80 + (int)(Noise.hash(this.seed, ci, cj, 133) * (cell - 160));
        int dx = Math.abs(column.x - fx);
        int dz = Math.abs(column.z - fz);
        int half = 11;
        if (dx > half || dz > half || Places.BASTION.distance(fx, fz) < FAIR) {
            return;
        }

        int base = Mth.floor(this.ground(fx, fz, 500.0));
        BlockState stone = Noise.hash(this.seed, column.x, column.z, 134) < 0.2 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
            : Blocks.STONE_BRICKS.defaultBlockState();
        column.fill(base - 3, base, stone);
        column.clear(base + 1, base + 12);
        boolean wall = dx == half || dz == half;
        boolean gate = dz == half && column.z > fz && dx <= 1;
        if (wall && !gate) {
            column.fill(base + 1, base + 5, stone);
            if ((dx + dz) % 2 == 0) {
                column.set(base + 6, stone);
            }
        }

        if (column.x - fx >= half - 3 && column.z - fz <= -half + 3) {
            column.fill(base + 1, base + 11, stone);
            column.set(base + 12, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }
    }
}
