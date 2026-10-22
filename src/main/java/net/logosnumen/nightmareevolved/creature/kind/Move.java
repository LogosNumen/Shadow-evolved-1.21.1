package net.logosnumen.nightmareevolved.creature.kind;

/**
 * One of a creature's moves: what kind of blow it is, how long it lasts in ticks, the ticks its blows land on, the range
 * it is used at (in blocks), how long before it can be used again, how often it is chosen, how hard it hits (times the
 * creature's attack damage), and how far it reaches.
 */
public record Move(String id, Style style, int duration, int[] impacts, double minRange, double maxRange, int cooldown, int weight, float power,
    double reach) {
    /** What a move does when its blows land. */
    public enum Style {
        /** A blow at whatever is in front within reach. */
        STRIKE,
        /** One bite or stab at the target, if it is still within reach. */
        BITE,
        /** Everything around the body within reach. */
        SWEEP,
        /** A leap at the target; the blow lands on everything near where it comes down. */
        POUNCE,
        /** A rush straight ahead, striking whatever it passes. */
        CHARGE,
        /** Something thrown, spat or loosed at the target. */
        SHOT,
        /** The ground beaten: a ring of force around the body. */
        SLAM,
        /** The target seized and dragged in. */
        GRAB,
        /** A blast of light, venom or sound over everything in a cone ahead. */
        BREATH,
        /** Kin called to the fight. */
        CALL,
        /** Handled by the creature itself. */
        SPECIAL
    }

    public static Move of(String id, Style style, int duration, double minRange, double maxRange, int cooldown, int weight, float power, double reach,
        int... impacts) {
        return new Move(id, style, duration, impacts, minRange, maxRange, cooldown, weight, power, reach);
    }

    /** A plain blow at close range: lasts {@code duration}, lands at {@code impact}. */
    public static Move melee(String id, Style style, int duration, int impact, double reach, float power, int cooldown) {
        return new Move(id, style, duration, new int[]{impact}, 0.0, reach + 0.5, cooldown, 10, power, reach);
    }

    /** The first blow's tick: until shortly before it, the creature keeps turning to follow its target. */
    public int firstImpact() {
        return this.impacts.length > 0 ? this.impacts[0] : this.duration / 2;
    }
}
