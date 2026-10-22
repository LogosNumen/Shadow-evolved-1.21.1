package net.logosnumen.nightmareevolved.client.colossus;

import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Ossuary Behemoth: the skeleton of a four-legged beast, every rib open to the air, a heart of light still beating
 * inside the cage. Variants differ in the colour of their bones and light, in antlers, ram's horns or forward spikes,
 * in how tall the spines stand along the back, in a club or a spike at the end of the tail, and, rarely, in a second
 * head on a second neck.
 */
public final class OssuaryModel extends ColossusModel {
    private static final Look[] LOOKS = {
        new Look(0xCFC8B4, 0x4A463C, 0xEDE8D8, 0x8A8270, 0xE6DFCC, 0x5AEBD2, 0x2E7A6E),
        new Look(0x8E8A82, 0x3A3630, 0xB4B0A6, 0x6A665E, 0xA8A39A, 0xFFA845, 0x8A5A2A),
        new Look(0x2A2630, 0x0E0C12, 0x4A4458, 0x1E1A24, 0x34303C, 0xB070FF, 0x5A3E8A),
        new Look(0xB89890, 0x4A1A1A, 0xD8BEB4, 0x7A5A54, 0xC9A49A, 0xFF4A4A, 0x8A1E1E),
        new Look(0xC8D8E0, 0x3A4A5A, 0xEAF4F8, 0x8AA0B0, 0xD8E8F0, 0x8AD8FF, 0x3A7AA8)
    };

    @Override
    public Look look(ColossusVariant variant) {
        return LOOKS[variant.palette()];
    }

    private static int heads(ColossusVariant v) {
        return v.elder() || v.chance("twin", 0.1F) ? 2 : 1;
    }

    @Override
    public Rig build(ColossusVariant v) {
        int spine = 4 + v.pick("spines", 6);
        int horns = v.pick("horns", 3);
        Rig rig = new Rig();
        rig.bone("root", null, 0.0F, 24.0F, 0.0F);
        rig.bone("spine", "root", 0.0F, -30.0F, 0.0F);
        rig.box("spine", Material.BONE, -2, -2, -20, 4, 4, 40);
        for (int z = -16; z <= 16; z += 5) {
            int length = spine - Math.abs(z) / 6;
            rig.box("spine", Material.BONE, -1, -2 - length, z, 2, length, 2);
        }

        for (int i = 0; i < 6; i++) {
            float z = -14 + i * 5;
            rig.box("spine", Material.BONE, -8, -1, z, 6, 2, 2);
            rig.box("spine", Material.BONE, 2, -1, z, 6, 2, 2);
            rig.box("spine", Material.BONE, -10, 1, z, 2, 10, 2);
            rig.box("spine", Material.BONE, 8, 1, z, 2, 10, 2);
            rig.box("spine", Material.BONE, -6, 11, z, 12, 2, 2);
        }

        rig.box("spine", Material.BONE, -8, -3, 14, 16, 6, 8);
        rig.box("spine", Material.BONE, -11, -3, -17, 5, 9, 8);
        rig.box("spine", Material.BONE, 6, -3, -17, 5, 9, 8);
        rig.bone("heart", "spine", 0.0F, 6.0F, -5.0F);
        rig.box("heart", Material.GLOW, -3, -3, -3, 6, 6, 6);
        rig.box("heart", Material.VEIN, -4, -2, -2, 8, 4, 4);

        int heads = heads(v);
        for (int h = 0; h < heads; h++) {
            this.neckAndHead(rig, v, h, heads, horns);
        }

        this.leg(rig, "leg_f", -9.0F, -14.0F);
        this.leg(rig, "leg_b", -8.0F, 16.0F);
        String parent = "spine";
        for (int i = 0; i < 6; i++) {
            String name = "tail" + i;
            rig.bone(name, parent, 0.0F, i == 0 ? -1.0F : 0.0F, i == 0 ? 21.0F : 6.0F, i == 0 ? -20.0F : 6.0F, 0.0F, 0.0F);
            int w = Math.max(1, 3 - i / 2);
            rig.box(name, Material.BONE, -w / 2.0F, -w / 2.0F, 0, w, w, 6);
            rig.box(name, Material.BONE, -0.5F, -w / 2.0F - 2, 2, 1, 2, 1);
            parent = name;
        }

        if (v.chance("club", 0.5F)) {
            rig.box(parent, Material.BONE, -2.5F, -2.5F, 5, 5, 5, 5);
            rig.box(parent, Material.CLAW, -3.5F, -0.5F, 7, 1, 1, 1);
            rig.box(parent, Material.CLAW, 2.5F, -0.5F, 7, 1, 1, 1);
        } else {
            rig.box(parent, Material.CLAW, -0.5F, -0.5F, 5, 1, 1, 8);
        }

        return rig;
    }

    /** A neck of three vertebrae and a horned skull; with two heads they fork apart from the shoulders. */
    private void neckAndHead(Rig rig, ColossusVariant v, int h, int heads, int horns) {
        float x = heads == 1 ? 0.0F : (h == 0 ? -6.0F : 6.0F);
        float spread = heads == 1 ? 0.0F : (h == 0 ? 18.0F : -18.0F);
        String parent = "spine";
        for (int i = 0; i < 3; i++) {
            String name = "neck" + h + "_" + i;
            if (i == 0) {
                rig.bone(name, parent, x, 0.0F, -20.0F, -18.0F, spread, 0.0F);
            } else {
                rig.bone(name, parent, 0.0F, 0.0F, -6.0F, -12.0F, 0.0F, 0.0F);
            }

            rig.box(name, Material.BONE, -2.5F, -2.5F, -7, 5, 5, 7);
            rig.box(name, Material.BONE, -0.5F, -6, -4, 1, 4, 2);
            parent = name;
        }

        String head = "head" + h;
        rig.bone(head, parent, 0.0F, 0.0F, -6.0F, 42.0F, 0.0F, 0.0F);
        rig.box(head, Material.BONE, -6, -6, -10, 12, 10, 11);
        rig.box(head, Material.BONE, -4, -3, -18, 8, 7, 8);
        rig.box(head, Material.VOID, -5, -4, -10.3F, 4, 3, 1);
        rig.box(head, Material.VOID, 1, -4, -10.3F, 4, 3, 1);
        rig.box(head, Material.EYE, -4.3F, -3.4F, -10.6F, 2, 2, 1);
        rig.box(head, Material.EYE, 2.3F, -3.4F, -10.6F, 2, 2, 1);
        rig.box(head, Material.VOID, -1.5F, -2, -18.3F, 3, 2, 1);
        String jaw = "jaw" + h;
        rig.bone(jaw, head, 0.0F, 4.0F, -9.0F);
        rig.box(jaw, Material.BONE, -4, 0, -10, 8, 3, 10);
        for (float tx = -3.5F; tx < 3.6F; tx += 2.3F) {
            rig.box(jaw, Material.CLAW, tx, -1, -9.5F, 1, 1, 1);
        }

        String horn = "horn" + h;
        switch (horns) {
            case 0 -> {
                // Antlers: a long tine sweeping back and out, with a branch off it.
                rig.pairBone(horn, head, -5.0F, -6.0F, -3.0F, -30.0F, -25.0F, -35.0F);
                rig.pair(horn, Material.BONE, -1, -12, -1, 2, 12, 2);
                rig.pairBone(horn + "b", horn, 0.0F, -6.0F, 0.0F, 0.0F, 0.0F, -45.0F);
                rig.pair(horn + "b", Material.BONE, -0.5F, -7, -0.5F, 1, 7, 1);
                rig.pairBone(horn + "c", horn, 0.0F, -11.0F, 0.0F, 25.0F, 0.0F, 30.0F);
                rig.pair(horn + "c", Material.BONE, -0.5F, -5, -0.5F, 1, 5, 1);
            }
            case 1 -> {
                // Ram's horns: thick, curling back and round under themselves.
                rig.pairBone(horn, head, -5.0F, -5.0F, -2.0F, 70.0F, -20.0F, -20.0F);
                rig.pair(horn, Material.HORN, -1.5F, -7, -1.5F, 3, 7, 3);
                rig.pairBone(horn + "b", horn, 0.0F, -6.0F, 0.0F, 70.0F, 0.0F, 0.0F);
                rig.pair(horn + "b", Material.HORN, -1.25F, -6, -1.25F, 2, 6, 2);
                rig.pairBone(horn + "c", horn + "b", 0.0F, -5.0F, 0.0F, 70.0F, 0.0F, 0.0F);
                rig.pair(horn + "c", Material.HORN, -1, -5, -1, 2, 5, 2);
            }
            default -> {
                // Spikes: two great points thrust forward like a bull's.
                rig.pairBone(horn, head, -4.5F, -5.0F, -8.0F, -75.0F, -15.0F, 0.0F);
                rig.pair(horn, Material.BONE, -1.5F, -13, -1.5F, 3, 13, 3);
                rig.pairBone(horn + "b", horn, 0.0F, -12.0F, 0.0F, -10.0F, 0.0F, 0.0F);
                rig.pair(horn + "b", Material.CLAW, -1, -5, -1, 2, 5, 2);
            }
        }
    }

    /** A leg of a thigh-bone, a shin and a cloven hoof, long enough to reach the ground from the spine. */
    private void leg(Rig rig, String name, float x, float z) {
        rig.pairBone(name, "spine", x, 4.0F, z, 0.0F, 0.0F, 0.0F);
        rig.pair(name, Material.BONE, -2.5F, -1, -2.5F, 5, 3, 5);
        rig.pair(name, Material.BONE, -2, 0, -2, 4, 13, 4);
        rig.pairBone(name + "2", name, 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair(name + "2", Material.BONE, -2, -1, -2, 4, 3, 4);
        rig.pair(name + "2", Material.BONE, -1.5F, 0, -1.5F, 3, 12, 3);
        rig.pairBone(name + "3", name + "2", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair(name + "3", Material.BONE, -3, 0, -4, 6, 2, 6);
        rig.pair(name + "3", Material.CLAW, -3, 1, -5, 2, 1, 2);
        rig.pair(name + "3", Material.CLAW, 1, 1, -5, 2, 1, 2);
    }

    @Override
    public void animate(Rig.Baked body, Pose p, ColossusVariant v) {
        int heads = heads(v);
        float yaw = Mth.clamp(p.headYaw(), -45.0F, 45.0F);
        float look = Mth.clamp(p.headPitch(), -30.0F, 40.0F);
        ModelPart root = body.part("root");
        ModelPart spine = body.part("spine");
        // The charge runs on its own clock: the legs gallop whatever the stride says.
        float run = p.key("charge", 0.25F, 0.0F, 0.3F, 1.0F, 0.75F, 1.0F, 0.8F, 0.0F);
        float paw = p.key("charge", 0.0F, 0.0F, 0.08F, 1.0F, 0.25F, 1.0F, 0.3F, 0.0F);
        float skid = p.key("charge", 0.74F, 0.0F, 0.8F, 1.0F, 0.92F, 1.0F, 1.0F, 0.0F);
        float stride = Math.max(p.stride(), run);
        float phase = p.walk() * 0.5F * (1.0F - run) + p.age() * 0.6F * run;
        float fr = Mth.cos(phase) * stride;
        float fl = Mth.cos(phase + Mth.PI) * stride;
        float amp = 26.0F + 16.0F * run;
        Bones.turn(body.part("leg_f_r"), fr * amp - 30.0F * skid + Mth.sin(p.age() * 0.8F) * 25.0F * paw, 0.0F, 0.0F);
        Bones.turn(body.part("leg_f_l"), fl * amp - 30.0F * skid, 0.0F, 0.0F);
        Bones.turn(body.part("leg_b_r"), fl * amp + 20.0F * skid, 0.0F, 0.0F);
        Bones.turn(body.part("leg_b_l"), fr * amp + 20.0F * skid, 0.0F, 0.0F);
        Bones.turn(body.part("leg_f2_r"), Math.max(0.0F, -fr) * 30.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg_f2_l"), Math.max(0.0F, -fl) * 30.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg_b2_r"), Math.max(0.0F, fl) * 30.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg_b2_l"), Math.max(0.0F, fr) * 30.0F, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, -Math.abs(fr) * (1.0F + run * 2.0F), 0.0F);
        Bones.turn(spine, p.wave(0.06F, 0.0F) * 0.8F + 6.0F * run - 6.0F * skid, 0.0F, fr * 2.0F);

        // The heart beats, faster in a rage, and gutters out in death.
        float beat = 1.0F + 0.12F * Math.max(0.0F, Mth.sin(p.age() * (p.enraged() ? 0.5F : 0.25F)));
        Bones.size(body.part("heart"), beat * (1.0F - p.fall(0.2F, 0.9F)));

        // Gore: the skull dips low and hooks upward. Rear: the forequarters rise and crash down.
        float dip = p.key("gore", 0.0F, 0.0F, 0.4F, 1.0F, 0.5F, 0.0F, 1.0F, 0.0F);
        float toss = p.key("gore", 0.42F, 0.0F, 0.52F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
        float rise = p.key("rear", 0.0F, 0.0F, 0.5F, 1.0F, 0.6F, 0.0F, 1.0F, 0.0F);
        float crash = p.key("rear", 0.55F, 0.0F, 0.62F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        Bones.turn(spine, -32.0F * rise + 6.0F * crash, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, -6.0F * rise, 0.0F);
        Bones.turnPair(body, "leg_b", 32.0F * rise, 0.0F, 0.0F);
        Bones.turnPair(body, "leg_f", -50.0F * rise - 25.0F * crash, 0.0F, 0.0F);
        Bones.turnPair(body, "leg_f2", -40.0F * rise, 0.0F, 0.0F);

        // Spikes: a stamp of the right forefoot and a bellow down at the ground. Howl: head thrown back to the sky.
        float stamp = p.key("spikes", 0.0F, 0.0F, 0.38F, 1.0F, 0.47F, -0.3F, 0.7F, -0.3F, 1.0F, 0.0F);
        Bones.turn(body.part("leg_f_r"), -55.0F * Math.max(0.0F, stamp), 0.0F, 0.0F);
        float bellow = p.key("spikes", 0.3F, 0.0F, 0.47F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        float howl = p.key("howl", 0.0F, 0.0F, 0.25F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        float lower = 0.6F * paw + 0.5F * run + dip;

        for (int h = 0; h < heads; h++) {
            float side = heads == 1 ? 0.0F : (h == 0 ? 1.0F : -1.0F);
            Bones.turn(body.part("neck" + h + "_0"), 20.0F * lower - 40.0F * toss - 30.0F * howl + 10.0F * bellow + look * 0.3F, yaw * 0.4F + side * p.wave(0.04F, h) * 6.0F, 0.0F);
            Bones.turn(body.part("neck" + h + "_1"), 8.0F * lower - 10.0F * howl, yaw * 0.3F, 0.0F);
            Bones.turn(body.part("head" + h), 15.0F * lower - 25.0F * toss - 20.0F * howl + 15.0F * bellow + look * 0.4F, yaw * 0.3F + 25.0F * toss * (h == 0 ? 1.0F : -1.0F), 0.0F);
            Bones.turn(body.part("jaw" + h), 4.0F + p.wave(0.08F, h) * 2.0F + 35.0F * Math.max(howl, bellow) + 20.0F * toss, 0.0F, 0.0F);
        }

        for (int i = 0; i < 6; i++) {
            Bones.turn(body.part("tail" + i), 3.0F * run - 6.0F * rise, p.wave(0.09F, -i * 0.6F) * (4.0F + i) * (1.0F + stride), 0.0F);
        }

        // Death: the legs fold, the frame drops and rolls onto its side.
        float fold = p.fall(0.0F, 0.5F);
        float roll = p.fall(0.3F, 1.0F);
        Bones.shift(root, 0.0F, 16.0F * fold, 0.0F);
        Bones.turn(spine, 0.0F, 0.0F, 70.0F * roll);
        Bones.turnPair(body, "leg_f", -30.0F * fold, 0.0F, 25.0F * roll);
        Bones.turnPair(body, "leg_b", 30.0F * fold, 0.0F, 25.0F * roll);
        Bones.turnPair(body, "leg_f2", 60.0F * fold, 0.0F, 0.0F);
        Bones.turnPair(body, "leg_b2", -60.0F * fold, 0.0F, 0.0F);
        for (int h = 0; h < heads; h++) {
            Bones.turn(body.part("neck" + h + "_0"), 35.0F * fold, 0.0F, 0.0F);
        }
    }
}
