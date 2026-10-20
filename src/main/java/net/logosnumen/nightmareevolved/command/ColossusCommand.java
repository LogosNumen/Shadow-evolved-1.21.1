package net.logosnumen.nightmareevolved.command;

import java.util.Arrays;
import java.util.Locale;
import javax.annotation.Nullable;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * {@code /ne colossus <kind> [seed]}: raises a colossus a little way in front of the caller, a random variant or the
 * one a seed names, and reports the seed so a variant worth keeping can be raised again.
 */
final class ColossusCommand {
    private ColossusCommand() {
    }

    /** The {@code colossus} branch of {@code /ne}. */
    static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("colossus")
            .then(
                Commands.argument("kind", StringArgumentType.word())
                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(ColossusKind.values()).map(kind -> kind.id), builder))
                    .executes(context -> raise(context, null))
                    .then(Commands.argument("seed", IntegerArgumentType.integer()).executes(context -> raise(context, IntegerArgumentType.getInteger(context, "seed"))))
            );
    }

    private static int raise(CommandContext<CommandSourceStack> context, @Nullable Integer seed) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "kind").toLowerCase(Locale.ROOT);
        ColossusKind kind = ColossusKind.byId(name);
        if (kind == null) {
            for (ColossusKind k : ColossusKind.values()) {
                if (k.name().equalsIgnoreCase(name)) {
                    kind = k;
                }
            }
        }

        if (kind == null) {
            source.sendFailure(Component.translatable("commands.nightmare_evolved.colossus.unknown", name));
            return 0;
        }

        ServerLevel level = source.getLevel();
        ColossusEntity colossus = Colossi.typeOf(kind).create(level);
        if (colossus == null) {
            return 0;
        }

        Vec3 facing = Vec3.directionFromRotation(0.0F, source.getRotation().y);
        Vec3 at = source.getPosition().add(facing.scale(10.0));
        colossus.moveTo(at.x, at.y, at.z, source.getRotation().y + 180.0F, 0.0F);
        colossus.setSeed(seed != null ? seed : level.random.nextInt());
        EventHooks.finalizeMobSpawn(colossus, level, level.getCurrentDifficultyAt(BlockPos.containing(at)), MobSpawnType.COMMAND, null);
        level.addFreshEntity(colossus);
        int used = colossus.variant().seed();
        source.sendSuccess(() -> Component.translatable("commands.nightmare_evolved.colossus.summoned", colossus.getDisplayName(), used), true);
        return 1;
    }
}
