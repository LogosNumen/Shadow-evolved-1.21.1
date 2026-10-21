package net.logosnumen.nightmareevolved.soulsea;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.core.ModSounds;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEntities;
import net.logosnumen.nightmareevolved.entity.soulsea.VesselEntity;
import net.logosnumen.nightmareevolved.network.MeditationPayload;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Going down into one's own Soul Sea and coming back up.
 *
 * <p>As in the novel, the mind goes and the body stays: the player sinks into their sea by meditating (standing still,
 * out of a fight, for a few moments), and their body sits where they left it, aware of what goes on around it. If the
 * body is struck, they are torn back up into it at once. Rising again returns them to it, facing the way it faces.
 * Each soul has its own region of the shared Soul Sea dimension; anyone else found there is sent home.
 */
public final class SoulSea {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, NightmareEvolved.id("soul_sea"));
    /** The still surface is solid; souls stand on it, one block above. */
    public static final double SURFACE_Y = 65.0;
    /** How far from its heart a soul may wander before its sea turns it back. */
    public static final double BOUNDARY = 72.0;
    /** Where a soul arrives: south of its heart, so that its suns, its Memories and its Citadel are all ahead. */
    public static final double ARRIVAL = 18.0;
    /** How long it takes to sink into the sea. */
    public static final int MEDITATION = 60;
    /** How long after a blow a soul is too shaken to sink. */
    private static final int COMBAT = 100;
    private static final Map<UUID, Meditation> MEDITATING = new HashMap<>();
    /** Keeps the ground under a body ticking while its mind is away, so the world goes on around it. */
    private static final TicketType<ChunkPos> BODY = TicketType.create("nightmare_evolved_body", Comparator.comparingLong(ChunkPos::toLong), 60);

    private record Meditation(Vec3 start, long began) {
    }

    private SoulSea() {
    }

    public static boolean in(Entity entity) {
        return entity.level().dimension().equals(DIMENSION);
    }

    public static boolean meditating(ServerPlayer player) {
        return MEDITATING.containsKey(player.getUUID());
    }

    /** The key: begin to meditate, stop, or rise back out of the sea. */
    public static void toggle(ServerPlayer player) {
        if (in(player)) {
            leave(player);
            return;
        }

        if (MEDITATING.remove(player.getUUID()) != null) {
            PacketDistributor.sendToPlayer(player, new MeditationPayload(0));
            tell(player, "stopped", ChatFormatting.GRAY);
            return;
        }

        String problem = whyNot(player);
        if (problem != null) {
            tell(player, problem, ChatFormatting.RED);
            return;
        }

        MEDITATING.put(player.getUUID(), new Meditation(player.position(), player.level().getGameTime()));
        PacketDistributor.sendToPlayer(player, new MeditationPayload(MEDITATION));
        player.serverLevel().playSound(null, player.blockPosition(), ModSounds.SOUL_SEA_MEDITATE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        tell(player, "meditating", ChatFormatting.DARK_AQUA);
    }

    @Nullable
    private static String whyNot(ServerPlayer player) {
        if (Soul.of(player).rank() == Rank.ASPIRANT) {
            return "no_sea";
        } else if (Nightmares.isNightmare(player.level())) {
            return "in_nightmare";
        } else if (player.isPassenger() || player.isSleeping() || player.isFallFlying() || !player.onGround()) {
            return "not_now";
        } else if (fighting(player)) {
            return "fighting";
        } else if (player.server.getLevel(DIMENSION) == null) {
            return "missing";
        }

        return null;
    }

    private static boolean fighting(ServerPlayer player) {
        boolean hurt = player.getLastHurtByMob() != null && player.tickCount - player.getLastHurtByMobTimestamp() < COMBAT;
        boolean struck = player.getLastHurtMob() != null && player.tickCount - player.getLastHurtMobTimestamp() < COMBAT;
        return hurt || struck || player.hurtTime > 0;
    }

    public static void tick(ServerPlayer player) {
        Meditation meditation = MEDITATING.get(player.getUUID());
        if (meditation != null) {
            meditate(player, meditation);
        }

        if (in(player)) {
            if (player.tickCount % 20 == 0) {
                guard(player);
                keepBody(player);
            }

            if (player.tickCount % 40 == 0) {
                SeaPresence.refresh(player);
                SeaBuilder.raise(player, player.serverLevel(), SeaRegions.get(player.server).regionOf(player.getUUID()));
            }
        }
    }

    private static void meditate(ServerPlayer player, Meditation meditation) {
        boolean moved = player.position().distanceToSqr(meditation.start()) > 0.09;
        if (moved || player.hurtTime > 0 || player.isPassenger() || !player.isAlive()) {
            MEDITATING.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new MeditationPayload(0));
            tell(player, moved ? "moved" : "broken", ChatFormatting.RED);
            return;
        }

        long elapsed = player.level().getGameTime() - meditation.began();
        ServerLevel level = player.serverLevel();
        if (elapsed % 3 == 0) {
            // Motes of the soul spiral inward and up as it sinks.
            double progress = Math.min(1.0, (double)elapsed / MEDITATION);
            double radius = 1.4 * (1.0 - progress) + 0.2;
            for (int i = 0; i < 3; i++) {
                double angle = elapsed * 0.35 + i * (Math.PI * 2.0 / 3.0);
                Vfx.mote(level, new Vec3(player.getX() + Math.cos(angle) * radius, player.getY() + 0.2 + progress * 1.6,
                    player.getZ() + Math.sin(angle) * radius), Vfx.Palette.SOUL.main, 0.01);
            }
        }

        if (elapsed >= MEDITATION) {
            MEDITATING.remove(player.getUUID());
            enter(player);
        }
    }

    /** The mind goes down into its own region of the sea; the body stays behind, sitting where it was. */
    public static void enter(ServerPlayer player) {
        ServerLevel sea = player.server.getLevel(DIMENSION);
        if (sea == null || in(player)) {
            return;
        }

        SoulData soul = Soul.of(player);
        VesselEntity vessel = SeaEntities.VESSEL.get().create(player.serverLevel());
        if (vessel != null) {
            vessel.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 25.0F);
            vessel.setOwner(player);
            player.serverLevel().addFreshEntity(vessel);
        }

        CompoundTag back = new CompoundTag();
        back.putString("Dimension", player.level().dimension().location().toString());
        back.putDouble("X", player.getX());
        back.putDouble("Y", player.getY());
        back.putDouble("Z", player.getZ());
        back.putFloat("Yaw", player.getYRot());
        back.putFloat("Pitch", player.getXRot());
        if (vessel != null) {
            back.putUUID("Vessel", vessel.getUUID());
        }

        soul.setSeaReturn(back);
        soul.visitSea();
        int region = SeaRegions.get(player.server).regionOf(player.getUUID());
        Vfx.burst(player.serverLevel(), Vfx.chest(player), Vfx.Palette.SOUL, 24, 0.2);
        prepare(sea, region);
        // Arrive south of the sea's heart, facing it and looking up a little toward the suns.
        player.teleportTo(sea, SeaRegions.centerX(region), SURFACE_Y, SeaRegions.centerZ(region) + ARRIVAL, 180.0F, -18.0F);
        player.playNotifySound(ModSounds.SOUL_SEA_ENTER.get(), SoundSource.PLAYERS, 0.9F, 1.0F);
        SeaBuilder.raise(player, sea, region);
        SeaPresence.populate(player);
        if (soul.seaVisits() == 1) {
            Spell.say(player, "spell.nightmare_evolved.soul_sea_first");
        }
    }

    /** While the mind is in its sea, the ground its body sits on keeps ticking: what hunts there can still find it. */
    private static void keepBody(ServerPlayer player) {
        CompoundTag back = Soul.of(player).seaReturn();
        ResourceLocation id = ResourceLocation.tryParse(back.getString("Dimension"));
        ServerLevel level = id == null || !back.contains("X") ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (level != null) {
            ChunkPos chunk = new ChunkPos(BlockPos.containing(back.getDouble("X"), back.getDouble("Y"), back.getDouble("Z")));
            level.getChunkSource().addRegionTicket(BODY, chunk, 3, chunk);
        }
    }

    /** Generates the chunks around a region's heart before anyone arrives, so its figures land in loaded ground. */
    private static void prepare(ServerLevel sea, int region) {
        int cx = SectionPos.blockToSectionCoord(SeaRegions.centerX(region));
        int cz = SectionPos.blockToSectionCoord(SeaRegions.centerZ(region));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                sea.getChunk(cx + dx, cz + dz);
            }
        }
    }

    /** The mind rises back into its body, wherever that is now; to its spawn, if the body is gone. */
    public static void leave(ServerPlayer player) {
        MEDITATING.remove(player.getUUID());
        SoulData soul = Soul.of(player);
        CompoundTag back = soul.seaReturn();
        ResourceLocation id = ResourceLocation.tryParse(back.getString("Dimension"));
        ServerLevel target = id == null ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        VesselEntity vessel = target == null ? null : vessel(target, back);
        Vec3 pos;
        float yaw = back.getFloat("Yaw");
        float pitch = back.getFloat("Pitch");
        if (vessel != null) {
            pos = vessel.position();
            yaw = vessel.getYRot();
            vessel.discard();
        } else if (target != null && !target.dimension().equals(DIMENSION) && back.contains("X")) {
            pos = new Vec3(back.getDouble("X"), back.getDouble("Y"), back.getDouble("Z"));
        } else {
            target = player.server.getLevel(player.getRespawnDimension());
            BlockPos respawn = player.getRespawnPosition();
            if (target == null || respawn == null) {
                target = player.server.overworld();
                respawn = target.getSharedSpawnPos();
            }

            pos = Vec3.atBottomCenterOf(target.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, respawn));
        }

        SeaPresence.clear(player);
        soul.setSeaReturn(new CompoundTag());
        player.playNotifySound(ModSounds.SOUL_SEA_EXIT.get(), SoundSource.PLAYERS, 0.9F, 1.0F);
        player.teleportTo(target, pos.x, pos.y, pos.z, yaw, pitch);
        Vfx.burst(target, Vfx.chest(player), Vfx.Palette.SOUL, 24, 0.2);
    }

    @Nullable
    private static VesselEntity vessel(ServerLevel level, CompoundTag back) {
        if (!back.hasUUID("Vessel")) {
            return null;
        }

        return level.getEntity(back.getUUID("Vessel")) instanceof VesselEntity vessel && vessel.isAlive() ? vessel : null;
    }

    /**
     * The body was struck while the mind was away: the mind is torn back up into it at once, and the blow lands.
     * Returns whether the owner was there to feel it.
     */
    public static boolean struck(VesselEntity vessel, DamageSource source, float amount) {
        UUID owner = vessel.owner();
        ServerPlayer player = owner == null || !(vessel.level() instanceof ServerLevel level) ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || !in(player)) {
            return false;
        }

        tell(player, "torn_back", ChatFormatting.RED);
        leave(player);
        player.invulnerableTime = 0;
        player.hurt(source, amount);
        return true;
    }

    /** Lets the body draw the eyes of whatever hunts nearby: the mind may be away, but the body is still there. */
    public static void lure(VesselEntity vessel) {
        for (Mob mob : vessel.level().getEntitiesOfClass(Mob.class, vessel.getBoundingBox().inflate(16.0), m -> m instanceof Enemy && m.isAlive())) {
            LivingEntity target = mob.getTarget();
            if ((target == null || target.getUUID().equals(vessel.owner())) && mob.hasLineOfSight(vessel)) {
                mob.setTarget(vessel);
            }
        }
    }

    /** Keeps the sea private and bounded: visitors are sent home, and wanderers turned back. */
    public static void guard(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        int index = SeaRegions.indexAt(player.getX(), player.getZ());
        Optional<UUID> owner = index < 0 ? Optional.empty() : SeaRegions.get(player.server).ownerOf(index);
        if (owner.isEmpty() || !owner.get().equals(player.getUUID())) {
            tell(player, "not_yours", ChatFormatting.DARK_PURPLE);
            leave(player);
            return;
        }

        double cx = SeaRegions.centerX(index);
        double cz = SeaRegions.centerZ(index);
        double dx = player.getX() - cx;
        double dz = player.getZ() - cz;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance > BOUNDARY || player.getY() < SURFACE_Y - 2.0) {
            double scale = distance > BOUNDARY ? (BOUNDARY - 6.0) / distance : 1.0;
            player.teleportTo(cx + dx * scale, SURFACE_Y, cz + dz * scale);
            tell(player, "boundary", ChatFormatting.DARK_AQUA);
        }
    }

    public static void forget(ServerPlayer player) {
        MEDITATING.remove(player.getUUID());
    }

    public static void reset() {
        MEDITATING.clear();
    }

    private static void tell(ServerPlayer player, String key, ChatFormatting colour) {
        player.displayClientMessage(Component.translatable("soul_sea.nightmare_evolved." + key).withStyle(colour), true);
    }
}
