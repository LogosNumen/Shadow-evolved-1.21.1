package net.logosnumen.nightmareevolved.client.creature.shore;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/**
 * The Forgotten Shore's stranger horrors: the Crimson Terror, the Lord of the Dead, the Soul Devouring Tree, the blood
 * flower, the rolling stone, and the Vile Thieving Bird's unhatched spawn.
 */
public final class ShoreHorrors {
    private ShoreHorrors() {
    }

    /**
     * The Crimson Terror: a hunched giant of crimson coral grown through mutilated flesh, studded with screaming human
     * faces whose eyes are wells of darkness, until they blaze with the light of the sun it was made to anchor.
     * Built at half size.
     */
    public static final class Terror extends CreatureModel {
        private static final Look LOOK = new Look(0xA02028, 0x3A0408, 0xD84048, 0xC88878, 0xC03038, 0xFFF0C0, 0xB82830);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 2.0F;
        }

        private static void face(Rig rig, String bone, float x, float y, float z) {
            rig.box(bone, Material.BELLY, x, y, z, 3, 4, 1);
            rig.box(bone, Material.VOID, x + 0.4F, y + 1.0F, z - 0.2F, 1, 1, 1);
            rig.box(bone, Material.VOID, x + 1.6F, y + 1.0F, z - 0.2F, 1, 1, 1);
            rig.box(bone, Material.VOID, x + 1.0F, y + 2.6F, z - 0.2F, 1, 1, 1);
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -16.0F, 0.0F);
            rig.box("body", Material.STONE, -8, -16, -6, 16, 18, 12);
            rig.box("body", Material.BELLY, -7, -14, -6.5F, 14, 12, 1);
            rig.box("body", Material.STONE, -6, 2, -5, 12, 4, 10);
            for (int i = 0; i < 9; i++) {
                face(rig, "body", -6.5F + (i % 3) * 4.5F, -13.0F + (i / 3) * 4.2F, -7.2F);
            }

            for (int c = 0; c < 6; c++) {
                float x = -7.0F + (c % 3) * 6.0F;
                rig.box("body", Material.HORN, x, -22 + (c / 3) * 3, -2 + (c % 2) * 4, 2, 7 - (c % 3), 2);
            }

            rig.bone("head", "body", 0.0F, -16.0F, -2.0F);
            rig.box("head", Material.STONE, -5, -9, -5, 10, 9, 9);
            for (int i = 0; i < 4; i++) {
                face(rig, "head", -4.5F + (i % 2) * 5.0F, -8.0F + (i / 2) * 4.0F, -5.6F);
            }

            // The light behind its eyes, shown only when it blazes.
            rig.bone("blaze", "head", 0.0F, -5.0F, -6.0F);
            rig.box("blaze", Material.GLOW, -4, -3, -1, 8, 6, 1);
            rig.pairBone("arm", "body", -9.0F, -13.0F, 0.0F, 0.0F, 0.0F, 12.0F);
            rig.pair("arm", Material.STONE, -3, -1, -3, 6, 16, 6);
            rig.pair("arm", Material.BELLY, -3.5F, 4, -3.5F, 2, 6, 7);
            rig.pairBone("hand", "arm", 0.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("hand", Material.STONE, -3.5F, 0, -4, 7, 6, 8);
            rig.pair("hand", Material.HORN, -3, 5, -5, 2, 4, 2);
            rig.pairBone("leg0", "body", -4.5F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("leg0", Material.STONE, -3, 0, -3, 6, 12, 6);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.hangingLegs(body, pose, 1, 18.0F, 0.35F);
            Gaits.look(body, pose, 0.5F, 0.4F);
            Bones.turn(body.part("body"), 12.0F + pose.wave(0.06F, 0.0F) * 2.0F, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -10.0F + pose.gait(0.35F, 0.0F) * 15.0F, 0.0F, 0.0F, -10.0F + pose.gait(0.35F, 1.0F) * 15.0F, 0.0F, 0.0F);
            float crush = pose.key("crush", 0.0F, 0.0F, 0.55F, -170.0F, 0.68F, -20.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", crush, 0.0F, 0.0F, crush, 0.0F, 0.0F);
            float lash = pose.key("lash", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), 0.0F, 40.0F * lash, 0.0F);
            Bones.turn(body.part("arm_r"), -70.0F * Math.abs(lash), 0.0F, 0.0F);
            float blaze = pose.key("sunlight", 0.0F, 0.0F, 0.5F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.size(body.part("blaze"), Math.max(0.01F, blaze));
            Bones.turn(body.part("head"), -15.0F * blaze, 0.0F, 0.0F);
            Gaits.fall(body, pose, 10.0F, 70.0F);
        }
    }

    /** The Lord of the Dead: a heaving mountain of bones and skulls, arms of bone reaching out of it. Built small and drawn large. */
    public static final class BoneLord extends CreatureModel {
        private static final Look LOOK = new Look(0xC8BEA8, 0x3A3028, 0xE8E0D0, 0xA89C88, 0xE0D6C2, 0x80F0FF, 0xB0A690);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 2.6F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.BONE, -14, -10, -12, 28, 10, 24);
            rig.box("body", Material.BONE, -10, -18, -9, 20, 8, 18);
            rig.box("body", Material.BONE, -6, -24, -5, 12, 6, 10);
            for (int i = 0; i < 14; i++) {
                float a = i * 2.4F;
                float r = 9.0F + (i % 4) * 2.0F;
                float x = Mth.cos(a) * r - 1.5F;
                float z = Mth.sin(a) * r * 0.8F - 1.5F;
                float y = -10.0F - (i % 5) * 3.0F + (r > 12.0F ? 6.0F : 0.0F);
                rig.box("body", Material.BONE, x, y, z, 3, 3, 3);
                rig.box("body", Material.VOID, x + 0.5F, y + 1.0F, z - 0.2F, 1, 1, 1);
                rig.box("body", Material.BONE, x - 2.0F, y + 2.0F, z + 1.0F, 6, 1, 1);
            }

            rig.bone("head", "body", 0.0F, -24.0F, -3.0F);
            rig.box("head", Material.BONE, -4, -7, -4, 8, 7, 7);
            both(rig, "head", Material.EYE, -2.8F, -5.0F, -4.2F, 2, 2, 1);
            rig.box("head", Material.VOID, -2, -2, -4.2F, 4, 1, 1);
            for (int k = 0; k < 2; k++) {
                rig.pairBone("arm" + k, "body", -12.0F + k * 2.0F, -12.0F + k * 4.0F, -4.0F + k * 6.0F, -30.0F, 0.0F, 50.0F - k * 10.0F);
                rig.pair("arm" + k, Material.BONE, -1.5F, -1, -1.5F, 3, 16, 3);
                rig.pair("arm" + k, Material.CLAW, -2.5F, 14, -2.5F, 5, 3, 5);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.1F, 0.0F) * 0.5F - Math.abs(pose.gait(0.3F, 0.0F)) * pose.stride(), 0.0F);
            Bones.turn(body.part("body"), 0.0F, 0.0F, pose.gait(0.3F, 0.0F) * 3.0F);
            Gaits.look(body, pose, 0.4F, 0.3F);
            for (int k = 0; k < 2; k++) {
                float sway = pose.wave(0.1F, k * 2.0F) * 12.0F;
                Gaits.pair(body, "arm" + k, sway, 0.0F, 0.0F, -sway, 0.0F, 0.0F);
            }

            float crash = pose.key("crash", 0.0F, 0.0F, 0.6F, -1.0F, 0.72F, 0.6F, 1.0F, 0.0F);
            Gaits.pair(body, "arm0", 120.0F * Math.min(0.0F, crash) - 20.0F * Math.max(0.0F, crash), 0.0F, -30.0F * crash,
                120.0F * Math.min(0.0F, crash) - 20.0F * Math.max(0.0F, crash), 0.0F, -30.0F * crash);
            float spears = pose.key("spears", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm1", -90.0F * spears, 0.0F, 0.0F, -90.0F * spears, 0.0F, 0.0F);
            float grasp = pose.key("grasp", 0.0F, 0.0F, 0.5F, 1.0F, 0.65F, 0.2F, 1.0F, 0.0F);
            Gaits.pair(body, "arm0", -70.0F * grasp, 0.0F, 30.0F * grasp, -70.0F * grasp, 0.0F, 30.0F * grasp);
            float f = pose.fall(0.0F, 1.0F);
            Bones.shift(body.part("body"), 0.0F, 10.0F * f, 0.0F);
            Bones.size(body.part("body"), 1.0F - 0.3F * f);
        }
    }

    /**
     * The Soul Devouring Tree, at the foot of its trunk: a great knot of black bark split by a maw lined with thorns, red
     * veins glowing through it, its roots lying out across the ash ready to lash. The tree itself towers above it.
     */
    public static final class SoulTree extends CreatureModel {
        private static final Look LOOK = new Look(0x141010, 0x060404, 0x2A2222, 0x7A1414, 0x3A2A28, 0xFF2020, 0xB01818);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.VEIN, -20, -60, -10, 40, 60, 24);
            rig.box("body", Material.STONE, -22, -20, -12, 44, 20, 26);
            rig.bone("maw", "body", 0.0F, -26.0F, -10.0F);
            rig.box("maw", Material.VOID, -8, -10, -1, 16, 18, 1);
            for (int t = 0; t < 6; t++) {
                rig.box("maw", Material.HORN, -7 + t * 2.6F, -11, -2, 1, 3, 1);
                rig.box("maw", Material.HORN, -7 + t * 2.6F, 6, -2, 1, 3, 1);
            }

            rig.box("maw", Material.EYE, -2, -16, -2, 4, 3, 1);
            for (int i = 0; i < 4; i++) {
                String root = "root" + i;
                float a = -0.9F + i * 0.6F;
                rig.bone(root, "body", Mth.sin(a) * 18.0F, -2.0F, -10.0F, 0.0F, a * Mth.RAD_TO_DEG, 0.0F);
                rig.box(root, Material.STONE, -2.5F, -2, -30, 5, 4, 30);
                rig.box(root, Material.VEIN, -1.5F, -1.5F, -42, 3, 3, 12);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            for (int i = 0; i < 4; i++) {
                Bones.turn(body.part("root" + i), pose.wave(0.05F, i * 1.4F) * 3.0F, pose.wave(0.04F, i) * 4.0F, 0.0F);
            }

            float lash = pose.key("lash", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int i = 0; i < 4; i++) {
                Bones.turn(body.part("root" + i), -25.0F * Math.max(0.0F, -lash), 50.0F * lash * (i < 2 ? 1.0F : -1.0F), 0.0F);
            }

            float enthrall = pose.key("enthrall", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.size(body.part("maw"), 1.0F + 0.25F * enthrall + pose.wave(0.08F, 0.0F) * 0.03F);
            Bones.size(body.part("body"), 1.0F - 0.15F * pose.fall(0.0F, 1.0F));
        }
    }

    /** A blood flower: a tall stalk and a great red bloom whose heart breathes out a red cloud of pollen. */
    public static final class Flower extends CreatureModel {
        private static final Look LOOK = new Look(0xC01828, 0x400810, 0xF04050, 0x3A5A2A, 0x4A6A2A, 0xFF4050, 0xD02030);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.BELLY, -1.5F, -18, -1.5F, 3, 18, 3);
            both(rig, "body", Material.BELLY, -7, -8, -1, 6, 1, 4);
            rig.bone("bloom", "body", 0.0F, -18.0F, 0.0F);
            rig.box("bloom", Material.GLOW, -3, -3, -3, 6, 4, 6);
            for (int p = 0; p < 6; p++) {
                String petal = "petal" + p;
                rig.bone(petal, "bloom", 0.0F, -1.0F, 0.0F, 0.0F, p * 60.0F, 0.0F);
                rig.box(petal, Material.MEMBRANE, -3, -1, -12, 6, 1, 9);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.turn(body.part("body"), pose.wave(0.05F, 0.0F) * 3.0F, 0.0F, pose.wave(0.04F, 1.0F) * 3.0F);
            float open = 25.0F + pose.wave(0.08F, 0.0F) * 5.0F - 45.0F * pose.key("pollen", 0.0F, 0.0F, 0.5F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            for (int p = 0; p < 6; p++) {
                Bones.turn(body.part("petal" + p), -open, 0.0F, 0.0F);
            }

            Bones.size(body.part("body"), 1.0F - 0.6F * pose.fall(0.0F, 1.0F));
        }
    }

    /** The rolling stone: a boulder with a mouth of long, sharp teeth across its face. */
    public static final class Stone extends CreatureModel {
        private static final Look LOOK = new Look(0x6A6460, 0x2A2624, 0x9A948E, 0x5A1A1A, 0xE0D8C8, 0xFFD040, 0x7A746E);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -11.0F, 0.0F);
            rig.box("body", Material.STONE, -10, -10, -10, 20, 20, 20);
            rig.box("body", Material.STONE, -11, -7, -7, 22, 14, 14);
            rig.box("body", Material.STONE, -7, -11, -7, 14, 22, 14);
            rig.box("body", Material.STONE, -7, -7, -11, 14, 14, 22);
            rig.bone("jaw", "body", 0.0F, 1.0F, -10.5F);
            rig.box("jaw", Material.VOID, -7, -3, -1, 14, 6, 1);
            for (int t = 0; t < 7; t++) {
                rig.box("jaw", Material.BONE, -6.5F + t * 1.9F, -3, -1.5F, 1, 3, 1);
                rig.box("jaw", Material.BONE, -6.0F + t * 1.9F, 0.5F, -1.5F, 1, 3, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            float roll = pose.key("roll", 0.0F, 0.0F, 0.15F, 0.0F, 0.9F, 1.0F, 1.0F, 1.0F);
            Bones.turn(body.part("body"), pose.walk() * 30.0F + 720.0F * roll, 0.0F, 0.0F);
            float gnash = pose.key("gnash", 0.0F, 0.0F, 0.4F, 1.0F, 0.6F, -0.3F, 1.0F, 0.0F);
            Bones.size(body.part("jaw"), 1.0F + 0.4F * gnash + pose.wave(0.3F, 0.0F) * 0.04F);
            Bones.shift(body.part("body"), 0.0F, 6.0F * pose.fall(0.0F, 1.0F), 0.0F);
        }
    }

    /** The Vile Thieving Bird's spawn, unhatched: a huge smooth egg of stone, its greys drifting like clouds. */
    public static final class Egg extends CreatureModel {
        private static final Look LOOK = new Look(0x8A8A90, 0x4A4A52, 0xC0C0C8, 0x6A6A72, 0x9A9AA2, 0xA060FF, 0x7A7A82);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            int[][] rings = {{12, 0, 6}, {15, 6, 12}, {16, 12, 20}, {15, 20, 28}, {12, 28, 34}, {8, 34, 39}, {4, 39, 41}};
            for (int[] ring : rings) {
                int w = ring[0];
                rig.box("body", Material.STONE, -w / 2.0F, -ring[2], -w / 2.0F, w, ring[2] - ring[1], w);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            float shudder = pose.key("shudder", 0.0F, 0.0F, 0.3F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), Mth.sin(pose.age() * 2.5F) * 4.0F * shudder, 0.0F, Mth.cos(pose.age() * 2.1F) * 4.0F * shudder);
            Bones.size(body.part("body"), 1.0F - 0.2F * pose.fall(0.0F, 1.0F));
        }
    }
}
