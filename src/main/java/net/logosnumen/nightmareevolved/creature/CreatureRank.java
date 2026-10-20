package net.logosnumen.nightmareevolved.creature;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** How corrupted a Nightmare Creature is, weakest first. */
public enum CreatureRank {
    DORMANT,
    AWAKENED,
    FALLEN,
    CORRUPTED,
    GREAT,
    CURSED,
    UNHOLY;

    public Component title() {
        return Component.translatable("creature.nightmare_evolved.rank." + this.name().toLowerCase(Locale.ROOT));
    }
}
