package net.logosnumen.nightmareevolved.client.anim;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;

/**
 * A short body animation: keyframes that pose a humanoid's head, body, arms and legs (in degrees), turn or tip the
 * whole body, and move the first-person hand. Between keys the pose eases smoothly; a part a key leaves out belongs to
 * the ordinary pose there, so clips blend in from whatever the body was doing and back out to it.
 */
public final class PoseClip {
    public enum Part {
        HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG
    }

    /** One keyframe. Rotations are degrees; the root is yaw, pitch and lift; the hand is x, y, z and three angles. */
    record Key(float t, EnumMap<Part, float[]> parts, @Nullable float[] root, @Nullable float[] hand) {
    }

    /** A sampled pose: per-part rotations in radians with how strongly each overrides the ordinary pose. */
    public static final class Sample {
        public final EnumMap<Part, float[]> rotation = new EnumMap<>(Part.class);
        public final EnumMap<Part, Float> weight = new EnumMap<>(Part.class);
        /** Whole-body yaw and pitch (degrees) and lift (blocks). */
        public final float[] root = new float[3];
        /** First-person hand offset (blocks) and turn (degrees). */
        public final float[] hand = new float[6];
    }

    final String id;
    final int duration;
    final List<Key> keys;
    final float shake;
    final float shakeAt;

    private PoseClip(String id, int duration, List<Key> keys, float shake, float shakeAt) {
        this.id = id;
        this.duration = duration;
        this.keys = keys;
        this.shake = shake;
        this.shakeAt = shakeAt;
    }

    public String id() {
        return this.id;
    }

    public int duration() {
        return this.duration;
    }

    /** Whether the clip ends away from the ordinary pose, holding there until another clip takes over. */
    public boolean holdsAtEnd() {
        return !this.keys.get(this.keys.size() - 1).parts().isEmpty();
    }

    static Builder clip(String id, int duration) {
        return new Builder(id, duration);
    }

    private static float ease(float u) {
        return u * u * (3.0F - 2.0F * u);
    }

    /** The pose {@code t} of the way through (0 to 1). */
    public Sample sample(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        Key before = this.keys.get(0);
        Key after = this.keys.get(this.keys.size() - 1);
        for (int i = 0; i < this.keys.size() - 1; i++) {
            if (t >= this.keys.get(i).t() && t <= this.keys.get(i + 1).t()) {
                before = this.keys.get(i);
                after = this.keys.get(i + 1);
                break;
            }
        }

        float span = after.t() - before.t();
        float u = span <= 0.0F ? 1.0F : ease((t - before.t()) / span);
        Sample sample = new Sample();
        for (Part part : Part.values()) {
            float[] a = before.parts().get(part);
            float[] b = after.parts().get(part);
            if (a == null && b == null) {
                continue;
            }

            float[] from = a != null ? a : b;
            float[] to = b != null ? b : a;
            float[] out = new float[3];
            for (int k = 0; k < 3; k++) {
                out[k] = Mth.lerp(u, from[k], to[k]) * Mth.DEG_TO_RAD;
            }

            sample.rotation.put(part, out);
            sample.weight.put(part, Mth.lerp(u, a != null ? 1.0F : 0.0F, b != null ? 1.0F : 0.0F));
        }

        lerpInto(sample.root, before.root(), after.root(), u);
        lerpInto(sample.hand, before.hand(), after.hand(), u);
        return sample;
    }

    private static void lerpInto(float[] out, @Nullable float[] a, @Nullable float[] b, float u) {
        for (int k = 0; k < out.length; k++) {
            out[k] = Mth.lerp(u, a != null ? a[k] : 0.0F, b != null ? b[k] : 0.0F);
        }
    }

    static final class Builder {
        private final String id;
        private final int duration;
        private final List<Key> keys = new ArrayList<>();
        private float shake;
        private float shakeAt;
        private EnumMap<Part, float[]> parts;
        private float[] root;
        private float[] hand;
        private float at = -1.0F;

        private Builder(String id, int duration) {
            this.id = id;
            this.duration = duration;
        }

        private void flush() {
            if (this.at >= 0.0F) {
                this.keys.add(new Key(this.at, this.parts, this.root, this.hand));
            }
        }

        /** Starts the keyframe {@code t} of the way through the clip. */
        Builder key(float t) {
            this.flush();
            this.at = t;
            this.parts = new EnumMap<>(Part.class);
            this.root = null;
            this.hand = null;
            return this;
        }

        Builder part(Part part, float x, float y, float z) {
            this.parts.put(part, new float[]{x, y, z});
            return this;
        }

        Builder head(float x, float y, float z) {
            return this.part(Part.HEAD, x, y, z);
        }

        Builder body(float x, float y, float z) {
            return this.part(Part.BODY, x, y, z);
        }

        Builder rightArm(float x, float y, float z) {
            return this.part(Part.RIGHT_ARM, x, y, z);
        }

        Builder leftArm(float x, float y, float z) {
            return this.part(Part.LEFT_ARM, x, y, z);
        }

        /** Both arms, the left mirroring the right. */
        Builder arms(float x, float y, float z) {
            return this.rightArm(x, y, z).leftArm(x, -y, -z);
        }

        Builder rightLeg(float x, float y, float z) {
            return this.part(Part.RIGHT_LEG, x, y, z);
        }

        Builder leftLeg(float x, float y, float z) {
            return this.part(Part.LEFT_LEG, x, y, z);
        }

        /** Turns (degrees), tips forward (degrees) and lifts (blocks) the whole body. */
        Builder root(float yaw, float pitch, float lift) {
            this.root = new float[]{yaw, pitch, lift};
            return this;
        }

        /** Moves the first-person hand (blocks: right, up, forward is negative) and turns it (degrees). */
        Builder hand(float x, float y, float z, float rx, float ry, float rz) {
            this.hand = new float[]{x, y, z, rx, ry, rz};
            return this;
        }

        /** Shakes the view of anyone near, at {@code t} of the way through. */
        Builder shake(float strength, float t) {
            this.shake = strength;
            this.shakeAt = t;
            return this;
        }

        /** Copies the previous keyframe's pose into this one, to hold it. */
        Builder hold() {
            Key last = this.keys.isEmpty() ? null : this.keys.get(this.keys.size() - 1);
            if (last != null) {
                this.parts = new EnumMap<>(last.parts());
                this.root = last.root();
                this.hand = last.hand();
            }

            return this;
        }

        PoseClip build() {
            this.flush();
            // Unless a clip says otherwise, it starts and ends in the ordinary pose.
            if (this.keys.isEmpty() || this.keys.get(0).t() > 0.0F) {
                this.keys.add(0, new Key(0.0F, new EnumMap<>(Part.class), null, null));
            }

            if (this.keys.get(this.keys.size() - 1).t() < 1.0F) {
                this.keys.add(new Key(1.0F, new EnumMap<>(Part.class), null, null));
            }

            return new PoseClip(this.id, this.duration, List.copyOf(this.keys), this.shake, this.shakeAt);
        }
    }
}
