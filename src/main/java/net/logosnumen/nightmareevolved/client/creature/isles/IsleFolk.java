package net.logosnumen.nightmareevolved.client.creature.isles;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The Chained Isles' upright and winged creatures: chain worms, chalice maidens, the Mirror Beast, sailor dolls, gargoyles. */
public final class IsleFolk {
    private IsleFolk() {
    }

    /** A chain worm: a snake's body under a human-like torso with two strong arms, a long toothy muzzle, leathery wings. */
    public static final class ChainWorm extends CreatureModel {
        private static final Look LOOK = new Look(0x4A3A30, 0x1A140F, 0x7A6450, 0x8A7A64, 0xD8CCB8, 0xFFC040, 0x5A4A3E);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -16.0F, 0.0F);
            rig.box("body", Material.SCALE, -4, -12, -2.5F, 8, 12, 5);
            rig.box("body", Material.BELLY, -3, -11, -3, 6, 10, 1);
            rig.bone("head", "body", 0.0F, -12.0F, 0.0F);
            rig.box("head", Material.SCALE, -2.5F, -4, -3, 5, 4, 5);
            rig.box("head", Material.SCALE, -1.5F, -3, -10, 3, 3, 7);
            both(rig, "head", Material.EYE, -2.6F, -3.0F, -2.6F, 1.0F, 1.0F, 1.0F);
            rig.bone("jaw", "head", 0.0F, -0.5F, -3.0F);
            rig.box("jaw", Material.SCALE, -1.4F, 0, -7, 3, 1, 7);
            for (int t = 0; t < 5; t++) {
                rig.box("jaw", Material.BONE, -1.2F + (t % 2) * 1.6F, -0.8F, -6.5F + t * 1.2F, 1, 1, 1);
            }

            rig.pairBone("arm", "body", -5.0F, -11.0F, 0.0F, -20.0F, 0.0F, 10.0F);
            rig.pair("arm", Material.SCALE, -1.5F, -1, -1.5F, 3, 9, 3);
            rig.pairBone("hand", "arm", 0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("hand", Material.CLAW, -1.5F, 0, -1.5F, 3, 3, 3);
            rig.pairBone("wing", "body", -3.0F, -10.0F, 1.5F, 0.0F, 0.0F, -10.0F);
            rig.pair("wing", Material.MEMBRANE, -18, -0.5F, 0, 18, 1, 10);
            rig.pairBone("tip", "wing", -18.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("tip", Material.MEMBRANE, -12, -0.5F, 0, 12, 1, 8);
            String parent = "body";
            for (int i = 0; i < 6; i++) {
                String seg = "seg" + i;
                rig.bone(seg, parent, 0.0F, i == 0 ? 0.0F : 0.0F, i == 0 ? 1.0F : 6.5F, i == 0 ? 40.0F : 8.0F, 0.0F, 0.0F);
                float w = 5.0F - i * 0.6F;
                box(rig, seg, Material.SCALE, -w / 2.0F, -w / 2.0F, 0.0F, w, w, 7.0F);
                parent = seg;
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.wings(body, pose, 30.0F, 0.35F, 0.0F);
            Gaits.look(body, pose, 0.7F, 0.6F);
            Gaits.ripple(body, pose, "seg", 6, 8.0F, 0.25F, 0.7F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.35F, 1.2F) * 1.2F, 0.0F);
            Gaits.pair(body, "arm", pose.wave(0.2F, 0.0F) * 8.0F, 0.0F, 0.0F, pose.wave(0.2F, 1.4F) * 8.0F, 0.0F, 0.0F);
            float claws = pose.key("claws", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -90.0F * Math.max(0.0F, -claws) + 30.0F * Math.max(0.0F, claws), 0.0F, 0.0F,
                -90.0F * Math.max(0.0F, -claws) + 30.0F * Math.max(0.0F, claws), 0.0F, 0.0F);
            float bite = pose.key("bite", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), 25.0F * bite, 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), 40.0F * Math.max(0.0F, -bite), 0.0F, 0.0F);
            float coil = pose.key("coil", 0.0F, 0.0F, 0.5F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            for (int i = 1; i < 6; i++) {
                Bones.turn(body.part("seg" + i), -40.0F * coil, 0.0F, 0.0F);
            }

            Gaits.fall(body, pose, 14.0F, 80.0F);
        }
    }

    /** A chalice maiden: a spectre of eerie beauty in a long, drifting robe, pale and bright-eyed, her hair a dark mist. */
    public static final class Maiden extends CreatureModel {
        private static final Look LOOK = new Look(0xD8E0F0, 0x5A6A8A, 0xF0F4FF, 0xE8E0E0, 0x2A1A2A, 0x80FFE0, 0xB0C0E0);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 35.0F, 8.0F, Material.CLOTH, Material.BELLY, Material.BELLY);
            box(rig, "body", Material.CLOTH, -5.0F, -2.0F, -3.5F, 10.0F, h.hips + 2.0F, 7.0F);
            box(rig, "head", Material.SMOKE, -3.0F, -h.head - 1.0F, -1.0F, 6.0F, h.head + 6.0F, 4.0F);
            both(rig, "head", Material.EYE, -1.5F, -h.head * 0.6F, -2.6F, 1.0F, 1.0F, 1.0F);
            rig.box("head", Material.GLOW, -2, -(int)h.head - 2, -2, 4, 1, 4);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.size(body.part("leg0_r"), 0.01F);
            Bones.size(body.part("leg0_l"), 0.01F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.1F, 0.0F) * 1.2F, 0.0F);
            Bones.turn(body.part("body"), pose.wave(0.07F, 0.5F) * 3.0F, 0.0F, pose.wave(0.05F, 1.0F) * 3.0F);
            Gaits.look(body, pose, 0.8F, 0.6F);
            Gaits.pair(body, "arm", -20.0F + pose.wave(0.08F, 0.0F) * 8.0F, 0.0F, 12.0F, -20.0F + pose.wave(0.08F, 1.5F) * 8.0F, 0.0F, 12.0F);
            float wail = pose.key("wail", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), -30.0F * wail, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -40.0F * wail, 0.0F, 70.0F * wail, -40.0F * wail, 0.0F, 70.0F * wail);
            float embrace = pose.key("embrace", 0.0F, 0.0F, 0.5F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -90.0F * embrace, -25.0F * embrace, 0.0F, -90.0F * embrace, -25.0F * embrace, 0.0F);
            Bones.size(body.part("body"), 1.0F - 0.9F * pose.fall(0.0F, 1.0F));
        }
    }

    /** The Mirror Beast as it is before it takes anyone's shape: a figure of mirror-bright metal with a blank face. */
    public static final class Mirror extends CreatureModel {
        private static final Look LOOK = new Look(0xC8D0E0, 0x4A5060, 0xF0F4FF, 0x8A90A0, 0xE0E8F8, 0xA0E8FF, 0xD0D8E8);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid.build(rig, 30.0F, 8.0F, Material.PLATE, Material.PLATE, Material.CRYSTAL);
            rig.box("chest", Material.CRYSTAL, -3, -8, -2.6F, 6, 6, 1);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 30.0F, 0.7F);
            Bones.turn(body.part("arm_r"), pose.key("cut", 0.0F, 0.0F, 0.4F, -130.0F, 0.56F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("thrust", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.pair(body, "arm", pose.key("lunge", 0.0F, 0.0F, 0.5F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("lunge", 0.0F, 0.0F, 0.5F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 13.8F);
        }
    }

    /** A sailor doll: a tall, menacing figure put together out of splintered wood, its hands long jagged blades. */
    public static final class Doll extends CreatureModel {
        private static final Look LOOK = new Look(0x6A4A30, 0x2A1A10, 0x9A7A58, 0x5A3E28, 0x8A8A90, 0x101010, 0xA0A4AC);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 37.0F, 9.0F, Material.BRICK, Material.BRICK, Material.BRICK);
            // Splinters standing out of it.
            both(rig, "chest", Material.BRICK, -6.0F, -h.chest * 0.5F, -1.0F, 2.0F, 1.0F, 1.0F);
            box(rig, "chest", Material.BRICK, 1.0F, -h.chest * 0.8F, -3.5F, 1.0F, 2.0F, 1.0F);
            box(rig, "head", Material.VOID, -2.0F, -h.head * 0.6F, -2.6F, 4.0F, 1.0F, 1.0F);
            rig.pair("hand", Material.PLATE, -0.5F, 0, -1, 1, 12, 2);
            rig.pair("hand", Material.PLATE, -0.5F, 10, -2, 1, 3, 1);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 26.0F, 0.6F);
            Bones.turn(body.part("chest"), 0.0F, pose.wave(0.4F, 0.0F) * 3.0F, pose.wave(0.23F, 1.0F) * 2.0F);
            float blades = pose.key("blades", 0.0F, 0.0F, 0.35F, -1.0F, 0.5F, 1.0F, 0.65F, -1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("arm_r"), -100.0F * Math.max(0.0F, blades), 30.0F * blades, 0.0F);
            Bones.turn(body.part("arm_l"), -100.0F * Math.max(0.0F, -blades), 30.0F * blades, 0.0F);
            Gaits.pair(body, "arm", pose.key("stab", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("stab", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            float f = pose.fall(0.0F, 1.0F);
            Humanoid.die(body, pose, 17.0F);
            Bones.size(body.part("head"), 1.0F - 0.5F * f);
        }
    }

    /** A gargoyle: a hunched winged imp of grey stone with horns and claws. */
    public static final class Gargoyle extends CreatureModel {
        private static final Look LOOK = new Look(0x7A7670, 0x34302C, 0xA8A49C, 0x5A5650, 0x9A968E, 0xFFB040, 0x6A665E);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 22.0F, 7.0F, Material.STONE, Material.STONE, Material.STONE);
            both(rig, "head", Material.EYE, -1.4F, -h.head * 0.6F, -1.6F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.HORN, -2.2F, -h.head - 2.0F, -0.5F, 1.0F, 3.0F, 1.0F);
            rig.pair("hand", Material.CLAW, -1, 0, -1, 2, 2, 2);
            rig.pairBone("wing", "chest", -2.0F, -h.chest * 0.6F, 1.5F, 0.0F, -20.0F, -10.0F);
            rig.pair("wing", Material.MEMBRANE, -14, -0.5F, 0, 14, 1, 8);
            rig.pairBone("tip", "wing", -14.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("tip", Material.MEMBRANE, -9, -0.5F, 0, 9, 1, 6);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.wings(body, pose, 35.0F, 0.45F, 0.0F);
            Gaits.look(body, pose, 0.7F, 0.6F);
            Bones.turn(body.part("chest"), 25.0F, 0.0F, 0.0F);
            Gaits.pair(body, "leg0", 40.0F, 0.0F, 0.0F, 40.0F, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -30.0F + pose.wave(0.3F, 0.0F) * 10.0F, 0.0F, 0.0F, -30.0F + pose.wave(0.3F, 1.0F) * 10.0F, 0.0F, 0.0F);
            float dive = pose.key("dive", 0.0F, 0.0F, 0.6F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -80.0F * dive, 0.0F, 0.0F, -80.0F * dive, 0.0F, 0.0F);
            Gaits.pair(body, "wing", 0.0F, 50.0F * dive, 0.0F, 0.0F, 50.0F * dive, 0.0F);
            float claws = pose.key("claws", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("arm_r"), -110.0F * Math.max(0.0F, -claws) + 20.0F * Math.max(0.0F, claws), 0.0F, 0.0F);
            Gaits.fall(body, pose, 8.0F, 80.0F);
        }
    }
}
