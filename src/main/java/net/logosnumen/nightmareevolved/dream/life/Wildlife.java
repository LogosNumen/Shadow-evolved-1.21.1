package net.logosnumen.nightmareevolved.dream.life;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.RealmState;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.law.DarkSea;
import net.logosnumen.nightmareevolved.entity.creature.CreatureTypes;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Dream Realm's creatures, coming and going around the souls in it. Every two seconds, each soul there may draw
 * a few more of the creatures that haunt the ground it stands in: on the ground near its own height (so the hollows and
 * the Underworld have theirs too), in the air above, or in the water, by day or by night as each kind keeps, up to a
 * dozen nearby. They wander off and are gone again when no one is near. Named creatures keep lairs (see {@link Lairs}).
 */
public final class Wildlife {
    private static final int PERIOD = 40;
    private static final int CAP = 12;
    private static final double NEAR = 72.0;

    private Wildlife() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % PERIOD != 0) {
            return;
        }

        ServerLevel realm = server.getLevel(DreamRealm.DIMENSION);
        if (realm == null) {
            return;
        }

        for (ServerPlayer player : realm.players()) {
            if (player.isSpectator()) {
                continue;
            }

            Lairs.visit(realm, player);
            if (!realm.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                continue;
            }

            int near = realm.getEntitiesOfClass(DreamCreature.class, player.getBoundingBox().inflate(NEAR), c -> !c.keepsLair()).size();
            if (near < CAP) {
                populate(realm, player, realm.random);
            }
        }
    }

    /** Whether it is night in the Dream Realm. */
    public static boolean night(ServerLevel realm) {
        long t = realm.getDayTime() % 24000L;
        return t >= 13000L && t < 23000L;
    }

    /** The floor nearest a height in a column: solid underfoot, with room above. */
    @Nullable
    public static BlockPos floor(ServerLevel level, int x, int y, int z, int up, int down) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = up; dy >= -down; dy--) {
            pos.set(x, y + dy, z);
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                // Branches are no floor: keep looking for the ground beneath them.
                continue;
            }

            if (state.isFaceSturdy(level, pos, Direction.UP) && level.isEmptyBlock(pos.above()) && level.isEmptyBlock(pos.above(2))) {
                return pos.above().immutable();
            }
        }

        return null;
    }

    private static void populate(ServerLevel realm, ServerPlayer player, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 24.0 + random.nextDouble() * 40.0;
        int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
        if (!realm.isLoaded(new BlockPos(x, player.getBlockY(), z))) {
            return;
        }

        BlockPos floor = floor(realm, x, player.getBlockY(), z, 16, 32);
        BlockPos water = waterAt(realm, x, player.getBlockY(), z);
        Region here = RealmMap.part(x, floor != null ? floor.getY() : player.getBlockY(), z);
        boolean night = night(realm);
        List<Kind> kinds = new ArrayList<>();
        List<Kind.Haunt> haunts = new ArrayList<>();
        collect(here, kinds, haunts);
        if (haunts.isEmpty() && here.whole() != here) {
            // A part of a region without creatures of its own (the Sea of Ash, the barrows) shares the whole's.
            collect(here.whole(), kinds, haunts);
        }

        int total = 0;
        for (int i = 0; i < haunts.size(); i++) {
            Kind.Haunt haunt = haunts.get(i);
            if (fits(haunt, night, floor, water) && haunt.reaches(x, z)) {
                total += haunt.weight();
            }
        }

        if (total <= 0) {
            return;
        }

        int roll = random.nextInt(total);
        for (int i = 0; i < haunts.size(); i++) {
            Kind.Haunt haunt = haunts.get(i);
            if (!fits(haunt, night, floor, water) || !haunt.reaches(x, z)) {
                continue;
            }

            roll -= haunt.weight();
            if (roll < 0) {
                BlockPos at = switch (haunt.ground()) {
                    case SURFACE -> floor;
                    case AIR -> floor == null ? null : floor.above(4 + random.nextInt(10));
                    case WATER -> water;
                };
                if (at != null && !(haunt.ground() == Kind.Ground.SURFACE && underDarkSea(realm, at))) {
                    spawnGroup(realm, kinds.get(i), haunt, at, random);
                }

                return;
            }
        }
    }

    private static void collect(Region region, List<Kind> kinds, List<Kind.Haunt> haunts) {
        haunts.addAll(Kinds.haunts(region, kinds));
    }

    private static boolean fits(Kind.Haunt haunt, boolean night, @Nullable BlockPos floor, @Nullable BlockPos water) {
        if (haunt.time() == Kind.Time.DAY && night || haunt.time() == Kind.Time.NIGHT && !night) {
            return false;
        }

        return haunt.ground() == Kind.Ground.WATER ? water != null : floor != null;
    }

    @Nullable
    private static BlockPos waterAt(ServerLevel level, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 8; dy >= -24; dy--) {
            pos.set(x, y + dy, z);
            if (level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.below()).is(FluidTags.WATER)) {
                return pos.below().immutable();
            }
        }

        return null;
    }

    /** Whether the Dark Sea covers a place now (land creatures keep clear of it). */
    private static boolean underDarkSea(ServerLevel realm, BlockPos at) {
        return DarkSea.under(at.getX(), at.getY(), at.getZ(), realm.getDayTime(), RealmState.get(realm.getServer()).darkSeaSealed());
    }

    private static void spawnGroup(ServerLevel realm, Kind kind, Kind.Haunt haunt, BlockPos at, RandomSource random) {
        int count = haunt.min() + random.nextInt(Math.max(1, haunt.max() - haunt.min() + 1));
        for (int i = 0; i < count; i++) {
            int x = at.getX() + (i == 0 ? 0 : random.nextInt(9) - 4);
            int z = at.getZ() + (i == 0 ? 0 : random.nextInt(9) - 4);
            BlockPos spot = haunt.ground() == Kind.Ground.SURFACE ? floor(realm, x, at.getY(), z, 4, 6) : new BlockPos(x, at.getY(), z);
            if (spot != null) {
                spawn(realm, kind, spot, MobSpawnType.NATURAL);
            }
        }
    }

    /** Brings one creature of a kind into the world at a place, if it fits there; null if it doesn't. */
    @Nullable
    public static DreamCreature spawn(ServerLevel level, Kind kind, BlockPos at, MobSpawnType reason) {
        DreamCreature creature = CreatureTypes.type(kind).create(level);
        if (creature == null) {
            return null;
        }

        // Rough ground (ash drifts, coral, rubble) catches large bodies: try a little higher before giving up.
        float yaw = level.random.nextFloat() * 360.0F;
        boolean fits = false;
        for (int lift = 0; lift <= 3 && !fits; lift++) {
            creature.moveTo(at.getX() + 0.5, at.getY() + lift, at.getZ() + 0.5, yaw, 0.0F);
            fits = level.noCollision(creature);
        }

        if (!fits) {
            creature.discard();
            return null;
        }

        EventHooks.finalizeMobSpawn(creature, level, level.getCurrentDifficultyAt(at), reason, null);
        level.addFreshEntity(creature);
        return creature;
    }
}
