package net.logosnumen.nightmareevolved.dream.seed;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gate.Gateways;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Godgrave;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.RealmTerrain;
import net.logosnumen.nightmareevolved.dream.gen.landmark.DesertPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.IslePlaces;
import net.logosnumen.nightmareevolved.dream.gen.shape.IslesShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Where the Seeds of Nightmare grow. Three are the novel's own: the Dark Seed in the Ivory Tower, the block of black
 * stone at the heart of the Tomb of Ariel, and the Seed on the ziggurat in Godgrave's hollow. The rest are scattered
 * over the realm, a few to a region (many in the Nightmare Desert, where countless Seeds seem to bloom), each on a dais
 * of black stone, of a Category that suits the region's danger. Where they are depends on the world's seed, the same
 * every time it is asked.
 */
public final class SeedSites {
    public static final int CELL = 700;
    /** A Seed: where it stands (the block a challenger touches), its Category, and the region it grows in. */
    public record Site(String id, BlockPos pos, int category, Region region, SeedBlock.Form form) {
    }

    public static final List<Site> CANON = List.of(
        new Site("dark_seed", new BlockPos(Places.IVORY_TOWER.x(), IslePlaces.IvoryTower.TOP + 1, Places.IVORY_TOWER.z()), 2, Region.CHAINED_ISLES,
            SeedBlock.Form.SHACKLES),
        new Site("tomb_of_ariel", new BlockPos(Places.TOMB_OF_ARIEL.x(), DesertPlaces.TombOfAriel.BASE + 1, Places.TOMB_OF_ARIEL.z() - 1), 3,
            Region.NIGHTMARE_DESERT, SeedBlock.Form.STONE),
        new Site("mictlan_ziggurat", new BlockPos(Places.MICTLAN_ZIGGURAT.x(), Godgrave.VAULT_FLOOR + 37, Places.MICTLAN_ZIGGURAT.z()), 2,
            Region.GODGRAVE, SeedBlock.Form.STONE)
    );

    private SeedSites() {
    }

    /** How likely a cell of a region is to hold a Seed. */
    private static double chance(Region region) {
        return switch (region) {
            case NIGHTMARE_DESERT -> 0.7;
            case GODGRAVE, CHAINED_ISLES, BURNED_FOREST -> 0.3;
            case FORGOTTEN_SHORE, MOONRIVER_PLAIN, GLASS_HELL, HOLLOW_MOUNTAINS -> 0.25;
            case RAVENHEART, WEEPING_GODDESS -> 0.15;
            case BASTION -> 0.1;
            default -> 0.0;
        };
    }

    /** The scattered Seed in a grid cell, or none. */
    @Nullable
    public static Site site(RealmTerrain terrain, int i, int j) {
        long seed = terrain.seed;
        int x = i * CELL + 100 + (int)(Noise.hash(seed, i, j, 301) * (CELL - 200));
        int z = j * CELL + 100 + (int)(Noise.hash(seed, i, j, 302) * (CELL - 200));
        Region region = RealmMap.part(x, 80, z);
        if (Noise.hash(seed, i, j, 303) > chance(region.whole()) || region == Region.LAND_OF_BARROWS) {
            return null;
        }

        for (Gateways.Gateway gateway : Gateways.CANON) {
            if (gateway.pos().distManhattan(new BlockPos(x, gateway.pos().getY(), z)) < 500) {
                return null;
            }
        }

        int y = base(terrain, region, x, z);
        if (y < Column.MIN_Y + 10) {
            return null;
        }

        Region whole = region.whole();
        int span = whole.seedMax - whole.seedMin + 1;
        double roll = Noise.hash(seed, i, j, 304);
        int category = whole.seedMin + Math.min(span - 1, (int)(roll * roll * span));
        return new Site("seed_" + i + "_" + j, new BlockPos(x, y + 1, z), category, whole, SeedBlock.Form.STONE);
    }

    /** The ground a Seed's dais stands on: the bare land, or an island's top over the Isles' void. */
    private static int base(RealmTerrain terrain, Region region, int x, int z) {
        if (region == Region.CHAINED_ISLES) {
            IslesShaper.Island island = terrain.isles().islandAt(x, z);
            return island == null || terrain.isles().reach(island, x, z) > 0.6 ? Column.MIN_Y : terrain.isles().topAt(island, x, z);
        }

        double ground = terrain.ground(x, z);
        if (region.whole() == Region.FORGOTTEN_SHORE && RealmMap.Shore.basin(x, z) < 1.0) {
            // In the labyrinth a Seed stands on a mound of coral, clear of the night's black water.
            return Math.max(Mth.floor(ground), 88);
        }

        return Mth.floor(ground);
    }

    /** Every Seed near a place: the canon ones and the scattered ones in the cells around it. */
    public static List<Site> near(RealmTerrain terrain, double x, double z, double range) {
        List<Site> sites = new ArrayList<>();
        for (Site site : CANON) {
            if (Math.hypot(site.pos.getX() - x, site.pos.getZ() - z) <= range) {
                sites.add(site);
            }
        }

        int reach = (int)Math.ceil(range / CELL) + 1;
        int ci = Mth.floor(x / CELL);
        int cj = Mth.floor(z / CELL);
        for (int di = -reach; di <= reach; di++) {
            for (int dj = -reach; dj <= reach; dj++) {
                Site site = site(terrain, ci + di, cj + dj);
                if (site != null && Math.hypot(site.pos.getX() - x, site.pos.getZ() - z) <= range) {
                    sites.add(site);
                }
            }
        }

        return sites;
    }

    /** The Seed whose block is at (or right by) a place. */
    @Nullable
    public static Site at(RealmTerrain terrain, BlockPos pos) {
        for (Site site : near(terrain, pos.getX(), pos.getZ(), 8.0)) {
            if (Math.abs(site.pos.getX() - pos.getX()) <= 2 && Math.abs(site.pos.getZ() - pos.getZ()) <= 2
                && Math.abs(site.pos.getY() - pos.getY()) <= 4) {
                return site;
            }
        }

        return null;
    }

    /** Paints a scattered Seed and its dais into a column: a ring of black stone, the Seed itself rising from it. */
    public static void paint(RealmTerrain terrain, Column column) {
        int i = Math.floorDiv(column.x, CELL);
        int j = Math.floorDiv(column.z, CELL);
        Site site = site(terrain, i, j);
        if (site == null) {
            return;
        }

        int dx = column.x - site.pos.getX();
        int dz = column.z - site.pos.getZ();
        if (Math.abs(dx) > 3 || Math.abs(dz) > 3) {
            return;
        }

        int floor = site.pos.getY() - 1;
        BlockState dais = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        column.clear(floor + 1, floor + 6);
        int under = column.solidBelow(floor);
        column.fill(Math.max(under, floor - 30), floor, Blocks.BLACKSTONE.defaultBlockState());
        column.set(floor, Math.abs(dx) == 3 || Math.abs(dz) == 3 ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState() : dais);
        if (dx == 0 && dz == 0) {
            column.fill(floor + 1, floor + 2, DreamBlocks.SEED.get().seed(site.form, site.category));
        }
    }
}
