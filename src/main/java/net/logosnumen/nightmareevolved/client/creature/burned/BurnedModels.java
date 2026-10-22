package net.logosnumen.nightmareevolved.client.creature.burned;

import net.logosnumen.nightmareevolved.client.creature.CreatureModel;
import net.logosnumen.nightmareevolved.client.creature.Gaits;
import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;

/** The Burned Forest's millipedes: the tribe, its painted elders and a queen. */
public final class BurnedModels {
    private BurnedModels() {
    }

    /**
     * A millipede of the Black Millipede tribe: a long chain of armoured segments, a pair of legs on each, two feelers,
     * and at the front a mouth too much like a person's, lipped and full of needle fangs.
     */
    public static class Millipede extends CreatureModel {
        private static final Look BLACK = new Look(0x141416, 0x050506, 0x2E2E34, 0x8A3A3A, 0xE8E0D0, 0xFF5030, 0x1E1E22);
        private final int segments;
        private final float width;

        public Millipede() {
            this(9, 9.0F);
        }

        protected Millipede(int segments, float width) {
            this.segments = segments;
            this.width = width;
        }

        @Override
        public Look look(int look) {
            return BLACK;
        }

        @Override
        public Rig build(int look) {
            Rig rig = new Rig();
            float w = this.width;
            float h = w * 0.7F;
            int leg = Math.round(w * 0.45F);
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, -h * 0.5F - 3.0F, -this.segments * 3.0F);
            String parent = "body";
            for (int s = 0; s < this.segments; s++) {
                String seg = "seg" + s;
                rig.bone(seg, parent, 0.0F, 0.0F, s == 0 ? 0.0F : 6.0F);
                float taper = s >= this.segments - 2 ? 0.75F : 1.0F;
                box(rig, seg, Material.PLATE, -w * taper / 2.0F, -h * taper / 2.0F, 0.0F, w * taper, h * taper, 6.0F);
                box(rig, seg, Material.SCALE, -w * taper / 2.0F - 0.3F, -h * taper / 2.0F - 0.3F, 4.8F, w * taper + 0.6F, h * taper * 0.6F, 1.2F);
                this.armour(rig, seg, s, w * taper, h * taper);
                rig.pairBone("leg" + s, seg, -w * taper / 2.0F, h * taper / 2.0F - 1.0F, 3.0F, 0.0F, 0.0F, 35.0F);
                rig.pair("leg" + s, Material.SCALE, -leg, -0.5F, -0.5F, leg, 1, 1);
                rig.pair("leg" + s, Material.CLAW, -leg - 1.0F, -0.5F, -0.5F, 1, 3, 1);
                parent = seg;
            }

            rig.bone("head", "seg0", 0.0F, 0.0F, 0.0F);
            box(rig, "head", Material.PLATE, -w * 0.45F, -h * 0.45F, -4.0F, w * 0.9F, h * 0.8F, 4.0F);
            both(rig, "head", Material.EYE, -w * 0.3F, -h * 0.3F, -4.4F, 1.0F, 1.0F, 1.0F);
            // The lips and the needle fangs inside them.
            box(rig, "head", Material.BELLY, -w * 0.3F, h * 0.05F, -4.6F, w * 0.6F, 1.0F, 1.0F);
            rig.bone("jaw", "head", 0.0F, h * 0.3F, -1.0F);
            box(rig, "jaw", Material.BELLY, -w * 0.3F, 0.0F, -3.6F, w * 0.6F, 1.5F, 3.6F);
            int fangs = Math.max(4, (int)(w * 0.6F));
            for (int f = 0; f < fangs; f++) {
                float fx = -w * 0.28F + f * (w * 0.56F / (fangs - 1)) - 0.25F;
                box(rig, "head", Material.HORN, fx, h * 0.05F + 0.8F, -4.3F, 0.5F, 1.4F, 0.5F);
                box(rig, "jaw", Material.HORN, fx, -1.2F, -3.4F, 0.5F, 1.4F, 0.5F);
            }

            rig.pairBone("feeler", "head", -w * 0.25F, -h * 0.4F, -3.0F, -30.0F, -25.0F, 0.0F);
            rig.pair("feeler", Material.SCALE, -0.5F, -0.5F, -w, 1, 1, (int)w);
            return rig;
        }

        /** What a segment wears beyond the plain shell. */
        protected void armour(Rig rig, String seg, int index, float w, float h) {
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.sway(body, pose, "seg", this.segments, 7.0F, 0.35F + 0.5F * pose.stride(), 0.55F);
            Gaits.sideLegs(body, pose, this.segments, 25.0F, 18.0F, 0.9F);
            Gaits.pair(body, "feeler", pose.wave(0.2F, 0.0F) * 10.0F, pose.wave(0.15F, 1.0F) * 10.0F, 0.0F, pose.wave(0.2F, 2.0F) * 10.0F,
                pose.wave(0.15F, 3.0F) * 10.0F, 0.0F);
            float bite = pose.key("bite", 0.0F, 0.0F, 0.4F, -1.0F, 0.6F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("jaw"), 50.0F * Math.max(0.0F, -bite), 0.0F, 0.0F);
            Bones.turn(body.part("head"), -20.0F * Math.max(0.0F, -bite) + 15.0F * Math.max(0.0F, bite), 0.0F, 0.0F);
            // Rearing: the front of it lifts high and comes crashing down.
            float rear = pose.key("rear", 0.0F, 0.0F, 0.5F, 1.0F, 0.62F, -0.3F, 1.0F, 0.0F);
            rear += pose.key("crush", 0.0F, 0.0F, 0.5F, 1.0F, 0.62F, -0.3F, 1.0F, 0.0F);
            Bones.turn(body.part("seg0"), -35.0F * rear, 0.0F, 0.0F);
            Bones.turn(body.part("seg1"), -20.0F * rear, 0.0F, 0.0F);
            Bones.turn(body.part("seg2"), 25.0F * Math.max(0.0F, rear), 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), 30.0F * Math.max(0.0F, rear), 0.0F, 0.0F);
            float rush = pose.key("rush", 0.0F, 0.0F, 0.15F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
            Bones.shift(body.part("body"), 0.0F, 1.5F * rush, 0.0F);
            float call = pose.key("call", 0.0F, 0.0F, 0.3F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            Bones.turn(body.part("seg0"), -25.0F * call, 0.0F, 0.0F);
            Bones.turn(body.part("jaw"), 40.0F * call, 0.0F, 0.0F);
            Gaits.fall(body, pose, 0.0F, 180.0F);
        }
    }

    /** An elder of the tribe, larger, its shell crimson, azure or gold. Built at the tribe's size and drawn larger. */
    public static final class Painted extends Millipede {
        private static final Look[] LOOKS = {
            new Look(0x9A1A20, 0x3A0608, 0xD84A40, 0x8A3A3A, 0xE8E0D0, 0xFFD040, 0xC8302A),
            new Look(0x1A4A9A, 0x081A3A, 0x4A8AD8, 0x8A3A3A, 0xE8E0D0, 0xFFD040, 0x2A6AC8),
            new Look(0xB08A1A, 0x4A3A08, 0xE8C84A, 0x8A3A3A, 0xE8E0D0, 0xFF5030, 0xD8A82A)
        };

        public Painted() {
            super(11, 9.0F);
        }

        @Override
        public Look look(int look) {
            return LOOKS[Math.floorMod(look, LOOKS.length)];
        }

        @Override
        public float renderScale() {
            return 1.6F;
        }

        @Override
        protected void armour(Rig rig, String seg, int index, float w, float h) {
            // A ridge of bright spines down its back.
            box(rig, seg, Material.HORN, -0.5F, -h / 2.0F - 2.0F, 2.0F, 1.0F, 2.0F, 2.0F);
        }
    }

    /**
     * A Millipede Queen: vast and long, plated in thick chitin with ridges along its edges, a crest of plates over the
     * head. Built at a size and drawn much larger.
     */
    public static final class Queen extends Millipede {
        private static final Look LOOK = new Look(0x0A0A0C, 0x020203, 0x3A4A5A, 0x6A2A30, 0xE8E0D0, 0x7AD0FF, 0x1A2A3A);

        public Queen() {
            super(13, 14.0F);
        }

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public float renderScale() {
            return 3.2F;
        }

        @Override
        protected void armour(Rig rig, String seg, int index, float w, float h) {
            // Thick ridged plates over each segment, flared at the edges.
            box(rig, seg, Material.PLATE, -w / 2.0F - 1.0F, -h / 2.0F - 1.5F, 0.5F, w + 2.0F, 2.0F, 5.0F);
            box(rig, seg, Material.HORN, -w / 2.0F - 2.0F, -h / 2.0F - 1.0F, 2.0F, 1.5F, 1.5F, 2.0F);
            box(rig, seg, Material.HORN, w / 2.0F + 0.5F, -h / 2.0F - 1.0F, 2.0F, 1.5F, 1.5F, 2.0F);
            if (index == 0) {
                box(rig, seg, Material.PLATE, -w / 2.0F, -h / 2.0F - 6.0F, -1.0F, w, 5.0F, 2.0F);
            }
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            super.animate(body, pose, look);
            // Bringing forth her brood: the hind segments swell and heave.
            float brood = pose.key("brood", 0.0F, 0.0F, 0.3F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
            for (int s = 9; s < 13; s++) {
                Bones.size(body.part("seg" + s), 1.0F + 0.15F * brood * (1.0F + pose.wave(0.5F, s)));
            }
        }
    }
}
