package net.logosnumen.nightmareevolved.colossus;

/**
 * One of a colossus's moves: how long it lasts in ticks, the ticks its blows land on, the range it is used at (in
 * blocks, scaled by the colossus's size), how long before it can be used again, and how often it is chosen.
 */
public record ColossusAttack(String id, int duration, int[] impacts, double minRange, double maxRange, int cooldown, int weight) {
    public static ColossusAttack of(String id, int duration, double minRange, double maxRange, int cooldown, int weight, int... impacts) {
        return new ColossusAttack(id, duration, impacts, minRange, maxRange, cooldown, weight);
    }

    /** The first blow's tick: until shortly before it, the colossus keeps turning to follow its target. */
    public int firstImpact() {
        return this.impacts.length > 0 ? this.impacts[0] : this.duration / 2;
    }
}
