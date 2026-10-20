package net.logosnumen.nightmareevolved.gametest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.creature.CreatureEvents;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.AspectTier;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The soul's rules: what each rank's cores hold, how rank gaps change a blow, the road of trials from Aspirant to
 * Divine, shards filling the cores, rank raising the body, and the Aspects' abilities waking and resting on time.
 */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class SoulGameTests {
    /** A stand-in player with a soul of its own, so no test sees another's. */
    private static ServerPlayer soul(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ne_test"));
    }

    @GameTest(template = "empty")
    public static void coresHoldMoreAtEachRank(GameTestHelper helper) {
        helper.assertTrue(SoulRules.capacity(Rank.ASPIRANT) == 0.0, "An Aspirant has no core to fill");
        helper.assertTrue(SoulRules.capacity(Rank.SLEEPER) == 12.0, "A Sleeper's core holds 12, held " + SoulRules.capacity(Rank.SLEEPER));
        helper.assertTrue(SoulRules.capacity(Rank.MASTER) == 96.0, "A Master's two cores hold 96, held " + SoulRules.capacity(Rank.MASTER));
        helper.assertTrue(SoulRules.shardValue(0) == 1.0 && SoulRules.shardValue(6) == 64.0, "Shards double in worth with each creature rank");
        for (Rank rank : Rank.values()) {
            Rank next = rank.next();
            if (next != rank) {
                helper.assertTrue(SoulRules.capacity(next) > SoulRules.capacity(rank), "Cores should hold more past " + rank.id());
                helper.assertTrue(SoulRules.maxEssence(next, AspectTier.DORMANT) > SoulRules.maxEssence(rank, AspectTier.DORMANT),
                    "Essence should grow from " + rank.id());
            }
        }

        helper.assertTrue(SoulRules.maxEssence(Rank.SLEEPER, AspectTier.TRANSCENDENT) > SoulRules.maxEssence(Rank.SLEEPER, AspectTier.DORMANT),
            "A rarer Aspect should hold more essence");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rankGapsBendBlows(GameTestHelper helper) {
        helper.assertTrue(Math.abs(CreatureEvents.gapMultiplier(0, 2) - 0.36F) < 1.0E-4F, "Two ranks up should take 0.36 of a blow");
        helper.assertTrue(Math.abs(CreatureEvents.gapMultiplier(2, 0) - 1.5F) < 1.0E-4F, "Two ranks down should take half again");
        helper.assertTrue(CreatureEvents.gapMultiplier(-1, 4) == 1.0F, "Aspirants stand outside the ladder");
        helper.assertTrue(CreatureEvents.gapMultiplier(3, 3) == 1.0F, "Equal ranks trade blows evenly");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void trialsLeadFromAspirantToDivine(GameTestHelper helper) {
        Rank rank = Rank.ASPIRANT;
        int trials = 0;
        while (Trial.next(rank) != null) {
            Trial trial = Trial.next(rank);
            helper.assertTrue(trial.from == rank, trial.id() + " should be faced from " + rank.id());
            helper.assertTrue(trial.to() == rank.next(), trial.id() + " should raise a soul one rank");
            helper.assertTrue(trial == Trial.FIRST_NIGHTMARE || trial.needsSaturation(), trial.id() + " should need full cores");
            rank = trial.to();
            trials++;
        }

        helper.assertTrue(rank == Rank.DIVINE && trials == Trial.values().length, "The trials should lead to Divine, ended at " + rank.id());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyAspectIsWhole(GameTestHelper helper) {
        Set<String> ids = new HashSet<>();
        for (Aspect aspect : Aspects.all()) {
            helper.assertTrue(aspect.abilities().size() == 2, aspect.id() + " should have two abilities");
            helper.assertTrue(aspect.abilities().get(0).unlock() == Rank.SLEEPER && aspect.abilities().get(1).unlock() == Rank.AWAKENED,
                aspect.id() + " should wake its abilities at Sleeper and Awakened");
            for (Ability ability : aspect.abilities()) {
                helper.assertTrue(ids.add(ability.id()), "Ability ids must be unique, " + ability.id() + " is not");
                helper.assertTrue(ability.cost() > 0.0 && ability.cooldown() > 0, ability.id() + " should cost essence and rest after use");
            }
        }

        helper.assertTrue(Aspects.all().size() == 5, "There should be five Aspects");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shardsFillTheCores(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        helper.assertTrue(Soul.absorb(player, 0, 5) == 0, "An Aspirant has no core to take shards into");
        Soul.setRank(player, Rank.SLEEPER);
        SoulData soul = Soul.of(player);
        helper.assertTrue(Soul.absorb(player, 0, 5) == 5 && soul.saturation() == 5.0, "Five Dormant shards should fill five of twelve");
        helper.assertTrue(Soul.absorb(player, 1, 10) == 4, "Awakened shards are worth two; four should top the core up");
        helper.assertTrue(soul.saturated(), "The core should be full");
        helper.assertTrue(Soul.absorb(player, 0, 1) == 0, "A full core takes nothing more");
        Soul.setRank(player, Rank.AWAKENED);
        helper.assertTrue(soul.saturation() == 0.0, "Rising a rank starts the cores empty again");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rankRaisesTheBody(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        double baseHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        Soul.setRank(player, Rank.MASTER);
        helper.assertTrue(player.getAttributeValue(Attributes.MAX_HEALTH) == baseHealth + SoulRules.health(Rank.MASTER), "A Master should be hardier");
        helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Rising a rank should heal");
        helper.assertTrue(Soul.of(player).essence() == Soul.of(player).maxEssence(), "Rising a rank should fill essence");
        Soul.setRank(player, Rank.ASPIRANT);
        helper.assertTrue(player.getAttributeValue(Attributes.MAX_HEALTH) == baseHealth, "An Aspirant has only a mortal body");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void abilitiesWakeAndRest(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        Soul.setRank(player, Rank.SLEEPER);
        Aspects.grant(player, Aspects.IRON_PENITENT, AspectTier.DORMANT);
        SoulData soul = Soul.of(player);
        double full = soul.essence();
        helper.assertFalse(Aspects.use(player, 1), "The second ability sleeps until Awakened");
        helper.assertTrue(Aspects.use(player, 0), "The first ability should answer a Sleeper");
        helper.assertTrue(soul.essence() == full - 6.0, "Penitent's Guard should cost six essence, essence is " + soul.essence());
        helper.assertFalse(Aspects.use(player, 0), "An ability should rest after use");
        soul.setReadyAt("penitents_guard", 0L);
        soul.setEssence(1.0);
        helper.assertFalse(Aspects.use(player, 0), "No ability without essence for it");
        helper.assertTrue(Aspects.power(AspectTier.TRANSCENDENT, Rank.AWAKENED) > Aspects.power(AspectTier.DORMANT, Rank.SLEEPER),
            "A rarer Aspect at a higher rank should hit harder");
        helper.succeed();
    }
}
