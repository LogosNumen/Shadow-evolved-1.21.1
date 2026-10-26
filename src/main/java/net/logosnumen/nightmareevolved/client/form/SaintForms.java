package net.logosnumen.nightmareevolved.client.form;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.creature.Quadruped;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The creatures Saints become when they transform. Each stands about as tall as a person, with its head where the
 * Saint's eyes are, and grows with the Transformation's size. In these bodies a pose's "enraged" means aloft.
 */
public final class SaintForms {
    private SaintForms() {
    }

    /** Cubes of side {@code w} laid along a line from one point to another on a bone: a neck, a limb drawn in blocks. */
    static void line(Rig rig, String bone, Material material, float x0, float y0, float z0, float x1, float y1, float z1, float w, int steps) {
        for (int i = 0; i < steps; i++) {
            float t = steps == 1 ? 0.0F : i / (float)(steps - 1);
            CreatureModel.box(rig, bone, material, Mth.lerp(t, x0, x1) - w / 2.0F, Mth.lerp(t, y0, y1) - w / 2.0F, Mth.lerp(t, z0, z1) - w / 2.0F, w, w, w);
        }
    }

    /** Two feathered or membrane wings (a {@code wing} and its {@code tip}) set on a bone. */
    static void wings(Rig rig, String bone, Material material, float x, float y, float z, int span, int tipSpan, int depth) {
        rig.pairBone("wing", bone, x, y, z, 0.0F, 0.0F, 0.0F);
        rig.pair("wing", material, -span, -0.5F, 0, span, 1, depth);
        rig.pairBone("tip", "wing", -span, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("tip", material, -tipSpan, -0.5F, 0, tipSpan, 1, depth - 2);
    }

    /** Wings beating while aloft, or folded back along the body on the ground. */
    static void wingsFor(Rig.Baked body, Pose pose, float beat) {
        if (pose.enraged()) {
            Gaits.wings(body, pose, beat, 0.35F, 0.0F);
        } else {
            // Folded: swept back along the flanks, the outer half doubled back over the inner.
            Gaits.pair(body, "wing", 0.0F, 75.0F, -15.0F, 0.0F, 75.0F, -15.0F);
            Gaits.pair(body, "tip", 0.0F, 160.0F, 0.0F, 0.0F, 160.0F, 0.0F);
            Bones.turn(body.part("wing_r"), 0.0F, 0.0F, pose.wave(0.08F, 0.0F) * 3.0F);
            Bones.turn(body.part("wing_l"), 0.0F, 0.0F, -pose.wave(0.08F, 0.0F) * 3.0F);
        }
    }

    /** A four-legged form: trots on the ground, and tucks its legs to fly if it has wings and is aloft. */
    static void beastGait(Rig.Baked body, Pose pose, float stride, float speed, int tail) {
        if (pose.enraged()) {
            Gaits.pair(body, "leg0", 45.0F, 0.0F, 0.0F, 45.0F, 0.0F, 0.0F);
            Gaits.pair(body, "leg1", 60.0F, 0.0F, 0.0F, 60.0F, 0.0F, 0.0F);
            Gaits.look(body, pose, 0.6F, 0.6F);
            Gaits.sway(body, pose, "tail", tail, 8.0F, 0.15F, 0.6F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.35F, 1.0F) * 1.5F, 0.0F);
        } else {
            Quadruped.trot(body, pose, stride, speed, tail);
        }
    }

    /** A swing with the forepaws: both raised, then brought down, with the jaws. */
    static void maul(Rig.Baked body, Pose pose, String move) {
        float m = pose.key(move, 0.0F, 0.0F, 0.4F, -1.0F, 0.65F, 1.0F, 1.0F, 0.0F);
        Gaits.pair(body, "leg0", -70.0F * Math.max(0.0F, -m) + 20.0F * Math.max(0.0F, m), 0.0F, 0.0F, -60.0F * Math.max(0.0F, -m), 0.0F, 0.0F);
        Bones.turn(body.part("body"), -10.0F * Math.max(0.0F, -m), 0.0F, 0.0F);
        Quadruped.bite(body, pose, move);
    }

    /** Kai's midnight dragon: scales the black-blue of the night sky, swept horns, a long neck and tail, great wings. */
    public static final class MidnightDragon extends CreatureModel {
        private static final Look LOOK = new Look(0x161C38, 0x06080F, 0x34406E, 0x262C50, 0x9AA2C0, 0xA8C4FF, 0x1C2448);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 18.0F, 32.0F, 11.0F, 6, Material.SCALE, Material.SCALE);
            box(rig, "body", Material.BELLY, -4.5F, -1.5F, -12.0F, 9.0F, 1.5F, 24.0F);
            for (int s = 0; s < 6; s++) {
                box(rig, "body", Material.HORN, -0.5F, -10.0F, -11.0F + s * 4.4F, 1.0F, 2.0F, 2.0F);
            }

            line(rig, "body", Material.SCALE, 0.0F, -6.0F, -12.0F, 0.0F, -15.0F, -17.0F, 5.0F, 4);
            both(rig, "head", Material.HORN, -3.0F, -5.5F, -2.0F, 1.2F, 1.2F, 6.0F);
            both(rig, "head", Material.EYE, -3.6F, -3.0F, -5.0F, 1.0F, 1.0F, 1.0F);
            for (int t = 0; t < 3; t++) {
                both(rig, "jaw", Material.BONE, -2.2F, -1.0F, -6.5F + t * 1.6F, 0.6F, 1.0F, 0.6F);
            }

            wings(rig, "body", Material.MEMBRANE, -5.0F, -7.5F, -6.0F, 26, 18, 16);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.shift(body.part("head"), 0.0F, -9.0F, -5.0F);
            beastGait(body, pose, 26.0F, 0.6F, 6);
            wingsFor(body, pose, 40.0F);
            Quadruped.bite(body, pose, "bite");
            Gaits.fall(body, pose, 10.0F, 80.0F);
        }
    }

    /** Tyris's thunderbird: white plumage, talons of steel, and lightning running along the edges of its wings. */
    public static final class Thunderbird extends CreatureModel {
        private static final Look LOOK = new Look(0xE8ECF0, 0x8A96A8, 0xFFFFFF, 0xF4F6FA, 0x6A7280, 0x9AD8FF, 0xA8B2C0);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -16.0F, 0.0F);
            rig.box("body", Material.FEATHER, -5, -6, -8, 10, 10, 16);
            rig.box("body", Material.BELLY, -4, 2, -7, 8, 2, 12);
            line(rig, "body", Material.FEATHER, 0.0F, -4.0F, -8.0F, 0.0F, -9.0F, -10.0F, 4.0F, 3);
            rig.bone("head", "body", 0.0F, -11.0F, -10.0F);
            rig.box("head", Material.FEATHER, -2.5F, -3, -4, 5, 5, 6);
            rig.box("head", Material.FEATHER, -1, -6, -1, 2, 3, 4);
            rig.box("head", Material.HORN, -1, -1, -8, 2, 2, 4);
            rig.bone("jaw", "head", 0.0F, 1.0F, -4.0F);
            rig.box("jaw", Material.HORN, -1, 0, -3, 2, 1, 3);
            both(rig, "head", Material.EYE, -2.7F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("leg0", "body", -3.0F, 4.0F, 1.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("leg0", Material.FEATHER, -1.5F, 0, -1.5F, 3, 6, 3);
            rig.pairBone("shin0", "leg0", 0.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("shin0", Material.PLATE, -0.75F, 0, -0.75F, 2, 6, 2);
            rig.pair("shin0", Material.PLATE, -1.5F, 5, -3.5F, 1, 1, 4);
            rig.pair("shin0", Material.PLATE, 0.5F, 5, -3.5F, 1, 1, 4);
            wings(rig, "body", Material.FEATHER, -5.0F, -4.0F, -5.0F, 24, 20, 13);
            rig.pair("wing", Material.RUNE, -24, 0.4F, 11, 24, 1, 1);
            rig.bone("tail0", "body", 0.0F, -2.0F, 8.0F, 10.0F, 0.0F, 0.0F);
            rig.box("tail0", Material.FEATHER, -4, -0.5F, 0, 8, 1, 10);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.look(body, pose, 0.7F, 0.6F);
            if (pose.enraged()) {
                Gaits.pair(body, "leg0", 55.0F, 0.0F, 0.0F, 55.0F, 0.0F, 0.0F);
                Bones.shift(body.part("body"), 0.0F, pose.wave(0.35F, 1.0F) * 1.5F, 0.0F);
            } else {
                Gaits.hangingLegs(body, pose, 1, 30.0F, 0.6F);
            }

            wingsFor(body, pose, 45.0F);
            float talons = pose.key("talons", 0.0F, 0.0F, 0.4F, -1.0F, 0.65F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "leg0", -80.0F * Math.max(0.0F, talons) + 20.0F * Math.max(0.0F, -talons), 0.0F, 0.0F,
                -80.0F * Math.max(0.0F, talons) + 20.0F * Math.max(0.0F, -talons), 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), 30.0F * Math.max(0.0F, -talons), 0.0F, 0.0F);
            Gaits.fall(body, pose, 12.0F, 80.0F);
        }
    }

    /** Roan's winged lion: white, with a pale gold mane, amber eyes, and white wings. */
    public static final class WingedLion extends CreatureModel {
        private static final Look LOOK = new Look(0xF2EEE4, 0x9A9284, 0xFFFFFF, 0xE8E0D0, 0x3A3028, 0xFFB030, 0xE8D8B8);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 18.0F, 30.0F, 11.0F, 3, Material.FUR, Material.FUR);
            box(rig, "head", Material.CLOTH, -5.5F, -6.5F, -3.0F, 11.0F, 10.0F, 4.0F);
            box(rig, "body", Material.CLOTH, -6.0F, -9.5F, -12.5F, 12.0F, 9.0F, 5.0F);
            both(rig, "head", Material.EYE, -3.6F, -3.0F, -5.6F, 1.0F, 1.0F, 1.0F);
            box(rig, "tail2", Material.CLOTH, -1.5F, -1.5F, 4.0F, 3.0F, 3.0F, 2.0F);
            wings(rig, "body", Material.FEATHER, -5.0F, -8.0F, -7.0F, 24, 18, 14);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.shift(body.part("head"), 0.0F, -5.0F, -2.0F);
            beastGait(body, pose, 28.0F, 0.7F, 3);
            wingsFor(body, pose, 40.0F);
            maul(body, pose, "maul");
            Gaits.fall(body, pose, 10.0F, 80.0F);
        }
    }

    /** Silent Stalker's panther: huge, black, sleek, with yellow-green eyes that are often the last thing seen. */
    public static final class Panther extends CreatureModel {
        private static final Look LOOK = new Look(0x101014, 0x040406, 0x24242C, 0x18181E, 0xD8D0C0, 0xC8FF5A, 0x16161C);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 16.0F, 32.0F, 9.0F, 6, Material.FUR, Material.FUR);
            both(rig, "head", Material.EYE, -3.2F, -3.0F, -5.6F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.FUR, -3.2F, -6.0F, -2.0F, 1.5F, 2.0F, 1.0F);
            for (int t = 0; t < 2; t++) {
                both(rig, "jaw", Material.BONE, -1.8F, -1.2F, -5.0F + t, 0.6F, 1.4F, 0.6F);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.shift(body.part("head"), 0.0F, -3.0F, -1.0F);
            Quadruped.trot(body, pose, 32.0F, 0.8F, 6);
            maul(body, pose, "maul");
            Gaits.fall(body, pose, 9.0F, 80.0F);
        }
    }

    /** Noctis's wolf of moonlight: colossal and pale, half there, with a crescent shining on its brow. */
    public static final class MoonlightWolf extends CreatureModel {
        private static final Look LOOK = new Look(0xC8D8F0, 0x5A6A88, 0xF4F8FF, 0xDCE6F8, 0xE8F0FF, 0xBFE4FF, 0xA8C0E0);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 20.0F, 30.0F, 10.0F, 4, Material.FUR, Material.FUR);
            both(rig, "head", Material.EYE, -3.6F, -3.5F, -6.6F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.FUR, -3.5F, -7.5F, -2.0F, 2.0F, 3.0F, 1.0F);
            // The crescent on its brow.
            box(rig, "head", Material.GLOW, -2.0F, -5.6F, -6.2F, 4.0F, 1.0F, 1.0F);
            both(rig, "head", Material.GLOW, -2.6F, -6.6F, -6.0F, 1.0F, 1.0F, 1.0F);
            for (int w = 0; w < 5; w++) {
                box(rig, "body", Material.SMOKE, -3.0F + (w % 3) * 2.0F, -11.0F - (w % 2), -10.0F + w * 5.0F, 3.0F, 2.0F, 3.0F);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.shift(body.part("head"), 0.0F, -4.0F, -2.0F);
            Quadruped.trot(body, pose, 26.0F, 0.6F, 4);
            Quadruped.bite(body, pose, "bite");
            Gaits.fall(body, pose, 11.0F, 80.0F);
        }
    }

    /** Helie's centaur: a chestnut horse's body, and above it her own, in green leather, with her bow. */
    public static final class Centaur extends CreatureModel {
        private static final Look LOOK = new Look(0x8A5A32, 0x3A2414, 0xB88458, 0xE0B894, 0x5A3A1E, 0x9CFF5A, 0x3A6A3A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 17.0F, 28.0F, 10.0F, 2, Material.FUR, Material.FUR);
            rig.bone("torso", "body", 0.0F, -7.6F, -9.5F);
            box(rig, "torso", Material.BELLY, -3.5F, -10.0F, -2.0F, 7.0F, 10.0F, 4.0F);
            box(rig, "torso", Material.PLATE, -4.0F, -9.5F, -2.5F, 8.0F, 7.0F, 5.0F);
            rig.bone("face", "torso", 0.0F, -10.0F, 0.0F);
            box(rig, "face", Material.BELLY, -2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F);
            box(rig, "face", Material.FUR, -2.7F, -5.4F, -1.0F, 5.4F, 2.0F, 4.0F);
            box(rig, "face", Material.FUR, -2.7F, -4.0F, 2.0F, 5.4F, 6.0F, 1.0F);
            both(rig, "face", Material.EYE, -1.6F, -3.0F, -2.7F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("arm", "torso", -4.5F, -9.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("arm", Material.BELLY, -1.2F, -0.5F, -1.2F, 2, 9, 2);
            rig.bone("bow", "arm_l", 0.0F, 8.0F, -1.0F);
            rig.box("bow", Material.HORN, -0.5F, -8, -0.5F, 1, 16, 1);
            rig.box("bow", Material.BONE, -0.25F, -7, 0.5F, 1, 14, 1);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            ModelPart horseHead = body.part("head");
            if (horseHead != null) {
                horseHead.visible = false;
            }

            Quadruped.trot(body, pose, 26.0F, 0.7F, 2);
            Bones.turn(body.part("face"), Mth.clamp(pose.headPitch(), -40.0F, 40.0F) * 0.7F, Mth.clamp(pose.headYaw(), -60.0F, 60.0F) * 0.7F, 0.0F);
            Gaits.pair(body, "arm", -15.0F, 0.0F, 5.0F, -30.0F, 0.0F, 5.0F);
            float shoot = pose.key("shoot", 0.0F, 0.0F, 0.3F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -75.0F * shoot, 30.0F * shoot, 0.0F, -75.0F * shoot, -20.0F * shoot, 0.0F);
            Gaits.fall(body, pose, 9.0F, 80.0F);
        }
    }

    /** Jest's horned fiend: dark red skin, a goat's legs and hooves, great curling horns, a tail, and claws. */
    public static final class Fiend extends CreatureModel {
        private static final Look LOOK = new Look(0x6A1A1A, 0x2A0808, 0x9A3A2A, 0x5A2A1A, 0x2A2420, 0xFF6020, 0x3A2A22);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 34.0F, 11.0F, Material.SCALE, Material.SCALE, Material.SCALE);
            for (int i = 0; i < 4; i++) {
                both(rig, "head", Material.HORN, -3.2F - i * 1.0F, -h.head - 1.5F - i * 1.2F + (i == 3 ? 3.0F : 0.0F), -1.0F + i * 1.2F, 1.8F, 1.8F, 1.8F);
            }

            both(rig, "head", Material.EYE, -1.6F, -h.head * 0.6F, -h.head * 0.5F - 0.2F, 1.0F, 1.0F, 1.0F);
            rig.pair("leg0", Material.FUR, -2.0F, 0.0F, -2.0F, 4, 9, 4);
            rig.pair("leg0", Material.HORN, -1.8F, h.hips - 2.0F, -2.6F, 4, 2, 4);
            rig.pair("hand", Material.CLAW, -1.2F, 0.0F, -1.2F, 2, 3, 2);
            String parent = "body";
            for (int t = 0; t < 3; t++) {
                String bone = "tail" + t;
                rig.bone(bone, parent, 0.0F, t == 0 ? -1.0F : 0.0F, t == 0 ? 2.5F : 5.0F, t == 0 ? 40.0F : -10.0F, 0.0F, 0.0F);
                box(rig, bone, Material.SCALE, -0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 5.0F);
                parent = bone;
            }

            box(rig, "tail2", Material.HORN, -1.5F, -1.0F, 4.5F, 3.0F, 2.0F, 2.0F);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 26.0F, 0.6F);
            Gaits.sway(body, pose, "tail", 3, 15.0F, 0.15F, 0.7F);
            Bones.turn(body.part("arm_r"), pose.key("claw", 0.0F, 0.0F, 0.4F, -140.0F, 0.6F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("chest"), pose.key("claw", 0.0F, 0.0F, 0.4F, -8.0F, 0.6F, 12.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 15.6F);
        }
    }

    /** Seishan's monstrous kin: a tall frame in grey shark's skin, a fin on the back, a shark's head full of teeth, claws. */
    public static final class SharkKin extends CreatureModel {
        private static final Look LOOK = new Look(0x5A6A78, 0x1E262E, 0x8A9AA8, 0xD8DCE0, 0xF0ECE0, 0xFF4040, 0x4A5A68);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 36.0F, 12.0F, Material.SCALE, Material.SCALE, Material.SCALE);
            box(rig, "chest", Material.BELLY, -4.0F, -h.chest * 0.6F, -3.4F, 8.0F, h.chest * 0.5F, 1.0F);
            box(rig, "chest", Material.MEMBRANE, -0.5F, -h.chest * 0.9F, 3.0F, 1.0F, 8.0F, 5.0F);
            box(rig, "head", Material.SCALE, -3.5F, -7.0F, -7.0F, 7.0F, 6.0F, 9.0F);
            box(rig, "head", Material.BELLY, -3.0F, -2.0F, -6.5F, 6.0F, 1.0F, 7.0F);
            both(rig, "head", Material.EYE, -3.7F, -5.0F, -4.0F, 1.0F, 1.0F, 1.0F);
            for (int t = 0; t < 5; t++) {
                box(rig, "head", Material.BONE, -2.6F + t * 1.2F, -1.6F, -6.8F, 0.6F, 1.2F, 0.6F);
            }

            for (int g = 0; g < 3; g++) {
                both(rig, "head", Material.VEIN, -3.6F, -5.0F + g * 1.3F, -1.0F, 1.0F, 0.5F, 1.5F);
            }

            rig.pair("hand", Material.CLAW, -1.4F, 0.0F, -1.4F, 3, 4, 2);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 24.0F, 0.55F);
            float rend = pose.key("rend", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -120.0F * Math.max(0.0F, -rend) - 30.0F * Math.max(0.0F, rend), 0.0F, 0.0F,
                -120.0F * Math.max(0.0F, -rend) - 30.0F * Math.max(0.0F, rend), 0.0F, 0.0F);
            Bones.turn(body.part("head"), -15.0F * Math.max(0.0F, -rend) + 10.0F * Math.max(0.0F, rend), 0.0F, 0.0F);
            Humanoid.die(body, pose, 16.6F);
        }
    }

    /** Revel's Lightslayer form: a dark, lean body on vast bat's wings, with eyes like violet coals. */
    public static final class BatWinged extends CreatureModel {
        private static final Look LOOK = new Look(0x1A1420, 0x08060C, 0x3A2A48, 0x2A2030, 0x0A080C, 0xC080FF, 0x241C2C);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 32.0F, 9.0F, Material.SCALE, Material.SCALE, Material.SCALE);
            both(rig, "head", Material.EYE, -1.4F, -h.head * 0.6F, -h.head * 0.45F - 0.2F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.MEMBRANE, -2.8F, -h.head - 2.0F, -0.5F, 1.0F, 3.0F, 2.0F);
            rig.pair("hand", Material.CLAW, -1.0F, 0.0F, -1.0F, 2, 3, 2);
            wings(rig, "chest", Material.MEMBRANE, -2.0F, -h.chest * 0.6F, 2.0F, 22, 18, 14);
            rig.pair("wing", Material.BONE, -22, -1, 0, 22, 1, 1);
            rig.pair("tip", Material.BONE, -18, -1, 0, 18, 1, 1);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            if (pose.enraged()) {
                Gaits.look(body, pose, 0.7F, 0.6F);
                Gaits.pair(body, "leg0", 25.0F, 0.0F, 0.0F, 30.0F, 0.0F, 0.0F);
                Gaits.pair(body, "arm", 20.0F, 0.0F, 15.0F, 20.0F, 0.0F, 15.0F);
                Gaits.wings(body, pose, 40.0F, 0.35F, 0.0F);
            } else {
                Humanoid.walk(body, pose, 22.0F, 0.6F);
                // On the ground the wings hang down the back like a cloak, the tips folded under.
                Gaits.pair(body, "wing", 0.0F, 30.0F, -70.0F, 0.0F, 30.0F, -70.0F);
                Gaits.pair(body, "tip", 0.0F, 150.0F, 0.0F, 0.0F, 150.0F, 0.0F);
            }

            Bones.turn(body.part("arm_r"), pose.key("claw", 0.0F, 0.0F, 0.4F, -140.0F, 0.6F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 14.7F);
        }
    }

    /** Ki Song's tide of blood: her body come apart into a turning column of blood, her shape still at its crest. */
    public static final class BloodTide extends CreatureModel {
        private static final Look LOOK = new Look(0x7A0A14, 0x2A0206, 0xB0202A, 0x9A1018, 0x4A0A0E, 0xFF3A3A, 0x6A0810);
        private static final int RINGS = 5;
        private static final int BLOBS = 8;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -1.0F, 0.0F);
            for (int r = 0; r < RINGS; r++) {
                String ring = "ring" + r;
                rig.bone(ring, "body", 0.0F, -r * 4.5F, 0.0F);
                float radius = 6.0F - r * 0.8F;
                for (int b = 0; b < BLOBS; b++) {
                    float a = b * Mth.TWO_PI / BLOBS + r * 0.4F;
                    float size = 3.0F - (b % 3) * 0.5F;
                    box(rig, ring, b % 2 == 0 ? Material.BELLY : Material.VEIN, Mth.sin(a) * radius - size / 2.0F, -size, Mth.cos(a) * radius - size / 2.0F,
                        size, size + 1.0F, size);
                }
            }

            rig.bone("head", "body", 0.0F, -RINGS * 4.5F, 0.0F);
            box(rig, "head", Material.BELLY, -3.5F, -2.0F, -2.0F, 7.0F, 3.0F, 4.0F);
            box(rig, "head", Material.BELLY, -2.0F, -7.0F, -2.0F, 4.0F, 5.0F, 4.0F);
            box(rig, "head", Material.VEIN, -2.2F, -7.4F, 0.0F, 4.4F, 6.0F, 2.4F);
            both(rig, "head", Material.EYE, -1.4F, -5.0F, -2.2F, 1.0F, 1.0F, 1.0F);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            float surge = pose.key("surge", 0.0F, 0.0F, 0.35F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
            for (int r = 0; r < RINGS; r++) {
                ModelPart ring = body.part("ring" + r);
                Bones.turn(ring, 0.0F, pose.age() * (r % 2 == 0 ? 4.0F : -5.0F), 0.0F);
                Bones.shift(ring, 0.0F, pose.wave(0.2F, r) * 0.8F, 0.0F);
                Bones.size(ring, 1.0F + 0.35F * surge * (1.0F - r * 0.12F));
            }

            Gaits.look(body, pose, 0.8F, 0.5F);
            Bones.turn(body.part("head"), 20.0F * surge, 0.0F, 0.0F);
            Gaits.fall(body, pose, 6.0F, 0.0F);
        }
    }
}
