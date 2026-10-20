package net.logosnumen.nightmareevolved.client.rig;

import javax.annotation.Nullable;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Small helpers for posing a baked rig: turns in degrees, pivot moves, and mirrored pairs. */
public final class Bones {
    private Bones() {
    }

    /** Adds a turn in degrees; missing bones (a variant without them) are skipped. */
    public static void turn(@Nullable ModelPart part, float x, float y, float z) {
        if (part != null) {
            part.xRot += x * Mth.DEG_TO_RAD;
            part.yRot += y * Mth.DEG_TO_RAD;
            part.zRot += z * Mth.DEG_TO_RAD;
        }
    }

    /** Moves a bone's pivot, in model units. */
    public static void shift(@Nullable ModelPart part, float x, float y, float z) {
        if (part != null) {
            part.x += x;
            part.y += y;
            part.z += z;
        }
    }

    /** Turns a pair of mirrored bones (_r and _l) together, the left one mirrored. */
    public static void turnPair(Rig.Baked body, String name, float x, float y, float z) {
        turn(body.part(name + "_r"), x, y, z);
        turn(body.part(name + "_l"), x, -y, -z);
    }

    /** Sets a bone's size on all three axes (rest poses don't reset size, so set it every frame it changes). */
    public static void size(@Nullable ModelPart part, float scale) {
        if (part != null) {
            part.xScale = scale;
            part.yScale = scale;
            part.zScale = scale;
        }
    }
}
