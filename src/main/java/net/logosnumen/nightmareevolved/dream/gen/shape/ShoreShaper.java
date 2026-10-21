package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Forgotten Shore: black earth under a grey sky, and in the great basin around the Crimson Spire an endless
 * labyrinth of crimson coral, walls of blades with corridors twisting between them, some running into tunnels through
 * coral mounds. The coral grows taller toward the Spire, which stands on an island in a moat of black water. West of the
 * labyrinth the coral dies grey and crumbles into the Sea of Ash; at the northern edge the land rises in tall barrows.
 * The Dark Sea that floods the basin every night is not water placed here: it is the Shore's law (see the tide).
 */
public final class ShoreShaper implements Shaper {
    /** The floor of the basin, where the Dark Sea's day level leaves it just dry. */
    public static final int BASIN_FLOOR = 64;
    /** The crest of the basin's rim. */
    public static final int RIM = 97;
    private final long seed;
    private final Noise rolling;
    private final Noise detail;
    private final Noise mazeA;
    private final Noise mazeB;
    private final Noise tunnel;
    private final Noise height;
    private final Noise jag;
    private final BlockState earth;
    private final BlockState coral;
    private final BlockState blades;
    private final BlockState ash;
    private final BlockState[] deadCoral = {
        Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState(), Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(),
        Blocks.DEAD_FIRE_CORAL_BLOCK.defaultBlockState(), Blocks.DEAD_HORN_CORAL_BLOCK.defaultBlockState()
    };

    public ShoreShaper(long seed) {
        this.seed = seed;
        this.rolling = new Noise(Noise.mix(seed, "shore.rolling"), 3);
        this.detail = new Noise(Noise.mix(seed, "shore.detail"), 2);
        this.mazeA = new Noise(Noise.mix(seed, "shore.maze.a"), 2);
        this.mazeB = new Noise(Noise.mix(seed, "shore.maze.b"), 2);
        this.tunnel = new Noise(Noise.mix(seed, "shore.tunnel"), 1);
        this.height = new Noise(Noise.mix(seed, "shore.height"), 2);
        this.jag = new Noise(Noise.mix(seed, "shore.jag"), 1);
        this.earth = DreamBlocks.BLACK_EARTH.get().defaultBlockState();
        this.coral = DreamBlocks.CRIMSON_CORAL.get().defaultBlockState();
        this.blades = DreamBlocks.CRIMSON_CORAL_BLADES.get().defaultBlockState();
        this.ash = DreamBlocks.ASH.get().defaultBlockState();
    }

    @Override
    public double ground(int x, int z, double edge) {
        double basin = RealmMap.Shore.basin(x, z);
        double plain = 82.0 + 6.0 * this.rolling.at(x / 260.0, z / 260.0) + 2.0 * this.detail.at(x / 50.0, z / 50.0);
        double floor = BASIN_FLOOR + 2.5 * this.rolling.at(x / 170.0, z / 170.0) + 1.0 * this.detail.at(x / 40.0, z / 40.0);
        double spire = Places.CRIMSON_SPIRE.distance(x, z);
        if (spire < 230.0) {
            // The Spire's island, and the moat of black water around it.
            double island = BASIN_FLOOR + 3.0;
            double moat = 50.0 + this.detail.at(x / 20.0, z / 20.0);
            if (spire < 78.0) {
                floor = island;
            } else if (spire < 96.0) {
                floor = Mth.lerp(Noise.smooth((spire - 78.0) / 18.0), island, moat);
            } else if (spire < 170.0) {
                floor = moat;
            } else {
                floor = Mth.lerp(Noise.smooth((spire - 170.0) / 60.0), moat, floor);
            }
        }

        double rim = RIM + 3.0 * this.detail.at(x / 90.0, z / 90.0);
        if (basin < 0.9) {
            return floor;
        } else if (basin < 1.0) {
            return Mth.lerp(Noise.smooth((basin - 0.9) / 0.1), floor, rim);
        } else if (basin < 1.16) {
            return Mth.lerp(Noise.smooth((basin - 1.0) / 0.16), rim, plain);
        }

        return plain;
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        Region part = RealmMap.refine(Region.FORGOTTEN_SHORE, x, top, z);
        double basin = RealmMap.Shore.basin(x, z);
        if (part == Region.SEA_OF_ASH) {
            this.ashen(column, top, basin);
            return;
        }

        Ground.lay(column, top, this.earth, this.earth, 4);
        if (part == Region.LAND_OF_BARROWS) {
            this.barrows(column, top);
            return;
        }

        if (basin < 0.97 && Places.CRIMSON_SPIRE.distance(x, z) > 96.0) {
            double fade = Noise.smooth((0.97 - basin) / 0.06) * Noise.smooth(blend.weight(Region.FORGOTTEN_SHORE) * 2.0 - 0.6);
            this.labyrinth(column, top, fade, this.coral, true);
        } else if (Noise.hash(this.seed, x, z, 11) < 0.012) {
            column.set(top + 1, this.blades);
        }
    }

    /**
     * Coral walls on the basin floor. Two webs of thin ridges cross each other into corridors and dead ends; where a
     * third noise swells, the coral grows into solid mounds with tunnels running through them.
     */
    private void labyrinth(Column column, int top, double fade, BlockState material, boolean living) {
        int x = column.x;
        int z = column.z;
        double a = Math.abs(this.mazeA.at(x / 46.0, z / 46.0));
        double b = Math.abs(this.mazeB.at(x / 71.0, z / 71.0)) * 0.85;
        double wall = Math.min(a, b);
        double mound = this.height.at(x / 210.0, 5.0, z / 210.0);
        boolean solid = wall < 0.042 || mound > 0.5;
        if (!solid || fade <= 0.05) {
            if (living && Noise.hash(this.seed, x, z, 3) < 0.035) {
                column.set(top + 1, this.blades);
            }

            return;
        }

        double spire = Places.CRIMSON_SPIRE.distance(x, z);
        double tall = 10.0 + 13.0 * this.height.unit(x / 140.0, z / 140.0) + 14.0 * Math.max(0.0, 1.0 - spire / 900.0);
        tall += 6.0 * this.jag.at(x / 4.0, z / 4.0);
        if (wall < 0.042) {
            // Thin walls are blade-edged: tallest at the ridge line.
            tall *= 1.0 - 0.45 * (wall / 0.042);
        }

        int height = Math.max(2, (int)Math.round(tall * fade));
        column.fill(top + 1, top + height, material);
        if (mound > 0.5 && Math.abs(this.tunnel.at(x / 26.0, z / 26.0)) < 0.07 && height > 6) {
            column.clear(top + 1, top + 4);
        }

        if (living && Noise.hash(this.seed, x, z, 5) < 0.3) {
            column.set(top + height + 1, this.blades);
        }
    }

    /** West of the labyrinth: coral turned grey and brittle, then nothing but ash. */
    private void ashen(Column column, int top, double basin) {
        int x = column.x;
        double west = Places.CRIMSON_SPIRE.x() - x;
        double t = Mth.clamp((west - 1250.0) / 380.0, 0.0, 1.0);
        Ground.stone(column, top);
        column.fill(top - 3, top, t > 0.6 ? this.ash : this.earth);
        if (t < 0.85 && basin < 0.97) {
            BlockState dead = this.deadCoral[(int)(Noise.hash(this.seed, x >> 3, column.z >> 3, 7) * this.deadCoral.length)];
            this.labyrinth(column, top, (1.0 - t / 0.85) * Noise.smooth((0.97 - basin) / 0.06), dead, false);
        }

        if (t > 0.5) {
            int layers = (int)Math.round(2.0 + 2.5 * this.detail.at(x / 13.0, column.z / 13.0));
            if (layers > 0 && column.get(top + 1) == null) {
                column.set(top + 1, DreamBlocks.ASH_LAYER.get().depth(layers));
            }
        }
    }

    /** The tall barrows at the Shore's northern edge, where its dead sky gives way to one with a sun. */
    private void barrows(Column column, int top) {
        int cell = 64;
        int cx = Math.floorDiv(column.x, cell);
        int cz = Math.floorDiv(column.z, cell);
        double best = 0.0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int i = cx + dx;
                int j = cz + dz;
                if (Noise.hash(this.seed, i, j, 21) > 0.62) {
                    continue;
                }

                double mx = i * cell + cell * (0.25 + 0.5 * Noise.hash(this.seed, i, j, 22));
                double mz = j * cell + cell * (0.25 + 0.5 * Noise.hash(this.seed, i, j, 23));
                double angle = Noise.hash(this.seed, i, j, 24) * Math.PI;
                double rx = 15.0 + 9.0 * Noise.hash(this.seed, i, j, 25);
                double rz = 8.0 + 5.0 * Noise.hash(this.seed, i, j, 26);
                double ox = column.x - mx;
                double oz = column.z - mz;
                double u = (ox * Math.cos(angle) + oz * Math.sin(angle)) / rx;
                double v = (-ox * Math.sin(angle) + oz * Math.cos(angle)) / rz;
                double d = u * u + v * v;
                if (d < 1.0) {
                    double tall = 9.0 + 9.0 * Noise.hash(this.seed, i, j, 27);
                    best = Math.max(best, tall * Math.pow(1.0 - d, 0.6));
                }
            }
        }

        if (best > 0.5) {
            column.fill(top + 1, top + (int)Math.round(best), this.earth);
        } else if (Noise.hash(this.seed, column.x, column.z, 28) < 0.01) {
            column.set(top + 1, Blocks.DEAD_BUSH.defaultBlockState());
        }
    }
}
