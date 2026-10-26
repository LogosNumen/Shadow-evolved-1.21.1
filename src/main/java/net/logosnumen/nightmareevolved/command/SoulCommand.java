package net.logosnumen.nightmareevolved.command;

import java.util.Arrays;
import java.util.List;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Awakening;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.dream.DreamTravel;
import net.logosnumen.nightmareevolved.dream.gate.Citadels;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.flaw.FlawEvents;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Operator tools for the soul and its trials. {@code /ne soul} sets the caller's Rank, Aspect (with its own Flaw, or a
 * chosen one), Flaw, Attributes, cores and fragments; fills essence; uses an Aspect ability by its step; or resets the soul
 * to an Aspirant's.
 * {@code /ne trial begin <trial>|win} enters any trial or wins the one under way.
 */
final class SoulCommand {
    private SoulCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> soul() {
        return Commands.literal("soul")
            .then(Commands.literal("rank").then(Commands.argument("rank", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Rank.values()).map(Rank::id), b))
                .executes(SoulCommand::rank)))
            .then(Commands.literal("aspect").then(Commands.argument("aspect", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Aspects.all().stream().map(Aspect::id), b))
                .executes(c -> aspect(c, false))
                .then(Commands.argument("flaw", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Flaw.values()).map(Flaw::id), b))
                    .executes(c -> aspect(c, true)))))
            .then(Commands.literal("flaw").then(Commands.argument("flaw", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Flaw.values()).map(Flaw::id), b))
                .executes(SoulCommand::flaw)))
            .then(Commands.literal("attribute")
                .then(Commands.literal("add").then(Commands.argument("attribute", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(SoulAttribute.values()).map(SoulAttribute::id), b))
                    .executes(c -> attribute(c, true))))
                .then(Commands.literal("remove").then(Commands.argument("attribute", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(SoulAttribute.values()).map(SoulAttribute::id), b))
                    .executes(c -> attribute(c, false)))))
            .then(Commands.literal("cores").then(Commands.argument("cores", IntegerArgumentType.integer(1, 7)).executes(SoulCommand::cores)))
            .then(Commands.literal("fragments").then(Commands.argument("fragments", IntegerArgumentType.integer(0)).executes(SoulCommand::fragments)))
            .then(Commands.literal("saturate").executes(SoulCommand::saturate))
            .then(Commands.literal("essence").executes(SoulCommand::essence))
            .then(Commands.literal("reset").executes(SoulCommand::reset))
            .then(Commands.literal("use").then(Commands.argument("stage", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Stage.values()).map(Stage::id), b))
                .executes(SoulCommand::use)));
    }

    static LiteralArgumentBuilder<CommandSourceStack> trial() {
        return Commands.literal("trial")
            .then(Commands.literal("begin").then(Commands.argument("trial", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Trial.values()).map(Trial::id), b))
                .executes(SoulCommand::begin)))
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

    /** Grants an Aspect with its own Flaw (or the one named), keeping the soul's Attributes if it has any. */
    private static int aspect(CommandContext<CommandSourceStack> context, boolean withFlaw) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Aspect aspect = Aspects.byId(StringArgumentType.getString(context, "aspect"));
        if (aspect == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_aspect"));
            return 0;
        }

        String flaw = withFlaw ? StringArgumentType.getString(context, "flaw") : Awakening.rollFlaw(aspect, player.getRandom());
        if (Flaw.byId(flaw) == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_flaw"));
            return 0;
        }

        SoulData soul = Soul.of(player);
        List<String> attributes = soul.attributes().isEmpty() ? Awakening.rollAttributes(aspect, player.getRandom()) : soul.attributes();
        Awakening.grant(player, aspect, flaw, attributes);
        return 1;
    }

    private static int flaw(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Flaw flaw = Flaw.byId(StringArgumentType.getString(context, "flaw"));
        if (flaw == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_flaw"));
            return 0;
        }

        Soul.of(player).setFlaw(flaw.id());
        FlawEvents.hideName(player, flaw == Flaw.OVERLOOKED);
        player.refreshDisplayName();
        player.refreshTabListName();
        Soul.applyStats(player);
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.soul.flaw", flaw.title()), false);
        return 1;
    }

    private static int attribute(CommandContext<CommandSourceStack> context, boolean add) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulAttribute attribute = SoulAttribute.byId(StringArgumentType.getString(context, "attribute"));
        if (attribute == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_attribute"));
            return 0;
        }

        SoulData soul = Soul.of(player);
        if (add) {
            soul.addAttribute(attribute.id());
        } else {
            soul.removeAttribute(attribute.id());
        }

        Soul.applyStats(player);
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.soul.attribute." + (add ? "added" : "removed"),
            attribute.title()), false);
        return 1;
    }

    private static int cores(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Soul.of(player).setCores(IntegerArgumentType.getInteger(context, "cores"));
        Soul.applyStats(player);
        return 1;
    }

    private static int fragments(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Soul.of(player).setFragments(IntegerArgumentType.getInteger(context, "fragments"));
        Soul.applyStats(player);
        return 1;
    }

    private static int saturate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        soul.setFragments(soul.capacity());
        Soul.applyStats(player);
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.soul.saturated"), false);
        return 1;
    }

    /** Fills the caller's essence and readies every ability at once. */
    private static int essence(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        soul.clearCooldowns();
        soul.setEssence(soul.maxEssence());
        Soul.sync(player);
        return 1;
    }

    /** Uses the caller's Aspect ability of a step, as its key would (a Transformation is taken on or let go). */
    private static int use(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(context, "stage");
        for (Stage stage : Stage.values()) {
            if (stage.id().equals(id)) {
                return Aspects.use(player, stage.ordinal()) ? 1 : 0;
            }
        }

        context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.soul.no_stage"));
        return 0;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulData soul = Soul.of(player);
        DreamTravel.wake(player);
        soul.resetDream();
        Citadels.release(player);
        soul.setAspect("", soul.aspectTier());
        soul.setFlaw("");
        soul.clearAttributes();
        soul.setRetryAt(0L);
        FlawEvents.hideName(player, false);
        player.refreshDisplayName();
        player.refreshTabListName();
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
