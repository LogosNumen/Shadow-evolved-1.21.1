package net.logosnumen.nightmareevolved.dream.gate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Domains: a Sovereign's rule over the Dream Realm, carried by Citadels. Every Citadel a Sovereign binds, and every
 * Citadel bound by a Saint sworn to them, holds the land around its Gateway for them: a Great Citadel far out, a lesser
 * one less far. Within their own Domain a Sovereign is all but untouchable. A Saint who serves no one has only a
 * nascent Domain, a little way around their own Citadel, where they are somewhat guarded. Where Domains overlap, a
 * Sovereign's outweighs a nascent one, and the nearer Citadel holds otherwise.
 */
public final class Domains {
    public static final int GREAT_REACH = 192;
    public static final int REACH = 96;
    public static final int NASCENT_REACH = 32;
    /** The share of a blow a Sovereign takes within their own Domain, and a Saint within their nascent one. */
    public static final float SUPREME_GUARD = 0.2F;
    public static final float NASCENT_GUARD = 0.7F;
    /** What share of each blow each soul takes where it stands now; absent means the whole blow. */
    private static final Map<UUID, Float> GUARD = new HashMap<>();
    /** The Domain each soul last stood in, to tell them when they cross into another. */
    private static final Map<UUID, String> STANDING = new HashMap<>();

    /** One Citadel's share of a Domain: where its Gateway stands, how far it reaches, and whose rule it carries. */
    public record Hold(String citadel, BlockPos pos, int reach, GatewayRecords.Lord ruler, boolean nascent) {
        public boolean covers(double x, double z) {
            double dx = x - (this.pos.getX() + 0.5);
            double dz = z - (this.pos.getZ() + 0.5);
            return dx * dx + dz * dz <= (double)this.reach * this.reach;
        }

        private double distance(double x, double z) {
            return Math.hypot(x - (this.pos.getX() + 0.5), z - (this.pos.getZ() + 0.5));
        }
    }

    private Domains() {
    }

    /** Every bound Citadel's hold, as the lords and oaths stand now. */
    public static List<Hold> holds(MinecraftServer server) {
        GatewayRecords records = GatewayRecords.get(server);
        List<Hold> holds = new ArrayList<>();
        for (Gateways.Gateway gateway : Gateways.all(server)) {
            GatewayRecords.Lord lord = gateway.citadel() == null ? null : records.lord(gateway.citadel());
            if (lord == null) {
                continue;
            }

            GatewayRecords.Oath oath = records.oath(lord.id());
            if (gateway.great()) {
                holds.add(new Hold(gateway.citadel(), gateway.pos(), GREAT_REACH, lord, false));
            } else if (oath != null) {
                holds.add(new Hold(gateway.citadel(), gateway.pos(), REACH, oath.liege(), false));
            } else if (records.sovereign(lord.id())) {
                holds.add(new Hold(gateway.citadel(), gateway.pos(), REACH, lord, false));
            } else {
                holds.add(new Hold(gateway.citadel(), gateway.pos(), NASCENT_REACH, lord, true));
            }
        }

        return holds;
    }

    /** The hold whose rule a place in the Dream Realm lies under, if any. */
    @Nullable
    public static Hold at(List<Hold> holds, double x, double z) {
        Hold best = null;
        for (Hold hold : holds) {
            if (hold.covers(x, z) && (best == null || best.nascent && !hold.nascent
                || best.nascent == hold.nascent && hold.distance(x, z) < best.distance(x, z))) {
                best = hold;
            }
        }

        return best;
    }

    /** The share of a blow a soul takes standing under a hold: little if it is their own Domain, all of it otherwise. */
    public static float guard(@Nullable Hold hold, UUID soul) {
        if (hold == null || !hold.ruler.id().equals(soul)) {
            return 1.0F;
        }

        return hold.nascent ? NASCENT_GUARD : SUPREME_GUARD;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) {
            return;
        }

        GatewayRecords records = GatewayRecords.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            records.noteSovereign(player.getUUID(), Soul.of(player).rank().atLeast(Rank.SOVEREIGN));
        }

        ServerLevel realm = server.getLevel(DreamRealm.DIMENSION);
        if (realm == null) {
            return;
        }

        List<Hold> holds = holds(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != realm || player.isSpectator()) {
                GUARD.remove(player.getUUID());
                STANDING.remove(player.getUUID());
                continue;
            }

            Hold hold = at(holds, player.getX(), player.getZ());
            float guard = guard(hold, player.getUUID());
            if (guard < 1.0F) {
                GUARD.put(player.getUUID(), guard);
            } else {
                GUARD.remove(player.getUUID());
            }

            announce(player, hold);
            if (guard == SUPREME_GUARD && server.getTickCount() % 40 == 0) {
                Vfx.halo(realm, player.position(), 1.4, Vfx.Palette.LIGHT.main, 30);
            }
        }
    }

    /** Tells a soul when it crosses into a Domain, or out of one. */
    private static void announce(ServerPlayer player, @Nullable Hold hold) {
        String key = hold == null ? "" : hold.ruler.id() + (hold.nascent ? "/nascent" : "");
        String was = STANDING.getOrDefault(player.getUUID(), "");
        if (key.equals(was)) {
            return;
        }

        STANDING.put(player.getUUID(), key);
        Component line;
        if (hold == null) {
            line = Component.translatable("domain.nightmare_evolved.leave");
        } else if (hold.ruler.id().equals(player.getUUID())) {
            line = Component.translatable(hold.nascent ? "domain.nightmare_evolved.own_nascent" : "domain.nightmare_evolved.own");
        } else {
            line = Component.translatable(hold.nascent ? "domain.nightmare_evolved.nascent" : "domain.nightmare_evolved.enter", hold.ruler.name());
        }

        player.displayClientMessage(line.copy().withStyle(style -> style.withColor(0xE6D2FF)), true);
    }

    /** Within their own Domain, a Sovereign is all but untouchable; a Saint in a nascent one, somewhat guarded. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Float guard = GUARD.get(event.getEntity().getUUID());
        if (guard != null && event.getEntity() instanceof ServerPlayer) {
            event.setAmount(event.getAmount() * guard);
        }
    }

    /** Nor can anything move a Sovereign within their own Domain. */
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        Float guard = GUARD.get(entity.getUUID());
        if (guard != null && guard <= SUPREME_GUARD && entity instanceof ServerPlayer) {
            event.setCanceled(true);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        GUARD.remove(event.getEntity().getUUID());
        STANDING.remove(event.getEntity().getUUID());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        GUARD.clear();
        STANDING.clear();
        Fealty.clear();
    }
}
