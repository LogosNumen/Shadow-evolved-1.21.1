package net.logosnumen.nightmareevolved.network;

import io.netty.buffer.ByteBuf;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A player began meditating toward their Soul Sea, for so many ticks (0: they stopped). */
public record MeditationPayload(int ticks) implements CustomPacketPayload {
    public static final Type<MeditationPayload> TYPE = new Type<>(NightmareEvolved.id("meditation"));
    public static final StreamCodec<ByteBuf, MeditationPayload> CODEC = ByteBufCodecs.VAR_INT.map(MeditationPayload::new, MeditationPayload::ticks);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
