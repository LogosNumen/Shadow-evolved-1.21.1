package net.logosnumen.nightmareevolved.gametest;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Awakening;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.creature.kind.ShoreKinds;
import net.logosnumen.nightmareevolved.dream.life.Wildlife;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.logosnumen.nightmareevolved.network.WornPayload;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.RankGap;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Checks of Ascension: Saints' Transformations, the rank gap, and Sovereigns' Domains. */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class AscensionGameTests {
    private static ServerPlayer saint(GameTestHelper helper, String name, String aspectId) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Aspect aspect = Aspects.byId(aspectId);
        Awakening.grant(player, aspect, Awakening.rollFlaw(aspect, player.getRandom()), Awakening.rollAttributes(aspect, player.getRandom()));
        Soul.setRank(player, Rank.SAINT);
        SoulData soul = Soul.of(player);
        soul.setEssence(soul.maxEssence());
        return player;
    }

    @GameTest(template = "empty")
    public static void aSaintsFormIsSeenByOthers(GameTestHelper helper) {
        ServerPlayer kai = saint(helper, "ne_form_kai", "nightingale");
        helper.assertTrue(WornPayload.of(kai).looks().stream().noneMatch(l -> l.startsWith(WornPayload.FORM)), "Untransformed, a Saint shows no form");
        helper.assertTrue(Aspects.use(kai, Stage.TRANSCENDENT.ordinal()), "Kai takes his Transformation");
        helper.assertTrue(WornPayload.of(kai).looks().contains(WornPayload.FORM + "midnight_dragon"), "Those who see him see the midnight dragon");
        helper.assertTrue(kai.getScale() > 2.0F, "and it is as large as a dragon");
        Aspects.use(kai, Stage.TRANSCENDENT.ordinal());
        helper.assertTrue(Soul.of(kai).form().isEmpty() && WornPayload.of(kai).looks().stream().noneMatch(l -> l.startsWith(WornPayload.FORM)),
            "Let go, the form is gone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theRankGapGuardsHigherSouls(GameTestHelper helper) {
        ServerPlayer saint = saint(helper, "ne_gap_saint", "sky_tide");
        DreamCreature scavenger = Wildlife.spawn(helper.getLevel(), ShoreKinds.CARAPACE_SCAVENGER, helper.absolutePos(new BlockPos(1, 2, 1)),
            MobSpawnType.SPAWN_EGG);
        helper.assertTrue(scavenger != null, "An Awakened creature comes");
        helper.assertTrue(RankGap.gap(scavenger, saint) == 2, "A Saint stands two ranks above an Awakened creature");
        helper.assertTrue(RankGap.multiplier(scavenger, saint) < 0.5F, "Its blows barely mark her");
        helper.assertTrue(RankGap.multiplier(saint, scavenger) > 1.0F, "and hers land with the weight of her rank");
        Soul.setRank(saint, Rank.AWAKENED);
        helper.assertTrue(RankGap.multiplier(scavenger, saint) == 1.0F, "Between equals there is no gap");
        Soul.setRank(saint, Rank.SLEEPER);
        helper.assertTrue(RankGap.multiplier(saint, scavenger) < 1.0F && RankGap.multiplier(saint, scavenger) > 0.5F,
            "A lower soul can still fight upward");
        scavenger.discard();
        helper.succeed();
    }
}
