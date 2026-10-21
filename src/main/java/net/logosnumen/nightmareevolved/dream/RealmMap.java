package net.logosnumen.nightmareevolved.dream;

import net.logosnumen.nightmareevolved.dream.gen.Godgrave;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * Where each region of the Dream Realm lies. The heart of the realm is laid out as the novel places its regions, on a
 * grid of {@link #CELL}-block squares (x runs east, z south):
 *
 * <pre>
 *            -3    -2    -1     0     1     2     3 ...
 *   -5 ...   Burned Forest, all the way north
 *   -4, -3   HM    Forgotten Shore     Nightmare Desert ...
 *   -2       Hollow Mountains, all the way east and west
 *   -1       HM    MP    GG    GH    Chained Isles ...
 *    0       HM    RH    GG    BS    Bastion     CI ...
 *    1       HM    WG    GG    BS    Bastion     CI ...
 *    2 ...   Stormsea, all the way south
 * </pre>
 *
 * (MP the Moonriver Plain, GG Godgrave, GH the Glass Hell, RH Ravenheart, BS the Bone Sea, WG the Weeping Goddess.) The
 * Shore lies north of the Hollow Mountains with the Desert to its east; south of the mountains run the Moonriver Plain,
 * Godgrave, the Glass Hell and the Chained Isles; Ravenheart is south of the plain, Bastion south of the Isles, and the
 * Stormsea south of both domains. Beyond the named lands, the vast ones go on: the Burned Forest north, the Desert and
 * the Isles east, the mountains west, the Stormsea south.
 * <p>
 * Borders wander (the grid is bent by a slow noise) and blend over {@link #BLEND} blocks. The layout is the same in every
 * world; only the land's smaller shapes come from the world's seed.
 */
public final class RealmMap {
    public static final int CELL = 2048;
    public static final int BLEND = 64;
    private static final double WARP = 180.0;
    private static final ImprovedNoise WARP_X = new ImprovedNoise(new XoroshiroRandomSource(0x5EA0F5A1L));
    private static final ImprovedNoise WARP_Z = new ImprovedNoise(new XoroshiroRandomSource(0x0DD5EA11L));
    private static final ImprovedNoise DETAIL = new ImprovedNoise(new XoroshiroRandomSource(0xA5BE5L));

    private RealmMap() {
    }

    /** The region a grid cell holds. */
    public static Region cell(int c, int r) {
        if (r <= -5) {
            return Region.BURNED_FOREST;
        }

        if (r <= -3) {
            return c <= -3 ? Region.HOLLOW_MOUNTAINS : c <= 0 ? Region.FORGOTTEN_SHORE : Region.NIGHTMARE_DESERT;
        }

        if (r == -2) {
            return Region.HOLLOW_MOUNTAINS;
        }

        if (r >= 2) {
            return Region.STORMSEA;
        }

        if (c <= -3) {
            return Region.HOLLOW_MOUNTAINS;
        }

        return switch (c) {
            case -2 -> r == -1 ? Region.MOONRIVER_PLAIN : r == 0 ? Region.RAVENHEART : Region.WEEPING_GODDESS;
            case -1 -> Region.GODGRAVE;
            case 0 -> r == -1 ? Region.GLASS_HELL : Region.BONE_SEA;
            case 1, 2 -> r == -1 ? Region.CHAINED_ISLES : Region.BASTION;
            default -> Region.CHAINED_ISLES;
        };
    }

    public static double warpX(double x, double z) {
        return x + WARP * (WARP_X.noise(x / 900.0, 0.0, z / 900.0) + 0.35 * WARP_X.noise(x / 230.0, 7.0, z / 230.0));
    }

    public static double warpZ(double x, double z) {
        return z + WARP * (WARP_Z.noise(x / 900.0, 0.0, z / 900.0) + 0.35 * WARP_Z.noise(x / 230.0, 7.0, z / 230.0));
    }

    /** The whole region at a place (not its parts: see {@link #part}). */
    public static Region regionAt(double x, double z) {
        double wx = warpX(x, z);
        double wz = warpZ(x, z);
        return cell(Mth.floor(wx / CELL), Mth.floor(wz / CELL));
    }

    /** The region at a place, down to its parts: the Sea of Ash, the Land of Barrows, the Underworld, Godgrave's hollows. */
    public static Region part(int x, int y, int z) {
        return refine(regionAt(x, z), x, y, z);
    }

    public static Region refine(Region region, int x, int y, int z) {
        return switch (region) {
            case FORGOTTEN_SHORE -> {
                if (z < Places.SHORE_NORTH + 300 + 60 * DETAIL.noise(x / 160.0, 0.0, 1.0)) {
                    yield Region.LAND_OF_BARROWS;
                }

                yield Shore.seaOfAsh(x, z) ? Region.SEA_OF_ASH : Region.FORGOTTEN_SHORE;
            }
            case HOLLOW_MOUNTAINS -> y < 28 ? Region.UNDERWORLD : region;
            case GODGRAVE -> Godgrave.inHollow(x, y, z) ? Region.GODGRAVE_HOLLOW : region;
            default -> region;
        };
    }

    /**
     * How strongly each region shapes the land at a place: one region well inside its borders, up to four where
     * borders meet. Weights sum to one.
     */
    public static Blend blend(double x, double z) {
        Blend blend = new Blend();
        double wx = warpX(x, z);
        double wz = warpZ(x, z);
        int c = Mth.floor(wx / CELL);
        int r = Mth.floor(wz / CELL);
        double u = wx - (double)c * CELL;
        double v = wz - (double)r * CELL;
        int nc = u < CELL / 2.0 ? c - 1 : c + 1;
        int nr = v < CELL / 2.0 ? r - 1 : r + 1;
        double eu = Math.min(u, CELL - u);
        double ev = Math.min(v, CELL - v);
        double a = side(eu);
        double b = side(ev);
        blend.add(cell(c, r), a * b);
        blend.add(cell(nc, r), (1.0 - a) * b);
        blend.add(cell(c, nr), a * (1.0 - b));
        blend.add(cell(nc, nr), (1.0 - a) * (1.0 - b));
        blend.edge = edge(c, r, u, v);
        return blend;
    }

    /** How much a cell holds its own at a distance from its edge: half at the edge, all of it {@link #BLEND} inside. */
    private static double side(double fromEdge) {
        double t = Mth.clamp((fromEdge + BLEND) / (2.0 * BLEND), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    /** How far a place lies inside its region: the distance to the nearest edge shared with a different region. */
    private static double edge(int c, int r, double u, double v) {
        Region here = cell(c, r);
        double best = CELL;
        for (int dc = -1; dc <= 1; dc++) {
            for (int dr = -1; dr <= 1; dr++) {
                if ((dc != 0 || dr != 0) && cell(c + dc, r + dr) != here) {
                    double du = dc < 0 ? u : dc > 0 ? CELL - u : 0.0;
                    double dv = dr < 0 ? v : dr > 0 ? CELL - v : 0.0;
                    best = Math.min(best, Math.sqrt(du * du + dv * dv));
                }
            }
        }

        return best;
    }

    /** The regions shaping a place, and how strongly. */
    public static final class Blend {
        public final Region[] regions = new Region[4];
        public final double[] weights = new double[4];
        public int count;
        /** How far the place lies inside its own cell's region (blocks). */
        public double edge;

        void add(Region region, double weight) {
            if (weight <= 1.0E-6) {
                return;
            }

            for (int i = 0; i < this.count; i++) {
                if (this.regions[i] == region) {
                    this.weights[i] += weight;
                    return;
                }
            }

            this.regions[this.count] = region;
            this.weights[this.count] = weight;
            this.count++;
        }

        public double weight(Region region) {
            for (int i = 0; i < this.count; i++) {
                if (this.regions[i] == region) {
                    return this.weights[i];
                }
            }

            return 0.0;
        }

        /** The region with the most say here. */
        public Region dominant() {
            int best = 0;
            for (int i = 1; i < this.count; i++) {
                if (this.weights[i] > this.weights[best]) {
                    best = i;
                }
            }

            return this.regions[best];
        }
    }

    /** The Forgotten Shore's great basin, where the labyrinth grows and the Dark Sea comes in. */
    public static final class Shore {
        public static final double RADIUS_X = 2500.0;
        public static final double RADIUS_Z = 1650.0;

        private Shore() {
        }

        /** How far out from the Crimson Spire a place lies, as a fraction of the basin: under 1 inside it. */
        public static double basin(double x, double z) {
            double dx = (x - Places.CRIMSON_SPIRE.x()) / RADIUS_X;
            double dz = (z - Places.CRIMSON_SPIRE.z()) / RADIUS_Z;
            return Math.sqrt(dx * dx + dz * dz) + 0.05 * DETAIL.noise(x / 300.0, 3.0, z / 300.0);
        }

        /** West of the labyrinth, where the coral died and ash took its place. */
        public static boolean seaOfAsh(double x, double z) {
            double west = Places.CRIMSON_SPIRE.x() - x + 90.0 * DETAIL.noise(1.0, z / 250.0, 0.0);
            return west > 1250.0 && basin(x, z) < 1.04;
        }
    }
}
