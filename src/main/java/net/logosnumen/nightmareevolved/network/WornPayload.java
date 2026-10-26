package net.logosnumen.nightmareevolved.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * What a player wears from their Memories that others can see (a garment over their shoulders, a crown or a mask on
 * their head), and the form they have taken if they are a transformed Saint. Sent to everyone who can see them.
 */
public record WornPayload(int entity, List<String> looks) implements CustomPacketPayload {
    public static final Type<WornPayload> TYPE = new Type<>(NightmareEvolved.id("worn"));
    public static final StreamCodec<ByteBuf, WornPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WornPayload::entity,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(16)), WornPayload::looks,
        WornPayload::new
    );

    /** How a Transformation is marked among the looks: this, then the form's ability id. */
    public static final String FORM = "form:";

    /** The looks of what a player wears: a named Memory's own id, or "cloak:" and a family for a woven cloak. */
    public static WornPayload of(ServerPlayer player) {
        List<String> looks = new ArrayList<>();
        for (MemoryRecord record : Soul.of(player).memories()) {
            MemoryDef def = record.def();
            if (def == null || def.carry != MemoryDef.Carry.WORN || !record.pieces.get(0).out) {
                continue;
            }

            MemoryData data = record.data();
            if (def.kind == MemoryDef.Kind.GARMENT || def.kind == MemoryDef.Kind.TOOL) {
                looks.add(def.named ? def.id : def.id + ":" + data.name().split("\\.")[0]);
            }
        }

        // A Saint's Transformation shows too: the form they have taken.
        String form = Soul.of(player).form();
        if (!form.isEmpty()) {
            looks.add(FORM + form);
        }

        return new WornPayload(player.getId(), looks);
    }

    /** Tells everyone who can see the player (and the player) what they now wear. */
    public static void broadcast(ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, of(player));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
