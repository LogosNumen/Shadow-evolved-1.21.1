package net.logosnumen.nightmareevolved.gametest;

import com.mojang.authlib.GameProfile;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.DreamState;
import net.logosnumen.nightmareevolved.dream.DreamTravel;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gate.Altars;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Godgrave;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.landmark.DesertPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.IslePlaces;
import net.logosnumen.nightmareevolved.dream.gen.shape.IslesShaper;
import net.logosnumen.nightmareevolved.dream.law.Crushing;
import net.logosnumen.nightmareevolved.dream.law.DarkSea;
import net.logosnumen.nightmareevolved.dream.law.RealmLaws;
import net.logosnumen.nightmareevolved.dream.seed.NightmareGateEntity;
import net.logosnumen.nightmareevolved.dream.seed.SeedEntities;
import net.logosnumen.nightmareevolved.dream.seed.SeedSites;
import net.logosnumen.nightmareevolved.dream.seed.Seeds;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Dream Realm as the novel lays it out: its regions where they belong, every Gateway and canon Seed standing in its
 * landmark, a Sleeper's random arrival always on safe ground, the Dark Sea rising by night, the Isles rising and
 * crushing, the one path to the Tomb of Ariel, and the two sets of belongings a dreamer keeps; the Seeds of Nightmare
 * scattered by region, turning away the unready, the altars' knives, and an open Gate sending its creatures through.
 */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class DreamRealmGameTests {
    private static final long SEED = 12345L;

    @GameTest(template = "empty")
    public static void regionsLieWhereTheNovelPutsThem(GameTestHelper helper) {
        check(helper, Places.CRIMSON_SPIRE, Region.FORGOTTEN_SHORE);
        check(helper, Places.DARK_CITY, Region.FORGOTTEN_SHORE);
        check(helper, Places.TOMB_OF_ARIEL, Region.NIGHTMARE_DESERT);
        check(helper, Places.BLACK_MOUNTAIN, Region.HOLLOW_MOUNTAINS);
        check(helper, Places.VANISHING_LAKE, Region.GODGRAVE);
        check(helper, Places.RED_HILL, Region.GLASS_HELL);
        check(helper, Places.TEAR, Region.CHAINED_ISLES);
        check(helper, Places.BASTION, Region.BASTION);
        check(helper, Places.RAVENHEART, Region.RAVENHEART);
        check(helper, Places.WEEPING_GODDESS, Region.WEEPING_GODDESS);
        check(helper, Places.NIGHT_GARDEN, Region.STORMSEA);
        helper.assertTrue(RealmMap.regionAt(-3000, -1000) == Region.MOONRIVER_PLAIN, "The Moonriver Plain lies north of Ravenheart");
        helper.assertTrue(RealmMap.regionAt(1000, 2000) == Region.BONE_SEA, "The Bone Sea lies south of the Glass Hell");
        helper.assertTrue(RealmMap.regionAt(0, -12000) == Region.BURNED_FOREST, "The Burned Forest lies beyond the Shore's north");
        helper.assertTrue(RealmMap.regionAt(0, 20000) == Region.STORMSEA, "The Stormsea goes on south");
        // The northern edge of the Shore holds the barrows; west of the labyrinth, the Sea of Ash.
        helper.assertTrue(RealmMap.part(-1000, 80, Places.SHORE_NORTH + 50) == Region.LAND_OF_BARROWS, "The barrows edge the Shore's north");
        helper.assertTrue(RealmMap.part(Places.CRIMSON_SPIRE.x() - 1800, 80, Places.CRIMSON_SPIRE.z()) == Region.SEA_OF_ASH, "The Sea of Ash lies west");
        helper.assertTrue(RealmMap.part(Places.BLACK_MOUNTAIN.x(), 0, Places.BLACK_MOUNTAIN.z()) == Region.UNDERWORLD, "The Underworld lies under the mountains");
        helper.assertTrue(RealmMap.part(Godgrave.AXIS, Godgrave.VAULT_FLOOR + 10, 0) == Region.GODGRAVE_HOLLOW, "The breastbone is hollow");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, Places.Place place, Region region) {
        helper.assertTrue(RealmMap.regionAt(place.x(), place.z()) == region, place.id() + " should lie in " + region.id());
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void everyGatewayStandsInItsLandmark(GameTestHelper helper) {
        RealmTerrain terrain = RealmTerrain.of(SEED);
        for (Gateways.Gateway gateway : Gateways.CANON) {
            Column column = terrain.column(gateway.pos().getX(), gateway.pos().getZ());
            BlockState state = column.get(gateway.pos().getY());
            helper.assertTrue(state != null && state.is(DreamBlocks.GATEWAY.get()), gateway.id() + " has its Gateway block where it should");
            helper.assertTrue(state.getValue(GatewayBlock.FORM) == gateway.form(), gateway.id() + " looks as it should");
            helper.assertTrue(RealmMap.regionAt(gateway.pos().getX(), gateway.pos().getZ()).whole() == gateway.region()
                || gateway.region() == Region.GODGRAVE, gateway.id() + " stands in its region");
            helper.assertTrue(open(terrain, gateway.pos()), gateway.id() + " stands open to be touched, not buried");
        }

        helper.succeed();
    }

    /** Whether a block has a face open above or beside it with room for a soul to come and lay a hand on it. */
    private static boolean open(RealmTerrain terrain, BlockPos pos) {
        Column above = terrain.column(pos.getX(), pos.getZ());
        if (!above.solid(pos.getY() + 1) && !above.solid(pos.getY() + 2)) {
            return true;
        }

        for (Direction side : Direction.Plane.HORIZONTAL) {
            Column beside = terrain.column(pos.getX() + side.getStepX(), pos.getZ() + side.getStepZ());
            if (!beside.solid(pos.getY()) && !beside.solid(pos.getY() + 1)) {
                return true;
            }
        }

        return false;
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void canonSeedsStandInTheirPlaces(GameTestHelper helper) {
        RealmTerrain terrain = RealmTerrain.of(SEED);
        BlockState dark = terrain.column(Places.IVORY_TOWER.x(), Places.IVORY_TOWER.z()).get(IslePlaces.IvoryTower.TOP + 1);
        helper.assertTrue(dark != null && dark.is(DreamBlocks.SEED.get()) && dark.getValue(SeedBlock.FORM) == SeedBlock.Form.SHACKLES
            && dark.getValue(SeedBlock.CATEGORY) == 2, "The Dark Seed grows in the Ivory Tower: seven shackles, Category Two");
        BlockState tomb = terrain.column(Places.TOMB_OF_ARIEL.x(), Places.TOMB_OF_ARIEL.z()).get(DesertPlaces.TombOfAriel.BASE + 2);
        helper.assertTrue(tomb != null && tomb.is(DreamBlocks.SEED.get()) && tomb.getValue(SeedBlock.CATEGORY) == 3,
            "A block of black stone at the Tomb of Ariel's heart holds a Third Nightmare");
        BlockState top = terrain.column(Places.MICTLAN_ZIGGURAT.x(), Places.MICTLAN_ZIGGURAT.z()).get(Godgrave.VAULT_FLOOR + 37);
        helper.assertTrue(top != null && top.is(DreamBlocks.SEED.get()) && top.getValue(SeedBlock.CATEGORY) == 2,
            "A Seed of the Second Nightmare rests on the ziggurat in Godgrave's hollow");
        for (SeedSites.Site site : SeedSites.CANON) {
            helper.assertTrue(open(terrain, site.pos()), site.id() + " can be reached and touched");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void sleepersArriveOnSafeGround(GameTestHelper helper) {
        RealmTerrain terrain = RealmTerrain.of(SEED);
        RandomSource random = RandomSource.create(77L);
        for (int i = 0; i < 24; i++) {
            DreamTravel.Arrival arrival = DreamTravel.random(terrain, region -> Gateways.forArrival(Gateways.CANON, region), random);
            int x = (int)Math.floor(arrival.pos().x);
            int z = (int)Math.floor(arrival.pos().z);
            int y = (int)Math.floor(arrival.pos().y);
            Region region = RealmMap.part(x, 80, z);
            Column column = terrain.column(x, z);
            helper.assertTrue(column.solid(y - 1) && !column.solid(y) && !column.solid(y + 1), "A Sleeper lands on solid ground with room to stand");
            helper.assertTrue(!region.deathZone, "No Sleeper is thrown into a Death Zone");
            helper.assertTrue(region.whole() != Region.FORGOTTEN_SHORE || y > DarkSea.HIGH, "On the Shore, a Sleeper lands above the night's sea");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theDarkSeaRisesByNight(GameTestHelper helper) {
        helper.assertTrue(DarkSea.level(6000, 0.0F) == DarkSea.LOW, "By day the Dark Sea keeps to the deep places");
        helper.assertTrue(DarkSea.level(18000, 0.0F) == DarkSea.HIGH, "By night it floods the basin");
        helper.assertTrue(DarkSea.reaches(Places.CRIMSON_SPIRE.x() + 300, Places.CRIMSON_SPIRE.z()), "It reaches the labyrinth");
        helper.assertTrue(!DarkSea.reaches(-1000, Places.SHORE_NORTH + 40), "It never comes past the barrows");
        double x = Places.CRIMSON_SPIRE.x() + 300;
        double z = Places.CRIMSON_SPIRE.z();
        helper.assertTrue(DarkSea.under(x, 70, z, 18000, false) && !DarkSea.under(x, 70, z, 6000, false), "The labyrinth floor is under it at night only");
        helper.assertTrue(!DarkSea.under(x, 70, z, 18000, true), "Once sealed, it comes no more");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void theIslesRiseAndCrush(GameTestHelper helper) {
        IslesShaper isles = RealmTerrain.of(SEED).isles();
        IslesShaper.Island island = isles.islandAt(Places.IRON_HAND.x(), Places.IRON_HAND.z());
        helper.assertTrue(island != null, "Iron Hand Island floats where it should");
        double most = 0.0;
        double least = 1.0;
        boolean rattled = false;
        for (long t = 0; t < Crushing.PERIOD; t += 50) {
            double p = Crushing.pressure(island, t);
            most = Math.max(most, p);
            least = Math.min(least, p);
            rattled |= Crushing.rattling(island, t);
        }

        helper.assertTrue(most >= 1.0 && least <= 0.0, "An island rises to the full Crushing and comes back to rest");
        helper.assertTrue(rattled, "Its chains rattle before it rises");
        IslesShaper.Island sanctuary = isles.islandAt(Places.SANCTUARY_OF_NOCTIS.x(), Places.SANCTUARY_OF_NOCTIS.z());
        for (long t = 0; t < Crushing.PERIOD; t += 200) {
            helper.assertTrue(Crushing.pressure(sanctuary, t) == 0.0, "The Sanctuary's island never rises or falls");
        }

        helper.assertTrue(Crushing.sky(3000, 300, -500) > 0.0, "The sky above the Isles is forbidden");
        helper.assertTrue(Crushing.sky(Places.IVORY_TOWER.x(), 300, Places.IVORY_TOWER.z()) == 0.0, "except around the Ivory Tower");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theTombHasOnePath(GameTestHelper helper) {
        double mx = (Places.BLACK_SKULL.x() + Places.TOMB_OF_ARIEL.x()) / 2.0;
        double mz = (Places.BLACK_SKULL.z() + Places.TOMB_OF_ARIEL.z()) / 2.0;
        helper.assertTrue(RealmLaws.onPath(mx, mz), "The line of black stones from the Black Skull leads to the Tomb");
        helper.assertTrue(!RealmLaws.onPath(Places.TOMB_OF_ARIEL.x(), Places.TOMB_OF_ARIEL.z() - 600), "Elsewhere the mirage holds");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void belongingsStayOnTheirSide(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ne_dream_test"));
        Soul.setRank(player, Rank.SLEEPER);
        player.getInventory().add(new ItemStack(Items.DIAMOND_SWORD));
        DreamState state = DreamState.of(player);
        DreamTravel.swap(player, state, true);
        helper.assertTrue(player.getInventory().isEmpty(), "A dreamer comes into the Dream Realm with nothing from the waking world");
        player.getInventory().add(new ItemStack(Items.BONE, 5));
        DreamTravel.swap(player, state, false);
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.DIAMOND_SWORD)), "Waking, they have their own things again");
        helper.assertTrue(!player.getInventory().contains(new ItemStack(Items.BONE)), "What they found in the dream stays there");
        DreamTravel.swap(player, state, true);
        helper.assertTrue(player.getInventory().countItem(Items.BONE) == 5, "and waits for them in their next dream");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void theRealmIsTheSameEveryTime(GameTestHelper helper) {
        RealmTerrain terrain = RealmTerrain.of(SEED);
        RandomSource random = RandomSource.create(5L);
        for (int i = 0; i < 40; i++) {
            int x = random.nextInt(16000) - 8000;
            int z = random.nextInt(16000) - 10000;
            Column a = terrain.column(x, z);
            int top = a.top();
            BlockState state = a.get(top);
            Column b = terrain.column(x, z);
            helper.assertTrue(b.top() == top && b.get(top) == state, "A column is built the same each time it is asked for");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void seedsGrowWhereTheirRegionsAllow(GameTestHelper helper) {
        RealmTerrain terrain = RealmTerrain.of(SEED);
        Map<Region, Integer> counts = new EnumMap<>(Region.class);
        int painted = 0;
        for (int i = -10; i <= 10; i++) {
            for (int j = -16; j <= 6; j++) {
                SeedSites.Site site = SeedSites.site(terrain, i, j);
                if (site == null) {
                    continue;
                }

                helper.assertTrue(site.equals(SeedSites.site(terrain, i, j)), "A Seed grows in the same place each time it is asked for");
                helper.assertTrue(site.category() >= site.region().seedMin && site.category() <= site.region().seedMax,
                    "A Seed's Category suits its region: " + site.id() + " in " + site.region().id());
                helper.assertTrue(site.region() != Region.LAND_OF_BARROWS && site.region() != Region.UNDERWORLD, "No Seed grows in the barrows or under the mountains");
                counts.merge(site.region(), 1, Integer::sum);
                if (painted < 6) {
                    BlockState state = terrain.column(site.pos().getX(), site.pos().getZ()).get(site.pos().getY());
                    helper.assertTrue(state != null && state.is(DreamBlocks.SEED.get()) && state.getValue(SeedBlock.CATEGORY) == site.category(),
                        "The Seed itself stands on its dais: " + site.id());
                    painted++;
                }
            }
        }

        int desert = counts.getOrDefault(Region.NIGHTMARE_DESERT, 0);
        helper.assertTrue(desert > 0 && counts.values().stream().allMatch(n -> n <= desert), "Nowhere do as many Seeds bloom as in the Nightmare Desert");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aSeedTurnsAwayTheUnready(GameTestHelper helper) {
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ne_seed_test"));
        BlockState second = DreamBlocks.SEED.get().defaultBlockState().setValue(SeedBlock.CATEGORY, 2);
        BlockState fourth = second.setValue(SeedBlock.CATEGORY, 4);
        Soul.setRank(player, Rank.SLEEPER);
        Seeds.challenge(player, at, second);
        helper.assertTrue(!Nightmares.inTrial(player), "A Sleeper cannot enter a Nightmare through a Seed");
        Soul.setRank(player, Rank.MASTER);
        Seeds.challenge(player, at, second);
        helper.assertTrue(!Nightmares.inTrial(player), "A Master has nothing more to gain from a Second Nightmare");
        Soul.setRank(player, Rank.AWAKENED);
        Seeds.challenge(player, at, fourth);
        helper.assertTrue(!Nightmares.inTrial(player), "An Awakened is turned away from a Fourth Nightmare");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theAltarsGiveTheKnives(GameTestHelper helper) {
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ne_altar_test"));
        Soul.of(player).resetDream();
        helper.assertTrue(!Altars.offer(player, at, GatewayBlock.Form.BLACK_ALTAR, new ItemStack(Items.BONE)), "The black altar wants blood, not bones");
        helper.assertTrue(Altars.offer(player, at, GatewayBlock.Form.BLACK_ALTAR, new ItemStack(Items.IRON_SWORD)), "A cut palm on the black altar...");
        helper.assertTrue(player.getInventory().countItem(SeedEntities.IVORY_KNIFE.get()) == 1, "...is repaid with the ivory knife");
        Altars.offer(player, at, GatewayBlock.Form.BLACK_ALTAR, new ItemStack(Items.IRON_SWORD));
        helper.assertTrue(player.getInventory().countItem(SeedEntities.IVORY_KNIFE.get()) == 1, "but only once");
        player.getInventory().add(new ItemStack(SeedEntities.ANCIENT_COIN.get(), 64));
        Altars.offer(player, at, GatewayBlock.Form.WHITE_ALTAR, new ItemStack(SeedEntities.ANCIENT_COIN.get()));
        helper.assertTrue(player.getInventory().countItem(SeedEntities.OBSIDIAN_KNIFE.get()) == 0
            && player.getInventory().countItem(SeedEntities.ANCIENT_COIN.get()) == 64, "A few coins buy nothing from the white altar");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void anOpenGateSendsItsCreatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        NightmareGateEntity gate = NightmareGateEntity.open(level, helper.absolutePos(new BlockPos(1, 2, 1)), "test_seed", 2);
        CompoundTag tag = gate.saveWithoutId(new CompoundTag());
        tag.putInt("Age", NightmareGateEntity.FORMING - 3);
        gate.load(tag);
        helper.assertTrue(!gate.isOpen(), "A Gate takes its time to open");
        helper.succeedWhen(() -> {
            helper.assertTrue(gate.isOpen(), "The Gate tears open");
            List<Mob> sent = level.getEntitiesOfClass(Mob.class, gate.getBoundingBox().inflate(16.0), gate::sent);
            helper.assertTrue(sent.size() >= 5, "Its first wave comes through");
            for (Mob mob : sent) {
                helper.assertTrue(Creatures.ofNightmare(mob), "Everything that comes through is a Nightmare Creature");
                mob.discard();
            }

            gate.discard();
        });
    }
}
