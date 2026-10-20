package net.logosnumen.nightmareevolved.nightmare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.Quake;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Runs the Spell's trials. A trial begins when a soul answers the Spell (or simply sleeps once it is ready): the Spell
 * remembers where it was, raises an arena in the Nightmare's void and takes it there. Waves of Nightmare Creatures
 * come, then the guardian; in the Awakening the soul must instead cross to the Gateway while creatures harry it.
 * Victory raises the soul and sends it home (with an Aspect, after the First Nightmare). Death in a trial keeps its
 * belongings safe, sends it home, and costs half its saturation and a short wait before it can try again; leaving the
 * game mid-trial counts the same.
 */
public final class Nightmares {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, NightmareEvolved.id("nightmare"));
    private static final int RETRY_TICKS = 2400;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static long nextId = 1;

    private Nightmares() {
    }

    public static boolean isNightmare(Level level) {
        return level.dimension() == DIMENSION;
    }

    public static boolean inTrial(Player player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    /** Why a soul can't face its next trial right now, or null when it can. */
    @Nullable
    public static Component refusal(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        Trial trial = Trial.next(soul.rank());
        if (trial == null) {
            return Spell.line("spell.nightmare_evolved.no_more_trials");
        }

        if (inTrial(player) || isNightmare(player.level())) {
            return Spell.line("spell.nightmare_evolved.already_in_trial");
        }

        long wait = soul.retryAt() - player.level().getGameTime();
        if (wait > 0) {
            return Spell.line("spell.nightmare_evolved.not_recovered", (wait + 19) / 20);
        }

        if (trial.needsSaturation() && !soul.saturated()) {
            return Spell.line("spell.nightmare_evolved.not_saturated", Math.round(soul.saturation()), Math.round(soul.capacity()));
        }

        return null;
    }

    /** Takes a soul into its next trial if it is ready; tells it why not otherwise. */
    public static boolean begin(ServerPlayer player) {
        Component refusal = refusal(player);
        if (refusal != null) {
            player.sendSystemMessage(refusal);
            return false;
        }

        return begin(player, Trial.next(Soul.of(player).rank()));
    }

    /** Takes a player into a given trial, whatever their state (used by operators to test trials). */
    public static boolean begin(ServerPlayer player, @Nullable Trial trial) {
        MinecraftServer server = player.server;
        ServerLevel level = server.getLevel(DIMENSION);
        if (trial == null || level == null || inTrial(player)) {
            return false;
        }

        if (!isNightmare(player.level())) {
            CompoundTag back = new CompoundTag();
            back.putString("Dimension", player.level().dimension().location().toString());
            back.putDouble("X", player.getX());
            back.putDouble("Y", player.getY());
            back.putDouble("Z", player.getZ());
            back.putFloat("Yaw", player.getYRot());
            Soul.of(player).setReturnPoint(back);
        }

        BlockPos centre = NightmareSlots.claim(server);
        Arenas.Layout layout = Arenas.build(level, trial, centre, level.random);
        Session session = new Session(player.getUUID(), trial, layout, nextId++);
        session.nextAt = level.getGameTime() + 80;
        SESSIONS.put(player.getUUID(), session);
        Vec3 arrival = layout.arrival();
        player.teleportTo(level, arrival.x, arrival.y, arrival.z, trial.objective == Trial.Objective.REACH ? 0.0F : player.getYRot(), 0.0F);
        session.bar.setName(trial.title());
        session.bar.setProgress(1.0F);
        session.bar.addPlayer(player);
        Vfx.flash(level, arrival.add(0.0, 1.0, 0.0), Vfx.Palette.SHADOW, 2.5F);
        level.playSound(null, BlockPos.containing(arrival), SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.5F, 0.6F);
        Spell.say(player, "spell.nightmare_evolved.trial_begins", trial.title());
        Spell.say(player, trial.objective == Trial.Objective.REACH ? "spell.nightmare_evolved.objective_reach" : "spell.nightmare_evolved.objective_slay");
        return true;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (SESSIONS.isEmpty()) {
            return;
        }

        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(DIMENSION);
        for (Session session : List.copyOf(SESSIONS.values())) {
            ServerPlayer player = server.getPlayerList().getPlayer(session.player);
            if (player == null || level == null) {
                end(server, session);
            } else {
                tick(level, player, session);
            }
        }
    }

    private static void tick(ServerLevel level, ServerPlayer player, Session session) {
        long now = level.getGameTime();
        if (session.state == Session.State.ENDING) {
            if (now >= session.nextAt) {
                conclude(player, session);
            }

            return;
        }

        if (!isNightmare(player.level()) || !player.isAlive()) {
            return;
        }

        session.creatures.removeIf(id -> {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                if (living.getY() < Arenas.GROUND - 40) {
                    living.kill();
                    return true;
                }

                if (now % 40 == 0 && living instanceof Mob mob && mob.getTarget() == null) {
                    mob.setTarget(player);
                }

                return false;
            }

            return true;
        });
        if (session.trial.objective == Trial.Objective.REACH) {
            reach(level, player, session, now);
        } else {
            slay(level, player, session, now);
        }
    }

    private static void slay(ServerLevel level, ServerPlayer player, Session session, long now) {
        Trial trial = session.trial;
        if (session.state == Session.State.GATHERING) {
            if (now >= session.nextAt) {
                session.state = Session.State.WAVES;
                session.nextAt = 0L;
                spawnWave(level, player, session);
            }

            return;
        }

        if (session.state == Session.State.WAVES) {
            session.bar.setName(Component.translatable("trial.nightmare_evolved.wave", trial.title(), session.wave, trial.waves));
            session.bar.setProgress(Mth.clamp(session.creatures.size() / (float)Math.max(1, session.waveSize), 0.0F, 1.0F));
            if (session.creatures.isEmpty()) {
                if (session.nextAt == 0L) {
                    session.nextAt = now + 60;
                } else if (now >= session.nextAt) {
                    session.nextAt = 0L;
                    if (session.wave < trial.waves) {
                        spawnWave(level, player, session);
                    } else {
                        session.state = Session.State.GUARDIAN;
                        spawnGuardian(level, player, session);
                    }
                }
            }

            return;
        }

        Entity guardian = session.guardian != null ? level.getEntity(session.guardian) : null;
        if (guardian instanceof LivingEntity living && living.isAlive()) {
            session.bar.setProgress(Mth.clamp(living.getHealth() / living.getMaxHealth(), 0.0F, 1.0F));
        } else {
            victory(level, player, session, now);
        }
    }

    private static void reach(ServerLevel level, ServerPlayer player, Session session, long now) {
        Vec3 goal = session.layout.goal();
        if (goal == null) {
            return;
        }

        if (session.state == Session.State.GATHERING && now >= session.nextAt) {
            session.state = Session.State.RUNNING;
        }

        if (now % 10 == 0) {
            Vfx.sigilFacing(level, goal.add(0.0, 3.5, 0.0), 2.4, Vfx.Palette.RUNE, new Vec3(0.0, 0.0, -1.0), 14);
        }

        double total = Arenas.CAUSEWAY;
        double left = Math.max(0.0, goal.z - player.getZ());
        session.bar.setName(Component.translatable("trial.nightmare_evolved.gateway", Math.round(left)));
        session.bar.setProgress(Mth.clamp((float)(1.0 - left / total), 0.0F, 1.0F));
        if (session.state == Session.State.RUNNING && now % 120 == 0 && session.creatures.size() < 8) {
            RandomSource random = level.random;
            for (int i = 0; i < 2 + random.nextInt(2); i++) {
                double z = Math.min(goal.z - 6.0, player.getZ() + 12.0 + random.nextDouble() * 10.0);
                double x = session.layout.centre().getX() + 0.5 + (random.nextDouble() - 0.5) * 4.0;
                double y = Quake.groundAt(level, x, Arenas.GROUND + 2, z);
                spawn(level, session.trial.pool.get(random.nextInt(session.trial.pool.size())), new Vec3(x, y, z), session.trial.creatures,
                    session.trial.classFor(i), false, session, player);
            }
        }

        if (player.position().distanceTo(goal) < 3.5) {
            victory(level, player, session, now);
        }
    }

    /** A point on the arena's ground between two distances from its middle. */
    private static Vec3 ring(ServerLevel level, Arenas.Layout layout, double near, double far) {
        RandomSource random = level.random;
        double a = random.nextDouble() * Mth.TWO_PI;
        double r = near + random.nextDouble() * Math.max(0.0, far - near);
        double x = layout.centre().getX() + 0.5 + Math.cos(a) * r;
        double z = layout.centre().getZ() + 0.5 + Math.sin(a) * r;
        return new Vec3(x, Quake.groundAt(level, x, Arenas.GROUND + 4, z), z);
    }

    private static void spawnWave(ServerLevel level, ServerPlayer player, Session session) {
        Trial trial = session.trial;
        RandomSource random = level.random;
        session.wave++;
        int count = 3 + trial.depth() + session.wave;
        session.waveSize = count;
        for (int i = 0; i < count; i++) {
            CreatureRank rank = session.wave == trial.waves && i == 0 ? CreatureRank.values()[Math.min(6, trial.creatures.ordinal() + 1)] : trial.creatures;
            EntityType<?> type = trial.pool.get(random.nextInt(trial.pool.size()));
            spawn(level, type, ring(level, session.layout, 9.0, session.layout.radius() - 4.0), rank, trial.classFor(i), false, session, player);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.5F, 0.6F + session.wave * 0.1F);
    }

    private static void spawnGuardian(ServerLevel level, ServerPlayer player, Session session) {
        Trial trial = session.trial;
        Vec3 at = ring(level, session.layout, 12.0, 14.0);
        Mob guardian = null;
        if (trial.colossus != null) {
            ColossusKind kind = trial.colossus == ColossusKind.CHAINBOUND ? ColossusKind.CHAINBOUND
                : List.of(ColossusKind.CAIRN, ColossusKind.OSSUARY, ColossusKind.GRAVEWYRM).get(level.random.nextInt(3));
            ColossusEntity colossus = Colossi.typeOf(kind).create(level);
            if (colossus != null) {
                colossus.setSeed(level.random.nextInt());
                guardian = spawn(level, colossus, at, trial.guardianRank, CreatureClass.TITAN, true, session, player);
                session.bar.setVisible(false);
            }
        } else if (trial.guardian != null) {
            CreatureClass creatureClass = trial.ordinal() >= Trial.THIRD_NIGHTMARE.ordinal() ? CreatureClass.TERROR : CreatureClass.TYRANT;
            guardian = spawn(level, trial.guardian, at, trial.guardianRank, creatureClass, true, session, player);
        }

        if (guardian == null) {
            victory(level, player, session, level.getGameTime());
            return;
        }

        session.guardian = guardian.getUUID();
        session.bar.setName(guardian.getDisplayName());
        session.bar.setColor(BossEvent.BossBarColor.RED);
        Vfx.shockwave(level, guardian.position(), 8.0, Vfx.Palette.BLOOD);
        level.playSound(null, guardian.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 3.0F, 0.6F);
        Spell.say(player, "spell.nightmare_evolved.guardian", Creatures.title(trial.guardianRank, Creatures.classOf(guardian)));
    }

    @Nullable
    private static Mob spawn(ServerLevel level, EntityType<?> type, Vec3 at, CreatureRank rank, CreatureClass creatureClass, boolean guardian, Session session,
        ServerPlayer player) {
        Entity entity = type.create(level);
        if (!(entity instanceof Mob mob)) {
            if (entity != null) {
                entity.discard();
            }

            return null;
        }

        return spawn(level, mob, at, rank, creatureClass, guardian, session, player);
    }

    private static Mob spawn(ServerLevel level, Mob mob, Vec3 at, CreatureRank rank, CreatureClass creatureClass, boolean guardian, Session session,
        ServerPlayer player) {
        mob.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360.0F, 0.0F);
        EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(BlockPos.containing(at)), MobSpawnType.EVENT, null);
        if (mob instanceof Zombie zombie) {
            zombie.setBaby(false);
        }

        Creatures.raise(mob, rank, creatureClass, guardian, session.id);
        mob.setTarget(player);
        level.addFreshEntity(mob);
        session.creatures.add(mob.getUUID());
        Vfx.smoke(level, at.add(0.0, 0.8, 0.0), Vfx.Palette.SHADOW.deep, 6, 0.6);
        return mob;
    }

    private static void victory(ServerLevel level, ServerPlayer player, Session session, long now) {
        if (session.state == Session.State.ENDING) {
            return;
        }

        session.state = Session.State.ENDING;
        session.nextAt = now + 80;
        session.bar.setVisible(true);
        session.bar.setName(session.trial.title());
        session.bar.setColor(BossEvent.BossBarColor.WHITE);
        session.bar.setProgress(1.0F);
        clearCreatures(level, session);
        Vfx.pillar(level, player.position(), 12.0, Vfx.Palette.LIGHT);
        Vfx.flash(level, Vfx.chest(player), Vfx.Palette.LIGHT, 3.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 0.8F);
        Spell.say(player, "spell.nightmare_evolved.trial_conquered", session.trial.title());
    }

    /** The trial is won: the soul is raised, given its Aspect after the First Nightmare, and sent home. */
    private static void conclude(ServerPlayer player, Session session) {
        Trial trial = session.trial;
        end(player.server, session);
        Soul.setRank(player, trial.to());
        Soul.of(player).passTrial();
        returnHome(player);
        Spell.say(player, "spell.nightmare_evolved.rank_reached", trial.to().title(), trial.to().tier());
        if (trial == Trial.FIRST_NIGHTMARE && !Soul.of(player).hasAspect()) {
            Aspects.grant(player);
        }
    }

    /** The trial is lost: half the soul's saturation goes, and it must wait before trying again. */
    private static void fail(ServerPlayer player, Session session) {
        end(player.server, session);
        SoulData soul = Soul.of(player);
        soul.setSaturation(soul.saturation() * 0.5);
        soul.setRetryAt(player.level().getGameTime() + RETRY_TICKS);
    }

    private static void clearCreatures(ServerLevel level, Session session) {
        for (UUID id : session.creatures) {
            Entity entity = level.getEntity(id);
            if (entity != null) {
                entity.discard();
            }
        }

        session.creatures.clear();
    }

    private static void end(MinecraftServer server, Session session) {
        ServerLevel level = server.getLevel(DIMENSION);
        if (level != null) {
            clearCreatures(level, session);
        }

        session.bar.removeAllPlayers();
        SESSIONS.remove(session.player);
    }

    /** Sends a player back to where they were when the trial took them, or to the world's spawn. */
    public static void returnHome(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        MinecraftServer server = player.server;
        ServerLevel home = server.overworld();
        double x = home.getSharedSpawnPos().getX() + 0.5;
        double y = home.getSharedSpawnPos().getY();
        double z = home.getSharedSpawnPos().getZ() + 0.5;
        float yaw = player.getYRot();
        if (soul.hasReturnPoint()) {
            CompoundTag back = soul.returnPoint();
            ResourceLocation dimension = ResourceLocation.tryParse(back.getString("Dimension"));
            ServerLevel there = dimension != null ? server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension)) : null;
            if (there != null) {
                home = there;
                x = back.getDouble("X");
                y = back.getDouble("Y");
                z = back.getDouble("Z");
                yaw = back.getFloat("Yaw");
            }
        }

        soul.setReturnPoint(new CompoundTag());
        player.teleportTo(home, x, y, z, yaw, 0.0F);
    }

    /** Ends a running trial as won, at once (for operators testing the road). */
    public static boolean forceWin(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return false;
        }

        victory(player.serverLevel(), player, session, player.level().getGameTime());
        return true;
    }

    // ---- Events -----------------------------------------------------------------------------------------------------

    /** A soul that sleeps while it is ready for its next trial is taken into it. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.isSleeping() && player.getSleepTimer() == 60 && refusal(player) == null) {
            player.stopSleepInBed(true, true);
            begin(player);
        }
    }

    /** Death in a trial: the soul's belongings are held safe (nothing drops into the void) and the trial is lost. */
    public static void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isNightmare(player.level())) {
            return;
        }

        SoulData soul = Soul.of(player);
        soul.setStash(player.getInventory().save(new ListTag()));
        soul.setStashedExperience(player.totalExperience);
        player.getInventory().clearContent();
        Session session = SESSIONS.get(player.getUUID());
        if (session != null) {
            if (session.state == Session.State.ENDING) {
                // Fell after the guardian did: the victory stands.
                conclude(player, session);
            } else {
                fail(player, session);
                Spell.say(player, "spell.nightmare_evolved.trial_failed", session.trial.title());
            }
        }
    }

    public static void onExperienceDrop(net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isNightmare(player.level())) {
            event.setCanceled(true);
        }
    }

    /** Back from death: the held belongings return, and so does the soul, to where the trial took it from. */
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SoulData soul = Soul.of(player);
        if (!soul.stash().isEmpty()) {
            player.getInventory().load(soul.stash());
            soul.setStash(new ListTag());
            player.giveExperiencePoints(soul.stashedExperience());
            soul.setStashedExperience(0);
        }

        if (soul.hasReturnPoint() && !inTrial(player)) {
            returnHome(player);
        }
    }

    /** Leaving the game mid-trial loses it (a won trial still counts). */
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Session session = SESSIONS.get(player.getUUID());
            if (session != null) {
                if (session.state == Session.State.ENDING) {
                    conclude(player, session);
                } else {
                    fail(player, session);
                }
            }
        }
    }

    /** Anyone who wakes in the Nightmare with no trial running is sent home. */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isNightmare(player.level()) && !inTrial(player)) {
            returnHome(player);
            Spell.say(player, "spell.nightmare_evolved.woke_home");
        }
    }

    public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        SESSIONS.clear();
    }
}
