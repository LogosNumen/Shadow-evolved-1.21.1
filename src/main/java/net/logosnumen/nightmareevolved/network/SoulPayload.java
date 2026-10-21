package net.logosnumen.nightmareevolved.network;

import java.util.ArrayList;
import java.util.List;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * A player's own soul, as their client needs it for the HUD and the runes: Rank, Aspect and its Rank, Flaw,
 * Attributes, cores and fragments, essence, when each step's ability is ready again, and what is held active.
 */
public record SoulPayload(
    int rank, String aspect, int aspectTier, String flaw, List<String> attributes, int cores, int fragments, float essence, float maxEssence,
    List<Long> ready, List<String> held, long time
) implements CustomPacketPayload {
    public static final SoulPayload EMPTY = new SoulPayload(0, "", 0, "", List.of(), 0, 0, 0.0F, 0.0F, List.of(0L, 0L, 0L, 0L, 0L), List.of(), 0L);
    public static final Type<SoulPayload> TYPE = new Type<>(NightmareEvolved.id("soul"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoulPayload> CODEC = StreamCodec.of(SoulPayload::write, SoulPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SoulPayload p) {
        buf.writeVarInt(p.rank);
        buf.writeUtf(p.aspect, 64);
        buf.writeVarInt(p.aspectTier);
        buf.writeUtf(p.flaw, 64);
        buf.writeVarInt(p.attributes.size());
        p.attributes.forEach(a -> buf.writeUtf(a, 64));
        buf.writeVarInt(p.cores);
        buf.writeVarInt(p.fragments);
        buf.writeFloat(p.essence);
        buf.writeFloat(p.maxEssence);
        buf.writeVarInt(p.ready.size());
        p.ready.forEach(buf::writeVarLong);
        buf.writeVarInt(p.held.size());
        p.held.forEach(h -> buf.writeUtf(h, 64));
        buf.writeVarLong(p.time);
    }

    private static SoulPayload read(RegistryFriendlyByteBuf buf) {
        int rank = buf.readVarInt();
        String aspect = buf.readUtf(64);
        int aspectTier = buf.readVarInt();
        String flaw = buf.readUtf(64);
        List<String> attributes = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) {
            attributes.add(buf.readUtf(64));
        }

        int cores = buf.readVarInt();
        int fragments = buf.readVarInt();
        float essence = buf.readFloat();
        float maxEssence = buf.readFloat();
        List<Long> ready = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) {
            ready.add(buf.readVarLong());
        }

        List<String> held = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) {
            held.add(buf.readUtf(64));
        }

        return new SoulPayload(rank, aspect, aspectTier, flaw, attributes, cores, fragments, essence, maxEssence, ready, held, buf.readVarLong());
    }

    public static SoulPayload of(ServerPlayer player, SoulData soul) {
        Aspect aspect = Aspects.byId(soul.aspect());
        List<Long> ready = new ArrayList<>();
        for (Stage stage : Stage.values()) {
            Ability ability = aspect == null ? null : aspect.at(stage);
            ready.add(ability == null ? 0L : soul.readyAt(ability.id));
        }

        List<String> held = new ArrayList<>(soul.toggles());
        if (!soul.form().isEmpty()) {
            held.add(soul.form());
        }

        return new SoulPayload(
            soul.rank().ordinal(), soul.aspect(), soul.aspectTier().ordinal(), soul.flaw(), soul.attributes(), soul.cores(), soul.fragments(),
            (float)soul.essence(), (float)soul.maxEssence(), ready, held, player.level().getGameTime()
        );
    }

    /** The game time at which the ability on a step's key (0 to 4) is ready again. */
    public long readyAt(int slot) {
        return slot >= 0 && slot < this.ready.size() ? this.ready.get(slot) : 0L;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
