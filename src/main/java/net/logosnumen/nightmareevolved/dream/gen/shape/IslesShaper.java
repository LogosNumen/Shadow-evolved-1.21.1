package net.logosnumen.nightmareevolved.dream.gen.shape;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.logosnumen.nightmareevolved.dream.gen.Trees;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Chained Isles: islands floating between the Sky Above and the Sky Below, bound to one another by gargantuan iron
 * chains, with drifting rocks and old masonry between them. Every island has its own nature: meadows white with
 * flowers that put sleepers to sleep forever, forests, bare rock, ash, ruins, a quiet lake. At the centre is the Tear,
 * a vast hole where islands have fallen away. The named isles stand where the novel puts them.
 */
public final class IslesShaper implements Shaper {
    public static final int CELL = 150;
    /** No island stands this close to the middle of the Tear. */
    public static final double TEAR = 360.0;
    private static final double NO_GROUND = Column.MIN_Y - 40;
    private final long seed;
    private final Noise rim;
    private final Noise bulge;
    private final Noise stalactites;
    private final Noise height;
    private final Noise cover;
    private final Noise rocks;

    /** What an island is like on top. */
    public enum Kind {
        MEADOW, FOREST, ROCKY, ASH, RUINS, LAKE, SANCTUARY, TWISTED, JAGGED, GROVE, CORRUPTED
    }

    /** One island: its middle, size, the height of its top, and how far its underside hangs. */
    public record Island(double x, double z, double radius, int top, int depth, Kind kind, int salt) {
    }

    private record Named(Places.Place place, double radius, int top, Kind kind) {
    }

    private static final List<Named> NAMED = List.of(
        new Named(Places.SANCTUARY_OF_NOCTIS, 46.0, 170, Kind.SANCTUARY),
        new Named(Places.NORTHERN_ISLAND, 74.0, 176, Kind.MEADOW),
        new Named(Places.IRON_HAND, 40.0, 160, Kind.ROCKY),
        new Named(Places.RECKONING, 38.0, 168, Kind.ROCKY),
        new Named(Places.SHIPWRECK, 46.0, 158, Kind.MEADOW),
        new Named(Places.TWISTED_TREES, 52.0, 165, Kind.TWISTED),
        new Named(Places.JAGGED_ROCKS, 40.0, 172, Kind.JAGGED),
        new Named(Places.DESECRATED_GROVE, 36.0, 162, Kind.GROVE),
        new Named(Places.CORRUPTED_ISLAND, 44.0, 166, Kind.CORRUPTED)
    );

    public IslesShaper(long seed) {
        this.seed = seed;
        this.rim = new Noise(Noise.mix(seed, "isles.rim"), 2);
        this.bulge = new Noise(Noise.mix(seed, "isles.bulge"), 2);
        this.stalactites = new Noise(Noise.mix(seed, "isles.stalactites"), 1);
        this.height = new Noise(Noise.mix(seed, "isles.height"), 1);
        this.cover = new Noise(Noise.mix(seed, "isles.cover"), 1);
        this.rocks = new Noise(Noise.mix(seed, "isles.rocks"), 1);
    }

    @Override
    public double ground(int x, int z, double edge) {
        return NO_GROUND;
    }

    private static final Island NONE = new Island(0.0, 0.0, 0.0, 0, 0, Kind.ROCKY, 0);
    private final ThreadLocal<Long2ObjectOpenHashMap<Island>> cache = ThreadLocal.withInitial(Long2ObjectOpenHashMap::new);

    /** The island in a grid cell, or none (remembered, as every column asks about the cells around it). */
    @Nullable
    public Island island(int i, int j) {
        Long2ObjectOpenHashMap<Island> cache = this.cache.get();
        long key = ((long)i << 32) ^ (j & 0xFFFFFFFFL);
        Island island = cache.get(key);
        if (island == null) {
            if (cache.size() > 4096) {
                cache.clear();
            }

            island = this.make(i, j);
            cache.put(key, island == null ? NONE : island);
        }

        return island == NONE ? null : island;
    }

    @Nullable
    private Island make(int i, int j) {
        double cx = i * CELL + CELL / 2.0;
        double cz = j * CELL + CELL / 2.0;
        for (Named named : NAMED) {
            if (Math.floorDiv(named.place.x(), CELL) == i && Math.floorDiv(named.place.z(), CELL) == j) {
                int depth = (int)(named.radius * 1.5);
                return new Island(named.place.x(), named.place.z(), named.radius, named.top, depth, named.kind, i * 31 + j);
            }
        }

        if (Noise.hash(this.seed, i, j, 71) > 0.74) {
            return null;
        }

        double x = cx + (Noise.hash(this.seed, i, j, 72) - 0.5) * CELL * 0.45;
        double z = cz + (Noise.hash(this.seed, i, j, 73) - 0.5) * CELL * 0.45;
        if (RealmMap.regionAt(x, z) != Region.CHAINED_ISLES || Places.TEAR.distance(x, z) < TEAR) {
            return null;
        }

        double radius = 20.0 + 30.0 * Noise.hash(this.seed, i, j, 74);
        for (Named named : NAMED) {
            if (named.place.distance(x, z) < named.radius + radius + 24.0) {
                return null;
            }
        }

        int top = 150 + (int)(38.0 * this.height.at(i * 0.37, j * 0.37));
        int depth = (int)(radius * (1.0 + 0.6 * Noise.hash(this.seed, i, j, 75)));
        Kind kind = switch ((int)(Noise.hash(this.seed, i, j, 76) * 9)) {
            case 0, 1 -> Kind.MEADOW;
            case 2, 3 -> Kind.FOREST;
            case 4 -> Kind.ROCKY;
            case 5 -> Kind.ASH;
            case 6 -> Kind.RUINS;
            case 7 -> Kind.LAKE;
            default -> Kind.GROVE;
        };
        return new Island(x, z, radius, top, depth, kind, i * 31 + j);
    }

    /** The island under or over a place, if any. */
    @Nullable
    public Island islandAt(double x, double z) {
        int ci = Math.floorDiv((int)Math.floor(x), CELL);
        int cj = Math.floorDiv((int)Math.floor(z), CELL);
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                Island island = this.island(ci + di, cj + dj);
                if (island != null && this.reach(island, x, z) < 1.0) {
                    return island;
                }
            }
        }

        return null;
    }

    /** How far out a place lies on an island, as a fraction of its ragged edge: under 1 on it. */
    public double reach(Island island, double x, double z) {
        double dx = x - island.x;
        double dz = z - island.z;
        double radius = island.radius * (1.0 + 0.2 * this.rim.at(x / 23.0, z / 23.0));
        return Math.sqrt(dx * dx + dz * dz) / radius;
    }

    /** The height of an island's top at a place on it. */
    public int topAt(Island island, double x, double z) {
        double d = this.reach(island, x, z);
        if (island.kind == Kind.SANCTUARY) {
            // The Sanctuary's ground was levelled long ago: flat within its ring of menhirs, so its pool and altar sit true.
            return island.top + (d < 0.9 ? 0 : (int)Math.round(1.2 * this.bulge.at(x / 15.0, z / 15.0)));
        }

        double rise = Math.min(6.0, island.radius * 0.12) * (1.0 - d * d);
        if (island.kind == Kind.LAKE && d < 0.45) {
            rise -= 5.0 * (1.0 - d / 0.45);
        }

        return island.top + (int)Math.round(rise + 1.2 * this.bulge.at(x / 15.0, z / 15.0));
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        if (height > Column.MIN_Y + 2) {
            // The edge of a neighbouring land, falling away into the Sky Below.
            int top = Mth.floor(height);
            Ground.stone(column, top, false);
            column.set(top, Blocks.STONE.defaultBlockState());
        }

        Island island = this.islandAt(x, z);
        if (island != null) {
            this.island(column, island);
        }

        this.rocks(column);
        this.chains(column);
    }

    /**
     * The chains between islands: each island may be bound to its neighbours east and south, and the islands at the
     * Tear's rim still hold the broken ends of chains that once reached across it.
     */
    private void chains(Column column) {
        int ci = Math.floorDiv(column.x, CELL);
        int cj = Math.floorDiv(column.z, CELL);
        for (int di = -2; di <= 1; di++) {
            for (int dj = -2; dj <= 1; dj++) {
                int i = ci + di;
                int j = cj + dj;
                Island a = this.island(i, j);
                if (a == null) {
                    continue;
                }

                for (int dir = 0; dir < 2; dir++) {
                    if (Noise.hash(this.seed, i, j, 101 + dir) > 0.62) {
                        continue;
                    }

                    Island b = dir == 0 ? this.island(i + 1, j) : this.island(i, j + 1);
                    if (b != null) {
                        this.link(column, a, b.x, b.z, b.radius, b.top);
                    }
                }

                double tear = Places.TEAR.distance(a.x, a.z);
                if (tear < TEAR + 260.0 && Noise.hash(this.seed, i, j, 103) < 0.7) {
                    // A broken chain, hanging out over the Tear.
                    double ux = (Places.TEAR.x() - a.x) / tear;
                    double uz = (Places.TEAR.z() - a.z) / tear;
                    double reach = a.radius * 0.85 + 30.0 + 50.0 * Noise.hash(this.seed, i, j, 104);
                    this.link(column, a, a.x + ux * reach, a.z + uz * reach, 0.0, a.top - 40);
                }
            }
        }
    }

    /** One chain from an island's edge to a point (another island's edge, or a broken end), sagging between. */
    private void link(Column column, Island a, double bx, double bz, double bRadius, int bTop) {
        double dx = bx - a.x;
        double dz = bz - a.z;
        double full = Math.sqrt(dx * dx + dz * dz);
        if (full < 1.0) {
            return;
        }

        double ux = dx / full;
        double uz = dz / full;
        double sx = a.x + ux * a.radius * 0.8;
        double sz = a.z + uz * a.radius * 0.8;
        double length = full - a.radius * 0.8 - bRadius * 0.8;
        if (length < 4.0) {
            return;
        }

        double ox = column.x + 0.5 - sx;
        double oz = column.z + 0.5 - sz;
        double along = ox * ux + oz * uz;
        double across = Math.abs(-ox * uz + oz * ux);
        if (along < 0.0 || along > length || across > 5.0) {
            return;
        }

        double t = along / length;
        double ya = a.top - 7;
        double yb = bTop - 7;
        double y = Mth.lerp(t, ya, yb) - 0.09 * length * 4.0 * t * (1.0 - t);
        // Gargantuan links, each ten blocks long, alternately lying flat and standing on edge.
        int k = (int)Math.floor(along / 9.0);
        double sc = along - k * 9.0 - 4.5;
        BlockState iron = Noise.hash(this.seed, column.x, column.z, 105) < 0.15 ? Blocks.RAW_IRON_BLOCK.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState();
        int yc = (int)Math.round(y);
        double ox2 = (sc / 5.5) * (sc / 5.5);
        double ix2 = (sc / 3.2) * (sc / 3.2);
        if ((k & 1) == 0) {
            double outer = ox2 + (across / 4.2) * (across / 4.2);
            double inner = ix2 + (across / 2.0) * (across / 2.0);
            if (outer <= 1.0 && inner > 1.0) {
                column.fill(yc - 1, yc + 1, iron);
            }
        } else if (across <= 1.6) {
            double ho = 4.2 * Math.sqrt(Math.max(0.0, 1.0 - ox2));
            double hi = 2.0 * Math.sqrt(Math.max(0.0, 1.0 - ix2));
            column.fill((int)Math.round(y - ho), (int)Math.round(y - hi), iron);
            column.fill((int)Math.round(y + hi), (int)Math.round(y + ho), iron);
        }
    }

    private void island(Column column, Island island) {
        int x = column.x;
        int z = column.z;
        double d = this.reach(island, x, z);
        int top = this.topAt(island, x, z);
        int bottom = island.top - (int)(island.depth * Math.pow(1.0 - d, 0.55)) - (int)(9.0 * Math.max(0.0, this.stalactites.at(x / 7.0, z / 7.0)));
        column.fill(bottom, top, Blocks.STONE.defaultBlockState());
        column.fill(bottom, Math.min(top, bottom + 3), Blocks.TUFF.defaultBlockState());
        double cover = this.cover.at(x / 7.0, z / 7.0);
        long salt = island.salt;
        switch (island.kind) {
            case MEADOW, SANCTUARY, GROVE, FOREST, LAKE, RUINS -> {
                column.fill(top - 3, top - 1, Blocks.DIRT.defaultBlockState());
                column.set(top, Blocks.GRASS_BLOCK.defaultBlockState());
                if (island.kind == Kind.LAKE && top < island.top - 1) {
                    column.set(top, Blocks.SAND.defaultBlockState());
                    column.flood(top + 1, island.top - 1, Ground.WATER);
                } else if (island.kind == Kind.MEADOW && cover > 0.2) {
                    // White flowers: lovely, and deadly to anyone who falls asleep among them.
                    column.set(top + 1, cover > 0.45 ? Blocks.LILY_OF_THE_VALLEY.defaultBlockState() : Blocks.OXEYE_DAISY.defaultBlockState());
                } else if (cover > 0.35) {
                    column.set(top + 1, Blocks.SHORT_GRASS.defaultBlockState());
                }
            }
            case ROCKY -> {
                column.set(top, cover > 0.3 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
            }
            case ASH -> {
                column.fill(top - 2, top, DreamBlocks.ASH.get().defaultBlockState());
                if (cover > 0.5) {
                    column.set(top - 1, Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            }
            case TWISTED -> {
                column.fill(top - 2, top, Blocks.PODZOL.defaultBlockState());
                column.fill(top - 3, top - 3, Blocks.ROOTED_DIRT.defaultBlockState());
            }
            case JAGGED -> {
                double spike = Math.max(0.0, this.rocks.at(x / 4.0, z / 4.0));
                column.fill(top, top + (int)(spike * 14.0 * (1.0 - d)), Blocks.ANDESITE.defaultBlockState());
            }
            case CORRUPTED -> {
                column.set(top, cover > 0.0 ? Blocks.SCULK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
                if (cover > 0.6) {
                    column.set(top + 1, Blocks.SCULK_VEIN.defaultBlockState());
                }
            }
        }

        if (island.kind == Kind.FOREST || island.kind == Kind.TWISTED || island.kind == Kind.GROVE) {
            Trees.paint(column, 9, (i, j) -> this.tree(island, i, j, salt));
        }
    }

    @Nullable
    private Trees.Tree tree(Island island, int i, int j, long salt) {
        double chance = switch (island.kind) {
            case FOREST -> 0.55;
            case TWISTED -> 0.7;
            default -> 0.25;
        };
        if (Noise.hash(this.seed + salt, i, j, 81) > chance) {
            return null;
        }

        int x = i * 9 + 2 + (int)(Noise.hash(this.seed + salt, i, j, 82) * 5);
        int z = j * 9 + 2 + (int)(Noise.hash(this.seed + salt, i, j, 83) * 5);
        if (this.reach(island, x, z) > 0.8) {
            return null;
        }

        int base = this.topAt(island, x, z) + 1;
        return switch (island.kind) {
            case TWISTED -> new Trees.Tree(x, z, base, 6 + (int)(Noise.hash(this.seed, i, j, 84) * 4), 3,
                Blocks.DARK_OAK_LOG.defaultBlockState(), Blocks.DARK_OAK_LEAVES.defaultBlockState(), Trees.Crown.WEEPING);
            case GROVE -> new Trees.Tree(x, z, base, 7, 4, Blocks.BIRCH_LOG.defaultBlockState(), Blocks.BIRCH_LEAVES.defaultBlockState(), Trees.Crown.ROUND);
            default -> new Trees.Tree(x, z, base, 6 + (int)(Noise.hash(this.seed, i, j, 85) * 4), 3,
                Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), Trees.Crown.ROUND);
        };
    }

    /** Rocks and broken masonry drifting in the air between the islands. */
    private void rocks(Column column) {
        int cell = 44;
        int ci = Math.floorDiv(column.x, cell);
        int cj = Math.floorDiv(column.z, cell);
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int i = ci + di;
                int j = cj + dj;
                if (Noise.hash(this.seed, i, j, 91) > 0.08) {
                    continue;
                }

                double rx = i * cell + cell * Noise.hash(this.seed, i, j, 92);
                double rz = j * cell + cell * Noise.hash(this.seed, i, j, 93);
                double r = 1.5 + 3.0 * Noise.hash(this.seed, i, j, 94);
                double dx = column.x - rx;
                double dz = column.z - rz;
                double flat = r * r - dx * dx - dz * dz;
                if (flat <= 0.0 || this.islandAt(rx, rz) != null) {
                    continue;
                }

                int y = 105 + (int)(130.0 * Noise.hash(this.seed, i, j, 95));
                int half = (int)Math.sqrt(flat);
                boolean masonry = Noise.hash(this.seed, i, j, 96) < 0.35;
                BlockState stone = masonry ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
                column.fill(y - half, y + Math.max(0, half / 2), stone);
                if (!masonry) {
                    column.set(y + Math.max(0, half / 2), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
            }
        }
    }
}
