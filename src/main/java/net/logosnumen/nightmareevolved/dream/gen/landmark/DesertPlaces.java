package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The named places of the Nightmare Desert. */
public final class DesertPlaces {
    private DesertPlaces() {
    }

    /**
     * The Tomb of Ariel: a giant black pyramid, perfectly symmetrical, that hangs on the horizon like a mirage and
     * cannot be reached by day. At its heart, in a chamber reached by a long passage, lies a great block of black stone
     * breathing a ghostly darkness: a Seed of the Third Nightmare.
     */
    public static final class TombOfAriel extends Built {
        public static final int BASE = 66;
        public static final int HALF = 110;
        public static final int HEIGHT = 140;
        private static final int CHAMBER = BASE + 30;
        private final BlockState black = DreamBlocks.EBONY.get().defaultBlockState();

        public TombOfAriel() {
            super(Places.TOMB_OF_ARIEL.x(), Places.TOMB_OF_ARIEL.z(), HALF + 4, 0xA21EL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            int m = Math.max(Math.abs(x), Math.abs(z));
            if (m > HALF) {
                return;
            }

            // The steps of the pyramid: each course of blocks one in from the last.
            int top = BASE + (HALF - m) * HEIGHT / HALF;
            column.fill(BASE - 4, top, this.black);
            if (column.get(top + 1) != null) {
                column.clear(top + 1, top + 20);
            }

            // The passage from the north face to the chamber at the heart.
            if (Math.abs(x) <= 2 && z < -10) {
                column.clear(BASE + 1, BASE + 6);
            }

            if (Math.abs(x) <= 10 && Math.abs(z) <= 10) {
                column.clear(BASE + 1, CHAMBER);
                column.set(BASE, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                // The Seed: a great block of black stone, with the Seed itself at its foot facing the passage.
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    column.fill(BASE + 1, BASE + 3, DreamBlocks.SEED.get().seed(SeedBlock.Form.STONE, 3));
                }
            }
        }
    }

    /**
     * The Black Skull: a huge skull, entirely black, with three eyes, lying half-sunk in the sand of the battlefield where
     * the gods died. A citadel was raised in it and around it; its Gateway lies in the hall inside the skull.
     */
    public static final class BlackSkull extends Built {
        public static final int GROUND = 74;
        private static final double RX = 46.0;
        private static final double RY = 36.0;
        private static final double RZ = 52.0;
        private final BlockState black = Blocks.BLACKSTONE.defaultBlockState();
        private final BlockState wall = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();

        public BlackSkull() {
            super(Places.BLACK_SKULL.x(), Places.BLACK_SKULL.z(), 100, 0xB5CAL);
        }

        public static int gatewayX() {
            return Places.BLACK_SKULL.x();
        }

        public static int gatewayZ() {
            return Places.BLACK_SKULL.z() + 6;
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            double d = this.dist(column);
            // The citadel's outer wall, a ring around the skull.
            if (d > 82.0 && d < 86.0) {
                int ground = column.topSolid();
                column.fill(ground - 2, GROUND + 14, this.wall);
                if (Math.floorMod(x + z, 3) == 0) {
                    column.set(GROUND + 15, this.wall);
                }

                if (Math.abs(x) <= 3 && z > 0) {
                    column.clear(GROUND + 1, GROUND + 8);
                }

                return;
            }

            if (d < 82.0) {
                int ground = column.topSolid();
                column.clear(GROUND + 1, Math.max(GROUND + 1, ground));
                column.fill(Math.min(ground, GROUND), GROUND, DreamBlocks.PALE_SAND.get().defaultBlockState());
            }

            double nx = x / RX;
            double nz = z / RZ;
            double flat = 1.0 - nx * nx - nz * nz;
            if (flat <= 0.0) {
                return;
            }

            double half = RY * Math.sqrt(flat);
            double centre = GROUND + 8;
            column.fill((int)(centre - half), (int)(centre + half), this.black);
            double innerFlat = 1.0 - Math.pow(x / (RX - 5.0), 2) - Math.pow(z / (RZ - 5.0), 2);
            if (innerFlat > 0.0) {
                double inner = (RY - 5.0) * Math.sqrt(innerFlat);
                column.clear(GROUND + 1, (int)(centre + inner));
                column.set(GROUND, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
            }

            // Three eyes in the face, which looks south: two sockets, and a third above them.
            if (z > 0) {
                for (int e = 0; e < 3; e++) {
                    double ex = e == 2 ? 0.0 : (e == 0 ? -17.0 : 17.0);
                    double ey = e == 2 ? centre + 22.0 : centre + 8.0;
                    double er = e == 2 ? 7.0 : 10.0;
                    if (Math.abs(x - ex) < er) {
                        double dy = Math.sqrt(er * er - (x - ex) * (x - ex));
                        int lo = (int)(ey - dy);
                        int hi = (int)(ey + dy);
                        if (z > RZ * 0.55) {
                            column.clear(lo, hi);
                        }
                    }
                }

                // The way in, where the jaw lies buried in the sand.
                if (Math.abs(x) < 8 && z > RZ * 0.6) {
                    column.clear(GROUND + 1, GROUND + 8);
                }
            }

            if (column.x == gatewayX() && column.z == gatewayZ()) {
                column.set(GROUND + 1, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART));
            }
        }
    }

    /**
     * Five strangely shaped metal towers in a half circle, half buried: the fingers of a massive steel gauntlet lying in
     * the sand, where survivors once made their camp.
     */
    public static final class Gauntlet extends Built {
        public Gauntlet() {
            super(Places.BLACK_SKULL.x() + 340, Places.BLACK_SKULL.z() - 260, 40, 0x6A07L);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            int x = this.dx(column);
            int z = this.dz(column);
            for (int f = 0; f < 5; f++) {
                double a = Math.toRadians(200.0 + f * 35.0);
                double fx = Math.cos(a) * 22.0;
                double fz = Math.sin(a) * 22.0;
                double r = f == 0 ? 3.0 : 2.4;
                if ((x - fx) * (x - fx) + (z - fz) * (z - fz) <= r * r) {
                    int ground = column.topSolid();
                    int tall = 26 + f * 7 - (f == 4 ? 14 : 0);
                    column.fill(ground - 6, ground + tall, Blocks.IRON_BLOCK.defaultBlockState());
                    if (Math.floorMod(x + z, 4) == 0) {
                        // The joints of the fingers.
                        for (int y = ground + 8; y < ground + tall; y += 9) {
                            column.set(y, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                        }
                    }
                    column.set(ground + tall + 1, Blocks.IRON_TRAPDOOR.defaultBlockState());
                }
            }

            // The back of the hand, a low iron mound under the sand.
            double hand = (x * x) / 400.0 + ((z + 8) * (z + 8)) / 160.0;
            if (hand < 1.0) {
                int ground = column.topSolid();
                column.fill(ground - 4, ground - 1, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
    }

    /**
     * The one way to the Tomb of Ariel by day: a line of black standing stones from the Black Skull across the dunes. Off
     * it, the Tomb never comes any nearer.
     */
    public static final class MiragePath extends Built {
        private static final int STEP = 48;

        public MiragePath() {
            super((Places.BLACK_SKULL.x() + Places.TOMB_OF_ARIEL.x()) / 2, (Places.BLACK_SKULL.z() + Places.TOMB_OF_ARIEL.z()) / 2,
                (int)Places.BLACK_SKULL.distance(Places.TOMB_OF_ARIEL.x(), Places.TOMB_OF_ARIEL.z()) / 2 + 10, 0xFA7EL);
        }

        @Override
        public void paint(Column column, RealmTerrain terrain) {
            double ax = Places.BLACK_SKULL.x();
            double az = Places.BLACK_SKULL.z();
            double vx = Places.TOMB_OF_ARIEL.x() - ax;
            double vz = Places.TOMB_OF_ARIEL.z() - az;
            double length = Math.sqrt(vx * vx + vz * vz);
            double ux = vx / length;
            double uz = vz / length;
            double ox = column.x + 0.5 - ax;
            double oz = column.z + 0.5 - az;
            double along = ox * ux + oz * uz;
            double across = -ox * uz + oz * ux;
            if (along < 100.0 || along > length - TombOfAriel.HALF - 20 || Math.abs(Math.abs(across) - 7.0) > 1.0) {
                return;
            }

            double step = Math.floorMod((int)Math.round(along), STEP);
            if (step < 2.0) {
                int ground = column.topSolid();
                column.fill(ground - 2, ground + 5, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                column.set(ground + 6, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            }
        }
    }
}
