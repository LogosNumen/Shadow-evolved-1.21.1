package net.logosnumen.nightmareevolved.client.colossus;

import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
import net.minecraft.util.Mth;

/**
 * The Gravewyrm: a plated head with rows of eyes down its sides, and one body segment and a tail the renderer strings
 * out behind it along the path its head has taken. Variants differ in hide, in hooked mandibles, tusks or a glowing
 * ringed maw, in the number of eyes, and in whether the segments carry back plates, spines or fins.
 */
public final class GravewyrmModel extends ColossusModel {
    /** Model units between segment centres (before the drawn doubling). */
    public static final float SEGMENT = 9.0F;
    private static final Look[] LOOKS = {
        new Look(0xB8B2A4, 0x4A4640, 0xDCD6C8, 0x8A8478, 0xE8E2D2, 0x5AEBD2, 0x9A948A),
        new Look(0x3E5A32, 0x16220F, 0x6A8A52, 0x8A9A5A, 0xC8D0A0, 0x9CFF5A, 0x2A4A22),
        new Look(0x6A1E22, 0x24080A, 0x9A3A3A, 0xB06A50, 0xE0C8B0, 0xFF4A4A, 0x4A1416),
        new Look(0x4A4642, 0x1A1816, 0x6E6A64, 0x8A7A64, 0xA89A84, 0xFFA845, 0x2E2A26),
        new Look(0x1A1E2E, 0x06080E, 0x2E3A5A, 0x2A3048, 0x8A8AB0, 0xB070FF, 0x24284A)
    };

    @Override
    public Look look(ColossusVariant variant) {
        return LOOKS[variant.palette()];
    }

    @Override
    public Rig build(ColossusVariant v) {
        int mouth = v.pick("mouth", 3);
        int eyes = 1 + v.pick("eyes", 3);
        Rig rig = new Rig();
        rig.bone("root", null, 0.0F, 24.0F, 0.0F);
        rig.bone("head", "root", 0.0F, -12.0F, 0.0F);
        rig.box("head", Material.SCALE, -8, -8, -12, 16, 15, 16);
        rig.box("head", Material.SCALE, -6, -5, -21, 12, 9, 9);
        rig.box("head", Material.PLATE, -3, -10, -13, 6, 2, 15);
        for (int i = 0; i < eyes; i++) {
            float z = -11 + i * 3.5F;
            rig.box("head", Material.EYE, -8.4F, -5, z, 1, 2, 2);
            rig.box("head", Material.EYE, 7.4F, -5, z, 1, 2, 2);
        }

        if (v.elder() || v.chance("crest", 0.5F)) {
            for (int i = 0; i < 3; i++) {
                rig.box("head", Material.HORN, -1, -14 + i, -10 + i * 4, 2, 5 - i, 2);
            }
        }

        rig.bone("jaw", "head", 0.0F, 4.0F, -10.0F);
        rig.box("jaw", Material.SCALE, -6, 0, -12, 12, 4, 12);
        rig.box("jaw", Material.BELLY, -5, 4, -11, 10, 1, 10);
        for (float x = -5; x < 5.1F; x += 2.5F) {
            rig.box("jaw", Material.BONE, x, -1.5F, -11.5F, 1, 2, 1);
            rig.box("head", Material.BONE, x, 3, -20.5F, 1, 2, 1);
        }

        switch (mouth) {
            case 0 -> {
                rig.pairBone("mand", "head", -7.0F, 3.0F, -17.0F, 0.0F, 20.0F, 0.0F);
                rig.pair("mand", Material.CLAW, -1.5F, -1.5F, -10, 3, 3, 10);
                rig.pairBone("mand2", "mand", 0.0F, 0.0F, -9.0F, 0.0F, -55.0F, 0.0F);
                rig.pair("mand2", Material.CLAW, -1, -1, -7, 2, 2, 7);
            }
            case 1 -> {
                rig.pairBone("tusk", "jaw", -5.0F, 1.0F, -10.0F, 40.0F, 15.0F, 0.0F);
                rig.pair("tusk", Material.HORN, -1.5F, -1.5F, -12, 3, 3, 12);
            }
            default -> {
                rig.box("head", Material.GLOW, -4, -3, -21.4F, 8, 6, 1);
                for (int i = 0; i < 8; i++) {
                    float a = i * Mth.TWO_PI / 8;
                    rig.box("head", Material.BONE, Mth.cos(a) * 5 - 0.5F, Mth.sin(a) * 4 - 0.5F, -21.8F, 1, 1, 1);
                }
            }
        }

        rig.bone("segment", "root", 0.0F, -7.0F, 0.0F);
        rig.box("segment", Material.SCALE, -7, -7, -5, 14, 13, 10);
        rig.box("segment", Material.BELLY, -6, 6, -5, 12, 1, 10);
        if (v.chance("plates", 0.6F)) {
            rig.box("segment", Material.PLATE, -5, -9, -4, 10, 2, 8);
        }

        if (v.chance("spines", 0.5F)) {
            rig.box("segment", Material.HORN, -1, -13, -1, 2, 5, 2);
        }

        if (v.chance("fins", 0.4F)) {
            rig.box("segment", Material.MEMBRANE, -15, -2, -4, 8, 0, 8);
            rig.box("segment", Material.MEMBRANE, 7, -2, -4, 8, 0, 8);
        }

        rig.bone("tail", "root", 0.0F, -5.0F, 0.0F);
        rig.box("tail", Material.SCALE, -4, -4, -6, 8, 8, 12);
        rig.box("tail", Material.HORN, -1, -1, 6, 2, 2, 9);
        return rig;
    }

    @Override
    public void animate(Rig.Baked body, ColossusPose p, ColossusVariant v) {
        // The segments and tail are drawn by the renderer along the trail, not with the head.
        body.part("segment").visible = false;
        body.part("tail").visible = false;
        float yaw = Mth.clamp(p.headYaw(), -40.0F, 40.0F);
        float look = Mth.clamp(p.headPitch(), -30.0F, 30.0F);
        float rear = p.key("lunge", 0.0F, 0.0F, 0.33F, 1.0F, 0.4F, 0.0F, 1.0F, 0.0F);
        float strike = p.key("lunge", 0.35F, 0.0F, 0.45F, 1.0F, 0.65F, 1.0F, 1.0F, 0.0F);
        float bite = p.key("lunge", 0.3F, 0.0F, 0.42F, 1.0F, 0.55F, 0.0F, 1.0F, 0.0F);
        float spit = p.key("spit", 0.0F, 0.0F, 0.4F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
        float dive = p.key("burrow", 0.0F, 0.0F, 0.2F, 1.0F, 0.72F, 1.0F, 0.76F, -1.0F, 0.95F, -1.0F, 1.0F, 0.0F);
        float thrash = p.key("thrash", 0.0F, 0.0F, 0.2F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        float pitch = look * 0.5F - 28.0F * rear + 18.0F * strike - 30.0F * spit + 55.0F * dive;
        float turn = yaw * 0.6F + p.wave(0.05F, 0.0F) * 6.0F + Mth.sin(p.age() * 0.9F) * 35.0F * thrash;
        Bones.turn(body.part("head"), pitch + p.wave(0.07F, 1.0F) * 3.0F, turn, p.wave(0.04F, 2.0F) * 4.0F);
        Bones.shift(body.part("head"), 0.0F, -6.0F * rear - 4.0F * spit, 4.0F * rear - 6.0F * strike);
        float open = 5.0F + p.wave(0.1F, 0.0F) * 3.0F + 40.0F * bite + 45.0F * spit + 30.0F * Math.max(0.0F, -dive);
        Bones.turn(body.part("jaw"), open, 0.0F, 0.0F);
        Bones.turnPair(body, "mand", 0.0F, -25.0F * bite + 10.0F * p.wave(0.15F, 0.0F), 0.0F);
        Bones.turnPair(body, "tusk", -15.0F * bite, 0.0F, 0.0F);
        Bones.shift(body.part("root"), 0.0F, 12.0F * p.fall(0.0F, 1.0F), 0.0F);
        Bones.turn(body.part("head"), 30.0F * p.fall(0.0F, 0.6F), 0.0F, 0.0F);
    }

    /** How far the body sways side to side at a segment (blocks before scaling), for the renderer. */
    public static float sway(ColossusPose p, int segment) {
        float thrash = p.key("thrash", 0.0F, 0.0F, 0.2F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        return Mth.sin(p.age() * (0.12F + thrash * 0.5F) - segment * 0.55F) * (0.18F + thrash * 0.6F);
    }
}
