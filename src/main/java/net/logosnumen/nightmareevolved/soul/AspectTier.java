package net.logosnumen.nightmareevolved.soul;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

/**
 * How strong an Aspect is born. Most are Dormant; a few are Awakened from the start, fewer Ascended, and a rare one is
 * Transcendent. A stronger Aspect holds more essence and hits harder with its abilities.
 */
public enum AspectTier {
    DORMANT(70, 1.0, 1.0),
    AWAKENED(22, 1.15, 1.1),
    ASCENDED(7, 1.3, 1.2),
    TRANSCENDENT(1, 1.5, 1.35);

    private final int weight;
    public final double essence;
    public final double potency;

    AspectTier(int weight, double essence, double potency) {
        this.weight = weight;
        this.essence = essence;
        this.potency = potency;
    }

    public Component title() {
        return Component.translatable("soul.nightmare_evolved.aspect_tier." + this.name().toLowerCase(Locale.ROOT));
    }

    public static AspectTier roll(RandomSource random) {
        int total = 0;
        for (AspectTier tier : values()) {
            total += tier.weight;
        }

        int pick = random.nextInt(total);
        for (AspectTier tier : values()) {
            pick -= tier.weight;
            if (pick < 0) {
                return tier;
            }
        }

        return DORMANT;
    }

    public static AspectTier byOrdinal(int ordinal) {
        AspectTier[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, ordinal))];
    }
}
