package net.logosnumen.nightmareevolved.dream.law;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.logosnumen.nightmareevolved.dream.DreamDamage;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.RealmState;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.shape.IslesShaper;
import net.logosnumen.nightmareevolved.network.RealmPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The laws of the Dream Realm's regions, as they bear on the people in it:
 * <ul>
 * <li>The Forgotten Shore: by night the Dark Sea floods the labyrinth, and what lives in it takes anyone caught there.</li>
 * <li>The Chained Isles: the Crushing as islands rise, the rattle of chains before it, and the forbidden sky above.</li>
 * <li>The Hollow Mountains: the mist of nothingness dissolves whoever lingers in it; their hollows and the Underworld
 * are full of a darkness light does not touch.</li>
 * <li>Godgrave: the white sky behind the torn veil (see {@link Cloudveil}).</li>
 * <li>The Nightmare Desert: by day the Tomb of Ariel stays as far away as a mirage, except along the one path to it.</li>
 * <li>The Stormsea: under its storms courses loop and places shift; without the stars, sailors are carried astray.</li>
 * <li>The Bone Sea's water melts flesh, and the Glass Hell's lenses gather the sun into fire.</li>
 * </ul>
 */
public final class RealmLaws {
    /** How close to the Tomb of Ariel the mirage lets anyone come by day. */
    public static final double MIRAGE = 760.0;
    /** The mist of nothingness lies below about this height in the Hollow Mountains. */
    public static final int MIST = 215;
    private static final ResourceLocation CRUSHED = ResourceLocation.fromNamespaceAndPath("nightmare_evolved", "crushing");
    private static final Map<UUID, Bearing> BEARING = new HashMap<>();
    private static final List<Focus> FOCI = new ArrayList<>();

    /** What the laws remember of one person: time spent in the mist, under the Dark Sea, how long they've sailed lost. */
    private static final class Bearing {
        int nothing;
        int drowned;
        int drift = -1;
        boolean sealed;
        float pressure = -1.0F;
        boolean rattling;
        int tears = -1;
    }

    private record Focus(Vec3 at, long burst) {
    }

    private RealmLaws() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel realm = server.getLevel(DreamRealm.DIMENSION);
        if (realm == null) {
            return;
        }

        List<ServerPlayer> players = realm.players();
        boolean tears = Cloudveil.tick(realm, players);
        long now = realm.getGameTime();
        foci(realm, now);
        if (now % 10 != 0 && !tears) {
            return;
        }

        RealmState state = RealmState.get(server);
        RealmTerrain terrain = RealmTerrain.of(realm);
        for (ServerPlayer player : players) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }

            Bearing bearing = BEARING.computeIfAbsent(player.getUUID(), id -> new Bearing());
            float pressure = 0.0F;
            boolean rattling = false;
            if (now % 10 == 0) {
                Region part = RealmMap.part(player.getBlockX(), player.getBlockY(), player.getBlockZ());
                darkSea(realm, player, bearing, state.darkSeaSealed());
                if (part == Region.CHAINED_ISLES) {
                    IslesShaper.Island island = terrain.isles().islandAt(player.getX(), player.getZ());
                    boolean on = island != null && player.getY() > island.top() - 8 && player.getY() < island.top() + 48;
                    pressure = (float)Math.max(on ? Crushing.pressure(island, now) : 0.0, Crushing.sky(player.getX(), player.getY(), player.getZ()));
                    rattling = on && Crushing.rattling(island, now);
                }

                crush(player, pressure, rattling, now);
                mist(realm, player, bearing, part);
                darkness(realm, player, part);
                mirage(realm, player, part, now);
                drift(realm, player, bearing, part, now);
                boneSea(player, part, now);
                lenses(realm, player, part, now);
            } else {
                pressure = Math.max(0.0F, bearing.pressure);
                rattling = bearing.rattling;
            }

            sync(player, bearing, state.darkSeaSealed(), pressure, rattling);
        }
    }

    private static void sync(ServerPlayer player, Bearing bearing, boolean sealed, float pressure, boolean rattling) {
        int tearCount = Cloudveil.tears().size();
        long tearKey = Cloudveil.tears().isEmpty() ? 0 : Cloudveil.tears().get(tearCount - 1).start();
        int tears = (int)(tearCount * 31 + tearKey);
        if (bearing.sealed == sealed && Math.abs(bearing.pressure - pressure) < 0.02F && bearing.rattling == rattling && bearing.tears == tears) {
            return;
        }

        bearing.sealed = sealed;
        bearing.pressure = pressure;
        bearing.rattling = rattling;
        bearing.tears = tears;
        List<RealmPayload.Tear> list = new ArrayList<>();
        for (Cloudveil.Tear tear : Cloudveil.tears()) {
            list.add(new RealmPayload.Tear(tear.x(), tear.z(), tear.radius(), tear.start()));
        }

        PacketDistributor.sendToPlayer(player, new RealmPayload(sealed, pressure, rattling, list));
    }

    /** Under the Dark Sea: no air, black water, and the things that live in it. */
    private static void darkSea(ServerLevel realm, ServerPlayer player, Bearing bearing, boolean sealed) {
        if (!DarkSea.under(player.getX(), player.getEyeY(), player.getZ(), realm.getDayTime(), sealed)) {
            bearing.drowned = 0;
            return;
        }

        bearing.drowned += 10;
        player.setAirSupply(Math.max(-20, player.getAirSupply() - 40));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        if (bearing.drowned % 20 == 0) {
            float harm = player.getAirSupply() <= 0 ? 3.0F : 1.0F + Math.min(5.0F, bearing.drowned / 100.0F);
            if (bearing.drowned > 40) {
                player.hurt(DreamDamage.source(realm, DreamDamage.DARK_SEA), harm);
            }
        }

        if (bearing.drowned == 10) {
            tell(player, "dark_sea", ChatFormatting.DARK_RED);
        }
    }

    /** The Crushing: slower, then unable to jump, then unable to breathe; the more weight carried, the worse. */
    private static void crush(ServerPlayer player, float pressure, boolean rattling, long now) {
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.removeModifier(CRUSHED);
            if (pressure > 0.5F) {
                jump.addTransientModifier(new AttributeModifier(CRUSHED, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        if (rattling && now % 40 == 0) {
            player.playNotifySound(SoundEvents.CHAIN_BREAK, SoundSource.AMBIENT, 1.4F, 0.4F + player.getRandom().nextFloat() * 0.3F);
            if (now % 200 == 0) {
                tell(player, "chains", ChatFormatting.GRAY);
            }
        }

        if (pressure <= 0.2F) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, Math.min(4, (int)(pressure * 4.0F)), false, false));
        if (pressure > 0.5F) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1, false, false));
        }

        if (pressure > 0.8F && now % 20 == 0) {
            double weight = 1.0 + player.getArmorValue() / 20.0;
            player.hurt(DreamDamage.source(player.level(), DreamDamage.CRUSHING), (float)((pressure - 0.75F) * 12.0F * weight));
        }
    }

    /** The mist of nothingness in the mountains' ravines: the longer one stays in it, the more of oneself dissolves. */
    private static void mist(ServerLevel realm, ServerPlayer player, Bearing bearing, Region part) {
        boolean in = part == Region.HOLLOW_MOUNTAINS && player.getEyeY() < MIST && realm.canSeeSky(BlockPos.containing(player.getEyePosition()));
        if (!in) {
            bearing.nothing = Math.max(0, bearing.nothing - 20);
            return;
        }

        bearing.nothing += 10;
        if (bearing.nothing == 10) {
            tell(player, "mist", ChatFormatting.WHITE);
        }

        if (bearing.nothing > 600) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, false));
        }

        if (bearing.nothing > 900 && bearing.nothing % 40 == 0) {
            player.hurt(DreamDamage.source(realm, DreamDamage.NOTHINGNESS), bearing.nothing > 1800 ? 3.0F : 1.0F);
        }
    }

    /** True darkness, in the hollows of the peaks and in the Underworld. */
    private static void darkness(ServerLevel realm, ServerPlayer player, Region part) {
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        boolean hollow = part == Region.HOLLOW_MOUNTAINS && player.getY() > 100 && realm.getBrightness(LightLayer.SKY, eyes) == 0;
        if (part == Region.UNDERWORLD || hollow) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        }
    }

    /**
     * By day the Tomb of Ariel is always in view and never any closer, however long one walks toward it: anyone who
     * comes too near finds themselves no nearer. Only along the hidden path from the Black Skull does it come within
     * reach; by night the mirage lifts.
     */
    private static void mirage(ServerLevel realm, ServerPlayer player, Region part, long now) {
        if (part != Region.NIGHTMARE_DESERT || isNight(realm) || onPath(player.getX(), player.getZ())) {
            return;
        }

        double dx = player.getX() - Places.TOMB_OF_ARIEL.x();
        double dz = player.getZ() - Places.TOMB_OF_ARIEL.z();
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d >= MIRAGE || d < 1.0) {
            return;
        }

        double scale = MIRAGE / d;
        double x = Places.TOMB_OF_ARIEL.x() + dx * scale;
        double z = Places.TOMB_OF_ARIEL.z() + dz * scale;
        int y = realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(x), (int)Math.floor(z));
        Entity mover = player.getRootVehicle();
        if (mover != player) {
            mover.teleportTo(x, y, z);
        }

        player.connection.teleport(x, y, z, 0.0F, 0.0F, EnumSet.of(RelativeMovement.X_ROT, RelativeMovement.Y_ROT));
        if (now % 100 == 0) {
            tell(player, "mirage", ChatFormatting.GOLD);
        }
    }

    /** The one way to the Tomb by day: the line of black stones from the Black Skull. */
    public static boolean onPath(double x, double z) {
        double ax = Places.BLACK_SKULL.x();
        double az = Places.BLACK_SKULL.z();
        double bx = Places.TOMB_OF_ARIEL.x();
        double bz = Places.TOMB_OF_ARIEL.z();
        double vx = bx - ax;
        double vz = bz - az;
        double t = Mth.clamp(((x - ax) * vx + (z - az) * vz) / (vx * vx + vz * vz), 0.0, 1.0);
        double px = ax + vx * t - x;
        double pz = az + vz * t - z;
        return px * px + pz * pz < 9.0 * 9.0;
    }

    private static boolean isNight(ServerLevel realm) {
        long time = Math.floorMod(realm.getDayTime(), 24000L);
        return time > 12800L && time < 23200L;
    }

    /** Whether the Stormsea's storms are up: most of the time, with clear spells when the stars can be read. */
    public static boolean stormy(long time) {
        return Math.floorMod(time, 30000L) < 22000L;
    }

    /** Sailing lost under the storm: after a while, the course loops and the sea carries one somewhere else. */
    private static void drift(ServerLevel realm, ServerPlayer player, Bearing bearing, Region part, long now) {
        boolean open = part == Region.STORMSEA && player.getZ() > 4500
            && realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.getBlockX(), player.getBlockZ()) <= 65
            && (player.isInWater() || player.getVehicle() != null);
        if (!open || !stormy(now)) {
            bearing.drift = -1;
            return;
        }

        if (bearing.drift < 0) {
            bearing.drift = 3000 + player.getRandom().nextInt(3000);
        }

        bearing.drift -= 10;
        if (bearing.drift > 0) {
            return;
        }

        bearing.drift = -1;
        double a = player.getRandom().nextDouble() * Math.PI * 2.0;
        double r = 300.0 + player.getRandom().nextDouble() * 500.0;
        double x = player.getX() + Math.cos(a) * r;
        double z = Math.max(4700.0, player.getZ() + Math.sin(a) * r);
        Entity mover = player.getRootVehicle();
        if (mover != player) {
            player.stopRiding();
            mover.teleportTo(x, mover.getY(), z);
            player.teleportTo(x, mover.getY() + 0.5, z);
            player.startRiding(mover, true);
        } else {
            player.teleportTo(x, player.getY(), z);
        }

        realm.playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.AMBIENT, 1.0F, 0.6F);
        tell(player, "astray", ChatFormatting.DARK_AQUA);
    }

    /** The Bone Sea's water eats flesh away within minutes. */
    private static void boneSea(ServerPlayer player, Region part, long now) {
        if (part == Region.BONE_SEA && player.isInWater() && now % 20 == 0) {
            player.hurt(DreamDamage.source(player.level(), DreamDamage.BONE_SEA), 1.5F);
        }
    }

    /** Points on the Glass Hell where the sunlight gathers: a shimmer of warning, then fire. */
    private static void lenses(ServerLevel realm, ServerPlayer player, Region part, long now) {
        if (part != Region.GLASS_HELL || isNight(realm) || !realm.canSeeSky(player.blockPosition()) || player.getRandom().nextInt(30) != 0) {
            return;
        }

        double a = player.getRandom().nextDouble() * Math.PI * 2.0;
        double r = player.getRandom().nextDouble() * 5.0;
        Vec3 at = new Vec3(player.getX() + Math.cos(a) * r, player.getY(), player.getZ() + Math.sin(a) * r);
        FOCI.add(new Focus(at, now + 50));
    }

    private static void foci(ServerLevel realm, long now) {
        FOCI.removeIf(focus -> {
            if (now < focus.burst) {
                if (now % 4 == 0) {
                    realm.sendParticles(ParticleTypes.END_ROD, focus.at.x, focus.at.y + 0.2, focus.at.z, 3, 0.3, 0.05, 0.3, 0.0);
                }

                return false;
            }

            realm.sendParticles(ParticleTypes.FLAME, focus.at.x, focus.at.y + 0.3, focus.at.z, 40, 0.5, 0.3, 0.5, 0.02);
            realm.playSound(null, BlockPos.containing(focus.at), SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 1.0F, 1.4F);
            for (LivingEntity living : realm.getEntitiesOfClass(LivingEntity.class, new AABB(focus.at, focus.at).inflate(1.8, 2.0, 1.8))) {
                living.hurt(DreamDamage.source(realm, DreamDamage.FOCUSED_SUN), 5.0F);
                living.igniteForSeconds(3.0F);
            }

            return true;
        });
    }

    private static void tell(ServerPlayer player, String key, ChatFormatting colour) {
        player.displayClientMessage(Component.translatable("realm.nightmare_evolved.law." + key).withStyle(colour), true);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BEARING.remove(event.getEntity().getUUID());
    }

    /** Coming into the Dream Realm again: the client must hear the realm's state anew. */
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        BEARING.remove(event.getEntity().getUUID());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        BEARING.clear();
        FOCI.clear();
        Cloudveil.reset();
    }
}
