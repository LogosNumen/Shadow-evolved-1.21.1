package net.logosnumen.nightmareevolved.network;

import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.memory.Worn;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.server.level.ServerPlayer;

/** Carries out what a player asked of their soul. Everything is checked again here; the client only asks. */
public final class SoulActions {
    private SoulActions() {
    }

    public static void handle(ServerPlayer player, SoulActionPayload payload) {
        int mask = payload.mask() == 0 ? -1 : payload.mask();
        switch (payload.action()) {
            case SUMMON -> SoulMemories.summon(player, payload.target(), mask);
            case DISMISS -> SoulMemories.dismiss(player, payload.target(), mask, false);
            case BIND -> {
                SoulData soul = Soul.of(player);
                MemoryRecord record = soul.memory(payload.target());
                if (record != null) {
                    SoulMemories.bindRelic(new Worn(player, soul, record.pieces.get(0).stack, record.data(), Worn.Slot.WORN), SoulAttribute.MARBLE_SHELL.id());
                }
            }
            case ECHO_SUMMON -> Echoes.summon(player, payload.target());
            case ECHO_DISMISS -> Echoes.dismiss(player, payload.target(), false);
            case ECHO_SHADOW -> Echoes.makeShadow(player, payload.target());
            case DISMISS_HELD -> {
                MemoryData held = SoulMemories.mine(player, player.getMainHandItem());
                if (held != null) {
                    SoulMemories.dismiss(player, held.id(), -1, false);
                }
            }
        }
    }
}
