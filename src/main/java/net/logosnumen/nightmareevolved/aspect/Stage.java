package net.logosnumen.nightmareevolved.aspect;

import java.util.Locale;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.minecraft.network.chat.Component;

/**
 * The step of Ascension at which an Aspect ability wakes, as the Spell names them: the Dormant Ability comes with the
 * First Nightmare, the Awakened Ability on leaving the Dream Realm, the Ascended Ability with Mastery, the
 * Transformation with Sainthood, and the Supreme Ability with Sovereignty. Each has its own key.
 */
public enum Stage {
    DORMANT(Rank.SLEEPER),
    AWAKENED(Rank.AWAKENED),
    ASCENDED(Rank.MASTER),
    TRANSCENDENT(Rank.SAINT),
    SUPREME(Rank.SOVEREIGN);

    /** The soul Rank that wakes abilities of this step. */
    public final Rank unlock;

    Stage(Rank unlock) {
        this.unlock = unlock;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("ability.nightmare_evolved.stage." + this.id());
    }

    public static Stage bySlot(int slot) {
        Stage[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, slot))];
    }
}
