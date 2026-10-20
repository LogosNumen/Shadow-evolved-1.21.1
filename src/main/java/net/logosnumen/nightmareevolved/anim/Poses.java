package net.logosnumen.nightmareevolved.anim;

import net.logosnumen.nightmareevolved.network.Network;
import net.logosnumen.nightmareevolved.network.PosePayload;
import net.minecraft.world.entity.LivingEntity;

/**
 * Plays body animations on the server's word. The clips themselves (a thrust, a cut, a spin, a roar, a slam, a guard,
 * raised arms and more) live on the client; this names one and sends it to everyone who can see the body move.
 */
public final class Poses {
    private Poses() {
    }

    /** Plays a clip on an entity for everyone who can see it. */
    public static void play(LivingEntity entity, String clip) {
        if (!entity.level().isClientSide() && !clip.isEmpty()) {
            Network.toTrackingAndSelf(entity, new PosePayload(entity.getId(), clip));
        }
    }
}
