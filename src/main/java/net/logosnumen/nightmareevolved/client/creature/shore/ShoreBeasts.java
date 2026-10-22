package net.logosnumen.nightmareevolved.client.creature.shore;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The Forgotten Shore's beasts: the Spire's heralds, the iron spiders, the flesh reavers, the corpse eaters, the weeping beast. */
public final class ShoreBeasts {
    private ShoreBeasts() {
    }

    /**
     * A Spire Messenger, or (larger, darker with blood) the Cursed Herald: a lion-raven with a corpse-pale body, black
     * feathers over its head and chest, two lion's hind legs, six clawed limbs from its chest, and a long jagged beak.
     */
    public static final class Herald extends CreatureModel {
        private final float s;
        private final Look[] looks;

        public Herald(float scale, Look... looks) {
            this.s = scale;
            this.looks = looks;
        }

        @Override
        public Look look(int look) {
            return this.looks[Math.floorMod(look, this.looks.length)];
        }

        @Override
        public Rig build(int look) {
            float s = this.s;
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -20.0F * s, 2.0F * s);
            box(rig, "body", Material.BELLY, -6.0F * s, -6.0F * s, -10.0F * s, 12.0F * s, 11.0F * s, 22.0F * s);
            box(rig, "body", Material.FEATHER, -6.5F * s, -7.0F * s, -11.0F * s, 13.0F * s, 9.0F * s, 9.0F * s);
            box(rig, "body", Material.FEATHER, -2.5F * s, -6.0F * s, 12.0F * s, 5.0F * s, 3.0F * s, 9.0F * s);
            rig.bone("head", "body", 0.0F, -6.0F * s, -10.0F * s);
            box(rig, "head", Material.FEATHER, -3.5F * s, -6.0F * s, -6.0F * s, 7.0F * s, 7.0F * s, 7.0F * s);
            both(rig, "head", Material.EYE, -3.6F * s, -4.2F * s, -4.0F * s, 1.0F, 1.0F, 1.0F);
            box(rig, "head", Material.CLAW, -1.5F * s, -4.0F * s, -16.0F * s, 3.0F * s, 3.0F * s, 10.0F * s);
            box(rig, "head", Material.CLAW, -1.0F * s, -4.5F * s, -18.0F * s, 2.0F * s, 3.0F * s, 3.0F * s);
            rig.bone("jaw", "head", 0.0F, -1.0F * s, -6.0F * s);
            box(rig, "jaw", Material.CLAW, -1.2F * s, 0.0F, -8.0F * s, 2.4F * s, 1.5F * s, 8.0F * s);
            box(rig, "jaw", Material.BONE, -1.0F * s, -0.6F * s, -7.0F * s, 2.0F * s, 1.0F, 1.0F);
            // Six clawed limbs from the chest.
            for (int k = 0; k < 3; k++) {
                rig.pairBone("claw" + k, "body", -4.0F * s, (-1.0F + k * 2.5F) * s, (-9.0F + k * 2.0F) * s, -40.0F, 0.0F, 20.0F + k * 10.0F);
                rig.pair("claw" + k, Material.BELLY, -1.0F * s, 0.0F, -1.0F * s, n(2.0F * s), n(8.0F * s), n(2.0F * s));
                rig.pair("claw" + k, Material.CLAW, -1.5F * s, 7.0F * s, -2.0F * s, n(3.0F * s), n(3.0F * s), n(2.0F * s));
            }

            // Two lion's hind legs.
            rig.pairBone("leg1", "body", -4.5F * s, 3.0F * s, 7.0F * s, 0.0F, 0.0F, 0.0F);
            rig.pair("leg1", Material.BELLY, -2.0F * s, -1.0F * s, -2.0F * s, n(4.0F * s), n(10.0F * s), n(5.0F * s));
            rig.pairBone("shin1", "leg1", 0.0F, 9.0F * s, 1.0F * s, 0.0F, 0.0F, 0.0F);
            rig.pair("shin1", Material.BELLY, -1.5F * s, 0.0F, -1.5F * s, n(3.0F * s), n(8.0F * s), n(3.0F * s));
            rig.pair("shin1", Material.CLAW, -2.0F * s, 7.0F * s, -3.0F * s, n(4.0F * s), n(2.0F * s), n(4.0F * s));
            // The wings.
            rig.pairBone("wing", "body", -6.0F * s, -5.0F * s, -4.0F * s, 0.0F, 0.0F, -10.0F);
            rig.pair("wing", Material.FEATHER, -22.0F * s, -1.0F * s, -3.0F * s, n(22.0F * s), n(2.0F * s), n(12.0F * s));
            rig.pairBone("tip", "wing", -22.0F * s, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("tip", Material.FEATHER, -18.0F * s, -0.5F * s, -1.0F * s, n(18.0F * s), n(1.0F * s), n(14.0F * s));
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.wings(body, pose, 35.0F, 0.32F, -5.0F);
            Gaits.look(body, pose, 0.7F, 0.6F);
            Bones.turn(body.part("jaw"), 10.0F + pose.wave(0.15F, 0.0F) * 6.0F, 0.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.32F, 1.2F) * 1.5F * this.s, 0.0F);
            for (int k = 0; k < 3; k++) {
                float flail = pose.wave(0.4F, k * 1.7F) * 20.0F;
                Gaits.pair(body, "claw" + k, flail, 0.0F, 0.0F, -flail, 0.0F, 0.0F);
            }

            Gaits.pair(body, "leg1", 30.0F, 0.0F, 0.0F, 30.0F, 0.0F, 0.0F);
            Gaits.pair(body, "shin1", 40.0F, 0.0F, 0.0F, 40.0F, 0.0F, 0.0F);

            // Swoop: wings swept back, every claw thrown forward.
            float swoop = pose.key("swoop", 0.0F, 0.0F, 0.6F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "wing", 0.0F, 40.0F * swoop, -20.0F * swoop, 0.0F, 40.0F * swoop, -20.0F * swoop);
            for (int k = 0; k < 3; k++) {
                Gaits.pair(body, "claw" + k, -70.0F * swoop, 0.0F, 0.0F, -70.0F * swoop, 0.0F, 0.0F);
            }

            float rake = pose.key("rake", 0.0F, 0.0F, 0.45F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            for (int k = 0; k < 3; k++) {
                Gaits.pair(body, "claw" + k, 70.0F * rake, 0.0F, 0.0F, 70.0F * rake, 0.0F, 0.0F);
            }

            float peck = pose.key("peck", 0.0F, 0.0F, 0.45F, -1.0F, 0.62F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), 25.0F * peck, 0.0F, 0.0F);
            Bones.shift(body.part("head"), 0.0F, 0.0F, -4.0F * peck * this.s);
            Bones.turn(body.part("jaw"), 30.0F * Math.max(0.0F, -peck), 0.0F, 0.0F);
            Gaits.fall(body, pose, 14.0F * this.s, 80.0F);
        }
    }

    /** An iron spider, or (built heavier, plated all over, drawn larger) the Matriarch of them all. */
    public static final class Spider extends CreatureModel {
        private static final Look LOOK = new Look(0x3A3C44, 0x121318, 0x6A6C78, 0x26282E, 0x8A8C98, 0xE02020, 0x5A5C66);
        private final float s;
        private final boolean matriarch;

        public Spider(float scale, boolean matriarch) {
            this.s = scale;
            this.matriarch = matriarch;
        }

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return this.matriarch ? 2.0F : 1.0F;
        }

        @Override
        public Rig build(int look) {
            float s = this.s;
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -9.0F * s, 0.0F);
            box(rig, "body", Material.SCALE, -4.0F * s, -3.0F * s, -6.0F * s, 8.0F * s, 6.0F * s, 8.0F * s);
            box(rig, "body", Material.PLATE, -3.5F * s, -4.0F * s, -5.5F * s, 7.0F * s, 1.0F, 7.0F * s);
            rig.bone("abdomen", "body", 0.0F, -1.0F * s, 2.0F * s);
            box(rig, "abdomen", Material.SCALE, -5.5F * s, -4.5F * s, 0.0F, 11.0F * s, 9.0F * s, 12.0F * s);
            box(rig, "abdomen", Material.PLATE, -5.0F * s, -5.5F * s, 1.0F * s, 10.0F * s, 1.0F, 10.0F * s);
            if (this.matriarch) {
                box(rig, "abdomen", Material.PLATE, -6.0F * s, -4.0F * s, 0.5F * s, 1.0F, 7.0F * s, 11.0F * s);
                box(rig, "abdomen", Material.PLATE, 5.0F * s, -4.0F * s, 0.5F * s, 1.0F, 7.0F * s, 11.0F * s);
                box(rig, "body", Material.PLATE, -4.5F * s, -2.5F * s, -6.5F * s, 9.0F * s, 4.0F * s, 1.0F);
            }

            rig.bone("head", "body", 0.0F, 0.0F, -6.0F * s);
            box(rig, "head", Material.SCALE, -3.0F * s, -2.5F * s, -3.0F * s, 6.0F * s, 5.0F * s, 3.0F * s);
            for (int e = 0; e < 4; e++) {
                box(rig, "head", Material.EYE, (-2.4F + e * 1.3F) * s, -2.0F * s, -3.2F * s, 1.0F, 1.0F, 1.0F);
            }

            rig.pairBone("fang", "head", -1.5F * s, 1.0F * s, -3.0F * s, 0.0F, 0.0F, 0.0F);
            rig.pair("fang", Material.CLAW, -0.5F * s, 0.0F, -1.0F * s, n(1.0F * s), n(3.0F * s), n(1.0F * s));
            for (int i = 0; i < 4; i++) {
                rig.pairBone("leg" + i, "body", -3.5F * s, 0.0F, (-4.5F + i * 2.5F) * s, 0.0F, -35.0F + i * 22.0F, 35.0F);
                rig.pair("leg" + i, Material.SCALE, -10.0F * s, -1.0F * s, -1.0F * s, n(10.0F * s), n(2.0F * s), n(2.0F * s));
                rig.pairBone("shin" + i, "leg" + i, -10.0F * s, 0.0F, 0.0F, 0.0F, 0.0F, -100.0F);
                rig.pair("shin" + i, Material.SCALE, -12.0F * s, -0.8F * s, -0.8F * s, n(12.0F * s), n(1.6F * s), n(1.6F * s));
                if (this.matriarch) {
                    rig.pair("leg" + i, Material.PLATE, -8.0F * s, -1.5F * s, -1.5F * s, n(5.0F * s), n(3.0F * s), n(3.0F * s));
                }
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sideLegs(body, pose, 4, 22.0F, 16.0F, 1.1F / this.s);
            Gaits.look(body, pose, 0.4F, 0.3F);
            Bones.turn(body.part("abdomen"), pose.wave(0.1F, 0.0F) * 3.0F, 0.0F, 0.0F);
            float bite = Math.max(pose.key("bite", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F),
                pose.key("fangs", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F));
            Bones.turn(body.part("body"), 15.0F * bite, 0.0F, 0.0F);
            Gaits.pair(body, "fang", -40.0F * bite, 0.0F, 0.0F, -40.0F * bite, 0.0F, 0.0F);
            Gaits.pair(body, "leg0", 0.0F, -20.0F * Math.max(0.0F, -bite), 40.0F * Math.max(0.0F, -bite), 0.0F, -20.0F * Math.max(0.0F, -bite),
                40.0F * Math.max(0.0F, -bite));
            float web = pose.key("web", 0.0F, 0.0F, 0.5F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("abdomen"), -45.0F * web, 0.0F, 0.0F);
            float stamp = pose.key("stamp", 0.0F, 0.0F, 0.55F, 1.0F, 0.65F, -0.4F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), -16.0F * Math.max(0.0F, stamp), 0.0F, 0.0F);
            Gaits.pair(body, "leg0", 0.0F, 0.0F, 50.0F * stamp, 0.0F, 0.0F, 50.0F * stamp);
            Gaits.pair(body, "leg1", 0.0F, 0.0F, 35.0F * stamp, 0.0F, 0.0F, 35.0F * stamp);
            float brood = pose.key("brood", 0.0F, 0.0F, 0.4F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("abdomen"), 20.0F * brood, 0.0F, 0.0F);
            Gaits.fall(body, pose, 6.0F * this.s, 180.0F);
        }
    }

    /** A flesh reaver: a locust the size of a dog, with great folded hind legs and ragged wings. */
    public static final class Reaver extends CreatureModel {
        private static final Look LOOK = new Look(0x6A6A2A, 0x2A2A10, 0x9A9A50, 0x8A7A40, 0x4A3A20, 0xE0D040, 0x7A6A3A);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -7.0F, 0.0F);
            rig.box("body", Material.SCALE, -2.5F, -2.5F, -4, 5, 5, 6);
            rig.bone("abdomen", "body", 0.0F, 0.0F, 2.0F);
            rig.box("abdomen", Material.BELLY, -2, -2, 0, 4, 4, 9);
            rig.bone("head", "body", 0.0F, -1.0F, -4.0F);
            rig.box("head", Material.SCALE, -2, -2.5F, -4, 4, 5, 4);
            both(rig, "head", Material.EYE, -2.4F, -2.0F, -3.5F, 1, 2, 2);
            rig.box("head", Material.CLAW, -1, 1.5F, -4.5F, 2, 2, 1);
            rig.pairBone("antenna", "head", -1.0F, -2.5F, -3.5F, -40.0F, -15.0F, 0.0F);
            rig.pair("antenna", Material.HORN, -0.5F, -8, -0.5F, 1, 8, 1);
            rig.pairBone("wing", "body", -1.5F, -2.5F, -2.0F, 0.0F, 0.0F, 0.0F);
            rig.pair("wing", Material.MEMBRANE, -2, -0.5F, 0, 3, 1, 13);
            rig.pairBone("leg0", "body", -2.0F, 2.0F, -2.5F, 0.0F, 0.0F, 25.0F);
            rig.pair("leg0", Material.SCALE, -0.5F, 0, -0.5F, 1, 6, 1);
            rig.pairBone("leg1", "body", -2.0F, 2.0F, 0.5F, 0.0F, 0.0F, 25.0F);
            rig.pair("leg1", Material.SCALE, -0.5F, 0, -0.5F, 1, 6, 1);
            // The great hind legs, folded.
            rig.pairBone("hind", "abdomen", -2.2F, 0.0F, 1.0F, 30.0F, 0.0F, 10.0F);
            rig.pair("hind", Material.SCALE, -1, -1, 0, 2, 2, 9);
            rig.pairBone("hindshin", "hind", 0.0F, 0.0F, 9.0F, -130.0F, 0.0F, 0.0F);
            rig.pair("hindshin", Material.SCALE, -0.5F, -0.5F, 0, 1, 1, 10);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.hangingLegs(body, pose, 2, 25.0F, 1.4F);
            Gaits.look(body, pose, 0.5F, 0.4F);
            Gaits.pair(body, "antenna", pose.wave(0.3F, 0.0F) * 10.0F, 0.0F, 0.0F, pose.wave(0.3F, 1.0F) * 10.0F, 0.0F, 0.0F);
            float leap = pose.key("leap", 0.0F, 0.0F, 0.45F, -0.6F, 0.6F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "hind", -50.0F * leap, 0.0F, 0.0F, -50.0F * leap, 0.0F, 0.0F);
            Gaits.pair(body, "hindshin", 100.0F * leap, 0.0F, 0.0F, 100.0F * leap, 0.0F, 0.0F);
            Gaits.pair(body, "wing", 0.0F, -30.0F * Math.max(0.0F, leap), -60.0F * Math.max(0.0F, leap) + pose.wave(2.0F, 0.0F) * 20.0F * Math.max(0.0F, leap),
                0.0F, -30.0F * Math.max(0.0F, leap), -60.0F * Math.max(0.0F, leap) + pose.wave(2.0F, 0.0F) * 20.0F * Math.max(0.0F, leap));
            float bite = pose.key("bite", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("head"), 20.0F * bite, 0.0F, 0.0F);
            Gaits.fall(body, pose, 3.0F, 180.0F);
        }
    }

    /** A corpse eater: a metre-long leech, slimy, half clear and the red of dried blood, with a ringed sucker of a mouth. */
    public static final class Leech extends CreatureModel {
        private static final Look LOOK = new Look(0x7A1418, 0x2A0406, 0xB04048, 0x9A2A30, 0x5A1014, 0xFF5050, 0x9A2026);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -4.0F, -6.0F);
            String parent = "body";
            for (int i = 0; i < 4; i++) {
                String name = "seg" + i;
                rig.bone(name, parent, 0.0F, 0.0F, i == 0 ? 0.0F : 4.5F);
                float w = 7.0F - Math.abs(i - 1.5F) * 1.4F;
                box(rig, name, Material.MEMBRANE, -w / 2.0F, -w / 2.0F + 0.5F, -0.5F, w, w - 1.0F, 5.0F);
                parent = name;
            }

            rig.box("body", Material.BELLY, -2.5F, -2.5F, -1.5F, 5, 5, 1);
            rig.box("body", Material.VOID, -1.5F, -1.5F, -1.8F, 3, 3, 1);
            for (int t = 0; t < 4; t++) {
                rig.box("body", Material.BONE, -2.0F + t * 1.2F, -2.2F, -2.0F, 1, 1, 1);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sway(body, pose, "seg", 4, 12.0F, 0.6F + pose.stride(), 0.9F);
            float latch = pose.key("latch", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("seg0"), -30.0F * latch, 0.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, 0.0F, -3.0F * latch);
            Gaits.fall(body, pose, 0.0F, 180.0F);
        }
    }

    /** The weeping beast: four long legs, rotten flesh hanging from its bones like a tattered coat, a drooping, sobbing head. */
    public static final class Weeper extends CreatureModel {
        private static final Look LOOK = new Look(0x5A4A40, 0x22180F, 0x8A7466, 0x9A3A3A, 0xD8CCB8, 0xB0B8C0, 0x6A3A30);

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -20.0F, 0.0F);
            rig.box("body", Material.BONE, -4, -4, -12, 8, 7, 24);
            rig.box("body", Material.CLOTH, -6, -5, -11, 12, 13, 21);
            for (int r = 0; r < 4; r++) {
                rig.box("body", Material.BONE, -5, -3, -8 + r * 4, 10, 1, 1);
            }

            rig.bone("head", "body", 0.0F, -2.0F, -12.0F, 25.0F, 0.0F, 0.0F);
            rig.box("head", Material.BONE, -3, -3, -9, 6, 6, 9);
            rig.box("head", Material.CLOTH, -4, -4, -7, 8, 9, 6);
            both(rig, "head", Material.EYE, -2.6F, -1.0F, -9.1F, 1, 1, 1);
            for (int i = 0; i < 2; i++) {
                float z = i == 0 ? -9.0F : 9.0F;
                rig.pairBone("leg" + i, "body", -4.0F, 2.0F, z, 0.0F, 0.0F, 0.0F);
                rig.pair("leg" + i, Material.BONE, -1.5F, 0, -1.5F, 3, 11, 3);
                rig.pair("leg" + i, Material.CLOTH, -2.5F, 0, -2.5F, 5, 7, 5);
                rig.pairBone("shin" + i, "leg" + i, 0.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                rig.pair("shin" + i, Material.BONE, -1, 0, -1, 2, 9, 2);
                rig.pair("shin" + i, Material.CLAW, -1.5F, 8, -2.5F, 3, 1, 4);
            }

            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.hangingLegs(body, pose, 2, 28.0F, 0.7F);
            Gaits.look(body, pose, 0.6F, 0.4F);
            // Its sobbing: a slow heave of the body and a shudder of the head.
            Bones.shift(body.part("body"), 0.0F, pose.wave(0.12F, 0.0F) * 0.6F, 0.0F);
            Bones.turn(body.part("head"), pose.wave(0.9F, 0.0F) * 2.0F, 0.0F, 0.0F);
            float maul = pose.key("maul", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("body"), -15.0F * Math.max(0.0F, -maul), 0.0F, 0.0F);
            Gaits.pair(body, "leg0", -60.0F * Math.max(0.0F, -maul) + 20.0F * Math.max(0.0F, maul), 0.0F, 0.0F,
                -60.0F * Math.max(0.0F, -maul) + 20.0F * Math.max(0.0F, maul), 0.0F, 0.0F);
            float lunge = pose.key("lunge", 0.0F, 0.0F, 0.35F, -0.5F, 0.65F, 1.0F, 1.0F, 0.0F);
            Gaits.pair(body, "leg0", -50.0F * lunge, 0.0F, 0.0F, -50.0F * lunge, 0.0F, 0.0F);
            Gaits.pair(body, "leg1", 40.0F * lunge, 0.0F, 0.0F, 40.0F * lunge, 0.0F, 0.0F);
            Gaits.fall(body, pose, 14.0F, 80.0F);
        }
    }
}
