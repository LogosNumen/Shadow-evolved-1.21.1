package net.logosnumen.nightmareevolved.client.soul;

import java.util.List;
import net.logosnumen.nightmareevolved.network.SoulPayload;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.logosnumen.nightmareevolved.soul.Tier;

/** The local player's soul as the server last described it. */
public final class ClientSoul {
    private static SoulPayload soul = SoulPayload.EMPTY;

    private ClientSoul() {
    }

    public static void set(SoulPayload payload) {
        soul = payload;
    }

    public static SoulPayload get() {
        return soul;
    }

    public static Rank rank() {
        return Rank.byOrdinal(soul.rank());
    }

    public static String aspect() {
        return soul.aspect();
    }

    public static Tier aspectTier() {
        return Tier.byOrdinal(soul.aspectTier());
    }

    public static String flaw() {
        return soul.flaw();
    }

    public static List<String> attributes() {
        return soul.attributes();
    }

    public static int cores() {
        return soul.cores();
    }

    public static int fragments() {
        return soul.fragments();
    }

    public static int capacity() {
        return SoulRules.capacity(soul.cores());
    }

    public static float essence() {
        return soul.essence();
    }

    public static float maxEssence() {
        return soul.maxEssence();
    }

    /** The game time at which the ability on a step's key (0 to 4) is ready again. */
    public static long readyAt(int slot) {
        return soul.readyAt(slot);
    }

    public static boolean held(String ability) {
        return soul.held().contains(ability);
    }

    public static void clear() {
        soul = SoulPayload.EMPTY;
    }
}
