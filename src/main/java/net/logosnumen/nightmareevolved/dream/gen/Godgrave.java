package net.logosnumen.nightmareevolved.dream.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The dead god of Godgrave: a skeleton so vast that its skull rests on the slopes of the Hollow Mountains while its
 * right knee, the southernmost part still whole, lies thousands of blocks away. It lies on its back, head to the north.
 * Its right hand reaches into the Song Domain, its left into the Sword Domain. The left side of the skull, its forehead
 * and one eye socket are shattered; the other socket is empty.
 * <p>
 * The bones are white, cracked, and hollow. Vermilion grass and red moss grow on them. Inside the breastbone lies a
 * jungle the size of a small country, the Breastbone Hollow, with the ruins of an old civilisation and Vanishing Lake at
 * its heart; the collarbones, the arm and thigh bones, and the canal of the spine are hollows too. The geometry is the
 * same in every world.
 */
public final class Godgrave {
    public static final int AXIS = Places.GODGRAVE_SKULL.x();
    /** The breastbone vault, where the jungle grows. */
    public static final int VAULT_FLOOR = 232;
    public static final int VAULT_ROOF = 304;
    public static final int VAULT_HALF = 170;
    public static final int VAULT_NORTH = -1450;
    public static final int VAULT_SOUTH = 650;
    private static final int SHELL = 7;
    private static final double SKULL_X = AXIS;
    private static final double SKULL_Y = 150.0;
    private static final double SKULL_Z = Places.GODGRAVE_SKULL.z();
    private static final double SKULL_RX = 300.0;
    private static final double SKULL_RY = 140.0;
    private static final double SKULL_RZ = 330.0;
    private static final int INDEX = 128;
    private static final long SEED = 0x60D6_2A7EL;
    private static final Noise CRACKS = new Noise(SEED, 2);
    private static final Noise BREAK = new Noise(SEED + 1, 2);
    private static final Noise GROWTH = new Noise(SEED + 2, 2);
    private static final List<Tube> TUBES = new ArrayList<>();
    private static final Map<Long, List<Tube>> GRID = new HashMap<>();

    /** A straight bone between two points, solid or hollow, with knobs at its ends if it has them. */
    private record Tube(double ax, double ay, double az, double bx, double by, double bz, double radius, double hollow, double knob,
        int minX, int maxX, int minZ, int maxZ) {
        static Tube of(double ax, double ay, double az, double bx, double by, double bz, double radius, double hollow, double knob) {
            double reach = Math.max(radius, knob) + 2.0;
            return new Tube(ax, ay, az, bx, by, bz, radius, hollow, knob, (int)Math.floor(Math.min(ax, bx) - reach), (int)Math.ceil(Math.max(ax, bx) + reach),
                (int)Math.floor(Math.min(az, bz) - reach), (int)Math.ceil(Math.max(az, bz) + reach));
        }
    }

    static {
        // The neck and spine, lying on the ground under the body; the spine's canal is a hollow of its own.
        for (double z = -1990.0; z < -1640.0; z += 90.0) {
            TUBES.add(Tube.of(AXIS, 95.0, z, AXIS, 95.0, z + 58.0, 52.0, 0.0, 0.0));
        }

        for (double z = -1620.0; z < 1500.0; z += 88.0) {
            TUBES.add(Tube.of(AXIS, 98.0, z, AXIS, 98.0, z + 70.0, 58.0, 17.0, 0.0));
            TUBES.add(Tube.of(AXIS, 150.0, z + 35.0, AXIS, 180.0, z + 35.0, 14.0, 0.0, 0.0));
        }

        // The collarbones, from the top of the breastbone out to the shoulders.
        for (int side = -1; side <= 1; side += 2) {
            TUBES.add(Tube.of(AXIS + side * 70.0, 268.0, VAULT_NORTH + 20.0, AXIS + side * 640.0, 150.0, -1560.0, 38.0, 26.0, 46.0));
            // The upper arm, the forearm's two bones, and the hand with its fingers spread.
            double sx = AXIS + side * 660.0;
            double ex = AXIS + side * 900.0;
            double wx = AXIS + side * 1150.0;
            TUBES.add(Tube.of(sx, 112.0, -1500.0, ex, 100.0, 150.0, 54.0, 38.0, 74.0));
            TUBES.add(Tube.of(ex, 100.0, 150.0, wx, 92.0, 1250.0, 34.0, 0.0, 44.0));
            TUBES.add(Tube.of(ex + side * 60.0, 96.0, 180.0, wx + side * 70.0, 90.0, 1240.0, 26.0, 0.0, 32.0));
            for (int f = 0; f < 5; f++) {
                double spread = Math.toRadians(-30.0 + f * 22.0);
                double dirX = side * Math.cos(spread);
                double dirZ = Math.sin(spread) + 0.6;
                double len = f == 0 ? 120.0 : 170.0 + (f == 2 ? 40.0 : 0.0);
                double px = wx + side * 50.0;
                double pz = 1300.0;
                double py = 88.0;
                for (int k = 0; k < 3; k++) {
                    double seg = len * (k == 0 ? 0.45 : 0.3);
                    double qx = px + dirX * seg;
                    double qz = pz + dirZ * seg;
                    double qy = py - 3.0;
                    TUBES.add(Tube.of(px, py, pz, qx, qy, qz, 16.0 - k * 2.5, 0.0, 19.0 - k * 2.5));
                    px = qx;
                    pz = qz;
                    py = qy;
                }
            }

            // The pelvis's wings.
            TUBES.add(Tube.of(AXIS + side * 90.0, 120.0, 1520.0, AXIS + side * 330.0, 150.0, 1640.0, 70.0, 0.0, 70.0));
            // The thigh bones: the right runs whole to its knee, the left is broken off short.
            double knee = side < 0 ? 3600.0 : 2900.0;
            TUBES.add(Tube.of(AXIS + side * 240.0, 112.0, 1800.0, AXIS + side * 300.0, 106.0, knee, 64.0, 46.0, 88.0));
        }

        TUBES.add(Tube.of(AXIS, 110.0, 1520.0, AXIS, 108.0, 1700.0, 74.0, 0.0, 74.0));
        TUBES.add(Tube.of(AXIS - 300.0, 106.0, 3600.0, AXIS - 300.0, 106.0, 3640.0, 70.0, 0.0, 92.0));
        for (Tube tube : TUBES) {
            for (int gx = Math.floorDiv(tube.minX, INDEX); gx <= Math.floorDiv(tube.maxX, INDEX); gx++) {
                for (int gz = Math.floorDiv(tube.minZ, INDEX); gz <= Math.floorDiv(tube.maxZ, INDEX); gz++) {
                    GRID.computeIfAbsent(key(gx, gz), k -> new ArrayList<>()).add(tube);
                }
            }
        }
    }

    private Godgrave() {
    }

    /** The skeleton's reach, for skipping columns far from it. */
    public static boolean near(int x, int z) {
        return x >= AXIS - 1600 && x <= AXIS + 1600 && z >= (int)(SKULL_Z - SKULL_RZ - 10) && z <= 3740;
    }

    private static long key(int gx, int gz) {
        return ((long)gx << 32) ^ (gz & 0xFFFFFFFFL);
    }

    private static List<Tube> tubesAt(int x, int z) {
        return GRID.getOrDefault(key(Math.floorDiv(x, INDEX), Math.floorDiv(z, INDEX)), List.of());
    }

    /** Whether a place lies inside one of the dead god's hollow bones. */
    public static boolean inHollow(int x, int y, int z) {
        if (!near(x, z)) {
            return false;
        }

        if (Math.abs(x - AXIS) < VAULT_HALF - SHELL && z > VAULT_NORTH && z < VAULT_SOUTH && y > VAULT_FLOOR && y < roof(x) - SHELL) {
            return true;
        }

        for (Tube tube : tubesAt(x, z)) {
            if (tube.hollow > 0.0 && distance(tube, x + 0.5, y + 0.5, z + 0.5) < tube.hollow) {
                return true;
            }
        }

        return false;
    }

    private static double roof(int x) {
        double t = (x - AXIS) / (double)VAULT_HALF;
        return VAULT_ROOF - 26.0 * t * t;
    }

    private static double distance(Tube tube, double px, double py, double pz) {
        double dx = tube.bx - tube.ax;
        double dy = tube.by - tube.ay;
        double dz = tube.bz - tube.az;
        double len2 = dx * dx + dy * dy + dz * dz;
        double t = Mth.clamp(((px - tube.ax) * dx + (py - tube.ay) * dy + (pz - tube.az) * dz) / len2, 0.0, 1.0);
        double cx = tube.ax + dx * t - px;
        double cy = tube.ay + dy * t - py;
        double cz = tube.az + dz * t - pz;
        return Math.sqrt(cx * cx + cy * cy + cz * cz);
    }

    /** Paints the skeleton into a column. */
    public static void paint(Column column) {
        int x = column.x;
        int z = column.z;
        if (!near(x, z)) {
            return;
        }

        BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
        boolean any = skull(column, bone);
        for (Tube tube : tubesAt(x, z)) {
            any |= tube(column, tube, bone);
        }

        any |= ribs(column, bone);
        any |= vault(column, bone);
        if (any) {
            grow(column);
        }
    }

    /** The y ranges where a vertical line at a column meets a tube; fills the bone and carves its hollow. */
    private static boolean tube(Column column, Tube tube, BlockState bone) {
        double px = column.x + 0.5;
        double pz = column.z + 0.5;
        if (px < tube.minX || px > tube.maxX || pz < tube.minZ || pz > tube.maxZ) {
            return false;
        }

        double lo = Double.MAX_VALUE;
        double hi = -Double.MAX_VALUE;
        double holeLo = Double.MAX_VALUE;
        double holeHi = -Double.MAX_VALUE;
        int from = (int)Math.floor(Math.min(tube.ay, tube.by) - Math.max(tube.radius, tube.knob) - 1);
        int to = (int)Math.ceil(Math.max(tube.ay, tube.by) + Math.max(tube.radius, tube.knob) + 1);
        for (int y = from; y <= to; y++) {
            double py = y + 0.5;
            double d = distance(tube, px, py, pz);
            boolean knob = tube.knob > 0.0 && (dist3(px, py, pz, tube.ax, tube.ay, tube.az) < tube.knob
                || dist3(px, py, pz, tube.bx, tube.by, tube.bz) < tube.knob);
            if (d < tube.radius || knob) {
                lo = Math.min(lo, y);
                hi = Math.max(hi, y);
            }

            if (tube.hollow > 0.0 && d < tube.hollow && !knob) {
                holeLo = Math.min(holeLo, y);
                holeHi = Math.max(holeHi, y);
            }
        }

        if (lo > hi) {
            return false;
        }

        if (cracked(column.x, (int)hi, column.z)) {
            hi -= SHELL;
        }

        column.fill((int)lo, (int)hi, bone);
        if (holeLo <= holeHi) {
            column.clear((int)holeLo, (int)holeHi);
            // The hollow's floor: moss and red growth, as in every hollow bone.
            int floor = (int)holeLo;
            column.fill(floor, floor + 1, DreamBlocks.RED_MOSS.get().defaultBlockState());
            if (Noise.hash(SEED, column.x, column.z, 1) < 0.25) {
                column.set(floor + 2, DreamBlocks.VERMILION_GRASS.get().defaultBlockState());
            }
        }

        return true;
    }

    private static double dist3(double ax, double ay, double az, double bx, double by, double bz) {
        double dx = ax - bx;
        double dy = ay - by;
        double dz = az - bz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Cracks and fissures in the bones' upper surfaces, leading down into their hollows. */
    private static boolean cracked(int x, int y, int z) {
        return Math.abs(CRACKS.at(x / 34.0, y / 50.0, z / 34.0)) < 0.035;
    }

    /** The skull: a shell, hollow inside, shattered on its left side and across its forehead, its right socket empty. */
    private static boolean skull(Column column, BlockState bone) {
        double nx = (column.x + 0.5 - SKULL_X) / SKULL_RX;
        double nz = (column.z + 0.5 - SKULL_Z) / SKULL_RZ;
        double flat = 1.0 - nx * nx - nz * nz;
        if (flat <= 0.0) {
            return false;
        }

        double half = SKULL_RY * Math.sqrt(flat);
        double innerFlat = 1.0 - Math.pow(nx * SKULL_RX / (SKULL_RX - 14.0), 2) - Math.pow(nz * SKULL_RZ / (SKULL_RZ - 14.0), 2);
        double innerHalf = innerFlat > 0.0 ? (SKULL_RY - 14.0) * Math.sqrt(innerFlat) : -1.0;
        int lo = (int)Math.floor(SKULL_Y - half);
        int hi = (int)Math.ceil(SKULL_Y + half);
        double east = column.x - SKULL_X;
        double north = SKULL_Z - column.z;
        double jag = 40.0 * BREAK.at(column.x / 40.0, column.z / 40.0);
        boolean leftGone = east > 60.0 + jag;
        boolean foreheadGone = north > 130.0 + jag;
        // The right eye socket, empty: a great round hole through the top of the face.
        double ex = column.x - (SKULL_X - 115.0);
        double ez = column.z - (SKULL_Z + 40.0);
        boolean socket = ex * ex + ez * ez < 62.0 * 62.0;
        double nose = Math.abs(column.x - SKULL_X) + Math.abs(column.z - (SKULL_Z + 150.0)) * 0.7;
        int top = hi;
        if (leftGone || foreheadGone) {
            // Shattered: only the lower part of the shell remains, broken off raggedly.
            top = (int)(SKULL_Y - half * 0.2 + jag * 0.5);
        }

        column.fill(lo, Math.min(hi, top), bone);
        if (innerHalf > 0.0) {
            int innerLo = (int)Math.floor(SKULL_Y - innerHalf);
            int innerHi = (int)Math.ceil(SKULL_Y + innerHalf);
            column.clear(innerLo, innerHi);
            column.fill(innerLo - 1, innerLo + 2, DreamBlocks.ASH.get().defaultBlockState());
        }

        if ((socket || nose < 34.0) && top == hi) {
            column.clear(hi - 16, hi);
        }

        return true;
    }

    /**
     * The ribs: twelve pairs, each a half-ellipse rising from the spine out to the side and up over to the breastbone.
     * The lowest pairs float free and stop short.
     */
    private static boolean ribs(Column column, BlockState bone) {
        int x = column.x;
        int z = column.z;
        boolean any = false;
        for (int k = 0; k < 12; k++) {
            double zk = -1450.0 + k * 150.0;
            double dz = z + 0.5 - zk;
            double radius = 16.0;
            if (Math.abs(dz) > radius) {
                continue;
            }

            double width = 380.0 + 170.0 * Math.sin(Math.PI * (k + 1) / 13.0);
            double height = 96.0;
            double centre = 176.0;
            double thick = Math.sqrt(radius * radius - dz * dz);
            double dx = Math.abs(x + 0.5 - AXIS);
            if (dx > width + thick) {
                continue;
            }

            double s = Math.min(1.0, dx / width);
            double c = Math.sqrt(1.0 - s * s);
            boolean floating = k >= 9;
            double lower = centre - height * c;
            double upper = centre + height * c;
            if (dx > width - thick * 1.5) {
                // The steep part at the side: a long run up the column.
                int to = floating ? (int)centre : (int)upper;
                column.fill((int)(lower - thick), to, bone);
            } else {
                column.fill((int)(lower - thick * 0.6), (int)(lower + thick * 0.6), bone);
                if (!floating || dx > width * 0.7) {
                    if (!floating && dx < VAULT_HALF) {
                        continue;
                    }

                    column.fill((int)(upper - thick * 0.6), (int)(upper + thick * 0.6), bone);
                }
            }

            any = true;
        }

        return any;
    }

    /**
     * The breastbone: a long vault of bone across the top of the ribcage, cracked open here and there, and inside it
     * the jungle of the Breastbone Hollow.
     */
    private static boolean vault(Column column, BlockState bone) {
        int x = column.x;
        int z = column.z;
        double dx = Math.abs(x + 0.5 - AXIS);
        if (dx > VAULT_HALF || z < VAULT_NORTH || z > VAULT_SOUTH) {
            return false;
        }

        int roof = (int)roof(x);
        column.fill(VAULT_FLOOR - 8, roof, bone);
        boolean wall = dx > VAULT_HALF - SHELL || z < VAULT_NORTH + SHELL || z > VAULT_SOUTH - SHELL;
        boolean broken = (z < VAULT_NORTH + SHELL || z > VAULT_SOUTH - SHELL) && dx < 40.0 && BREAK.at(x / 9.0, z / 9.0) > -0.2;
        if (!wall || broken) {
            int open = cracked(x, roof, z) ? roof : roof - SHELL;
            column.clear(VAULT_FLOOR + 1, open);
            jungle(column);
        }

        return true;
    }

    /**
     * The Breastbone Hollow's jungle: blood-red moss over everything, vermilion turf, ferns and vines, and colossal
     * stalks rising all the way to the vault's roof.
     */
    private static void jungle(Column column) {
        int x = column.x;
        int z = column.z;
        BlockState moss = DreamBlocks.RED_MOSS.get().defaultBlockState();
        double g = GROWTH.at(x / 11.0, z / 11.0);
        column.set(VAULT_FLOOR, g > 0.0 ? moss : DreamBlocks.VERMILION_TURF.get().defaultBlockState());
        column.set(VAULT_FLOOR - 1, Blocks.ROOTED_DIRT.defaultBlockState());
        if (Places.VANISHING_LAKE.distance(x, z) < 80.0) {
            // Vanishing Lake: its basin is dug and filled where the citadel is built.
            return;
        }

        if (g > 0.45) {
            column.set(VAULT_FLOOR + 1, DreamBlocks.RED_MOSS_CARPET.get().defaultBlockState());
        } else if (g < -0.3) {
            column.set(VAULT_FLOOR + 1, Noise.hash(SEED, x, z, 3) < 0.5 ? Blocks.FERN.defaultBlockState() : DreamBlocks.VERMILION_GRASS.get().defaultBlockState());
        }

        // The colossal stalks, with crowns of blood-red leaves under the roof.
        Trees.paint(column, 22, (i, j) -> {
            if (Noise.hash(SEED, i, j, 4) > 0.3) {
                return null;
            }

            int tx = i * 22 + 4 + (int)(Noise.hash(SEED, i, j, 5) * 14);
            int tz = j * 22 + 4 + (int)(Noise.hash(SEED, i, j, 6) * 14);
            if (Math.abs(tx - AXIS) > VAULT_HALF - 20 || tz < VAULT_NORTH + 20 || tz > VAULT_SOUTH - 20
                || Places.VANISHING_LAKE.distance(tx, tz) < 100.0 || Places.MICTLAN_ZIGGURAT.distance(tx, tz) < 50.0) {
                return null;
            }

            boolean colossal = Noise.hash(SEED, i, j, 7) < 0.25;
            int height = colossal ? (int)roof(tx) - VAULT_FLOOR - 10 : 12 + (int)(Noise.hash(SEED, i, j, 8) * 14);
            return new Trees.Tree(tx, tz, VAULT_FLOOR + 1, height, colossal ? 7 : 4, Blocks.JUNGLE_LOG.defaultBlockState(),
                DreamBlocks.BLOOD_LEAVES.get().defaultBlockState(), colossal ? Trees.Crown.CANOPY : Trees.Crown.ROUND);
        });
    }

    /** Vermilion grass and red moss on the bones' upper surfaces, where they lie open to the sky. */
    private static void grow(Column column) {
        int top = column.top();
        BlockState here = column.get(top);
        if (here == null || !here.is(Blocks.BONE_BLOCK)) {
            return;
        }

        double g = GROWTH.at(column.x / 23.0, column.z / 23.0);
        if (g > 0.25) {
            column.set(top, DreamBlocks.VERMILION_TURF.get().defaultBlockState());
            if (Noise.hash(SEED, column.x, column.z, 9) < 0.4) {
                column.set(top + 1, DreamBlocks.VERMILION_GRASS.get().defaultBlockState());
            }
        } else if (g < -0.45) {
            column.set(top + 1, DreamBlocks.RED_MOSS_CARPET.get().defaultBlockState());
        }
    }
}
