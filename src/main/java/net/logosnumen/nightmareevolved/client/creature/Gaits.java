package net.logosnumen.nightmareevolved.client.creature;

import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/**
 * The ways bodies move, shared by every creature model: legs that step in waves or in pairs, wings that beat, tails and
 * coils that sway, heads that follow the gaze, chests that breathe, and bodies that fall in death. Bones are found by
 * name ({@code leg0_r}, {@code arm_l}, {@code head}, {@code tail2}...); whatever a body lacks is skipped.
 */
public final class Gaits {
    private Gaits() {
    }

    /** Turns the right bone of a pair as given and its left twin mirrored. */
    public static void pair(Rig.Baked body, String name, float x, float y, float z, float lx, float ly, float lz) {
        Bones.turn(body.part(name + "_r"), x, y, z);
        Bones.turn(body.part(name + "_l"), lx, -ly, -lz);
    }

    /**
     * Legs that stick out to the sides (insects, crabs, spiders): {@code pairs} pairs named {@code leg0} on, front to
     * back, stepping in alternating tripods. Each foot swings fore and aft by {@code swing} degrees and lifts by
     * {@code lift} as it comes forward.
     */
    public static void sideLegs(Rig.Baked body, Pose pose, int pairs, float swing, float lift, float speed) {
        for (int i = 0; i < pairs; i++) {
            float phase = i % 2;
            float r = pose.gait(speed, phase);
            float l = pose.gait(speed, phase + 1.0F);
            float rUp = Math.max(0.0F, pose.gait(speed, phase + 0.5F)) * lift;
            float lUp = Math.max(0.0F, pose.gait(speed, phase + 1.5F)) * lift;
            Bones.turn(body.part("leg" + i + "_r"), 0.0F, r * swing, rUp);
            Bones.turn(body.part("leg" + i + "_l"), 0.0F, -l * swing, -lUp);
        }
    }

    /**
     * Legs that hang beneath the body (beasts, people): pairs named {@code leg0} (front) and {@code leg1} (hind) and so
     * on, each pair's sides opposite, and each pair a half-step from the one before (a trot).
     */
    public static void hangingLegs(Rig.Baked body, Pose pose, int pairs, float swing, float speed) {
        for (int i = 0; i < pairs; i++) {
            float phase = i % 2;
            Bones.turn(body.part("leg" + i + "_r"), -pose.gait(speed, phase) * swing, 0.0F, 0.0F);
            Bones.turn(body.part("leg" + i + "_l"), -pose.gait(speed, phase + 1.0F) * swing, 0.0F, 0.0F);
            Bones.turn(body.part("shin" + i + "_r"), Math.max(0.0F, pose.gait(speed, phase + 0.5F)) * swing * 0.8F, 0.0F, 0.0F);
            Bones.turn(body.part("shin" + i + "_l"), Math.max(0.0F, pose.gait(speed, phase + 1.5F)) * swing * 0.8F, 0.0F, 0.0F);
        }
    }

    /** Arms that swing against the legs as a walker strides. */
    public static void armSwing(Rig.Baked body, Pose pose, float swing, float speed) {
        Bones.turn(body.part("arm_r"), pose.gait(speed, 0.0F) * swing, 0.0F, 0.0F);
        Bones.turn(body.part("arm_l"), pose.gait(speed, 1.0F) * swing, 0.0F, 0.0F);
    }

    /** The head follows the gaze, within limits. */
    public static void look(Rig.Baked body, Pose pose, float yawShare, float pitchShare) {
        Bones.turn(body.part("head"), Mth.clamp(pose.headPitch(), -45.0F, 45.0F) * pitchShare, Mth.clamp(pose.headYaw(), -70.0F, 70.0F) * yawShare, 0.0F);
    }

    /** A slow rise and fall of the body as it breathes. */
    public static void breathe(Rig.Baked body, Pose pose, String bone, float amount, float speed) {
        Bones.shift(body.part(bone), 0.0F, pose.wave(speed, 0.0F) * amount, 0.0F);
    }

    /** Wings named {@code wing} (with an outer {@code tip}) beating up and down. */
    public static void wings(Rig.Baked body, Pose pose, float beat, float speed, float rest) {
        float flap = pose.wave(speed, 0.0F);
        pair(body, "wing", 0.0F, 0.0F, rest + flap * beat, 0.0F, 0.0F, rest + flap * beat);
        float tip = pose.wave(speed, -0.9F) * beat * 0.6F;
        pair(body, "tip", 0.0F, 0.0F, tip, 0.0F, 0.0F, tip);
    }

    /** A chain of bones named {@code prefix0}, {@code prefix1}... swaying side to side in a travelling wave. */
    public static void sway(Rig.Baked body, Pose pose, String prefix, int count, float degrees, float speed, float lag) {
        float strength = 0.4F + 0.6F * pose.stride();
        for (int i = 0; i < count; i++) {
            Bones.turn(body.part(prefix + i), 0.0F, pose.wave(speed, -i * lag) * degrees * strength, 0.0F);
        }
    }

    /** The same chain rippling up and down instead. */
    public static void ripple(Rig.Baked body, Pose pose, String prefix, int count, float degrees, float speed, float lag) {
        for (int i = 0; i < count; i++) {
            Bones.turn(body.part(prefix + i), pose.wave(speed, -i * lag) * degrees, 0.0F, 0.0F);
        }
    }

    /** Collapses the body in death: it sinks by {@code sink} units and rolls onto its side. */
    public static void fall(Rig.Baked body, Pose pose, float sink, float roll) {
        if (pose.death() <= 0.0F) {
            return;
        }

        float f = pose.fall(0.0F, 1.0F);
        Bones.shift(body.part("root"), 0.0F, sink * f, 0.0F);
        Bones.turn(body.part("body"), 0.0F, 0.0F, roll * f);
    }
}
