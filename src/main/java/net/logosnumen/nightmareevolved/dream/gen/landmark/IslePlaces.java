package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.Trees;
import net.logosnumen.nightmareevolved.dream.gen.shape.IslesShaper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The named places of the Chained Isles. */
public final class IslePlaces {
    private IslePlaces() {
    }

    /**
     * The Sanctuary of Noctis, the citadel of the Isles' Awakened, on a small island that never rises or falls. Two
     * perfect circles of massive menhirs form its outer and inner walls, with stone monoliths laid flat across their
     * tops into a second world of grass and moss above. Inside is a quiet park and a pool of clear water; stepping
     * stones lead to an islet where, in an ancient tree's shade, stands the white marble altar that is the Gateway, an
     * endless stream of water running from it. Seven streams flow out of the pool and fall from the island's edges.
     */
    public static final class Sanctuary extends Built {
        public static final int TOP = 170;
        private static final double OUTER = 38.0;
        private static final double INNER = 24.0;
        private static final double POOL = 12.0;
        private final BlockState stone = Blocks.STONE.defaultBlockState();
        private final BlockState slab = Blocks.SMOOTH_STONE.defaultBlockState();

        public Sanctuary() {
            super(Places.SANCTUARY_OF_NOCTIS.x(), Places.SANCTUARY_OF_NOCTIS.z(), 52, 0x5A7CL);
        }

        /** Where the altar stands, on the islet in the pool. */
        public static int altarX() {
            return Places.SANCTUARY_OF_NOCTIS.x();
        }

        public static int altarZ() {
            return Places.SANCTUARY_OF_NOCTIS.z() - 1;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double d = this.dist(column);
            int top = column.topSolid();
            if (top < TOP - 12) {
                return;
            }

            double angle = Math.atan2(column.z + 0.5 - this.cz, column.x + 0.5 - this.cx);
            boolean stream = streamAt(angle, d);
            if (Math.abs(d - OUTER) < 2.0 || Math.abs(d - INNER) < 1.6) {
                double step = Math.abs(d - OUTER) < 2.0 ? Math.toRadians(9.0) : Math.toRadians(14.0);
                double nearest = Math.round(angle / step) * step;
                if (Math.abs(angle - nearest) * d < 1.4 && !stream) {
                    // A menhir.
                    column.fill(top + 1, top + 14, this.stone);
                }
            }

            if (d > INNER - 3.0 && d < OUTER + 3.0) {
                // The monoliths laid across the menhirs, and the green world on top of them.
                column.fill(top + 15, top + 16, this.slab);
                column.set(top + 17, this.hash(column, 1) < 0.7 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                if (this.hash(column, 2) < 0.2) {
                    column.set(top + 18, Blocks.SHORT_GRASS.defaultBlockState());
                }

                // Siege engines at the outer edge, watching the neighbouring islands.
                if (d > OUTER + 1.5 && Math.floorMod((int)Math.round(Math.toDegrees(angle)), 40) < 2) {
                    column.set(top + 18, Blocks.DISPENSER.defaultBlockState());
                    column.set(top + 19, Blocks.OAK_FENCE.defaultBlockState());
                }
            }

            if (d < POOL) {
                // The pool, and the islet in the middle of it.
                double islet = Math.sqrt(Math.pow(column.x + 0.5 - altarX(), 2) + Math.pow(column.z + 0.5 - altarZ(), 2));
                if (islet < 3.5) {
                    column.set(top, Blocks.GRASS_BLOCK.defaultBlockState());
                } else {
                    column.clear(top - 2, top);
                    column.fill(top - 3, top - 3, Blocks.SAND.defaultBlockState());
                    column.fill(top - 2, top, Ground.WATER);
                    // The stepping stones from the south.
                    if (Math.abs(column.x - this.cx) <= 1 && column.z > this.cz && Math.floorMod(column.z - this.cz, 3) == 0) {
                        column.set(top, Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
            } else if (stream && d < 50.0) {
                // One of the seven streams running out to the edge.
                column.set(top, Ground.WATER);
                column.set(top - 1, Blocks.STONE.defaultBlockState());
            } else if (d < INNER - 2.0 && column.get(top + 1) == null) {
                double c = this.hash(column, 3);
                if (c < 0.12) {
                    column.set(top + 1, c < 0.05 ? Blocks.WHITE_TULIP.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState());
                }
            }

            // The ancient tree on the islet, and the altar in its shade.
            Trees.paint(column, new Trees.Tree(altarX() + 2, altarZ() + 1, TOP + 1, 11, 6, Blocks.OAK_LOG.defaultBlockState(),
                Blocks.OAK_LEAVES.defaultBlockState(), Trees.Crown.ROUND));
            if (column.x == altarX() && column.z == altarZ()) {
                column.set(TOP + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.WHITE_ALTAR));
            } else if (column.x == altarX() && column.z == altarZ() - 1) {
                // The water that never stops running from the altar.
                column.set(TOP + 1, Ground.WATER);
            }
        }

        private static boolean streamAt(double angle, double d) {
            double step = Math.PI * 2.0 / 7.0;
            double nearest = Math.round((angle - 0.3) / step) * step + 0.3;
            return d > POOL - 0.5 && Math.abs(angle - nearest) * d < 0.7;
        }
    }

    /**
     * The Northern Island, beautiful and silent, with the ruins of an ancient fortress on its northern edge. From the
     * platform past the fortress gate, a narrow stair winds down the overhanging slope to the island's dark side, where
     * the Night Temple hangs from its underbelly like a cathedral built upside down: its seven belfries reach down into
     * the void, each ending in a short chain and a great bell of tarnished green copper. Inside, the black stone altar
     * that is its Gateway, and behind it the statue of a veiled woman holding a star and a bolt of lightning.
     */
    public static final class NorthernIsland extends Built {
        public static final int TOP = 176;
        public static final int HALL = 96;
        private static final int TEMPLE_Z = -66;
        private static final double STAIR = 77.0;
        private final BlockState black = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        private final BlockState dark = Blocks.BLACKSTONE.defaultBlockState();

        public NorthernIsland() {
            super(Places.NORTHERN_ISLAND.x(), Places.NORTHERN_ISLAND.z(), 96, 0x2077L);
        }

        public static int altarX() {
            return Places.NORTHERN_ISLAND.x();
        }

        public static int altarZ() {
            return Places.NORTHERN_ISLAND.z() + TEMPLE_Z + 18;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            this.fortress(column, x, z);
            this.stair(column, x, z);
            this.temple(column, x, z);
        }

        /** The fortress ruins along the northern edge: broken walls and a gate onto a platform at the brink. */
        private void fortress(Column column, int x, int z) {
            int top = column.topSolid();
            if (top < TOP - 8 || z > -40 || z < -74 || Math.abs(x) > 30) {
                return;
            }

            boolean wall = z == -44 || Math.abs(x) == 30 || (z == -66 && Math.abs(x) > 5);
            if (wall) {
                int tall = 3 + (int)(this.hash(column, 1) * 9);
                column.fill(top + 1, top + tall, this.hash(column, 2) < 0.4 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                    : Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            } else if (z < -66 && z > -74 && Math.abs(x) <= 12) {
                column.set(top, Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        /**
         * The narrow stair cut into the rock, winding down from the platform's corner around the island's rim to its
         * dark side: barely room for two, the rock on one hand and the Sky Below on the other.
         */
        private void stair(Column column, int x, int z) {
            double d = Math.sqrt(x * x + z * z);
            if (d < STAIR - 1.0 || d > STAIR + 1.5) {
                return;
            }

            double phi = Math.toDegrees(Math.atan2(z, x));
            for (int k = -1; k <= 1; k++) {
                double t = (240.0 - phi - 360.0 * k) / 330.0;
                if (t < 0.0 || t > 1.0) {
                    continue;
                }

                int y = (int)Math.round(TOP - (TOP - HALL) * t);
                column.clear(y + 1, y + 4);
                column.set(y, Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        /** The Night Temple, hanging under the island. */
        private void temple(Column column, int x, int z) {
            int tz = z - TEMPLE_Z;
            if (Math.abs(x) > 15 || tz < -6 || tz > 24) {
                // The ledge from the stair's foot to the temple's gate.
                if (Math.abs(x) <= 1 && z < TEMPLE_Z - 6 && z > -STAIR - 1) {
                    column.set(HALL, this.dark);
                    column.clear(HALL + 1, HALL + 4);
                }

                return;
            }

            // The hall's walls rise from its floor into the island's underside.
            int roof = Math.min(column.solidAbove(HALL + 4), TOP - 4);
            boolean shell = Math.abs(x) == 15 || tz == -6 || tz == 24;
            // The hall: walls hanging from the rock above down to its floor, and a floor over the void.
            if (shell) {
                column.fill(HALL - 3, roof, this.black);
                if (tz == -6 && Math.abs(x) <= 2) {
                    column.clear(HALL + 1, HALL + 6);
                }

                if (!(tz == -6 || tz == 24) && Math.floorMod(tz, 6) == 3) {
                    column.clear(HALL + 5, HALL + 12);
                }
            } else {
                column.clear(HALL + 1, roof - 1);
                column.fill(HALL - 3, HALL, this.black);
            }

            // The altar of black stone, and behind it the veiled woman with her star and lightning.
            if (x == 0 && z == altarZ() - this.cz) {
                column.set(HALL + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.BLACK_ALTAR));
            }

            if (Math.abs(x) <= 1 && tz == 22) {
                column.fill(HALL + 1, HALL + 9, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                if (x == 0) {
                    column.set(HALL + 10, Blocks.CALCITE.defaultBlockState());
                }
            }

            if (tz == 21 && x == -2) {
                column.set(HALL + 8, Blocks.END_ROD.defaultBlockState());
            }

            if (tz == 21 && x == 2) {
                column.set(HALL + 8, Blocks.LIGHTNING_ROD.defaultBlockState());
            }

            // The seven belfries reaching down into the void, chains and copper bells at their ends.
            for (int b = 0; b < 7; b++) {
                int bx = -12 + b * 4;
                int bz = 2 + (b % 2) * 14;
                int ox = Math.abs(x - bx);
                int oz = Math.abs(tz - bz);
                int depth = 30 + (b == 3 ? 18 : (b % 3) * 6);
                if (ox <= 1 && oz <= 1) {
                    column.fill(HALL - depth, HALL - 4, this.black);
                    if (ox == 0 && oz == 0) {
                        column.fill(HALL - depth - 4, HALL - depth - 1, Blocks.CHAIN.defaultBlockState());
                    }
                }

                if (ox <= 2 && oz <= 2) {
                    double r = Math.sqrt(ox * ox + oz * oz);
                    int bellTop = HALL - depth - 5;
                    if (r <= 2.3) {
                        // A great bell: copper all round, open at the mouth.
                        column.fill(bellTop - 3, bellTop, Blocks.OXIDIZED_COPPER.defaultBlockState());
                        if (r < 1.5) {
                            column.clear(bellTop - 3, bellTop - 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * The Ivory Tower: a pagoda of a flawlessly white stuff that is neither stone nor wood, drifting high over the Tear
     * on an island barely wider than its base, with shattered marble floating around it and seven broken chains hanging
     * from its soil. Beside it, a meadow and a grove, a white gazebo with an empty white arch, and a clear lake whose
     * streams fall from the island's edge. A dragon's white bones wrap the tower's foot, its skull before the gates. The
     * Dark Seed grows inside.
     */
    public static final class IvoryTower extends Built {
        public static final int TOP = 300;
        private static final double RADIUS = 24.0;
        private static final int TIERS = 7;
        private static final int TIER = 9;
        private static final int HALF = 9;
        private static final Noise RIM = new Noise(0x1702L, 2);
        private final BlockState ivory = DreamBlocks.IVORY.get().defaultBlockState();

        public IvoryTower() {
            super(Places.IVORY_TOWER.x(), Places.IVORY_TOWER.z(), 64, 0x1702L);
        }

        /** The empty white arch in the gazebo, where the way from the Ebony Tower comes out. */
        public static int archX() {
            return Places.IVORY_TOWER.x() - 15;
        }

        public static int archZ() {
            return Places.IVORY_TOWER.z() + 2;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double d = this.dist(column);
            double edge = RADIUS * (1.0 + 0.15 * RIM.at(column.x / 9.0, column.z / 9.0));
            if (d < edge) {
                int under = TOP - (int)(26.0 * Math.pow(1.0 - d / edge, 0.6));
                column.fill(under, TOP - 1, Blocks.STONE.defaultBlockState());
                column.fill(TOP - 3, TOP - 1, Blocks.DIRT.defaultBlockState());
                column.set(TOP, Blocks.GRASS_BLOCK.defaultBlockState());
                this.grounds(column, x, z);
            }

            // Shattered marble drifting around the island.
            if (d > edge + 3.0 && d < 56.0 && Noise.hash(this.salt, column.x >> 2, column.z >> 2, 1) < 0.08) {
                int y = TOP - 12 + (int)(Noise.hash(this.salt, column.x >> 2, column.z >> 2, 2) * 24.0);
                column.set(y, this.hash(column, 3) < 0.5 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.QUARTZ_SLAB.defaultBlockState());
            }

            // The seven broken chains hanging from the island's soil.
            for (int c = 0; c < 7; c++) {
                double a = c * Math.PI * 2.0 / 7.0 + 0.2;
                int chx = (int)Math.round(Math.cos(a) * (RADIUS - 4.0));
                int chz = (int)Math.round(Math.sin(a) * (RADIUS - 4.0));
                if (x == chx && z == chz) {
                    int under = column.solidBelow(TOP) - 26;
                    int length = 18 + (c * 7) % 23;
                    for (int y = TOP - 6; y > TOP - 26 - length; y--) {
                        if (!column.solid(y)) {
                            column.set(y, Blocks.CHAIN.defaultBlockState());
                        }
                    }

                    column.set(under, Blocks.IRON_BLOCK.defaultBlockState());
                }
            }

            this.tower(column, x, z);
            this.dragon(column, x, z);
        }

        /** The meadow, the grove, the gazebo with its empty arch, the bench, and the lake. */
        private void grounds(Column column, int x, int z) {
            // The lake, on the far side from the gazebo.
            double lx = x - 14.0;
            double lz = z - 2.0;
            if (lx * lx + lz * lz < 36.0) {
                column.fill(TOP - 2, TOP, Ground.WATER);
                column.set(TOP - 3, Blocks.SAND.defaultBlockState());
            } else if (Math.abs(z - 2) <= 0 && x > 19 && x < 24) {
                // A stream running out to the edge and over it.
                column.set(TOP, Ground.WATER);
            } else if (Math.abs(x - (archX() - this.cx)) <= 3 && Math.abs(z - (archZ() - this.cz)) <= 3) {
                // The gazebo: four white pillars, a roof, and the empty arch between.
                int gx = x - (archX() - this.cx);
                int gz = z - (archZ() - this.cz);
                column.set(TOP, this.ivory);
                if (Math.abs(gx) == 3 && Math.abs(gz) == 3) {
                    column.fill(TOP + 1, TOP + 5, Blocks.QUARTZ_PILLAR.defaultBlockState());
                }

                column.set(TOP + 6, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
                if (gx == 0 && Math.abs(gz) == 1) {
                    column.fill(TOP + 1, TOP + 4, this.ivory);
                }

                if (gx == 0 && gz == 0) {
                    column.set(TOP + 4, this.ivory);
                }
            } else if (this.hash(column, 4) < 0.15) {
                column.set(TOP + 1, this.hash(column, 5) < 0.4 ? Blocks.OXEYE_DAISY.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState());
            }

            if (x == -6 && z >= 13 && z <= 15) {
                column.set(TOP + 1, Blocks.QUARTZ_STAIRS.defaultBlockState());
            }

            if (x < -8 && z < -8) {
                Trees.paint(column, new Trees.Tree(this.cx - 14, this.cz - 13, TOP + 1, 7, 3, Blocks.BIRCH_LOG.defaultBlockState(),
                    Blocks.BIRCH_LEAVES.defaultBlockState(), Trees.Crown.ROUND));
            }
        }

        /** The pagoda: seven tiers, each a little narrower, with broad eaves, and the Dark Seed in its hall. */
        private void tower(Column column, int x, int z) {
            int ax = Math.abs(x);
            int az = Math.abs(z);
            for (int t = 0; t < TIERS; t++) {
                int half = HALF - t;
                int y0 = TOP + 1 + t * TIER;
                int y1 = y0 + TIER - 3;
                if (ax <= half && az <= half) {
                    boolean shell = ax == half || az == half;
                    if (shell) {
                        column.fill(y0, y1, this.ivory);
                        if (t == 0 && z == half && ax <= 1) {
                            column.clear(y0, y0 + 3);
                        } else if ((ax + az) % 4 == 2 && !(ax == half && az == half)) {
                            column.clear(y0 + 2, y0 + 3);
                        }
                    } else {
                        column.clear(y0, y1 + 2);
                    }
                }

                // The eaves, overhanging the tier below.
                int eave = half + 2;
                if (ax <= eave && az <= eave && (ax > half - 1 || az > half - 1 || t == TIERS - 1)) {
                    column.set(y1 + 1, Blocks.QUARTZ_SLAB.defaultBlockState());
                }

                if (ax <= half && az <= half) {
                    column.set(y1 + 2, this.ivory);
                }
            }

            if (ax <= 1 && az <= 1) {
                int spire = TOP + 1 + TIERS * TIER;
                column.fill(spire, spire + (ax == 0 && az == 0 ? 9 : 3), this.ivory);
            }

            // The Dark Seed, in the middle of the hall.
            if (x == 0 && z == 0) {
                column.set(TOP + 1, DreamBlocks.SEED.get().seed(SeedBlock.Form.SHACKLES, 2));
            }
        }

        /** The dragon's bones: the tail wound around the tower's foot, the skull before the gates. */
        private void dragon(Column column, int x, int z) {
            double angle = Math.atan2(z, x);
            double d = Math.sqrt(x * x + z * z);
            double turn = (angle + Math.PI) / (Math.PI * 2.0);
            double coil = HALF + 3.5;
            if (Math.abs(d - coil) < 1.6) {
                int y = TOP + 1 + (int)(turn * 6.0);
                column.fill(y, y + 1, Blocks.BONE_BLOCK.defaultBlockState());
            }

            double sx = x;
            double sz = z - (HALF + 9);
            if (Math.abs(sx) < 5.0 && Math.abs(sz) < 7.0) {
                double n = (sx / 5.0) * (sx / 5.0) + (sz / 7.0) * (sz / 7.0);
                if (n < 1.0) {
                    int h = (int)(4.0 * Math.sqrt(1.0 - n));
                    column.fill(TOP + 1, TOP + 1 + h, Blocks.BONE_BLOCK.defaultBlockState());
                    if (Math.abs(sx) < 2.5 && sz < -3.0) {
                        column.clear(TOP + 2, TOP + h);
                    }
                }

                if (sz > 5.0 && Math.abs(sx) == 3) {
                    column.fill(TOP + 1, TOP + 3, Blocks.BONE_BLOCK.defaultBlockState());
                }
            }
        }
    }

    /**
     * The Ebony Tower, deep in the Sky Below: a perfect black replica of the Ivory Tower, larger at the ground, on a
     * small island of dark stone hidden from the light, with shattered obsidian drifting around it and obsidian
     * pillars jutting out from its edges like wharves into the void. The tower is covered in soot. A way at its top
     * leads to the Ivory Tower's island.
     */
    public static final class EbonyTower extends Built {
        public static final int TOP = 24;
        private static final double RADIUS = 28.0;
        private static final int TIERS = 7;
        private static final int TIER = 10;
        private static final int HALF = 11;
        private final BlockState ebony = DreamBlocks.EBONY.get().defaultBlockState();

        public EbonyTower() {
            super(Places.EBONY_TOWER.x(), Places.EBONY_TOWER.z(), 70, 0xEB0EL);
        }

        /** The arch on the top floor, the way to the Ivory Tower. */
        public static int archY() {
            return TOP + 1 + (TIERS - 1) * TIER;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double d = this.dist(column);
            if (d < RADIUS) {
                int under = TOP - (int)(30.0 * Math.pow(1.0 - d / RADIUS, 0.6));
                column.fill(under, TOP, Blocks.BLACKSTONE.defaultBlockState());
                column.set(TOP, Blocks.BASALT.defaultBlockState());
                if (this.hash(column, 1) < 0.04 && d > HALF + 3) {
                    column.fill(TOP + 1, TOP + 1 + (int)(this.hash(column, 2) * 4), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                }
            }

            // Obsidian pillars jutting out over the void like wharves.
            for (int p = 0; p < 4; p++) {
                double a = p * Math.PI / 2.0 + 0.35;
                double ux = Math.cos(a);
                double uz = Math.sin(a);
                double along = x * ux + z * uz;
                double across = Math.abs(-x * uz + z * ux);
                if (across < 1.2 && along > RADIUS - 6 && along < RADIUS + 18) {
                    column.fill(TOP - 1, TOP, Blocks.OBSIDIAN.defaultBlockState());
                }
            }

            if (d > RADIUS + 3 && d < 64 && Noise.hash(this.salt, column.x >> 2, column.z >> 2, 3) < 0.06) {
                int y = TOP - 10 + (int)(Noise.hash(this.salt, column.x >> 2, column.z >> 2, 4) * 24);
                column.set(y, Blocks.OBSIDIAN.defaultBlockState());
            }

            int ax = Math.abs(x);
            int az = Math.abs(z);
            for (int t = 0; t < TIERS; t++) {
                int half = HALF - t;
                int y0 = TOP + 1 + t * TIER;
                int y1 = y0 + TIER - 3;
                if (ax <= half && az <= half) {
                    if (ax == half || az == half) {
                        column.fill(y0, y1, this.hash(column, 5 + t) < 0.25 ? Blocks.COAL_BLOCK.defaultBlockState() : this.ebony);
                        if (t == 0 && z == half && ax <= 1) {
                            column.clear(y0, y0 + 4);
                        }
                    } else {
                        column.clear(y0, y1 + 2);
                    }

                    column.set(y1 + 2, this.ebony);
                }

                int eave = half + 2;
                if (ax <= eave && az <= eave && (ax > half - 1 || az > half - 1)) {
                    column.set(y1 + 1, Blocks.BLACKSTONE_SLAB.defaultBlockState());
                }
            }

            // The arch on the top floor: the way to the Ivory Tower.
            int top = archY();
            if (z == 0 && ax <= 2) {
                if (ax == 2) {
                    column.fill(top, top + 4, this.ebony);
                } else {
                    column.set(top + 5, this.ebony);
                }
            }
        }
    }

    /** The lesser named isles: Iron Hand, Reckoning, Shipwreck and the Desecrated Grove. */
    public static final class Lesser extends Built {
        private final Places.Place place;

        public Lesser(Places.Place place) {
            super(place.x(), place.z(), 50, place.id().hashCode());
            this.place = place;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int top = column.topSolid();
            if (top < 120) {
                return;
            }

            int x = this.dx(column);
            int z = this.dz(column);
            if (this.place == Places.IRON_HAND) {
                this.hand(column, x, z, top);
            } else if (this.place == Places.RECKONING) {
                this.reckoning(column, x, z, top);
            } else if (this.place == Places.SHIPWRECK) {
                this.ship(column, x, z, top);
            } else if (this.place == Places.DESECRATED_GROVE) {
                this.grove(column, x, z, top, terrain);
            }
        }

        /** A colossal hand of iron rising from the rock, its fingers reaching for the Sky Above. */
        private void hand(Column column, int x, int z, int top) {
            BlockState iron = this.hash(column, 1) < 0.3 ? Blocks.RAW_IRON_BLOCK.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState();
            if (Math.abs(x) <= 6 && Math.abs(z) <= 3) {
                column.fill(top - 2, top + 14, iron);
            }

            for (int f = 0; f < 5; f++) {
                int fx = -6 + f * 3;
                int tall = f == 0 ? 22 : 30 - Math.abs(f - 2) * 3;
                int lean = f == 0 ? -4 : 0;
                for (int y = 14; y <= tall; y++) {
                    int ox = x - fx - (lean * (y - 14)) / Math.max(1, tall - 14);
                    if (Math.abs(ox) <= 1 && Math.abs(z) <= 1) {
                        column.set(top + y, iron);
                    }
                }
            }
        }

        /** A ring of standing stones around a mirror-still pool. */
        private void reckoning(Column column, int x, int z, int top) {
            double d = Math.sqrt(x * x + z * z);
            if (d < 9.0) {
                column.set(top - 1, Blocks.OBSIDIAN.defaultBlockState());
                column.set(top, Ground.WATER);
            } else if (d < 10.0) {
                column.set(top, Blocks.OBSIDIAN.defaultBlockState());
            } else if (Math.abs(d - 18.0) < 1.5 && Math.floorMod((int)Math.round(Math.toDegrees(Math.atan2(z, x))), 30) < 6) {
                column.fill(top + 1, top + 9, Blocks.STONE.defaultBlockState());
            }
        }

        /** A great ship, broken in two, stranded high on the island. */
        private void ship(Column column, int x, int z, int top) {
            if (Math.abs(x) > 22 || Math.abs(z) > 6) {
                return;
            }

            boolean broken = x > -3 && x < 2;
            if (broken) {
                return;
            }

            int tilt = x > 0 ? (x - 2) / 6 : 0;
            int keel = top + 1 + tilt;
            int beam = 6 - Math.abs(x) / 5;
            if (Math.abs(z) > beam) {
                return;
            }

            BlockState hull = Blocks.DARK_OAK_PLANKS.defaultBlockState();
            boolean side = Math.abs(z) == beam || Math.abs(x) == 22;
            int deck = keel + 5;
            if (side) {
                column.fill(keel, deck + 1, hull);
            } else {
                column.set(keel, hull);
                column.set(deck, Blocks.DARK_OAK_SLAB.defaultBlockState());
            }

            if (z == 0 && (x == -12 || x == 10)) {
                column.fill(deck + 1, deck + 14 - (x > 0 ? 6 : 0), Blocks.DARK_OAK_LOG.defaultBlockState());
            }
        }

        /** A grove of dead trees around a circle of broken statues. */
        private void grove(Column column, int x, int z, int top, RealmTerrain terrain) {
            double d = Math.sqrt(x * x + z * z);
            if (Math.abs(d - 10.0) < 1.0 && Math.floorMod((int)Math.round(Math.toDegrees(Math.atan2(z, x))), 45) < 8) {
                int tall = 2 + (int)(this.hash(column, 2) * 4);
                column.fill(top + 1, top + tall, Blocks.STONE_BRICK_WALL.defaultBlockState());
                column.set(top + tall + 1, this.hash(column, 3) < 0.5 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : null);
            }

            if (d < 30.0) {
                Trees.paint(column, 7, (i, j) -> {
                    if (Noise.hash(this.salt, i, j, 4) > 0.35) {
                        return null;
                    }

                    int tx = i * 7 + 3;
                    int tz = j * 7 + 3;
                    double td = Math.sqrt((tx - this.cx) * (tx - this.cx) + (tz - this.cz) * (tz - this.cz));
                    if (td < 13.0 || td > 26.0) {
                        return null;
                    }

                    IslesShaper.Island island = terrain.isles().islandAt(tx, tz);
                    if (island == null) {
                        return null;
                    }

                    int base = terrain.isles().topAt(island, tx, tz) + 1;
                    return new Trees.Tree(tx, tz, base, 5 + (int)(Noise.hash(this.salt, i, j, 5) * 4), 0, Blocks.STRIPPED_BIRCH_LOG.defaultBlockState(),
                        Blocks.AIR.defaultBlockState(), Trees.Crown.DEAD);
                });
            }
        }
    }
}
