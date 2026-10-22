package net.logosnumen.nightmareevolved.client.creature;

import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/**
 * A body on four legs, laid out from its size: a long {@code body}, a {@code head} on a short neck with a {@code jaw},
 * front legs {@code leg0} and hind legs {@code leg1} (each with a {@code shin}), and a tail of {@code tail0},
 * {@code tail1}... Models add fur, spines, wings or horns.
 */
public final class Quadruped {
    public final float height;
    public final float length;
    public final float width;
    public final float legs;

    private Quadruped(float height, float length, float width, float legs) {
        this.height = height;
        this.length = length;
        this.width = width;
        this.legs = legs;
    }

    /**
     * Builds the frame: {@code height} from the ground to the top of the back, {@code length} nose to rump, {@code width}
     * across, with a tail of {@code tail} segments.
     */
    public static Quadruped build(Rig rig, float height, float length, float width, int tail, Material hide, Material limbs) {
        float legs = height * 0.55F;
        float depth = height - legs;
        Quadruped q = new Quadruped(height, length, width, legs);
        float limb = Math.max(2.0F, width * 0.3F);
        rig.bone("root", null, 0.0F, 24.0F, 0.0F);
        rig.bone("body", "root", 0.0F, -legs, 0.0F);
        CreatureModel.box(rig, "body", hide, -width / 2.0F, -depth, -length * 0.4F, width, depth, length * 0.8F);
        rig.bone("head", "body", 0.0F, -depth * 0.8F, -length * 0.4F);
        float head = depth * 0.9F;
        CreatureModel.box(rig, "head", hide, -head * 0.45F, -head * 0.55F, -head * 0.9F, head * 0.9F, head * 0.7F, head);
        rig.bone("jaw", "head", 0.0F, head * 0.1F, -head * 0.3F);
        CreatureModel.box(rig, "jaw", hide, -head * 0.35F, 0.0F, -head * 0.95F, head * 0.7F, head * 0.25F, head * 0.75F);
        for (int i = 0; i < 2; i++) {
            float z = i == 0 ? -length * 0.3F : length * 0.3F;
            rig.pairBone("leg" + i, "body", -width / 2.0F + limb * 0.4F, -1.0F, z, 0.0F, 0.0F, 0.0F);
            rig.pair("leg" + i, limbs, -limb / 2.0F, 0.0F, -limb / 2.0F, CreatureModel.n(limb), CreatureModel.n(legs * 0.55F), CreatureModel.n(limb));
            rig.pairBone("shin" + i, "leg" + i, 0.0F, legs * 0.5F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("shin" + i, limbs, -limb * 0.4F, 0.0F, -limb * 0.4F, CreatureModel.n(limb * 0.8F), CreatureModel.n(legs * 0.52F), CreatureModel.n(limb * 0.8F));
        }

        String parent = "body";
        for (int t = 0; t < tail; t++) {
            String bone = "tail" + t;
            rig.bone(bone, parent, 0.0F, t == 0 ? -depth * 0.7F : 0.0F, t == 0 ? length * 0.4F : length * 0.18F, t == 0 ? -20.0F : 8.0F, 0.0F, 0.0F);
            float w = Math.max(1.0F, limb * (0.8F - t * 0.15F));
            CreatureModel.box(rig, bone, hide, -w / 2.0F, -w / 2.0F, 0.0F, w, w, length * 0.18F);
            parent = bone;
        }

        return q;
    }

    /** Trots: legs in diagonal pairs, the head following the gaze, the tail swaying, the back rising and falling. */
    public static void trot(Rig.Baked body, Pose pose, float stride, float speed, int tail) {
        Gaits.hangingLegs(body, pose, 2, stride, speed);
        Gaits.look(body, pose, 0.7F, 0.6F);
        Gaits.sway(body, pose, "tail", tail, 10.0F, 0.2F + speed * pose.stride(), 0.8F);
        Bones.shift(body.part("body"), 0.0F, -Math.abs(pose.gait(speed, 0.5F)) * pose.stride() * 0.8F, 0.0F);
    }

    /** Pounces: haunches down, then thrown forward with the forelegs reaching. */
    public static void pounce(Rig.Baked body, Pose pose, String move) {
        float p = pose.key(move, 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("body"), -12.0F * p, 0.0F, 0.0F);
        Gaits.pair(body, "leg0", -60.0F * Math.max(0.0F, p), 0.0F, 0.0F, -60.0F * Math.max(0.0F, p), 0.0F, 0.0F);
        Gaits.pair(body, "leg1", 50.0F * Math.max(0.0F, p) - 30.0F * Math.max(0.0F, -p), 0.0F, 0.0F, 50.0F * Math.max(0.0F, p) - 30.0F * Math.max(0.0F, -p),
            0.0F, 0.0F);
        Bones.turn(body.part("jaw"), 35.0F * Math.max(0.0F, p), 0.0F, 0.0F);
    }

    /** Bites: the head drawn back, then snapped forward with the jaw wide. */
    public static void bite(Rig.Baked body, Pose pose, String move) {
        float b = pose.key(move, 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("head"), 20.0F * b, 0.0F, 0.0F);
        Bones.turn(body.part("jaw"), 40.0F * Math.max(0.0F, -b) + 10.0F * Math.max(0.0F, b), 0.0F, 0.0F);
        Bones.turn(body.part("body"), 6.0F * b, 0.0F, 0.0F);
    }
}
