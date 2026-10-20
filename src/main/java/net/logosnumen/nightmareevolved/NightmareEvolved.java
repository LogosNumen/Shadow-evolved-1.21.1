package net.logosnumen.nightmareevolved;

import net.logosnumen.nightmareevolved.aspect.AspectEvents;
import net.logosnumen.nightmareevolved.client.ClientSetup;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.command.NeCommands;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.core.ModTab;
import net.logosnumen.nightmareevolved.core.Scheduler;
import net.logosnumen.nightmareevolved.creature.CreatureEvents;
import net.logosnumen.nightmareevolved.item.ModItems;
import net.logosnumen.nightmareevolved.network.Network;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.SoulEvents;
import net.logosnumen.nightmareevolved.vfx.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Nightmare Evolved: a fan-made Shadow Slave mod. This is where everything is wired together: particles, the
 * colossi, the soul and its Aspect, Nightmare Creatures and soul shards, the Nightmares themselves, the creative tab,
 * networking, the task scheduler, commands, and (on clients) rendering and the HUD.
 */
@Mod(NightmareEvolved.MOD_ID)
public final class NightmareEvolved {
    public static final String MOD_ID = "nightmare_evolved";

    public NightmareEvolved(IEventBus modBus, ModContainer container) {
        ModParticles.register(modBus);
        Colossi.register(modBus);
        ModTab.register(modBus);
        ModAttachments.register(modBus);
        ModItems.register(modBus);
        modBus.addListener(Network::register);
        NeoForge.EVENT_BUS.addListener(Scheduler::onServerTickStart);
        NeoForge.EVENT_BUS.addListener(Scheduler::onServerTick);
        NeoForge.EVENT_BUS.addListener(Scheduler::onServerStopped);
        NeoForge.EVENT_BUS.addListener(NeCommands::register);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onRespawn);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onChangeDimension);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onTick);
        NeoForge.EVENT_BUS.addListener(CreatureEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(CreatureEvents::onDrops);
        NeoForge.EVENT_BUS.addListener(CreatureEvents::onFinalizeSpawn);
        NeoForge.EVENT_BUS.addListener(AspectEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(Nightmares::onServerTick);
        NeoForge.EVENT_BUS.addListener(Nightmares::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(Nightmares::onDeath);
        NeoForge.EVENT_BUS.addListener(Nightmares::onExperienceDrop);
        NeoForge.EVENT_BUS.addListener(Nightmares::onRespawn);
        NeoForge.EVENT_BUS.addListener(Nightmares::onLogout);
        NeoForge.EVENT_BUS.addListener(Nightmares::onLogin);
        NeoForge.EVENT_BUS.addListener(Nightmares::onServerStopped);
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.register(modBus);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
