package net.logosnumen.nightmareevolved.network;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Tells clients to start a body animation clip on an entity. */
public record PosePayload(int entityId, String clip) implements CustomPacketPayload {
    public static final Type<PosePayload> TYPE = new Type<>(NightmareEvolved.id("pose"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PosePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PosePayload::entityId, ByteBufCodecs.stringUtf8(64), PosePayload::clip, PosePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
