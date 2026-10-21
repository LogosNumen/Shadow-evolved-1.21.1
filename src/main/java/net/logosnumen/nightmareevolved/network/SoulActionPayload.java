package net.logosnumen.nightmareevolved.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Something a player asks of their soul from the Soul screen or a key: summon or dismiss a Memory (or some of its
 * armour layers), bind a relic, call or dismiss an Echo, make an Echo a Shadow, or dismiss whatever Memory is in
 * hand.
 */
public record SoulActionPayload(Action action, UUID target, int mask) implements CustomPacketPayload {
    public enum Action {
        SUMMON,
        DISMISS,
        BIND,
        ECHO_SUMMON,
        ECHO_DISMISS,
        ECHO_SHADOW,
        DISMISS_HELD
    }

    public static final Type<SoulActionPayload> TYPE = new Type<>(NightmareEvolved.id("soul_action"));
    public static final StreamCodec<ByteBuf, SoulActionPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT.map(i -> Action.values()[Math.max(0, Math.min(Action.values().length - 1, i))], Action::ordinal), SoulActionPayload::action,
        UUIDUtil.STREAM_CODEC, SoulActionPayload::target,
        ByteBufCodecs.VAR_INT, SoulActionPayload::mask,
        SoulActionPayload::new
    );

    public static SoulActionPayload of(Action action) {
        return new SoulActionPayload(action, Util.NIL_UUID, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
