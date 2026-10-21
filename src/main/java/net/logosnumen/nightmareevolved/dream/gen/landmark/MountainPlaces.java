package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The named places of the Hollow Mountains and the Underworld beneath them. */
public final class MountainPlaces {
    private MountainPlaces() {
    }

    /**
     * The door of the Underworld: a dark fissure in a mountain face hidden by the mist, framed by tall pillars of black
     * rock under a triangular pediment carved with eroded dark wings, so that it looks like the door of a temple. Behind
     * it a passage falls steeply into the mountain, down to the Underworld's halls.
     */
    public static final class UnderworldGate extends Built {
        private static final int FLOOR = 132;
        private static final int BOTTOM = -36;
        private final BlockState black = Blocks.BLACKSTONE.defaultBlockState();
        private final BlockState carved = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();

        public UnderworldGate() {
            super(Places.UNDERWORLD_GATE.x(), Places.UNDERWORLD_GATE.z(), 200, 0x0DD0L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            // A ravine running down through the mountain to the door, where the mist pools thickest.
            if (Math.abs(x) <= 10 + (-z) / 20 && z >= -195 && z < 0) {
                int floor = FLOOR + (int)(-z * 0.6);
                column.clear(floor + 1, Column.MAX_Y);
                column.fill(floor - 3, floor, Blocks.STONE.defaultBlockState());
            }

            if (z >= 0 && z <= 3 && Math.abs(x) <= 12) {
                // The facade: two pillars, the fissure between them, the pediment over all.
                column.fill(FLOOR + 1, FLOOR + 30, this.black);
                if (Math.abs(x) <= 4) {
                    column.clear(FLOOR + 1, FLOOR + 22);
                }

                int pediment = FLOOR + 31 + (12 - Math.abs(x)) * 10 / 12;
                column.fill(FLOOR + 31, pediment, Math.floorMod(x + pediment, 3) == 0 ? this.carved : this.black);
                if (Math.abs(x) >= 9 && Math.abs(x) <= 11) {
                    column.fill(FLOOR + 1, FLOOR + 31, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                }
            }

            // The passage behind, falling steeply south into the mountain to the Underworld.
            if (Math.abs(x) <= 3 && z > 3) {
                int y = FLOOR - (z - 3) / 2;
                if (y >= BOTTOM) {
                    column.clear(y + 1, y + 7);
                    column.set(y, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                }
            }
        }
    }

    /**
     * Nether's City in the Underworld's middle layer: a magnificent city of dark stone built under the mountains at the
     * edge of an abyssal chasm, with the throne hall of its prince at its heart. Beyond the chasm's edge there is
     * nothing, all the way down.
     */
    public static final class NethersCity extends Built {
        private static final int GROUND = -36;
        private static final int ROOF = 22;
        private static final int RADIUS = 170;
        private static final Noise ROOF_NOISE = new Noise(0x0EEDL, 2);
        private final Town.Style style = Town.Style.of(Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(),
            Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2, 5, 0.25);

        public NethersCity() {
            super(Places.NETHERS_CITY.x(), Places.NETHERS_CITY.z(), RADIUS + 60, 0x0EEEL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double d = this.dist(column);
            if (d > RADIUS + 50) {
                return;
            }

            // The great hall of the Underworld the city stands in.
            int roof = ROOF + (int)(10.0 * ROOF_NOISE.at(column.x / 40.0, column.z / 40.0)) - (int)Math.max(0.0, (d - RADIUS) * 0.6);
            column.clear(GROUND + 1, Math.max(GROUND + 1, roof));
            column.fill(GROUND - 4, GROUND, Blocks.DEEPSLATE.defaultBlockState());
            // The chasm along the east: no floor, and no bottom.
            if (x > RADIUS - 40 + 12.0 * ROOF_NOISE.at(column.z / 30.0, 0.0)) {
                column.clear(Column.MIN_Y, GROUND);
                return;
            }

            if (d > RADIUS) {
                return;
            }

            // The throne hall.
            if (Math.abs(x) <= 22 && Math.abs(z) <= 30) {
                boolean shell = Math.abs(x) == 22 || Math.abs(z) == 30;
                column.set(GROUND, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                if (shell) {
                    column.fill(GROUND + 1, GROUND + 34, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                    if (z == 30 && Math.abs(x) <= 3) {
                        column.clear(GROUND + 1, GROUND + 12);
                    }
                } else {
                    column.set(GROUND + 35, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                    if (Math.floorMod(z, 10) == 0 && Math.abs(x) == 12) {
                        column.fill(GROUND + 1, GROUND + 34, Blocks.BLACKSTONE.defaultBlockState());
                    }
                }

                // The throne, on a dais at the north end.
                if (Math.abs(x) <= 3 && z <= -24) {
                    column.fill(GROUND + 1, GROUND + 3, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
                    if (Math.abs(x) <= 1 && z == -26) {
                        column.fill(GROUND + 4, GROUND + 9, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                    }
                }

                return;
            }

            Town.paint(column, this.cx, this.cz, GROUND, this.salt, this.style);
        }
    }

    /**
     * The Black Mountain: one jagged peak towering over the snowy range around it, and on its flat summit, under the
     * snow, the Nameless Temple of black marble: colossal columns, cracked walls, fallen roof, broken gates, thousands of
     * bones around it, and a part of it swallowed by darkness.
     */
    public static final class BlackMountain extends Built {
        private static final int SUMMIT = 336;
        private static final double RADIUS = 520.0;
        private static final double PLATEAU = 52.0;
        private static final Noise JAG = new Noise(0xB1ACL, 3);
        private final BlockState marble = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        private final BlockState pillar = Blocks.BLACKSTONE.defaultBlockState();

        public BlackMountain() {
            super(Places.BLACK_MOUNTAIN.x(), Places.BLACK_MOUNTAIN.z(), (int)RADIUS, 0xB1ADL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double d = this.dist(column);
            if (d > RADIUS) {
                return;
            }

            double t = Math.max(0.0, (d - PLATEAU) / (RADIUS - PLATEAU));
            double jag = 26.0 * JAG.at(column.x / 50.0, column.z / 50.0) * t;
            int height = (int)Math.round(d < PLATEAU ? SUMMIT : SUMMIT - Math.pow(t, 0.7) * (SUMMIT - 130) + jag);
            int top = column.topSolid();
            if (height > top) {
                column.fill(top, height, Blocks.STONE.defaultBlockState());
            }

            if (height > 270) {
                column.fill(height - 2, height, Blocks.SNOW_BLOCK.defaultBlockState());
            }

            if (d < PLATEAU) {
                column.clear(SUMMIT + 1, Column.MAX_Y);
                this.temple(column);
            }
        }

        private void temple(Column column) {
            int x = this.dx(column);
            int z = this.dz(column);
            int ax = Math.abs(x);
            if (this.hash(column, 1) < 0.03) {
                column.set(SUMMIT + 1, this.hash(column, 2) < 0.5 ? Blocks.BONE_BLOCK.defaultBlockState() : Blocks.SKELETON_SKULL.defaultBlockState());
            }

            if (ax > 18 || z < -30 || z > 30) {
                return;
            }

            column.set(SUMMIT, this.marble);
            boolean shell = ax == 18 || Math.abs(z) == 30;
            boolean gate = z == 30 && ax <= 4;
            boolean dark = x < -6 && z < -10;
            if (dark) {
                // The part swallowed by darkness.
                column.fill(SUMMIT + 1, SUMMIT + 30, Blocks.BLACK_CONCRETE.defaultBlockState());
                return;
            }

            if (shell && !gate) {
                int tall = 26 - (int)(this.hash(column, 3) < 0.25 ? this.hash(column, 4) * 14 : 0);
                column.fill(SUMMIT + 1, SUMMIT + tall, this.marble);
            } else if (gate) {
                column.fill(SUMMIT + 1, SUMMIT + 3 + (int)(this.hash(column, 5) * 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                column.clear(SUMMIT + 1, SUMMIT + 2);
            }

            // Colossal columns down the length of the hall.
            if ((ax == 10) && Math.floorMod(z, 8) == 0) {
                int tall = this.hash(column, 6) < 0.2 ? 8 : 26;
                for (int y = SUMMIT + 1; y <= SUMMIT + tall; y++) {
                    column.set(y, this.pillar);
                }
            }

            // The roof, fallen in places.
            if (!shell && Mth.abs(x) < 18 && this.hash(column, 7) > 0.35) {
                column.set(SUMMIT + 27, this.marble);
            }
        }
    }
}
