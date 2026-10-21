package net.logosnumen.nightmareevolved.soul;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * The seven Ranks the Spell gives a soul, an Aspect or a Memory, from Dormant to Divine. A soul's Rank sets its
 * strength; an Aspect's sets its potential, which is not the same thing as its power.
 */
public enum Tier {
    DORMANT(ChatFormatting.GRAY),
    AWAKENED(ChatFormatting.AQUA),
    ASCENDED(ChatFormatting.GREEN),
    TRANSCENDENT(ChatFormatting.YELLOW),
    SUPREME(ChatFormatting.GOLD),
    SACRED(ChatFormatting.LIGHT_PURPLE),
    DIVINE(ChatFormatting.WHITE);

    public final ChatFormatting colour;

    Tier(ChatFormatting colour) {
        this.colour = colour;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("soul.nightmare_evolved.tier." + this.id()).withStyle(this.colour);
    }

    public static Tier byOrdinal(int ordinal) {
        Tier[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }

    public static Tier byId(String id) {
        for (Tier tier : values()) {
            if (tier.id().equals(id)) {
                return tier;
            }
        }

        return DORMANT;
    }
}
