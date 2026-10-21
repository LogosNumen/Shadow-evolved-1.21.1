package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ravenheart's land of snow and ash, at the northern edge of the Song Domain: snowy peaks to the north, a bottomless
 * chasm across the land, and beyond it a fuming volcano with a city of black stone on its slopes. Hot springs steam in
 * the cold. The palace and the great bridge over the chasm are built on top of this.
 */
public final class RavenheartShaper implements Shaper {
    /** How wide the chasm is, roughly. */
    public static final double CHASM = 38.0;
    public static final double VOLCANO = 520.0;
    private final long seed;
    private final Noise peaks;
    private final Noise chasmLine;
    private final Noise rough;
    private final Noise snow;

    public RavenheartShaper(long seed) {
        this.seed = seed;
        this.peaks = new Noise(Noise.mix(seed, "ravenheart.peaks"), 3);
        this.chasmLine = new Noise(0x7A5E_C4A5L, 2);
        this.rough = new Noise(Noise.mix(seed, "ravenheart.rough"), 2);
        this.snow = new Noise(Noise.mix(seed, "ravenheart.snow"), 1);
    }

    /** Where the chasm runs, as a z for each x: the same in every world, since the palace and bridge stand on it. */
    public static double chasmZ(double x) {
        return Places.RAVENHEART.z() + 230.0 + 36.0 * Math.sin((x - Places.RAVENHEART.x()) / 260.0) * Math.min(1.0, Math.abs(x - Places.RAVENHEART.x()) / 300.0);
    }

    public static double chasmWidth(double x) {
        return CHASM + 8.0 * Math.sin(x / 97.0);
    }

    @Override
    public double ground(int x, int z, double edge) {
        double cz = chasmZ(x);
        double inside = Noise.smooth(edge / 300.0);
        if (z < cz) {
            // North of the chasm, the mountains climb toward the peaks.
            double north = Noise.smooth((cz - z) / 700.0);
            double ridge = Math.pow(this.peaks.ridge(x / 260.0, z / 260.0), 1.8);
            return 112.0 + inside * (north * 120.0 + ridge * 70.0 * north) + 4.0 * this.rough.at(x / 40.0, z / 40.0);
        }

        double d = Places.RAVENHEART_VOLCANO.distance(x, z);
        double land = 104.0 + 5.0 * this.rough.at(x / 90.0, z / 90.0);
        if (d < VOLCANO) {
            double cone = 252.0 * Math.pow(1.0 - d / VOLCANO, 1.35);
            double crater = d < 48.0 ? -26.0 * (1.0 - d / 48.0) : 0.0;
            return Math.max(land, 104.0 + cone * inside + crater);
        }

        return land;
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        double cz = chasmZ(x);
        double d = Places.RAVENHEART_VOLCANO.distance(x, z);
        Ground.stone(column, top);
        if (z > cz && d < VOLCANO) {
            // The volcano: black rock, ash, and a lake of fire in its crater.
            column.fill(top - 3, top, Blocks.BASALT.defaultBlockState());
            column.set(top, this.snow.at(x / 12.0, z / 12.0) > 0.4 ? DreamBlocks.ASH.get().defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
            if (d < 44.0) {
                column.flood(top + 1, 344, Blocks.LAVA.defaultBlockState());
            }
        } else {
            boolean ashen = z > cz;
            double cover = this.snow.at(x / 16.0, z / 16.0);
            column.fill(top - 2, top, ashen ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState());
            BlockState surface = ashen && cover < -0.1 ? DreamBlocks.ASH.get().defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
            column.set(top, surface);
            if (ashen && cover > 0.3) {
                column.set(top + 1, Blocks.SNOW.defaultBlockState());
            }
        }

        // Hot springs: still pools over warm rock.
        int cell = 150;
        int ci = Math.floorDiv(x, cell);
        int cj = Math.floorDiv(z, cell);
        if (Noise.hash(this.seed, ci, cj, 111) < 0.3 && z > cz + 60) {
            double px = ci * cell + cell * Noise.hash(this.seed, ci, cj, 112);
            double pz = cj * cell + cell * Noise.hash(this.seed, ci, cj, 113);
            double r = 3.0 + 4.0 * Noise.hash(this.seed, ci, cj, 114);
            double dd = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz));
            if (dd < r && d > 60.0) {
                int depth = 1 + (int)(2.0 * (1.0 - dd / r));
                column.set(top - depth - 1, Blocks.MAGMA_BLOCK.defaultBlockState());
                column.fill(top - depth, top, Ground.WATER);
            }
        }

        // The chasm: no bottom to it at all.
        double w = chasmWidth(x) / 2.0 + 3.0 * this.rough.at(x / 9.0, z / 9.0);
        if (Math.abs(z - cz) < w) {
            column.clear(Column.MIN_Y, Column.MAX_Y);
        }
    }
}
