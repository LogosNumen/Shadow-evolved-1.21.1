package net.logosnumen.nightmareevolved.client.anim;

import java.util.HashSet;
import java.util.Set;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Client hooks for body animation: whole-body turns around each player, the view's shake, and the clock. */
public final class PoseEvents {
    /** Players drawn this frame with a turn or tilt pushed, to pop it again once they are drawn. */
    private static final Set<Integer> PUSHED = new HashSet<>();

    private PoseEvents() {
    }

    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (ClientPoses.pushRoot(event.getPoseStack(), event.getEntity(), event.getPartialTick())) {
            PUSHED.add(event.getEntity().getId());
        }
    }

    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (PUSHED.remove(event.getEntity().getId())) {
            event.getPoseStack().popPose();
        }
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float[] shake = ClientPoses.shake((float)event.getPartialTick());
        if (shake[0] != 0.0F || shake[1] != 0.0F) {
            event.setRoll(event.getRoll() + shake[0]);
            event.setPitch(event.getPitch() + shake[1]);
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        ClientPoses.tick();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientPoses.clear();
        PUSHED.clear();
    }
}
