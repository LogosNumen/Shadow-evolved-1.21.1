package net.logosnumen.nightmareevolved.client.creature.shore;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The Forgotten Shore's creatures that walk upright: the Black Knight, the stone saints, the coral golems, the blood fiends. */
public final class ShoreFolk {
    private ShoreFolk() {
    }

    /** Swings a one-handed weapon arm through the moves knights share: an overhead cleave, a thrust, a wide sweep. */
    static void swordwork(Rig.Baked body, Pose pose) {
        // Cleave: the blade raised high, then brought down.
        Bones.turn(body.part("arm_r"), pose.key("cleave", 0.0F, 0.0F, 0.45F, -150.0F, 0.58F, -35.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("chest"), pose.key("cleave", 0.0F, 0.0F, 0.45F, -8.0F, 0.58F, 14.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        // Cut: the same, quicker.
        Bones.turn(body.part("arm_r"), pose.key("cut", 0.0F, 0.0F, 0.4F, -140.0F, 0.56F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        // Thrust: drawn back, then driven forward.
        Bones.turn(body.part("arm_r"), pose.key("thrust", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("chest"), pose.key("thrust", 0.0F, 0.0F, 0.4F, -5.0F, 0.6F, 10.0F, 1.0F, 0.0F),
            pose.key("thrust", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -20.0F, 1.0F, 0.0F), 0.0F);
        // Sweep: across from right to left.
        Bones.turn(body.part("arm_r"), pose.key("sweep", 0.0F, 0.0F, 0.3F, -80.0F, 0.7F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("chest"), 0.0F, pose.key("sweep", 0.0F, 0.0F, 0.4F, -50.0F, 0.6F, 50.0F, 1.0F, 0.0F), 0.0F);
    }

    /** The Forsaken Knight: anthracite gothic plate engraved all over, a horned helm, red flames in its visor, a black sword. */
    public static final class Knight extends CreatureModel {
        private static final Look LOOK = new Look(0x2E2E34, 0x0C0C10, 0x5A5A66, 0x1A1A20, 0x24242A, 0xFF2A1A, 0x34343C);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 38.0F, 11.0F, Material.PLATE, Material.PLATE, Material.PLATE);
            // The engraved breastplate and faulds, and heavy pauldrons.
            box(rig, "chest", Material.RUNE, -5.0F, -h.chest * 0.62F, -3.4F, 10.0F, h.chest * 0.5F, 1.0F);
            both(rig, "chest", Material.PLATE, -8.5F, -h.chest * 0.72F, -3.5F, 4.0F, 4.0F, 7.0F);
            box(rig, "body", Material.RUNE, -4.5F, -2.0F, -3.0F, 9.0F, 5.0F, 1.0F);
            both(rig, "body", Material.PLATE, -5.0F, -1.0F, -2.8F, 3.0F, 6.0F, 5.6F);
            // The helm: a narrow visor slit with two red flames in it, and curved horns that may once have been wings.
            box(rig, "head", Material.VOID, -2.4F, -3.6F, -2.9F, 4.8F, 1.0F, 1.0F);
            both(rig, "head", Material.EYE, -1.8F, -3.6F, -3.0F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < 4; i++) {
                both(rig, "head", Material.HORN, -3.4F - i * 1.2F, -5.0F - i * 2.2F, -0.5F + i * 1.4F, 1.6F, 2.6F, 1.6F);
            }

            // The black sword, held low and forward.
            rig.bone("sword", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            rig.box("sword", Material.PLATE, -1, -1, -1, 2, 2, 3);
            rig.box("sword", Material.HORN, -2.5F, -1.5F, -2, 5, 1, 1);
            rig.box("sword", Material.VOID, -0.5F, -0.5F, -22, 1, 1, 20);
            rig.box("sword", Material.PLATE, -1, -0.5F, -21, 2, 1, 19);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 28.0F, 0.6F);
            swordwork(body, pose);
            float step = pose.key("shadow_step", 0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), 25.0F * step, 0.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, 6.0F * step, 0.0F);
            Humanoid.die(body, pose, 17.5F);
        }
    }

    /** A living statue of a stone warrior, with a sword and a tall shield, and ruby eyes. */
    public static final class Saint extends CreatureModel {
        private static final Look LOOK = new Look(0x8A8680, 0x4A4640, 0xB8B4AC, 0x6A6660, 0xA8A49C, 0xD02030, 0x9A968E);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 34.0F, 10.0F, Material.STONE, Material.STONE, Material.STONE);
            box(rig, "chest", Material.PLATE, -4.5F, -h.chest * 0.6F, -3.0F, 9.0F, h.chest * 0.45F, 1.0F);
            both(rig, "chest", Material.STONE, -7.5F, -h.chest * 0.7F, -3.0F, 3.0F, 3.0F, 6.0F);
            both(rig, "head", Material.EYE, -1.6F, -3.2F, -2.6F, 1.0F, 1.0F, 1.0F);
            box(rig, "head", Material.STONE, -2.6F, -5.6F, -2.6F, 5.2F, 1.0F, 5.2F);
            rig.bone("sword", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            rig.box("sword", Material.STONE, -1, -1, -1, 2, 2, 3);
            rig.box("sword", Material.STONE, -2, -1.5F, -2, 4, 1, 1);
            rig.box("sword", Material.PLATE, -0.5F, -0.5F, -17, 1, 1, 15);
            rig.bone("shield", "hand_l", 0.0F, -2.0F, -2.0F);
            rig.box("shield", Material.STONE, -1.5F, -9, -6, 2, 16, 11);
            rig.box("shield", Material.RUNE, -2, -5, -2.5F, 1, 8, 4);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 26.0F, 0.6F);
            Bones.turn(body.part("arm_l"), -30.0F, 0.0F, -10.0F);
            swordwork(body, pose);
            float bash = pose.key("bash", 0.0F, 0.0F, 0.4F, -0.4F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("arm_l"), -60.0F * bash, 20.0F * bash, 0.0F);
            Humanoid.die(body, pose, 15.6F);
        }
    }

    /** A golem of crimson coral in the shape of one of the people given to the Spire's sun, staring at nothing. */
    public static final class Golem extends CreatureModel {
        private static final Look LOOK = new Look(0xB02830, 0x4A0810, 0xE0505A, 0x8A1A22, 0xD04048, 0x200408, 0xC03038);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 42.0F, 12.0F, Material.STONE, Material.STONE, Material.STONE);
            both(rig, "head", Material.VOID, -2.4F, -4.2F, -3.2F, 1.4F, 1.4F, 1.0F);
            // Branches of coral growing out of it.
            for (int i = 0; i < 3; i++) {
                both(rig, "chest", Material.HORN, -6.5F - i, -h.chest * (0.3F + i * 0.18F), -1.0F + i, 1.5F, 3.5F + i, 1.5F);
            }

            box(rig, "head", Material.HORN, 1.0F, -h.head - 3.0F, -1.0F, 1.5F, 4.0F, 1.5F);
            rig.pair("hand", Material.STONE, -2.5F, -1.0F, -2.5F, 5, 4, 5);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 18.0F, 0.45F);
            float slam = pose.key("slam", 0.0F, 0.0F, 0.5F, 1.0F, 0.62F, -0.3F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -170.0F * Math.max(0.0F, slam), 0.0F, 0.0F, -170.0F * Math.max(0.0F, slam), 0.0F, 0.0F);
            Bones.turn(body.part("chest"), 25.0F * Math.max(0.0F, -slam * 3.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 19.3F);
        }
    }

    /** A blood fiend: hunched and skeletal in a shroud browned with old blood, long clawed arms, a wide fanged mouth. */
    public static final class Fiend extends CreatureModel {
        private static final Look LOOK = new Look(0x6A4A36, 0x2A1A12, 0x9A7A60, 0x5A1010, 0xD8CCB8, 0xE8D8A0, 0x7A2A20);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 35.0F, 8.0F, Material.CLOTH, Material.BONE, Material.BONE);
            box(rig, "chest", Material.CLOTH, -5.0F, -h.chest * 0.6F, -2.6F, 10.0F, h.chest * 0.9F, 5.2F);
            box(rig, "body", Material.CLOTH, -4.5F, -2.0F, -2.5F, 9.0F, 12.0F, 5.0F);
            // The deformed head and its wide mouth of fangs.
            box(rig, "head", Material.BONE, -3.0F, -h.head * 0.9F, -3.5F, 6.0F, h.head * 0.6F, 2.0F);
            box(rig, "head", Material.VOID, -2.6F, -2.2F, -3.8F, 5.2F, 1.4F, 1.0F);
            for (int i = 0; i < 5; i++) {
                box(rig, "head", Material.BONE, -2.4F + i * 1.1F, -2.6F, -3.9F, 0.6F, 1.0F, 1.0F);
            }

            both(rig, "head", Material.EYE, -1.8F, -h.head * 0.75F, -3.6F, 1.0F, 1.0F, 1.0F);
            for (int c = 0; c < 3; c++) {
                rig.pair("hand", Material.CLAW, -1.5F + c, 0.0F, -0.5F, 1, 4, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 30.0F, 0.7F);
            Bones.turn(body.part("chest"), 28.0F, 0.0F, 0.0F);
            Bones.turn(body.part("head"), -24.0F, 0.0F, 0.0F);
            Gaits.pair(body, "arm", -18.0F, 0.0F, 6.0F, -18.0F, 0.0F, 6.0F);
            float claws = pose.key("claws", 0.0F, 0.0F, 0.4F, 1.0F, 0.6F, -0.3F, 1.0F, 0.0F);
            Bones.turn(body.part("arm_r"), -120.0F * Math.max(0.0F, claws), 0.0F, 0.0F);
            float lunge = pose.key("lunge", 0.0F, 0.0F, 0.3F, -0.5F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -100.0F * lunge, 0.0F, 0.0F, -100.0F * lunge, 0.0F, 0.0F);
            Bones.turn(body.part("chest"), 20.0F * lunge, 0.0F, 0.0F);
            float bite = pose.key("bite", 0.0F, 0.0F, 0.4F, -0.3F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("chest"), 25.0F * bite, 0.0F, 0.0F);
            Bones.turn(body.part("head"), 20.0F * bite, 0.0F, 0.0F);
            Humanoid.die(body, pose, 16.1F);
        }
    }
}
