package net.logosnumen.nightmareevolved.client.creature.godgrave;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/** Godgrave's greater creatures: the asuras and Condemnation, Abjuration, and the Guardian of Vanishing Lake. */
public final class GodgraveGreats {
    private GodgraveGreats() {
    }

    /**
     * An asura of Condemnation: a hunched golem of cracked stone in a robe of red moss, a mummy inside it, its blank
     * face sprouting two flowers on black stalks from its eye holes, a diamond mace in its hand.
     */
    public static final class Asura extends CreatureModel {
        private static final Look LOOK = new Look(0x8A8478, 0x3A3630, 0xB8B2A6, 0xA01820, 0xF0E0E8, 0xB0E8FF, 0xC8D8E8);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 56.0F, 15.0F, Material.STONE, Material.STONE, Material.STONE);
            box(rig, "chest", Material.MOSS, -8.0F, -h.chest * 0.66F, -4.5F, 16.0F, h.chest * 0.7F, 9.0F);
            box(rig, "body", Material.MOSS, -7.5F, -3.0F, -4.2F, 15.0F, h.hips * 0.7F, 8.4F);
            for (int s = 0; s < 2; s++) {
                float x = s == 0 ? -2.6F : 1.4F;
                box(rig, "head", Material.VOID, x, -h.head * 0.65F, -4.2F, 1.2F, 1.2F, 1.0F);
                box(rig, "head", Material.VOID, x + 0.3F, -h.head * 0.65F - 3.0F, -4.6F, 0.6F, 3.0F, 0.6F);
                box(rig, "head", Material.HORN, x - 0.6F, -h.head * 0.65F - 4.6F, -5.2F, 2.4F, 1.6F, 2.4F);
            }

            rig.bone("mace", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            rig.box("mace", Material.STONE, -1, -1, -10, 2, 2, 12);
            rig.box("mace", Material.CRYSTAL, -3, -3, -16, 6, 6, 7);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 20.0F, 0.4F);
            Bones.turn(body.part("chest"), 22.0F, 0.0F, 0.0F);
            Bones.turn(body.part("head"), -18.0F, 0.0F, 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("mace", 0.0F, 0.0F, 0.45F, -150.0F, 0.6F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.pair(body, "arm", pose.key("crush", 0.0F, 0.0F, 0.55F, -170.0F, 0.66F, -25.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("crush", 0.0F, 0.0F, 0.55F, -170.0F, 0.66F, -25.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 25.8F);
        }
    }

    /**
     * Condemnation: a shape like a person's, roughly made by something that never understood people, built of the ruins
     * themselves, of stone and soil, bones, trees and whole buildings. Built small and drawn as high as the hollow allows.
     */
    public static final class Condemnation extends CreatureModel {
        private static final Look LOOK = new Look(0x6A6458, 0x2A2420, 0x9A9284, 0x5A7A3A, 0xD8D0C0, 0xFF5A2A, 0x7A6E5E);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 6.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 64.0F, 20.0F, Material.BRICK, Material.STONE, Material.STONE);
            box(rig, "chest", Material.MOSS, -10.0F, -h.chest * 0.6F, -5.6F, 9.0F, 8.0F, 1.0F);
            box(rig, "chest", Material.BONE, 2.0F, -h.chest * 0.5F, -6.0F, 7.0F, 2.0F, 2.0F);
            box(rig, "chest", Material.BRICK, -4.0F, -h.chest * 0.9F, 3.0F, 9.0F, 7.0F, 6.0F);
            both(rig, "chest", Material.STONE, -14.0F, -h.chest * 0.75F, -4.0F, 5.0F, 6.0F, 8.0F);
            box(rig, "head", Material.STONE, -6.0F, -h.head * 1.2F, -4.0F, 12.0F, h.head * 1.1F, 9.0F);
            both(rig, "head", Material.EYE, -3.5F, -h.head * 0.7F, -4.3F, 2.0F, 2.0F, 1.0F);
            // Trees growing out of its shoulders and back.
            for (int t = 0; t < 3; t++) {
                float x = -8.0F + t * 8.0F;
                box(rig, "chest", Material.SCALE, x, -h.chest - 10.0F - t * 2.0F, 0.0F, 1.5F, 12.0F + t * 2.0F, 1.5F);
                box(rig, "chest", Material.MOSS, x - 3.0F, -h.chest - 15.0F - t * 2.0F, -3.0F, 7.0F, 6.0F, 7.0F);
            }

            rig.pair("hand", Material.BRICK, -4, -1, -4, 8, 6, 8);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 14.0F, 0.18F);
            Bones.turn(body.part("head"), 0.0F, 0.0F, 6.0F + pose.wave(0.03F, 0.0F) * 3.0F);
            Gaits.pair(body, "arm", pose.key("ruin", 0.0F, 0.0F, 0.6F, -160.0F, 0.72F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                pose.key("ruin", 0.0F, 0.0F, 0.6F, -160.0F, 0.72F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("chest"), 0.0F, pose.key("sweep", 0.0F, 0.0F, 0.4F, -40.0F, 0.6F, 40.0F, 1.0F, 0.0F), 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("sweep", 0.0F, 0.0F, 0.3F, -80.0F, 0.7F, -80.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("arm_r"), pose.key("rubble", 0.0F, 0.0F, 0.4F, -150.0F, 0.9F, -150.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Bones.turn(body.part("head"), pose.key("call", 0.0F, 0.0F, 0.4F, -30.0F, 0.8F, -30.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Humanoid.die(body, pose, 29.4F);
        }
    }

    /**
     * Abjuration: a towering mound of grey flesh overgrown with scarlet moss, a hundred limbs reaching out of it like a
     * forest, eyes opening everywhere across it. Built small and drawn large.
     */
    public static final class Abjuration extends CreatureModel {
        private static final Look LOOK = new Look(0x8A8A90, 0x3A3A40, 0xB4B4BA, 0xB02028, 0xA8A8AE, 0xFFF0A0, 0x7A7A80);
        private static final int LIMBS = 12;

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
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            rig.box("body", Material.FUR, -14, -16, -14, 28, 16, 28);
            rig.box("body", Material.FUR, -10, -28, -10, 20, 12, 20);
            rig.box("body", Material.FUR, -6, -36, -6, 12, 8, 12);
            rig.box("body", Material.MOSS, -15, -14, -15, 30, 8, 30);
            rig.box("body", Material.MOSS, -11, -27, -11, 22, 6, 22);
            for (int e = 0; e < 22; e++) {
                float a = e * 2.399F;
                float y = -3.0F - (e % 9) * 3.6F;
                float r = y > -16.0F ? 14.2F : y > -28.0F ? 10.2F : 6.2F;
                rig.box("body", Material.EYE, Mth.sin(a) * r - 0.5F, y, Mth.cos(a) * r - 0.5F, 1, 1, 1);
            }

            for (int l = 0; l < LIMBS; l++) {
                float a = l * Mth.TWO_PI / LIMBS;
                String limb = "limb" + l;
                float y = -8.0F - (l % 3) * 9.0F;
                float r = y > -16.0F ? 13.0F : y > -28.0F ? 9.0F : 5.0F;
                rig.bone(limb, "body", Mth.sin(a) * r, y, Mth.cos(a) * r, 0.0F, a * Mth.RAD_TO_DEG, 0.0F);
                rig.box(limb, Material.FUR, -1, -1, 0, 2, 2, 12);
                rig.bone(limb + "b", limb, 0.0F, 0.0F, 12.0F, 40.0F, 0.0F, 0.0F);
                rig.box(limb + "b", Material.FUR, -0.5F, -0.5F, 0, 1, 1, 10);
                rig.box(limb + "b", Material.CLAW, -1, -1, 10, 2, 2, 2);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            for (int l = 0; l < LIMBS; l++) {
                Bones.turn(body.part("limb" + l), pose.wave(0.06F, l * 1.1F) * 15.0F, pose.wave(0.05F, l * 0.7F) * 10.0F, 0.0F);
                Bones.turn(body.part("limb" + l + "b"), pose.wave(0.08F, l) * 20.0F, 0.0F, 0.0F);
            }

            Bones.size(body.part("body"), 1.0F + pose.wave(0.04F, 0.0F) * 0.02F);
            float limbs = pose.key("limbs", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int l = 0; l < LIMBS; l++) {
                Bones.turn(body.part("limb" + l), -30.0F * Math.abs(limbs), 30.0F * limbs, 0.0F);
            }

            float deny = pose.key("deny", 0.0F, 0.0F, 0.4F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.size(body.part("body"), 1.0F + 0.08F * deny - 0.3F * pose.fall(0.0F, 1.0F));
        }
    }

    /**
     * The Guardian of Vanishing Lake: towering and slender, robed in billowing darkness, silver armour on its long arms
     * and narrow body, its face hidden, and six pairs of beautiful grey wings spread behind it. Built small, drawn larger.
     */
    public static final class LakeGuardian extends CreatureModel {
        private static final Look LOOK = new Look(0x1A1A22, 0x050508, 0x3A3A48, 0x2A2A34, 0xC8CCD8, 0xF0F4FF, 0xB8BCC8);

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
            Humanoid h = Humanoid.build(rig, 56.0F, 11.0F, Material.PLATE, Material.PLATE, Material.CLOTH);
            box(rig, "body", Material.CLOTH, -8.0F, -4.0F, -5.0F, 16.0F, h.hips + 4.0F, 10.0F);
            box(rig, "chest", Material.CLOTH, -7.0F, -h.chest * 0.7F, 2.8F, 14.0F, h.chest * 0.7F, 2.0F);
            box(rig, "head", Material.VOID, -2.5F, -h.head * 0.8F, -4.2F, 5.0F, h.head * 0.6F, 1.0F);
            both(rig, "head", Material.EYE, -1.6F, -h.head * 0.6F, -4.4F, 1.0F, 1.0F, 1.0F);
            for (int w = 0; w < 6; w++) {
                String wing = "wing" + w;
                float y = -h.chest * 0.75F + w * 2.4F;
                rig.pairBone(wing, "chest", -3.0F, y, 3.0F, 0.0F, -15.0F - w * 6.0F, -40.0F + w * 14.0F);
                rig.pair(wing, Material.MEMBRANE, -22 + w, -0.5F, 0, 22 - w, 1, 8);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.size(body.part("leg0_r"), 0.01F);
            Bones.size(body.part("leg0_l"), 0.01F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.07F, 0.0F) * 1.5F, 0.0F);
            Gaits.look(body, pose, 0.8F, 0.6F);
            for (int w = 0; w < 6; w++) {
                float beat = pose.wave(0.12F, w * 0.4F) * 8.0F;
                Gaits.pair(body, "wing" + w, 0.0F, beat, beat, 0.0F, beat, beat);
            }

            Gaits.pair(body, "arm", -15.0F, 0.0F, 10.0F, -15.0F, 0.0F, 10.0F);
            Bones.turn(body.part("arm_r"), pose.key("rend", 0.0F, 0.0F, 0.4F, -140.0F, 0.56F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            float wings = pose.key("wings", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int w = 0; w < 6; w++) {
                Gaits.pair(body, "wing" + w, 0.0F, -50.0F * wings, 0.0F, 0.0F, -50.0F * wings, 0.0F);
            }

            float descend = pose.key("descend", 0.0F, 0.0F, 0.6F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "arm", -110.0F * descend, 0.0F, 0.0F, -110.0F * descend, 0.0F, 0.0F);
            Bones.size(body.part("body"), 1.0F - 0.6F * pose.fall(0.0F, 1.0F));
        }
    }
}
