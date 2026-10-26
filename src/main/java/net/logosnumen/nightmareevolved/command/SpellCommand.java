package net.logosnumen.nightmareevolved.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.DreamState;
import net.logosnumen.nightmareevolved.dream.gate.Fealty;
import net.logosnumen.nightmareevolved.dream.gate.GatewayRecords;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.spell.SpellReplies;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /spell}, for every player: {@code accept} answers the Spell's call and enters the next trial (the same as
 * sleeping when ready), {@code status} asks where the soul stands, and {@code runes} has the Spell recite them in chat
 * (the R key shows them on a screen). {@code swear <sovereign>} offers a Saint's fealty, {@code receive <saint>}
 * accepts it, and {@code renounce} breaks one's oath (or, for a Sovereign, {@code renounce <saint>} releases a vassal).
 */
public final class SpellCommand {
    private SpellCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("spell")
                .then(Commands.literal("accept").executes(SpellCommand::accept))
                .then(Commands.literal("status").executes(SpellCommand::status))
                .then(Commands.literal("runes").executes(SpellCommand::runes))
                .then(Commands.literal("swear").then(Commands.argument("sovereign", EntityArgument.player())
                    .executes(c -> Fealty.swear(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "sovereign")) ? 1 : 0)))
                .then(Commands.literal("receive").then(Commands.argument("saint", EntityArgument.player())
                    .executes(c -> Fealty.receive(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "saint")) ? 1 : 0)))
                .then(Commands.literal("renounce")
                    .executes(c -> Fealty.renounce(c.getSource().getPlayerOrException()) ? 1 : 0)
                    .then(Commands.argument("vassal", StringArgumentType.word())
                        .executes(c -> Fealty.release(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "vassal")) ? 1 : 0)))
        );
    }

    private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return Nightmares.begin(context.getSource().getPlayerOrException()) ? 1 : 0;
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.rank", soul.rank().title(), soul.rank().tier()));
        if (soul.cores() > 0) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.essence", Math.round(soul.essence()), Math.round(soul.maxEssence())));
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.cores", soul.cores(), soul.fragments(), soul.capacity()));
        }

        DreamState dream = DreamState.of(player);
        if (!dream.anchor().isEmpty() && player.getServer() != null) {
            ServerLevel realm = player.getServer().getLevel(DreamRealm.DIMENSION);
            Gateways.Gateway anchor = realm == null ? null : Gateways.byId(realm, dream.anchor());
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.anchor", anchor != null ? anchor.title()
                : Component.translatable("spell.nightmare_evolved.status.anchor_lost")));
        }

        if (player.getServer() != null) {
            GatewayRecords.Oath oath = GatewayRecords.get(player.getServer()).oath(player.getUUID());
            if (oath != null) {
                player.sendSystemMessage(Spell.line("spell.nightmare_evolved.status.liege", oath.liege().name()));
            }
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

    private static int runes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        player.sendSystemMessage(Spell.line("runes.nightmare_evolved.name", player.getName()));
        player.sendSystemMessage(Spell.line("runes.nightmare_evolved.rank", soul.rank().title().copy().append(" (").append(soul.rank().tier()).append(")")));
        Aspect aspect = Aspects.byId(soul.aspect());
        if (aspect == null) {
            player.sendSystemMessage(Spell.line("runes.nightmare_evolved.no_aspect"));
            return 1;
        }

        player.sendSystemMessage(Spell.line("runes.nightmare_evolved.aspect", aspect.title().copy().append(" (").append(aspect.tier().title()).append(")")));
        for (Ability ability : aspect.abilities()) {
            boolean awake = soul.rank().atLeast(ability.stage.unlock);
            Component label = ability.innate ? Component.translatable("runes.nightmare_evolved.innate") : ability.stage.title();
            player.sendSystemMessage(Component.literal("  ").append(label).append(": ")
                .append(ability.title().copy().withStyle(awake ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY)));
        }

        Flaw flaw = Flaw.byId(soul.flaw());
        if (flaw != null) {
            player.sendSystemMessage(Spell.line("runes.nightmare_evolved.flaw", flaw.title()));
        }

        for (String id : soul.attributes()) {
            SoulAttribute attribute = SoulAttribute.byId(id);
            if (attribute != null) {
                player.sendSystemMessage(Component.literal("  [").append(attribute.title()).append("]"));
            }
        }

        return 1;
    }
}
