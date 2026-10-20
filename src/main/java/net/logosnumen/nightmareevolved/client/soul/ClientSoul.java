package net.logosnumen.nightmareevolved.client.soul;

import net.logosnumen.nightmareevolved.network.SoulPayload;
import net.logosnumen.nightmareevolved.soul.AspectTier;
import net.logosnumen.nightmareevolved.soul.Rank;

/** The local player's soul as the server last described it. */
public final class ClientSoul {
    private static SoulPayload soul = SoulPayload.EMPTY;

    private ClientSoul() {
    }

    public static void set(SoulPayload payload) {
        soul = payload;
    }

    public static Rank rank() {
        return Rank.byOrdinal(soul.rank());
    }

    public static String aspect() {
        return soul.aspect();
    }

    public static AspectTier aspectTier() {
        return AspectTier.byOrdinal(soul.aspectTier());
    }

    public static float essence() {
        return soul.essence();
    }

    public static float maxEssence() {
        return soul.maxEssence();
    }

    public static float saturation() {
        return soul.saturation();
    }

    public static float capacity() {
        return soul.capacity();
    }

    /** The game time at which the ability in a slot (0 or 1) is ready again. */
    public static long readyAt(int slot) {
        return soul.readyAt(slot);
    }

    public static void clear() {
        soul = SoulPayload.EMPTY;
    }
}
