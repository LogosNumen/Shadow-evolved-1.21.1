package net.logosnumen.nightmareevolved.network;

import net.logosnumen.nightmareevolved.client.anim.ClientPoses;
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
    }

    private static void onPose(PosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientPoses.start(payload.entityId(), payload.clip()));
    }

    /** Sends a message to every player who can see an entity, and to the entity itself if it is a player. */
    public static void toTrackingAndSelf(Entity entity, PosePayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }
}
