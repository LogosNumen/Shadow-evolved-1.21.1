package net.logosnumen.nightmareevolved.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.spell.SpellReplies;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /spell}, for every player: {@code accept} answers the Spell's call and enters the next trial (the same as
 * sleeping when ready), and {@code status} asks the Spell what it knows of your soul.
 */
public final class SpellCommand {
    private SpellCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("spell")
                .then(Commands.literal("accept").executes(SpellCommand::accept))
                .then(Commands.literal("status").executes(SpellCommand::status))
        );
    }

    private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return Nightmares.begin(context.getSource().getPlayerOrException()) ? 1 : 0;
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.rank", soul.rank().title(), soul.rank().tier()));
        Aspect aspect = Aspects.byId(soul.aspect());
        if (aspect != null) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.aspect", aspect.title(), soul.aspectTier().title(), aspect.flawTitle()));
        }

        if (soul.rank().cores > 0) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.essence", Math.round(soul.essence()), Math.round(soul.maxEssence())));
            player.sendSystemMessage(Spell.line(
                "spell.nightmare_evolved.status.cores", soul.rank().cores, Math.round(soul.saturation()), Math.round(soul.capacity())
            ));
        }

        Trial trial = Trial.next(soul.rank());
        if (trial != null) {
            Component refusal = Nightmares.refusal(player);
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.next", trial.title()));
            if (refusal != null) {
                player.sendSystemMessage(refusal);
            } else {
                SpellReplies.offer(player);
            }
        }

        return 1;
    }
}
