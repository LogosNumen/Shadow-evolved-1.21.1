package net.logosnumen.nightmareevolved.dream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.law.DarkSea;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEntities;
import net.logosnumen.nightmareevolved.entity.soulsea.VesselEntity;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Going into the Dream Realm and coming out of it, as the Spell has it.
 * <ul>
 * <li>A Sleeper's next sleep after the First Nightmare drops them somewhere in the Dream Realm at random. Their body
 * stays asleep in the waking world, and only their soul's own things (Memories, Echoes) come with them. The only way
 * home is a Gateway; passing through one makes them Awakened, and the Gateway becomes their anchor.</li>
 * <li>From then on, every sleep takes them to their anchor, and any Gateway takes them home (and becomes their new
 * anchor). Whatever they gathered in the Dream Realm stays there, waiting for their next dream; whatever they carried in
 * the waking world stays with their sleeping body.</li>
 * <li>The sleeping body can be found and struck. If it is, the dreamer wakes at once, and the blow lands.</li>
 * <li>A Master can cross over in body at will, belongings and all, leaving a tether to return to. A Saint is a living
 * Gateway: those beside them who wish it are carried along, and they can tether themselves in the Dream Realm too.</li>
 * </ul>
 */
public final class DreamTravel {
    /** How long passing through a Gateway takes: the body glows brighter until it fades away like a summoned thing. */
    public static final int PASSAGE = 60;
    /** How long a Master must hold still to cross over in body. */
    public static final int CROSSING = 100;
    private static final TicketType<ChunkPos> BODY = TicketType.create("nightmare_evolved_dreamer", Comparator.comparingLong(ChunkPos::toLong), 60);
    private static final Map<UUID, Passage> PASSING = new HashMap<>();
    private static final Map<UUID, Crossing> CROSSING_OVER = new HashMap<>();

    private record Passage(String gateway, BlockPos at, long start) {
    }

    private record Crossing(Vec3 at, long start) {
    }

    private DreamTravel() {
    }

    public static boolean dreaming(Player player) {
        return DreamState.of(player).dreaming();
    }

    // ---- Going in ---------------------------------------------------------------------------------------------------

    /**
     * A sleeper's soul goes into the Dream Realm: to its anchor if it has one, otherwise somewhere at random. Its body
     * is left asleep where it lay.
     */
    public static boolean dream(ServerPlayer player) {
        ServerLevel realm = player.server.getLevel(DreamRealm.DIMENSION);
        if (realm == null || DreamRealm.in(player) || dreaming(player)) {
            return false;
        }

        SoulData soul = Soul.of(player);
        DreamState state = DreamState.of(player);
        Arrival arrival = destination(realm, player, soul, state, null);
        BlockPos bed = player.getSleepingPos().orElse(player.blockPosition());
        player.stopSleepInBed(true, true);
        SoulMemories.dismissAll(player, true);
        Echoes.dismissAll(player);
        VesselEntity body = SeaEntities.VESSEL.get().create(player.serverLevel());
        CompoundTag back = point(player);
        if (body != null) {
            body.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            body.setOwner(player);
            body.dream(player, bed);
            player.serverLevel().addFreshEntity(body);
            back.putUUID("Body", body.getUUID());
        }

        state.setReturn(back, false);
        if (separate(player)) {
            swap(player, state, true);
        }

        arrive(player, realm, arrival);
        return true;
    }

    /** Where a soul comes into the Dream Realm, and what to tell it. */
    public record Arrival(Vec3 pos, String message, Component where) {
    }

    private static Arrival destination(ServerLevel realm, ServerPlayer player, SoulData soul, DreamState state, @Nullable ServerPlayer saint) {
        if (saint != null) {
            Gateways.Gateway anchor = Gateways.byId(realm, DreamState.of(saint).anchor());
            if (anchor != null) {
                return new Arrival(Vec3.atBottomCenterOf(Gateways.arrival(realm, anchor)), "spell.nightmare_evolved.dream_carried", anchor.title());
            }
        }

        Gateways.Gateway anchor = state.anchor().isEmpty() ? null : Gateways.byId(realm, state.anchor());
        if (anchor != null) {
            return new Arrival(Vec3.atBottomCenterOf(Gateways.arrival(realm, anchor)), "spell.nightmare_evolved.dream_anchor", anchor.title());
        }

        if (!state.anchor().isEmpty()) {
            // The anchor was destroyed: the Spell carries the soul to a Gateway at random, as on its first journey.
            List<Gateways.Gateway> all = Gateways.all(realm);
            Gateways.Gateway any = all.get(player.getRandom().nextInt(all.size()));
            return new Arrival(Vec3.atBottomCenterOf(Gateways.arrival(realm, any)), "spell.nightmare_evolved.dream_anchor_lost", any.title());
        }

        Arrival random = random(RealmTerrain.of(realm), region -> Gateways.forArrival(realm, region), player.getRandom());
        // Only a Sleeper is told how to find their way out and Awaken; anyone else simply has no anchor yet.
        return soul.rank() == Rank.SLEEPER ? random : new Arrival(random.pos, "spell.nightmare_evolved.dream_unanchored", random.where);
    }

    /**
     * A Sleeper's first journey: a random region (never a Death Zone), a random place in it some way from one of its
     * Gateways, on dry, solid ground (and on the Shore, above where the Dark Sea comes at night).
     */
    public static Arrival random(RealmTerrain terrain, Function<Region, List<Gateways.Gateway>> gatewaysOf, RandomSource random) {
        List<Region> regions = new ArrayList<>();
        int total = 0;
        for (Region region : Region.values()) {
            if (region.arrival > 0 && !gatewaysOf.apply(region).isEmpty()) {
                regions.add(region);
                total += region.arrival;
            }
        }

        for (int attempt = 0; attempt < 6; attempt++) {
            int pick = random.nextInt(total);
            Region region = regions.get(0);
            for (Region r : regions) {
                pick -= r.arrival;
                if (pick < 0) {
                    region = r;
                    break;
                }
            }

            List<Gateways.Gateway> gateways = gatewaysOf.apply(region);
            Gateways.Gateway from = gateways.get(random.nextInt(gateways.size()));
            for (int tries = 0; tries < 48; tries++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = 450.0 + random.nextDouble() * 750.0;
                int x = (int)Math.floor(from.pos().getX() + Math.cos(a) * d);
                int z = (int)Math.floor(from.pos().getZ() + Math.sin(a) * d);
                if (RealmMap.part(x, 80, z).whole() != region.whole() || RealmMap.part(x, 80, z) == Region.LAND_OF_BARROWS) {
                    continue;
                }

                Column column = terrain.column(x, z);
                int top = column.topSolid();
                // Solid, dry ground, and on the Shore above where the Dark Sea comes at night.
                if (top < Column.MIN_Y + 20 || column.top() != top || (region == Region.FORGOTTEN_SHORE && top < DarkSea.HIGH + 2)) {
                    continue;
                }

                return new Arrival(new Vec3(x + 0.5, top + 1, z + 0.5), "spell.nightmare_evolved.dream_first", region.where());
            }
        }

        Gateways.Gateway fallback = Gateways.CANON.get(0);
        return new Arrival(Vec3.atBottomCenterOf(fallback.pos().above(2)), "spell.nightmare_evolved.dream_first", fallback.region().where());
    }

    private static void arrive(ServerPlayer player, ServerLevel realm, Arrival arrival) {
        realm.getChunk(BlockPos.containing(arrival.pos));
        player.teleportTo(realm, arrival.pos.x, arrival.pos.y, arrival.pos.z, player.getRandom().nextFloat() * 360.0F, 0.0F);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
        Vfx.burst(realm, Vfx.chest(player), Vfx.Palette.SOUL, 30, 0.15);
        realm.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        DreamState.of(player).visit();
        Spell.say(player, arrival.message, arrival.where);
    }

    // ---- Passing through a Gateway ----------------------------------------------------------------------------------

    /**
     * A soul in the Dream Realm reaches a Gateway (stands in its ring, touches its altar, steps into its pond, or comes
     * near the Night Temple's): the passage begins. Its body shines brighter and brighter, and then is gone.
     */
    public static void touch(ServerPlayer player, BlockPos pos) {
        if (!DreamRealm.in(player) || PASSING.containsKey(player.getUUID()) || player.isSpectator()) {
            return;
        }

        Gateways.Gateway gateway = Gateways.at(player.serverLevel(), pos);
        if (gateway == null) {
            return;
        }

        PASSING.put(player.getUUID(), new Passage(gateway.id(), pos, player.level().getGameTime()));
        player.level().playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 0.7F);
        Spell.say(player, "spell.nightmare_evolved.gateway_passing", gateway.title());
    }

    private static void passage(ServerPlayer player, Passage passage) {
        ServerLevel level = player.serverLevel();
        long elapsed = level.getGameTime() - passage.start;
        if (!DreamRealm.in(player) || player.position().distanceTo(Vec3.atCenterOf(passage.at)) > 4.5 || !player.isAlive()) {
            PASSING.remove(player.getUUID());
            return;
        }

        float glow = elapsed / (float)PASSAGE;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false));
        if (elapsed % 2 == 0) {
            Vfx.aura(level, player, Vfx.Palette.LIGHT, 2 + (int)(glow * 10));
        }

        if (elapsed < PASSAGE) {
            return;
        }

        PASSING.remove(player.getUUID());
        Gateways.Gateway gateway = Gateways.byId(level, passage.gateway);
        if (gateway == null) {
            return;
        }

        Vfx.flash(level, Vfx.chest(player), Vfx.Palette.LIGHT, 3.0F);
        Vfx.burst(level, Vfx.chest(player), Vfx.Palette.LIGHT, 40, 0.25);
        through(player, gateway);
    }

    /** Through the Gateway: a Sleeper Awakens; the Gateway becomes the soul's anchor; it wakes in the waking world. */
    public static void through(ServerPlayer player, Gateways.Gateway gateway) {
        SoulData soul = Soul.of(player);
        DreamState state = DreamState.of(player);
        boolean awakening = soul.rank() == Rank.SLEEPER;
        boolean newAnchor = !gateway.id().equals(state.anchor());
        state.setAnchor(gateway.id());
        wake(player);
        if (awakening) {
            Nightmares.awaken(player);
        } else if (newAnchor) {
            Spell.say(player, "spell.nightmare_evolved.anchor_changed", gateway.title());
        }
    }

    // ---- Coming back --------------------------------------------------------------------------------------------------

    /**
     * The soul comes back out of the Dream Realm: a dreamer wakes in their body (and leaves what they gathered behind,
     * waiting); one who crossed in body returns to their tether.
     */
    public static void wake(ServerPlayer player) {
        DreamState state = DreamState.of(player);
        if (!state.dreaming()) {
            return;
        }

        PASSING.remove(player.getUUID());
        SoulMemories.dismissAll(player, true);
        Echoes.dismissAll(player);
        boolean inBody = state.inBody();
        CompoundTag back = state.returnPoint();
        MinecraftServer server = player.server;
        ServerLevel target = level(server, back);
        VesselEntity body = target == null ? null : body(target, state);
        Vec3 pos;
        float yaw = back.getFloat("Yaw");
        if (body != null) {
            pos = body.position();
            body.wakeAndVanish();
        } else if (target != null && back.contains("X")) {
            pos = new Vec3(back.getDouble("X"), back.getDouble("Y"), back.getDouble("Z"));
        } else {
            target = server.overworld();
            BlockPos spawn = target.getSharedSpawnPos();
            pos = Vec3.atBottomCenterOf(target.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn));
        }

        if (!inBody && separate(player)) {
            swap(player, state, false);
        }

        state.clearReturn();
        player.teleportTo(target, pos.x, pos.y, pos.z, yaw, 0.0F);
        Vfx.burst(target, Vfx.chest(player), Vfx.Palette.SOUL, 24, 0.2);
        target.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 0.6F);
    }

    /** The body was struck while its soul was away dreaming: it wakes at once, and the blow lands. */
    public static boolean struck(VesselEntity body, DamageSource source, float amount) {
        UUID owner = body.owner();
        ServerPlayer player = owner == null || !(body.level() instanceof ServerLevel level) ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || !dreaming(player) || DreamState.of(player).inBody()) {
            return false;
        }

        player.displayClientMessage(Component.translatable("spell.nightmare_evolved.torn_awake").withStyle(ChatFormatting.RED), true);
        wake(player);
        player.invulnerableTime = 0;
        player.hurt(source, amount);
        return true;
    }

    // ---- Crossing over in body (Masters and higher) -----------------------------------------------------------------

    /** Whether a soul may cross between the worlds in body: a Master or higher. */
    public static boolean canCross(SoulData soul) {
        return soul.rank().atLeast(Rank.MASTER);
    }

    /** Begins crossing over (or back), for a soul strong enough: it must hold still while its tether takes hold. */
    public static void cross(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        if (!canCross(soul)) {
            Spell.say(player, "spell.nightmare_evolved.cannot_cross");
            return;
        }

        DreamState state = DreamState.of(player);
        if (state.dreaming() && !state.inBody()) {
            // A dreamer simply wakes.
            wake(player);
            return;
        }

        if (CROSSING_OVER.containsKey(player.getUUID())) {
            CROSSING_OVER.remove(player.getUUID());
            Spell.say(player, "spell.nightmare_evolved.crossing_stopped");
            return;
        }

        CROSSING_OVER.put(player.getUUID(), new Crossing(player.position(), player.level().getGameTime()));
        Spell.say(player, DreamRealm.in(player) ? "spell.nightmare_evolved.crossing_back" : "spell.nightmare_evolved.crossing");
    }

    private static void crossing(ServerPlayer player, Crossing crossing) {
        long elapsed = player.level().getGameTime() - crossing.start;
        if (player.position().distanceTo(crossing.at) > 1.5 || !player.isAlive()) {
            CROSSING_OVER.remove(player.getUUID());
            Spell.say(player, "spell.nightmare_evolved.crossing_stopped");
            return;
        }

        if (elapsed % 4 == 0) {
            Vfx.ring(player.serverLevel(), player.position().add(0.0, 0.1, 0.0), 1.2 + elapsed / (double)CROSSING, Vfx.Palette.SOUL, 16);
        }

        if (elapsed < CROSSING) {
            return;
        }

        CROSSING_OVER.remove(player.getUUID());
        SoulData soul = Soul.of(player);
        DreamState state = DreamState.of(player);
        ServerLevel realm = player.server.getLevel(DreamRealm.DIMENSION);
        if (realm == null) {
            return;
        }

        List<ServerPlayer> carried = carried(player, soul);
        if (DreamRealm.in(player)) {
            if (soul.rank().atLeast(Rank.SAINT)) {
                // A Saint's tether in the Dream Realm: where crossing over will bring them next time.
                state.setTether(true, point(player));
            }

            wake(player);
            for (ServerPlayer other : carried) {
                if (DreamState.of(other).dreaming()) {
                    wake(other);
                }
            }

            return;
        }

        CompoundTag tether = point(player);
        state.setTether(false, tether);
        Arrival arrival;
        CompoundTag realmTether = state.tether(true);
        if (soul.rank().atLeast(Rank.SAINT) && realmTether.contains("X")) {
            arrival = new Arrival(new Vec3(realmTether.getDouble("X"), realmTether.getDouble("Y"), realmTether.getDouble("Z")),
                "spell.nightmare_evolved.dream_tether", Component.translatable("spell.nightmare_evolved.your_tether"));
        } else {
            arrival = destination(realm, player, soul, state, null);
        }

        SoulMemories.dismissAll(player, true);
        Echoes.dismissAll(player);
        state.setReturn(tether, true);
        arrive(player, realm, arrival);
        for (ServerPlayer other : carried) {
            carry(other, player, realm, arrival);
        }
    }

    /** Those a Saint carries with them: anyone close beside them, crouching to show they wish to go. */
    private static List<ServerPlayer> carried(ServerPlayer saint, SoulData soul) {
        if (!soul.rank().atLeast(Rank.SAINT)) {
            return List.of();
        }

        List<ServerPlayer> carried = new ArrayList<>();
        for (ServerPlayer other : saint.serverLevel().players()) {
            if (other != saint && other.isShiftKeyDown() && other.distanceTo(saint) < 4.0 && !other.isSpectator()) {
                carried.add(other);
            }
        }

        return carried;
    }

    /** A Saint carries someone along into the Dream Realm: they come in body, without the Spell, beside the Saint. */
    private static void carry(ServerPlayer other, ServerPlayer saint, ServerLevel realm, Arrival arrival) {
        DreamState state = DreamState.of(other);
        if (state.dreaming()) {
            return;
        }

        SoulMemories.dismissAll(other, true);
        Echoes.dismissAll(other);
        state.setReturn(point(other), true);
        Vec3 beside = arrival.pos.add(other.getRandom().nextDouble() * 2.0 - 1.0, 0.0, other.getRandom().nextDouble() * 2.0 - 1.0);
        arrive(other, realm, new Arrival(beside, "spell.nightmare_evolved.dream_carried", saint.getDisplayName()));
    }

    // ---- Belongings -------------------------------------------------------------------------------------------------

    private static boolean separate(ServerPlayer player) {
        return player.serverLevel().getGameRules().getBoolean(DreamRealm.SEPARATE_BELONGINGS);
    }

    /**
     * Swaps what the soul carries for what it left on the other side: going in, the waking world's things stay with the
     * body and the dream's are taken up again; coming out, the reverse.
     */
    public static void swap(ServerPlayer player, DreamState state, boolean goingIn) {
        ListTag carried = player.getInventory().save(new ListTag());
        int experience = player.totalExperience;
        player.getInventory().clearContent();
        player.setExperiencePoints(0);
        player.setExperienceLevels(0);
        player.totalExperience = 0;
        if (goingIn) {
            state.setWaking(carried, experience);
            player.getInventory().load(state.dreamt());
            player.giveExperiencePoints(state.dreamtExperience());
            state.setDreamt(new ListTag(), 0);
        } else {
            state.setDreamt(carried, experience);
            player.getInventory().load(state.waking());
            player.giveExperiencePoints(state.wakingExperience());
            state.clearWaking();
        }
    }

    // ---- Bookkeeping ------------------------------------------------------------------------------------------------

    private static CompoundTag point(ServerPlayer player) {
        CompoundTag point = new CompoundTag();
        point.putString("Dimension", player.level().dimension().location().toString());
        point.putDouble("X", player.getX());
        point.putDouble("Y", player.getY());
        point.putDouble("Z", player.getZ());
        point.putFloat("Yaw", player.getYRot());
        return point;
    }

    @Nullable
    private static ServerLevel level(MinecraftServer server, CompoundTag point) {
        ResourceLocation id = ResourceLocation.tryParse(point.getString("Dimension"));
        return id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
    }

    @Nullable
    private static VesselEntity body(ServerLevel level, DreamState state) {
        UUID id = state.body();
        return id != null && level.getEntity(id) instanceof VesselEntity vessel && vessel.isAlive() ? vessel : null;
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Passage passage = PASSING.get(player.getUUID());
        if (passage != null) {
            passage(player, passage);
        }

        Crossing crossing = CROSSING_OVER.get(player.getUUID());
        if (crossing != null) {
            crossing(player, crossing);
        }

        DreamState state = DreamState.of(player);
        if (state.dreaming() && player.tickCount % 20 == 0) {
            keepBody(player, state);
            nearGateways(player);
        }
    }

    /** While its soul dreams, the ground the body sleeps on keeps ticking: what hunts there can still find it. */
    private static void keepBody(ServerPlayer player, DreamState state) {
        if (state.inBody()) {
            return;
        }

        CompoundTag back = state.returnPoint();
        ServerLevel level = level(player.server, back);
        if (level != null && back.contains("X")) {
            ChunkPos chunk = new ChunkPos(BlockPos.containing(back.getDouble("X"), back.getDouble("Y"), back.getDouble("Z")));
            level.getChunkSource().addRegionTicket(BODY, chunk, 3, chunk);
        }
    }

    /** The Night Temple's Gateway needs no touch: coming near its altar is enough. */
    private static void nearGateways(ServerPlayer player) {
        if (!DreamRealm.in(player)) {
            return;
        }

        for (Gateways.Gateway gateway : Gateways.CANON) {
            if (gateway.form() == GatewayBlock.Form.BLACK_ALTAR && gateway.pos().distToCenterSqr(player.position()) < 3.5 * 3.5) {
                touch(player, gateway.pos());
            }
        }
    }

    /**
     * Death in the Dream Realm, for a dreamer: the body asleep in the waking world wakes too, alone, with what it held;
     * whatever the dreamer carried falls where they died.
     */
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DreamState state = DreamState.of(player);
        if (!state.dreaming()) {
            return;
        }

        ServerLevel level = level(player.server, state.returnPoint());
        VesselEntity body = level == null ? null : body(level, state);
        if (body != null) {
            body.wakeAndVanish();
        }

        if (!state.inBody() && separate(player)) {
            player.getInventory().load(state.waking());
            player.giveExperiencePoints(state.wakingExperience());
            state.clearWaking();
        }

        state.clearReturn();
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PASSING.remove(event.getEntity().getUUID());
        CROSSING_OVER.remove(event.getEntity().getUUID());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        PASSING.clear();
        CROSSING_OVER.clear();
    }

    /** Whether a level is the waking world, for the Spell's sleep. */
    public static boolean waking(Level level) {
        return !DreamRealm.is(level) && !Nightmares.isNightmare(level);
    }
}
