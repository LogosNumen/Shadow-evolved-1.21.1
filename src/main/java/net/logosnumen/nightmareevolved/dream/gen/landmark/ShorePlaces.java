package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.Trees;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The named places of the Forgotten Shore, besides the Crimson Spire. */
public final class ShorePlaces {
    private ShorePlaces() {
    }

    /**
     * The Ashen Barrow: a tall hill shaped like a colossal barrow, covered in the grey sand dead coral leaves behind,
     * standing at the far side of the Sea of Ash. At night the Dark Sea makes it an island. On its crown grows the Soul
     * Devouring Tree, black-barked with blood-red leaves, its roots running down the slopes.
     */
    public static final class AshenBarrow extends Built {
        private static final double TALL = 88.0;
        private static final double TRUNK = 50.0;
        private static final Noise SURFACE = new Noise(0xA5BA_7210L, 2);
        private final BlockState ash = DreamBlocks.ASH.get().defaultBlockState();
        private final BlockState bark = DreamBlocks.BLACK_BARK_LOG.get().defaultBlockState();
        private final BlockState leaves = DreamBlocks.BLOOD_LEAVES.get().defaultBlockState();

        public AshenBarrow() {
            super(Places.ASHEN_BARROW.x(), Places.ASHEN_BARROW.z(), 220, 0xA5BAL);
        }

        /** How high the barrow rises above the land at an offset from its middle; its western slope is the steeper. */
        private static double rise(double u, double v) {
            double rx = u < 0.0 ? 120.0 : 205.0;
            double d = (u / rx) * (u / rx) + (v / 135.0) * (v / 135.0);
            return d >= 1.0 ? 0.0 : TALL * Math.pow(1.0 - d, 0.75);
        }

        private int crown() {
            return 64 + (int)TALL;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double u = column.x + 0.5 - this.cx;
            double v = column.z + 0.5 - this.cz;
            double rise = rise(u, v);
            if (rise > 0.5) {
                int ground = column.topSolid();
                int top = (int)Math.round(64 + rise + 3.0 * SURFACE.at(column.x / 30.0, column.z / 30.0) * Math.min(1.0, rise / 20.0));
                if (top > ground) {
                    column.clear(ground + 1, top + 30);
                    column.fill(ground, top - 5, Blocks.STONE.defaultBlockState());
                    column.fill(top - 4, top, this.ash);
                }
            }

            this.tree(column);
        }

        /** The Soul Devouring Tree on the barrow's crown. */
        private void tree(Column column) {
            int base = this.crown() - 2;
            double dx = column.x + 0.5 - this.cx;
            double dz = column.z + 0.5 - this.cz;
            double d = Math.sqrt(dx * dx + dz * dz);
            // The trunk, flaring at the foot.
            if (d < 9.5) {
                for (int y = base - 4; y <= base + (int)TRUNK; y++) {
                    double r = 5.0 + 4.0 * Math.max(0.0, 1.0 - (y - base) / 10.0);
                    if (d <= r) {
                        column.set(y, Trees.upright(this.bark));
                    }
                }
            }

            // Branches and the crown.
            for (int b = 0; b < 7; b++) {
                double angle = b * (Math.PI * 2.0 / 7.0) + 0.4;
                double from = base + 28.0 + (b % 3) * 7.0;
                double reach = 20.0 + (b % 2) * 7.0;
                double ex = this.cx + Math.cos(angle) * reach;
                double ez = this.cz + Math.sin(angle) * reach;
                double ey = from + 12.0;
                tube(column, this.cx, from, this.cz, ex, ey, ez, 2.2, this.bark.setValue(RotatedPillarBlock.AXIS, Math.abs(Math.cos(angle)) > 0.7
                    ? Direction.Axis.X : Direction.Axis.Z));
                ball(column, ex, ey + 3.0, ez, 10.5, Trees.persistent(this.leaves), true);
            }

            ball(column, this.cx, base + TRUNK + 4.0, this.cz, 15.0, Trees.persistent(this.leaves), true);
            // Roots running down the slopes, just under and over the ash.
            if (d > 8.0 && d < 70.0) {
                double angle = Math.atan2(dz, dx);
                double nearest = Math.round(angle / (Math.PI / 5.0)) * (Math.PI / 5.0);
                double off = Math.abs(angle - nearest) * d;
                if (off < 1.8 - d / 70.0) {
                    int top = column.topSolid();
                    column.fill(top - 1, top + (d < 30.0 ? 1 : 0), this.bark.setValue(RotatedPillarBlock.AXIS,
                        Math.abs(Math.cos(nearest)) > 0.7 ? Direction.Axis.X : Direction.Axis.Z));
                }
            }
        }
    }

    /**
     * Bone Ridge: the skeleton of a colossal sea monster lying on a great mound of coral, its spine arching high enough
     * to stay above the Dark Sea at night. Its cracked skull's upper jaw makes a cave, and the hollow spine runs on from
     * it like a road through a tunnel, lit through the gaps between the vertebrae. The bones of lesser leviathans make
     * arches in the labyrinth around it.
     */
    public static final class BoneRidge extends Built {
        private static final int LENGTH = 170;
        private final BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
        private final BlockState coral = DreamBlocks.CRIMSON_CORAL.get().defaultBlockState();

        public BoneRidge() {
            super(Places.BONE_RIDGE.x(), Places.BONE_RIDGE.z(), 230, 0xB0E2L);
        }

        /** The spine's height along its length: an arch peaking well above the night's water. */
        private static double spineY(double u) {
            double t = u / LENGTH;
            return 98.0 + 62.0 * Math.max(0.0, 1.0 - t * t);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double u = column.x + 0.5 - this.cx;
            double v = column.z + 0.5 - this.cz;
            // The coral mound the beast died on.
            double mound = (u / 200.0) * (u / 200.0) + (v / 70.0) * (v / 70.0);
            if (mound < 1.0) {
                int ground = column.solidBelow(120);
                int rise = (int)(30.0 * Math.pow(1.0 - mound, 0.6));
                column.fill(ground, ground + rise, this.coral);
                if (this.hash(column, 1) < 0.25) {
                    column.set(ground + rise + 1, DreamBlocks.CRIMSON_CORAL_BLADES.get().defaultBlockState());
                }
            }

            if (Math.abs(u) <= LENGTH) {
                double y = spineY(u);
                // The vertebrae: rings with gaps between them, a road along their floor.
                int segment = (int)Math.floor((u + LENGTH) / 9.0);
                double inSegment = (u + LENGTH) - segment * 9.0;
                double outer = 9.0;
                double inner = 6.5;
                double flat = outer * outer - v * v;
                if (flat > 0.0 && inSegment < 7.5) {
                    double hiO = Math.sqrt(flat);
                    double hiI = inner * inner - v * v > 0.0 ? Math.sqrt(inner * inner - v * v) : -1.0;
                    if (hiI < 0.0) {
                        column.fill((int)(y - hiO), (int)(y + hiO), this.bone);
                    } else {
                        column.fill((int)(y - hiO), (int)(y - hiI), this.bone);
                        column.fill((int)(y + hiI), (int)(y + hiO), this.bone);
                    }
                } else if (flat > 0.0 && Math.abs(v) < 4.0) {
                    // The road along the hollow's floor, carried across the gaps.
                    column.set((int)(y - 6.0), this.bone);
                }

                // The ribs, falling from the spine to the coral on both sides.
                if (Math.floorMod(segment, 2) == 0 && inSegment < 2.5) {
                    double span = 34.0 * Math.max(0.2, 1.0 - Math.abs(u) / (LENGTH * 1.1));
                    double av = Math.abs(v);
                    if (av > 8.0 && av < span) {
                        double drop = (av - 8.0) / (span - 8.0);
                        int ry = (int)(y - 4.0 - drop * drop * (y - 78.0));
                        column.fill(ry - 1, ry + 1, this.bone);
                    }
                }
            }

            this.skull(column, u, v);
        }

        /** The skull at the eastern end, its upper jaw a cavern opening onto the spine's road. */
        private void skull(Column column, double u, double v) {
            double sx = u - (LENGTH + 30.0);
            double nx = sx / 40.0;
            double nz = v / 22.0;
            double flat = 1.0 - nx * nx - nz * nz;
            if (flat <= 0.0) {
                return;
            }

            double half = 20.0 * Math.sqrt(flat);
            double y = 104.0;
            column.fill((int)(y - half), (int)(y + half), this.bone);
            double innerFlat = 1.0 - (sx / 36.0) * (sx / 36.0) - (v / 18.0) * (v / 18.0);
            if (innerFlat > 0.0) {
                double ih = 16.0 * Math.sqrt(innerFlat);
                column.clear((int)(y - ih * 0.5), (int)(y + ih));
            }

            // The cracked crown, and the jaw's mouth gaping east.
            if (sx > 24.0 && Math.abs(v) < 12.0) {
                column.clear((int)(y - 6.0), (int)(y + 8.0));
            }

            if (Math.abs(nz) < 0.12 && nx < 0.4) {
                column.clear((int)(y + half - 3.0), (int)(y + half));
            }
        }
    }

    /**
     * The Dark City, perched on the eastern rim of the great basin: a ruined city of weathered stone inside tall walls
     * no sea monster can cross. On a hill in it stands the Bright Castle; among its streets, the Ruined Cathedral, the
     * Library Ruins with their great floor mosaic, and the way down into the Catacombs.
     */
    public static final class DarkCity extends Built {
        public static final int GROUND = 98;
        private static final int RADIUS = 236;
        private static final int CASTLE_X = 70;
        private static final int CASTLE_Z = 20;
        private final BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
        private final BlockState cracked = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        private final BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        private final BlockState white = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        private final BlockState whiteTrim = Blocks.QUARTZ_BRICKS.defaultBlockState();
        private final Town.Style town = Town.ruinedStone(0.55);

        public DarkCity() {
            super(Places.DARK_CITY.x(), Places.DARK_CITY.z(), 300, 0xDA2CL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double d = this.dist(column);
            if (d > 296.0) {
                return;
            }

            int ground = column.topSolid();
            if (d < RADIUS + 10) {
                // The city's ground: level, paved, raised over the basin's slope.
                column.clear(GROUND + 1, Math.max(GROUND + 1, ground + 40));
                column.fill(Math.min(ground, GROUND - 6), GROUND, Blocks.STONE.defaultBlockState());
                column.set(GROUND, Blocks.COBBLESTONE.defaultBlockState());
            } else {
                // A slope down from the walls' foot to the land.
                double t = (d - RADIUS - 10.0) / 50.0;
                int y = (int)Math.round(GROUND + (ground - GROUND) * t);
                if (y > ground) {
                    column.fill(ground, y, Blocks.STONE.defaultBlockState());
                    column.set(y, DreamBlocks.BLACK_EARTH.get().defaultBlockState());
                } else {
                    column.clear(y + 1, ground);
                }

                return;
            }

            int dx = this.dx(column);
            int dz = this.dz(column);
            if (d > RADIUS - 6 && d <= RADIUS) {
                this.walls(column, dx, dz);
                return;
            }

            if (this.castle(column, dx, dz) || this.cathedral(column, dx, dz) || this.library(column, dx, dz) || this.catacombs(column, dx, dz)) {
                return;
            }

            Town.paint(column, this.cx, this.cz, GROUND, this.salt, this.town);
        }

        /** The city wall, its round towers, and gates west to the crater and south. */
        private void walls(Column column, int dx, int dz) {
            double angle = Math.toDegrees(Math.atan2(dz, dx));
            boolean gate = Math.abs(angle - 180.0) < 2.5 || Math.abs(angle + 180.0) < 2.5 || Math.abs(angle - 90.0) < 2.5;
            column.fill(GROUND + 1, GROUND + 26, this.hash(column, 1) < 0.2 ? this.cracked : this.wall);
            if (gate) {
                column.clear(GROUND + 1, GROUND + 10);
            }

            if (Math.floorMod((int)Math.round(angle * 3.0), 2) == 0) {
                column.set(GROUND + 27, this.wall);
            }
        }

        /** The Bright Castle on its hill: a white keep and towers within a curtain wall. */
        private boolean castle(Column column, int dx, int dz) {
            double hx = dx - CASTLE_X;
            double hz = dz - CASTLE_Z;
            double hd = Math.sqrt(hx * hx + hz * hz);
            if (hd > 64.0) {
                return false;
            }

            int hill = GROUND + (int)Math.round(24.0 * Math.pow(Math.max(0.0, 1.0 - hd / 64.0), 0.5));
            column.fill(GROUND, hill, Blocks.STONE.defaultBlockState());
            column.set(hill, hd < 52.0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
            if (hd > 52.0) {
                return true;
            }

            int top = GROUND + 24;
            if (hd > 48.0) {
                column.fill(top + 1, top + 12, this.whiteTrim);
                if (((int)hx + (int)hz) % 2 == 0) {
                    column.set(top + 13, this.whiteTrim);
                }

                if (Math.abs(hx) < 3.0 && hz > 0.0) {
                    column.clear(top + 1, top + 6);
                }

                return true;
            }

            int ax = (int)Math.abs(hx);
            int az = (int)Math.abs(hz);
            if (ax <= 16 && az <= 16) {
                boolean shell = ax == 16 || az == 16;
                if (shell) {
                    column.fill(top + 1, top + 40, this.white);
                    if ((ax + az) % 4 == 1) {
                        for (int y = top + 8; y < top + 38; y += 8) {
                            column.clear(y, y + 2);
                        }
                    }
                } else {
                    for (int y = top + 10; y <= top + 40; y += 10) {
                        column.set(y, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
                    }
                }

                if (az == 16 && hz > 0 && ax <= 2) {
                    column.clear(top + 1, top + 5);
                }

                column.set(top + 41, (ax + az) % 2 == 0 ? this.whiteTrim : null);
            }

            for (int cxs = -1; cxs <= 1; cxs += 2) {
                for (int czs = -1; czs <= 1; czs += 2) {
                    double tx = hx - cxs * 18.0;
                    double tz = hz - czs * 18.0;
                    if (tx * tx + tz * tz <= 30.0) {
                        column.fill(top + 1, top + 52, this.white);
                        column.set(top + 53, Blocks.QUARTZ_SLAB.defaultBlockState());
                    }
                }
            }

            return true;
        }

        /** The Ruined Cathedral: a long nave open to the sky where its roof fell, a round window in its west front. */
        private boolean cathedral(Column column, int dx, int dz) {
            int x = dx + 90;
            int z = dz + 70;
            if (x < -26 || x > 26 || z < -11 || z > 11) {
                return false;
            }

            boolean wallX = Math.abs(x) == 26;
            boolean wallZ = Math.abs(z) == 11;
            column.set(GROUND, Blocks.POLISHED_ANDESITE.defaultBlockState());
            if (wallX || wallZ) {
                int tall = 22 - (int)(this.hash(column, 2) * (wallZ ? 10 : 2));
                column.fill(GROUND + 1, GROUND + tall, this.hash(column, 3) < 0.3 ? this.mossy : this.wall);
                if (wallZ && Math.floorMod(x, 6) == 0) {
                    column.clear(GROUND + 6, GROUND + 15);
                }

                if (x == -26) {
                    double rz = z;
                    for (int y = GROUND + 10; y <= GROUND + 20; y++) {
                        double ry = y - (GROUND + 15);
                        if (rz * rz + ry * ry < 20.0) {
                            column.clear(y, y);
                        }
                    }

                    if (Math.abs(z) <= 2) {
                        column.clear(GROUND + 1, GROUND + 7);
                    }
                }
            } else if (Math.floorMod(x, 8) == 0 && Math.abs(z) == 6) {
                column.fill(GROUND + 1, GROUND + 18, Blocks.STONE_BRICK_WALL.defaultBlockState());
            } else if (this.hash(column, 4) < 0.08) {
                column.set(GROUND + 1, this.cracked);
            }

            // The bell tower at the east end.
            if (x >= 20 && x <= 26 && Math.abs(z) <= 3 && (x == 20 || x == 26 || Math.abs(z) == 3)) {
                column.fill(GROUND + 1, GROUND + 36, this.wall);
            }

            return true;
        }

        /** The Library Ruins: broken walls and fallen rubble over a great mosaic on the main hall's floor. */
        private boolean library(Column column, int dx, int dz) {
            int x = dx + 95;
            int z = dz - 80;
            if (x < -16 || x > 16 || z < -13 || z > 13) {
                return false;
            }

            column.set(GROUND, mosaic(x, z));
            if (Math.abs(x) == 16 || Math.abs(z) == 13) {
                int tall = 4 + (int)(this.hash(column, 5) * 10);
                column.fill(GROUND + 1, GROUND + tall, this.hash(column, 6) < 0.4 ? this.cracked : this.wall);
            } else if (this.hash(column, 7) < 0.1) {
                column.fill(GROUND + 1, GROUND + 1 + (int)(this.hash(column, 8) * 2), Blocks.COBBLESTONE.defaultBlockState());
            }

            return true;
        }

        /**
         * The mosaic: seven figures around a radiant three-eyed one, their faces chipped away; around them the walled
         * city, the tower and the sun.
         */
        private static BlockState mosaic(int x, int z) {
            double d = Math.sqrt(x * x + z * z);
            if (d < 2.5) {
                return Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState();
            }

            if (d < 4.0) {
                return Blocks.WHITE_TERRACOTTA.defaultBlockState();
            }

            for (int i = 0; i < 7; i++) {
                double a = i * Math.PI * 2.0 / 7.0;
                double fx = Math.cos(a) * 9.0;
                double fz = Math.sin(a) * 7.5;
                double f = Math.sqrt((x - fx) * (x - fx) + (z - fz) * (z - fz));
                if (f < 1.0) {
                    // The faces, obliterated.
                    return Blocks.GRAY_CONCRETE.defaultBlockState();
                }

                if (f < 2.2) {
                    return switch (i) {
                        case 0 -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
                        case 1 -> Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
                        case 2 -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
                        case 3 -> Blocks.BROWN_TERRACOTTA.defaultBlockState();
                        case 4 -> Blocks.YELLOW_TERRACOTTA.defaultBlockState();
                        case 5 -> Blocks.RED_TERRACOTTA.defaultBlockState();
                        default -> Blocks.GRAY_TERRACOTTA.defaultBlockState();
                    };
                }
            }

            return (x + z) % 2 == 0 ? Blocks.CYAN_TERRACOTTA.defaultBlockState() : Blocks.BLUE_TERRACOTTA.defaultBlockState();
        }

        /** A small mausoleum over the stair down to the Catacombs, and their halls under the city. */
        private boolean catacombs(Column column, int dx, int dz) {
            int x = dx + 10;
            int z = dz + 120;
            // The tunnels: a grid of corridors under this quarter.
            if (Math.abs(x) <= 60 && Math.abs(z) <= 60) {
                boolean corridor = Math.floorMod(x, 12) <= 2 || Math.floorMod(z, 12) <= 2;
                if (corridor) {
                    column.clear(GROUND - 22, GROUND - 18);
                    column.set(GROUND - 23, Blocks.POLISHED_ANDESITE.defaultBlockState());
                    if (Math.floorMod(x, 12) == 0 || Math.floorMod(z, 12) == 0) {
                        if (this.hash(column, 9) < 0.15) {
                            column.set(GROUND - 21, Blocks.BONE_BLOCK.defaultBlockState());
                        } else if (this.hash(column, 10) < 0.05) {
                            column.set(GROUND - 20, Blocks.SKELETON_SKULL.defaultBlockState());
                        }
                    }
                }
            }

            if (Math.abs(x) > 5 || Math.abs(z) > 5) {
                return false;
            }

            boolean shell = Math.abs(x) == 5 || Math.abs(z) == 5;
            column.set(GROUND, Blocks.POLISHED_ANDESITE.defaultBlockState());
            if (shell) {
                column.fill(GROUND + 1, GROUND + 8, this.mossy);
                if (z == 5 && Math.abs(x) <= 1) {
                    column.clear(GROUND + 1, GROUND + 3);
                }
            } else {
                column.set(GROUND + 9, Blocks.STONE_BRICK_SLAB.defaultBlockState());
                // The stair winding down.
                if (Math.abs(x) <= 1 && z >= -3 && z <= 3) {
                    int step = (z + 3) * 3;
                    column.clear(GROUND - 22, GROUND);
                    column.set(GROUND - step - 1, Blocks.STONE_BRICKS.defaultBlockState());
                }
            }

            return true;
        }
    }

    /**
     * The Underground Mine under the Hollow Mountains, opening near the Dark City: a wide tunnel that forks deeper in,
     * its walls carved with the Shore's old story; a round hall with a dark chasm at its heart like a gate to the
     * Underworld; and at the bottom an ink-black river under drifting mist, with a stone pillar on its shore and a
     * pale wooden boat tied to it.
     */
    public static final class UndergroundMine extends Built {
        private static final int FLOOR = 86;
        private static final int HALL_Z = 240;
        private static final int RIVER_Y = 44;
        private final BlockState carving = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();

        public UndergroundMine() {
            super(Places.UNDERGROUND_MINE.x(), Places.UNDERGROUND_MINE.z(), 300, 0x3141L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            // The main tunnel, running south into the mountains.
            if (Math.abs(x) <= 4 && z >= -40 && z <= HALL_Z) {
                column.clear(FLOOR + 1, FLOOR + 8);
                column.set(FLOOR, Blocks.STONE_BRICKS.defaultBlockState());
            } else if (Math.abs(x) == 5 && z > 80 && z <= HALL_Z - 20) {
                // Deeper in, the walls are carved: patterns first, then the story of the Shore.
                for (int y = FLOOR + 1; y <= FLOOR + 6; y++) {
                    if (column.solid(y)) {
                        column.set(y, Math.floorMod(z / 3 + y, 4) == 0 ? this.carving : Blocks.POLISHED_ANDESITE.defaultBlockState());
                    }
                }
            }

            // Forks off to either side.
            for (int f = 0; f < 3; f++) {
                int at = 60 + f * 50;
                int side = f % 2 == 0 ? 1 : -1;
                double along = (x * side) / 1.0;
                double off = z - at - along * 0.6;
                if (along > 4.0 && along < 90.0 && Math.abs(off) <= 2.5) {
                    column.clear(FLOOR + 1, FLOOR + 6);
                    column.set(FLOOR, Blocks.STONE.defaultBlockState());
                }
            }

            // The round hall, with the chasm in its heart.
            double hx = x;
            double hz = z - HALL_Z;
            double hd = Math.sqrt(hx * hx + hz * hz);
            if (hd < 23.0) {
                column.clear(FLOOR + 1, FLOOR + 16 - (int)(hd / 3.0));
                column.set(FLOOR, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                if (hd < 7.0) {
                    column.clear(-30, FLOOR);
                } else if (hd < 8.0) {
                    column.set(FLOOR + 1, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
                }
            }

            // A ramp spiralling down from the hall to the river.
            double angle = Math.atan2(hz, hx);
            if (hd > 13.0 && hd < 19.0) {
                double turn = (angle + Math.PI) / (Math.PI * 2.0);
                for (int k = 0; k < 4; k++) {
                    int y = (int)(FLOOR - (turn + k) * 12.0);
                    if (y > RIVER_Y + 1 && y < FLOOR) {
                        column.clear(y + 1, y + 5);
                        column.set(y, Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
            }

            // The underground river, black as ink, running east to west below the hall.
            double rz = z - HALL_Z - 30.0;
            if (Math.abs(rz) < 9.0 && Math.abs(x) < 150) {
                column.clear(RIVER_Y - 3, RIVER_Y + 8);
                column.set(RIVER_Y - 4, Blocks.DEEPSLATE.defaultBlockState());
                if (Math.abs(rz) < 6.0) {
                    column.fill(RIVER_Y - 3, RIVER_Y, Blocks.WATER.defaultBlockState());
                } else {
                    column.fill(RIVER_Y - 3, RIVER_Y, Blocks.DEEPSLATE.defaultBlockState());
                }
            }

            if (Math.abs(rz - 7.0) < 1.0 && Math.abs(x) < 1) {
                column.fill(RIVER_Y + 1, RIVER_Y + 5, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        }

        @Override
        public void decorate(WorldGenLevel level, ChunkPos chunk, RealmTerrain terrain) {
            BlockPos pillar = new BlockPos(this.cx, RIVER_Y + 1, this.cz + HALL_Z + 30 + 4);
            if (chunk.equals(new ChunkPos(pillar))) {
                Boat boat = EntityType.BOAT.create(level.getLevel());
                if (boat != null) {
                    boat.setVariant(Boat.Type.BIRCH);
                    boat.moveTo(pillar.getX() + 0.5, RIVER_Y + 0.6, pillar.getZ() + 0.5, 90.0F, 0.0F);
                    level.addFreshEntity(boat);
                }
            }
        }
    }
}
