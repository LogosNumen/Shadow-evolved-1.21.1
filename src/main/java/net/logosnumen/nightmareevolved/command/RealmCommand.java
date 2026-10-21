package net.logosnumen.nightmareevolved.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.DreamTravel;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.RealmState;
import net.logosnumen.nightmareevolved.dream.gate.Citadels;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.law.Cloudveil;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * {@code /ne realm}, for operators exploring the Dream Realm: {@code go <place>} goes to a named place, {@code tp <x>
 * <z>} to any spot (on its ground, or high over open sky), and {@code here} names the region underfoot; {@code tear}
 * tears Godgrave's veil open ahead of you, and {@code seal}/{@code unseal} seal the Dark Sea away or let it loose again;
 * {@code dream}, {@code wake} and {@code cross} go in and out as sleeping, waking and crossing in body would, and
 * {@code state} shows what the soul's journeys have left in it; {@code bind} binds the Citadel of the Gateway beside
 * you, as kneeling at it would, and {@code release} lets your Citadel go.
 */
final class RealmCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private RealmCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("realm")
            .then(Commands.literal("go").then(Commands.argument("place", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Places.ALL.stream().map(Places.Place::id), b))
                .executes(RealmCommand::go)))
            .then(Commands.literal("tp").then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer())
                .executes(c -> tp(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "x"), IntegerArgumentType.getInteger(c, "z"))))))
            .then(Commands.literal("here").executes(RealmCommand::here))
            .then(Commands.literal("tear").executes(RealmCommand::tear))
            .then(Commands.literal("seal").executes(c -> seal(c, true)))
            .then(Commands.literal("unseal").executes(c -> seal(c, false)))
            .then(Commands.literal("dream").executes(c -> DreamTravel.dream(c.getSource().getPlayerOrException()) ? 1 : 0))
            .then(Commands.literal("wake").executes(c -> {
                DreamTravel.wake(c.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("state").executes(c -> {
                ServerPlayer player = c.getSource().getPlayerOrException();
                c.getSource().sendSuccess(() -> Component.literal(Soul.of(player).dream().toString()), false);
                LOGGER.info("Dream state of {}: {}", player.getName().getString(), Soul.of(player).dream());
                return 1;
            }))
            .then(Commands.literal("cross").executes(c -> {
                DreamTravel.cross(c.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("bind").executes(RealmCommand::bind))
            .then(Commands.literal("release").executes(c -> {
                Citadels.release(c.getSource().getPlayerOrException());
                return 1;
            }));
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

    /** Tears Godgrave's veil open a little way ahead of you. */
    private static int tear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Vec3 ahead = player.position().add(player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(30.0));
        Cloudveil.open(player.serverLevel(), ahead.x, ahead.z, 14.0F);
        return 1;
    }

    /** Seals the Dark Sea away, or lets it loose again. */
    private static int seal(CommandContext<CommandSourceStack> context, boolean sealed) {
        RealmState.get(context.getSource().getServer()).setDarkSeaSealed(sealed);
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.realm." + (sealed ? "sealed" : "unsealed")), true);
        return 1;
    }

    /** Binds the Citadel of the Gateway beside you to your soul, as kneeling at it would. */
    private static int bind(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Gateways.Gateway gateway = DreamRealm.in(player) ? Gateways.at(player.serverLevel(), player.blockPosition()) : null;
        if (gateway == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.realm.no_gateway"));
            return 0;
        }

        return Citadels.bind(player, gateway) ? 1 : 0;
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
