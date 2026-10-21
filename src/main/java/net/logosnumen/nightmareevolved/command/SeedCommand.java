package net.logosnumen.nightmareevolved.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.seed.SeedRecords;
import net.logosnumen.nightmareevolved.dream.seed.SeedSites;
import net.logosnumen.nightmareevolved.dream.seed.Seeds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /ne seed}, for operators: {@code near [range]} lists the Seeds of Nightmare around you (by your position in
 * the Dream Realm, wherever you are), {@code go <id>} takes you to one, {@code bloom <id>} makes one bloom now and open
 * its Gate near you, and {@code conquer <id>} counts it as conquered.
 */
final class SeedCommand {
    private SeedCommand() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("seed")
            .then(Commands.literal("near")
                .executes(c -> near(c, 1500))
                .then(Commands.argument("range", IntegerArgumentType.integer(16, 20000)).executes(c -> near(c, IntegerArgumentType.getInteger(c, "range")))))
            .then(Commands.literal("go").then(Commands.argument("id", StringArgumentType.word()).executes(SeedCommand::go)))
            .then(Commands.literal("bloom").then(Commands.argument("id", StringArgumentType.word()).executes(SeedCommand::bloom)))
            .then(Commands.literal("conquer").then(Commands.argument("id", StringArgumentType.word()).executes(c -> {
                Seeds.conquered(c.getSource().getServer(), StringArgumentType.getString(c, "id"));
                return 1;
            })));
    }

    @Nullable
    private static RealmTerrain terrain(CommandSourceStack source) {
        ServerLevel realm = source.getServer().getLevel(DreamRealm.DIMENSION);
        return realm == null ? null : RealmTerrain.of(realm);
    }

    private static int near(CommandContext<CommandSourceStack> context, int range) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        RealmTerrain terrain = terrain(context.getSource());
        if (terrain == null) {
            return 0;
        }

        List<SeedSites.Site> sites = SeedSites.near(terrain, player.getX(), player.getZ(), range).stream()
            .sorted(Comparator.comparingDouble(s -> s.pos().distSqr(player.blockPosition()))).toList();
        SeedRecords records = SeedRecords.get(player.server);
        for (SeedSites.Site site : sites) {
            SeedRecords.Seed seed = records.get(site.id());
            String state = seed == null ? "unfound" : seed.conquered ? "conquered" : seed.bloomed ? "bloomed" : "found";
            context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.seed.line", site.id(), site.category(),
                site.region().title(), site.pos().getX(), site.pos().getY(), site.pos().getZ(), state), false);
        }

        int count = sites.size();
        context.getSource().sendSuccess(() -> Component.translatable("commands.nightmare_evolved.seed.count", count, range), false);
        return count;
    }

    @Nullable
    private static SeedSites.Site site(CommandContext<CommandSourceStack> context) {
        RealmTerrain terrain = terrain(context.getSource());
        String id = StringArgumentType.getString(context, "id");
        if (terrain == null) {
            return null;
        }

        for (SeedSites.Site site : SeedSites.CANON) {
            if (site.id().equals(id)) {
                return site;
            }
        }

        String[] parts = id.split("_");
        if (parts.length == 3 && parts[0].equals("seed")) {
            try {
                return SeedSites.site(terrain, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private static int go(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SeedSites.Site site = site(context);
        if (site == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.seed.unknown"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel realm = player.server.getLevel(DreamRealm.DIMENSION);
        if (realm == null) {
            return 0;
        }

        realm.getChunk(site.pos().getX() >> 4, site.pos().getZ() >> 4);
        player.teleportTo(realm, site.pos().getX() + 0.5, site.pos().getY() + 3.0, site.pos().getZ() + 3.5, 180.0F, 20.0F);
        return 1;
    }

    private static int bloom(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SeedSites.Site site = site(context);
        if (site == null) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.seed.unknown"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        SeedRecords records = SeedRecords.get(player.server);
        long now = player.server.overworld().getGameTime();
        SeedRecords.Seed seed = records.find(site, now, now);
        if (seed.conquered) {
            context.getSource().sendFailure(Component.translatable("commands.nightmare_evolved.seed.conquered"));
            return 0;
        }

        Seeds.forceBloom(player, seed);
        return 1;
    }
}
