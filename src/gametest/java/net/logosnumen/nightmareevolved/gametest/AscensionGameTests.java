package net.logosnumen.nightmareevolved.gametest;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Awakening;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.creature.kind.ShoreKinds;
import net.logosnumen.nightmareevolved.dream.gate.Citadels;
import net.logosnumen.nightmareevolved.dream.gate.Domains;
import net.logosnumen.nightmareevolved.dream.gate.Fealty;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Checks of Ascension: Saints' Transformations, the rank gap, and fealty and Sovereigns' Domains. */
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

    private static Gateways.Gateway gateway(String id) {
        return Gateways.CANON.stream().filter(g -> g.id().equals(id)).findFirst().orElseThrow();
    }

    @GameTest(template = "empty")
    public static void fealtyCarriesASovereignsDomain(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer saint = saint(helper, "ne_domain_saint", "sky_tide");
        ServerPlayer sovereign = saint(helper, "ne_domain_sovereign", "king_of_swords");
        Soul.setRank(sovereign, Rank.SOVEREIGN);
        Gateways.Gateway sanctuary = gateway("sanctuary_of_noctis");
        helper.assertTrue(Citadels.bind(saint, sanctuary), "The Saint binds the Sanctuary");
        BlockPos at = sanctuary.pos();
        Domains.Hold nascent = Domains.at(Domains.holds(server), at.getX() + 10, at.getZ());
        helper.assertTrue(nascent != null && nascent.nascent() && nascent.ruler().id().equals(saint.getUUID()),
            "Serving no one, the Saint has a nascent Domain around their Citadel");
        helper.assertTrue(Domains.guard(nascent, saint.getUUID()) == Domains.NASCENT_GUARD && Domains.guard(nascent, sovereign.getUUID()) == 1.0F,
            "It guards the Saint a little, and no one else");
        helper.assertTrue(Domains.at(Domains.holds(server), at.getX() + Domains.NASCENT_REACH + 20, at.getZ()) == null, "It reaches only a little way");

        helper.assertTrue(!Fealty.receive(sovereign, saint), "Fealty can't be received before it is offered");
        helper.assertTrue(Fealty.swear(saint, sovereign) && Fealty.receive(sovereign, saint), "The Saint swears fealty, and the Sovereign receives it");
        Domains.Hold held = Domains.at(Domains.holds(server), at.getX() + Domains.NASCENT_REACH + 20, at.getZ());
        helper.assertTrue(held != null && !held.nascent() && held.ruler().id().equals(sovereign.getUUID()),
            "Now the Citadel carries the Sovereign's Domain, and further");
        helper.assertTrue(Domains.guard(held, sovereign.getUUID()) == Domains.SUPREME_GUARD, "where the Sovereign is all but untouchable");
        helper.assertTrue(!Fealty.swear(saint, sovereign), "A sworn Saint can't swear again");

        helper.assertTrue(Fealty.renounce(saint), "The Saint renounces the oath");
        Domains.Hold after = Domains.at(Domains.holds(server), at.getX() + 10, at.getZ());
        helper.assertTrue(after != null && after.nascent() && after.ruler().id().equals(saint.getUUID()), "and the Citadel is theirs alone again");
        helper.assertTrue(!Fealty.swear(sovereign, saint), "A Sovereign swears to no one");
        Citadels.release(saint);
        helper.succeed();
    }
}
