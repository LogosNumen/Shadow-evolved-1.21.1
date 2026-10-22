package net.logosnumen.nightmareevolved.client.creature.shore;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/**
 * The carapace creatures of the Forgotten Shore. Scavengers and centurions share one build, a demonic crab with a
 * centaur's torso: a shell on four pairs of scythe-tipped legs, an armoured torso rising from its front with the head set
 * straight on it, slit eyes, slimy mandibles, and pincers (or, for centurions, bone scythes). The Carapace Demon is the
 * same shape grown huge and cast in polished steel, on legs like pillars, with four arms and a horned, nearly human face.
 */
public final class CarapaceModels {
    private CarapaceModels() {
    }

    /** The scavengers' and centurions' build, at a scale (a scavenger's is 1), with pincers or bone scythes, bare or spiked. */
    abstract static class Crab extends CreatureModel {
        private final float s;
        private final boolean scythes;
        private final boolean spikes;

        Crab(float scale, boolean scythes, boolean spikes) {
            this.s = scale;
            this.scythes = scythes;
            this.spikes = spikes;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            float s = this.s;
            boolean scythes = this.scythes;
            boolean spikes = this.spikes;
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -18.0F * s, 2.0F * s);
            box(rig, "body", Material.SCALE, -9.0F * s, -4.0F * s, -8.0F * s, 18.0F * s, 8.0F * s, 18.0F * s);
            box(rig, "body", Material.PLATE, -8.0F * s, -6.0F * s, -7.0F * s, 16.0F * s, 2.0F * s, 16.0F * s);
            box(rig, "body", Material.BELLY, -7.0F * s, 4.0F * s, -6.0F * s, 14.0F * s, 2.0F * s, 14.0F * s);
            box(rig, "body", Material.PLATE, -6.0F * s, -5.0F * s, 10.0F * s, 12.0F * s, 6.0F * s, 3.0F * s);
            if (spikes) {
                for (int j = 0; j < 4; j++) {
                    both(rig, "body", Material.HORN, -8.0F * s, -7.5F * s - (j % 2) * 1.5F * s, (-6.0F + j * 5.0F) * s, 1.5F * s, 3.0F * s, 1.5F * s);
                    box(rig, "body", Material.HORN, -0.75F * s, -8.5F * s - (j % 2) * 1.5F * s, (-5.0F + j * 5.0F) * s, 1.5F * s, 3.0F * s, 1.5F * s);
                }
            }

            for (int i = 0; i < 4; i++) {
                float z = (-6.0F + i * 5.0F) * s;
                rig.pairBone("leg" + i, "body", -8.5F * s, 1.0F * s, z, 0.0F, -30.0F + i * 20.0F, 40.0F);
                rig.pair("leg" + i, Material.SCALE, -12.0F * s, -1.5F * s, -1.5F * s, n(12.0F * s), n(3.0F * s), n(3.0F * s));
                rig.pairBone("shin" + i, "leg" + i, -12.0F * s, 0.0F, 0.0F, 0.0F, 0.0F, -115.0F);
                rig.pair("shin" + i, Material.SCALE, -17.0F * s, -1.0F * s, -1.0F * s, n(17.0F * s), n(2.0F * s), n(2.0F * s));
                rig.pair("shin" + i, Material.CLAW, -23.0F * s, -0.5F * s, -0.5F * s, n(6.0F * s), 1, 1);
            }

            rig.bone("torso", "body", 0.0F, -3.0F * s, -8.0F * s);
            box(rig, "torso", Material.SCALE, -5.0F * s, -14.0F * s, -3.5F * s, 10.0F * s, 14.0F * s, 7.0F * s);
            box(rig, "torso", Material.PLATE, -4.5F * s, -13.0F * s, -4.0F * s, 9.0F * s, 7.0F * s, 1.0F);
            box(rig, "torso", Material.PLATE, -5.5F * s, -14.5F * s, -2.0F * s, 11.0F * s, 3.0F * s, 5.0F * s);
            if (spikes) {
                both(rig, "torso", Material.HORN, -6.0F * s, -18.0F * s, -1.0F * s, 1.5F * s, 4.0F * s, 1.5F * s);
            }

            rig.bone("head", "torso", 0.0F, -14.0F * s, -0.5F * s);
            box(rig, "head", Material.SCALE, -3.5F * s, -6.0F * s, -4.0F * s, 7.0F * s, 6.0F * s, 7.0F * s);
            box(rig, "head", Material.PLATE, -3.0F * s, -6.8F * s, -3.5F * s, 6.0F * s, 1.0F, 6.0F * s);
            both(rig, "head", Material.EYE, -2.8F * s, -4.2F * s, -4.2F * s, 2.0F * s, 1.0F, 1.0F);
            both(rig, "head", Material.CLAW, -2.2F * s, -1.8F * s, -4.8F * s, 1.0F, 3.0F * s, 1.0F);
            box(rig, "head", Material.BELLY, -1.2F * s, -1.6F * s, -4.4F * s, 2.4F * s, 2.0F * s, 1.0F);

            rig.pairBone("arm", "torso", -5.5F * s, -12.0F * s, 0.0F, -20.0F, 0.0F, 15.0F);
            rig.pair("arm", Material.SCALE, -3.0F * s, -1.5F * s, -1.5F * s, n(3.0F * s), n(10.0F * s), n(3.0F * s));
            rig.pairBone("claw", "arm", -1.5F * s, 9.0F * s, 0.0F, 15.0F, 0.0F, 0.0F);
            if (scythes) {
                // Bone scythes in place of pincers, long and hooked.
                rig.pair("claw", Material.BONE, -1.0F * s, -1.0F * s, -14.0F * s, n(2.0F * s), n(2.0F * s), n(14.0F * s));
                rig.pair("claw", Material.BONE, -1.0F * s, -0.5F * s, -16.0F * s, n(2.0F * s), n(4.0F * s), n(3.0F * s));
                rig.pair("claw", Material.BONE, -0.5F * s, 2.5F * s, -15.0F * s, 1, n(2.0F * s), n(2.0F * s));
            } else {
                rig.pair("claw", Material.SCALE, -2.5F * s, -2.0F * s, -7.0F * s, n(4.0F * s), n(4.0F * s), n(7.0F * s));
                rig.pair("claw", Material.CLAW, -1.5F * s, 1.0F * s, -12.0F * s, n(3.0F * s), n(1.5F * s), n(5.0F * s));
                rig.pairBone("pincer", "claw", -0.5F * s, -2.0F * s, -7.0F * s, 0.0F, 0.0F, 0.0F);
                rig.pair("pincer", Material.CLAW, -1.5F * s, -1.0F, -6.0F * s, n(3.0F * s), n(2.0F * s), n(6.0F * s));
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            float s = this.s;
            Gaits.sideLegs(body, pose, 4, 16.0F, 14.0F, 0.8F / s);
            Gaits.look(body, pose, 0.6F, 0.5F);
            Gaits.breathe(body, pose, "torso", 0.3F, 0.1F);
            Bones.turn(body.part("torso"), pose.gait(0.8F / s, 0.5F) * 3.0F, 0.0F, 0.0F);
            float clack = Math.max(0.0F, pose.wave(0.21F, 0.0F));
            Gaits.pair(body, "pincer", -12.0F * clack, 0.0F, 0.0F, -12.0F * clack, 0.0F, 0.0F);
            Gaits.pair(body, "arm", pose.wave(0.07F, 0.0F) * 4.0F, 0.0F, 0.0F, pose.wave(0.07F, 1.3F) * 4.0F, 0.0F, 0.0F);

            // Snap: both arms thrust out and the pincers bite shut.
            float reach = pose.key("snap", 0.0F, 0.0F, 0.4F, 1.0F, 0.65F, 1.0F, 1.0F, 0.0F);
            float jaw = pose.key("snap", 0.0F, 0.0F, 0.35F, 1.0F, 0.55F, -0.2F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -55.0F * reach, 0.0F, -10.0F * reach, -55.0F * reach, 0.0F, -10.0F * reach);
            Gaits.pair(body, "pincer", -40.0F * jaw, 0.0F, 0.0F, -40.0F * jaw, 0.0F, 0.0F);
            Bones.turn(body.part("torso"), 14.0F * reach, 0.0F, 0.0F);

            // Stab: the front legs rear up, then drive their scythe tips down and forward.
            float rise = pose.key("stab", 0.0F, 0.0F, 0.5F, 1.0F, 0.62F, -0.4F, 1.0F, 0.0F);
            Gaits.pair(body, "leg0", 0.0F, -35.0F * rise, 45.0F * rise, 0.0F, -35.0F * rise, 45.0F * rise);
            Bones.turn(body.part("body"), -8.0F * Math.max(0.0F, rise), 0.0F, 0.0F);

            // Scythes: a cut from high on the right, then from the left.
            float right = pose.key("scythes", 0.0F, 0.0F, 0.35F, -1.0F, 0.5F, 0.8F, 0.6F, 0.8F, 1.0F, 0.0F);
            float left = pose.key("scythes", 0.0F, 0.0F, 0.45F, 0.0F, 0.6F, -1.0F, 0.75F, 0.8F, 1.0F, 0.0F);
            Bones.turn(body.part("arm_r"), -70.0F * Math.max(0.0F, -right) + 40.0F * Math.max(0.0F, right), 30.0F * right, 0.0F);
            Bones.turn(body.part("arm_l"), -70.0F * Math.max(0.0F, -left) + 40.0F * Math.max(0.0F, left), -30.0F * left, 0.0F);

            // Thrust: both scythes driven straight forward.
            float thrust = pose.key("thrust", 0.0F, 0.0F, 0.45F, -0.4F, 0.62F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -70.0F * thrust, 0.0F, 0.0F, -70.0F * thrust, 0.0F, 0.0F);
            Bones.turn(body.part("torso"), 18.0F * thrust, 0.0F, 0.0F);

            // Rally: rearing back with the arms spread.
            float rally = pose.key("rally", 0.0F, 0.0F, 0.4F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("torso"), -22.0F * rally, 0.0F, 0.0F);
            Bones.turn(body.part("head"), -25.0F * rally, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -40.0F * rally, 0.0F, 35.0F * rally, -40.0F * rally, 0.0F, 35.0F * rally);

            Gaits.fall(body, pose, 10.0F * s, 0.0F);
            float f = pose.fall(0.0F, 1.0F);
            for (int i = 0; i < 4; i++) {
                Gaits.pair(body, "leg" + i, 0.0F, 0.0F, -35.0F * f, 0.0F, 0.0F, -35.0F * f);
            }

            Bones.turn(body.part("torso"), 40.0F * f, 0.0F, 0.0F);
        }
    }

    public static final class Scavenger extends Crab {
        private static final Look[] LOOKS = {
            new Look(0x6A2A22, 0x2A0E0C, 0x9A4A3A, 0xB89A80, 0xD8C8B0, 0xFFC040, 0x7A3A2E),
            new Look(0x5A4A3A, 0x221A14, 0x8A7A64, 0xB0A088, 0xD8CCB4, 0xFFC040, 0x6A5A46)
        };

        public Scavenger() {
            super(1.0F, false, false);
        }

        @Override
        public Look look(int look) {
            return LOOKS[Math.floorMod(look, LOOKS.length)];
        }
    }

    public static final class Centurion extends Crab {
        private static final Look LOOK = new Look(0x1A1214, 0x080506, 0x3A2A2C, 0x5A1A1E, 0xE0D6C2, 0xFF3030, 0x8A1A22);

        public Centurion() {
            super(1.2F, true, true);
        }

        @Override
        public Look look(int look) {
            return LOOK;
        }
    }

    /** Built at half size and drawn at twice that: six metres of steel. */
    public static final class Demon extends CreatureModel {
        private static final Look LOOK = new Look(0x8A8E98, 0x2E3038, 0xDCE0EA, 0x4A4C56, 0xC8CCD8, 0xFF6A2A, 0xB8BCC8);

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
            rig.bone("body", "root", 0.0F, -26.0F, 3.0F);
            rig.box("body", Material.PLATE, -11, -5, -11, 22, 10, 24);
            rig.box("body", Material.PLATE, -9, -8, -9, 18, 3, 20);
            rig.box("body", Material.SCALE, -10, 5, -10, 20, 2, 22);
            rig.box("body", Material.PLATE, -8, -6, 13, 16, 8, 4);
            for (int j = 0; j < 5; j++) {
                float z = -8.0F + j * 5.0F;
                both(rig, "body", Material.HORN, -10.5F, -10.0F - (j % 2) * 2.0F, z, 1.5F, 5.0F, 1.5F);
                both(rig, "body", Material.HORN, -5.0F, -11.5F - (j % 2), z + 2.0F, 1.0F, 4.0F, 1.0F);
            }

            for (int i = 0; i < 4; i++) {
                float z = -7.0F + i * 6.0F;
                rig.pairBone("leg" + i, "body", -10.0F, 2.0F, z, 0.0F, -24.0F + i * 16.0F, 20.0F);
                rig.pair("leg" + i, Material.PLATE, -8, -3, -3, 8, 6, 6);
                rig.pairBone("shin" + i, "leg" + i, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, -100.0F);
                rig.pair("shin" + i, Material.SCALE, -24, -2.5F, -2.5F, 24, 5, 5);
                rig.pair("shin" + i, Material.PLATE, -10, -3.5F, -3.5F, 7, 7, 7);
                rig.pair("shin" + i, Material.CLAW, -27, -2, -2, 3, 4, 4);
                rig.pair("shin" + i, Material.HORN, -4, -3.5F, -0.5F, 2, 2, 1);
            }

            rig.bone("torso", "body", 0.0F, -5.0F, -10.0F);
            rig.box("torso", Material.PLATE, -7, -18, -5, 14, 18, 10);
            rig.box("torso", Material.SCALE, -6, -17, -5.5F, 12, 10, 1);
            rig.box("torso", Material.PLATE, -9, -19, -3, 18, 4, 7);
            both(rig, "torso", Material.HORN, -9.5F, -24.0F, -1.0F, 2.0F, 6.0F, 2.0F);

            rig.bone("head", "torso", 0.0F, -19.0F, -1.0F);
            rig.box("head", Material.PLATE, -4, -8, -4.5F, 8, 8, 9);
            rig.box("head", Material.BONE, -3, -6, -5, 6, 5, 1);
            both(rig, "head", Material.EYE, -2.5F, -4.6F, -5.3F, 2.0F, 1.0F, 1.0F);
            rig.box("head", Material.VOID, -1.5F, -2.0F, -5.3F, 3, 1, 1);
            for (int h = 0; h < 3; h++) {
                both(rig, "head", Material.HORN, -4.0F + h * 1.2F, -12.0F + h * 1.5F, -2.0F + h * 2.0F, 1.5F, 5.0F - h, 1.5F);
            }

            // The upper arms end in pincers, the lower ones in scythes.
            rig.pairBone("arm", "torso", -7.5F, -16.0F, 0.0F, -25.0F, 0.0F, 20.0F);
            rig.pair("arm", Material.PLATE, -3, -2, -2, 4, 11, 4);
            rig.pairBone("claw", "arm", -1.0F, 10.0F, 0.0F, 10.0F, 0.0F, 0.0F);
            rig.pair("claw", Material.PLATE, -3, -2.5F, -9, 5, 5, 9);
            rig.pair("claw", Material.CLAW, -2, 1.5F, -15, 3, 2, 6);
            rig.pairBone("pincer", "claw", -0.5F, -2.5F, -9.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("pincer", Material.CLAW, -2, -1, -7, 3, 2, 7);
            rig.pairBone("arm2", "torso", -7.0F, -8.0F, 0.0F, -10.0F, 0.0F, 30.0F);
            rig.pair("arm2", Material.PLATE, -2.5F, -1.5F, -1.5F, 3, 9, 3);
            rig.pairBone("scythe", "arm2", -1.0F, 8.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            rig.pair("scythe", Material.HORN, -0.5F, -1, -16, 1, 3, 16);
            rig.pair("scythe", Material.HORN, -0.5F, -1, -18, 1, 6, 3);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sideLegs(body, pose, 4, 9.0F, 10.0F, 0.45F);
            Gaits.look(body, pose, 0.5F, 0.4F);
            Gaits.breathe(body, pose, "torso", 0.4F, 0.06F);
            Bones.shift(body.part("body"), 0.0F, Math.abs(pose.gait(0.45F, 0.0F)) * -0.6F, 0.0F);
            Gaits.pair(body, "scythe", pose.wave(0.05F, 0.0F) * 5.0F, 0.0F, 0.0F, pose.wave(0.05F, 1.0F) * 5.0F, 0.0F, 0.0F);

            // Crush: the pincer arms reach out together and close on whatever they hold.
            float reach = pose.key("crush", 0.0F, 0.0F, 0.45F, 1.0F, 0.7F, 0.8F, 1.0F, 0.0F);
            float jaw = pose.key("crush", 0.0F, 0.0F, 0.4F, 1.0F, 0.58F, -0.2F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -60.0F * reach, 0.0F, -12.0F * reach, -60.0F * reach, 0.0F, -12.0F * reach);
            Gaits.pair(body, "pincer", -45.0F * jaw, 0.0F, 0.0F, -45.0F * jaw, 0.0F, 0.0F);

            // Reap: both scythe arms drawn back, then swept through in a wide circle.
            float reap = pose.key("reap", 0.0F, 0.0F, 0.4F, -1.0F, 0.55F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm2", -40.0F * Math.abs(reap), 70.0F * reap, -20.0F * Math.max(0.0F, -reap), -40.0F * Math.abs(reap), 70.0F * reap,
                -20.0F * Math.max(0.0F, -reap));
            Bones.turn(body.part("torso"), 0.0F, -15.0F * reap, 0.0F);

            // Stamp: rearing on the hind legs and bringing the front ones down.
            float stamp = pose.key("stamp", 0.0F, 0.0F, 0.5F, 1.0F, 0.6F, -0.3F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), -14.0F * Math.max(0.0F, stamp), 0.0F, 0.0F);
            Gaits.pair(body, "leg0", 0.0F, -20.0F * stamp, 35.0F * stamp, 0.0F, -20.0F * stamp, 35.0F * stamp);
            Gaits.pair(body, "leg1", 0.0F, -10.0F * stamp, 25.0F * stamp, 0.0F, -10.0F * stamp, 25.0F * stamp);

            // Command: rearing tall, arms raised, the head thrown back.
            float command = pose.key("command", 0.0F, 0.0F, 0.35F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("torso"), -20.0F * command, 0.0F, 0.0F);
            Bones.turn(body.part("head"), -30.0F * command, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -100.0F * command, 0.0F, 30.0F * command, -100.0F * command, 0.0F, 30.0F * command);

            Gaits.fall(body, pose, 12.0F, 0.0F);
            float f = pose.fall(0.0F, 1.0F);
            for (int i = 0; i < 4; i++) {
                Gaits.pair(body, "leg" + i, 0.0F, 0.0F, -30.0F * f, 0.0F, 0.0F, -30.0F * f);
            }

            Bones.turn(body.part("torso"), 50.0F * f, 0.0F, 0.0F);
        }
    }
}
