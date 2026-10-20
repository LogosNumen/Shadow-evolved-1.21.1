package net.logosnumen.nightmareevolved.command;

import java.util.Arrays;
import java.util.Locale;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.AspectTier;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Operator tools for the soul and its trials: {@code /ne soul rank|aspect|saturate|essence|reset} sets the caller's
 * soul directly, and {@code /ne trial begin <trial>|win} enters any trial or wins the one under way.
 */
final class SoulCommand {
    private SoulCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> soul() {
        return Commands.literal("soul")
            .then(Commands.literal("rank").then(
                Commands.argument("rank", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Rank.values()).map(Rank::id), b))
                    .executes(SoulCommand::rank)
            ))
            .then(Commands.literal("aspect").then(
                Commands.argument("aspect", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Aspects.all().stream().map(Aspect::id), b))
                    .executes(c -> aspect(c, false))
                    .then(Commands.argument("tier", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(AspectTier.values()).map(t -> t.name().toLowerCase(Locale.ROOT)), b))
                        .executes(c -> aspect(c, true)))
            ))
            .then(Commands.literal("saturate").executes(SoulCommand::saturate))
            .then(Commands.literal("essence").executes(SoulCommand::essence))
            .then(Commands.literal("reset").executes(SoulCommand::reset));
    }

    static LiteralArgumentBuilder<CommandSourceStack> trial() {
        return Commands.literal("trial")
            .then(Commands.literal("begin").then(
                Commands.argument("trial", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Trial.values()).map(Trial::id), b))
                    .executes(SoulCommand::begin)
            ))
            .then(Commands.literal("win").executes(c -> Nightmares.forceWin(c.getSource().getPlayerOrException()) ? 1 : 0));
    }

    private static int rank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(context, "rank");
        for (Rank rank : Rank.values()) {
            if (rank.id().equals(id)) {
                Soul.setRank(player, rank);
                context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.soul.rank", rank.title()), false);
                return 1;
            }
        }

        context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_rank", id));
        return 0;
    }

    private static int aspect(CommandContext<CommandSourceStack> context, boolean withTier) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Aspect aspect = Aspects.byId(StringArgumentType.getString(context, "aspect"));
        if (aspect == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_aspect"));
            return 0;
        }

        AspectTier tier = AspectTier.DORMANT;
        if (withTier) {
            try {
                tier = AspectTier.valueOf(StringArgumentType.getString(context, "tier").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_tier"));
                return 0;
            }
        }

        Aspects.grant(player, aspect, tier);
        return 1;
    }

    private static int saturate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        soul.setSaturation(soul.capacity());
        Soul.sync(player);
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.soul.saturated"), false);
        return 1;
    }

    /** Fills the caller's essence and readies every ability at once. */
    private static int essence(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        soul.setEssence(soul.maxEssence());
        for (Aspect aspect : Aspects.all()) {
            aspect.abilities().forEach(ability -> soul.setReadyAt(ability.id(), 0L));
        }

        Soul.sync(player);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        soul.setAspect("", AspectTier.DORMANT);
        soul.setRetryAt(0L);
        Soul.setRank(player, Rank.ASPIRANT);
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.soul.reset"), false);
        return 1;
    }

    private static int begin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(context, "trial");
        for (Trial trial : Trial.values()) {
            if (trial.id().equals(id)) {
                return Nightmares.begin(player, trial) ? 1 : 0;
            }
        }

        context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.trial.unknown", id));
        return 0;
    }
}
