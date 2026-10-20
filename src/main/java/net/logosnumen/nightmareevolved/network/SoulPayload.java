package net.logosnumen.nightmareevolved.network;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A player's own soul, as their client needs it for the HUD: rank, Aspect, essence, how full the cores are, and the
 * game time at which each of the Aspect's two abilities is ready again.
 */
public record SoulPayload(
    int rank, String aspect, int aspectTier, float essence, float maxEssence, float saturation, float capacity, long ready0, long ready1
) implements CustomPacketPayload {
    public static final SoulPayload EMPTY = new SoulPayload(0, "", 0, 0.0F, 0.0F, 0.0F, 0.0F, 0L, 0L);
    public static final Type<SoulPayload> TYPE = new Type<>(NightmareEvolved.id("soul"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoulPayload> CODEC = StreamCodec.of(
        (buf, p) -> {
            buf.writeVarInt(p.rank);
            buf.writeUtf(p.aspect, 64);
            buf.writeVarInt(p.aspectTier);
            buf.writeFloat(p.essence);
            buf.writeFloat(p.maxEssence);
            buf.writeFloat(p.saturation);
            buf.writeFloat(p.capacity);
            buf.writeVarLong(p.ready0);
            buf.writeVarLong(p.ready1);
        },
        buf -> new SoulPayload(
            buf.readVarInt(), buf.readUtf(64), buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarLong(),
            buf.readVarLong()
        )
    );

    public static SoulPayload of(SoulData soul) {
        Aspect aspect = Aspects.byId(soul.aspect());
        long ready0 = aspect == null ? 0L : soul.readyAt(aspect.abilities().get(0).id());
        long ready1 = aspect == null || aspect.abilities().size() < 2 ? 0L : soul.readyAt(aspect.abilities().get(1).id());
        return new SoulPayload(
            soul.rank().ordinal(), soul.aspect(), soul.aspectTier().ordinal(), (float)soul.essence(), (float)soul.maxEssence(), (float)soul.saturation(),
            (float)soul.capacity(), ready0, ready1
        );
    }

    public long readyAt(int slot) {
        return slot == 0 ? this.ready0 : this.ready1;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
