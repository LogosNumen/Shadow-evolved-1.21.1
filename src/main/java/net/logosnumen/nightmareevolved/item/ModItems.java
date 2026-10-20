package net.logosnumen.nightmareevolved.item;

import com.mojang.serialization.Codec;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The mod's items, and the data they carry. */
public final class ModItems {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NightmareEvolved.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NightmareEvolved.MOD_ID);
    /** The creature rank a soul shard came from (Dormant 0 to Unholy 6). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHARD_RANK = COMPONENTS.register(
        "shard_rank", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build()
    );
    public static final DeferredItem<SoulShardItem> SOUL_SHARD = ITEMS.register("soul_shard", () -> new SoulShardItem(new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ITEMS.register(modBus);
    }

    /** Soul shards of a creature rank. */
    public static ItemStack shard(CreatureRank rank, int count) {
        ItemStack stack = new ItemStack(SOUL_SHARD.get(), count);
        stack.set(SHARD_RANK.get(), rank.ordinal());
        return stack;
    }

    public static int shardRank(ItemStack stack) {
        Integer rank = stack.get(SHARD_RANK.get());
        return rank != null ? rank : 0;
    }
}
