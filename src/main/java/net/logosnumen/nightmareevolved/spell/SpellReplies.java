package net.logosnumen.nightmareevolved.spell;

import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** What the Spell says at the turns of a soul's road, and the answers it offers. */
public final class SpellReplies {
    private SpellReplies() {
    }

    /** The first time the Spell finds a player: it names them an Aspirant and tells them their trial waits in sleep. */
    public static void greet(ServerPlayer player) {
        Spell.say(player, "spell.nightmare_evolved.greeting");
        Spell.say(player, "spell.nightmare_evolved.greeting_trial");
        offer(player);
    }

    /** Every soul core is full: the next trial can be faced. */
    public static void saturated(ServerPlayer player) {
        Spell.say(player, "spell.nightmare_evolved.saturated");
        offer(player);
    }

    /** Offers the next trial as a line to click (or the player can simply sleep). */
    public static void offer(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        Trial trial = Trial.next(soul.rank());
        if (trial == null) {
            return;
        }

        player.sendSystemMessage(
            Component.empty()
                .append(Spell.choice("spell.nightmare_evolved.answer", "/spell accept"))
                .append(Component.literal("  "))
                .append(Component.translatable("spell.nightmare_evolved.or_sleep", trial.title()).withStyle(style -> style.withColor(0x8A8098)))
        );
    }
}
