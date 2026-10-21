package net.logosnumen.nightmareevolved.dream.seed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.CreatureProfile;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.GatewayRecords;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Seeds of Nightmare: how they are found, how they call, how they are challenged, how they bloom, and what is left
 * when they are destroyed.
 * <ul>
 * <li>A Seed found (anyone coming near it in the Dream Realm) begins to ripen. Left alone, it blooms, and a Gate opens in
 * the waking world (see {@link NightmareGateEntity}). The novel's own Seeds are not ripe enough ever to bloom.</li>
 * <li>Every Seed sends out the Call of Nightmare, and the Awakened feel it: the stronger the soul, the farther it
 * reaches and the more maddening it is.</li>
 * <li>A Seed of Category N holds an Nth Nightmare: challenging it takes a soul ready for that Nightmare into it. Before
 * or after it blooms, conquering it destroys it; a Gateway is left in its place, and its Gate, if open, closes.</li>
 * <li>The Dark Seed in the Ivory Tower was grown from the chains that bound a daemon. Entering it without the ivory
 * knife from the Night Temple's black altar, or the obsidian knife from the Sanctuary's white one, is certain death:
 * the chains will not yield to an ordinary blade.</li>
 * </ul>
 */
public final class Seeds {
    /** How long a found Seed takes to bloom: six days. */
    public static final long BLOOM = 6L * 24000L;
    /** How long before it blooms the waking world feels a Gate forming. */
    private static final long WARNING = 2400L;
    private static final Map<UUID, Long> WARNED = new HashMap<>();
    private static final Map<String, Long> WARNINGS = new HashMap<>();

    private Seeds() {
    }

    // ---- Each tick --------------------------------------------------------------------------------------------------

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel realm = server.getLevel(DreamRealm.DIMENSION);
        if (realm == null || server.getTickCount() % 100 != 0) {
            return;
        }

        SeedRecords records = SeedRecords.get(server);
        RealmTerrain terrain = RealmTerrain.of(realm);
        long now = realm.getGameTime();
        for (ServerPlayer player : realm.players()) {
            if (player.isSpectator()) {
                continue;
            }

            for (SeedSites.Site site : SeedSites.near(terrain, player.getX(), player.getZ(), 96.0)) {
                SeedRecords.Seed seed = records.find(site, now, canon(site) ? Long.MAX_VALUE : now + BLOOM);
                settle(realm, seed);
            }

            call(player, terrain, records);
        }

        if (!server.getGameRules().getBoolean(DreamRealm.GATES)) {
            return;
        }

        for (SeedRecords.Seed seed : List.copyOf(records.all())) {
            if (seed.conquered || seed.bloomed || seed.bloomAt == Long.MAX_VALUE) {
                continue;
            }

            if (now >= seed.bloomAt) {
                bloom(server, realm, seed);
            } else if (seed.bloomAt - now <= WARNING && !WARNINGS.containsKey(seed.id)) {
                WARNINGS.put(seed.id, now);
                for (ServerPlayer player : server.overworld().players()) {
                    if (Soul.of(player).rank().atLeast(Rank.AWAKENED)) {
                        tell(player, "gate_forming", seed.category);
                    }
                }
            }
        }
    }

    private static boolean canon(SeedSites.Site site) {
        return SeedSites.CANON.contains(site);
    }

    /**
     * The Call of Nightmare: every so often, an Awakened near an untouched Seed hears it from that direction, the
     * stronger the soul, the farther away.
     */
    private static void call(ServerPlayer player, RealmTerrain terrain, SeedRecords records) {
        SoulData soul = Soul.of(player);
        if (!soul.rank().atLeast(Rank.AWAKENED)) {
            return;
        }

        double range = 160.0 + 120.0 * (soul.rank().ordinal() - Rank.AWAKENED.ordinal());
        SeedSites.Site nearest = null;
        double best = Double.MAX_VALUE;
        for (SeedSites.Site site : SeedSites.near(terrain, player.getX(), player.getZ(), range)) {
            SeedRecords.Seed seed = records.get(site.id());
            double d = Math.hypot(site.pos().getX() - player.getX(), site.pos().getZ() - player.getZ());
            if ((seed == null || !seed.conquered) && d < best) {
                best = d;
                nearest = site;
            }
        }

        if (nearest == null || player.getRandom().nextInt(3) != 0) {
            return;
        }

        Vec3 toward = Vec3.atCenterOf(nearest.pos()).subtract(player.position()).multiply(1.0, 0.0, 1.0).normalize().scale(12.0);
        Vec3 from = player.position().add(toward);
        float strength = (float)Mth.clamp(1.0 - best / range, 0.15, 1.0);
        player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvents.SCULK_SHRIEKER_SHRIEK), SoundSource.AMBIENT, from.x, from.y + 1.0,
            from.z, 0.25F + strength * 0.5F, 0.5F, player.getRandom().nextLong()));
        long now = player.level().getGameTime();
        Long last = WARNED.get(player.getUUID());
        if (last == null || now - last > 1200) {
            WARNED.put(player.getUUID(), now);
            player.displayClientMessage(Component.translatable("seed.nightmare_evolved.call", nearest.category(), direction(toward))
                .withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }

    private static Component direction(Vec3 toward) {
        double angle = Math.toDegrees(Math.atan2(toward.z, toward.x));
        String[] names = {"east", "southeast", "south", "southwest", "west", "northwest", "north", "northeast"};
        int index = Math.floorMod((int)Math.round(angle / 45.0), 8);
        return Component.translatable("seed.nightmare_evolved.direction." + names[index]);
    }

    // ---- Challenge ----------------------------------------------------------------------------------------------------

    /**
     * A soul lays its hand on a Seed: if it is ready for the Nightmare inside, it is taken into it. Too weak, or already
     * past it, and the Seed has nothing to say to it.
     */
    public static void challenge(ServerPlayer player, BlockPos pos, BlockState state) {
        ServerLevel level = player.serverLevel();
        SeedSites.Site site = DreamRealm.is(level) ? SeedSites.at(RealmTerrain.of(level), pos) : null;
        int category = site != null ? site.category() : state.getValue(SeedBlock.CATEGORY);
        String id = site != null ? site.id() : "seed_at_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
        SeedRecords records = SeedRecords.get(player.server);
        SeedRecords.Seed seed = records.get(id);
        if (seed != null && seed.conquered) {
            return;
        }

        SoulData soul = Soul.of(player);
        Trial trial = NightmareGateEntity.trial(category);
        Trial next = Trial.next(soul.rank());
        if (!soul.rank().atLeast(Rank.AWAKENED)) {
            tell(player, "not_awakened", category);
            return;
        }

        if (next == null || next.ordinal() > trial.ordinal()) {
            tell(player, "beneath", category);
            return;
        }

        if (next.ordinal() < trial.ordinal()) {
            tell(player, "beyond", category);
            return;
        }

        if ("dark_seed".equals(id) && !knives(player)) {
            Long warned = WARNED.get(player.getUUID());
            if (warned == null || player.level().getGameTime() - warned > 200) {
                WARNED.put(player.getUUID(), player.level().getGameTime());
                tell(player, "dark_seed_warning", category);
                return;
            }
        }

        if (site != null) {
            records.find(site, level.getGameTime(), canon(site) ? Long.MAX_VALUE : level.getGameTime() + BLOOM);
        }

        level.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0F, 0.5F);
        Vfx.burst(level, Vec3.atCenterOf(pos), Vfx.Palette.SHADOW, 30, 0.2);
        Nightmares.begin(player, trial, id);
    }

    /** Whether a soul carries a knife that can cut the Dark Seed's chains. */
    public static boolean knives(ServerPlayer player) {
        return player.getInventory().hasAnyMatching(stack -> stack.is(SeedEntities.IVORY_KNIFE.get()) || stack.is(SeedEntities.OBSIDIAN_KNIFE.get()));
    }

    /** Inside the Dark Seed, its guardian cannot be harmed but with one of the two knives; with both, all the more. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !Creatures.ofNightmare(victim)) {
            return;
        }

        CreatureProfile profile = Creatures.profile(victim);
        if (!profile.guardian() || !"dark_seed".equals(Nightmares.seedOf(profile.trial()))) {
            return;
        }

        boolean ivory = player.getInventory().hasAnyMatching(stack -> stack.is(SeedEntities.IVORY_KNIFE.get()));
        boolean obsidian = player.getInventory().hasAnyMatching(stack -> stack.is(SeedEntities.OBSIDIAN_KNIFE.get()));
        if (!ivory && !obsidian) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("seed.nightmare_evolved.chains_hold").withStyle(ChatFormatting.DARK_RED), true);
        } else if (ivory && obsidian) {
            event.setAmount(event.getAmount() * 1.5F);
        }
    }

    // ---- Bloom, Gate, conquest --------------------------------------------------------------------------------------

    /** The Seed blooms: a Gate opens in the waking world, near someone who is there to see it. */
    private static void bloom(MinecraftServer server, ServerLevel realm, SeedRecords.Seed seed) {
        ServerLevel waking = server.overworld();
        List<ServerPlayer> there = waking.players().stream().filter(p -> !p.isSpectator()).toList();
        if (there.isEmpty()) {
            // No one in the waking world for it to open near: it waits a while longer.
            seed.bloomAt += 6000L;
            SeedRecords.get(server).changed();
            return;
        }

        ServerPlayer near = there.get(waking.random.nextInt(there.size()));
        BlockPos at = gateSpot(waking, near.blockPosition(), waking.random);
        seed.bloomed = true;
        SeedRecords.get(server).changed();
        settle(realm, seed);
        NightmareGateEntity.open(waking, at, seed.id, seed.category);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tell(player, "bloomed", seed.category);
        }
    }

    /** Opens a Seed's Gate now (for operators). */
    public static void forceBloom(ServerPlayer player, SeedRecords.Seed seed) {
        ServerLevel realm = player.server.getLevel(DreamRealm.DIMENSION);
        seed.bloomed = true;
        SeedRecords.get(player.server).changed();
        if (realm != null) {
            settle(realm, seed);
        }

        NightmareGateEntity.open(player.server.overworld(), gateSpot(player.server.overworld(), player.blockPosition(), player.getRandom()), seed.id,
            seed.category);
    }

    private static BlockPos gateSpot(ServerLevel level, BlockPos near, RandomSource random) {
        double a = random.nextDouble() * Math.PI * 2.0;
        double r = 40.0 + random.nextDouble() * 60.0;
        int x = near.getX() + (int)(Math.cos(a) * r);
        int z = near.getZ() + (int)(Math.sin(a) * r);
        level.getChunk(x >> 4, z >> 4);
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
    }

    /** A Gate closed: its Guardian slain (the Seed will ripen again), or its Seed conquered. */
    public static void gateClosed(MinecraftServer server, String id, boolean conquered) {
        SeedRecords records = SeedRecords.get(server);
        SeedRecords.Seed seed = records.get(id);
        if (seed == null) {
            return;
        }

        seed.bloomed = false;
        if (!conquered) {
            seed.bloomAt = server.overworld().getGameTime() + BLOOM;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tell(player, "gate_closed", seed.category);
            }
        }

        records.changed();
        ServerLevel realm = server.getLevel(DreamRealm.DIMENSION);
        if (realm != null) {
            settle(realm, seed);
        }
    }

    /**
     * A Seed's Nightmare was conquered: the Seed is destroyed, a Gateway takes its place, and its Gate, if one is open,
     * closes.
     */
    public static void conquered(MinecraftServer server, String id) {
        SeedRecords records = SeedRecords.get(server);
        SeedRecords.Seed seed = records.get(id);
        if (seed == null || seed.conquered) {
            return;
        }

        seed.conquered = true;
        seed.bloomed = false;
        records.changed();
        String citadel = "dark_seed".equals(id) ? "ivory_tower" : null;
        GatewayRecords.get(server).addBorn(new Gateways.Gateway(id, seed.region, seed.pos, GatewayBlock.Form.HEART, citadel, false));
        ServerLevel realm = server.getLevel(DreamRealm.DIMENSION);
        if (realm != null) {
            settle(realm, seed);
        }

        for (ServerLevel level : server.getAllLevels()) {
            for (NightmareGateEntity gate : level.getEntities(SeedEntities.GATE.get(), g -> g.seed().equals(id))) {
                gate.close(level);
            }
        }
    }

    /** Brings a Seed's blocks into line with its record: bloomed, or gone with a Gateway in its place. */
    private static void settle(ServerLevel realm, SeedRecords.Seed seed) {
        if (!realm.isLoaded(seed.pos)) {
            return;
        }

        for (BlockPos pos : BlockPos.betweenClosed(seed.pos.offset(-1, 0, -1), seed.pos.offset(1, 3, 3))) {
            BlockState state = realm.getBlockState(pos);
            if (!state.is(DreamBlocks.SEED.get())) {
                continue;
            }

            if (seed.conquered) {
                BlockState left = pos.equals(seed.pos) ? DreamBlocks.GATEWAY.get().form(GatewayBlock.Form.HEART)
                    : "tomb_of_ariel".equals(seed.id) ? DreamBlocks.EBONY.get().defaultBlockState() : Blocks.AIR.defaultBlockState();
                realm.setBlock(pos, left, Block.UPDATE_ALL);
            } else if (state.getValue(SeedBlock.BLOOMED) != seed.bloomed) {
                realm.setBlock(pos, state.setValue(SeedBlock.BLOOMED, seed.bloomed), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Where an Awakened drawn by an open Gate's Call comes into the Dream Realm: some way from its Seed, on safe ground. */
    @Nullable
    public static Vec3 drawnTo(ServerPlayer player, ServerLevel realm) {
        NightmareGateEntity gate = null;
        for (NightmareGateEntity g : player.serverLevel().getEntities(SeedEntities.GATE.get(), g -> g.calls(player.position()))) {
            gate = g;
        }

        SeedRecords.Seed seed = gate == null ? null : SeedRecords.get(player.server).get(gate.seed());
        if (seed == null) {
            return null;
        }

        RealmTerrain terrain = RealmTerrain.of(realm);
        RandomSource random = player.getRandom();
        for (int tries = 0; tries < 40; tries++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double d = 100.0 + random.nextDouble() * 150.0;
            int x = (int)Math.floor(seed.pos.getX() + Math.cos(a) * d);
            int z = (int)Math.floor(seed.pos.getZ() + Math.sin(a) * d);
            Column column = terrain.column(x, z);
            int top = column.topSolid();
            if (top > Column.MIN_Y + 20 && column.top() == top) {
                return new Vec3(x + 0.5, top + 1, z + 0.5);
            }
        }

        return Vec3.atBottomCenterOf(seed.pos.above(3));
    }

    public static void tell(ServerPlayer player, String key, Object... args) {
        Spell.say(player, "seed.nightmare_evolved." + key, args);
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        WARNED.clear();
        WARNINGS.clear();
    }
}
