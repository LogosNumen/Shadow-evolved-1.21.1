package net.logosnumen.nightmareevolved.client.creature.moonriver;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.creature.Humanoid;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The creatures of the Moonriver Plain. */
public final class MoonriverModels {
    private MoonriverModels() {
    }

    /**
     * A gaunt maw: a tall frame of sinew over bone, three pairs of long clawed arms, and a head far too big for it, with
     * two tiny bloodshot eyes over a crimson maw that splits it nearly in two.
     */
    public static final class GauntMaw extends CreatureModel {
        private static final Look LOOK = new Look(0x8A7A6A, 0x3A2E26, 0xB0A090, 0xB0202A, 0xE0D8C8, 0xFF3030, 0x6A3A30);
        private static final int ARMS = 2;

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            Humanoid h = Humanoid.build(rig, 46.0F, 9.0F, Material.BELLY, Material.BELLY, Material.BELLY);
            // Sinew drawn tight over the ribs.
            for (int r = 0; r < 4; r++) {
                box(rig, "chest", Material.VEIN, -4.0F, -h.chest * 0.6F + r * 2.6F, -2.6F, 8.0F, 0.8F, 0.5F);
            }

            // The head, much too big, nearly all maw.
            box(rig, "head", Material.BELLY, -6.0F, -12.0F, -6.0F, 12.0F, 10.0F, 11.0F);
            both(rig, "head", Material.EYE, -3.6F, -10.5F, -6.3F, 1.0F, 1.0F, 1.0F);
            box(rig, "head", Material.VEIN, -5.0F, -7.5F, -6.4F, 10.0F, 5.0F, 1.0F);
            for (int t = 0; t < 6; t++) {
                box(rig, "head", Material.BONE, -4.6F + t * 1.7F, -7.5F, -6.6F, 0.8F, 1.6F, 0.8F);
            }

            rig.bone("jaw", "head", 0.0F, -2.5F, -1.0F);
            box(rig, "jaw", Material.BELLY, -5.5F, 0.0F, -5.5F, 11.0F, 3.0F, 10.0F);
            box(rig, "jaw", Material.VEIN, -4.5F, -0.5F, -5.0F, 9.0F, 1.0F, 8.0F);
            for (int t = 0; t < 6; t++) {
                box(rig, "jaw", Material.BONE, -4.6F + t * 1.7F, -1.4F, -5.2F, 0.8F, 1.6F, 0.8F);
            }

            box(rig, "hand_r", Material.CLAW, -1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 1.0F);
            box(rig, "hand_l", Material.CLAW, -1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 1.0F);
            // The other arms, lower on the chest, as long and as thin.
            for (int a = 1; a <= ARMS; a++) {
                String arm = "arm" + a;
                rig.pairBone(arm, "chest", -5.0F, -h.chest * 0.55F + a * 3.5F, 0.0F, -10.0F * a, 0.0F, 12.0F + 6.0F * a);
                rig.pair(arm, Material.BELLY, -1, -1, -1, 2, 20, 2);
                rig.pair(arm, Material.CLAW, -1.5F, 19, -1, 3, 4, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Humanoid.walk(body, pose, 24.0F, 0.5F);
            for (int a = 1; a <= ARMS; a++) {
                Gaits.pair(body, "arm" + a, pose.gait(0.5F, a) * 15.0F + pose.wave(0.1F, a) * 6.0F, 0.0F, 0.0F,
                    pose.gait(0.5F, a + 1.0F) * 15.0F + pose.wave(0.1F, a + 1.5F) * 6.0F, 0.0F, 0.0F);
            }

            Bones.turn(body.part("jaw"), 8.0F + pose.wave(0.15F, 0.0F) * 4.0F, 0.0F, 0.0F);
            // Claws: the arms rake across one pair after another.
            for (int a = 0; a <= ARMS; a++) {
                String arm = a == 0 ? "arm" : "arm" + a;
                float rake = pose.key("claws", 0.0F, 0.0F, 0.25F + a * 0.1F, -110.0F, 0.45F + a * 0.12F, -20.0F, 1.0F, 0.0F);
                Gaits.pair(body, arm, rake, 0.0F, 0.0F, rake, 0.0F, 0.0F);
                float seize = pose.key("seize", 0.0F, 0.0F, 0.45F, -80.0F, 0.8F, -60.0F, 1.0F, 0.0F);
                Gaits.pair(body, arm, seize, 0.0F, -15.0F * seize / 80.0F, seize, 0.0F, -15.0F * seize / 80.0F);
            }

            float devour = pose.key("devour", 0.0F, 0.0F, 0.45F, -1.0F, 0.62F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("jaw"), 55.0F * Math.max(0.0F, -devour), 0.0F, 0.0F);
            Bones.turn(body.part("chest"), 25.0F * Math.max(0.0F, devour), 0.0F, 0.0F);
            Humanoid.die(body, pose, 21.2F);
        }
    }
}
