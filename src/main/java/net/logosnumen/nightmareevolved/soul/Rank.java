package net.logosnumen.nightmareevolved.soul;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/**
 * The ladder a soul climbs under the Nightmare Spell. An Aspirant has been touched by the Spell but not yet tested;
 * the First Nightmare makes a Sleeper; escaping the Dream Realm makes one Awakened; and each Nightmare after that
 * raises the soul again: Ascended (a Master), Transcendent (a Saint), Supreme (a Sovereign), then Sacred and Divine.
 * {@link #ladder} is the soul's place on the same scale as Nightmare Creature ranks (a Sleeper stands with Dormant
 * creatures, a Divine soul with Unholy ones).
 */
public enum Rank {
    ASPIRANT(-1, 0),
    SLEEPER(0, 1),
    AWAKENED(1, 1),
    MASTER(2, 2),
    SAINT(3, 3),
    SOVEREIGN(4, 4),
    SACRED(5, 5),
    DIVINE(6, 6);

    /** The matching creature rank's index (Dormant 0 to Unholy 6), or -1 for an Aspirant. */
    public final int ladder;
    /** Soul cores the rank holds. */
    public final int cores;

    Rank(int ladder, int cores) {
        this.ladder = ladder;
        this.cores = cores;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** What the bearer is called: Aspirant, Sleeper, Awakened, Master, Saint, Sovereign, Sacred, Divine. */
    public Component title() {
        return Component.translatable("soul.nightmare_evolved.rank." + this.id());
    }

    /** The rank of the soul itself: Dormant, Awakened, Ascended, Transcendent, Supreme, Sacred, Divine. */
    public Component tier() {
        return Component.translatable("soul.nightmare_evolved.tier." + this.id());
    }

    public boolean atLeast(Rank other) {
        return this.ordinal() >= other.ordinal();
    }

    /** The next rank up, or this one at the top. */
    public Rank next() {
        Rank[] all = values();
        return all[Math.min(all.length - 1, this.ordinal() + 1)];
    }

    public static Rank byOrdinal(int ordinal) {
        Rank[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }
}
