package net.logosnumen.nightmareevolved.aspect;

import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;

/**
 * An Aspect: the power the Spell grants a soul after the First Nightmare, with its abilities (one waking at each rank
 * from Sleeper) and the Flaw that comes with it.
 */
public record Aspect(String id, List<Ability> abilities, Flaw flaw) {
    public enum Flaw {
        /** Hits a fifth less hard in bright light. */
        LIGHT_SHY,
        /** Hits a quarter less hard while wet. */
        DOUSED,
        /** Moves a tenth slower, always. */
        LEADEN,
        /** Abilities cost twice as much in the Nether. */
        PARCHED,
        /** Takes a sixth more damage from everything. */
        BRITTLE
    }

    public Component title() {
        return Component.translatable("aspect.nightmare_evolved." + this.id);
    }

    public Component description() {
        return Component.translatable("aspect.nightmare_evolved." + this.id + ".desc");
    }

    public Component flawTitle() {
        return Component.translatable("flaw.nightmare_evolved." + this.flaw.name().toLowerCase(Locale.ROOT));
    }

    public Component flawDescription() {
        return Component.translatable("flaw.nightmare_evolved." + this.flaw.name().toLowerCase(Locale.ROOT) + ".desc");
    }
}
