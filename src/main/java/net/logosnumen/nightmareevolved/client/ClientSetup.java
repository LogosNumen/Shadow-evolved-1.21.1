package net.logosnumen.nightmareevolved.client;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.client.anim.PoseEvents;
import net.logosnumen.nightmareevolved.client.creature.CreatureBoltRenderer;
import net.logosnumen.nightmareevolved.client.creature.CreatureRenderer;
import net.logosnumen.nightmareevolved.client.dream.ClientRealm;
import net.logosnumen.nightmareevolved.client.dream.DreamSky;
import net.logosnumen.nightmareevolved.client.dream.NightmareGateRenderer;
import net.logosnumen.nightmareevolved.client.dream.RealmFog;
import net.logosnumen.nightmareevolved.client.dream.RealmRender;
import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.client.memory.ClientWorn;
import net.logosnumen.nightmareevolved.client.memory.WalkingCofferRenderer;
import net.logosnumen.nightmareevolved.client.memory.WornMemoryLayer;
import net.logosnumen.nightmareevolved.client.soul.AbilityKeys;
import net.logosnumen.nightmareevolved.client.soul.ClientSea;
import net.logosnumen.nightmareevolved.client.soulsea.MeditationOverlay;
import net.logosnumen.nightmareevolved.client.soulsea.SeaEchoRenderer;
import net.logosnumen.nightmareevolved.client.soulsea.SeaHoardRenderer;
import net.logosnumen.nightmareevolved.client.soulsea.SeaMemoryRenderer;
import net.logosnumen.nightmareevolved.client.soulsea.SeaShadowRenderer;
import net.logosnumen.nightmareevolved.client.soulsea.SeaSunRenderer;
import net.logosnumen.nightmareevolved.client.soulsea.SoulSeaClient;
import net.logosnumen.nightmareevolved.client.soulsea.SoulSeaSky;
import net.logosnumen.nightmareevolved.client.soulsea.VesselRenderer;
import net.logosnumen.nightmareevolved.dream.seed.SeedEntities;
import net.logosnumen.nightmareevolved.entity.creature.CreatureTypes;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.logosnumen.nightmareevolved.entity.memory.MemoryEntities;
import net.logosnumen.nightmareevolved.entity.soulsea.SeaEntities;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.logosnumen.nightmareevolved.client.soul.ClientSoul;
import net.logosnumen.nightmareevolved.client.soul.FlyingSwordRenderer;
import net.logosnumen.nightmareevolved.client.soul.SoulConstructRenderer;
import net.logosnumen.nightmareevolved.client.soul.SoulHud;
import net.logosnumen.nightmareevolved.item.ModItems;
import net.logosnumen.nightmareevolved.item.SoulShardItem;
import net.logosnumen.nightmareevolved.client.colossus.ColossusRenderer;
import net.logosnumen.nightmareevolved.client.particle.VfxParticle;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.entity.soul.SoulEntities;
import net.logosnumen.nightmareevolved.vfx.ModParticles;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
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
        modBus.addListener(ClientSetup::onAddLayers);
        modBus.addListener(ClientSetup::onDimensionEffects);
        modBus.addListener(ClientSetup::onClientSetup);
        NeoForge.EVENT_BUS.addListener(SoulSeaClient::onRenderFog);
        NeoForge.EVENT_BUS.addListener(SoulSeaClient::onFogColour);
        NeoForge.EVENT_BUS.addListener(RealmFog::onRenderFog);
        NeoForge.EVENT_BUS.addListener(RealmFog::onFogColour);
        NeoForge.EVENT_BUS.addListener(ClientRealm::onClientTick);
        NeoForge.EVENT_BUS.addListener(RealmRender::onRenderLevel);
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
        event.registerEntityRenderer(SoulEntities.CONSTRUCT.get(), SoulConstructRenderer::new);
        event.registerEntityRenderer(SoulEntities.FLYING_SWORD.get(), FlyingSwordRenderer::new);
        event.registerEntityRenderer(MemoryEntities.THROWN_MEMORY.get(), context -> new ThrownItemRenderer<>(context, 1.2F, false));
        event.registerEntityRenderer(MemoryEntities.WALKING_COFFER.get(), WalkingCofferRenderer::new);
        event.registerEntityRenderer(SeaEntities.VESSEL.get(), VesselRenderer::new);
        event.registerEntityRenderer(SeaEntities.SUN.get(), SeaSunRenderer::new);
        event.registerEntityRenderer(SeaEntities.MEMORY.get(), SeaMemoryRenderer::new);
        event.registerEntityRenderer(SeaEntities.ECHO.get(), SeaEchoRenderer::new);
        event.registerEntityRenderer(SeaEntities.SHADOW.get(), SeaShadowRenderer::new);
        event.registerEntityRenderer(SeaEntities.HOARD.get(), SeaHoardRenderer::new);
        event.registerEntityRenderer(SeedEntities.GATE.get(), NightmareGateRenderer::new);
        for (EntityType<DreamCreature> type : CreatureTypes.types()) {
            event.registerEntityRenderer(type, CreatureRenderer::new);
        }

        event.registerEntityRenderer(CreatureTypes.BOLT.get(), CreatureBoltRenderer::new);
    }

    /** What players wear from their Memories, drawn on every player model. */
    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : PlayerSkin.Model.values()) {
            if (event.getSkin(model) instanceof PlayerRenderer renderer) {
                WornMemoryLayer.add(renderer);
            }
        }
    }

    private static void onDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(NightmareEvolved.id("soul_sea"), new SoulSeaSky());
        event.register(NightmareEvolved.id("dream_realm"), new DreamSky());
    }

    /** Memory bows draw like bows and Memory shields block like shields. */
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Supplier<? extends Item> memory : MemoryItems.all()) {
                Item item = memory.get();
                if (item instanceof BowItem) {
                    ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) -> entity == null
                        || entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F);
                    ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, seed) -> entity != null
                        && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
                } else if (item instanceof ShieldItem) {
                    ItemProperties.register(item, ResourceLocation.withDefaultNamespace("blocking"), (stack, level, entity, seed) -> entity != null
                        && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
                }
            }
        });
    }

    private static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, NightmareEvolved.id("soul"), SoulHud::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, NightmareEvolved.id("meditation"), MeditationOverlay::render);
        event.registerBelow(VanillaGuiLayers.HOTBAR, NightmareEvolved.id("crushing"), RealmFog::renderPressure);
    }

    private static void onRegisterItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? SoulShardItem.colour(stack) : -1, ModItems.SOUL_SHARD.get());
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSoul.clear();
        ClientSea.clear();
        ClientWorn.clear();
        ClientRealm.clear();
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
