package net.logosnumen.nightmareevolved.client;

import net.logosnumen.nightmareevolved.client.anim.PoseEvents;
import net.logosnumen.nightmareevolved.client.colossus.ColossusRenderer;
import net.logosnumen.nightmareevolved.client.particle.VfxParticle;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.vfx.ModParticles;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only wiring: entity renderers, the effect particles, and the hooks body animation needs each frame. */
public final class ClientSetup {
    private ClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientSetup::onRegisterRenderers);
        modBus.addListener(ClientSetup::onRegisterParticles);
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
