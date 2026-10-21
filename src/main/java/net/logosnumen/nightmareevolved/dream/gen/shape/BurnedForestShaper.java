package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Burned Forest, a Death Zone beyond the Forgotten Shore: a sea of black pillars as wide as towers, the broken
 * trunks of a titanic forest that burned for thousands of years and still stands, over a deadfall of fallen trunks so
 * thick that the ground is hidden beneath it.
 */
public final class BurnedForestShaper implements Shaper {
    private static final int TRUNK_CELL = 40;
    private static final int FALLEN_CELL = 26;
    private final long seed;
    private final Noise land;
    private final Noise jag;
    private final BlockState log;
    private final BlockState logX;
    private final BlockState logZ;

    public BurnedForestShaper(long seed) {
        this.seed = seed;
        this.land = new Noise(Noise.mix(seed, "burned.land"), 2);
        this.jag = new Noise(Noise.mix(seed, "burned.jag"), 1);
        this.log = DreamBlocks.CHARRED_LOG.get().defaultBlockState();
        this.logX = this.log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        this.logZ = this.log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
    }

    @Override
    public double ground(int x, int z, double edge) {
        return 66.0 + 2.5 * this.land.at(x / 120.0, z / 120.0);
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int top = Mth.floor(height);
        Ground.lay(column, top, Blocks.COARSE_DIRT.defaultBlockState(), DreamBlocks.ASH.get().defaultBlockState(), 4);
        double depth = Noise.smooth(blend.edge / 160.0);
        this.standing(column, top, depth);
        this.fallen(column, top, depth);
    }

    /** The standing trunks, broken off at jagged heights. */
    private void standing(Column column, int top, double depth) {
        int ci = Math.floorDiv(column.x, TRUNK_CELL);
        int cj = Math.floorDiv(column.z, TRUNK_CELL);
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int i = ci + di;
                int j = cj + dj;
                if (Noise.hash(this.seed, i, j, 161) > 0.5) {
                    continue;
                }

                double tx = i * TRUNK_CELL + TRUNK_CELL * Noise.hash(this.seed, i, j, 162);
                double tz = j * TRUNK_CELL + TRUNK_CELL * Noise.hash(this.seed, i, j, 163);
                double r = 3.0 + 3.5 * Noise.hash(this.seed, i, j, 164);
                double dx = column.x - tx;
                double dz = column.z - tz;
                if (dx * dx + dz * dz > r * r) {
                    continue;
                }

                int tall = (int)((50.0 + 130.0 * Noise.hash(this.seed, i, j, 165)) * (0.3 + 0.7 * depth));
                tall += (int)(6.0 * this.jag.at(column.x / 2.0, column.z / 2.0));
                column.fill(top - 2, top + tall, this.log);
            }
        }
    }

    /** The deadfall: trunks lying at every angle, piled into a tangle. */
    private void fallen(Column column, int top, double depth) {
        int ci = Math.floorDiv(column.x, FALLEN_CELL);
        int cj = Math.floorDiv(column.z, FALLEN_CELL);
        for (int di = -2; di <= 2; di++) {
            for (int dj = -2; dj <= 2; dj++) {
                int i = ci + di;
                int j = cj + dj;
                for (int n = 0; n < 2; n++) {
                    int salt = 170 + n * 10;
                    if (Noise.hash(this.seed, i, j, salt) > 0.75 * depth + 0.1) {
                        continue;
                    }

                    double ax = i * FALLEN_CELL + FALLEN_CELL * Noise.hash(this.seed, i, j, salt + 1);
                    double az = j * FALLEN_CELL + FALLEN_CELL * Noise.hash(this.seed, i, j, salt + 2);
                    double angle = Noise.hash(this.seed, i, j, salt + 3) * Math.PI * 2.0;
                    double length = 24.0 + 40.0 * Noise.hash(this.seed, i, j, salt + 4);
                    double r = 1.6 + 2.6 * Noise.hash(this.seed, i, j, salt + 5);
                    double lift = 4.0 + 46.0 * Noise.hash(this.seed, i, j, salt + 6) * depth;
                    double slope = (Noise.hash(this.seed, i, j, salt + 7) - 0.5) * 1.2;
                    double ux = Math.cos(angle);
                    double uz = Math.sin(angle);
                    double ox = column.x - ax;
                    double oz = column.z - az;
                    double along = ox * ux + oz * uz;
                    double across = Math.abs(-ox * uz + oz * ux);
                    if (along < 0.0 || along > length || across > r) {
                        continue;
                    }

                    double y = top + lift + slope * along;
                    if (y < top) {
                        continue;
                    }

                    double half = Math.sqrt(r * r - across * across);
                    BlockState state = Math.abs(ux) > Math.abs(uz) ? this.logX : this.logZ;
                    column.fill((int)Math.round(y - half), (int)Math.round(y + half), state);
                }
            }
        }
    }
}
