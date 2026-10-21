package net.logosnumen.nightmareevolved.dream.gen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.seed.SeedSites;
import net.logosnumen.nightmareevolved.dream.gen.shape.BastionShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.BurnedForestShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.DesertShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.GlassHellShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.GodgraveShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.IslesShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.MoonriverShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.MountainShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.RavenheartShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.SeaShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.ShoreShaper;
import net.logosnumen.nightmareevolved.dream.gen.shape.WeepingShaper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * The Dream Realm's land for one world's seed: each region's shaper, the landmarks, and the way they come together in a
 * column. Everything here is pure: a column is the same whenever and wherever it is asked for, so the server can ask
 * where the ground is at any place without generating it.
 */
public final class RealmTerrain {
    private static final Map<Long, RealmTerrain> BY_SEED = new ConcurrentHashMap<>();
    private static final int INDEX = 256;
    public final long seed;
    private final EnumMap<Region, Shaper> shapers = new EnumMap<>(Region.class);
    private final List<Landmark> landmarks = new ArrayList<>();
    private final Map<Long, List<Landmark>> index = new ConcurrentHashMap<>();
    private final Noise border;
    private final IslesShaper isles;
    private final ThreadLocal<Column> scratch = ThreadLocal.withInitial(Column::new);

    private RealmTerrain(long seed) {
        this.seed = seed;
        this.border = new Noise(Noise.mix(seed, "border"), 2);
        this.isles = new IslesShaper(seed);
        ShoreShaper shore = new ShoreShaper(seed);
        MountainShaper mountains = new MountainShaper(seed);
        GodgraveShaper godgrave = new GodgraveShaper(seed);
        this.shapers.put(Region.FORGOTTEN_SHORE, shore);
        this.shapers.put(Region.SEA_OF_ASH, shore);
        this.shapers.put(Region.LAND_OF_BARROWS, shore);
        this.shapers.put(Region.BURNED_FOREST, new BurnedForestShaper(seed));
        this.shapers.put(Region.NIGHTMARE_DESERT, new DesertShaper(seed));
        this.shapers.put(Region.HOLLOW_MOUNTAINS, mountains);
        this.shapers.put(Region.UNDERWORLD, mountains);
        this.shapers.put(Region.MOONRIVER_PLAIN, new MoonriverShaper(seed));
        this.shapers.put(Region.GODGRAVE, godgrave);
        this.shapers.put(Region.GODGRAVE_HOLLOW, godgrave);
        this.shapers.put(Region.GLASS_HELL, new GlassHellShaper(seed));
        this.shapers.put(Region.CHAINED_ISLES, this.isles);
        this.shapers.put(Region.RAVENHEART, new RavenheartShaper(seed));
        this.shapers.put(Region.BONE_SEA, new SeaShaper(seed, true));
        this.shapers.put(Region.BASTION, new BastionShaper(seed));
        this.shapers.put(Region.WEEPING_GODDESS, new WeepingShaper(seed));
        this.shapers.put(Region.STORMSEA, new SeaShaper(seed, false));
        Landmarks.addAll(this, this.landmarks);
        for (Landmark landmark : this.landmarks) {
            for (int gx = Math.floorDiv(landmark.minX(), INDEX); gx <= Math.floorDiv(landmark.maxX(), INDEX); gx++) {
                for (int gz = Math.floorDiv(landmark.minZ(), INDEX); gz <= Math.floorDiv(landmark.maxZ(), INDEX); gz++) {
                    this.index.computeIfAbsent(key(gx, gz), k -> new ArrayList<>()).add(landmark);
                }
            }
        }
    }

    public static RealmTerrain of(long seed) {
        return BY_SEED.computeIfAbsent(seed, RealmTerrain::new);
    }

    /** The terrain for the world a generator's random state was made for. */
    public static RealmTerrain of(RandomState random) {
        return of(random.getOrCreateRandomFactory(NightmareEvolved.id("dream_realm")).at(0, 0, 0).nextLong());
    }

    public static RealmTerrain of(ServerLevel level) {
        return of(level.getChunkSource().randomState());
    }

    private static long key(int gx, int gz) {
        return ((long)gx << 32) ^ (gz & 0xFFFFFFFFL);
    }

    public Shaper shaper(Region region) {
        return this.shapers.get(region);
    }

    public IslesShaper isles() {
        return this.isles;
    }

    public List<Landmark> landmarks() {
        return this.landmarks;
    }

    public List<Landmark> landmarksAt(int x, int z) {
        List<Landmark> here = this.index.get(key(Math.floorDiv(x, INDEX), Math.floorDiv(z, INDEX)));
        return here == null ? List.of() : here;
    }

    /** The bare land's height at a place, blended across borders, before anything grows or is built on it. */
    public double ground(int x, int z) {
        RealmMap.Blend blend = RealmMap.blend(x, z);
        return this.ground(blend, x, z);
    }

    private double ground(RealmMap.Blend blend, int x, int z) {
        double height = 0.0;
        for (int i = 0; i < blend.count; i++) {
            height += blend.weights[i] * this.shapers.get(blend.regions[i]).ground(x, z, i == 0 ? blend.edge : 0.0);
        }

        return height;
    }

    /** Builds a whole column: land, seas, the dead god's bones, and the landmarks. */
    public void build(Column column, int x, int z) {
        column.reset(x, z);
        RealmMap.Blend blend = RealmMap.blend(x, z);
        double height = this.ground(blend, x, z);
        column.ground = height;
        Region dominant = blend.regions[0];
        if (blend.count > 1) {
            double best = -1.0;
            double jitter = 0.14 * this.border.at(x / 15.0, z / 15.0);
            for (int i = 0; i < blend.count; i++) {
                double score = blend.weights[i] + (i == 0 ? jitter : -jitter / blend.count);
                if (score > best) {
                    best = score;
                    dominant = blend.regions[i];
                }
            }
        }

        this.shapers.get(dominant).build(column, height, blend);
        if (dominant != Region.STORMSEA && dominant != Region.BONE_SEA && (blend.weight(Region.STORMSEA) > 0.0 || blend.weight(Region.BONE_SEA) > 0.0)) {
            Ground.sea(column, Ground.SEA_LEVEL);
        }

        Godgrave.paint(column);
        for (Landmark landmark : this.landmarksAt(x, z)) {
            if (landmark.covers(x, z)) {
                landmark.paint(column, this);
            }
        }

        SeedSites.paint(this, column);
    }

    /** The highest solid block at a place, or one below the world over open sky. */
    public int surface(int x, int z) {
        Column column = this.scratch.get();
        this.build(column, x, z);
        return column.topSolid();
    }

    /** A column built at a place, for looking at; it is reused, so it must not be kept. */
    public Column column(int x, int z) {
        Column column = this.scratch.get();
        this.build(column, x, z);
        return column;
    }
}
