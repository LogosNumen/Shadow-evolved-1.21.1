package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Landmark;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A landmark built around a centre, with the helpers its painting needs: boxes, rings and discs measured from that
 * centre, a steady hash for weathering, and its square bounds.
 */
public abstract class Built implements Landmark {
    protected final int cx;
    protected final int cz;
    protected final int reach;
    protected final long salt;

    protected Built(int cx, int cz, int reach, long salt) {
        this.cx = cx;
        this.cz = cz;
        this.reach = reach;
        this.salt = salt;
    }

    @Override
    public int minX() {
        return this.cx - this.reach;
    }

    @Override
    public int maxX() {
        return this.cx + this.reach;
    }

    @Override
    public int minZ() {
        return this.cz - this.reach;
    }

    @Override
    public int maxZ() {
        return this.cz + this.reach;
    }

    /** A steady random number from 0 to 1 for a column and a salt. */
    protected double hash(Column column, int salt) {
        return Noise.hash(this.salt, column.x, column.z, salt);
    }

    protected double hash(int x, int y, int z) {
        return Noise.hash(this.salt + y * 0x9E37L, x, z, 7);
    }

    /** Distance from the centre on the ground plane. */
    protected double dist(Column column) {
        double dx = column.x + 0.5 - this.cx;
        double dz = column.z + 0.5 - this.cz;
        return Math.sqrt(dx * dx + dz * dz);
    }

    protected int dx(Column column) {
        return column.x - this.cx;
    }

    protected int dz(Column column) {
        return column.z - this.cz;
    }

    /** Whether a column lies in a box given relative to the centre (inclusive). */
    protected boolean in(Column column, int x0, int z0, int x1, int z1) {
        int x = column.x - this.cx;
        int z = column.z - this.cz;
        return x >= Math.min(x0, x1) && x <= Math.max(x0, x1) && z >= Math.min(z0, z1) && z <= Math.max(z0, z1);
    }

    /** Fills a span of a column if it lies in a box given relative to the centre. */
    protected void box(Column column, int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
        if (this.in(column, x0, z0, x1, z1)) {
            column.fill(y0, y1, state);
        }
    }

    /** The walls of a box (relative to the centre), one block thick, from one height to another. */
    protected boolean walls(Column column, int x0, int z0, int x1, int z1) {
        if (!this.in(column, x0, z0, x1, z1)) {
            return false;
        }

        int x = column.x - this.cx;
        int z = column.z - this.cz;
        return x == Math.min(x0, x1) || x == Math.max(x0, x1) || z == Math.min(z0, z1) || z == Math.max(z0, z1);
    }

    /** A ball of blocks: the span of it the column passes through. */
    protected static void ball(Column column, double cx, double cy, double cz, double r, BlockState state, boolean onlyEmpty) {
        double dx = column.x + 0.5 - cx;
        double dz = column.z + 0.5 - cz;
        double flat = r * r - dx * dx - dz * dz;
        if (flat <= 0.0) {
            return;
        }

        double half = Math.sqrt(flat);
        int lo = (int)Math.floor(cy - half);
        int hi = (int)Math.floor(cy + half);
        if (onlyEmpty) {
            for (int y = lo; y <= hi; y++) {
                column.setIfEmpty(y, state);
            }
        } else {
            column.fill(lo, hi, state);
        }
    }

    /** A straight beam of blocks between two points, with a radius: the span of it the column passes through. */
    protected static void tube(Column column, double ax, double ay, double az, double bx, double by, double bz, double r, BlockState state) {
        double px = column.x + 0.5;
        double pz = column.z + 0.5;
        if (px < Math.min(ax, bx) - r - 1 || px > Math.max(ax, bx) + r + 1 || pz < Math.min(az, bz) - r - 1 || pz > Math.max(az, bz) + r + 1) {
            return;
        }

        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double len2 = dx * dx + dy * dy + dz * dz;
        int lo = (int)Math.floor(Math.min(ay, by) - r - 1);
        int hi = (int)Math.ceil(Math.max(ay, by) + r + 1);
        for (int y = lo; y <= hi; y++) {
            double py = y + 0.5;
            double t = len2 < 1.0E-6 ? 0.0 : Math.max(0.0, Math.min(1.0, ((px - ax) * dx + (py - ay) * dy + (pz - az) * dz) / len2));
            double cx = ax + dx * t - px;
            double cy = ay + dy * t - py;
            double cz = az + dz * t - pz;
            if (cx * cx + cy * cy + cz * cz <= r * r) {
                column.set(y, state);
            }
        }
    }

    /** Solid ground under a building, down to the land, so nothing floats. */
    protected static void footing(Column column, int y, BlockState state) {
        int ground = column.solidBelow(y - 1);
        if (ground < y - 1) {
            column.fill(Math.max(ground + 1, y - 40), y - 1, state);
        }
    }
}
