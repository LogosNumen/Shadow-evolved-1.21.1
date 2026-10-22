package net.logosnumen.nightmareevolved.client.creature.isles;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Quadruped;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/** The Chained Isles' horrors: the Mordant Mimic, the Wormvine, the Blood-Nourished Root, and the Corrupted isle's horror. */
public final class IsleHorrors {
    private IsleHorrors() {
    }

    /**
     * The Mordant Mimic: a long chest of dark wood bound with dull metal and a flat lid. Roused, it rises on long,
     * emaciated limbs and its lid gapes over rows of jagged teeth.
     */
    public static final class Mimic extends CreatureModel {
        private static final Look LOOK = new Look(0x3A2A1E, 0x140C08, 0x5E4632, 0x8A8070, 0xE0D6C2, 0xFF3A2A, 0x8A8070);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.SCALE, -12, -12, -15, 24, 12, 30);
            for (int s = 0; s < 3; s++) {
                rig.box("body", Material.PLATE, -12.5F, -12.5F, -11 + s * 10, 25, 13, 2);
            }

            rig.box("body", Material.VOID, -11, -12.2F, -14, 22, 1, 28);
            for (int t = 0; t < 9; t++) {
                rig.box("body", Material.BONE, -11 + t * 2.6F, -13.5F, -14.5F, 1, 2, 1);
            }

            rig.bone("jaw", "body", 0.0F, -12.0F, 15.0F);
            rig.box("jaw", Material.SCALE, -12.5F, -3, -30, 25, 3, 30);
            for (int s = 0; s < 3; s++) {
                rig.box("jaw", Material.PLATE, -13, -3.5F, -26 + s * 10, 26, 4, 2);
            }

            for (int t = 0; t < 9; t++) {
                rig.box("jaw", Material.BONE, -11 + t * 2.6F, 0, -29.5F, 1, 2, 1);
                rig.box("jaw", Material.BONE, -11.5F + (t % 2) * 22.0F, 0, -27 + t * 3.0F, 1, 2, 1);
            }

            rig.box("body", Material.EYE, -2, -11.5F, -15.4F, 4, 1, 1);
            for (int i = 0; i < 2; i++) {
                float z = i == 0 ? -11.0F : 11.0F;
                rig.pairBone("leg" + i, "body", -11.0F, 0.0F, z, 0.0F, 0.0F, 15.0F);
                rig.pair("leg" + i, Material.BELLY, -1, 0, -1, 2, 16, 2);
                rig.pairBone("shin" + i, "leg" + i, 0.0F, 15.0F, 0.0F, 0.0F, 0.0F, -15.0F);
                rig.pair("shin" + i, Material.BELLY, -1, 0, -1, 2, 16, 2);
                rig.pair("shin" + i, Material.CLAW, -1.5F, 15, -2, 3, 1, 3);
            }

            rig.pairBone("arm", "body", -12.0F, -10.0F, -12.0F, -60.0F, 0.0F, 20.0F);
            rig.pair("arm", Material.BELLY, -1, 0, -1, 2, 18, 2);
            rig.pair("arm", Material.CLAW, -1.5F, 17, -1.5F, 3, 4, 3);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            boolean roused = pose.enraged();
            float limbs = roused ? 1.0F : 0.01F;
            for (String bone : new String[]{"leg0_r", "leg0_l", "leg1_r", "leg1_l", "arm_r", "arm_l"}) {
                Bones.size(body.part(bone), limbs);
            }

            if (!roused) {
                return;
            }

            Bones.shift(body.part("body"), 0.0F, -30.0F, 0.0F);
            Gaits.hangingLegs(body, pose, 2, 30.0F, 0.9F);
            Bones.turn(body.part("jaw"), -18.0F - Math.max(0.0F, pose.wave(0.2F, 0.0F)) * 12.0F, 0.0F, 0.0F);
            Gaits.pair(body, "arm", pose.wave(0.15F, 0.0F) * 15.0F, 0.0F, 0.0F, pose.wave(0.15F, 1.4F) * 15.0F, 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), pose.key("swallow", 0.0F, 0.0F, 0.5F, -95.0F, 0.62F, 15.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("body"), pose.key("swallow", 0.0F, 0.0F, 0.5F, -15.0F, 0.62F, 10.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), pose.key("snap", 0.0F, 0.0F, 0.4F, -70.0F, 0.58F, 18.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            float rake = pose.key("rake", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -50.0F * Math.abs(rake), 60.0F * rake, 0.0F, -50.0F * Math.abs(rake), 60.0F * rake, 0.0F);
            Gaits.fall(body, pose, 30.0F, 70.0F);
        }
    }

    /**
     * The Wormvine: a pulsing mass of moss with a human shape twisted in agony at its heart, and reddish-brown vines
     * sprawling out from it across the ground.
     */
    public static final class Wormvine extends CreatureModel {
        private static final Look LOOK = new Look(0x6A3A22, 0x2A1408, 0x9A5A3A, 0x3A5A2A, 0xC8B8A0, 0xFFC0D8, 0x5A3A22);
        private static final int VINES = 7;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.VEIN, -20, -18, -20, 40, 18, 40);
            rig.box("body", Material.MOSS, -22, -20, -22, 44, 20, 44);
            rig.bone("figure", "body", 0.0F, -18.0F, -6.0F, -20.0F, 30.0F, 15.0F);
            rig.box("figure", Material.BELLY, -3, -12, -2, 6, 12, 4);
            rig.box("figure", Material.BONE, -2, -17, -2, 4, 5, 4);
            rig.box("figure", Material.BELLY, -7, -14, -1, 4, 3, 2);
            rig.box("figure", Material.BELLY, 3, -12, -1, 6, 2, 2);
            for (int v = 0; v < VINES; v++) {
                float a = v * Mth.TWO_PI / VINES;
                String vine = "vine" + v;
                rig.bone(vine, "body", Mth.sin(a) * 18.0F, -3.0F, Mth.cos(a) * 18.0F, 0.0F, a * Mth.RAD_TO_DEG + 180.0F, 0.0F);
                rig.box(vine, Material.SCALE, -1.5F, -1.5F, -24, 3, 3, 24);
                rig.bone(vine + "b", vine, 0.0F, 0.0F, -24.0F);
                rig.box(vine + "b", Material.SCALE, -1, -1, -22, 2, 2, 22);
                rig.box(vine + "b", Material.GLOW, -1.5F, -1.5F, -23, 3, 3, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.size(body.part("body"), 1.0F + pose.wave(0.08F, 0.0F) * 0.03F);
            for (int v = 0; v < VINES; v++) {
                Bones.turn(body.part("vine" + v), 0.0F, pose.wave(0.05F, v * 1.3F) * 6.0F, 0.0F);
                Bones.turn(body.part("vine" + v + "b"), pose.wave(0.07F, v) * 4.0F, pose.wave(0.06F, v * 0.7F) * 8.0F, 0.0F);
            }

            float lash = pose.key("lash", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int v = 0; v < VINES; v++) {
                Bones.turn(body.part("vine" + v), -20.0F * Math.max(0.0F, -lash), 40.0F * lash, 0.0F);
            }

            float seize = pose.key("seize", 0.0F, 0.0F, 0.5F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("vine0"), -35.0F * seize, 0.0F, 0.0F);
            Bones.size(body.part("vine0b"), 1.0F + 0.6F * seize);
            float sweet = pose.key("sweetness", 0.0F, 0.0F, 0.4F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Bones.size(body.part("figure"), 1.0F + 0.15F * sweet);
            Bones.size(body.part("body"), 1.0F + 0.08F * sweet - 0.3F * pose.fall(0.0F, 1.0F));
        }
    }

    /** The Blood-Nourished Root: a knot of dead, blood-dark root clinging under the island, its tentacle roots hanging into the dark. */
    public static final class Root extends CreatureModel {
        private static final Look LOOK = new Look(0x4A2A1A, 0x1A0A06, 0x7A4A30, 0x8A1A1A, 0x6A3A2A, 0xFF3020, 0x5A2A1A);
        private static final int TENTACLES = 6;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 2.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -48.0F, 0.0F);
            rig.box("body", Material.VEIN, -14, -4, -14, 28, 8, 28);
            rig.box("body", Material.STONE, -10, 4, -10, 20, 6, 20);
            for (int t = 0; t < TENTACLES; t++) {
                float a = t * Mth.TWO_PI / TENTACLES;
                String parent = "body";
                for (int s = 0; s < 4; s++) {
                    String bone = "tent" + t + "_" + s;
                    rig.bone(bone, parent, s == 0 ? Mth.sin(a) * 8.0F : 0.0F, s == 0 ? 8.0F : 10.0F, s == 0 ? Mth.cos(a) * 8.0F : 0.0F,
                        s == 0 ? 15.0F * Mth.cos(a) : 6.0F, 0.0F, s == 0 ? -15.0F * Mth.sin(a) : 0.0F);
                    float w = 3.5F - s * 0.6F;
                    box(rig, bone, s == 3 ? Material.VEIN : Material.SCALE, -w / 2.0F, 0.0F, -w / 2.0F, w, 10.0F, w);
                    parent = bone;
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            for (int t = 0; t < TENTACLES; t++) {
                for (int s = 1; s < 4; s++) {
                    Bones.turn(body.part("tent" + t + "_" + s), pose.wave(0.06F, t + s * 0.8F) * 10.0F, 0.0F, pose.wave(0.05F, t * 1.7F + s) * 10.0F);
                }
            }

            float snatch = pose.key("snatch", 0.0F, 0.0F, 0.5F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
            for (int s = 0; s < 4; s++) {
                Bones.turn(body.part("tent0_" + s), -30.0F * snatch, 0.0F, 0.0F);
            }

            float lash = pose.key("lash", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int t = 0; t < TENTACLES; t++) {
                Bones.turn(body.part("tent" + t + "_1"), 0.0F, 0.0F, 30.0F * lash);
            }

            Bones.size(body.part("body"), 1.0F - 0.3F * pose.fall(0.0F, 1.0F));
        }
    }

    /**
     * The horror of the Corrupted isle (the mod's own reading of a creature the novel never shows): a hunched mass of grey
     * hide and bone plates on four crooked limbs, a cluster of eyes, and a maw that splits its whole front. Built at half size.
     */
    public static final class IsleHorror extends CreatureModel {
        private static final Look LOOK = new Look(0x6A6A70, 0x26262C, 0x9A9AA2, 0x8A3A3A, 0xD8D0C0, 0x9CFF5A, 0x5A5A62);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 2.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 28.0F, 30.0F, 20.0F, 2, Material.SCALE, Material.SCALE);
            for (int p = 0; p < 4; p++) {
                rig.box("body", Material.BONE, -8 + p * 1.5F, -15 - (p % 2), -10 + p * 5, 16 - p * 3, 3, 4);
            }

            rig.box("body", Material.VOID, -8, -11, -12.2F, 16, 8, 1);
            for (int t = 0; t < 7; t++) {
                rig.box("body", Material.BONE, -7.5F + t * 2.3F, -11.5F, -12.6F, 1, 2, 1);
                rig.box("body", Material.BONE, -7.0F + t * 2.3F, -4.5F, -12.6F, 1, 2, 1);
            }

            for (int e = 0; e < 7; e++) {
                rig.box("head", Material.EYE, -4.0F + (e % 4) * 2.4F, -6.0F + (e / 4) * 2.0F, -9.4F, 1, 1, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 22.0F, 0.5F, 2);
            Quadruped.bite(body, pose, "maw");
            float trample = pose.key("trample", 0.0F, 0.0F, 0.15F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), 10.0F * trample, 0.0F, 0.0F);
            float slam = pose.key("slam", 0.0F, 0.0F, 0.55F, 1.0F, 0.65F, -0.4F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), -18.0F * Math.max(0.0F, slam), 0.0F, 0.0F);
            Gaits.pair(body, "leg0", -70.0F * Math.max(0.0F, slam), 0.0F, 0.0F, -70.0F * Math.max(0.0F, slam), 0.0F, 0.0F);
            Gaits.fall(body, pose, 10.0F, 80.0F);
        }
    }
}
