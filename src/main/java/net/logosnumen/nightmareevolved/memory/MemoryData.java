package net.logosnumen.nightmareevolved.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * What makes an item a Memory: which Memory it is ({@code id}, the same for every piece of an armour Memory), what it
 * was woven as ({@code def}), its name if the Spell named it after its prey, its Rank (Dormant 0 to Divine 6) and Tier
 * (I to VII, the class of the creature that left it), its enchantments, whose soul it belongs to, and which summoning
 * this copy came from. A copy whose {@code nonce} no longer matches its owner's soul is a leftover of an earlier
 * summoning and dissolves.
 */
public record MemoryData(UUID id, String def, String name, int rank, int tier, List<String> enchants, UUID owner, int nonce) {
    /** The owner of a Memory nobody holds: a Shard Memory outliving its owner. */
    public static final UUID NOBODY = Util.NIL_UUID;
    public static final Codec<MemoryData> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(MemoryData::id),
        Codec.STRING.fieldOf("def").forGetter(MemoryData::def),
        Codec.STRING.optionalFieldOf("name", "").forGetter(MemoryData::name),
        Codec.INT.fieldOf("rank").forGetter(MemoryData::rank),
        Codec.INT.fieldOf("tier").forGetter(MemoryData::tier),
        Codec.STRING.listOf().optionalFieldOf("enchants", List.of()).forGetter(MemoryData::enchants),
        UUIDUtil.CODEC.optionalFieldOf("owner", NOBODY).forGetter(MemoryData::owner),
        Codec.INT.optionalFieldOf("nonce", 0).forGetter(MemoryData::nonce)
    ).apply(i, MemoryData::new));
    public static final StreamCodec<ByteBuf, MemoryData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public MemoryData {
        enchants = List.copyOf(enchants);
        rank = Math.max(0, Math.min(6, rank));
        tier = Math.max(1, Math.min(7, tier));
    }

    public boolean owned() {
        return !NOBODY.equals(this.owner);
    }

    public MemoryData withOwner(UUID owner) {
        return new MemoryData(this.id, this.def, this.name, this.rank, this.tier, this.enchants, owner, this.nonce);
    }

    public MemoryData withNonce(int nonce) {
        return new MemoryData(this.id, this.def, this.name, this.rank, this.tier, this.enchants, this.owner, nonce);
    }

    public MemoryData withRank(int rank) {
        return new MemoryData(this.id, this.def, this.name, rank, this.tier, this.enchants, this.owner, this.nonce);
    }

    public boolean has(Enchant enchant) {
        return this.enchants.contains(enchant.id());
    }
}
