package net.logosnumen.nightmareevolved.client.creature;

import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/**
 * One kind of creature's body: its colourings, how it is built, and how it moves. Bodies are built in model units with
 * the feet on the root's pivot and the creature facing -z; a creature's right side is -x.
 */
public abstract class CreatureModel {
    /** The colours of one of the kind's looks. */
    public abstract Look look(int look);

    /** Builds the body for one of the kind's looks. */
    public abstract Rig build(int look);

    /** Poses the body for this frame. Every bone starts from its rest pose and size. */
    public abstract void animate(Rig.Baked body, Pose pose, int look);

    /** How much larger than its model units the body is drawn (large bodies are built small and drawn larger). */
    public float renderScale() {
        return 1.0F;
    }

    public static int n(float v) {
        return Math.max(1, Math.round(v));
    }

    /** A box whose size is given in fractional units (rounded, at least one). */
    public static void box(Rig rig, String bone, Material material, float x, float y, float z, float w, float h, float d) {
        rig.box(bone, material, x, y, z, n(w), n(h), n(d));
    }

    /** A box on one bone and its mirror image across the middle of the same bone. */
    public static void both(Rig rig, String bone, Material material, float x, float y, float z, float w, float h, float d) {
        rig.box(bone, material, x, y, z, n(w), n(h), n(d));
        rig.box(bone, material, -x - n(w), y, z, n(w), n(h), n(d));
    }
}
