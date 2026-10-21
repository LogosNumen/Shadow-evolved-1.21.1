package net.logosnumen.nightmareevolved.soul;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

/**
 * The ladder a soul climbs under the Nightmare Spell. An Aspirant carries the Spell but has not been tested; the First
 * Nightmare makes a Sleeper, whose soul is Dormant; reaching a Gateway out of the Dream Realm makes one Awakened; and
 * each Nightmare after that raises the soul again: a Master is Ascended, a Saint Transcendent, a Sovereign Supreme,
 * and beyond them the Sacred and the Divine. {@link #ladder} is the soul's place on the scale Nightmare Creatures share
 * (a Sleeper stands with Dormant creatures, a Divine soul with Unholy ones).
 */
public enum Rank {
    ASPIRANT(-1),
    SLEEPER(0),
    AWAKENED(1),
    MASTER(2),
    SAINT(3),
    SOVEREIGN(4),
    SACRED(5),
    DIVINE(6);

    /** The matching creature rank's index (Dormant 0 to Unholy 6), or -1 for an Aspirant. */
    public final int ladder;

    Rank(int ladder) {
        this.ladder = ladder;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** What the bearer is called: Aspirant, Sleeper, Awakened, Master, Saint, Sovereign, Sacred, Divine. */
    public Component title() {
        return Component.translatable("soul.nightmare_evolved.rank." + this.id());
    }

    /** The Rank of the soul itself (Dormant to Divine), or null for an Aspirant, who has no core yet. */
    @Nullable
    public Tier soulTier() {
        return this.ladder < 0 ? null : Tier.byOrdinal(this.ladder);
    }

    /** The soul's Rank as a name, "unranked" for an Aspirant. */
    public Component tier() {
        Tier tier = this.soulTier();
        return tier == null ? Component.translatable("soul.nightmare_evolved.tier.none") : tier.title();
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

    public static Rank byId(String id) {
        for (Rank rank : values()) {
            if (rank.id().equals(id)) {
                return rank;
            }
        }

        return ASPIRANT;
    }
}
