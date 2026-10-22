package net.logosnumen.nightmareevolved.client.creature.desert;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The creatures of the Nightmare Desert: the Deathless and their giants, and the Skinwalker's vessels. */
public final class DesertModels {
    private DesertModels() {
    }

    /** Lying in pieces on the sand, waiting to rise. */
    static void broken(Rig.Baked body, Pose pose, float hips) {
        if (!pose.enraged()) {
            return;
        }

        Bones.turn(body.part("body"), 88.0F, 0.0F, 12.0F);
        Bones.shift(body.part("body"), 0.0F, hips * 0.9F, -hips * 0.2F);
        Gaits.pair(body, "arm", -40.0F, 30.0F, 60.0F, -60.0F, 10.0F, 40.0F);
        Gaits.pair(body, "leg0", 20.0F, 0.0F, 30.0F, -10.0F, 0.0F, 20.0F);
        Bones.shift(body.part("head"), 3.0F, 2.0F, 2.0F);
    }

    /**
     * A Deathless warrior: a black skeleton in rusted armour, sand running from its bones, darkness kindled in its
     * empty eyes, holding a sword, a war hammer or a spear.
     */
    public static final class Warrior extends CreatureModel {
        private static final Look LOOK = new Look(0x1A1A1E, 0x050506, 0x3A3A42, 0x2A2A30, 0x2A2A30, 0x6A3AFF, 0x7A4A2A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 32.0F, 8.0F, Material.BONE, Material.BONE, Material.BONE);
            box(rig, "chest", Material.PLATE, -4.5F, -h.chest * 0.65F, -2.6F, 9.0F, h.chest * 0.5F, 5.2F);
            box(rig, "body", Material.PLATE, -4.0F, -2.0F, -2.4F, 8.0F, 4.0F, 4.8F);
            box(rig, "head", Material.PLATE, -2.6F, -h.head - 1.0F, -2.6F, 5.2F, 2.0F, 5.2F);
            both(rig, "head", Material.EYE, -1.6F, -h.head * 0.6F, -2.5F, 1.0F, 1.0F, 1.0F);
            rig.bone("weapon", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            switch (Math.floorMod(look, 3)) {
                case 0 -> {
                    rig.box("weapon", Material.PLATE, -0.5F, -0.5F, -17, 1, 1, 15);
                    rig.box("weapon", Material.PLATE, -2, -1, -2, 4, 1, 1);
                }
                case 1 -> {
                    rig.box("weapon", Material.CLOTH, -0.5F, -0.5F, -14, 1, 1, 16);
                    rig.box("weapon", Material.PLATE, -2.5F, -2.5F, -18, 5, 5, 5);
                }
                default -> {
                    rig.box("weapon", Material.CLOTH, -0.5F, -0.5F, -26, 1, 1, 30);
                    rig.box("weapon", Material.PLATE, -1, -1, -30, 2, 2, 4);
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 26.0F, 0.6F);
            Bones.turn(body.part("arm_r"), pose.key("cut", 0.0F, 0.0F, 0.4F, -140.0F, 0.56F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("thrust", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.pair(body, "arm", pose.key("volley", 0.0F, 0.0F, 0.5F, -100.0F, 0.7F, -60.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("volley", 0.0F, 0.0F, 0.5F, -100.0F, 0.7F, -60.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            broken(body, pose, 14.7F);
            Humanoid.die(body, pose, 14.7F);
        }
    }

    /** A Deathless giant: the skeleton of something far larger than a person, black bone like a monument. Built small. */
    public static final class Giant extends CreatureModel {
        private static final Look LOOK = new Look(0x101012, 0x020203, 0x2E2E36, 0x222228, 0x26262C, 0x6A3AFF, 0x6A4A2A);

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
            Humanoid h = Humanoid.build(rig, 32.0F, 10.0F, Material.BONE, Material.BONE, Material.BONE);
            for (int r = 0; r < 4; r++) {
                box(rig, "chest", Material.BONE, -5.0F, -h.chest * 0.6F + r * 2.2F, -2.8F, 10.0F, 1.0F, 1.0F);
            }

            both(rig, "head", Material.EYE, -1.8F, -h.head * 0.6F, -2.5F, 1.0F, 1.0F, 1.0F);
            both(rig, "head", Material.HORN, -3.0F, -h.head - 2.0F, -0.5F, 1.0F, 3.0F, 1.0F);
            rig.bone("weapon", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            rig.box("weapon", Material.CLOTH, -0.5F, -0.5F, -12, 1, 1, 14);
            rig.box("weapon", Material.PLATE, -1, -3, -15, 2, 6, 6);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 20.0F, 0.35F);
            Bones.turn(body.part("arm_r"), pose.key("smash", 0.0F, 0.0F, 0.55F, -160.0F, 0.66F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("chest"), 0.0F, pose.key("cleave", 0.0F, 0.0F, 0.4F, -45.0F, 0.6F, 45.0F, 1.0F, 0.0F), 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("cleave", 0.0F, 0.0F, 0.3F, -80.0F, 0.7F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            broken(body, pose, 14.7F);
            Humanoid.die(body, pose, 14.7F);
        }
    }

    /** A traveller, as a Skinwalker's vessel appears when no one who knows better is looking (usually drawn as a player). */
    public static final class Vessel extends CreatureModel {
        private static final Look LOOK = new Look(0x6A5A4A, 0x2A2018, 0x9A8A70, 0xC89A7A, 0x3A2A1A, 0x101010, 0x5A4A3A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid.build(rig, 29.0F, 8.0F, Material.CLOTH, Material.CLOTH, Material.BELLY);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 30.0F, 0.7F);
            Humanoid.die(body, pose, 13.3F);
        }
    }
}
