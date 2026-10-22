package net.logosnumen.nightmareevolved.client.creature.isles;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Quadruped;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The Chained Isles' beasts: black ravagers, dread wolves, griffins and giant ants. */
public final class IsleBeasts {
    private IsleBeasts() {
    }

    /** A black ravager: lean and black-furred on four mighty paws, a wide maw of fangs, two burning red eyes. */
    public static final class Ravager extends CreatureModel {
        private static final Look LOOK = new Look(0x141214, 0x040304, 0x2E2A30, 0x3A2A2A, 0xE0D8C8, 0xFF2A1A, 0x241E24);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 22.0F, 26.0F, 9.0F, 3, Material.FUR, Material.FUR);
            both(rig, "head", Material.EYE, -3.2F, -5.0F, -8.4F, 1.0F, 1.0F, 1.0F);
            for (int t = 0; t < 4; t++) {
                box(rig, "head", Material.BONE, -2.6F + t * 1.6F, -0.6F, -9.4F, 0.8F, 1.6F, 0.8F);
                box(rig, "jaw", Material.BONE, -2.2F + t * 1.6F, -1.2F, -8.0F, 0.8F, 1.2F, 0.8F);
            }

            both(rig, "head", Material.FUR, -4.6F, -6.5F, -3.0F, 1.5F, 3.0F, 1.5F);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 32.0F, 0.9F, 3);
            Quadruped.bite(body, pose, "bite");
            Quadruped.pounce(body, pose, "pounce");
            Gaits.fall(body, pose, 10.0F, 80.0F);
        }
    }

    /** A dread wolf: the size of a small truck, fur like steel wire, a long muzzle and jaws that grind armour to dust. */
    public static final class DreadWolf extends CreatureModel {
        private static final Look LOOK = new Look(0x5A5A60, 0x22222A, 0x8A8A94, 0x6A6A70, 0xD8D4CC, 0xE8C040, 0x4A4A52);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 36.0F, 44.0F, 16.0F, 4, Material.FUR, Material.FUR);
            box(rig, "body", Material.FUR, -9.0F, -18.0F, -18.0F, 18.0F, 6.0F, 12.0F);
            box(rig, "head", Material.FUR, -3.0F, -6.0F, -20.0F, 6.0F, 5.0F, 8.0F);
            box(rig, "jaw", Material.FUR, -2.6F, 0.0F, -18.0F, 5.2F, 2.5F, 7.0F);
            both(rig, "head", Material.EYE, -4.2F, -8.0F, -12.0F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.FUR, -6.0F, -13.0F, -6.0F, 2.0F, 4.0F, 2.0F);
            for (int t = 0; t < 5; t++) {
                box(rig, "head", Material.BONE, -2.4F + t * 1.2F, -1.2F, -20.2F, 0.8F, 1.6F, 0.8F);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Quadruped.trot(body, pose, 28.0F, 0.6F, 4);
            Quadruped.bite(body, pose, "jaws");
            Quadruped.pounce(body, pose, "pounce");
            float breath = pose.key("breath", 0.0F, 0.0F, 0.3F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("jaw"), 35.0F * breath, 0.0F, 0.0F);
            Bones.turn(body.part("head"), -15.0F * breath, 0.0F, 0.0F);
            Gaits.fall(body, pose, 16.0F, 80.0F);
        }
    }

    /** A griffin: the body of a lion, the head, talons and wings of an eagle, and something of pure nightmare in it. */
    public static final class Griffin extends CreatureModel {
        private static final Look LOOK = new Look(0xB08A4A, 0x4A3418, 0xD8B878, 0xE8E0D0, 0x3A2A1A, 0xFF4020, 0xC8A060);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Quadruped.build(rig, 28.0F, 34.0F, 12.0F, 3, Material.FUR, Material.FUR);
            box(rig, "body", Material.FEATHER, -6.5F, -13.0F, -14.0F, 13.0F, 10.0F, 10.0F);
            box(rig, "head", Material.BELLY, -4.0F, -8.0F, -8.0F, 8.0F, 7.0F, 8.0F);
            box(rig, "head", Material.CLAW, -1.5F, -5.0F, -13.0F, 3.0F, 3.0F, 5.0F);
            box(rig, "jaw", Material.CLAW, -1.2F, 0.0F, -9.0F, 2.4F, 1.5F, 5.0F);
            both(rig, "head", Material.EYE, -4.2F, -6.0F, -6.0F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("wing", "body", -6.0F, -11.0F, -6.0F, 0.0F, 0.0F, -15.0F);
            rig.pair("wing", Material.FEATHER, -24, -1, -3, 24, 2, 12);
            rig.pairBone("tip", "wing", -24.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("tip", Material.FEATHER, -18, -0.5F, -1, 18, 1, 14);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.wings(body, pose, 35.0F, 0.3F, -5.0F);
            Gaits.look(body, pose, 0.7F, 0.6F);
            Gaits.pair(body, "leg0", -30.0F, 0.0F, 0.0F, -30.0F, 0.0F, 0.0F);
            Gaits.pair(body, "leg1", 40.0F, 0.0F, 0.0F, 40.0F, 0.0F, 0.0F);
            Gaits.sway(body, pose, "tail", 3, 12.0F, 0.2F, 0.8F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.3F, 1.2F) * 1.5F, 0.0F);
            Quadruped.pounce(body, pose, "swoop");
            float talons = pose.key("talons", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "leg0", -80.0F * talons, 0.0F, 0.0F, -80.0F * talons, 0.0F, 0.0F);
            Quadruped.bite(body, pose, "beak");
            Gaits.fall(body, pose, 14.0F, 80.0F);
        }
    }

    /** A giant ant, the size of a dog, with great mandibles. */
    public static final class Ant extends CreatureModel {
        private static final Look LOOK = new Look(0x3A2418, 0x120804, 0x6A4A30, 0x2A1A10, 0x8A4A2A, 0xFFE0A0, 0x4A2E1C);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -7.0F, 0.0F);
            rig.box("body", Material.SCALE, -2.5F, -2.5F, -3, 5, 4, 6);
            rig.bone("abdomen", "body", 0.0F, -1.0F, 3.0F);
            rig.box("abdomen", Material.SCALE, -3.5F, -3.5F, 0, 7, 6, 8);
            rig.bone("head", "body", 0.0F, -1.0F, -3.0F);
            rig.box("head", Material.SCALE, -3, -3, -5, 6, 5, 5);
            both(rig, "head", Material.EYE, -3.2F, -2.0F, -3.5F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("fang", "head", -1.5F, 1.0F, -5.0F, 0.0F, 20.0F, 0.0F);
            rig.pair("fang", Material.CLAW, -0.5F, -0.5F, -4, 1, 1, 4);
            for (int i = 0; i < 3; i++) {
                rig.pairBone("leg" + i, "body", -2.5F, 1.0F, -2.0F + i * 2.0F, 0.0F, -30.0F + i * 30.0F, 30.0F);
                rig.pair("leg" + i, Material.SCALE, -6, -0.5F, -0.5F, 6, 1, 1);
                rig.pairBone("shin" + i, "leg" + i, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -90.0F);
                rig.pair("shin" + i, Material.SCALE, -7, -0.5F, -0.5F, 7, 1, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sideLegs(body, pose, 3, 25.0F, 15.0F, 1.6F);
            Gaits.look(body, pose, 0.5F, 0.4F);
            float bite = pose.key("mandibles", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "fang", 0.0F, 30.0F * bite, 0.0F, 0.0F, 30.0F * bite, 0.0F);
            Gaits.fall(body, pose, 3.0F, 180.0F);
        }
    }
}
