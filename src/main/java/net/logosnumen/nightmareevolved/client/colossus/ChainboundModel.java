package net.logosnumen.nightmareevolved.client.colossus;

import net.logosnumen.nightmareevolved.client.rig.Bones;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
import net.logosnumen.nightmareevolved.entity.colossus.ChainboundColossusEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Chainbound Colossus: plate armour over a body of nothing, two lights in the visor's slit, a rune burning on the
 * breastplate, and chains wound about it and hanging from its wrists. Variants differ in the metal, in a greatsword,
 * warhammer or halberd, in a horned, crested, crowned or great helm, in a shield on the left arm, a cape, and spiked
 * pauldrons.
 */
public final class ChainboundModel extends ColossusModel {
    private static final Look[] LOOKS = {
        new Look(0x3A2A5A, 0x120C1E, 0x5A4A7A, 0x2A2A2A, 0x6A6E78, 0x8AD8FF, 0x9AA0AC),
        new Look(0x6A2A1A, 0x2A0E08, 0x8A4A30, 0x3A2A1A, 0x6A4A2A, 0xFFA845, 0xB08040),
        new Look(0x2A1A1A, 0x0A0606, 0x4A3030, 0x1A1A1A, 0x3A3A40, 0xB070FF, 0x3A3A42),
        new Look(0xE8E0D0, 0x8A8070, 0xFFFFFF, 0x3A3020, 0xA08A4A, 0xFFE070, 0xD8B050),
        new Look(0x1A1A1A, 0x080808, 0x3A3A3A, 0x2A1010, 0x5A2A2A, 0xFF4A4A, 0x8A2A2A)
    };

    @Override
    public Look look(ColossusVariant variant) {
        return LOOKS[variant.palette()];
    }

    @Override
    public Rig build(ColossusVariant v) {
        Rig rig = new Rig();
        rig.bone("root", null, 0.0F, 24.0F, 0.0F);
        rig.bone("hips", "root", 0.0F, -30.0F, 0.0F);
        rig.box("hips", Material.VOID, -8, -3, -5, 16, 8, 10);
        rig.box("hips", Material.PLATE, -9, -2, -6, 18, 5, 12);
        rig.box("hips", Material.PLATE, -8, 3, -6.5F, 7, 8, 1);
        rig.box("hips", Material.PLATE, 1, 3, -6.5F, 7, 8, 1);
        rig.pairBone("leg", "hips", -5.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg", Material.VOID, -3.5F, -1, -3.5F, 7, 13, 7);
        rig.pair("leg", Material.PLATE, -4, 1, -4.5F, 8, 10, 2);
        rig.pairBone("leg2", "leg", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg2", Material.PLATE, -3.5F, 0.5F, -3.5F, 7, 10, 7);
        rig.pair("leg2", Material.PLATE, -4, -1.5F, -4.5F, 8, 4, 3);
        rig.pair("leg2", Material.HORN, -1, -1, -6.5F, 2, 2, 2);
        rig.pairBone("leg3", "leg2", 0.0F, 11.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg3", Material.PLATE, -4, 0, -6, 8, 4, 10);

        rig.bone("torso", "hips", 0.0F, -2.0F, 0.0F, 3.0F, 0.0F, 0.0F);
        rig.box("torso", Material.VOID, -10, -26, -6, 20, 26, 12);
        rig.box("torso", Material.PLATE, -11, -26, -7, 22, 17, 14);
        rig.box("torso", Material.PLATE, -9, -9, -6.5F, 18, 8, 13);
        rig.box("torso", Material.CLAW, -10, -10, -7.2F, 20, 2, 14);
        rig.box("torso", Material.RUNE, -3, -22, -7.6F, 6, 6, 1);
        rig.bone("chains", "torso", 0.0F, -16.0F, 0.0F, 0.0F, 0.0F, 32.0F);
        for (int k = 0; k < 12; k++) {
            boolean flat = k % 2 == 0;
            float x = -13 + k * 2.2F;
            rig.box("chains", Material.CLAW, x, flat ? -1 : -0.5F, -7.8F, 3, flat ? 2 : 1, 1);
            rig.box("chains", Material.CLAW, x, flat ? -1 : -0.5F, 7.0F, 3, flat ? 2 : 1, 1);
        }

        this.helm(rig, v);
        this.arms(rig, v);
        if (v.elder() || v.chance("cape", 0.6F)) {
            rig.bone("cape", "torso", 0.0F, -25.0F, 7.5F, 4.0F, 0.0F, 0.0F);
            rig.box("cape", Material.CLOTH, -10, 0, 0, 20, 40, 1);
        }

        return rig;
    }

    private void helm(Rig rig, ColossusVariant v) {
        rig.bone("head", "torso", 0.0F, -26.0F, -1.0F);
        rig.box("head", Material.PLATE, -6, -11, -6, 12, 11, 12);
        rig.box("head", Material.VOID, -4, -6, -6.4F, 8, 1, 1);
        rig.box("head", Material.EYE, -3, -6, -6.6F, 2, 1, 1);
        rig.box("head", Material.EYE, 1, -6, -6.6F, 2, 1, 1);
        switch (v.pick("helm", 4)) {
            case 0 -> {
                rig.pairBone("horn", "head", -6.0F, -8.0F, 0.0F, -10.0F, 0.0F, 40.0F);
                rig.pair("horn", Material.HORN, -1.5F, -8, -1.5F, 3, 8, 3);
                rig.pairBone("horn2", "horn", 0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -50.0F);
                rig.pair("horn2", Material.HORN, -1, -7, -1, 2, 7, 2);
            }
            case 1 -> {
                rig.box("head", Material.PLATE, -1, -15, -6, 2, 4, 12);
                rig.box("head", Material.CLOTH, -1, -18, -2, 2, 4, 14);
            }
            case 2 -> {
                for (int i = 0; i < 6; i++) {
                    float a = i * Mth.TWO_PI / 6;
                    rig.box("head", i % 2 == 0 ? Material.HORN : Material.GLOW, Mth.cos(a) * 5 - 0.5F, -15, Mth.sin(a) * 5 - 0.5F, 1, 4, 1);
                }
            }
            default -> {
                rig.box("head", Material.PLATE, -6.5F, -12, -6.5F, 13, 2, 13);
                for (int i = 0; i < 4; i++) {
                    rig.box("head", Material.VOID, -3 + i * 2, -3, -6.3F, 1, 1, 1);
                }
            }
        }
    }

    private void arms(Rig rig, ColossusVariant v) {
        boolean spiked = v.chance("spikes", 0.5F);
        rig.pairBone("arm", "torso", -14.0F, -23.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("arm", Material.PLATE, -6, -5, -7, 12, 8, 14);
        if (spiked) {
            rig.pair("arm", Material.HORN, -2, -10, -1, 2, 5, 2);
            rig.pair("arm", Material.HORN, -5, -8, -1, 2, 3, 2);
        }

        rig.pair("arm", Material.VOID, -3.5F, 2, -3.5F, 7, 14, 7);
        rig.pair("arm", Material.PLATE, -4, 4, -4, 8, 10, 8);
        rig.pairBone("fore", "arm", 0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("fore", Material.VOID, -3, 0, -3, 6, 12, 6);
        rig.pair("fore", Material.PLATE, -3.5F, 1, -3.5F, 7, 10, 7);
        for (int j = 0; j < 4; j++) {
            boolean flat = j % 2 == 0;
            rig.pair("fore", Material.CLAW, flat ? -1 : -0.5F, 10 + j * 2.2F, 3.5F, flat ? 2 : 1, 3, 1);
        }

        rig.pairBone("hand", "fore", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("hand", Material.PLATE, -4, 0, -4, 8, 7, 8);
        rig.bone("weapon", "hand_r", 0.0F, 4.0F, 0.0F, 60.0F, 0.0F, 0.0F);
        switch (ChainboundColossusEntity.weapon(v)) {
            case ChainboundColossusEntity.GREATSWORD -> {
                rig.box("weapon", Material.CLAW, -1, -1, -2, 2, 2, 8);
                rig.box("weapon", Material.PLATE, -6, -1.5F, -4, 12, 3, 2);
                rig.box("weapon", Material.PLATE, -2.5F, -1, -46, 5, 2, 42);
                rig.box("weapon", Material.GLOW, -0.5F, -1.4F, -42, 1, 1, 36);
                rig.box("weapon", Material.PLATE, -1.5F, -1, -49, 3, 2, 3);
            }
            case ChainboundColossusEntity.WARHAMMER -> {
                rig.box("weapon", Material.CLAW, -1, -1, -40, 2, 2, 46);
                rig.box("weapon", Material.PLATE, -6, -4, -47, 12, 8, 8);
                rig.box("weapon", Material.HORN, -1, -1, -51, 2, 2, 4);
                rig.box("weapon", Material.RUNE, -6.4F, -2, -45, 1, 4, 4);
                rig.box("weapon", Material.RUNE, 5.4F, -2, -45, 1, 4, 4);
            }
            default -> {
                rig.box("weapon", Material.CLAW, -1, -1, -56, 2, 2, 62);
                rig.box("weapon", Material.PLATE, 1, -0.5F, -53, 10, 1, 11);
                rig.box("weapon", Material.GLOW, 10, -0.7F, -52, 1, 1, 9);
                rig.box("weapon", Material.HORN, -1, -1, -63, 2, 2, 7);
                rig.box("weapon", Material.CLAW, -5, -0.5F, -51, 4, 1, 3);
            }
        }

        if (ChainboundColossusEntity.shield(v)) {
            rig.bone("shield", "fore_l", 3.5F, 5.0F, 0.0F);
            rig.box("shield", Material.PLATE, 0, -12, -8, 2, 22, 16);
            rig.box("shield", Material.RUNE, 1.6F, -6, -4, 1, 10, 8);
        }
    }

    @Override
    public void animate(Rig.Baked body, Pose p, ColossusVariant v) {
        float walk = p.gait(0.45F, 0.0F);
        float yaw = Mth.clamp(p.headYaw(), -50.0F, 50.0F);
        float look = Mth.clamp(p.headPitch(), -40.0F, 40.0F);
        ModelPart root = body.part("root");
        Bones.shift(root, 0.0F, -Math.abs(walk) * 1.2F, 0.0F);
        Bones.turn(body.part("torso"), p.wave(0.05F, 0.0F) * 0.8F, walk * 4.0F, 0.0F);
        Bones.turn(body.part("leg_r"), walk * 22.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg_l"), -walk * 22.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg2_r"), Math.max(0.0F, -walk) * 20.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg2_l"), Math.max(0.0F, walk) * 20.0F, 0.0F, 0.0F);
        // At rest the weapon is carried low and forward, the off arm swinging with the stride.
        Bones.turn(body.part("arm_r"), -18.0F - walk * 6.0F, 0.0F, 6.0F);
        Bones.turn(body.part("fore_r"), -35.0F, 0.0F, 0.0F);
        Bones.turn(body.part("arm_l"), walk * 14.0F, 0.0F, -6.0F);
        Bones.turn(body.part("fore_l"), -12.0F, 0.0F, 0.0F);
        Bones.turn(body.part("head"), look * 0.5F, yaw * 0.7F, 0.0F);
        Bones.turn(body.part("cape"), 3.0F + p.stride() * 15.0F + p.wave(0.1F, 0.0F) * 3.0F, 0.0F, 0.0F);

        // Cleave: both hands take the weapon high overhead and bring it straight down into the ground ahead.
        float lift = p.key("cleave", 0.0F, 0.0F, 0.52F, 1.0F, 0.6F, 0.0F, 1.0F, 0.0F);
        float chop = p.key("cleave", 0.52F, 0.0F, 0.6F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
        Bones.turnPair(body, "arm", -150.0F * lift - 45.0F * chop, -15.0F * (lift + chop), 0.0F);
        Bones.turn(body.part("torso"), -12.0F * lift + 28.0F * chop, 0.0F, 0.0F);
        Bones.turnPair(body, "leg", -20.0F * chop, 0.0F, 0.0F);
        Bones.turnPair(body, "leg2", 30.0F * chop, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, 3.0F * chop, 0.0F);

        // Sweep: wound back to the right, the weapon swings round through a half circle.
        float wind = p.key("sweep", 0.0F, 0.0F, 0.45F, 1.0F, 0.53F, 0.0F, 1.0F, 0.0F);
        float swing = p.key("sweep", 0.45F, 0.0F, 0.58F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("arm_r"), -70.0F * (wind + swing), 0.0F, 40.0F * wind);
        Bones.turn(body.part("torso"), 0.0F, 60.0F * wind - 70.0F * swing, 0.0F);

        // Hook: the off hand flings the chain out at full stretch, then hauls it back in.
        float fling = p.key("hook", 0.0F, 0.0F, 0.41F, 1.0F, 0.5F, 1.0F, 0.6F, 0.0F, 1.0F, 0.0F);
        float haul = p.key("hook", 0.48F, 0.0F, 0.6F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("arm_l"), -95.0F * fling - 45.0F * haul, 0.0F, 0.0F);
        Bones.turn(body.part("fore_l"), -70.0F * haul, 0.0F, 0.0F);
        Bones.turn(body.part("torso"), 0.0F, -20.0F * fling + 15.0F * haul, 0.0F);

        // Stab: raised high, the weapon is driven point-first into the earth; the knees bend into it.
        float raise = p.key("stab", 0.0F, 0.0F, 0.52F, 1.0F, 0.59F, 0.0F, 1.0F, 0.0F);
        float drive = p.key("stab", 0.52F, 0.0F, 0.6F, 1.0F, 0.88F, 1.0F, 1.0F, 0.0F);
        Bones.turnPair(body, "arm", -175.0F * raise - 55.0F * drive, -20.0F * (raise + drive), 0.0F);
        Bones.turn(body.part("weapon"), -60.0F * drive, 0.0F, 0.0F);
        Bones.turn(body.part("torso"), -10.0F * raise + 30.0F * drive, 0.0F, 0.0F);
        Bones.turnPair(body, "leg", -35.0F * drive, 0.0F, 0.0F);
        Bones.turnPair(body, "leg2", 55.0F * drive, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, 6.0F * drive, 0.0F);

        // Bash: the shield arm drives forward with the shoulder behind it.
        float bash = p.key("bash", 0.0F, 0.0F, 0.35F, -0.4F, 0.5F, 1.0F, 0.7F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("arm_l"), -80.0F * Math.max(0.0F, bash) + 30.0F * Math.max(0.0F, -bash), 0.0F, 0.0F);
        Bones.turn(body.part("torso"), 0.0F, -25.0F * bash, 0.0F);

        // Death: down onto both knees, the weapon falling from the hand, then forward onto its face.
        float kneel = p.fall(0.0F, 0.45F);
        float fall = p.fall(0.45F, 1.0F);
        Bones.turnPair(body, "leg", -80.0F * kneel, 0.0F, 0.0F);
        Bones.turnPair(body, "leg2", 95.0F * kneel, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, 13.0F * kneel, 0.0F);
        Bones.turn(root, 80.0F * fall, 0.0F, 0.0F);
        Bones.turn(body.part("weapon"), 50.0F * kneel, 0.0F, 30.0F * kneel);
        Bones.turnPair(body, "arm", 10.0F * kneel - 30.0F * fall, 0.0F, 10.0F * kneel);
        Bones.turn(body.part("head"), 30.0F * kneel, 0.0F, 0.0F);
    }
}
