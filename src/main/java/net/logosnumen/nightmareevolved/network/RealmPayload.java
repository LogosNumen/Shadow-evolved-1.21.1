package net.logosnumen.nightmareevolved.network;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * What a player's client needs to draw the Dream Realm's laws around them: whether the Dark Sea is sealed, how hard the
 * Crushing presses (and whether the chains are rattling), and where Godgrave's veil is torn.
 */
public record RealmPayload(boolean sealed, float pressure, boolean rattling, List<Tear> tears) implements CustomPacketPayload {
    public static final Type<RealmPayload> TYPE = new Type<>(NightmareEvolved.id("realm"));

    /** A tear in Godgrave's veil: where, how wide, and the game time it opened. */
    public record Tear(double x, double z, float radius, long start) {
        public static final StreamCodec<ByteBuf, Tear> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Tear::x, ByteBufCodecs.DOUBLE, Tear::z, ByteBufCodecs.FLOAT, Tear::radius, ByteBufCodecs.VAR_LONG, Tear::start, Tear::new
        );
    }

    public static final StreamCodec<ByteBuf, RealmPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, RealmPayload::sealed, ByteBufCodecs.FLOAT, RealmPayload::pressure, ByteBufCodecs.BOOL, RealmPayload::rattling,
        Tear.CODEC.apply(ByteBufCodecs.<ByteBuf, Tear, List<Tear>>collection(ArrayList::new)), RealmPayload::tears, RealmPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
