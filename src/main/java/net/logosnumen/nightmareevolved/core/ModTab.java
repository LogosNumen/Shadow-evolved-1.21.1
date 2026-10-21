package net.logosnumen.nightmareevolved.core;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.item.ModItems;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The mod's creative tab. */
public final class ModTab {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NightmareEvolved.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
        "main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nightmare_evolved"))
            .icon(() -> new ItemStack(Colossi.CAIRN_EGG.get()))
            .displayItems((params, output) -> {
                output.accept(Colossi.CAIRN_EGG.get());
                output.accept(Colossi.OSSUARY_EGG.get());
                output.accept(Colossi.GRAVEWYRM_EGG.get());
                output.accept(Colossi.CHAINBOUND_EGG.get());
                for (CreatureRank rank : CreatureRank.values()) {
                    output.accept(ModItems.shard(rank, 1));
                }
                for (MemoryDef def : Memories.named()) {
                    output.accept(MemoryItems.display(def));
                }

                output.accept(MemoryItems.OATH_KEY.get());
                for (Item item : DreamBlocks.items()) {
                    output.accept(item);
                }
            })
            .build()
    );

    private ModTab() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
