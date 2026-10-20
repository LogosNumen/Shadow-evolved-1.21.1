package net.logosnumen.nightmareevolved.network;

import io.netty.buffer.ByteBuf;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A player pressing one of their Aspect's ability keys: slot 0 for the first ability, 1 for the second. */
public record AbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<AbilityPayload> TYPE = new Type<>(NightmareEvolved.id("ability"));
    public static final StreamCodec<ByteBuf, AbilityPayload> CODEC = ByteBufCodecs.VAR_INT.map(AbilityPayload::new, AbilityPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
