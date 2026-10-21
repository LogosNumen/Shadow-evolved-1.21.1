package net.logosnumen.nightmareevolved.soul;

/**
 * The numbers behind a soul, kept in one place so they are easy to tune. As in the novel, each soul core holds a
 * thousand soul fragments; shards from slain creatures add fragments, more the higher the creature's Rank and far fewer
 * when it ranks below the soul. A core's saturation (how full it is) strengthens the body and deepens the essence it
 * holds; extra cores, which only a Divine Aspect can grow, strengthen both further.
 */
public final class SoulRules {
    /** Fragments one soul core holds when saturated. */
    public static final int FRAGMENTS_PER_CORE = 1000;
    /** The most cores a soul can hold, one for each class from Beast to Titan. */
    public static final int MAX_CORES = 7;

    private static final double[] ESSENCE = {0, 40, 100, 220, 450, 900, 1800, 3600};
    private static final double[] HEALTH = {0, 0, 4, 10, 20, 34, 52, 76};
    private static final double[] DAMAGE = {0, 0, 1, 2.5, 4.5, 7, 10, 14};
    private static final double[] ARMOR = {0, 0, 1, 3, 5, 8, 11, 15};
    private static final double[] SPEED = {0, 0, 0.02, 0.05, 0.08, 0.12, 0.16, 0.2};
    /** Fragments a shard holds by the creature's Rank, Dormant to Unholy. */
    private static final int[] SHARD = {5, 20, 60, 150, 400, 1000, 1000};

    private SoulRules() {
    }

    /** Fragments all of a soul's cores hold together when saturated. */
    public static int capacity(int cores) {
        return Math.max(0, cores) * FRAGMENTS_PER_CORE;
    }

    /**
     * Fragments a shard of a creature Rank (Dormant 0 to Unholy 6) gives a soul standing at {@code soulLadder}: its
     * full worth from creatures of the soul's Rank or above, a quarter as much for each Rank below.
     */
    public static int shardValue(int creatureRank, int soulLadder) {
        int rank = Math.max(0, Math.min(SHARD.length - 1, creatureRank));
        int below = Math.max(0, soulLadder - rank);
        return Math.max(1, (int)(SHARD[rank] / Math.pow(4.0, below)));
    }

    /** How much more an Aspect of a Rank lets its bearer hold: its potential, not its power. */
    public static double aspectFactor(Tier aspect) {
        return 1.0 + 0.1 * aspect.ordinal();
    }

    /** Essence a soul holds: by Rank, deepened by its Aspect, its saturation, and any extra cores. */
    public static double maxEssence(Rank rank, Tier aspect, int cores, double saturation) {
        double full = ESSENCE[rank.ordinal()] * aspectFactor(aspect) * (1.0 + 0.5 * Math.max(0, cores - 1));
        return full * (0.4 + 0.6 * clamp(saturation));
    }

    /** Essence a soul recovers each second before Flaws and Attributes: a hundredth of what it holds. */
    public static double regenPerSecond(double maxEssence) {
        return maxEssence * 0.01;
    }

    /** How strongly a soul's saturation shows in the body: three fifths at an empty core, all of it when full. */
    public static double bodyShare(double saturation) {
        return 0.6 + 0.4 * clamp(saturation);
    }

    public static double health(Rank rank, int cores, double saturation) {
        return HEALTH[rank.ordinal()] * bodyShare(saturation) + 2.0 * Math.max(0, cores - 1);
    }

    public static double damage(Rank rank, int cores, double saturation) {
        return DAMAGE[rank.ordinal()] * bodyShare(saturation) + 0.5 * Math.max(0, cores - 1);
    }

    public static double armor(Rank rank, int cores, double saturation) {
        return ARMOR[rank.ordinal()] * bodyShare(saturation) + 0.5 * Math.max(0, cores - 1);
    }

    public static double speed(Rank rank, double saturation) {
        return SPEED[rank.ordinal()] * bodyShare(saturation);
    }

    /** How much harder a soul's abilities hit: a third again for each Rank past Sleeper, a little more for a rarer Aspect. */
    public static double power(Rank rank, Tier aspect) {
        return (1.0 + 0.35 * Math.max(0, rank.ladder)) * (1.0 + 0.06 * aspect.ordinal());
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
