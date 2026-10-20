package net.logosnumen.nightmareevolved.creature;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** What kind of Nightmare Creature something is, from the plainest beast to a titan. */
public enum CreatureClass {
    BEAST,
    MONSTER,
    DEMON,
    DEVIL,
    TYRANT,
    TERROR,
    TITAN;

    public Component title() {
        return Component.translatable("creature.nightmare_evolved.class." + this.name().toLowerCase(Locale.ROOT));
    }
}
