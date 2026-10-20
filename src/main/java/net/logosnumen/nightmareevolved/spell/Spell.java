package net.logosnumen.nightmareevolved.spell;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * The voice of the Nightmare Spell: the bracketed lines it speaks into a mind. Everything it says is in the language
 * file (original wording), shown in its own pale violet so it never reads as ordinary chat.
 */
public final class Spell {
    public static final int COLOUR = 0xB9A7E8;
    private static final Style VOICE = Style.EMPTY.withColor(TextColor.fromRgb(COLOUR));

    private Spell() {
    }

    /** A line of the Spell's, wrapped in its brackets. */
    public static MutableComponent line(String key, Object... args) {
        return Component.literal("[").withStyle(VOICE).append(Component.translatable(key, args).withStyle(VOICE)).append(Component.literal("]").withStyle(VOICE));
    }

    /** Says a line to one player, with the faint chime that comes with the Spell's voice. */
    public static void say(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(line(key, args));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 0.6F);
    }

    /** A line the player can click to answer, running a command of theirs. */
    public static MutableComponent choice(String key, String command) {
        return Component.translatable(key).withStyle(style -> style
            .withColor(ChatFormatting.LIGHT_PURPLE)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(command))));
    }
}
