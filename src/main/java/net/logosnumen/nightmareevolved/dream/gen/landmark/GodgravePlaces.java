package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Godgrave;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The places inside the Breastbone Hollow, Godgrave's jungle. */
public final class GodgravePlaces {
    private static final int FLOOR = Godgrave.VAULT_FLOOR;

    private GodgravePlaces() {
    }

    /**
     * Vanishing Lake, at the centre of the Breastbone Hollow: a lake with a tall temple-castle rising from its middle,
     * its walls of pale wood like countless white trees grown together, its tiers of roofs tiled in faded crimson,
     * overgrown with vermilion moss and vines. Hidden inside is a grove under a crystal dome, where ordinary green things
     * grow untouched by corruption around a pond of clear water: the Gateway.
     */
    public static final class VanishingLake extends Built {
        private static final double LAKE = 72.0;
        private static final int[] TIERS = {19, 16, 13, 10, 7};
        private static final int TIER = 12;
        private final BlockState pale = Blocks.STRIPPED_BIRCH_LOG.defaultBlockState();
        private final BlockState planks = Blocks.BIRCH_PLANKS.defaultBlockState();
        private final BlockState tiles = Blocks.RED_TERRACOTTA.defaultBlockState();

        public VanishingLake() {
            super(Places.VANISHING_LAKE.x(), Places.VANISHING_LAKE.z(), 80, 0x7A15L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double d = this.dist(column);
            if (d < LAKE && d > TIERS[0] + 2) {
                column.clear(FLOOR - 9, FLOOR + 1);
                column.fill(FLOOR - 10, FLOOR - 10, Blocks.MUD.defaultBlockState());
                column.fill(FLOOR - 9, FLOOR - 1, Ground.WATER);
                // A narrow causeway of roots from the southern shore.
                if (Math.abs(x) <= 1 && z > 0) {
                    column.set(FLOOR - 1, this.pale.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                }

                return;
            }

            if (d > LAKE) {
                return;
            }

            int ax = Math.abs(x);
            int az = Math.abs(z);
            int y = FLOOR;
            for (int t = 0; t < TIERS.length; t++) {
                int half = TIERS[t];
                int y0 = y + 1;
                int y1 = y + TIER - 3;
                if (ax <= half && az <= half) {
                    boolean shell = ax == half || az == half;
                    if (shell) {
                        column.fill(y0, y1, (ax + az) % 3 == 0 ? this.pale : this.planks);
                        if (t == 0 && z == half && ax <= 2) {
                            column.clear(y0, y0 + 4);
                        } else if ((ax + az) % 5 == 2) {
                            column.clear(y0 + 3, y0 + 5);
                        }

                        if (this.hash(column, t) < 0.3) {
                            column.set(y1, DreamBlocks.RED_MOSS.get().defaultBlockState());
                        }
                    } else {
                        column.clear(y0, y1 + 1);
                        column.set(y, this.planks);
                    }
                }

                // The deep eaves, tiled in faded crimson.
                int eave = half + 3;
                if (ax <= eave && az <= eave && (ax >= half || az >= half || t == TIERS.length - 1)) {
                    int lift = Math.max(0, Math.max(ax, az) - half);
                    column.set(y1 + 2 - lift / 2, this.hash(column, 9) < 0.25 ? Blocks.RED_NETHER_BRICKS.defaultBlockState() : this.tiles);
                    if (lift == 3 && this.hash(column, 10) < 0.3) {
                        column.fill(y1 - 3, y1, Blocks.WEEPING_VINES_PLANT.defaultBlockState());
                    }
                }

                y += TIER;
            }

            if (ax <= 2 && az <= 2) {
                column.fill(y + 1, y + 6, this.tiles);
            }

            this.grove(column, x, z);
        }

        /** The hidden grove under its crystal dome, in the temple's ground hall, and the pond that is the Gateway. */
        private void grove(Column column, int x, int z) {
            double d = Math.sqrt(x * x + z * z);
            if (d > 8.0) {
                return;
            }

            double dome = Math.sqrt(Math.max(0.0, 64.0 - d * d));
            int top = FLOOR + (int)dome;
            column.set(top + 1, Blocks.GLASS.defaultBlockState());
            column.clear(FLOOR + 1, top);
            column.set(FLOOR, d < 2.5 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
            if (x == 0 && z == 0) {
                column.set(FLOOR - 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.POOL));
                column.set(FLOOR, Ground.WATER);
            } else if (d < 1.6) {
                column.set(FLOOR, Ground.WATER);
                column.set(FLOOR - 1, Blocks.MOSS_BLOCK.defaultBlockState());
            } else if (d > 6.5) {
                column.set(FLOOR + 1, Blocks.WEEPING_VINES.defaultBlockState());
            } else if (this.hash(column, 11) < 0.35) {
                column.set(FLOOR + 1, this.hash(column, 12) < 0.4 ? Blocks.CORNFLOWER.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState());
            } else if (this.hash(column, 13) < 0.05) {
                column.fill(FLOOR + 1, FLOOR + 3, Blocks.OAK_LOG.defaultBlockState());
                column.set(FLOOR + 4, Blocks.OAK_LEAVES.defaultBlockState());
            }
        }
    }

    /**
     * An immense ziggurat towering over the ruins of the old civilisation in the hollow, its terraces furred with red
     * moss. A Seed of the Second Nightmare rests on its summit.
     */
    public static final class Ziggurat extends Built {
        private static final int HALF = 32;
        private static final int STEP = 6;
        private static final int STEPS = 6;
        /** How far the great stair runs out past the ziggurat's foot, so that it climbs one block at a time. */
        private static final int STAIR = 6;

        public Ziggurat() {
            super(Places.MICTLAN_ZIGGURAT.x(), Places.MICTLAN_ZIGGURAT.z(), HALF + STAIR, 0x2166L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            int m = Math.max(Math.abs(x), Math.abs(z));
            boolean stairway = Math.abs(x) <= 2 && z > 0 && z <= HALF + STAIR;
            if (m > HALF && !stairway) {
                return;
            }

            if (m <= HALF) {
                int step = Math.min(STEPS - 1, (HALF - m) / 5);
                int top = FLOOR + (step + 1) * STEP;
                column.clear(FLOOR + 1, top + 12);
                column.fill(FLOOR + 1, top, Blocks.STONE_BRICKS.defaultBlockState());
                column.set(top, this.hash(column, 1) < 0.55 ? DreamBlocks.RED_MOSS.get().defaultBlockState() : Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            }

            // The great stair up the southern face, one block to a step from the jungle floor to the top.
            if (stairway) {
                int stair = FLOOR + Math.min(STEPS * STEP, HALF + STAIR - z);
                column.fill(FLOOR + 1, stair, Blocks.STONE_BRICKS.defaultBlockState());
                column.clear(stair + 1, FLOOR + STEPS * STEP + 6);
            }

            if (x == 0 && z == 0) {
                column.set(FLOOR + STEPS * STEP + 1, DreamBlocks.SEED.get().seed(SeedBlock.Form.STONE, 2));
            }
        }
    }

    /** The ruined cities of the hollow's old civilisation: stone edifices under red moss, scattered through the jungle. */
    public static final class Ruins extends Built {
        private static final Noise MASK = new Noise(0x3C71L, 2);
        private final Town.Style style = Town.Style.of(Blocks.STONE_BRICKS.defaultBlockState(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
            Blocks.STONE.defaultBlockState(), DreamBlocks.RED_MOSS.get().defaultBlockState(), DreamBlocks.RED_MOSS.get().defaultBlockState(), 1, 3, 0.7);

        public Ruins() {
            super(Godgrave.AXIS, (Godgrave.VAULT_NORTH + Godgrave.VAULT_SOUTH) / 2, (Godgrave.VAULT_SOUTH - Godgrave.VAULT_NORTH) / 2, 0x3C72L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            if (Math.abs(column.x - Godgrave.AXIS) > Godgrave.VAULT_HALF - 14 || column.z < Godgrave.VAULT_NORTH + 14 || column.z > Godgrave.VAULT_SOUTH - 14) {
                return;
            }

            if (Places.VANISHING_LAKE.distance(column.x, column.z) < 90.0 || Places.MICTLAN_ZIGGURAT.distance(column.x, column.z) < 50.0) {
                return;
            }

            if (MASK.at(column.x / 70.0, column.z / 70.0) > 0.15 && column.get(FLOOR) != null) {
                Town.paint(column, Godgrave.AXIS, Godgrave.VAULT_NORTH, FLOOR, this.salt, this.style);
            }
        }
    }
}
