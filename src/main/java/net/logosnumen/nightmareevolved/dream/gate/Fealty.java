package net.logosnumen.nightmareevolved.dream.gate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerPlayer;

/**
 * Oaths of fealty between Saints and Sovereigns. A Saint offers their fealty; the Sovereign receives it (or doesn't).
 * From then on the Saint's Citadel carries their liege's Domain, until either of them renounces the oath. Offers last
 * until the server stops; oaths are kept with the Gateways' records.
 */
public final class Fealty {
    /** The Saints who have offered fealty to each Sovereign and not yet been answered. */
    private static final Map<UUID, Set<UUID>> OFFERS = new HashMap<>();

    private Fealty() {
    }

    /** A Saint offers their fealty to a Sovereign; true if the offer is made. */
    public static boolean swear(ServerPlayer saint, ServerPlayer sovereign) {
        Rank rank = Soul.of(saint).rank();
        if (!rank.atLeast(Rank.SAINT) || rank.atLeast(Rank.SOVEREIGN)) {
            Spell.say(saint, "spell.nightmare_evolved.fealty_not_saint");
            return false;
        }

        if (saint == sovereign || !Soul.of(sovereign).rank().atLeast(Rank.SOVEREIGN)) {
            Spell.say(saint, "spell.nightmare_evolved.fealty_not_sovereign", sovereign.getDisplayName());
            return false;
        }

        GatewayRecords.Oath oath = GatewayRecords.get(saint.server).oath(saint.getUUID());
        if (oath != null) {
            Spell.say(saint, "spell.nightmare_evolved.fealty_already", oath.liege().name());
            return false;
        }

        OFFERS.computeIfAbsent(sovereign.getUUID(), id -> new HashSet<>()).add(saint.getUUID());
        Spell.say(saint, "spell.nightmare_evolved.fealty_offered", sovereign.getDisplayName());
        Spell.say(sovereign, "spell.nightmare_evolved.fealty_offer", saint.getDisplayName());
        String name = saint.getGameProfile().getName();
        sovereign.sendSystemMessage(Spell.choice("spell.nightmare_evolved.fealty_receive", "/spell receive " + name));
        return true;
    }

    /** A Sovereign receives a Saint's offered fealty; true if the oath is sworn. */
    public static boolean receive(ServerPlayer sovereign, ServerPlayer saint) {
        Set<UUID> offers = OFFERS.get(sovereign.getUUID());
        if (offers == null || !offers.remove(saint.getUUID())) {
            Spell.say(sovereign, "spell.nightmare_evolved.fealty_no_offer", saint.getDisplayName());
            return false;
        }

        if (!Soul.of(sovereign).rank().atLeast(Rank.SOVEREIGN) || !Soul.of(saint).rank().atLeast(Rank.SAINT)) {
            Spell.say(sovereign, "spell.nightmare_evolved.fealty_not_sovereign", sovereign.getDisplayName());
            return false;
        }

        GatewayRecords.get(sovereign.server).swear(saint.getUUID(), saint.getGameProfile().getName(),
            new GatewayRecords.Lord(sovereign.getUUID(), sovereign.getGameProfile().getName()));
        Spell.say(sovereign, "spell.nightmare_evolved.fealty_received", saint.getDisplayName());
        Spell.say(saint, "spell.nightmare_evolved.fealty_bound", sovereign.getDisplayName());
        Vfx.halo(saint.serverLevel(), saint.position(), 1.6, Vfx.Palette.LIGHT.main, 40);
        return true;
    }

    /** A Saint renounces their own oath; true if they had one. */
    public static boolean renounce(ServerPlayer saint) {
        GatewayRecords records = GatewayRecords.get(saint.server);
        GatewayRecords.Oath oath = records.oath(saint.getUUID());
        if (oath == null) {
            Spell.say(saint, "spell.nightmare_evolved.fealty_none");
            return false;
        }

        records.renounce(saint.getUUID());
        Spell.say(saint, "spell.nightmare_evolved.fealty_renounced", oath.liege().name());
        ServerPlayer liege = saint.server.getPlayerList().getPlayer(oath.liege().id());
        if (liege != null) {
            Spell.say(liege, "spell.nightmare_evolved.fealty_forsaken", saint.getDisplayName());
        }

        return true;
    }

    /** A Sovereign releases a Saint from their oath, by the Saint's name; true if such a Saint was sworn to them. */
    public static boolean release(ServerPlayer sovereign, String saintName) {
        GatewayRecords records = GatewayRecords.get(sovereign.server);
        for (Map.Entry<UUID, GatewayRecords.Oath> vassal : records.vassals(sovereign.getUUID()).entrySet()) {
            if (vassal.getValue().saint().equalsIgnoreCase(saintName)) {
                records.renounce(vassal.getKey());
                Spell.say(sovereign, "spell.nightmare_evolved.fealty_released", vassal.getValue().saint());
                ServerPlayer saint = sovereign.server.getPlayerList().getPlayer(vassal.getKey());
                if (saint != null) {
                    Spell.say(saint, "spell.nightmare_evolved.fealty_freed", sovereign.getDisplayName());
                }

                return true;
            }
        }

        Spell.say(sovereign, "spell.nightmare_evolved.fealty_no_vassal", saintName);
        return false;
    }

    public static void clear() {
        OFFERS.clear();
    }
}
