package net.logosnumen.nightmareevolved.dream.gate;

import net.logosnumen.nightmareevolved.dream.DreamState;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * The Citadels: human fortresses built around conquered Gateways. A Saint can bind one to their soul by laying a hand on
 * its Gateway while kneeling before it (crouching); from then on it is theirs alone to command, its likeness stands in
 * their Soul Sea, and its Gateway is where they come into the Dream Realm. The Great Citadels (Bastion, Ravenheart and
 * the Night Garden) are made for Sovereigns and answer no lesser soul. A soul holds one Citadel at a time, and a Citadel
 * one lord.
 */
public final class Citadels {
    private Citadels() {
    }

    public static Component title(String citadel) {
        return Component.translatable("citadel.nightmare_evolved." + citadel);
    }

    /** A soul tries to bind a Gateway's Citadel to itself; true if it is bound to them now. */
    public static boolean bind(ServerPlayer player, Gateways.Gateway gateway) {
        String citadel = gateway.citadel();
        if (citadel == null) {
            Spell.say(player, "spell.nightmare_evolved.citadel_none");
            return false;
        }

        Component title = title(citadel);
        SoulData soul = Soul.of(player);
        if (!soul.rank().atLeast(Rank.SAINT)) {
            Spell.say(player, "spell.nightmare_evolved.citadel_weak", title);
            return false;
        }

        if (gateway.great() && !soul.rank().atLeast(Rank.SOVEREIGN)) {
            Spell.say(player, "spell.nightmare_evolved.citadel_great", title);
            return false;
        }

        GatewayRecords records = GatewayRecords.get(player.server);
        GatewayRecords.Lord lord = records.lord(citadel);
        if (lord != null) {
            if (lord.id().equals(player.getUUID())) {
                Spell.say(player, "spell.nightmare_evolved.citadel_yours", title);
                return true;
            }

            Spell.say(player, "spell.nightmare_evolved.citadel_held", title, lord.name());
            return false;
        }

        String old = soul.citadel();
        if (!old.isEmpty()) {
            records.release(old, player.getUUID());
        }

        records.bind(citadel, player.getUUID(), player.getGameProfile().getName());
        soul.setCitadel(citadel);
        DreamState.of(player).setAnchor(gateway.id());
        Vec3 at = Vec3.atCenterOf(gateway.pos());
        Vfx.pillar(player.serverLevel(), at, 14.0, Vfx.Palette.LIGHT);
        Vfx.shockwave(player.serverLevel(), at, 10.0, Vfx.Palette.LIGHT);
        player.serverLevel().playSound(null, gateway.pos(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 0.6F);
        if (old.isEmpty()) {
            Spell.say(player, "spell.nightmare_evolved.citadel_bound", title);
        } else {
            Spell.say(player, "spell.nightmare_evolved.citadel_rebound", title, title(old));
        }

        return true;
    }

    /** A soul lets its Citadel go. */
    public static void release(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        if (soul.citadel().isEmpty()) {
            return;
        }

        GatewayRecords.get(player.server).release(soul.citadel(), player.getUUID());
        soul.setCitadel("");
    }
}
