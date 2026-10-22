package net.logosnumen.nightmareevolved.client.creature.godgrave;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.creature.Quadruped;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/** The beasts of Godgrave's hollows. */
public final class GodgraveBeasts {
    private GodgraveBeasts() {
    }

    /** The maggot beast: mottled fur seething with pale maggots, a misshapen head, limbs of claws, scythes and tentacles. */
    public static final class MaggotBeast extends CreatureModel {
        private static final Look LOOK = new Look(0x6A5A4A, 0x2A2018, 0x9A8A70, 0xE8E0D0, 0xD8D0C0, 0xFFE0A0, 0x5A4A3A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 40.0F, 44.0F, 20.0F, 0, Material.FUR, Material.FUR);
            for (int m = 0; m < 14; m++) {
                box(rig, "body", Material.BELLY, -9.0F + (m * 7) % 18, -17.0F - (m % 3), -14.0F + (m * 5) % 28, 2.0F, 1.0F, 1.0F);
            }

            box(rig, "head", Material.FUR, -6.0F, -12.0F, -10.0F, 12.0F, 6.0F, 6.0F);
            for (int t = 0; t < 4; t++) {
                box(rig, "jaw", Material.BONE, -3.0F + t * 1.8F, -1.5F, -12.0F, 1.0F, 2.0F, 1.0F);
            }

            both(rig, "head", Material.EYE, -5.0F, -9.0F, -9.4F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("scythe", "body", -10.0F, -10.0F, -14.0F, -30.0F, 0.0F, 20.0F);
            rig.pair("scythe", Material.BONE, -1, 0, -1, 2, 14, 2);
            rig.pair("scythe", Material.BONE, -1, 12, -8, 2, 2, 8);
            for (int t = 0; t < 3; t++) {
                String parent = "body";
                for (int s = 0; s < 3; s++) {
                    String bone = "tent" + t + "_" + s;
                    rig.bone(bone, parent, s == 0 ? -6.0F + t * 6.0F : 0.0F, s == 0 ? -4.0F : 0.0F, s == 0 ? -16.0F : -6.0F, s == 0 ? 30.0F : 15.0F, 0.0F, 0.0F);
                    rig.box(bone, Material.BELLY, -1, -1, -6, 2, 2, 6);
                    parent = bone;
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 26.0F, 0.6F, 0);
            for (int t = 0; t < 3; t++) {
                for (int s = 0; s < 3; s++) {
                    Bones.turn(body.part("tent" + t + "_" + s), pose.wave(0.15F, t + s) * 15.0F, pose.wave(0.12F, t * 2.0F + s) * 15.0F, 0.0F);
                }
            }

            Gaits.pair(body, "scythe", pose.key("rend", 0.0F, 0.0F, 0.4F, -100.0F, 0.6F, 30.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("rend", 0.0F, 0.0F, 0.4F, -100.0F, 0.6F, 30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Quadruped.bite(body, pose, "maw");
            float tent = pose.key("tentacles", 0.0F, 0.0F, 0.5F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
            for (int t = 0; t < 3; t++) {
                Bones.turn(body.part("tent" + t + "_0"), -40.0F * tent, 0.0F, 0.0F);
            }

            Gaits.fall(body, pose, 18.0F, 80.0F);
        }
    }

    /** The jackal giant: a humanoid hill of scorched, rotting flesh with a jackal's head and golden fangs. Built small. */
    public static final class JackalGiant extends CreatureModel {
        private static final Look LOOK = new Look(0x2A1E1A, 0x0A0606, 0x4A3A30, 0x5A2A20, 0xE0B040, 0xFF8030, 0x3A2A22);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 3.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 48.0F, 16.0F, Material.SCALE, Material.SCALE, Material.SCALE);
            box(rig, "chest", Material.VEIN, -7.0F, -h.chest * 0.6F, -4.4F, 14.0F, h.chest * 0.5F, 1.0F);
            box(rig, "head", Material.SCALE, -2.5F, -h.head * 0.6F, -10.0F, 5.0F, 4.0F, 6.0F);
            rig.bone("jaw", "head", 0.0F, -1.0F, -4.0F);
            rig.box("jaw", Material.SCALE, -2, 0, -6, 4, 2, 6);
            for (int t = 0; t < 3; t++) {
                rig.box("jaw", Material.HORN, -1.5F + t * 1.2F, -1.5F, -5.5F, 1, 2, 1);
            }

            both(rig, "head", Material.SCALE, -3.6F, -h.head - 5.0F, -0.5F, 2.0F, 6.0F, 1.0F);
            both(rig, "head", Material.EYE, -2.4F, -h.head * 0.8F, -4.6F, 1.0F, 1.0F, 1.0F);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 18.0F, 0.3F);
            Gaits.pair(body, "arm", pose.key("smash", 0.0F, 0.0F, 0.55F, -170.0F, 0.66F, -25.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("smash", 0.0F, 0.0F, 0.55F, -170.0F, 0.66F, -25.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            float jaws = pose.key("jaws", 0.0F, 0.0F, 0.45F, -1.0F, 0.62F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("chest"), 25.0F * Math.max(0.0F, jaws), 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), 40.0F * Math.max(0.0F, -jaws), 0.0F, 0.0F);
            Gaits.pair(body, "arm", pose.key("grab", 0.0F, 0.0F, 0.5F, -90.0F, 0.75F, -60.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("grab", 0.0F, 0.0F, 0.5F, -90.0F, 0.75F, -60.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 22.0F);
        }
    }

    /** A crimson python, long and heavy, lying coiled like the red vines of a bridge until it strikes. */
    public static final class Python extends CreatureModel {
        private static final Look LOOK = new Look(0xA01820, 0x3A0808, 0xD04048, 0xC88878, 0xE0D0C0, 0xFFD040, 0x8A1418);
        private static final int SEGMENTS = 9;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -4.0F, -10.0F);
            rig.bone("head", "body", 0.0F, 0.0F, 0.0F);
            rig.box("head", Material.SCALE, -3, -3, -7, 6, 5, 7);
            both(rig, "head", Material.EYE, -3.2F, -2.0F, -5.5F, 1.0F, 1.0F, 1.0F);
            rig.bone("jaw", "head", 0.0F, 1.0F, -2.0F);
            rig.box("jaw", Material.BELLY, -2.5F, 0, -5, 5, 1, 5);
            String parent = "body";
            for (int s = 0; s < SEGMENTS; s++) {
                String seg = "seg" + s;
                rig.bone(seg, parent, 0.0F, 0.0F, s == 0 ? 0.0F : 6.0F, 0.0F, s == 0 ? 0.0F : 22.0F, 0.0F);
                float w = 5.5F - Math.max(0, s - 4) * 0.7F;
                box(rig, seg, Material.SCALE, -w / 2.0F, -w / 2.0F, 0.0F, w, w, 6.0F);
                parent = seg;
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sway(body, pose, "seg", SEGMENTS, 14.0F, 0.15F + 0.6F * pose.stride(), 0.7F);
            Gaits.look(body, pose, 0.6F, 0.4F);
            float strike = pose.key("strike", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), -30.0F * Math.max(0.0F, -strike) + 15.0F * Math.max(0.0F, strike), 0.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, -6.0F * Math.max(0.0F, -strike), -8.0F * Math.max(0.0F, strike));
            Bones.turn(body.part("jaw"), 45.0F * Math.max(0.0F, -strike), 0.0F, 0.0F);
            float coil = pose.key("coil", 0.0F, 0.0F, 0.5F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            for (int s = 1; s < SEGMENTS; s++) {
                Bones.turn(body.part("seg" + s), 0.0F, 18.0F * coil, 0.0F);
            }

            Gaits.fall(body, pose, 0.0F, 180.0F);
        }
    }

    /** The black tiger: black fur striped with open red wounds, blood-red grass sprouting from the rot. */
    public static final class BlackTiger extends CreatureModel {
        private static final Look LOOK = new Look(0x101012, 0x020203, 0x26262C, 0xA01820, 0xE0D8C8, 0xFF2A2A, 0x1A1A1E);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 28.0F, 40.0F, 13.0F, 4, Material.VEIN, Material.FUR);
            for (int g = 0; g < 6; g++) {
                box(rig, "body", Material.MOSS, -5.0F + (g * 4) % 10, -13.0F, -12.0F + g * 5.0F, 2.0F, 2.0F, 2.0F);
            }

            both(rig, "head", Material.EYE, -4.0F, -6.0F, -10.4F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.FUR, -4.5F, -9.5F, -4.0F, 2.0F, 2.0F, 1.0F);
            for (int t = 0; t < 2; t++) {
                both(rig, "jaw", Material.BONE, -2.0F, -1.5F, -10.0F + t, 1.0F, 2.0F, 1.0F);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 30.0F, 0.75F, 4);
            Quadruped.bite(body, pose, "bite");
            Quadruped.pounce(body, pose, "pounce");
            Gaits.pair(body, "leg0", pose.key("rake", 0.0F, 0.0F, 0.4F, -90.0F, 0.6F, 20.0F, 1.0F, 0.0F), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            Gaits.fall(body, pose, 12.0F, 80.0F);
        }
    }

    /** A blood mosquito the size of a dog, its swollen belly glistening with rotten blood. */
    public static final class Mosquito extends CreatureModel {
        private static final Look LOOK = new Look(0x3A2A2A, 0x140C0C, 0x6A5050, 0x8A1010, 0x2A1A1A, 0xFF4040, 0x9A8A8A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -8.0F, 0.0F);
            rig.box("body", Material.SCALE, -2, -2, -3, 4, 4, 5);
            rig.bone("abdomen", "body", 0.0F, 0.0F, 2.0F, -15.0F, 0.0F, 0.0F);
            rig.box("abdomen", Material.BELLY, -2.5F, -2, 0, 5, 4, 9);
            rig.bone("head", "body", 0.0F, -0.5F, -3.0F);
            rig.box("head", Material.SCALE, -1.5F, -1.5F, -3, 3, 3, 3);
            both(rig, "head", Material.EYE, -1.8F, -1.0F, -2.8F, 1.0F, 2.0F, 2.0F);
            rig.box("head", Material.CLAW, -0.5F, 0, -10, 1, 1, 7);
            rig.pairBone("wing", "body", -1.5F, -2.0F, -1.0F, 0.0F, -10.0F, 0.0F);
            rig.pair("wing", Material.MEMBRANE, -12, -0.5F, 0, 12, 1, 4);
            for (int i = 0; i < 3; i++) {
                rig.pairBone("leg" + i, "body", -2.0F, 1.5F, -2.0F + i * 2.0F, 0.0F, -20.0F + i * 20.0F, 40.0F);
                rig.pair("leg" + i, Material.SCALE, -10, -0.5F, -0.5F, 10, 1, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.pair(body, "wing", 0.0F, 0.0F, pose.wave(3.0F, 0.0F) * 30.0F, 0.0F, 0.0F, pose.wave(3.0F, 0.0F) * 30.0F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.4F, 0.0F) * 1.0F, 0.0F);
            Bones.turn(body.part("head"), pose.key("pierce", 0.0F, 0.0F, 0.4F, -20.0F, 0.6F, 30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.fall(body, pose, 6.0F, 180.0F);
        }
    }

    /** A harpy: a withered, wild-haired woman's body on great wings, with an eagle's talons for feet and hands. */
    public static final class Harpy extends CreatureModel {
        private static final Look LOOK = new Look(0x6A5A4A, 0x2A2018, 0x9A8A70, 0xB0A090, 0x2A2420, 0xFFD040, 0x5A4A3A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 29.0F, 7.0F, Material.BELLY, Material.BELLY, Material.BELLY);
            box(rig, "head", Material.SMOKE, -3.0F, -h.head - 1.0F, -1.0F, 6.0F, h.head + 5.0F, 4.0F);
            both(rig, "head", Material.EYE, -1.4F, -h.head * 0.6F, -2.4F, 1.0F, 1.0F, 1.0F);
            rig.pair("hand", Material.CLAW, -1, 0, -1, 2, 3, 2);
            rig.pairBone("wing", "chest", -2.0F, -h.chest * 0.6F, 1.5F, 0.0F, -15.0F, -10.0F);
            rig.pair("wing", Material.FEATHER, -18, -0.5F, 0, 18, 1, 9);
            rig.pairBone("tip", "wing", -18.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("tip", Material.FEATHER, -12, -0.5F, 0, 12, 1, 11);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.wings(body, pose, 35.0F, 0.35F, 0.0F);
            Gaits.look(body, pose, 0.7F, 0.6F);
            Gaits.pair(body, "leg0", 35.0F, 0.0F, 0.0F, 35.0F, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -30.0F, 0.0F, 15.0F, -30.0F, 0.0F, 15.0F);
            Gaits.pair(body, "leg0", pose.key("swoop", 0.0F, 0.0F, 0.6F, -80.0F, 0.85F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("swoop", 0.0F, 0.0F, 0.6F, -80.0F, 0.85F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("talons", 0.0F, 0.0F, 0.4F, -120.0F, 0.6F, 20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("head"), pose.key("shriek", 0.0F, 0.0F, 0.4F, -30.0F, 0.8F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.fall(body, pose, 10.0F, 80.0F);
        }
    }

    /** A hound as big as a hill, three-headed. Built small and drawn large. */
    public static final class Hound extends CreatureModel {
        private static final Look LOOK = new Look(0x1A1414, 0x060404, 0x3A2E2E, 0x5A2A20, 0xE0D0C0, 0xFF6020, 0x2A2020);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 3.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 32.0F, 34.0F, 14.0F, 3, Material.FUR, Material.FUR);
            both(rig, "head", Material.EYE, -4.0F, -8.0F, -12.8F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("side", "body", -6.0F, -12.0F, -12.0F, 0.0F, 25.0F, 0.0F);
            rig.pair("side", Material.FUR, -4, -5, -11, 8, 9, 12);
            rig.pair("side", Material.FUR, -3, 2, -12, 6, 3, 8);
            rig.pair("side", Material.EYE, -4.2F, -3, -10.5F, 1, 1, 1);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 26.0F, 0.45F, 3);
            Gaits.pair(body, "side", pose.wave(0.1F, 0.0F) * 8.0F, pose.wave(0.08F, 0.5F) * 10.0F, 0.0F, pose.wave(0.1F, 1.7F) * 8.0F,
                pose.wave(0.08F, 2.0F) * 10.0F, 0.0F);
            Quadruped.bite(body, pose, "jaws");
            Quadruped.pounce(body, pose, "pounce");
            float howl = pose.key("howl", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), -40.0F * howl, 0.0F, 0.0F);
            Gaits.pair(body, "side", -40.0F * howl, 0.0F, 0.0F, -40.0F * howl, 0.0F, 0.0F);
            Gaits.fall(body, pose, 14.0F, 80.0F);
        }
    }

    /** A horned lion with a living viper for a tail. */
    public static final class ViperLion extends CreatureModel {
        private static final Look LOOK = new Look(0x8A6A3A, 0x3A2A14, 0xB8965A, 0xD8C8A0, 0x2A2420, 0x9CFF5A, 0x3A6A2A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 30.0F, 40.0F, 14.0F, 4, Material.FUR, Material.FUR);
            box(rig, "head", Material.FUR, -7.0F, -12.0F, -6.0F, 14.0F, 12.0F, 5.0F);
            both(rig, "head", Material.HORN, -5.0F, -15.0F, -4.0F, 1.5F, 6.0F, 1.5F);
            both(rig, "head", Material.EYE, -4.0F, -7.0F, -11.4F, 1.0F, 1.0F, 1.0F);
            // The viper at the end of its tail.
            rig.bone("viper", "tail3", 0.0F, 0.0F, 7.0F, -30.0F, 0.0F, 0.0F);
            rig.box("viper", Material.PLATE, -1.5F, -1.5F, 0, 3, 3, 5);
            rig.box("viper", Material.EYE, -1.6F, -1.0F, 3, 1, 1, 1);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 28.0F, 0.7F, 4);
            Quadruped.bite(body, pose, "bite");
            Quadruped.pounce(body, pose, "pounce");
            float viper = pose.key("viper", 0.0F, 0.0F, 0.4F, 1.0F, 0.6F, -1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("tail0"), -50.0F * viper, 0.0F, 0.0F);
            Bones.turn(body.part("tail1"), -40.0F * viper, 0.0F, 0.0F);
            Bones.turn(body.part("viper"), 60.0F * Math.max(0.0F, -viper), 0.0F, 0.0F);
            Gaits.fall(body, pose, 14.0F, 80.0F);
        }
    }

    /** The tyrant lizard: an enormous reptile on two mighty hind legs, with little arms, a long tail and a huge head. Built small. */
    public static final class TyrantLizard extends CreatureModel {
        private static final Look LOOK = new Look(0x4A5A3A, 0x1A2014, 0x7A8A5A, 0x9A8A6A, 0xE0D8C8, 0xFFC040, 0x3A4A2A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 2.5F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -18.0F, 0.0F);
            rig.box("body", Material.SCALE, -6, -8, -10, 12, 12, 20);
            rig.box("body", Material.BELLY, -5, 3, -9, 10, 2, 16);
            rig.bone("head", "body", 0.0F, -6.0F, -10.0F, 10.0F, 0.0F, 0.0F);
            rig.box("head", Material.SCALE, -4, -6, -14, 8, 7, 14);
            both(rig, "head", Material.EYE, -4.2F, -4.0F, -6.0F, 1.0F, 1.0F, 1.0F);
            rig.bone("jaw", "head", 0.0F, 1.0F, -2.0F);
            rig.box("jaw", Material.SCALE, -3.5F, 0, -12, 7, 3, 12);
            for (int t = 0; t < 5; t++) {
                both(rig, "jaw", Material.BONE, -3.2F, -1.0F, -11.0F + t * 2.0F, 1.0F, 1.0F, 1.0F);
            }

            rig.pairBone("arm", "body", -6.0F, 0.0F, -7.0F, -40.0F, 0.0F, 10.0F);
            rig.pair("arm", Material.SCALE, -1, 0, -1, 2, 5, 2);
            rig.pairBone("leg0", "body", -5.0F, -1.0F, 4.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("leg0", Material.SCALE, -2.5F, 0, -3, 5, 11, 6);
            rig.pairBone("shin0", "leg0", 0.0F, 10.0F, 2.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("shin0", Material.SCALE, -2, 0, -2, 4, 10, 4);
            rig.pair("shin0", Material.CLAW, -2.5F, 9, -5, 5, 1, 6);
            String parent = "body";
            for (int s = 0; s < 4; s++) {
                String bone = "tail" + s;
                rig.bone(bone, parent, 0.0F, s == 0 ? -4.0F : 0.0F, s == 0 ? 10.0F : 7.0F, s == 0 ? -5.0F : 3.0F, 0.0F, 0.0F);
                float w = 8.0F - s * 1.6F;
                box(rig, bone, Material.SCALE, -w / 2.0F, -w / 2.0F, 0.0F, w, w, 7.0F);
                parent = bone;
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.hangingLegs(body, pose, 1, 30.0F, 0.5F);
            Gaits.look(body, pose, 0.6F, 0.4F);
            Gaits.sway(body, pose, "tail", 4, 8.0F, 0.25F, 0.6F);
            Bones.turn(body.part("body"), pose.gait(0.5F, 0.0F) * 3.0F, 0.0F, pose.gait(0.5F, 0.5F) * 2.0F);
            float jaws = pose.key("jaws", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), 25.0F * jaws, 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), 45.0F * Math.max(0.0F, -jaws), 0.0F, 0.0F);
            float stomp = pose.key("stomp", 0.0F, 0.0F, 0.5F, 1.0F, 0.62F, -0.3F, 1.0F, 0.0F);
            Bones.turn(body.part("leg0_r"), -50.0F * Math.max(0.0F, stomp), 0.0F, 0.0F);
            Bones.turn(body.part("body"), 12.0F * pose.key("charge", 0.0F, 0.0F, 0.15F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.fall(body, pose, 10.0F, 80.0F);
        }
    }

    /** An ash worm: a long grey worm of dead flesh, a round maw ringed with teeth at its front. Built small and drawn large. */
    public static final class AshWorm extends CreatureModel {
        private static final Look LOOK = new Look(0x8A8A86, 0x3A3A38, 0xB0B0AA, 0x6A6A66, 0xD8D0C0, 0x2A2A28, 0x5A5A56);
        private static final int SEGMENTS = 10;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 2.5F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -5.0F, -6.0F);
            rig.bone("head", "body", 0.0F, 0.0F, 0.0F);
            rig.box("head", Material.BELLY, -5, -5, -7, 10, 10, 7);
            rig.box("head", Material.VEIN, -5.5F, -5.5F, -2, 11, 11, 1);
            for (int t = 0; t < 10; t++) {
                float a = t * Mth.TWO_PI / 10.0F;
                rig.box("head", Material.BONE, Mth.sin(a) * 3.6F - 0.5F, Mth.cos(a) * 3.6F - 0.5F, -7.6F, 1, 1, 2);
            }

            rig.box("head", Material.EYE, -2.5F, -2.5F, -7.2F, 5, 5, 1);
            String parent = "body";
            for (int s = 0; s < SEGMENTS; s++) {
                String seg = "seg" + s;
                rig.bone(seg, parent, 0.0F, 0.0F, s == 0 ? 0.0F : 7.0F);
                float w = 9.5F - Math.max(0, s - 5) * 1.2F;
                box(rig, seg, Material.BELLY, -w / 2.0F, -w / 2.0F, 0.0F, w, w, 7.0F);
                box(rig, seg, Material.VEIN, -w / 2.0F - 0.3F, -w / 2.0F - 0.3F, 6.0F, w + 0.6F, w + 0.6F, 1.0F);
                parent = seg;
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sway(body, pose, "seg", SEGMENTS, 10.0F, 0.2F + 0.5F * pose.stride(), 0.6F);
            Gaits.ripple(body, pose, "seg", SEGMENTS, 4.0F, 0.15F, 0.8F);
            Gaits.look(body, pose, 0.5F, 0.4F);
            float maw = pose.key("maw", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, -3.0F * Math.max(0.0F, -maw), -6.0F * Math.max(0.0F, maw));
            float crush = pose.key("crush", 0.0F, 0.0F, 0.55F, 1.0F, 0.66F, -0.4F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), -40.0F * Math.max(0.0F, crush) + 30.0F * Math.max(0.0F, -crush), 0.0F, 0.0F);
            Bones.turn(body.part("seg0"), -20.0F * Math.max(0.0F, crush), 0.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, -8.0F * Math.max(0.0F, crush), 0.0F);
            float lunge = pose.key("lunge", 0.0F, 0.0F, 0.15F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), 10.0F * lunge, 0.0F, 0.0F);
            Gaits.fall(body, pose, 0.0F, 120.0F);
        }
    }
}
