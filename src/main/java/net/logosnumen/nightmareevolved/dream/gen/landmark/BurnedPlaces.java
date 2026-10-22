package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The places of the Burned Forest. */
public final class BurnedPlaces {
    private BurnedPlaces() {
    }

    /**
     * A Millipede Queen's hive: a fortified dome raised out of the deadfall, its walls chewed charred wood packed with
     * stone, ringed by a palisade of sharpened trunks. Four tunnels lead in to the chamber where she lies.
     */
    public static final class Hive extends Built {
        /** The chamber's floor. */
        public static final int FLOOR = 66;
        private static final double DOME = 40.0;
        private static final double DOME_HEIGHT = 30.0;
        private static final double CHAMBER = 33.0;
        private static final double CHAMBER_HEIGHT = 22.0;
        private static final double PALISADE = 45.0;
        private static final int TUNNEL = 2;
        private static final int TUNNEL_HEIGHT = 7;
        private final BlockState wood = DreamBlocks.CHARRED_LOG.get().defaultBlockState();
        private final BlockState stake = DreamBlocks.CHARRED_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        private final BlockState stone = Blocks.BLACKSTONE.defaultBlockState();
        private final BlockState ash = DreamBlocks.ASH.get().defaultBlockState();

        public Hive() {
            super(Places.MILLIPEDE_HIVE.x(), Places.MILLIPEDE_HIVE.z(), 48, 0x41BEL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double d = this.dist(column);
            if (d > PALISADE + 2.0) {
                return;
            }

            // The yard: the deadfall cleared away and the ground beaten flat.
            column.clear(FLOOR + 1, Column.MAX_Y);
            column.fill(FLOOR - 4, FLOOR, this.ash);
            int x = this.dx(column);
            int z = this.dz(column);
            boolean way = Math.abs(x) <= TUNNEL || Math.abs(z) <= TUNNEL;
            if (d >= PALISADE - 1.0 && d < PALISADE + 1.0 && !way) {
                // The palisade: trunks driven into the ground, sharpened, of uneven heights.
                if (this.hash(column, 1) < 0.85) {
                    column.fill(FLOOR + 1, FLOOR + 5 + (int)(this.hash(column, 2) * 6), this.stake);
                }

                return;
            }

            if (d > DOME) {
                if (this.hash(column, 3) < 0.04) {
                    column.set(FLOOR + 1, Blocks.BONE_BLOCK.defaultBlockState());
                }

                return;
            }

            double t = d / DOME;
            int roof = FLOOR + (int)Math.round(DOME_HEIGHT * Math.sqrt(Math.max(0.0, 1.0 - t * t)));
            for (int y = FLOOR + 1; y <= roof; y++) {
                column.set(y, this.hash(column.x, y, column.z) < 0.55 ? this.wood : this.stone);
            }

            if (d < CHAMBER) {
                double u = d / CHAMBER;
                int ceiling = FLOOR + (int)Math.round(CHAMBER_HEIGHT * Math.sqrt(Math.max(0.0, 1.0 - u * u)));
                column.clear(FLOOR + 1, ceiling);
                double strewn = this.hash(column, 4);
                if (strewn < 0.012) {
                    column.set(FLOOR + 1, Blocks.BONE_BLOCK.defaultBlockState());
                } else if (strewn < 0.016) {
                    column.set(FLOOR + 1, Blocks.SKELETON_SKULL.defaultBlockState());
                }
            }

            if (way) {
                // The tunnels in, a little crooked.
                column.clear(FLOOR + 1, FLOOR + TUNNEL_HEIGHT - (this.hash(column, 5) < 0.3 ? 1 : 0));
            }
        }
    }
}
