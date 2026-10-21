package net.logosnumen.nightmareevolved.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.echo.EchoRecord;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * What a player's client needs to show their Memories, Echoes and Soul Sea: each Memory (as its item, so its runes
 * show), which pieces are out and how worn it is and what it costs; each Echo (its kind, Rank, class, Attributes, how
 * whole it is, whether it is out, a Shadow, or asleep, and whether it could become a Shadow now); and whether the soul
 * can keep things in its sea.
 */
public record SeaPayload(List<MemoryEntry> memories, List<EchoEntry> echoes, boolean vault, String citadel, int echoLimit) implements CustomPacketPayload {
    public static final Type<SeaPayload> TYPE = new Type<>(NightmareEvolved.id("sea"));
    public static final SeaPayload EMPTY = new SeaPayload(List.of(), List.of(), false, "", 0);
    public static final StreamCodec<RegistryFriendlyByteBuf, SeaPayload> CODEC = StreamCodec.of(SeaPayload::write, SeaPayload::read);

    public record MemoryEntry(UUID id, ItemStack icon, List<ItemStack> pieces, int out, float condition, float cost, int carry, boolean relic) {
    }

    public record EchoEntry(UUID id, String type, Component name, int rank, int cls, List<String> traits, float health, boolean out, boolean shadow,
        long sleeps, float cost, boolean canShadow) {
    }

    private static void write(RegistryFriendlyByteBuf buf, SeaPayload p) {
        buf.writeVarInt(p.memories.size());
        for (MemoryEntry m : p.memories) {
            UUIDUtil.STREAM_CODEC.encode(buf, m.id);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, m.icon);
            buf.writeVarInt(m.pieces.size());
            m.pieces.forEach(stack -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack));
            buf.writeVarInt(m.out);
            buf.writeFloat(m.condition);
            buf.writeFloat(m.cost);
            buf.writeVarInt(m.carry);
            buf.writeBoolean(m.relic);
        }

        buf.writeVarInt(p.echoes.size());
        for (EchoEntry e : p.echoes) {
            UUIDUtil.STREAM_CODEC.encode(buf, e.id);
            buf.writeUtf(e.type, 128);
            ComponentSerialization.STREAM_CODEC.encode(buf, e.name);
            buf.writeVarInt(e.rank);
            buf.writeVarInt(e.cls);
            buf.writeVarInt(e.traits.size());
            e.traits.forEach(t -> buf.writeUtf(t, 64));
            buf.writeFloat(e.health);
            buf.writeBoolean(e.out);
            buf.writeBoolean(e.shadow);
            buf.writeVarLong(e.sleeps);
            buf.writeFloat(e.cost);
            buf.writeBoolean(e.canShadow);
        }

        buf.writeBoolean(p.vault);
        buf.writeUtf(p.citadel, 128);
        buf.writeVarInt(p.echoLimit);
    }

    private static SeaPayload read(RegistryFriendlyByteBuf buf) {
        List<MemoryEntry> memories = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            List<ItemStack> pieces = new ArrayList<>();
            for (int j = buf.readVarInt(); j > 0; j--) {
                pieces.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }

            memories.add(new MemoryEntry(id, icon, pieces, buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readBoolean()));
        }

        List<EchoEntry> echoes = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            String type = buf.readUtf(128);
            Component name = ComponentSerialization.STREAM_CODEC.decode(buf);
            int rank = buf.readVarInt();
            int cls = buf.readVarInt();
            List<String> traits = new ArrayList<>();
            for (int j = buf.readVarInt(); j > 0; j--) {
                traits.add(buf.readUtf(64));
            }

            echoes.add(new EchoEntry(id, type, name, rank, cls, traits, buf.readFloat(), buf.readBoolean(), buf.readBoolean(), buf.readVarLong(),
                buf.readFloat(), buf.readBoolean()));
        }

        return new SeaPayload(memories, echoes, buf.readBoolean(), buf.readUtf(128), buf.readVarInt());
    }

    public static SeaPayload of(SoulData soul) {
        List<MemoryEntry> memories = new ArrayList<>();
        for (MemoryRecord record : soul.memories()) {
            MemoryData data = record.data();
            MemoryDef def = record.def();
            int out = 0;
            float condition = 1.0F;
            List<ItemStack> pieces = new ArrayList<>();
            for (int i = 0; i < record.pieces.size(); i++) {
                MemoryRecord.Piece piece = record.pieces.get(i);
                out |= piece.out ? 1 << i : 0;
                pieces.add(piece.stack.copy());
                if (piece.stack.isDamageableItem()) {
                    condition = Math.min(condition, 1.0F - (float)piece.stack.getDamageValue() / piece.stack.getMaxDamage());
                }
            }

            ItemStack icon = record.pieces.get(def != null && def.armor() && record.pieces.size() > 1 ? 1 : 0).stack.copy();
            memories.add(new MemoryEntry(record.id, icon, pieces, out, condition, (float)SoulMemories.cost(data, soul),
                def == null ? 0 : def.carry.ordinal(), data.has(Enchant.SOULBOUND_RELIC)));
        }

        List<EchoEntry> echoes = new ArrayList<>();
        for (EchoRecord record : soul.echoes()) {
            echoes.add(new EchoEntry(record.id, record.type, Echoes.name(record), record.rank, record.cls, record.traits, record.health, record.out,
                record.shadow, record.sleepsUntil, (float)Echoes.cost(record, soul), Echoes.whyNotShadow(soul, record) == null));
        }

        return new SeaPayload(memories, echoes, false, "", Echoes.limit(soul));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
