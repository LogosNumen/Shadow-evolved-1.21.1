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
 * The Cairn Colossus: a hunched giant of piled stone, long arms hanging nearly to the ground, its chest split by a
 * glowing fissure around a core of light. Variants differ in their stone, in one, two or three eyes, crystal growths
 * along the back and shoulders, a crown of horns, a coat of moss, the length of their arms, how far they stoop, and
 * now and then a ruined tower still standing on their back.
 */
public final class CairnModel extends ColossusModel {
    private static final Look[] LOOKS = {
        new Look(0x6E6B66, 0x2E2C2A, 0xA19C94, 0x5E7A3A, 0xC8BFA8, 0xFFA845, 0xFFD27A),
        new Look(0x3A3838, 0x141313, 0x5E5A58, 0x4A4030, 0x2A2626, 0xFF5A1A, 0xFF8A3A),
        new Look(0x5F6658, 0x262A22, 0x8C9480, 0x4E8A2E, 0xB8C0A0, 0x9CFF5A, 0x7FFFB0),
        new Look(0xCDB88A, 0x6E5A38, 0xEADBB4, 0x8A9A4A, 0xFFF0C8, 0xFFE070, 0xFFF2A0),
        new Look(0x221C2E, 0x0A080E, 0x3E3450, 0x2E3A4A, 0x6A5A8A, 0xB070FF, 0xD8A8FF)
    };

    @Override
    public Look look(ColossusVariant variant) {
        return LOOKS[variant.palette()];
    }

    @Override
    public Rig build(ColossusVariant v) {
        int eyes = 1 + v.pick("eyes", 3);
        int crystals = v.pick("crystals", 7) + (v.elder() ? 3 : 0);
        boolean crown = v.elder() || v.chance("crown", 0.4F);
        boolean tower = v.chance("tower", 0.25F);
        boolean moss = v.palette() == 2 || v.chance("moss", 0.3F);
        int fore = 12 + v.pick("arms", 5);
        Rig rig = new Rig();
        rig.bone("root", null, 0.0F, 24.0F, 0.0F);
        rig.bone("hips", "root", 0.0F, -30.0F, 0.0F);
        rig.box("hips", Material.STONE, -9, -3, -6, 18, 8, 12);
        rig.pairBone("leg", "hips", -6.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg", Material.STONE, -4.5F, -1, -4.5F, 9, 13, 9);
        rig.pairBone("leg2", "leg", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg2", Material.STONE, -4, 0, -4, 8, 12, 8);
        rig.pair("leg2", Material.STONE, -4.5F, -1, -5.5F, 9, 4, 2);
        rig.pairBone("leg3", "leg2", 0.0F, 11.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("leg3", Material.STONE, -5, 0, -7, 10, 4, 12);

        rig.bone("torso", "hips", 0.0F, -2.0F, 0.0F, 5.0F + v.roll("hunch") * 18.0F, 0.0F, 0.0F);
        rig.box("torso", Material.STONE, -10, -12, -7, 20, 12, 14);
        rig.box("torso", Material.STONE, -14, -27, -9, 28, 15, 18);
        rig.box("torso", Material.VEIN, -8, -24, -9.6F, 16, 11, 1);
        rig.box("torso", Material.GLOW, -2.5F, -21, -10.2F, 5, 5, 1);
        rig.box("torso", Material.STONE, -12, -25, 9, 24, 10, 2);
        if (moss) {
            rig.box("torso", Material.MOSS, -14, -28, -9, 28, 1, 18);
            rig.box("torso", Material.MOSS, -12, -25, 11, 24, 10, 1);
        }

        for (int i = 0; i < crystals; i++) {
            String name = "crystal" + i;
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (3.0F + v.roll(name + "x") * 10.0F);
            float z = -4.0F + v.roll(name + "z") * 13.0F;
            rig.bone(name, "torso", x, -27.0F, z, -10.0F - v.roll(name + "p") * 30.0F, 0.0F, side * (10.0F + v.roll(name + "r") * 25.0F));
            int length = 5 + v.pick(name + "l", 8);
            rig.box(name, Material.CRYSTAL, -1.5F, -length, -1.5F, 3, length, 3);
            rig.box(name, Material.CRYSTAL, 0.5F, -length / 2.0F, 0.5F, 2, length / 2, 2);
        }

        if (tower) {
            rig.bone("tower", "torso", 0.0F, -27.0F, 4.0F, -8.0F, 15.0F, 6.0F);
            rig.box("tower", Material.BRICK, -5, -14, -5, 10, 14, 10);
            for (int[] c : new int[][]{{-6, -6}, {3, -6}, {-6, 3}, {3, 3}}) {
                rig.box("tower", Material.BRICK, c[0], -16, c[1], 3, 2, 3);
            }

            rig.box("tower", Material.GLOW, -1, -10, -5.3F, 2, 3, 1);
        }

        rig.bone("head", "torso", 0.0F, -27.0F, -2.0F);
        rig.box("head", Material.STONE, -6, -10, -6, 12, 10, 12);
        rig.box("head", Material.STONE, -7, -8, -7.5F, 14, 3, 4);
        if (eyes == 1) {
            rig.box("head", Material.EYE, -2, -6.5F, -6.6F, 4, 2, 1);
        } else {
            rig.box("head", Material.EYE, -5, -6.5F, -6.6F, 3, 2, 1);
            rig.box("head", Material.EYE, 2, -6.5F, -6.6F, 3, 2, 1);
            if (eyes == 3) {
                rig.box("head", Material.EYE, -1, -7.5F, -7.9F, 2, 2, 1);
            }
        }

        rig.bone("jaw", "head", 0.0F, -2.0F, -1.0F);
        rig.box("jaw", Material.STONE, -5, 0, -5, 10, 3, 8);
        if (crown) {
            rig.pairBone("horn", "head", -5.0F, -9.0F, 0.0F, -15.0F, 0.0F, -25.0F);
            rig.pair("horn", Material.HORN, -1.5F, -9, -1.5F, 3, 9, 3);
            rig.bone("horn_mid", "head", 0.0F, -10.0F, 1.0F, -25.0F, 0.0F, 0.0F);
            rig.box("horn_mid", Material.HORN, -1, -7, -1, 2, 7, 2);
        }

        rig.pairBone("arm", "torso", -16.0F, -23.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("arm", Material.STONE, -6, -4, -7, 12, 10, 14);
        rig.pair("arm", Material.STONE, -4.5F, 5, -4.5F, 9, 14, 9);
        if (moss) {
            rig.pair("arm", Material.MOSS, -6, -5, -7, 12, 1, 14);
        }

        rig.pairBone("fore", "arm", 0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("fore", Material.STONE, -5, 0, -5, 10, fore, 10);
        rig.pair("fore", Material.VEIN, -5.4F, 3, -2, 1, fore - 6, 4);
        rig.pairBone("fist", "fore", 0.0F, fore, 0.0F, 0.0F, 0.0F, 0.0F);
        rig.pair("fist", Material.STONE, -6, 0, -6, 12, 9, 12);
        rig.pair("fist", Material.HORN, -6, 7, -6.6F, 12, 2, 2);
        rig.bone("held", "fist_r", 0.0F, 6.0F, -4.0F);
        rig.box("held", Material.STONE, -5, 0, -5, 10, 9, 10);
        return rig;
    }

    @Override
    public void animate(Rig.Baked body, Pose p, ColossusVariant v) {
        float walk = p.gait(0.45F, 0.0F);
        float yaw = Mth.clamp(p.headYaw(), -50.0F, 50.0F);
        float look = Mth.clamp(p.headPitch(), -40.0F, 40.0F);
        ModelPart root = body.part("root");
        Bones.shift(root, 0.0F, -Math.abs(walk) * 1.5F, 0.0F);
        Bones.turn(body.part("torso"), p.wave(0.05F, 0.0F), 0.0F, walk * 3.0F);
        Bones.turn(body.part("leg_r"), walk * 24.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg_l"), -walk * 24.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg2_r"), Math.max(0.0F, -walk) * 22.0F, 0.0F, 0.0F);
        Bones.turn(body.part("leg2_l"), Math.max(0.0F, walk) * 22.0F, 0.0F, 0.0F);
        Bones.turn(body.part("arm_r"), -walk * 14.0F, 0.0F, 6.0F + p.wave(0.05F, 1.0F));
        Bones.turn(body.part("arm_l"), walk * 14.0F, 0.0F, -6.0F - p.wave(0.05F, 1.0F));
        Bones.turnPair(body, "fore", -12.0F, 0.0F, 0.0F);
        Bones.turn(body.part("head"), look * 0.5F, yaw * 0.7F, 0.0F);

        // Slam: both fists climb overhead, then come down together on the ground ahead.
        float raise = p.key("slam", 0.0F, 0.0F, 0.45F, 1.0F, 0.53F, 0.0F, 1.0F, 0.0F);
        float smash = p.key("slam", 0.45F, 0.0F, 0.55F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        Bones.turnPair(body, "arm", -165.0F * raise - 75.0F * smash, 0.0F, -8.0F * raise);
        Bones.turn(body.part("torso"), -12.0F * raise + 35.0F * smash, 0.0F, 0.0F);
        Bones.turn(body.part("head"), -10.0F * raise + 10.0F * smash, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, 4.0F * smash, 0.0F);
        Bones.turnPair(body, "leg", -25.0F * smash, 0.0F, 0.0F);
        Bones.turnPair(body, "leg2", 40.0F * smash, 0.0F, 0.0F);

        // Stomp: the right foot rises high, the body leans off it, and it comes down hard.
        float lift = p.key("stomp", 0.0F, 0.0F, 0.42F, 1.0F, 0.53F, 0.0F, 1.0F, 0.0F);
        float land = p.key("stomp", 0.5F, 0.0F, 0.55F, 1.0F, 0.75F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("leg_r"), -75.0F * lift, 0.0F, 0.0F);
        Bones.turn(body.part("leg2_r"), 85.0F * lift, 0.0F, 0.0F);
        Bones.turn(body.part("torso"), 0.0F, 0.0F, -9.0F * lift);
        Bones.turnPair(body, "arm", -20.0F * lift, 0.0F, 25.0F * lift);
        Bones.shift(root, 0.0F, 2.0F * land, 0.0F);

        // Sweep: the right arm winds back across the body, then swings round through everything in front.
        float wind = p.key("sweep", 0.0F, 0.0F, 0.45F, 1.0F, 0.55F, 0.0F, 1.0F, 0.0F);
        float swing = p.key("sweep", 0.45F, 0.0F, 0.6F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("arm_r"), -80.0F * (wind + swing), 0.0F, 35.0F * wind);
        Bones.turn(body.part("torso"), 0.0F, 50.0F * wind - 65.0F * swing, 0.0F);

        // Throw: it tears a boulder out of its own chest, draws it back over its head and hurls it.
        float rip = p.key("throw", 0.0F, 0.0F, 0.3F, 1.0F, 0.45F, 1.0F, 0.55F, 0.0F, 1.0F, 0.0F);
        float draw = p.key("throw", 0.35F, 0.0F, 0.55F, 1.0F, 0.61F, 0.0F, 1.0F, 0.0F);
        float hurl = p.key("throw", 0.58F, 0.0F, 0.65F, 1.0F, 0.85F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("arm_r"), -50.0F * rip - 175.0F * draw - 95.0F * hurl, 20.0F * rip, -15.0F * rip);
        Bones.turn(body.part("torso"), 8.0F * rip - 15.0F * draw + 22.0F * hurl, 20.0F * draw - 18.0F * hurl, 0.0F);
        ModelPart held = body.part("held");
        if (held != null) {
            held.visible = "throw".equals(p.action()) && p.progress() > 0.25F && p.progress() < 0.61F;
        }

        // Roar: head thrown back, arms flung wide, the whole frame shaking.
        float roar = p.key("roar", 0.0F, 0.0F, 0.25F, 1.0F, 0.8F, 1.0F, 1.0F, 0.0F);
        Bones.turn(body.part("head"), -35.0F * roar, Mth.sin(p.age() * 1.3F) * 4.0F * roar, 0.0F);
        Bones.turn(body.part("jaw"), 30.0F * roar, 0.0F, 0.0F);
        Bones.turnPair(body, "arm", -30.0F * roar, 0.0F, 55.0F * roar);
        Bones.turn(body.part("torso"), -15.0F * roar, 0.0F, 0.0F);

        // Death: the knees go, it drops, and the whole mass topples forward.
        float buckle = p.fall(0.0F, 0.45F);
        float topple = p.fall(0.35F, 1.0F);
        Bones.turnPair(body, "leg", -55.0F * buckle, 0.0F, 0.0F);
        Bones.turnPair(body, "leg2", 95.0F * buckle, 0.0F, 0.0F);
        Bones.shift(root, 0.0F, 14.0F * buckle, 0.0F);
        Bones.turn(root, 75.0F * topple, 0.0F, 0.0F);
        Bones.turnPair(body, "arm", -25.0F * topple, 0.0F, 15.0F * buckle);
        Bones.turn(body.part("head"), 25.0F * buckle, 0.0F, 0.0F);
    }
}
