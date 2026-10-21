package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.shape.ShoreShaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Crimson Spire, at the heart of the labyrinth and of the Forgotten Shore: an inconceivably tall tower, like a
 * bloodied sword thrust into the heavens, standing on an island in a moat of black water. Crimson coral streams from its
 * walls and spreads out from its base to devour the land.
 * <ul>
 * <li>At its foot, seven severed heads guard the seven locks of its gate.</li>
 * <li>At the centre of its ground floor, a gargantuan well, where the Dark Sea is imprisoned once the seal is closed.</li>
 * <li>Inside, there are no stairs: twisted pillars of coral wind upward, steep and narrowing as they climb, past stone
 * galleries on the walls.</li>
 * <li>Almost halfway up, a vast balcony juts from the wall, its broken marble pillars streaked with coral. It leads to a
 * circular dais with a wide iron ring sunk into its stone, ringed by shining runes, where no coral grows: the Gateway.</li>
 * <li>At the top burns the artificial sun.</li>
 * </ul>
 */
public final class CrimsonSpire extends Built {
    public static final int BASE = ShoreShaper.BASIN_FLOOR + 3;
    public static final int TOP = 360;
    public static final int BALCONY = 220;
    public static final int BASE_RADIUS = 34;
    public static final int TOP_RADIUS = 10;
    private static final int SHELL = 4;
    private static final int PITCH = 26;
    private static final Noise STREAKS = new Noise(0x5B1D_C0A7L, 2);
    private final BlockState wall = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private final BlockState dark = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private final BlockState coral = DreamBlocks.CRIMSON_CORAL.get().defaultBlockState();
    private final BlockState marble = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private final BlockState pillar = Blocks.QUARTZ_PILLAR.defaultBlockState();

    public CrimsonSpire() {
        super(Places.CRIMSON_SPIRE.x(), Places.CRIMSON_SPIRE.z(), 90, 0x5B1DL);
    }

    /** The Gateway's dais, on the balcony. */
    public static int gatewayX() {
        return Places.CRIMSON_SPIRE.x();
    }

    public static int gatewayZ() {
        return Places.CRIMSON_SPIRE.z() + (int)Math.round(radiusAt(BALCONY)) + 12;
    }

    /** The seven-starred lock, set in the wall beside the gate. */
    public static int lockX() {
        return Places.CRIMSON_SPIRE.x() + 7;
    }

    public static int lockZ() {
        return Places.CRIMSON_SPIRE.z() + (int)Math.floor(radiusAt(BASE + 6)) - 1;
    }

    /** The tower's outer radius at a height: wide at the foot, narrowing to the top. */
    public static double radiusAt(double y) {
        double t = Mth.clamp((y - BASE) / (TOP - BASE), 0.0, 1.0);
        return Mth.lerp(t, BASE_RADIUS, TOP_RADIUS);
    }

    /** The height at which the tower's outer face reaches out to a distance (an octagonal measure). */
    private static double heightAt(double d) {
        return BASE + (BASE_RADIUS - d) / (BASE_RADIUS - TOP_RADIUS) * (TOP - BASE);
    }

    /** An octagonal distance, so the tower has faces like a blade's. */
    private static double octagon(double dx, double dz) {
        double ax = Math.abs(dx);
        double az = Math.abs(dz);
        return Math.max(Math.max(ax, az), (ax + az) / 1.35);
    }

    @Override
    public void paint(Column column, RealmTerrain terrain) {
        double dx = column.x + 0.5 - this.cx;
        double dz = column.z + 0.5 - this.cz;
        double d = octagon(dx, dz);
        double round = Math.sqrt(dx * dx + dz * dz);
        this.apron(column, round, d);
        if (d <= BASE_RADIUS) {
            this.tower(column, dx, dz, d);
        }

        this.balcony(column, dx, dz);
        this.locks(column, dx, dz);
        this.sun(column, round);
    }

    /** Coral spreading out from the Spire's foot, thick near the tower and thinning toward the moat. */
    private void apron(Column column, double round, double d) {
        if (round > 78.0 || d < BASE_RADIUS - 1) {
            return;
        }

        int ground = column.topSolid();
        double t = (round - BASE_RADIUS) / (78.0 - BASE_RADIUS);
        int rise = (int)Math.round((1.0 - t) * (5.0 + 6.0 * STREAKS.at(column.x / 6.0, column.z / 6.0)));
        if (rise > 0 && ground >= BASE - 3) {
            column.fill(ground + 1, ground + rise, this.coral);
            if (this.hash(column, 1) < 0.3) {
                column.set(ground + rise + 1, DreamBlocks.CRIMSON_CORAL_BLADES.get().defaultBlockState());
            }
        }
    }

    private void tower(Column column, double dx, double dz, double d) {
        int top = (int)Math.min(TOP, Math.floor(heightAt(d)));
        int innerTop = (int)Math.min(TOP - 4, Math.floor(heightAt(d + SHELL)));
        double angle = Math.atan2(dz, dx);
        boolean streak = STREAKS.at(angle * 6.0, column.x / 30.0 + column.z / 30.0) > 0.25;
        // The shell, from the foot to where it meets this column's distance.
        column.fill(BASE - 6, top, this.wall);
        if ((column.x + column.z) % 3 == 0) {
            column.set(top, this.dark);
        }

        column.clear(BASE + 1, innerTop);
        if (d >= TOP_RADIUS && streak) {
            // Coral pouring down the outside like blood.
            column.fill(Math.max(innerTop + 1, top - 2 - (int)(this.hash(column, 2) * 12)), top, this.coral);
        }

        // The floor of the ground hall, and the well in its middle.
        column.set(BASE, this.dark);
        double round = Math.sqrt(dx * dx + dz * dz);
        if (round < 11.0) {
            column.clear(BASE - 40, BASE);
            if (round > 9.0) {
                column.fill(BASE - 40, BASE + 1, this.dark);
            } else {
                column.set(BASE - 41, Blocks.BLACK_CONCRETE.defaultBlockState());
            }
        }

        if (d > SHELL && d < radiusAt(BASE) - SHELL) {
            this.ramps(column, angle, d, innerTop);
            this.galleries(column, d);
        }

        // Narrow slits of windows up the faces.
        if (Math.floorMod((int)Math.round(angle * 16.0 / Math.PI), 4) == 0) {
            for (int y = BASE + 20; y < top - 4; y += 30) {
                if (y > innerTop) {
                    column.clear(y, Math.min(top - 1, y + 3));
                }
            }
        }
    }

    /**
     * The twisted coral pillars that wind up inside the tower: two of them, opposite each other, each a ramp a few
     * blocks wide rising {@value #PITCH} blocks a turn, narrowing as it nears the top.
     */
    private void ramps(Column column, double angle, double d, int innerTop) {
        for (int strand = 0; strand < 2; strand++) {
            double turn = (angle + Math.PI + strand * Math.PI) / (2.0 * Math.PI);
            for (int k = 0; k < (TOP - BASE) / PITCH + 1; k++) {
                double y = BASE + 3 + (turn % 1.0 + k) * PITCH;
                if (y > innerTop - 2) {
                    break;
                }

                double wallAt = radiusAt(y) - SHELL;
                double width = Mth.lerp((y - BASE) / (TOP - BASE), 7.0, 3.5);
                if (d > wallAt - width && d < wallAt + 0.5) {
                    int yi = (int)Math.floor(y);
                    column.fill(yi - 1, yi, this.coral);
                }
            }
        }
    }

    /** Stone galleries ringing the inside of the wall every so often. */
    private void galleries(Column column, double d) {
        for (int y = BASE + 40; y < TOP - 30; y += 46) {
            double wallAt = radiusAt(y) - SHELL;
            if (d > wallAt - 3.0 && d < wallAt + 0.5) {
                column.set(y, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        }
    }

    /**
     * The balcony on the southern face, almost halfway up: broken marble pillars streaked with coral, and at its end the
     * dais of the Gateway, clean of coral, with its iron ring and shining runes.
     */
    private void balcony(Column column, double dx, double dz) {
        double face = radiusAt(BALCONY);
        if (Math.abs(dx) > 10.5 || dz < face - SHELL - 1 || dz > face + 20.0) {
            return;
        }

        int x = column.x - this.cx;
        // A doorway through the wall onto the balcony.
        if (dz < face + 1.0 && Math.abs(dx) <= 4.0) {
            column.clear(BALCONY + 1, BALCONY + 8);
        }

        if (dz < face - 1.0) {
            return;
        }

        column.fill(BALCONY - 3, BALCONY, this.marble);
        column.set(BALCONY - 3, this.wall);
        if (dz < face + 5.0) {
            // Corbels of dark stone holding the balcony out from the wall.
            column.fill(BALCONY - 7, BALCONY - 4, this.dark);
        }
        double gx = column.x + 0.5 - gatewayX();
        double gz = column.z + 0.5 - gatewayZ();
        double ring = Math.sqrt(gx * gx + gz * gz);
        if (ring <= 5.5) {
            column.set(BALCONY, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            if (column.x == gatewayX() && column.z == gatewayZ()) {
                column.set(BALCONY, DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.RING));
            } else if (ring > 4.3) {
                column.set(BALCONY, DreamBlocks.RUNE_STONE.get().defaultBlockState());
            } else if (ring > 1.6 && ring < 2.8) {
                column.set(BALCONY, Blocks.IRON_BLOCK.defaultBlockState());
            }

            return;
        }

        // Broken marble pillars down both sides, coral climbing them.
        if (Math.abs(x) == 9 && Math.floorMod(column.z - this.cz, 5) == 0) {
            int tall = 3 + (int)(this.hash(column, 3) * 8);
            column.fill(BALCONY + 1, BALCONY + tall, this.pillar);
            if (this.hash(column, 4) < 0.6) {
                column.fill(BALCONY + 1, BALCONY + 1 + (int)(tall * this.hash(column, 5)), this.coral);
            }
        } else if (Math.abs(x) == 10) {
            column.set(BALCONY + 1, Blocks.QUARTZ_SLAB.defaultBlockState());
        } else if (this.hash(column, 6) < 0.12) {
            column.set(BALCONY + 1, this.coral);
        }
    }

    /** The seven locks of the gate at the Spire's foot, and the seven severed heads that guard them. */
    private void locks(Column column, double dx, double dz) {
        double face = radiusAt(BASE + 6);
        // The gate: a tall opening in the southern face, with the seven-starred lock beside it.
        if (Math.abs(dx) <= 5.0 && dz > face - SHELL - 2 && dz < face + 1) {
            column.clear(BASE + 1, BASE + 16);
            column.set(BASE, this.dark);
        }

        if (column.x == lockX() && column.z == lockZ()) {
            column.set(BASE + 4, DreamBlocks.SEAL.get().defaultBlockState());
        }

        // Seven heads on plinths in an arc before the gate.
        for (int i = 0; i < 7; i++) {
            double a = Math.toRadians(90.0 + (i - 3) * 16.0);
            double hx = Math.cos(a) * (face + 12.0);
            double hz = Math.sin(a) * (face + 12.0);
            double ox = dx - hx;
            double oz = dz - hz;
            if (Math.abs(ox) <= 1.5 && Math.abs(oz) <= 1.5) {
                int ground = column.solidBelow(BASE + 12);
                column.fill(ground + 1, ground + 2, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                column.fill(ground + 3, ground + 5, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            }
        }
    }

    /** The artificial sun burning over the top of the Spire, fed by the souls of all that bled on the coral. */
    private void sun(Column column, double round) {
        if (round > 7.0) {
            return;
        }

        int centre = TOP + 14;
        double half = Math.sqrt(49.0 - round * round);
        column.fill((int)(centre - half), (int)(centre + half), Blocks.SHROOMLIGHT.defaultBlockState());
        if (round < 2.0) {
            column.fill(TOP + 1, (int)(centre - half), Blocks.CHAIN.defaultBlockState());
        }
    }
}
