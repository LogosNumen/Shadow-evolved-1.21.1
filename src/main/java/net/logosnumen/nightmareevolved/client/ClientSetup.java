package net.logosnumen.nightmareevolved.client;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.client.anim.PoseEvents;
import net.logosnumen.nightmareevolved.client.soul.AbilityKeys;
import net.logosnumen.nightmareevolved.client.soul.ClientSoul;
import net.logosnumen.nightmareevolved.client.soul.SoulHud;
import net.logosnumen.nightmareevolved.item.ModItems;
import net.logosnumen.nightmareevolved.item.SoulShardItem;
import net.logosnumen.nightmareevolved.client.colossus.ColossusRenderer;
import net.logosnumen.nightmareevolved.client.particle.VfxParticle;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.vfx.ModParticles;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only wiring: entity renderers, the effect particles, the hooks body animation needs each frame, the soul HUD,
 * the ability keys and the tint of soul shards.
 */
public final class ClientSetup {
    private ClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientSetup::onRegisterRenderers);
        modBus.addListener(ClientSetup::onRegisterParticles);
        modBus.addListener(ClientSetup::onRegisterLayers);
        modBus.addListener(ClientSetup::onRegisterItemColours);
        modBus.addListener(AbilityKeys::onRegister);
        NeoForge.EVENT_BUS.addListener(AbilityKeys::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(VfxParticle::onRenderLevel);
        // Pushes and pops must pair: the push listens last, so nothing can cancel the render after it.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PoseEvents::onRenderPlayerPre);
        NeoForge.EVENT_BUS.addListener(PoseEvents::onRenderPlayerPost);
        NeoForge.EVENT_BUS.addListener(PoseEvents::onCameraAngles);
        NeoForge.EVENT_BUS.addListener(PoseEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(PoseEvents::onLoggingOut);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Colossi.CAIRN.get(), ColossusRenderer::new);
        event.registerEntityRenderer(Colossi.OSSUARY.get(), ColossusRenderer::new);
        event.registerEntityRenderer(Colossi.GRAVEWYRM.get(), ColossusRenderer::new);
        event.registerEntityRenderer(Colossi.CHAINBOUND.get(), ColossusRenderer::new);
        event.registerEntityRenderer(Colossi.BOULDER.get(), context -> new ThrownItemRenderer<>(context, 7.0F, false));
    }

    private static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, NightmareEvolved.id("soul"), SoulHud::render);
    }

    private static void onRegisterItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? SoulShardItem.colour(stack) : -1, ModItems.SOUL_SHARD.get());
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSoul.clear();
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MOTE.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.MOTE));
        event.registerSpriteSet(ModParticles.SPARK.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.SPARK));
        event.registerSpriteSet(ModParticles.EMBER.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.EMBER));
        event.registerSpriteSet(ModParticles.WISP.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.WISP));
        event.registerSpriteSet(ModParticles.GLYPH.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.GLYPH));
        event.registerSpriteSet(ModParticles.PULSE.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.PULSE));
        event.registerSpriteSet(ModParticles.SHARD.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.SHARD));
        event.registerSpriteSet(ModParticles.RING.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.RING));
        event.registerSpriteSet(ModParticles.HALO.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.HALO));
        event.registerSpriteSet(ModParticles.SIGIL.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.SIGIL));
        event.registerSpriteSet(ModParticles.SWEEP.get(), sprites -> new VfxParticle.Provider(sprites, VfxParticle.Kind.SWEEP));
    }
}
