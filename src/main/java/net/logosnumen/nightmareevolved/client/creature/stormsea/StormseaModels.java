package net.logosnumen.nightmareevolved.client.creature.stormsea;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/** The creatures of the Stormsea. */
public final class StormseaModels {
    private StormseaModels() {
    }

    /** A sea serpent: a long finned body the colour of storm water, a narrow head and a mouth of hooked teeth. */
    public static final class SeaSerpent extends CreatureModel {
        private static final Look LOOK = new Look(0x3A5A5A, 0x142424, 0x6A8A84, 0xC8D8D0, 0xE8E0D0, 0xE0FF60, 0x2A4A4A);
        private static final int SEGMENTS = 10;

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
            rig.bone("body", "root", 0.0F, -6.0F, -14.0F);
            rig.bone("head", "body", 0.0F, 0.0F, 0.0F);
            rig.box("head", Material.SCALE, -3, -3, -8, 6, 5, 8);
            both(rig, "head", Material.EYE, -3.2F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F);
            rig.pairBone("frill", "head", -3.0F, -2.0F, -1.0F, 0.0F, -30.0F, 0.0F);
            rig.pair("frill", Material.MEMBRANE, -4, -2, 0, 4, 4, 1);
            rig.bone("jaw", "head", 0.0F, 2.0F, -2.0F);
            rig.box("jaw", Material.BELLY, -2.5F, 0, -6, 5, 1, 6);
            for (int t = 0; t < 4; t++) {
                both(rig, "jaw", Material.BONE, -2.2F, -1.2F, -5.6F + t * 1.4F, 0.5F, 1.2F, 0.5F);
            }

            String parent = "body";
            for (int s = 0; s < SEGMENTS; s++) {
                String seg = "seg" + s;
                rig.bone(seg, parent, 0.0F, 0.0F, s == 0 ? 0.0F : 5.0F);
                float w = 5.0F - Math.max(0, s - 5) * 0.7F;
                box(rig, seg, Material.SCALE, -w / 2.0F, -w / 2.0F, 0.0F, w, w, 5.0F);
                box(rig, seg, Material.BELLY, -w / 2.0F + 0.5F, w / 2.0F - 0.6F, 0.0F, w - 1.0F, 1.0F, 5.0F);
                box(rig, seg, Material.MEMBRANE, -0.5F, -w / 2.0F - 2.0F, 0.5F, 1.0F, 2.0F, 4.0F);
                parent = seg;
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sway(body, pose, "seg", SEGMENTS, 14.0F, 0.25F + 0.5F * pose.stride(), 0.6F);
            Gaits.look(body, pose, 0.5F, 0.5F);
            Gaits.pair(body, "frill", 0.0F, pose.wave(0.2F, 0.0F) * 10.0F, 0.0F, 0.0F, pose.wave(0.2F, 0.0F) * 10.0F, 0.0F);
            float bite = pose.key("bite", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("jaw"), 45.0F * Math.max(0.0F, -bite), 0.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, 0.0F, -6.0F * Math.max(0.0F, bite));
            float coil = pose.key("coil", 0.0F, 0.0F, 0.5F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            for (int s = 1; s < SEGMENTS; s++) {
                Bones.turn(body.part("seg" + s), 0.0F, 20.0F * coil, 0.0F);
            }

            Bones.turn(body.part("head"), 12.0F * pose.key("lunge", 0.0F, 0.0F, 0.15F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F), 0.0F, 0.0F);
            Gaits.fall(body, pose, 0.0F, 180.0F);
        }
    }

    /**
     * Old Tom: a vast oyster shell, gold where it shows through a forest of black growths, its upper half lifted a
     * little, and out of the gap a tangle of tentacles far too big for it. Built at a size and drawn huge.
     */
    public static final class OldTom extends CreatureModel {
        private static final Look LOOK = new Look(0xC8A040, 0x5A4818, 0xF0D070, 0x2A1A22, 0x0C0C0E, 0x101010, 0xC09838);
        private static final int TENTACLES = 8;
        private static final int LINKS = 6;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 7.0F;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            // The lower half of the shell, ridged, and the upper half hinged at the back.
            rig.box("body", Material.PLATE, -16, -4, -14, 32, 4, 28);
            rig.box("body", Material.PLATE, -14, -5, -12, 28, 1, 24);
            rig.box("body", Material.BELLY, -13, -6, -11, 26, 1, 20);
            rig.bone("lid", "body", 0.0F, -5.0F, 13.0F, 0.0F, 0.0F, 0.0F);
            rig.box("lid", Material.PLATE, -16, -4, -27, 32, 4, 28);
            rig.box("lid", Material.PLATE, -12, -6, -23, 24, 2, 20);
            // The black growths over it, a forest of them.
            for (int g = 0; g < 26; g++) {
                float a = g * 2.399F;
                float r = 3.0F + (g % 7) * 1.7F;
                int tall = 2 + (g * 5) % 6;
                box(rig, "lid", Material.VOID, Mth.sin(a) * r - 0.6F, -6.0F - tall, -13.0F + Mth.cos(a) * r - 0.6F, 1.2F, tall, 1.2F);
            }

            for (int t = 0; t < TENTACLES; t++) {
                float a = (t - (TENTACLES - 1) / 2.0F) * 0.4F;
                String parent = "body";
                for (int s = 0; s < LINKS; s++) {
                    String bone = "tent" + t + "_" + s;
                    if (s == 0) {
                        rig.bone(bone, parent, Mth.sin(a) * 12.0F, -6.0F, -Mth.cos(a) * 11.0F, -50.0F + (t % 3) * 15.0F, a * Mth.RAD_TO_DEG, 0.0F);
                    } else {
                        rig.bone(bone, parent, 0.0F, 0.0F, -6.0F, 12.0F, 0.0F, 0.0F);
                    }

                    float w = 3.0F - s * 0.35F;
                    box(rig, bone, Material.BELLY, -w / 2.0F, -w / 2.0F, -6.0F, w, w, 6.0F);
                    parent = bone;
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Bones.turn(body.part("lid"), -12.0F - pose.wave(0.04F, 0.0F) * 3.0F, 0.0F, 0.0F);
            float sweep = pose.key("tentacles", 0.0F, 0.0F, 0.45F, -1.0F, 0.65F, 1.0F, 1.0F, 0.0F);
            float crush = pose.key("crush", 0.0F, 0.0F, 0.5F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            float deluge = pose.key("deluge", 0.0F, 0.0F, 0.55F, -1.0F, 0.66F, 1.0F, 1.0F, 0.0F);
            for (int t = 0; t < TENTACLES; t++) {
                for (int s = 0; s < LINKS; s++) {
                    Bones.turn(body.part("tent" + t + "_" + s), pose.wave(0.07F, t * 0.9F + s * 0.5F) * 12.0F + 18.0F * crush + 25.0F * deluge * (s == 0 ? 1.0F : 0.3F),
                        pose.wave(0.05F, t * 1.3F + s) * 10.0F + 30.0F * sweep * (s == 0 ? 1.0F : 0.2F), 0.0F);
                }
            }

            Bones.turn(body.part("lid"), -10.0F * Math.max(0.0F, -deluge), 0.0F, 0.0F);
            Gaits.fall(body, pose, 1.0F, 0.0F);
        }
    }
}
