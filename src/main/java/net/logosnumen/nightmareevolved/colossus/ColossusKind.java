package net.logosnumen.nightmareevolved.colossus;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;

/** The four colossi, each a body plan of its own. Every one born varies within its kind (see {@link ColossusVariant}). */
public enum ColossusKind {
    /** A giant of piled stone that walks on two legs, fissures glowing in its chest. */
    CAIRN("cairn_colossus", CreatureRank.CURSED),
    /** The skeleton of some vast four-legged beast, a light still burning inside its ribs. */
    OSSUARY("ossuary_behemoth", CreatureRank.CURSED),
    /** A great burrowing serpent of plates and spines that swims through the ground. */
    GRAVEWYRM("gravewyrm", CreatureRank.CURSED),
    /** An armoured giant bound in chains, carrying a weapon taller than a house. */
    CHAINBOUND("chainbound_colossus", CreatureRank.UNHOLY);

    /** How many colourings each kind comes in. */
    public static final int PALETTES = 5;
    public final String id;
    /** The rank this kind is born at; every colossus is of the Titan class. */
    public final CreatureRank rank;

    ColossusKind(String id, CreatureRank rank) {
        this.id = id;
        this.rank = rank;
    }

    public CreatureClass creatureClass() {
        return CreatureClass.TITAN;
    }

    @Nullable
    public static ColossusKind byId(String id) {
        for (ColossusKind kind : values()) {
            if (kind.id.equals(id)) {
                return kind;
            }
        }

        return null;
    }
}
