package net.logosnumen.nightmareevolved.client.creature.bastion;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.util.Mth;

/** The creatures of the lands around Bastion. */
public final class BastionModels {
    private BastionModels() {
    }

    /**
     * The Undetectable Forest Maw, opened: a patch of forest floor split down the middle, its two mossy lids heaved up
     * and fringed with teeth, a dark throat beneath them, and roots reaching out all round.
     */
    public static final class ForestMaw extends CreatureModel {
        private static final Look LOOK = new Look(0x4A3A2A, 0x1A120C, 0x6A5A3A, 0x8A2A24, 0xE0D8C0, 0xFF6030, 0x3A5A2A);
        private static final int ROOTS = 6;

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
            // The ring of earth around the opening, and the throat inside it.
            rig.box("body", Material.SCALE, -13, -3, -11, 26, 3, 3);
            rig.box("body", Material.SCALE, -13, -3, 8, 26, 3, 3);
            rig.box("body", Material.MOSS, -13, -4, -11, 26, 1, 3);
            rig.box("body", Material.MOSS, -13, -4, 8, 26, 1, 3);
            rig.box("body", Material.VOID, -11, -1, -8, 22, 1, 16);
            rig.box("body", Material.VEIN, -11, -2, -8, 22, 1, 1);
            rig.box("body", Material.VEIN, -11, -2, 7, 22, 1, 1);
            // The lids, hinged along the long sides: moss above, red flesh below, teeth along their edges.
            rig.pairBone("lid", "body", -12.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("lid", Material.MOSS, 0, -2, -8, 12, 2, 16);
            rig.pair("lid", Material.BELLY, 0, 0, -8, 11, 1, 16);
            for (int t = 0; t < 7; t++) {
                rig.pair("lid", Material.BONE, 10.5F, 0.5F, -7.0F + t * 2.2F, 1, 2, 1);
            }

            for (int r = 0; r < ROOTS; r++) {
                float a = r * Mth.TWO_PI / ROOTS + 0.3F;
                String parent = "body";
                for (int s = 0; s < 3; s++) {
                    String bone = "root" + r + "_" + s;
                    if (s == 0) {
                        rig.bone(bone, parent, Mth.sin(a) * 12.0F, -3.0F, Mth.cos(a) * 9.0F, -50.0F, a * Mth.RAD_TO_DEG, 0.0F);
                    } else {
                        rig.bone(bone, parent, 0.0F, 0.0F, 6.0F, 20.0F, 0.0F, 0.0F);
                    }

                    box(rig, bone, Material.SCALE, -0.75F + s * 0.2F, -0.75F + s * 0.2F, 0.0F, 1.5F - s * 0.4F, 1.5F - s * 0.4F, 6.0F);
                    parent = bone;
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            // Gaping, the lids heaving a little as it breathes; a bite slams them shut and throws them open again.
            float bite = pose.key("devour", 0.0F, 0.0F, 0.45F, 1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            float open = (1.0F - bite) * (0.85F + pose.wave(0.1F, 0.0F) * 0.1F);
            Gaits.pair(body, "lid", 0.0F, 0.0F, -70.0F * open, 0.0F, 0.0F, -70.0F * open);
            float grab = pose.key("roots", 0.0F, 0.0F, 0.5F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            float lash = pose.key("lash", 0.0F, 0.0F, 0.35F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int r = 0; r < ROOTS; r++) {
                for (int s = 0; s < 3; s++) {
                    Bones.turn(body.part("root" + r + "_" + s), pose.wave(0.12F, r + s * 0.7F) * 15.0F + 25.0F * grab, pose.wave(0.09F, r * 1.7F + s) * 12.0F
                        + 40.0F * lash, 0.0F);
                }
            }

            Gaits.fall(body, pose, 2.0F, 0.0F);
        }
    }
}
