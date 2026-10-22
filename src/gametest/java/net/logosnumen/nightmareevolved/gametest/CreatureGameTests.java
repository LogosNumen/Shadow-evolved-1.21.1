package net.logosnumen.nightmareevolved.gametest;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import net.logosnumen.nightmareevolved.creature.kind.ShoreKinds;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.life.Wildlife;
import net.logosnumen.nightmareevolved.entity.creature.CreatureTypes;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
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
        for (Kind kind : ShoreKinds.ALL) {
            if (kind.lair != null) {
                int x = kind.lair.place().x() + kind.lair.dx();
                int z = kind.lair.place().z() + kind.lair.dz();
                helper.assertTrue(RealmMap.regionAt(x, z).whole() == Region.FORGOTTEN_SHORE, kind.id + " keeps its lair on the Forgotten Shore");
            }
        }

        helper.assertTrue(RealmMap.Shore.basin(Places.IRON_NEST.x(), Places.IRON_NEST.z()) < 1.0, "The iron spiders nest in the labyrinth");
        helper.succeed();
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
}
