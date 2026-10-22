package net.logosnumen.nightmareevolved.client.colossus;

import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;

/** One kind of colossus's body: its colourings, how a variant is built, and how it moves. */
public abstract class ColossusModel {
    /** The colours of a variant. */
    public abstract Look look(ColossusVariant variant);

    /** Builds the body of a variant, at half its drawn size (the renderer doubles it). */
    public abstract Rig build(ColossusVariant variant);

    /** Poses the body for this frame. Every bone starts from its rest pose and size. */
    public abstract void animate(Rig.Baked body, Pose pose, ColossusVariant variant);

    /** How much larger than its model units the body is drawn. */
    public float renderScale() {
        return 2.0F;
    }

    protected static int n(float v) {
        return Math.max(1, Math.round(v));
    }
}
