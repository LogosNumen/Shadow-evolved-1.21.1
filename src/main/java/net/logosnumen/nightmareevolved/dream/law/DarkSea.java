package net.logosnumen.nightmareevolved.dream.law;

import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.minecraft.util.Mth;

/**
 * The Dark Sea, the Forgotten Shore's curse: a sea of pitch-black water, flat and still as a mirror, that rises out of
 * the depths every night to flood the labyrinth and draws back by day, afraid of the light. Only high ground stays dry:
 * coral tops, Bone Ridge's spine, the Ashen Barrow (an island by night), the Dark City behind its walls. It never comes
 * past the barrows at the Shore's northern edge. Once the seven Shard Memories are joined into the Oath Key and set in
 * the Crimson Spire's lock, it is sealed away in the Spire's well for good.
 * <p>
 * Its height depends only on the time of day, so the client draws it where the server says it is without being told.
 */
public final class DarkSea {
    /** By day the Dark Sea keeps to the deep places: the moat around the Spire, nothing more. */
    public static final double LOW = 58.0;
    /** By night it stands this high over the whole basin. */
    public static final double HIGH = 84.0;

    private DarkSea() {
    }

    /** The surface's height at a time of day (0 is sunrise), whether or not it is sealed. */
    public static double level(long dayTime, float partialTick) {
        double t = Math.floorMod(dayTime, 24000L) + partialTick;
        double rise = smooth((t - 12200.0) / 1600.0);
        double fall = smooth((t - 22400.0) / 1500.0);
        return Mth.lerp(rise - fall, LOW, HIGH);
    }

    private static double smooth(double t) {
        t = Mth.clamp(t, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    /** Whether the Dark Sea reaches a place at all: the Shore, but not the barrows or beyond. */
    public static boolean reaches(double x, double z) {
        Region region = RealmMap.part((int)Math.floor(x), 70, (int)Math.floor(z));
        return region == Region.FORGOTTEN_SHORE || region == Region.SEA_OF_ASH;
    }

    /** Whether a point is under the Dark Sea right now. */
    public static boolean under(double x, double y, double z, long dayTime, boolean sealed) {
        return !sealed && y < level(dayTime, 0.0F) && reaches(x, z);
    }
}
