package net.logosnumen.nightmareevolved.client.creature;

import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/**
 * A body on two legs with two arms, laid out from its height: hips ({@code body}), a chest above them, a {@code head},
 * {@code arm_r}/{@code arm_l} hanging from the shoulders with {@code hand} bones at their ends, and {@code leg0_r}/
 * {@code leg0_l} reaching down to the ground. Models add armour, weapons and horns to the bones it leaves them.
 */
public final class Humanoid {
    /** The height from the ground to the hips, the chest's height, and the head's size, in units. */
    public final float hips;
    public final float chest;
    public final float head;
    public final float shoulders;
    public final float arm;

    private Humanoid(float hips, float chest, float head, float shoulders, float arm) {
        this.hips = hips;
        this.chest = chest;
        this.head = head;
        this.shoulders = shoulders;
        this.arm = arm;
    }

    /**
     * Builds the frame for a body {@code h} units tall and {@code w} across the shoulders, of the given materials, and
     * returns its measurements.
     */
    public static Humanoid build(Rig rig, float h, float w, Material torso, Material limbs, Material head) {
        float hips = h * 0.46F;
        float chest = h * 0.34F;
        float headSize = h * 0.14F;
        float limb = Math.max(2.0F, w * 0.28F);
        float arm = h * 0.4F;
        Humanoid body = new Humanoid(hips, chest, headSize, w, arm);
        rig.bone("root", null, 0.0F, 24.0F, 0.0F);
        rig.bone("body", "root", 0.0F, -hips, 0.0F);
        CreatureModel.box(rig, "body", torso, -w * 0.4F, -chest * 0.35F, -w * 0.22F, w * 0.8F, chest * 0.35F, w * 0.44F);
        rig.bone("chest", "body", 0.0F, -chest * 0.35F, 0.0F);
        CreatureModel.box(rig, "chest", torso, -w * 0.5F, -chest * 0.65F, -w * 0.26F, w, chest * 0.65F, w * 0.52F);
        rig.bone("head", "chest", 0.0F, -chest * 0.65F, 0.0F);
        CreatureModel.box(rig, "head", head, -headSize * 0.45F, -headSize, -headSize * 0.5F, headSize * 0.9F, headSize, headSize * 0.9F);
        rig.pairBone("arm", "chest", -w * 0.5F - limb * 0.5F, -chest * 0.6F, 0.0F, 0.0F, 0.0F, 4.0F);
        rig.pair("arm", limbs, -limb * 0.5F, -1.0F, -limb * 0.5F, CreatureModel.n(limb), CreatureModel.n(arm), CreatureModel.n(limb));
        rig.pairBone("hand", "arm", 0.0F, arm - 1.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pairBone("leg0", "body", -w * 0.22F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg0", limbs, -limb * 0.55F, 0.0F, -limb * 0.55F, CreatureModel.n(limb * 1.1F), CreatureModel.n(hips), CreatureModel.n(limb * 1.1F));
        return body;
    }

    /** Walks: legs striding, arms swinging against them, the head following the gaze, the chest breathing. */
    public static void walk(Rig.Baked body, Pose pose, float stride, float speed) {
        Gaits.hangingLegs(body, pose, 1, stride, speed);
        Gaits.armSwing(body, pose, stride * 0.7F, speed);
        Gaits.look(body, pose, 0.8F, 0.8F);
        Bones.turn(body.part("chest"), pose.wave(0.08F, 0.0F) * 1.5F, 0.0F, 0.0F);
    }

    /** Falls forward onto its face in death. */
    public static void die(Rig.Baked body, Pose pose, float hips) {
        float f = pose.fall(0.0F, 1.0F);
        if (f <= 0.0F) {
            return;
        }

        Bones.turn(body.part("body"), 85.0F * f, 0.0F, 0.0F);
        Bones.shift(body.part("body"), 0.0F, hips * 0.85F * f, -hips * 0.3F * f);
        Gaits.pair(body, "arm", -60.0F * f, 0.0F, 20.0F * f, -60.0F * f, 0.0F, 20.0F * f);
    }
}
