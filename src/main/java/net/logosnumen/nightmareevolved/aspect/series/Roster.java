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
            LightBringer.ASPECT
        );
    }
}
