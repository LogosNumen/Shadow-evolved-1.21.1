package net.logosnumen.nightmareevolved.gametest;

import java.util.List;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.creature.kind.BastionKinds;
import net.logosnumen.nightmareevolved.creature.kind.BurnedKinds;
import net.logosnumen.nightmareevolved.creature.kind.DesertKinds;
import net.logosnumen.nightmareevolved.creature.kind.GodgraveKinds;
import net.logosnumen.nightmareevolved.creature.kind.IsleKinds;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import net.logosnumen.nightmareevolved.creature.kind.MountainKinds;
import net.logosnumen.nightmareevolved.creature.kind.RavenheartKinds;
import net.logosnumen.nightmareevolved.creature.kind.ShoreKinds;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.life.Wildlife;
import net.logosnumen.nightmareevolved.entity.creature.CreatureTypes;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.logosnumen.nightmareevolved.entity.creature.MillipedeQueen;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Dream Realm's creatures: every kind registered with what the novel makes it, each a Nightmare Creature of its
 * own rank and class when it comes into the world, raised only by what lies above that, and landing the blows its
 * moves name.
 */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class CreatureGameTests {
    @GameTest(template = "empty")
    public static void everyKindIsRegistered(GameTestHelper helper) {
        for (Kind kind : Kinds.all()) {
            EntityType<DreamCreature> type = CreatureTypes.type(kind);
            helper.assertTrue(CreatureTypes.kindOf(type) == kind, kind.id + " has its own entity type");
            helper.assertTrue(DefaultAttributes.hasSupplier(type), kind.id + " has attributes");
            helper.assertTrue(DefaultAttributes.getSupplier(type).getBaseValue(Attributes.MAX_HEALTH) == kind.health, kind.id + " has its health");
            helper.assertTrue(!kind.moves.isEmpty(), kind.id + " has moves to fight with");
            helper.assertTrue(!kind.haunts.isEmpty() || kind.lair != null, kind.id + " lives somewhere in the Dream Realm");
        }

        helper.assertTrue(Memories.leftBy(ShoreKinds.CARAPACE_SCAVENGER.id) != null, "The novel's creatures are named for the Memories they leave");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lairsLieInTheirRegions(GameTestHelper helper) {
        lairsIn(helper, ShoreKinds.ALL, Region.FORGOTTEN_SHORE);
        lairsIn(helper, IsleKinds.ALL, Region.CHAINED_ISLES);
        lairsIn(helper, MountainKinds.ALL, Region.HOLLOW_MOUNTAINS);
        lairsIn(helper, GodgraveKinds.ALL, Region.GODGRAVE);
        lairsIn(helper, BurnedKinds.ALL, Region.BURNED_FOREST);

        helper.assertTrue(RealmMap.Shore.basin(Places.IRON_NEST.x(), Places.IRON_NEST.z()) < 1.0, "The iron spiders nest in the labyrinth");
        helper.succeed();
    }

    private static void lairsIn(GameTestHelper helper, List<Kind> kinds, Region region) {
        for (Kind kind : kinds) {
            if (kind.lair != null) {
                int x = kind.lair.place().x() + kind.lair.dx();
                int z = kind.lair.place().z() + kind.lair.dz();
                helper.assertTrue(RealmMap.regionAt(x, z).whole() == region, kind.id + " keeps its lair in " + region.id());
            }
        }
    }

    @GameTest(template = "empty")
    public static void creaturesKeepTheirRank(GameTestHelper helper) {
        DreamCreature scavenger = Wildlife.spawn(helper.getLevel(), ShoreKinds.CARAPACE_SCAVENGER, helper.absolutePos(new BlockPos(1, 2, 1)),
            MobSpawnType.SPAWN_EGG);
        helper.assertTrue(scavenger != null, "A Carapace Scavenger comes into the world");
        helper.assertTrue(Creatures.ofNightmare(scavenger) && Creatures.rankOf(scavenger) == CreatureRank.AWAKENED
            && Creatures.classOf(scavenger) == CreatureClass.BEAST, "It is an Awakened Beast, as the novel has it");
        helper.assertTrue(ShoreKinds.CARAPACE_SCAVENGER.id.equals(scavenger.getPersistentData().getString(DreamCreature.LORE)),
            "It may leave the Memory the novel gives its kind");
        float health = scavenger.getMaxHealth();
        Creatures.raise(scavenger, CreatureRank.AWAKENED, CreatureClass.BEAST, false, -1L);
        helper.assertTrue(scavenger.getMaxHealth() == health, "Raised to its own rank, it is no stronger");
        Creatures.raise(scavenger, CreatureRank.FALLEN, CreatureClass.BEAST, false, -1L);
        helper.assertTrue(scavenger.getMaxHealth() > health * 1.8F && scavenger.getScale() == 1.0F, "Raised a rank above, it is stronger but no larger");
        scavenger.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void movesLandTheirBlows(GameTestHelper helper) {
        DreamCreature scavenger = Wildlife.spawn(helper.getLevel(), ShoreKinds.CARAPACE_SCAVENGER, helper.absolutePos(new BlockPos(1, 2, 1)),
            MobSpawnType.SPAWN_EGG);
        Zombie prey = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        helper.assertTrue(scavenger != null, "A Carapace Scavenger comes into the world");
        scavenger.setTarget(prey);
        float before = prey.getHealth();
        scavenger.forceMove("snap");
        helper.succeedWhen(() -> {
            helper.assertTrue(prey.getHealth() < before, "Its pincers close on its prey");
            scavenger.discard();
            prey.discard();
        });
    }

    @GameTest(template = "empty")
    public static void theDeathlessCannotDie(GameTestHelper helper) {
        DreamCreature dead = Wildlife.spawn(helper.getLevel(), DesertKinds.DEATHLESS, helper.absolutePos(new BlockPos(1, 2, 1)), MobSpawnType.SPAWN_EGG);
        helper.assertTrue(dead != null, "A Deathless rises");
        helper.assertTrue(!Creatures.feedsSouls(dead), "It has no soul to feed anyone's");
        dead.hurt(helper.getLevel().damageSources().generic(), 10000.0F);
        helper.assertTrue(dead.isAlive() && dead.roused(), "A killing blow only breaks it apart");
        float health = dead.getHealth();
        dead.hurt(helper.getLevel().damageSources().generic(), 50.0F);
        helper.assertTrue(dead.getHealth() == health, "Lying in pieces, it cannot be harmed further");
        dead.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void skinwalkersShowThemselvesWhenStruck(GameTestHelper helper) {
        DreamCreature vessel = Wildlife.spawn(helper.getLevel(), DesertKinds.SKINWALKER, helper.absolutePos(new BlockPos(1, 2, 1)), MobSpawnType.SPAWN_EGG);
        helper.assertTrue(vessel != null && !vessel.roused(), "A Skinwalker walks as a traveller");
        vessel.hurt(helper.getLevel().damageSources().generic(), 2.0F);
        helper.assertTrue(vessel.roused(), "Struck, it shows what it is");
        vessel.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theMillipedeQueenSlipsBetweenMoments(GameTestHelper helper) {
        // She is too big for the test's little room to pass a spawn's check for space, so she is set down directly.
        DreamCreature spawned = helper.spawn(CreatureTypes.type(BurnedKinds.MILLIPEDE_QUEEN), new BlockPos(2, 2, 2));
        helper.assertTrue(spawned instanceof MillipedeQueen, "A Millipede Queen comes into the world");
        MillipedeQueen queen = (MillipedeQueen)spawned;
        helper.assertTrue(!queen.between(), "At rest she stays in one moment");
        Husk foe = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(1, 2, 8));
        queen.setTarget(foe);
        helper.succeedWhen(() -> {
            queen.setTarget(foe);
            helper.assertTrue(queen.between(), "In a fight she slips between two moments");
            float health = queen.getHealth();
            queen.hurt(helper.getLevel().damageSources().generic(), 50.0F);
            helper.assertTrue(queen.getHealth() == health, "Between moments, blows pass through her");
            queen.discard();
            foe.discard();
        });
    }

    @GameTest(template = "empty")
    public static void chaliceKnightsBecomeBeastsWhenHardPressed(GameTestHelper helper) {
        DreamCreature knight = Wildlife.spawn(helper.getLevel(), RavenheartKinds.CHALICE_KNIGHT, helper.absolutePos(new BlockPos(2, 2, 2)), MobSpawnType.SPAWN_EGG);
        helper.assertTrue(knight != null && !knight.roused(), "A Chalice Knight fights as a knight");
        double speed = knight.getAttributeValue(Attributes.MOVEMENT_SPEED);
        knight.hurt(helper.getLevel().damageSources().generic(), knight.getMaxHealth() * 0.2F);
        helper.assertTrue(!knight.roused(), "A glancing blow doesn't bring the beast out");
        knight.invulnerableTime = 0;
        knight.hurt(helper.getLevel().damageSources().generic(), knight.getMaxHealth() * 0.4F);
        helper.assertTrue(knight.roused() && knight.getAttributeValue(Attributes.MOVEMENT_SPEED) > speed, "Hard pressed, the beast bursts out, and it is faster");
        knight.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theForestMawCannotBeSeenUntilItOpens(GameTestHelper helper) {
        DreamCreature maw = Wildlife.spawn(helper.getLevel(), BastionKinds.FOREST_MAW, helper.absolutePos(new BlockPos(2, 2, 2)), MobSpawnType.SPAWN_EGG);
        helper.assertTrue(maw != null && !maw.roused() && maw.tint() >>> 24 == 0 && !maw.shouldShowName(), "Waiting, the Forest Maw shows nothing of itself");
        maw.hurt(helper.getLevel().damageSources().generic(), 1.0F);
        helper.assertTrue(maw.roused() && maw.tint() >>> 24 == 0xFF, "Struck, it opens and is there to see");
        maw.discard();
        helper.succeed();
    }
}
