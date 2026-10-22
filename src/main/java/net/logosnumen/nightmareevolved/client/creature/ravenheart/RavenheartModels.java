package net.logosnumen.nightmareevolved.client.creature.ravenheart;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** The creatures around Ravenheart: the Chalice Knights and the stone worms. */
public final class RavenheartModels {
    private RavenheartModels() {
    }

    /**
     * A Chalice Knight: rust-green plate, a helm shaped like a beast's snouted head, a chalice worked on the breastplate,
     * a heavy blade. When the beast bursts out, a fanged maw splits the helm, spines tear through the back of the armour,
     * claws through the gauntlets, and the blade is cast aside.
     */
    public static final class ChaliceKnight extends CreatureModel {
        private static final Look LOOK = new Look(0x4A5A3A, 0x1A2010, 0x7A8A5A, 0x6A2A24, 0x8A4A2A, 0xFFB030, 0x4E5E36);
        private static final String[] BEAST = {"maw", "spines", "claw_r", "claw_l"};

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 40.0F, 12.0F, Material.PLATE, Material.PLATE, Material.PLATE);
            // Heavy pauldrons, the faulds, and rust bleeding through at the edges.
            both(rig, "chest", Material.PLATE, -9.0F, -h.chest * 0.72F, -3.6F, 4.0F, 4.0F, 7.2F);
            both(rig, "chest", Material.HORN, -9.4F, -h.chest * 0.74F, -1.0F, 1.0F, 1.0F, 2.0F);
            box(rig, "body", Material.HORN, -5.0F, -1.0F, -3.2F, 10.0F, 1.0F, 6.4F);
            // The chalice on the breastplate: a cup on a stem.
            box(rig, "chest", Material.RUNE, -2.0F, -h.chest * 0.6F, -3.5F, 4.0F, 2.0F, 1.0F);
            box(rig, "chest", Material.RUNE, -0.5F, -h.chest * 0.6F + 2.0F, -3.5F, 1.0F, 2.0F, 1.0F);
            box(rig, "chest", Material.RUNE, -1.5F, -h.chest * 0.6F + 4.0F, -3.5F, 3.0F, 1.0F, 1.0F);
            // The helm: a beast's head, snouted, with eye slits and swept-back horns.
            box(rig, "head", Material.PLATE, -2.0F, -h.head * 0.55F, -h.head * 0.5F - 3.0F, 4.0F, 3.0F, 3.0F);
            both(rig, "head", Material.EYE, -2.2F, -h.head * 0.7F, -h.head * 0.5F - 0.4F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < 3; i++) {
                both(rig, "head", Material.HORN, -3.2F - i * 0.6F, -h.head - 1.0F - i * 1.4F, 0.5F + i * 1.6F, 1.2F, 2.0F, 1.6F);
            }

            // The beast within: hidden until it bursts out.
            rig.bone("maw", "head", 0.0F, -h.head * 0.25F, -h.head * 0.5F - 1.0F);
            box(rig, "maw", Material.BELLY, -2.5F, 0.0F, -5.0F, 5.0F, 2.0F, 5.0F);
            for (int t = 0; t < 4; t++) {
                box(rig, "maw", Material.BONE, -2.2F + t * 1.3F, -1.2F, -4.8F, 0.6F, 1.4F, 0.6F);
            }

            rig.bone("spines", "chest", 0.0F, -h.chest * 0.5F, h.shoulders * 0.26F);
            for (int s = 0; s < 5; s++) {
                box(rig, "spines", Material.BONE, -0.6F + (s % 2) * 1.2F - 0.6F, -1.0F + s * 1.8F, 0.0F, 1.0F, 1.0F, 3.0F + (s % 3));
            }

            rig.pairBone("claw", "hand", 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            for (int c = 0; c < 3; c++) {
                rig.pair("claw", Material.CLAW, -1.6F + c * 1.1F, 0.0F, -1.5F, 1, 5, 1);
            }

            // The blade, heavy and broad.
            rig.bone("sword", "hand_r", 0.0F, 0.0F, 0.0F, 20.0F, 0.0F, 0.0F);
            rig.box("sword", Material.HORN, -1, -1, -1, 2, 2, 3);
            rig.box("sword", Material.PLATE, -3, -1.5F, -2, 6, 1, 1);
            rig.box("sword", Material.PLATE, -1.5F, -0.5F, -24, 3, 1, 22);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            boolean beast = pose.enraged();
            for (String bone : BEAST) {
                visible(body.part(bone), beast);
            }

            visible(body.part("sword"), !beast);
            Humanoid.walk(body, pose, beast ? 34.0F : 26.0F, beast ? 0.8F : 0.55F);
            if (beast) {
                // Hunched forward, head thrust out, arms low and wide.
                Bones.turn(body.part("body"), 22.0F, 0.0F, 0.0F);
                Bones.turn(body.part("head"), -18.0F, 0.0F, 0.0F);
                Gaits.pair(body, "arm", -25.0F, 0.0F, 18.0F, -25.0F, 0.0F, 18.0F);
                Bones.turn(body.part("maw"), 15.0F + pose.wave(0.3F, 0.0F) * 5.0F, 0.0F, 0.0F);
                Gaits.pair(body, "arm", pose.key("rend", 0.0F, 0.0F, 0.4F, -120.0F, 0.56F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F,
                    pose.key("rend", 0.0F, 0.0F, 0.5F, -110.0F, 0.7F, -20.0F, 1.0F, 0.0F), 0.0F, 0.0F);
                float maul = pose.key("maul", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
                Bones.turn(body.part("maw"), 35.0F * Math.max(0.0F, -maul), 0.0F, 0.0F);
                Bones.turn(body.part("chest"), 20.0F * Math.max(0.0F, maul), 0.0F, 0.0F);
                float pounce = pose.key("pounce", 0.0F, 0.0F, 0.3F, -1.0F, 0.5F, 1.0F, 1.0F, 0.0F);
                Bones.turn(body.part("body"), 25.0F * Math.max(0.0F, pounce), 0.0F, 0.0F);
                Gaits.pair(body, "arm", -90.0F * Math.max(0.0F, pounce), 0.0F, 0.0F, -90.0F * Math.max(0.0F, pounce), 0.0F, 0.0F);
                Bones.shift(body.part("body"), 0.0F, 4.0F * Math.max(0.0F, -pounce), 0.0F);
            } else {
                Humanoid.swordwork(body, pose);
            }

            Humanoid.die(body, pose, 18.4F);
        }

        private static void visible(ModelPart part, boolean visible) {
            if (part != null) {
                part.visible = visible;
            }
        }
    }

    /** A stone worm: grey scales, six thin limbs ending in dagger claws, and a round mouth ringed with needles. */
    public static final class StoneWorm extends CreatureModel {
        private static final Look LOOK = new Look(0x6A6460, 0x2A2622, 0x9A948C, 0x4A3A36, 0xD8D0C0, 0xFF9030, 0x5A544E);
        private static final int SEGMENTS = 6;
        private static final int LEGS = 3;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -5.0F, -9.0F);
            rig.bone("head", "body", 0.0F, 0.0F, 0.0F);
            rig.box("head", Material.SCALE, -3, -3, -4, 6, 6, 4);
            for (int t = 0; t < 8; t++) {
                float a = t * Mth.TWO_PI / 8.0F;
                box(rig, "head", Material.BONE, Mth.sin(a) * 2.0F - 0.25F, Mth.cos(a) * 2.0F - 0.25F, -5.0F, 0.5F, 0.5F, 1.5F);
            }

            rig.box("head", Material.EYE, -1.5F, -1.5F, -4.3F, 3, 3, 1);
            String parent = "body";
            for (int s = 0; s < SEGMENTS; s++) {
                String seg = "seg" + s;
                rig.bone(seg, parent, 0.0F, 0.0F, s == 0 ? 0.0F : 4.0F);
                float w = 6.0F - Math.max(0, s - 3) * 1.2F;
                box(rig, seg, Material.SCALE, -w / 2.0F, -w / 2.0F, 0.0F, w, w, 4.0F);
                box(rig, seg, Material.PLATE, -w / 2.0F + 0.5F, -w / 2.0F - 0.5F, 0.5F, w - 1.0F, 1.0F, 3.0F);
                if (s >= 1 && s <= LEGS) {
                    String leg = "leg" + (s - 1);
                    rig.pairBone(leg, seg, -w / 2.0F, w / 2.0F - 1.0F, 2.0F, 0.0F, 0.0F, 40.0F);
                    rig.pair(leg, Material.SCALE, -6, -0.5F, -0.5F, 6, 1, 1);
                    rig.pair(leg, Material.CLAW, -7, -0.5F, -0.5F, 1, 3, 1);
                }

                parent = seg;
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sway(body, pose, "seg", SEGMENTS, 9.0F, 0.4F + 0.5F * pose.stride(), 0.6F);
            Gaits.sideLegs(body, pose, LEGS, 30.0F, 20.0F, 0.9F);
            Gaits.look(body, pose, 0.5F, 0.4F);
            float bite = pose.key("bite", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, 0.0F, -3.0F * Math.max(0.0F, bite));
            Bones.turn(body.part("head"), -15.0F * Math.max(0.0F, -bite), 0.0F, 0.0F);
            float claws = pose.key("claws", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "leg0", 0.0F, -50.0F * claws, -30.0F * Math.abs(claws), 0.0F, -50.0F * claws, -30.0F * Math.abs(claws));
            float lunge = pose.key("lunge", 0.0F, 0.0F, 0.3F, -1.0F, 0.5F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("seg0"), -20.0F * Math.max(0.0F, -lunge), 0.0F, 0.0F);
            Gaits.fall(body, pose, 0.0F, 180.0F);
        }
    }
}
