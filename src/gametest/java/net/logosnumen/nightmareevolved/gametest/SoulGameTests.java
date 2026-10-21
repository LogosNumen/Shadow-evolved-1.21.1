package net.logosnumen.nightmareevolved.gametest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Awakening;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.creature.CreatureEvents;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The soul as the novel has it: one core of a thousand fragments (seven for a Divine Aspect), shards worth more from
 * higher creatures; every Aspect whole and paired with a Flaw; Attributes that grow with Rank; abilities that wake,
 * cost, rest and hold; and Flaws that bite.
 */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class SoulGameTests {
    /** A stand-in player with a soul of its own, so no test sees another's. */
    private static ServerPlayer soul(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ne_test"));
    }

    @GameTest(template = "empty")
    public static void coresHoldAThousandFragments(GameTestHelper helper) {
        helper.assertTrue(SoulRules.capacity(1) == 1000 && SoulRules.capacity(7) == 7000, "Each core holds a thousand fragments");
        helper.assertTrue(SoulRules.shardValue(0, 0) == 5 && SoulRules.shardValue(1, 0) == 20, "Higher creatures' shards are worth more");
        helper.assertTrue(SoulRules.shardValue(0, 2) < SoulRules.shardValue(0, 0), "Shards from creatures below the soul are worth less");
        for (Rank rank : Rank.values()) {
            Rank next = rank.next();
            if (next != rank) {
                helper.assertTrue(SoulRules.maxEssence(next, Tier.DORMANT, 1, 1.0) > SoulRules.maxEssence(rank, Tier.DORMANT, 1, 1.0),
                    "Essence should grow past " + rank.id());
            }
        }

        helper.assertTrue(SoulRules.maxEssence(Rank.SAINT, Tier.DORMANT, 1, 1.0) > SoulRules.maxEssence(Rank.SAINT, Tier.DORMANT, 1, 0.0),
            "A saturated core should hold more essence");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rankGapsBendBlows(GameTestHelper helper) {
        helper.assertTrue(Math.abs(CreatureEvents.gapMultiplier(0, 2) - 0.36F) < 1.0E-4F, "Two ranks up should take 0.36 of a blow");
        helper.assertTrue(Math.abs(CreatureEvents.gapMultiplier(2, 0) - 1.5F) < 1.0E-4F, "Two ranks down should take half again");
        helper.assertTrue(CreatureEvents.gapMultiplier(-1, 4) == 1.0F, "Aspirants stand outside the ladder");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void trialsLeadFromAspirantToDivine(GameTestHelper helper) {
        Rank rank = Rank.ASPIRANT;
        int trials = 0;
        while (Trial.next(rank) != null) {
            Trial trial = Trial.next(rank);
            helper.assertTrue(trial.from == rank && trial.to() == rank.next(), trial.id() + " should raise a soul one rank");
            rank = trial.to();
            trials++;
        }

        helper.assertTrue(rank == Rank.DIVINE && trials == Trial.values().length, "The trials should lead to Divine");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyAspectIsWhole(GameTestHelper helper) {
        Set<String> abilities = new HashSet<>();
        for (Aspect aspect : Aspects.all()) {
            helper.assertTrue(aspect.at(Stage.DORMANT) != null, aspect.id() + " should have a Dormant ability");
            helper.assertTrue(!aspect.flaws().isEmpty(), aspect.id() + " should come with a Flaw");
            for (String flaw : aspect.flaws()) {
                helper.assertTrue(Flaw.byId(flaw) != null, aspect.id() + " names an unknown Flaw " + flaw);
            }

            for (String attribute : aspect.signature()) {
                helper.assertTrue(SoulAttribute.byId(attribute) != null, aspect.id() + " names an unknown Attribute " + attribute);
            }

            for (Ability ability : aspect.abilities()) {
                helper.assertTrue(abilities.add(ability.id), "Ability ids must be unique, " + ability.id + " is not");
                helper.assertTrue(ability.kind != Ability.Kind.TRANSFORMATION || ability.stage == Stage.TRANSCENDENT,
                    ability.id + " is a Transformation outside Sainthood");
                helper.assertTrue(ability.kind == Ability.Kind.PASSIVE || ability.cost > 0.0, ability.id + " should cost essence");
            }
        }

        helper.assertTrue(Aspects.all().size() >= 34, "Every Aspect of the novel should be here, found " + Aspects.all().size());
        helper.assertTrue(Aspects.all().stream().filter(Aspect::divine).count() == 3, "Three Aspects are Divine");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyFlawBelongsToAnAspect(GameTestHelper helper) {
        for (Flaw flaw : Flaw.values()) {
            boolean owned = Aspects.all().stream().anyMatch(a -> a.flaws().contains(flaw.id()) && a.canonFlaw());
            helper.assertTrue(owned, flaw.id() + " should be the Flaw of the Aspect whose bearer had it");
        }

        helper.assertTrue(Flaw.values().length == 25, "The novel shows 25 Flaws");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void attributesGrowWithRank(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        SoulData soul = Soul.of(player);
        soul.addAttribute("mark_of_divinity");
        soul.addAttribute("child_of_shadows");
        soul.addAttribute("lesser_iron_body");
        Soul.setRank(player, Rank.SAINT);
        helper.assertTrue(soul.hasAttribute("ember_of_divinity"), "A Saint's Mark of Divinity should have grown to an Ember");
        helper.assertTrue(soul.hasAttribute("master_of_shadows"), "A Saint's Child of Shadows should be a Master of Shadows");
        helper.assertTrue(soul.hasAttribute("complete_steel_body"), "A Saint's iron body should be complete steel");
        helper.assertFalse(soul.hasAttribute("mark_of_divinity"), "The lesser form should be gone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void divineSoulsGrowCores(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        Soul.setRank(player, Rank.AWAKENED);
        Awakening.grant(player, Aspects.byId("light_bringer"), "pristine_soul", List.of());
        SoulData soul = Soul.of(player);
        helper.assertTrue(soul.cores() == 1, "Every soul starts with one core");
        soul.setFragments(soul.capacity());
        Soul.absorb(player, 1, 1);
        helper.assertTrue(soul.cores() == 2, "A Divine soul with a full core should grow a second, has " + soul.cores());
        helper.assertTrue(Soul.soulClass(soul).ordinal() == 1, "Two cores make a Monster");

        ServerPlayer plain = soul(helper);
        Soul.setRank(plain, Rank.AWAKENED);
        Awakening.grant(plain, Aspects.byId("raised_by_wolves"), "ravenous_body", List.of());
        Soul.of(plain).setFragments(Soul.of(plain).capacity());
        helper.assertTrue(Soul.absorb(plain, 1, 1) == 0 && Soul.of(plain).cores() == 1, "Other souls keep a single core");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shadowCoreRefusesShards(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        Soul.setRank(player, Rank.SLEEPER);
        Awakening.grant(player, Aspects.byId("shadow_slave"), "clear_conscience", List.of("fated", "mark_of_divinity", "child_of_shadows"));
        helper.assertTrue(Soul.absorb(player, 0, 5) == 0, "A Shadow Core takes nothing from shards");
        Soul.feed(player, 300);
        helper.assertTrue(Soul.of(player).fragments() == 300, "It grows from what its bearer kills");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void abilitiesWakeHoldAndRest(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        Soul.setRank(player, Rank.SLEEPER);
        Awakening.grant(player, Aspects.byId("raised_by_wolves"), "ravenous_body", List.of());
        SoulData soul = Soul.of(player);
        double base = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        helper.assertFalse(Aspects.use(player, 0), "A passive ability has nothing to call");
        helper.assertFalse(Aspects.use(player, 1), "Skin of Steel sleeps until Awakened");
        Soul.setRank(player, Rank.AWAKENED);
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) >= base, "War Body should still hold");
        helper.assertTrue(Aspects.use(player, 1) && soul.toggled("skin_of_steel"), "Skin of Steel should switch on");
        double armour = player.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(armour >= 10.0, "Skin of Steel should harden the body, armour is " + armour);
        double before = soul.essence();
        Aspects.upkeep(player);
        helper.assertTrue(soul.essence() < before, "A held ability should draw essence");
        helper.assertTrue(Aspects.use(player, 1) && !soul.toggled("skin_of_steel"), "Skin of Steel should switch off");
        helper.assertFalse(Aspects.use(player, 1), "It should rest a moment after");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crownOfThornsCannotKill(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        Soul.setRank(player, Rank.SLEEPER);
        Awakening.grant(player, Aspects.byId("distant_sky"), "crown_of_thorns", List.of());
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        zombie.hurt(helper.getLevel().damageSources().playerAttack(player), 500.0F);
        helper.assertTrue(zombie.isAlive() && zombie.getHealth() < zombie.getMaxHealth() * 0.25F,
            "Rain's blow should leave its target alive but barely, health " + zombie.getHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crosshairFindsWhatIsLookedAt(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 10.0F);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 4));
        zombie.setNoAi(true);
        LivingEntity seen = Targets.looked(player, 16.0);
        helper.assertTrue(seen == zombie, "The creature straight ahead should be found, found " + seen + " from " + player.position()
            + " looking " + player.getLookAngle() + " at " + zombie.position());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crosshairForgivesANearMiss(GameTestHelper helper) {
        ServerPlayer player = soul(helper);
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 15.0F);
        Zombie close = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        close.setNoAi(true);
        helper.assertTrue(Targets.looked(player, 16.0) == close, "A creature right in front of the face should be found");
        close.discard();
        Zombie aside = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 5));
        aside.setNoAi(true);
        aside.moveTo(aside.getX() + 0.55, aside.getY(), aside.getZ());
        helper.assertTrue(Targets.looked(player, 16.0) == aside, "A near miss should still count");
        helper.succeed();
    }
}
