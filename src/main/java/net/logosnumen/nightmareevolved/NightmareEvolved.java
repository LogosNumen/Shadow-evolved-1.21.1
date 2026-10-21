package net.logosnumen.nightmareevolved;

import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.client.ClientSetup;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.command.NeCommands;
import net.logosnumen.nightmareevolved.command.SpellCommand;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.core.ModSounds;
import net.logosnumen.nightmareevolved.core.ModTab;
import net.logosnumen.nightmareevolved.core.Scheduler;
import net.logosnumen.nightmareevolved.creature.CreatureEvents;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.DreamTravel;
import net.logosnumen.nightmareevolved.dream.law.RealmLaws;
import net.logosnumen.nightmareevolved.echo.EchoEvents;
import net.logosnumen.nightmareevolved.entity.memory.MemoryEntities;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEntities;
import net.logosnumen.nightmareevolved.entity.soul.SoulEntities;
import net.logosnumen.nightmareevolved.flaw.FlawEvents;
import net.logosnumen.nightmareevolved.item.ModItems;
import net.logosnumen.nightmareevolved.memory.MemoryEvents;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.logosnumen.nightmareevolved.network.Network;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.SoulEvents;
import net.logosnumen.nightmareevolved.soulsea.SeaBlocks;
import net.logosnumen.nightmareevolved.soulsea.SoulSeaEvents;
import net.logosnumen.nightmareevolved.vfx.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Nightmare Evolved: a fan-made Shadow Slave mod. This is where everything is wired together: particles, the
 * colossi, the soul and its Aspect, Memories, Nightmare Creatures and soul shards, the Nightmares themselves, the
 * creative tab, networking, the task scheduler, commands, and (on clients) rendering and the HUD.
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
        SoulEntities.register(modBus);
        ModSounds.register(modBus);
        MemoryItems.register(modBus);
        MemoryEntities.register(modBus);
        SeaBlocks.register(modBus);
        SeaEntities.register(modBus);
        DreamBlocks.register(modBus);
        DreamRealm.register(modBus);
        modBus.addListener(Network::register);
        NeoForge.EVENT_BUS.addListener(Scheduler::onServerTickStart);
        NeoForge.EVENT_BUS.addListener(Scheduler::onServerTick);
        NeoForge.EVENT_BUS.addListener(Scheduler::onServerStopped);
        NeoForge.EVENT_BUS.addListener(NeCommands::register);
        NeoForge.EVENT_BUS.addListener(SpellCommand::register);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onRespawn);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onChangeDimension);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onTick);
        NeoForge.EVENT_BUS.addListener(CreatureEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(CreatureEvents::onDrops);
        NeoForge.EVENT_BUS.addListener(CreatureEvents::onFinalizeSpawn);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(SoulEvents::onDeath);
        NeoForge.EVENT_BUS.addListener(Afflictions::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(Afflictions::onEntityTick);
        NeoForge.EVENT_BUS.addListener(Bound::onChangeTarget);
        NeoForge.EVENT_BUS.addListener(Bound::onEntityTick);
        NeoForge.EVENT_BUS.addListener(FlawEvents::onChat);
        NeoForge.EVENT_BUS.addListener(FlawEvents::onNameFormat);
        NeoForge.EVENT_BUS.addListener(FlawEvents::onTabName);
        NeoForge.EVENT_BUS.addListener(FlawEvents::onInteract);
        NeoForge.EVENT_BUS.addListener(FlawEvents::onBreak);
        NeoForge.EVENT_BUS.addListener(FlawEvents::onDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, MemoryEvents::onPlayerDeath);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onKill);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onShieldBlock);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onInteract);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onChat);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, MemoryEvents::onNameFormat);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, MemoryEvents::onTabName);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onGameEvent);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onLogout);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(MemoryEvents::onStartTracking);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EchoEvents::onDrops);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onExperience);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onDeath);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onLogout);
        NeoForge.EVENT_BUS.addListener(EchoEvents::onChangeDimension);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, SoulSeaEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onPlace);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onBreak);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onUseOnBlock);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onToss);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onLogout);
        NeoForge.EVENT_BUS.addListener(SoulSeaEvents::onServerStopped);
        NeoForge.EVENT_BUS.addListener(DreamTravel::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamTravel::onRespawn);
        NeoForge.EVENT_BUS.addListener(DreamTravel::onLogout);
        NeoForge.EVENT_BUS.addListener(DreamTravel::onServerStopped);
        NeoForge.EVENT_BUS.addListener(RealmLaws::onServerTick);
        NeoForge.EVENT_BUS.addListener(RealmLaws::onLogout);
        NeoForge.EVENT_BUS.addListener(RealmLaws::onChangeDimension);
        NeoForge.EVENT_BUS.addListener(RealmLaws::onServerStopped);
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
