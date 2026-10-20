package net.logosnumen.nightmareevolved.client.anim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Body animation on the client: the clips entities are playing, how players hold themselves in flight, the
 * first-person hand, and the shake big moments send through the view. Clips arrive from the server; flight is told
 * by the wings state the server sends for each player.
 */
public final class ClientPoses {
    private record Active(PoseClip clip, float start) {
        float progress(float age) {
            return (age - this.start) / this.clip.duration();
        }
    }

    /** One player's wings and flight, eased tick by tick so poses never snap. */
    public static final class Flight {
        public int element = -1;
        public boolean flying;
        float tilt;
        float prevTilt;
        float open;
        float prevOpen;
        float blend;
        float prevBlend;

        public float tilt(float partial) {
            return Mth.lerp(partial, this.prevTilt, this.tilt);
        }

        public float open(float partial) {
            return Mth.lerp(partial, this.prevOpen, this.open);
        }

        public float blend(float partial) {
            return Mth.lerp(partial, this.prevBlend, this.blend);
        }
    }

    private record Shake(long due, float strength) {
    }

    private static final Map<Integer, Active> ACTIVE = new HashMap<>();
    private static final Map<UUID, Flight> FLIGHT = new HashMap<>();
    private static final List<Shake> SHAKES = new ArrayList<>();
    private static float shake;
    private static float prevShake;
    private static long ticks;

    private ClientPoses() {
    }

    /** Starts a clip on an entity, and queues its shake for whoever is close enough to feel it. */
    public static void start(int entityId, String clipId) {
        PoseClip clip = PoseClips.get(clipId);
        Minecraft mc = Minecraft.getInstance();
        if (clip == null || mc.level == null) {
            return;
        }

        Entity entity = mc.level.getEntity(entityId);
        ACTIVE.put(entityId, new Active(clip, entity != null ? entity.tickCount : 0));
        if (clip.shake > 0.0F && entity != null && mc.player != null) {
            float near = 1.0F - (float)Math.min(1.0, mc.player.distanceTo(entity) / 24.0);
            if (near > 0.0F) {
                SHAKES.add(new Shake(ticks + Math.round(clip.shakeAt * clip.duration()), clip.shake * near));
            }
        }
    }

    public static Flight flight(UUID player) {
        return FLIGHT.computeIfAbsent(player, id -> new Flight());
    }

    @Nullable
    public static Flight flightIfAny(UUID player) {
        return FLIGHT.get(player);
    }

    public static void setWings(UUID player, int element, boolean flying) {
        Flight flight = flight(player);
        flight.element = element;
        flight.flying = flying;
    }

    @Nullable
    private static PoseClip.Sample sample(Entity entity, float age) {
        Active active = ACTIVE.get(entity.getId());
        if (active == null) {
            return null;
        }

        float progress = active.progress(age);
        return progress < 0.0F || progress > 1.0F && !active.clip().holdsAtEnd() ? null : active.clip().sample(progress);
    }

    /** A clip an entity is playing: its id, how far through it is (0 to 1), and the pose it gives there. */
    public record Playing(String clip, float progress, PoseClip.Sample sample) {
    }

    /** What clip an entity is playing at this moment, for bodies that read it their own way; null when none. */
    @Nullable
    public static Playing playing(Entity entity, float age) {
        Active active = ACTIVE.get(entity.getId());
        if (active == null) {
            return null;
        }

        float progress = active.progress(age);
        if (progress < 0.0F || progress > 1.0F && !active.clip().holdsAtEnd()) {
            return null;
        }

        float at = Math.min(progress, 1.0F);
        return new Playing(active.clip().id(), at, active.clip().sample(at));
    }

    /** Called at the end of a humanoid model's own posing: flight first, then any clip over it. */
    public static void pose(HumanoidModel<?> model, LivingEntity entity, float ageInTicks) {
        float partial = ageInTicks - entity.tickCount;
        if (entity instanceof Player player) {
            Flight flight = FLIGHT.get(player.getUUID());
            if (flight != null) {
                poseFlight(model, flight, ageInTicks, partial);
            }
        }

        PoseClip.Sample sample = sample(entity, ageInTicks);
        if (sample != null) {
            blend(model.head, sample, PoseClip.Part.HEAD);
            blend(model.body, sample, PoseClip.Part.BODY);
            blend(model.rightArm, sample, PoseClip.Part.RIGHT_ARM);
            blend(model.leftArm, sample, PoseClip.Part.LEFT_ARM);
            blend(model.rightLeg, sample, PoseClip.Part.RIGHT_LEG);
            blend(model.leftLeg, sample, PoseClip.Part.LEFT_LEG);
            model.hat.copyFrom(model.head);
        }
    }

    private static void blend(ModelPart part, PoseClip.Sample sample, PoseClip.Part which) {
        float[] rotation = sample.rotation.get(which);
        if (rotation != null) {
            float w = sample.weight.getOrDefault(which, 0.0F);
            part.xRot = Mth.lerp(w, part.xRot, rotation[0]);
            part.yRot = Mth.lerp(w, part.yRot, rotation[1]);
            part.zRot = Mth.lerp(w, part.zRot, rotation[2]);
        }
    }

    private static void set(ModelPart part, float w, float x, float y, float z) {
        part.xRot = Mth.lerp(w, part.xRot, x * Mth.DEG_TO_RAD);
        part.yRot = Mth.lerp(w, part.yRot, y * Mth.DEG_TO_RAD);
        part.zRot = Mth.lerp(w, part.zRot, z * Mth.DEG_TO_RAD);
    }

    /** Hovering, the legs hang and sway and the arms float out; at speed, the body streams out straight. */
    private static void poseFlight(HumanoidModel<?> model, Flight flight, float age, float partial) {
        float w = flight.blend(partial);
        if (w <= 0.001F) {
            return;
        }

        float tilt = flight.tilt(partial);
        float sway = Mth.sin(age * 0.08F) * 6.0F * (1.0F - tilt);
        set(model.rightLeg, w, Mth.lerp(tilt, 10.0F + sway, 4.0F), 0.0F, Mth.lerp(tilt, 3.0F, 1.0F));
        set(model.leftLeg, w, Mth.lerp(tilt, 14.0F - sway, 6.0F), 0.0F, Mth.lerp(tilt, -3.0F, -1.0F));
        set(model.rightArm, w, Mth.lerp(tilt, -12.0F, 18.0F), 0.0F, Mth.lerp(tilt, 22.0F, 8.0F));
        set(model.leftArm, w, Mth.lerp(tilt, -12.0F, 18.0F), 0.0F, Mth.lerp(tilt, -22.0F, -8.0F));
        // Lying into the flight, the head lifts to keep looking ahead.
        model.head.xRot -= w * tilt * 55.0F * Mth.DEG_TO_RAD;
        model.hat.copyFrom(model.head);
    }

    /** Whole-body turns, tilts and lifts, applied around the body's middle before the player is drawn. */
    public static boolean pushRoot(PoseStack poseStack, Player player, float partial) {
        float age = player.tickCount + partial;
        float pitch = 0.0F;
        float yaw = 0.0F;
        float lift = 0.0F;
        Flight flight = FLIGHT.get(player.getUUID());
        if (flight != null) {
            float w = flight.blend(partial);
            float tilt = flight.tilt(partial);
            pitch += w * tilt * 70.0F;
            lift += w * (1.0F - tilt) * Mth.sin(age * 0.1F) * 0.06F;
        }

        PoseClip.Sample sample = sample(player, age);
        if (sample != null) {
            yaw += sample.root[0];
            pitch += sample.root[1];
            lift += sample.root[2];
        }

        if (pitch == 0.0F && yaw == 0.0F && lift == 0.0F) {
            return false;
        }

        float bodyYaw = 180.0F - Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
        float middle = player.getBbHeight() * 0.5F;
        poseStack.pushPose();
        poseStack.translate(0.0F, lift + middle, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw + yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        poseStack.translate(0.0F, -middle, 0.0F);
        return true;
    }

    /** Moves the first-person hand the way the local player's clip says; false when there is nothing to do. */
    public static boolean pushHand(PoseStack poseStack, Player player, HumanoidArm arm, float partial) {
        PoseClip.Sample sample = sample(player, player.tickCount + partial);
        if (sample == null || arm != player.getMainArm()) {
            return false;
        }

        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float[] h = sample.hand;
        poseStack.pushPose();
        poseStack.translate(h[0] * side, h[1], h[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(h[3]));
        poseStack.mulPose(Axis.YP.rotationDegrees(h[4] * side));
        poseStack.mulPose(Axis.ZP.rotationDegrees(h[5] * side));
        return true;
    }

    /** Shakes the local view now, at least this hard (a colossus's footfall or blow nearby). */
    public static void quake(float strength) {
        shake = Math.max(shake, strength);
    }

    /** The view's shake this frame, as roll and pitch in degrees. */
    public static float[] shake(float partial) {
        float s = Mth.lerp(partial, prevShake, shake);
        if (s <= 0.01F) {
            return new float[]{0.0F, 0.0F};
        }

        float time = ticks + partial;
        return new float[]{Mth.sin(time * 2.7F) * s * 3.0F, Mth.cos(time * 3.3F) * s * 1.8F};
    }

    /** Eases flight toward what each player is doing, runs due shakes and forgets finished clips. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        prevShake = shake;
        shake *= 0.82F;
        for (Iterator<Shake> it = SHAKES.iterator(); it.hasNext();) {
            Shake due = it.next();
            if (due.due() <= ticks) {
                shake = Math.max(shake, due.strength());
                it.remove();
            }
        }

        if (mc.level == null) {
            return;
        }

        ACTIVE.entrySet().removeIf(entry -> {
            Entity entity = mc.level.getEntity(entry.getKey());
            Active active = entry.getValue();
            float limit = active.clip().holdsAtEnd() ? 1.0F + 10.0F / active.clip().duration() : 1.0F;
            return entity == null || active.progress(entity.tickCount) > limit;
        });
        for (Map.Entry<UUID, Flight> entry : FLIGHT.entrySet()) {
            Flight flight = entry.getValue();
            Player player = mc.level.getPlayerByUUID(entry.getKey());
            flight.prevTilt = flight.tilt;
            flight.prevOpen = flight.open;
            flight.prevBlend = flight.blend;
            double speed = player == null ? 0.0 : Math.sqrt(Mth.square(player.getX() - player.xo) + Mth.square(player.getZ() - player.zo));
            float tiltTarget = flight.flying ? (float)Mth.clamp((speed - 0.08) / 0.5, 0.0, 1.0) : 0.0F;
            flight.tilt += (tiltTarget - flight.tilt) * 0.2F;
            flight.open += ((flight.flying ? 1.0F : 0.0F) - flight.open) * 0.2F;
            flight.blend += ((flight.flying ? 1.0F : 0.0F) - flight.blend) * 0.25F;
        }
    }

    public static void clear() {
        ACTIVE.clear();
        FLIGHT.clear();
        SHAKES.clear();
        shake = 0.0F;
        prevShake = 0.0F;
    }
}
