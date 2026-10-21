package net.logosnumen.nightmareevolved.command;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.logosnumen.nightmareevolved.anim.Poses;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /ne}, for operators: {@code colossus <kind> [seed]} raises a colossus, {@code soul ...} and {@code trial ...}
 * shape your soul and its trials, {@code memory ...} and {@code echo ...} tend its Memories and Echoes,
 * {@code vfx <shape> <palette>} plays an effect in front of you, and {@code pose <clip>} plays a body animation on you.
 */
public final class NeCommands {
    private static final List<String> SHAPES = List.of(
        "burst", "flash", "ring", "glyphs", "shockwave", "beam", "slash", "cut", "pillar", "dome", "cone", "spiral", "smoke", "aura", "embers", "shards",
        "sigil", "wall", "halo", "vortex", "trail"
    );
    private static final List<String> CLIPS = List.of(
        "cast", "cast_both", "raise", "raise_one", "slash", "slash_down", "spin", "roar", "breath", "slam", "dash", "guard", "channel", "channel_self",
        "write", "point", "flap", "cloak", "rise", "transform_gather", "transform_burst"
    );

    private NeCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("ne")
                .requires(source -> source.hasPermission(2))
                .then(ColossusCommand.node())
                .then(SoulCommand.soul())
                .then(SoulCommand.trial())
                .then(MemoryCommand.memory())
                .then(MemoryCommand.echo())
                .then(
                    Commands.literal("vfx")
                        .then(
                            Commands.argument("shape", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(SHAPES, builder))
                                .then(
                                    Commands.argument("palette", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                            Arrays.stream(Vfx.Palette.values()).map(p -> p.name().toLowerCase(Locale.ROOT)), builder
                                        ))
                                        .executes(NeCommands::vfx)
                                )
                        )
                )
                .then(
                    Commands.literal("pose")
                        .then(
                            Commands.argument("clip", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(CLIPS, builder))
                                .executes(NeCommands::pose)
                        )
                )
        );
    }

    private static int pose(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Poses.play(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "clip"));
        return 1;
    }

    private static int vfx(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String shape = StringArgumentType.getString(context, "shape");
        Vfx.Palette palette;
        try {
            palette = Vfx.Palette.valueOf(StringArgumentType.getString(context, "palette").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.vfx.palette"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        Vec3 ahead = player.position().add(player.getLookAngle().multiply(3.0, 0.0, 3.0));
        Vec3 chest = ahead.add(0.0, 1.2, 0.0);
        switch (shape) {
            case "burst" -> Vfx.burst(level, chest, palette, 30, 0.3);
            case "flash" -> Vfx.flash(level, chest, palette, 2.0F);
            case "ring" -> Vfx.ring(level, ahead, 2.0, palette, 32);
            case "glyphs" -> Vfx.glyphs(level, chest, 1.0, palette, 8);
            case "shockwave" -> Vfx.shockwave(level, player.position(), 6.0, palette);
            case "beam" -> Vfx.beam(level, player.getEyePosition(), player.getEyePosition().add(player.getLookAngle().scale(8.0)), palette, 0.25);
            case "slash" -> Vfx.slash(level, player, 1.8, palette, 140);
            case "cut" -> Vfx.slash(level, player, 1.8, palette, 140, 35);
            case "pillar" -> Vfx.pillar(level, ahead, 3.0, palette);
            case "dome" -> Vfx.dome(level, ahead, 2.0, palette, 60);
            case "cone" -> Vfx.cone(level, player.getEyePosition(), player.getLookAngle(), 7.0, 20.0, palette, 50, palette == Vfx.Palette.FIRE);
            case "spiral" -> Vfx.spiral(level, ahead, 0.8, 2.4, palette, 40);
            case "smoke" -> Vfx.smoke(level, chest, palette.deep, 16, 0.6);
            case "aura" -> Vfx.aura(level, player, palette, 24);
            case "embers" -> Vfx.embers(level, chest, palette, 24, 0.6);
            case "shards" -> Vfx.shards(level, chest, palette, 16);
            case "sigil" -> Vfx.sigil(level, ahead, 2.5, palette, 60);
            case "wall" -> Vfx.sigilFacing(level, chest, 1.5, palette, player.getLookAngle(), 60);
            case "halo" -> Vfx.halo(level, ahead, 2.0, palette.main, 40);
            case "vortex" -> Vfx.over(40, tick -> Vfx.vortex(level, ahead, 2.0, 2.5, palette, tick / 40.0, tick));
            case "trail" -> Vfx.trail(level, player.position(), ahead.add(player.getLookAngle().scale(4.0)), palette);
            default -> {
                context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.vfx.shape", String.join(", ", SHAPES)));
                return 0;
            }
        }

        return 1;
    }
}
