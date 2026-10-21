package net.logosnumen.nightmareevolved.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /ne realm}, for operators exploring the Dream Realm: {@code go <place>} goes to a named place, {@code tp <x>
 * <z>} to any spot (on its ground, or high over open sky), and {@code here} names the region underfoot.
 */
final class RealmCommand {
    private RealmCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("realm")
            .then(Commands.literal("go").then(Commands.argument("place", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Places.ALL.stream().map(Places.Place::id), b))
                .executes(RealmCommand::go)))
            .then(Commands.literal("tp").then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer())
                .executes(c -> tp(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "x"), IntegerArgumentType.getInteger(c, "z"))))))
            .then(Commands.literal("here").executes(RealmCommand::here));
    }

    private static int go(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Places.Place place = Places.byId(StringArgumentType.getString(context, "place"));
        if (place == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.realm.unknown"));
            return 0;
        }

        return tp(context.getSource().getPlayerOrException(), place.x(), place.z());
    }

    static int tp(ServerPlayer player, int x, int z) {
        ServerLevel level = player.server.getLevel(DreamRealm.DIMENSION);
        if (level == null) {
            return 0;
        }

        int y = RealmTerrain.of(level).surface(x, z);
        if (y < Column.MIN_Y) {
            y = 220;
        }

        level.getChunk(x >> 4, z >> 4);
        player.teleportTo(level, x + 0.5, y + 1.0, z + 0.5, player.getYRot(), player.getXRot());
        return 1;
    }

    private static int here(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int x = player.getBlockX();
        int z = player.getBlockZ();
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.realm.here",
            RealmMap.part(x, player.getBlockY(), z).title(), x, z), false);
        return 1;
    }
}
