package net.logosnumen.nightmareevolved.network;

import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.client.anim.ClientPoses;
import net.logosnumen.nightmareevolved.client.memory.ClientWorn;
import net.logosnumen.nightmareevolved.client.soul.ClientSea;
import net.logosnumen.nightmareevolved.client.soul.ClientSoul;
import net.logosnumen.nightmareevolved.client.soulsea.MeditationOverlay;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** The mod's messages between server and clients. */
public final class Network {
    private Network() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(PosePayload.TYPE, PosePayload.CODEC, Network::onPose);
        registrar.playToClient(SoulPayload.TYPE, SoulPayload.CODEC, Network::onSoul);
        registrar.playToServer(AbilityPayload.TYPE, AbilityPayload.CODEC, Network::onAbility);
        registrar.playToClient(SeaPayload.TYPE, SeaPayload.CODEC, Network::onSea);
        registrar.playToServer(SoulActionPayload.TYPE, SoulActionPayload.CODEC, Network::onAction);
        registrar.playToClient(MeditationPayload.TYPE, MeditationPayload.CODEC, Network::onMeditation);
        registrar.playToClient(WornPayload.TYPE, WornPayload.CODEC, Network::onWorn);
    }

    private static void onWorn(WornPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientWorn.set(payload));
    }

    private static void onMeditation(MeditationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> MeditationOverlay.start(payload.ticks()));
    }

    private static void onSea(SeaPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSea.set(payload));
    }

    private static void onAction(SoulActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.isAlive()) {
                SoulActions.handle(player, payload);
            }
        });
    }

    private static void onSoul(SoulPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSoul.set(payload));
    }

    private static void onAbility(AbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Aspects.use(player, payload.slot());
            }
        });
    }

    private static void onPose(PosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientPoses.start(payload.entityId(), payload.clip()));
    }

    /** Sends a message to every player who can see an entity, and to the entity itself if it is a player. */
    public static void toTrackingAndSelf(Entity entity, PosePayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }
}
