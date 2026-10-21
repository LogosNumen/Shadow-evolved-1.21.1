package net.logosnumen.nightmareevolved.aspect.series;

import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Aspect;

/**
 * Every Aspect of the novel's Awakened that the mod carries: the three Divine Aspects first, then the rest, roughly by
 * their bearers' standing. Each comes with the Flaw the novel gives its bearer, or (where the novel never names one)
 * a few Flaws that suit it.
 */
public final class Roster {
    private Roster() {
    }

    public static List<Aspect> all() {
        return List.of(
            ShadowSlave.ASPECT,
            LightBringer.ASPECT,
            PrinceOfNothing.ASPECT,
            SongOfTheFallen.ASPECT,
            SoulReaper.ASPECT,
            KingOfSwords.ASPECT,
            QueenOfWorms.ASPECT,
            RaisedByWolves.ASPECT,
            PrincessOfWar.ASPECT,
            Nightingale.ASPECT,
            SongDaughters.CRIMSON_HUNGER,
            SongDaughters.LIGHTSLAYER,
            SongDaughters.BEASTMASTER,
            SongDaughters.SILENT_STALKER,
            Jester.ASPECT,
            Valor.HUNTRESS_BOW,
            Valor.SUMMER_KNIGHT,
            Valor.SKY_TIDE,
            Valor.GOLDEN_THUNDER,
            Valor.STEPPING_AIR,
            Valor.ROSE_AEGIS
        );
    }
}
