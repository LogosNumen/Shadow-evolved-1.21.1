package net.logosnumen.nightmareevolved.client.creature.mountains;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/** The creatures of the Hollow Mountains and the Underworld. */
public final class MountainModels {
    private MountainModels() {
    }

    /** Sword strokes shared by the jade saints and the stone shells. */
    static void blade(Rig.Baked body, Pose pose) {
        Bones.turn(body.part("arm_r"), pose.key("cut", 0.0F, 0.0F, 0.4F, -140.0F, 0.56F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("arm_r"), pose.key("strike", 0.0F, 0.0F, 0.45F, -150.0F, 0.6F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("arm_r"), pose.key("thrust", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("chest"), 0.0F, pose.key("sweep", 0.0F, 0.0F, 0.4F, -50.0F, 0.6F, 50.0F, 1.0F, 0.0F), 0.0F);
        Bones.turn(body.part("arm_r"), pose.key("sweep", 0.0F, 0.0F, 0.3F, -80.0F, 0.7F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
        Bones.turn(body.part("arm_l"), pose.key("bash", 0.0F, 0.0F, 0.4F, 20.0F, 0.6F, -70.0F, 1.0F, 0.0F), 0.0F, 0.0F);
    }

    static void sword(Rig rig, Material material, int length) {
        rig.bone("sword", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
        rig.box("sword", material, -1, -1, -1, 2, 2, 3);
        rig.box("sword", material, -2, -1.5F, -2, 4, 1, 1);
        rig.box("sword", material, -0.5F, -0.5F, -length - 2, 1, 1, length);
    }

    /** A being of the mist: a tall, faint shape of fog with long arms, seen only as a thickening of the white. */
    public static final class MistBeing extends CreatureModel {
        private static final Look LOOK = new Look(0xC8CCD0, 0x8A9098, 0xE8ECF0, 0xB0B4B8, 0xD0D4D8, 0xA0D8FF, 0xC0C4C8);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 42.0F, 9.0F, Material.SMOKE, Material.SMOKE, Material.SMOKE);
            box(rig, "body", Material.SMOKE, -6.0F, -2.0F, -4.0F, 12.0F, h.hips + 2.0F, 8.0F);
            box(rig, "head", Material.SMOKE, -5.0F, -h.head - 2.0F, -4.0F, 10.0F, h.head + 6.0F, 8.0F);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.size(body.part("leg0_r"), 0.01F);
            Bones.size(body.part("leg0_l"), 0.01F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.06F, 0.0F) * 2.0F, 0.0F);
            Bones.turn(body.part("body"), 0.0F, pose.wave(0.03F, 0.0F) * 10.0F, pose.wave(0.05F, 1.0F) * 4.0F);
            Gaits.pair(body, "arm", -15.0F + pose.wave(0.05F, 0.0F) * 12.0F, 0.0F, 15.0F, -15.0F + pose.wave(0.05F, 2.0F) * 12.0F, 0.0F, 15.0F);
            float unmake = pose.key("unmake", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -90.0F * unmake, 0.0F, 30.0F * unmake, -90.0F * unmake, 0.0F, 30.0F * unmake);
            Bones.turn(body.part("arm_r"), pose.key("touch", 0.0F, 0.0F, 0.5F, -90.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.size(body.part("body"), 1.0F - 0.9F * pose.fall(0.0F, 1.0F));
        }
    }

    /** A Saint of Tainted Jade: a stone saint's warrior shape gone to dark green jade veined with corruption. */
    public static final class TaintedSaint extends CreatureModel {
        private static final Look LOOK = new Look(0x2E7A4C, 0x0A2A14, 0x6AB880, 0x1E4A2E, 0x4A8A62, 0x4AA870, 0x2E6E48);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 38.0F, 11.0F, Material.CRYSTAL, Material.CRYSTAL, Material.CRYSTAL);
            box(rig, "chest", Material.VEIN, -5.0F, -h.chest * 0.6F, -3.4F, 10.0F, h.chest * 0.5F, 1.0F);
            both(rig, "chest", Material.CRYSTAL, -8.0F, -h.chest * 0.7F, -3.0F, 3.0F, 3.0F, 6.0F);
            both(rig, "head", Material.EYE, -1.8F, -h.head * 0.6F, -2.9F, 1.0F, 1.0F, 1.0F);
            sword(rig, Material.CRYSTAL, 16);
            rig.bone("shield", "hand_l", 0.0F, -2.0F, -2.0F);
            rig.box("shield", Material.CRYSTAL, -1.5F, -9, -6, 2, 16, 11);
            rig.box("shield", Material.VEIN, -2, -5, -2.5F, 1, 8, 4);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 26.0F, 0.6F);
            Bones.turn(body.part("arm_l"), -30.0F, 0.0F, -10.0F);
            blade(body, pose);
            Humanoid.die(body, pose, 17.5F);
        }
    }

    /** Absence: a Saint of Tainted Jade grown greater, a hollow of darkness where its face should be, darkness trailing from it. */
    public static final class Absence extends CreatureModel {
        private static final Look LOOK = new Look(0x245E3C, 0x041008, 0x4A9068, 0x0A0A0A, 0x3A7A52, 0x2A6A40, 0x1E4E32);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 42.0F, 11.0F, Material.CRYSTAL, Material.CRYSTAL, Material.CRYSTAL);
            box(rig, "head", Material.VOID, -h.head * 0.4F, -h.head * 0.9F, -h.head * 0.55F, h.head * 0.8F, h.head * 0.8F, 1.0F);
            box(rig, "chest", Material.SMOKE, -6.0F, -h.chest * 0.65F, 3.0F, 12.0F, h.chest + h.hips, 2.0F);
            sword(rig, Material.CRYSTAL, 20);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 26.0F, 0.6F);
            blade(body, pose);
            float v = pose.key("void", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -80.0F * v, 0.0F, 40.0F * v, -80.0F * v, 0.0F, 40.0F * v);
            Bones.turn(body.part("head"), -25.0F * v, 0.0F, 0.0F);
            Humanoid.die(body, pose, 19.3F);
        }
    }

    /** A dark one: the shell of a stone knight, cracked open, with soulless darkness pouring from every join. */
    public static final class DarkOne extends CreatureModel {
        private static final Look LOOK = new Look(0x4A4A50, 0x1A1A1E, 0x7A7A82, 0x050505, 0x6A6A70, 0x3A1A6A, 0x5A5A62);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 37.0F, 10.0F, Material.STONE, Material.STONE, Material.STONE);
            box(rig, "chest", Material.VOID, -2.0F, -h.chest * 0.55F, -3.2F, 4.0F, h.chest * 0.5F, 1.0F);
            box(rig, "head", Material.VOID, -2.0F, -h.head * 0.65F, -2.7F, 4.0F, 2.0F, 1.0F);
            both(rig, "chest", Material.SMOKE, -7.0F, -h.chest * 0.75F, -2.0F, 3.0F, 4.0F, 4.0F);
            box(rig, "body", Material.SMOKE, -4.0F, -3.0F, -3.0F, 8.0F, 6.0F, 6.0F);
            sword(rig, Material.STONE, 15);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 22.0F, 0.5F);
            Bones.turn(body.part("head"), 0.0F, 0.0F, 8.0F + pose.wave(0.07F, 0.0F) * 4.0F);
            blade(body, pose);
            float engulf = pose.key("engulf", 0.0F, 0.0F, 0.5F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -90.0F * engulf, -20.0F * engulf, 0.0F, -90.0F * engulf, -20.0F * engulf, 0.0F);
            Humanoid.die(body, pose, 17.0F);
        }
    }

    /**
     * The Jade Titan: a towering colossus armoured in jade, darkness boiling within its visor, and a vast tendril of
     * darkness rising from its back like the stem of a black flower. Built small and drawn very large.
     */
    public static final class JadeTitan extends CreatureModel {
        private static final Look LOOK = new Look(0x2A7A4E, 0x062A14, 0x5AB07A, 0x0A0A0A, 0x3A9A60, 0x3AA066, 0x2A7048);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 4.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 64.0F, 18.0F, Material.CRYSTAL, Material.CRYSTAL, Material.CRYSTAL);
            box(rig, "chest", Material.VEIN, -7.0F, -h.chest * 0.6F, -5.2F, 14.0F, h.chest * 0.45F, 1.0F);
            both(rig, "chest", Material.CRYSTAL, -13.0F, -h.chest * 0.75F, -5.0F, 5.0F, 5.0F, 10.0F);
            box(rig, "head", Material.VOID, -3.0F, -h.head * 0.65F, -4.5F, 6.0F, 2.0F, 1.0F);
            for (int c = 0; c < 4; c++) {
                both(rig, "head", Material.CRYSTAL, -4.0F + c * 1.0F, -h.head - 3.0F - c, -2.0F, 1.5F, 3.0F + c, 1.5F);
            }

            // The tendril of darkness from its back.
            String parent = "chest";
            for (int t = 0; t < 6; t++) {
                String bone = "tendril" + t;
                rig.bone(bone, parent, 0.0F, t == 0 ? -h.chest * 0.5F : -9.0F, t == 0 ? 6.0F : 0.0F, t == 0 ? -35.0F : -8.0F, 0.0F, 0.0F);
                float w = 6.0F - t * 0.7F;
                box(rig, bone, Material.VOID, -w / 2.0F, -9.0F, -w / 2.0F, w, 9.0F, w);
                parent = bone;
            }

            sword(rig, Material.CRYSTAL, 28);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 18.0F, 0.3F);
            Gaits.ripple(body, pose, "tendril", 6, 6.0F, 0.05F, 0.6F);
            Bones.turn(body.part("arm_r"), pose.key("smite", 0.0F, 0.0F, 0.55F, -160.0F, 0.66F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("chest"), 0.0F, pose.key("sweep", 0.0F, 0.0F, 0.4F, -45.0F, 0.6F, 45.0F, 1.0F, 0.0F), 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("sweep", 0.0F, 0.0F, 0.3F, -80.0F, 0.7F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            float dark = pose.key("darkness", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("tendril0"), 30.0F * dark, 0.0F, 0.0F);
            Bones.turn(body.part("head"), -15.0F * dark, 0.0F, 0.0F);
            float muster = pose.key("muster", 0.0F, 0.0F, 0.4F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("arm_l"), -150.0F * muster, 0.0F, 0.0F);
            Humanoid.die(body, pose, 29.4F);
        }
    }

    /** The Horror of the Lightless Current: great tentacles rising out of the black water, dripping venom. */
    public static final class LightlessHorror extends CreatureModel {
        private static final Look LOOK = new Look(0x0A0A10, 0x000000, 0x2A2A36, 0x3A1A2A, 0x1A1A22, 0x9CFF5A, 0x14141C);
        private static final int TENTACLES = 5;

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
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.SCALE, -10, -4, -10, 20, 4, 20);
            for (int t = 0; t < TENTACLES; t++) {
                float a = t * Mth.TWO_PI / TENTACLES;
                String parent = "body";
                for (int s = 0; s < 5; s++) {
                    String bone = "tent" + t + "_" + s;
                    rig.bone(bone, parent, s == 0 ? Mth.sin(a) * 6.0F : 0.0F, s == 0 ? -3.0F : -8.0F, s == 0 ? Mth.cos(a) * 6.0F : 0.0F,
                        s == 0 ? -20.0F * Mth.cos(a) : 4.0F, 0.0F, s == 0 ? 20.0F * Mth.sin(a) : 0.0F);
                    float w = 4.0F - s * 0.6F;
                    box(rig, bone, s >= 3 ? Material.VEIN : Material.SCALE, -w / 2.0F, -8.0F, -w / 2.0F, w, 8.0F, w);
                    parent = bone;
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            for (int t = 0; t < TENTACLES; t++) {
                for (int s = 1; s < 5; s++) {
                    Bones.turn(body.part("tent" + t + "_" + s), pose.wave(0.07F, t * 1.3F + s * 0.7F) * 12.0F, 0.0F, pose.wave(0.06F, t + s) * 10.0F);
                }
            }

            float crush = pose.key("crush", 0.0F, 0.0F, 0.5F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
            for (int s = 0; s < 5; s++) {
                Bones.turn(body.part("tent0_" + s), 25.0F * crush, 0.0F, 0.0F);
            }

            float lash = pose.key("lash", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int t = 0; t < TENTACLES; t++) {
                Bones.turn(body.part("tent" + t + "_1"), 0.0F, 0.0F, 35.0F * lash);
            }

            Bones.size(body.part("body"), 1.0F - 0.4F * pose.fall(0.0F, 1.0F));
        }
    }
}
