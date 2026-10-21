package net.logosnumen.nightmareevolved.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.echo.EchoRecord;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.memory.Family;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soulsea.SoulSea;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * {@code /ne memory}, {@code /ne echo} and {@code /ne sea}, for operators testing Memories, Echoes and the Soul Sea:
 * <ul>
 * <li>{@code memory give <memory> [rank] [tier]} puts a Memory in your soul; {@code memory weave <shape> <family> <rank>
 * <tier>} has the Spell weave one; {@code memory shards} gives all seven Shard Memories; {@code memory summon
 * <memory>|all} summons them; {@code memory list}, {@code memory dismiss} and {@code memory clear} do what they say.</li>
 * <li>{@code echo give <creature> [rank] [class]} freezes a creature of that kind into an Echo; {@code echo summon}
 * calls out every one that will come; {@code echo list} and {@code echo clear}.</li>
 * <li>{@code sea enter|leave} goes down into your Soul Sea or comes back at once; {@code sea citadel <citadel>|none}
 * binds a Citadel to your soul; {@code sea shadow <creature>} stands the shadow of a slain creature in it.</li>
 * </ul>
 */
final class MemoryCommand {
    private static final List<String> CITADELS = List.of("bright_castle", "crimson_spire", "ivory_tower", "sanctuary_of_noctis", "night_temple",
        "black_skull", "vanishing_lake", "red_hill", "ravenheart", "bastion", "sorrow", "rivergate", "night_garden", "black_obelisk", "none");

    private MemoryCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> memory() {
        return Commands.literal("memory")
            .then(Commands.literal("give").then(Commands.argument("memory", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Memories.named().stream().map(d -> d.id), b))
                .executes(c -> give(c, -1, -1))
                .then(Commands.argument("rank", IntegerArgumentType.integer(0, 6))
                    .executes(c -> give(c, IntegerArgumentType.getInteger(c, "rank"), -1))
                    .then(Commands.argument("tier", IntegerArgumentType.integer(1, 7))
                        .executes(c -> give(c, IntegerArgumentType.getInteger(c, "rank"), IntegerArgumentType.getInteger(c, "tier")))))))
            .then(Commands.literal("weave").then(Commands.argument("shape", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Memories.all().stream().filter(d -> !d.named).map(d -> d.id), b))
                .then(Commands.argument("family", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Family.values()).map(Family::id), b))
                    .then(Commands.argument("rank", IntegerArgumentType.integer(0, 6))
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 7)).executes(MemoryCommand::weave))))))
            .then(Commands.literal("shards").executes(MemoryCommand::shards))
            .then(Commands.literal("summon").then(Commands.argument("memory", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Memories.all().stream().map(d -> d.id), b))
                .executes(MemoryCommand::summon)))
            .then(Commands.literal("list").executes(MemoryCommand::list))
            .then(Commands.literal("dismiss").executes(c -> {
                SoulMemories.dismissAll(c.getSource().getPlayerOrException(), false);
                return 1;
            }))
            .then(Commands.literal("clear").executes(MemoryCommand::clear));
    }

    static LiteralArgumentBuilder<CommandSourceStack> echo() {
        return Commands.literal("echo")
            .then(Commands.literal("give").then(Commands.argument("creature", StringArgumentType.greedyString())
                .suggests((c, b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b))
                .executes(MemoryCommand::echo)))
            .then(Commands.literal("list").executes(MemoryCommand::echoes))
            .then(Commands.literal("summon").executes(c -> {
                ServerPlayer player = c.getSource().getPlayerOrException();
                int called = 0;
                for (EchoRecord record : List.copyOf(Soul.of(player).echoes())) {
                    called += Echoes.summon(player, record.id) ? 1 : 0;
                }

                return called;
            }))
            .then(Commands.literal("clear").executes(c -> {
                ServerPlayer player = c.getSource().getPlayerOrException();
                Echoes.dismissAll(player);
                Soul.of(player).echoes().clear();
                Soul.of(player).seaChanged();
                return 1;
            }));
    }

    static LiteralArgumentBuilder<CommandSourceStack> sea() {
        return Commands.literal("sea")
            .then(Commands.literal("enter").executes(c -> {
                SoulSea.enter(c.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("leave").executes(c -> {
                SoulSea.leave(c.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("citadel").then(Commands.argument("citadel", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(CITADELS, b))
                .executes(c -> {
                    String id = StringArgumentType.getString(c, "citadel");
                    Soul.of(c.getSource().getPlayerOrException()).setCitadel("none".equals(id) ? "" : id);
                    c.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.sea.citadel", id), false);
                    return 1;
                })))
            .then(Commands.literal("shadow").then(Commands.argument("creature", StringArgumentType.greedyString())
                .suggests((c, b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b))
                .executes(MemoryCommand::standShadow)));
    }

    private static int give(CommandContext<CommandSourceStack> context, int rank, int tier) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MemoryDef def = Memories.byId(StringArgumentType.getString(context, "memory"));
        if (def == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.memory.unknown"));
            return 0;
        }

        MemoryData data = Memories.data(def, player.getUUID());
        if (rank >= 0) {
            data = new MemoryData(data.id(), data.def(), data.name(), rank, tier > 0 ? tier : data.tier(), data.enchants(), data.owner(), 0);
        }

        return SoulMemories.grant(player, data, true) != null ? 1 : 0;
    }

    private static int weave(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MemoryDef def = Memories.byId(StringArgumentType.getString(context, "shape"));
        Family family;
        try {
            family = Family.valueOf(StringArgumentType.getString(context, "family").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            family = Family.STRANGE;
        }

        if (def == null || def.named) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.memory.unknown"));
            return 0;
        }

        MemoryData data = Memories.roll(def, family, IntegerArgumentType.getInteger(context, "rank"), IntegerArgumentType.getInteger(context, "tier"),
            player.getUUID(), player.getRandom());
        return SoulMemories.grant(player, data, true) != null ? 1 : 0;
    }

    /** Summons every Memory of a kind the soul holds ("all" for every one), paying for each as usual. */
    private static int summon(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(context, "memory");
        int summoned = 0;
        for (MemoryRecord record : List.copyOf(Soul.of(player).memories())) {
            if (("all".equals(id) || id.equals(record.data().def())) && !record.anyOut()) {
                summoned += SoulMemories.summon(player, record.id, -1) ? 1 : 0;
            }
        }

        return summoned;
    }

    private static int shards(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        for (MemoryDef shard : Memories.SHARDS) {
            SoulMemories.grant(player, shard, true);
        }

        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SoulData soul = Soul.of(context.getSource().getPlayerOrException());
        for (MemoryRecord record : soul.memories()) {
            MemoryData data = record.data();
            context.getSource().sendSuccess(() -> SoulMemories.title(record.pieces.get(0).stack).copy()
                .append(Component.literal(" " + (record.anyOut() ? "out" : "resting") + " " + data.enchants())), false);
        }

        return soul.memories().size();
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        for (MemoryRecord record : List.copyOf(Soul.of(player).memories())) {
            SoulMemories.destroy(player, record.id, true);
        }

        return 1;
    }

    private static int echo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String[] parts = StringArgumentType.getString(context, "creature").trim().split("\\s+");
        EntityType<?> type = EntityType.byString(parts[0]).orElse(null);
        Entity made = type == null ? null : type.create(player.serverLevel());
        if (!(made instanceof Mob mob)) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.echo.unknown"));
            return 0;
        }

        if (parts.length >= 3) {
            try {
                Creatures.raise(mob, CreatureRank.values()[Integer.parseInt(parts[1])], CreatureClass.values()[Integer.parseInt(parts[2])], false, -1L);
            } catch (RuntimeException e) {
                context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.echo.unknown"));
                return 0;
            }
        }

        mob.moveTo(player.getX(), player.getY(), player.getZ());
        EchoRecord record = Echoes.grant(player, mob, true);
        mob.discard();
        return record != null ? 1 : 0;
    }

    private static int echoes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SoulData soul = Soul.of(context.getSource().getPlayerOrException());
        for (EchoRecord record : soul.echoes()) {
            context.getSource().sendSuccess(() -> Echoes.name(record).append(Component.literal(" " + (record.shadow ? "shadow " : "") + (record.out ? "out" : "resting")
                + " " + Math.round(record.health * 100) + "% " + record.traits)), false);
        }

        return soul.echoes().size();
    }

    private static int standShadow(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = ResourceLocation.tryParse(StringArgumentType.getString(context, "creature").trim());
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.echo.unknown"));
            return 0;
        }

        SoulData soul = Soul.of(player);
        ListTag shadows = Echoes.standing(soul);
        shadows.add(StringTag.valueOf(id.toString()));
        soul.abilityState().put("Shadows", shadows);
        soul.seaChanged();
        return 1;
    }
}
