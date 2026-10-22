package net.logosnumen.nightmareevolved.client.rig;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;

/**
 * What a creature's body is doing this frame: its clock, stride and gaze, the move it is in and how far through, how
 * far it has fallen in death, and whether it rages. Angles are degrees.
 */
public record Pose(float age, float walk, float stride, float headYaw, float headPitch, @Nullable String action, float progress, float death,
    boolean enraged) {
    /**
     * A keyframed value for one move: pairs of (progress, value), eased between, 0 when that move isn't running.
     * Curves start and end at 0 so the body flows out of and back into its walk.
     */
    public float key(String id, float... keys) {
        return id.equals(this.action) ? curve(this.progress, keys) : 0.0F;
    }

    public static float curve(float t, float... keys) {
        if (t < 0.0F || keys.length < 2) {
            return 0.0F;
        }

        if (t <= keys[0]) {
            return keys[1];
        }

        for (int i = 2; i + 1 < keys.length; i += 2) {
            if (t <= keys[i]) {
                float a = (t - keys[i - 2]) / Math.max(1.0E-4F, keys[i] - keys[i - 2]);
                a = a * a * (3.0F - 2.0F * a);
                return Mth.lerp(a, keys[i - 1], keys[i + 1]);
            }
        }

        return keys[keys.length - 1];
    }

    /** The stride swing for a limb, from -1 to 1, with a phase offset in half-turns. */
    public float gait(float speed, float phase) {
        return Mth.cos(this.walk * speed + phase * Mth.PI) * this.stride;
    }

    public float wave(float speed, float phase) {
        return Mth.sin(this.age * speed + phase);
    }

    /** Eased death from {@code from} to {@code to} (fractions of the fall). */
    public float fall(float from, float to) {
        float a = Mth.clamp((this.death - from) / (to - from), 0.0F, 1.0F);
        return a * a * (3.0F - 2.0F * a);
    }
}
