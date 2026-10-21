package net.logosnumen.nightmareevolved.dream.law;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.shape.IslesShaper;
import net.minecraft.util.Mth;

/**
 * The Crushing of the Chained Isles. Every island rises and falls in its own cycle; as it rises, the pressure on its
 * surface grows until no one can move or breathe, and the rattle of its chains gives warning before it begins. The
 * less weight a body carries, the easier the Crushing is to endure. Above the islands the sky itself is forbidden: the
 * higher one climbs, the stronger it presses, and nothing survives there but the Ivory Tower. Only the Sanctuary of
 * Noctis's island never rises or falls.
 */
public final class Crushing {
    /** One island's cycle, in ticks. */
    public static final long PERIOD = 9600L;
    /** How long before an island starts to rise its chains rattle. */
    public static final long WARNING = 500L;
    /** Above this height over the Isles, the sky presses down on anyone at all. */
    public static final double FORBIDDEN = 268.0;
    private static final long SALT = 0xC2A5L;

    private Crushing() {
    }

    private static double phase(IslesShaper.Island island, long time) {
        long offset = (long)(Noise.hash(SALT, island.salt(), 0, 1) * PERIOD);
        return Math.floorMod(time + offset, PERIOD) / (double)PERIOD;
    }

    /** The pressure on an island at a time, from 0 (at rest) to 1 (crushing). */
    public static double pressure(@Nullable IslesShaper.Island island, long time) {
        if (island == null || Places.SANCTUARY_OF_NOCTIS.distance(island.x(), island.z()) < 1.0) {
            return 0.0;
        }

        double phase = phase(island, time);
        if (phase < 0.3) {
            return smooth(phase / 0.3);
        } else if (phase < 0.4) {
            return 1.0;
        } else if (phase < 0.6) {
            return 1.0 - smooth((phase - 0.4) / 0.2);
        }

        return 0.0;
    }

    /** Whether an island's chains are rattling: it is about to rise. */
    public static boolean rattling(@Nullable IslesShaper.Island island, long time) {
        if (island == null || pressure(island, time) > 0.0) {
            return false;
        }

        return phase(island, time) > 1.0 - WARNING / (double)PERIOD;
    }

    /** The forbidden sky's own pressure at a height, wherever one is over the Isles (but near the Ivory Tower). */
    public static double sky(double x, double y, double z) {
        if (y < FORBIDDEN || Places.IVORY_TOWER.distance(x, z) < 70.0) {
            return 0.0;
        }

        return Mth.clamp((y - FORBIDDEN) / 30.0 + 0.6, 0.0, 1.5);
    }

    private static double smooth(double t) {
        t = Mth.clamp(t, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}
