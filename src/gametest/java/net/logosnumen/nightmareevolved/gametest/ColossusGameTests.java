package net.logosnumen.nightmareevolved.gametest;

import java.util.HashSet;
import java.util.Set;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
import net.logosnumen.nightmareevolved.colossus.Quake;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The colossi: every kind builds the same from the same seed, varies across seeds, ranks as a Titan, and hits hard. */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class ColossusGameTests {
    @GameTest(template = "empty")
    public static void colossiVaryBySeedAndRankAsTitans(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (ColossusKind kind : ColossusKind.values()) {
            EntityType<? extends ColossusEntity> type = Colossi.typeOf(kind);
            ColossusEntity a = type.create(level);
            ColossusEntity b = type.create(level);
            helper.assertTrue(a != null && b != null, "A " + kind.id + " can be made");
            a.setSeed(1234);
            b.setSeed(1234);
            helper.assertTrue(a.variant().equals(b.variant()) && a.getScale() == b.getScale(), "The same seed should make the same " + kind.id);
            helper.assertTrue(!a.attacks().isEmpty(), kind.id + " has no attacks");
            helper.assertTrue(kind.creatureClass() == CreatureClass.TITAN && kind.rank.ordinal() >= CreatureRank.CURSED.ordinal(),
                kind.id + " should be a Titan of Cursed rank or higher");
            helper.assertTrue(a.isInvulnerableTo(level.damageSources().fall()) && a.isInvulnerableTo(level.damageSources().inWall()),
                kind.id + " shouldn't take fall or suffocation damage");
            a.discard();
            b.discard();
        }

        Set<Integer> palettes = new HashSet<>();
        Set<Float> sizes = new HashSet<>();
        int elders = 0;
        for (int seed = 0; seed < 200; seed++) {
            ColossusVariant variant = new ColossusVariant(ColossusKind.CAIRN, seed);
            palettes.add(variant.palette());
            sizes.add(variant.size());
            elders += variant.elder() ? 1 : 0;
        }

        helper.assertTrue(palettes.size() == ColossusKind.PALETTES, "Every colouring should turn up, got " + palettes);
        helper.assertTrue(elders > 0 && elders < 50, "Elders should be uncommon, got " + elders + " in 200");
        helper.assertTrue(sizes.size() > 100, "Sizes should vary");
        helper.succeed();
    }

    /** A slam hurts what stands near it, and never another colossus. */
    @GameTest(template = "empty")
    public static void colossusSlamsSpareTheirOwn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(1, 2, 1));
        ColossusEntity cairn = Colossi.CAIRN.get().create(level);
        ColossusEntity wyrm = Colossi.GRAVEWYRM.get().create(level);
        Husk husk = EntityType.HUSK.create(level);
        helper.assertTrue(cairn != null && wyrm != null && husk != null, "The creatures can be made");
        cairn.setSeed(7);
        wyrm.setSeed(8);
        cairn.moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0F, 0.0F);
        wyrm.moveTo(base.getX() + 2.5, base.getY(), base.getZ() + 0.5, 0.0F, 0.0F);
        husk.moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 2.5, 0.0F, 0.0F);
        level.addFreshEntity(cairn);
        level.addFreshEntity(wyrm);
        level.addFreshEntity(husk);
        float huskBefore = husk.getHealth();
        float wyrmBefore = wyrm.getHealth();
        Quake.slam(level, cairn, Vec3.atBottomCenterOf(base), 4.0, 10.0F, 0.5, Vfx.Palette.EARTH);
        helper.assertTrue(husk.getHealth() < huskBefore, "The slam should hurt the husk");
        helper.assertTrue(wyrm.getHealth() == wyrmBefore, "The slam shouldn't hurt another colossus");
        cairn.discard();
        wyrm.discard();
        husk.discard();
        helper.succeed();
    }
}
