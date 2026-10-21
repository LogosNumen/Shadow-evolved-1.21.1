package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Hollow Mountains: a Death Zone of jagged peaks like a dragon's fangs, white with snow, with the mist of
 * nothingness pouring down into their ravines. The great peaks are hollow, and the hollows are full of darkness. Far
 * below them lies the Underworld: vast caverns, and in the deepest places a chasm that has no bottom at all.
 */
public final class MountainShaper implements Shaper {
    /** The Underworld's caverns reach up to about here. */
    public static final int UNDERWORLD_TOP = 26;
    private final Noise ridgesLarge;
    private final Noise ridgesMid;
    private final Noise ridgesFine;
    private final Noise snow;
    private final Noise hollow;
    private final Noise tunnels;
    private final Noise tunnelLevel;
    private final Noise caverns;
    private final Noise floor;
    private final Noise ceiling;
    private final Noise chasm;
    private final Noise strata;
    private final BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState();
    private final BlockState powder = Blocks.SNOW.defaultBlockState();

    public MountainShaper(long seed) {
        this.ridgesLarge = new Noise(Noise.mix(seed, "mountain.large"), 3);
        this.ridgesMid = new Noise(Noise.mix(seed, "mountain.mid"), 2);
        this.ridgesFine = new Noise(Noise.mix(seed, "mountain.fine"), 2);
        this.snow = new Noise(Noise.mix(seed, "mountain.snow"), 2);
        this.hollow = new Noise(Noise.mix(seed, "mountain.hollow"), 2);
        this.tunnels = new Noise(Noise.mix(seed, "mountain.tunnels"), 2);
        this.tunnelLevel = new Noise(Noise.mix(seed, "mountain.tunnel_level"), 1);
        this.caverns = new Noise(Noise.mix(seed, "underworld.caverns"), 2);
        this.floor = new Noise(Noise.mix(seed, "underworld.floor"), 2);
        this.ceiling = new Noise(Noise.mix(seed, "underworld.ceiling"), 2);
        this.chasm = new Noise(Noise.mix(seed, "underworld.chasm"), 2);
        this.strata = new Noise(Noise.mix(seed, "mountain.strata"), 1);
    }

    @Override
    public double ground(int x, int z, double edge) {
        double inside = Noise.smooth(edge / 520.0);
        double base = 98.0 + 72.0 * inside;
        double large = Math.pow(this.ridgesLarge.ridge(x / 420.0, z / 420.0), 2.8);
        double mid = Math.pow(this.ridgesMid.ridge(x / 150.0, z / 150.0), 3.0);
        // Fangs: sharp spires where the finest ridges cross, tallest on the great ridges.
        double fang = Math.max(0.0, this.ridgesFine.ridge(x / 38.0, z / 38.0) - 0.55) / 0.45;
        double peaks = 185.0 * large + 85.0 * mid * (0.35 + large) + 48.0 * fang * fang * (0.25 + large);
        return Math.min(372.0, base + inside * peaks);
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        Ground.stone(column, top);
        this.strata(column, top);
        int snowLine = 262 + (int)(14.0 * this.snow.at(x / 80.0, z / 80.0));
        if (top >= snowLine) {
            column.fill(top - 2, top, this.snowBlock);
        } else if (top >= snowLine - 30 && this.snow.at(x / 11.0, z / 11.0) > (snowLine - top) / 30.0 - 0.5) {
            column.set(top + 1, this.powder);
        }

        // The great peaks are hollow: a cavity under the shell, full of darkness.
        if (top > 248) {
            double mask = this.hollow.at(x / 95.0, z / 95.0);
            if (mask > 0.18) {
                int shell = 24 + (int)(10.0 * (mask - 0.18));
                int floorY = 128 + (int)(12.0 * this.hollow.at(x / 31.0, 3.0, z / 31.0));
                int roof = top - shell;
                if (roof > floorY + 6) {
                    column.clear(floorY, roof);
                    column.set(floorY - 1, Blocks.BLACKSTONE.defaultBlockState());
                }
            }
        }

        // Old tunnels winding through the rock at a few depths.
        double tunnel = Math.abs(this.tunnels.at(x / 74.0, z / 74.0));
        if (tunnel < 0.024) {
            int level = 96 + (int)(42.0 * this.tunnelLevel.at(x / 310.0, z / 310.0));
            if (level + 5 < top - 4) {
                column.clear(level, level + 4);
            }
        }

        this.underworld(column);
    }

    /** Streaks of other stone through the mountains. */
    private void strata(Column column, int top) {
        double s = this.strata.at(column.x / 60.0, column.z / 60.0);
        for (int y = 8; y <= top - 3; y += 1) {
            double band = Math.sin(y * 0.21 + s * 3.0);
            if (band > 0.82) {
                column.set(y, Blocks.ANDESITE.defaultBlockState());
            } else if (band < -0.9) {
                column.set(y, Blocks.TUFF.defaultBlockState());
            }
        }
    }

    /**
     * The Underworld's halls: caverns between a floor and a ceiling that wander with the noise, and where the chasm
     * runs, nothing at all below them, not even the bottom of the world.
     */
    private void underworld(Column column) {
        int x = column.x;
        int z = column.z;
        double mask = this.caverns.at(x / 230.0, z / 230.0);
        if (mask < -0.22) {
            return;
        }

        int floorY = -44 + (int)(9.0 * this.floor.at(x / 70.0, z / 70.0));
        int roof = 8 + (int)(16.0 * this.ceiling.at(x / 55.0, z / 55.0)) - (int)(18.0 * Math.max(0.0, -mask - 0.1));
        roof = Math.min(roof, UNDERWORLD_TOP - 2);
        if (roof <= floorY + 4) {
            return;
        }

        column.clear(floorY, roof);
        column.set(floorY - 1, Blocks.BLACKSTONE.defaultBlockState());
        if (Math.abs(this.chasm.at(x / 410.0, z / 410.0)) < 0.022) {
            // The chasm down to the Abyss: no floor, and no bottom to the world either.
            column.clear(Column.MIN_Y, floorY - 1);
        }
    }
}
