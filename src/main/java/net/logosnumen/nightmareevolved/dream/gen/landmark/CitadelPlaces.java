package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.Trees;
import net.logosnumen.nightmareevolved.dream.gen.shape.BastionShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.RavenheartShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.WeepingShaper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The human citadels of the south: Bastion, Ravenheart, the Weeping Goddess, Rivergate, Red Hill, and those of the Stormsea. */
public final class CitadelPlaces {
    private CitadelPlaces() {
    }

    /** Falling water, as it pours down a wall. */
    static BlockState falling() {
        return Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 8);
    }

    /**
     * Bastion: a grey stone castle as large as a city, rising out of the tranquil Lake of Bones, its walls scarred by a
     * thousand sieges. Towers ring its outer wall; a town fills the ward inside; an inner wall guards the keep, where the
     * Gateway is. A long bridge crosses the lake from the south, and lanterns burn along the battlements.
     */
    public static final class Bastion extends Built {
        public static final int GROUND = 70;
        private static final int ISLAND = 150;
        private static final int OUTER = 138;
        private static final int INNER = 66;
        private static final int KEEP = 20;
        private final BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        private final BlockState scarred = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        private final Town.Style town = Town.Style.of(Blocks.STONE_BRICKS.defaultBlockState(), Blocks.POLISHED_ANDESITE.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.STONE.defaultBlockState(), 2, 4, 0.04);

        public Bastion() {
            super(Places.BASTION.x(), Places.BASTION.z(), (int)BastionShaper.LAKE + 20, 0xBA57L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double d = this.dist(column);
            if (d > ISLAND + 8) {
                this.bridge(column, x, z);
                return;
            }

            // The rock the castle stands on, rising out of the lake.
            int ground = d < ISLAND ? GROUND : GROUND - (int)((d - ISLAND) * 1.2);
            column.clear(ground + 1, Ground.SEA_LEVEL);
            column.fill(20, ground, Blocks.STONE.defaultBlockState());
            if (d >= ISLAND) {
                return;
            }

            column.set(GROUND, Blocks.COBBLESTONE.defaultBlockState());
            if (this.ring(column, d, OUTER, 7, 50, 10, 30.0)) {
                return;
            }

            if (this.ring(column, d, INNER, 5, 40, 8, 45.0)) {
                return;
            }

            if (Math.abs(x) <= KEEP && Math.abs(z) <= KEEP) {
                this.keep(column, x, z);
            } else if (d > INNER + 6 && d < OUTER - 10) {
                Town.paint(column, this.cx, this.cz, GROUND, this.salt, this.town);
            } else if (this.hash(column, 1) < 0.3) {
                column.set(GROUND, Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }

        /** A circular wall with round towers: true if the column was part of it. */
        private boolean ring(Column column, double d, int radius, int thick, int tall, int towerRadius, double towerStep) {
            int x = this.dx(column);
            int z = this.dz(column);
            double angle = Math.toDegrees(Math.atan2(z, x));
            double nearest = Math.round(angle / towerStep) * towerStep;
            double tx = Math.cos(Math.toRadians(nearest)) * radius;
            double tz = Math.sin(Math.toRadians(nearest)) * radius;
            double td = Math.sqrt((x - tx) * (x - tx) + (z - tz) * (z - tz));
            if (td <= towerRadius) {
                column.fill(GROUND + 1, GROUND + tall + 20, td > towerRadius - 2 ? this.stone : Blocks.STONE.defaultBlockState());
                if (td < towerRadius - 2) {
                    column.clear(GROUND + 1, GROUND + tall + 18);
                    for (int y = GROUND + 10; y < GROUND + tall + 18; y += 10) {
                        column.set(y, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    }
                }

                column.set(GROUND + tall + 21, (x + z) % 2 == 0 ? this.stone : null);
                return true;
            }

            if (Math.abs(d - radius) <= thick / 2.0) {
                column.fill(GROUND + 1, GROUND + tall, this.hash(column, 2) < 0.18 ? this.scarred : this.stone);
                // Scars of sieges: holes knocked in the walls.
                if (this.hash(column, 3) < 0.03) {
                    column.clear(GROUND + tall - 8, GROUND + tall - 3);
                }

                boolean gate = Math.abs(x) <= 4 && z > 0;
                if (gate) {
                    column.clear(GROUND + 1, GROUND + 14);
                }

                if (Math.abs(d - radius) > thick / 2.0 - 1.0 && Math.floorMod(x + z, 2) == 0) {
                    column.set(GROUND + tall + 1, this.stone);
                }

                if (Math.floorMod(x * 7 + z * 3, 23) == 0) {
                    column.set(GROUND + tall + 1, Blocks.LANTERN.defaultBlockState());
                }

                return true;
            }

            return false;
        }

        /** The keep: a great tower of halls, corner turrets above, and the Gateway in its heart. */
        private void keep(Column column, int x, int z) {
            int ax = Math.abs(x);
            int az = Math.abs(z);
            boolean shell = ax == KEEP || az == KEEP;
            column.set(GROUND, Blocks.POLISHED_ANDESITE.defaultBlockState());
            if (shell) {
                column.fill(GROUND + 1, GROUND + 108, this.stone);
                if (z == KEEP && ax <= 2) {
                    column.clear(GROUND + 1, GROUND + 8);
                } else if ((ax + az) % 5 == 0) {
                    for (int y = GROUND + 16; y < GROUND + 100; y += 14) {
                        column.clear(y, y + 3);
                    }
                }
            } else {
                for (int y = GROUND + 18; y <= GROUND + 108; y += 18) {
                    column.set(y, Blocks.SPRUCE_PLANKS.defaultBlockState());
                }
            }

            if (ax >= KEEP - 4 && az >= KEEP - 4) {
                column.fill(GROUND + 1, GROUND + 128, this.stone);
                column.set(GROUND + 129, Blocks.LANTERN.defaultBlockState());
            }

            if (x == 0 && z == 0) {
                column.set(GROUND + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
            }
        }

        /** The bridge from the southern shore to the castle gate, on arches over the lake. */
        private void bridge(Column column, int x, int z) {
            if (Math.abs(x) > 4 || z < ISLAND || z > BastionShaper.LAKE + 12) {
                return;
            }

            column.set(GROUND, this.stone);
            column.clear(GROUND + 1, GROUND + 6);
            if (Math.abs(x) == 4) {
                column.set(GROUND + 1, Blocks.STONE_BRICK_WALL.defaultBlockState());
            }

            int arch = Math.floorMod(z, 24);
            if (arch < 4) {
                column.fill(30, GROUND - 1, this.stone);
            } else {
                double t = (arch - 4) / 20.0;
                int under = GROUND - 1 - (int)(8.0 * Math.sin(Math.PI * t));
                column.fill(under, GROUND - 1, this.stone);
            }
        }
    }

    /**
     * Ravenheart, the Jade Palace: a sprawling palace on the snowy slope at the edge of the bottomless chasm, black as
     * obsidian glass outside and white jade within, with halls built for beings far larger than men. Near its main gate,
     * a hall where clear water cascades down white jade walls hides the Gateway behind falling water. Under it run caves
     * of ice. From its gate the titanic bridge, black-pillared and engraved, crosses the chasm toward the fuming volcano,
     * a town of the Awakened built along it, and the mundane city of black stone climbs the volcano's slope beyond.
     */
    public static final class Ravenheart extends Built {
        public static final int FLOOR = 150;
        private static final int NORTH = -150;
        private static final int SOUTH = 200;
        private static final int HALF = 105;
        private final BlockState black = DreamBlocks.BLACK_JADE.get().defaultBlockState();
        private final BlockState white = DreamBlocks.WHITE_JADE.get().defaultBlockState();
        private final Town.Style bridgeTown = Town.Style.of(Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), DreamBlocks.BLACK_JADE.get().defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 1, 3, 0.03);
        private final Town.Style city = Town.Style.of(Blocks.BLACKSTONE.defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 1, 3, 0.05);

        public Ravenheart() {
            super(Places.RAVENHEART.x(), Places.RAVENHEART.z(), 520, 0x2AE7L);
        }

        /** The palace's southern face, at the chasm's edge. */
        private int edge() {
            return (int)RavenheartShaper.chasmZ(this.cx) - (int)(RavenheartShaper.chasmWidth(this.cx) / 2.0) - 4 - this.cz;
        }

        public static int gatewayX() {
            return Places.RAVENHEART.x();
        }

        public static int gatewayZ() {
            return (int)RavenheartShaper.chasmZ(Places.RAVENHEART.x()) - (int)(RavenheartShaper.chasmWidth(Places.RAVENHEART.x()) / 2.0) - 40;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            int edge = this.edge();
            if (Math.abs(x) <= HALF && z >= NORTH && z <= edge) {
                this.palace(column, x, z, edge);
            } else if (Math.abs(x) <= 26 && z > edge && z < edge + 120) {
                this.bridge(column, x, z, edge);
            } else if (Math.abs(x) <= 200 && z >= edge + 120 && z < edge + 400) {
                this.volcanoCity(column, x, z, edge);
            }
        }

        private void palace(Column column, int x, int z, int edge) {
            int ax = Math.abs(x);
            // Level ground for the palace, cut into the slope.
            column.clear(FLOOR + 1, Column.MAX_Y);
            column.fill(FLOOR - 30, FLOOR, Blocks.STONE.defaultBlockState());
            column.set(FLOOR, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            // Courts and halls on a grid; the central hall is built to a giant's scale.
            boolean giant = ax <= 24 && z > NORTH + 40 && z < edge - 60;
            int cellX = Math.floorMod(x + HALF, 35);
            int cellZ = Math.floorMod(z - NORTH, 35);
            boolean court = !giant && cellX > 8 && cellX < 27 && cellZ > 8 && cellZ < 27 && (Math.floorDiv(x + HALF, 35) + Math.floorDiv(z - NORTH, 35)) % 2 == 0;
            if (court) {
                // A stone garden with a pool of icy water, or a steaming hot spring.
                if (cellX > 13 && cellX < 22 && cellZ > 13 && cellZ < 22) {
                    boolean hot = Math.floorDiv(x + HALF, 35) % 2 == 0;
                    column.set(FLOOR - 1, hot ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                    column.set(FLOOR, Ground.WATER);
                } else if (this.hash(column, 1) < 0.04) {
                    column.fill(FLOOR + 1, FLOOR + 3, this.white);
                }

                return;
            }

            int tall = giant ? 62 : 24;
            boolean wallX = cellX == 0 || cellX == 34 || ax == HALF;
            boolean wallZ = cellZ == 0 || cellZ == 34 || z == NORTH || z == edge;
            if (giant) {
                wallX = ax == 24;
                wallZ = z == NORTH + 40 || z == edge - 60;
            }

            if (wallX || wallZ) {
                column.fill(FLOOR + 1, FLOOR + tall, this.black);
                column.set(FLOOR + 1, this.white);
                boolean door = (wallZ && Math.floorMod(x, 35) == 17) || (wallX && Math.floorMod(z, 35) == 17) || (z == edge && ax <= 5);
                if (door) {
                    column.clear(FLOOR + 1, FLOOR + (giant ? 20 : 6));
                }
            } else {
                column.set(FLOOR + tall, this.black);
                column.set(FLOOR + tall - 1, this.white);
                column.clear(FLOOR + 1, FLOOR + tall - 2);
                if (!giant && this.hash(column, 2) < 0.01) {
                    column.fill(FLOOR + 1, FLOOR + 4, this.white);
                }
            }

            // The roofs: dark tiles sweeping up at the eaves.
            if (wallX || wallZ || !giant) {
                column.set(FLOOR + tall + 1, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            }

            this.gatewayHall(column, x, z);
            this.iceCaves(column, x, z);
        }

        /** The hall of falling water by the main gate, the Gateway behind its cascade. */
        private void gatewayHall(Column column, int x, int z) {
            int hz = column.z - gatewayZ();
            int ax = Math.abs(x);
            if (ax > 14 || hz < -2 || hz > 26) {
                return;
            }

            column.clear(FLOOR + 1, FLOOR + 22);
            column.set(FLOOR, this.white);
            column.set(FLOOR + 23, this.white);
            if (ax == 14 || hz == -2) {
                column.fill(FLOOR + 1, FLOOR + 22, this.white);
            } else if (ax == 13 || hz == -1) {
                // Water cascading down the white jade walls into channels at their feet.
                column.fill(FLOOR + 1, FLOOR + 22, falling());
                column.set(FLOOR, Ground.WATER);
                column.set(FLOOR - 1, this.white);
            }

            if (column.x == gatewayX() && hz == -2) {
                column.set(FLOOR + 3, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
            }
        }

        /** The caves of ice in the mountain under the palace. */
        private void iceCaves(Column column, int x, int z) {
            double t = Math.sin(x / 13.0) + Math.cos(z / 11.0) + Math.sin((x + z) / 23.0);
            if (t > 1.2 && z < -40) {
                int floor = FLOOR - 26 + (int)(3.0 * Math.sin(z / 7.0));
                column.clear(floor + 1, floor + 9);
                column.set(floor, Blocks.PACKED_ICE.defaultBlockState());
                column.set(floor + 10, Blocks.BLUE_ICE.defaultBlockState());
            }
        }

        /** The bridge over the chasm: a deck on black, engraved pillars that rise out of the depths, and a town along it. */
        private void bridge(Column column, int x, int z, int edge) {
            int ax = Math.abs(x);
            column.fill(FLOOR - 4, FLOOR, this.black);
            if (ax == 26) {
                column.fill(FLOOR + 1, FLOOR + 2, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
            }

            // The pillars, from the bottom of the world.
            int along = z - edge;
            if ((ax >= 20 && ax <= 25) && Math.floorMod(along, 30) < 6) {
                column.fill(Column.MIN_Y, FLOOR - 5, Math.floorMod(along * 3 + ax, 5) == 0 ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState() : this.black);
            }

            if (ax > 6 && ax < 24) {
                Town.paint(column, this.cx - 24, this.cz + edge, FLOOR, this.salt, this.bridgeTown);
            }
        }

        /** The mundane city of black stone, its streets climbing the volcano's slope in terraces. */
        private void volcanoCity(Column column, int x, int z, int edge) {
            int row = (z - edge - 120) / 17;
            int ground = FLOOR + row * 6;
            int top = column.topSolid();
            if (top < ground - 12 || Places.RAVENHEART_VOLCANO.distance(column.x, column.z) < 120.0) {
                return;
            }

            column.clear(ground + 1, Math.max(ground + 1, top));
            column.fill(Math.min(top, ground), ground, Blocks.BLACKSTONE.defaultBlockState());
            Town.paint(column, this.cx - 200, this.cz + edge + 120, ground, this.salt + 1, this.city);
        }
    }

    /**
     * The citadel of Clan Sorrow, perched on the cliffs of the Great Waterfall, and the city below it on the shore of the
     * Lake of Tears, the Song Domain's busy river port.
     */
    public static final class Sorrow extends Built {
        private static final int OFFSET_X = -90;
        private final BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        private final Town.Style port = Town.Style.of(Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), 1, 3, 0.0);

        public Sorrow() {
            super(Places.WEEPING_GODDESS.x() + OFFSET_X, Places.WEEPING_GODDESS.z(), 320, 0x5022L);
        }

        public static int gatewayX() {
            return Places.WEEPING_GODDESS.x() + OFFSET_X;
        }

        public static int gatewayZ() {
            return (int)WeepingShaper.cliffZ(gatewayX()) - 30;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            double cliff = WeepingShaper.cliffZ(column.x);
            // The citadel on the clifftop.
            int kz = column.z - gatewayZ();
            if (Math.abs(x) <= 28 && kz >= -24 && column.z < cliff - 1) {
                int ground = WeepingShaper.PLATEAU;
                column.clear(ground + 1, Column.MAX_Y);
                column.fill(ground - 8, ground, Blocks.STONE.defaultBlockState());
                boolean wall = Math.abs(x) == 28 || kz == -24 || column.z >= cliff - 2;
                if (wall) {
                    column.fill(ground + 1, ground + 16, this.stone);
                    if (Math.floorMod(x + kz, 2) == 0) {
                        column.set(ground + 17, this.stone);
                    }

                    if (kz == -24 && Math.abs(x) <= 2) {
                        column.clear(ground + 1, ground + 6);
                    }
                } else if (Math.abs(x) <= 10 && kz >= -8 && kz <= 8) {
                    boolean shell = Math.abs(x) == 10 || Math.abs(kz) == 8;
                    column.fill(ground + 1, ground + (shell ? 34 : 0), this.stone);
                    column.set(ground + 35, Blocks.DEEPSLATE_TILES.defaultBlockState());
                    if (kz == -8 && Math.abs(x) <= 1) {
                        column.clear(ground + 1, ground + 4);
                    }

                    if (x == 0 && kz == 0) {
                        column.set(ground + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
                    }
                }

                return;
            }

            // The port city on the lake's western shore.
            double lake = WeepingShaper.lake(column.x, column.z);
            if (lake > 1.15 && lake < 1.9 && column.x < Places.WEEPING_GODDESS.x() && column.z > cliff + 20) {
                int ground = column.topSolid();
                if (ground >= WeepingShaper.LAKE + 2 && ground < WeepingShaper.LOWLAND + 8) {
                    column.clear(ground + 1, ground + 30);
                    Town.paint(column, this.cx, this.cz, ground, this.salt, this.port);
                }
            }

            // Docks reaching into the lake.
            if (lake > 0.92 && lake < 1.12 && column.x < Places.WEEPING_GODDESS.x() - 100 && Math.floorMod(column.z, 30) < 3) {
                column.set(WeepingShaper.LAKE, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    /**
     * Rivergate, citadel of Clan Dagonet: across the River of Tears where it falls toward the Stormsea, a series of
     * walls descending the cliffs like steps meant for a giant, each holding a lock chamber that is filled and drained
     * so ships can climb or descend without leaving the river. Siege engines stand on the battlements.
     */
    public static final class Rivergate extends Built {
        private static final int SPAN = 80;
        private final BlockState stone = Blocks.POLISHED_ANDESITE.defaultBlockState();
        private final BlockState block = Blocks.STONE_BRICKS.defaultBlockState();

        public Rivergate() {
            super(Places.RIVERGATE.x(), WeepingShaper.LOCKS_START + WeepingShaper.LOCK_LENGTH * WeepingShaper.LOCKS / 2, 130, 0xD4605L);
        }

        public static int gatewayX() {
            return Places.RIVERGATE.x() - SPAN + 6;
        }

        public static int gatewayZ() {
            return WeepingShaper.LOCKS_START + 2;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int z = column.z;
            double river = WeepingShaper.riverX(z);
            int x = (int)Math.round(column.x - river);
            if (Math.abs(x) > SPAN || z < WeepingShaper.LOCKS_START - 4 || z > WeepingShaper.LOCKS_END + 4) {
                return;
            }

            for (int k = 0; k <= WeepingShaper.LOCKS; k++) {
                int wz = WeepingShaper.LOCKS_START + k * WeepingShaper.LOCK_LENGTH;
                if (z < wz - 3 || z > wz + 4) {
                    continue;
                }

                int above = WeepingShaper.riverLevel(wz - 1);
                int below = WeepingShaper.riverLevel(wz + 5);
                int top = above + 12;
                column.fill(below - 12, top, this.hash(column, k) < 0.1 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : this.block);
                if (Math.abs(x) <= 12) {
                    // The lock gates, in the river channel.
                    column.fill(below - 6, above + 2, Blocks.IRON_BARS.defaultBlockState());
                    column.fill(below - 8, below - 7, this.stone);
                    column.set(top, this.stone);
                } else if (Math.floorMod(x, 3) == 0) {
                    column.set(top + 1, this.block);
                }

                if (Math.floorMod(x, 20) == 10 && Math.abs(x) > 14) {
                    column.set(top + 1, Blocks.DISPENSER.defaultBlockState());
                    column.set(top + 2, Blocks.OAK_FENCE.defaultBlockState());
                }
            }

            // The tower over the top lock, where the Gateway is.
            int gx = column.x - gatewayX();
            int gz = z - gatewayZ();
            if (Math.abs(gx) <= 5 && Math.abs(gz) <= 5) {
                int base = WeepingShaper.LAKE;
                boolean shell = Math.abs(gx) == 5 || Math.abs(gz) == 5;
                column.fill(base - 10, base + (shell ? 40 : 0), this.block);
                column.clear(base + 1, shell ? base : base + 39);
                if (shell) {
                    column.fill(base + 1, base + 40, this.block);
                    if (gz == 5 && Math.abs(gx) <= 1) {
                        column.clear(base + 1, base + 4);
                    }
                }

                column.set(base + 41, Blocks.DEEPSLATE_TILES.defaultBlockState());
                if (gx == 0 && gz == 0) {
                    column.set(base + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
                }
            }
        }
    }

    /**
     * Red Hill, a young city on its hill at the Glass Hell's southern edge: the wealthier houses faced with wood and
     * stone, the poorer ones bare glass. Glass quarries open into the plain around it, and the Gateway is kept in a
     * small fort on the hilltop.
     */
    public static final class RedHill extends Built {
        private static final double HILL = 110.0;
        private static final int TOP = 102;
        private final Town.Style rich = Town.Style.of(Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_SLAB.defaultBlockState(), Blocks.RED_SANDSTONE.defaultBlockState(), 1, 2, 0.0);
        private final Town.Style poor = Town.Style.of(Blocks.GLASS.defaultBlockState(), Blocks.TINTED_GLASS.defaultBlockState(),
            Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Blocks.GLASS.defaultBlockState(), Blocks.RED_SANDSTONE.defaultBlockState(), 1, 1, 0.0);

        public RedHill() {
            super(Places.RED_HILL.x(), Places.RED_HILL.z(), 200, 0x2ED1L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double d = this.dist(column);
            int x = this.dx(column);
            int z = this.dz(column);
            if (d < HILL) {
                int ground = column.topSolid();
                int top = TOP - (int)(30.0 * Math.pow(d / HILL, 2.2));
                if (top > ground) {
                    column.fill(ground, top, Blocks.RED_SANDSTONE.defaultBlockState());
                }

                column.set(top, Blocks.RED_SAND.defaultBlockState());
                if (Math.abs(x) <= 9 && Math.abs(z) <= 9) {
                    boolean shell = Math.abs(x) == 9 || Math.abs(z) == 9;
                    column.set(top, Blocks.STONE_BRICKS.defaultBlockState());
                    if (shell) {
                        column.fill(top + 1, top + 8, Blocks.STONE_BRICKS.defaultBlockState());
                        if (z == 9 && Math.abs(x) <= 1) {
                            column.clear(top + 1, top + 3);
                        }
                    }

                    if (x == 0 && z == 0) {
                        column.set(top + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
                    }
                } else if (d < HILL - 18) {
                    boolean wealthy = d < 50.0;
                    Town.paint(column, this.cx, this.cz, top, this.salt, wealthy ? this.rich : this.poor);
                }

                return;
            }

            // Quarries cut into the glass around the hill.
            int qx = Math.floorMod(column.x, 60);
            int qz = Math.floorMod(column.z, 60);
            if (d < 190.0 && qx > 10 && qx < 34 && qz > 14 && qz < 32 && this.hashCell(Math.floorDiv(column.x, 60), Math.floorDiv(column.z, 60)) < 0.45) {
                int top = column.topSolid();
                int depth = Math.min(qx - 10, Math.min(34 - qx, Math.min(qz - 14, 32 - qz))) * 2;
                column.clear(top - depth, top);
            }
        }

        private double hashCell(int i, int j) {
            return Noise.hash(this.salt, i, j, 3);
        }
    }

    /**
     * The Night Garden, great citadel of the House of Night: a titanic ship, its seamless hull hollowed from a single
     * branch of an ancient tree, sailing the Stormsea like a city of its own, with decks and palaces and tall pagodas,
     * and gardens growing on it.
     */
    public static final class NightGarden extends Built {
        private static final int LENGTH = 150;
        private static final int BEAM = 32;
        private static final int KEEL = 40;
        private static final int DECK = 76;
        private final BlockState hull = Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
        private final BlockState deck = Blocks.DARK_OAK_PLANKS.defaultBlockState();

        public NightGarden() {
            super(Places.NIGHT_GARDEN.x(), Places.NIGHT_GARDEN.z(), LENGTH + 10, 0x416EL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double along = x / (double)LENGTH;
            if (Math.abs(along) > 1.0) {
                return;
            }

            // The hull narrows to a point at bow and stern.
            double beam = BEAM * Math.sqrt(1.0 - along * along);
            if (Math.abs(z) > beam) {
                return;
            }

            double across = Math.abs(z) / Math.max(1.0, beam);
            int keel = (int)(KEEL + (DECK - KEEL) * across * across);
            boolean side = Math.abs(z) > beam - 2.0;
            column.clear(keel, DECK + 60);
            if (side) {
                column.fill(keel, DECK + 2, this.hull);
            } else {
                column.set(keel, this.hull);
                for (int y = DECK - 12; y > keel; y -= 12) {
                    column.set(y, this.deck);
                }

                column.set(DECK, this.deck);
            }

            if (side) {
                return;
            }

            // The palace amidships, the pagodas fore and aft, and the gardens between.
            int ax = Math.abs(x);
            if (ax <= 18 && Math.abs(z) <= 12) {
                boolean shell = ax == 18 || Math.abs(z) == 12;
                column.fill(DECK + 1, DECK + (shell ? 16 : 0), Blocks.DARK_OAK_PLANKS.defaultBlockState());
                column.set(DECK + 17, Blocks.DEEPSLATE_TILES.defaultBlockState());
                if (Math.abs(z) == 12 && ax <= 2) {
                    column.clear(DECK + 1, DECK + 4);
                }

                if (x == 0 && z == 0) {
                    column.set(DECK + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
                }

                return;
            }

            for (int p = -1; p <= 1; p += 2) {
                int px = x - p * 80;
                for (int t = 0; t < 5; t++) {
                    int half = 9 - t * 2;
                    int y0 = DECK + 1 + t * 8;
                    if (Math.abs(px) <= half && Math.abs(z) <= half) {
                        boolean shell = Math.abs(px) == half || Math.abs(z) == half;
                        column.fill(y0, y0 + 5, shell ? Blocks.DARK_OAK_PLANKS.defaultBlockState() : null);
                        column.set(y0 + 6, Blocks.DEEPSLATE_TILES.defaultBlockState());
                    }
                }
            }

            if (ax > 30 && ax < 60 && this.hash(column, 1) < 0.5) {
                column.set(DECK, Blocks.GRASS_BLOCK.defaultBlockState());
                column.set(DECK - 1, Blocks.DIRT.defaultBlockState());
                Trees.paint(column, 8, (i, j) -> {
                    int tx = i * 8 + 4;
                    int tz = j * 8 + 4;
                    int dx = tx - this.cx;
                    if (Math.abs(dx) <= 30 || Math.abs(dx) >= 60 || Math.abs(tz - this.cz) > BEAM - 8) {
                        return null;
                    }

                    return new Trees.Tree(tx, tz, DECK + 1, 6, 3, Blocks.DARK_OAK_LOG.defaultBlockState(), Blocks.AZALEA_LEAVES.defaultBlockState(),
                        Trees.Crown.ROUND);
                });
            }
        }
    }

    /**
     * The Black Obelisk: a great black obelisk rising from a rocky island of the Stormsea, a citadel hollow inside, its
     * Gateway in the hall at its foot.
     */
    public static final class BlackObelisk extends Built {
        private static final double ISLAND = 64.0;
        private static final int BASE = 72;
        private static final int TOP = 250;
        private final BlockState black = Blocks.POLISHED_BLACKSTONE.defaultBlockState();

        public BlackObelisk() {
            super(Places.BLACK_OBELISK.x(), Places.BLACK_OBELISK.z(), (int)ISLAND + 10, 0x0BE1L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double d = this.dist(column);
            int x = this.dx(column);
            int z = this.dz(column);
            if (d < ISLAND) {
                int top = BASE - (int)(14.0 * Math.pow(d / ISLAND, 2.0));
                column.fill(20, top, Blocks.STONE.defaultBlockState());
                column.set(top, d < ISLAND * 0.7 ? Blocks.STONE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
            }

            int m = Math.max(Math.abs(x), Math.abs(z));
            for (int y = BASE + 1; y <= TOP; y++) {
                double half = 13.0 - 10.0 * (y - BASE) / (double)(TOP - BASE);
                if (m <= half) {
                    boolean shell = m > half - 1.5;
                    column.set(y, shell || y > TOP - 6 ? this.black : null);
                    if (!shell && (y - BASE) % 12 == 0) {
                        column.set(y, Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState());
                    }
                }
            }

            if (z == 13 && Math.abs(x) <= 1) {
                column.clear(BASE + 1, BASE + 4);
            }

            if (x == 0 && z == 0) {
                column.set(BASE + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
            }
        }
    }
}
