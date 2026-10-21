package net.logosnumen.nightmareevolved.dream.gen;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/**
 * Makes the Dream Realm. Each column is built whole by {@link RealmTerrain} (its regions' land, the dead god of
 * Godgrave, the landmarks) and written straight into the chunk; there are no carvers or vanilla structures. Landmarks
 * set up their chests, Gateways and Seeds as the chunk is decorated.
 */
public final class RealmGenerator extends ChunkGenerator {
    public static final MapCodec<RealmGenerator> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
            .apply(instance, instance.stable(RealmGenerator::new))
    );

    public RealmGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager,
        ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getGenDepth() {
        return Column.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return Ground.SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return Column.MIN_Y;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_dream_realm", () -> this.fill(RealmTerrain.of(random), chunk)),
            Util.backgroundExecutor());
    }

    private ChunkAccess fill(RealmTerrain terrain, ChunkAccess chunk) {
        Set<LevelChunkSection> held = new HashSet<>();
        for (LevelChunkSection section : chunk.getSections()) {
            section.acquire();
            held.add(section);
        }

        try {
            Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
            ChunkPos pos = chunk.getPos();
            Column column = new Column();
            int minY = chunk.getMinBuildHeight();
            int maxY = chunk.getMaxBuildHeight() - 1;
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    terrain.build(column, pos.getMinBlockX() + lx, pos.getMinBlockZ() + lz);
                    for (int y = Math.max(minY, Column.MIN_Y); y <= Math.min(maxY, Column.MAX_Y); y++) {
                        BlockState state = column.get(y);
                        if (state == null || state.isAir()) {
                            continue;
                        }

                        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                        section.setBlockState(lx, y & 15, lz, state, false);
                        floor.update(lx, y, lz, state);
                        surface.update(lx, y, lz, state);
                    }
                }
            }
        } finally {
            for (LevelChunkSection section : held) {
                section.release();
            }
        }

        return chunk;
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);
        RealmTerrain terrain = RealmTerrain.of(level.getLevel().getChunkSource().randomState());
        ChunkPos pos = chunk.getPos();
        for (Landmark landmark : terrain.landmarksAt(pos.getMinBlockX(), pos.getMinBlockZ())) {
            if (landmark.touches(pos)) {
                landmark.decorate(level, pos, terrain);
            }
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        Column column = RealmTerrain.of(random).column(x, z);
        for (int y = Column.MAX_Y; y >= Column.MIN_Y; y--) {
            BlockState state = column.get(y);
            if (state != null && type.isOpaque().test(state)) {
                return y + 1;
            }
        }

        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        Column column = RealmTerrain.of(random).column(x, z);
        BlockState[] states = new BlockState[Column.HEIGHT];
        for (int i = 0; i < Column.HEIGHT; i++) {
            BlockState state = column.get(Column.MIN_Y + i);
            states[i] = state == null ? Blocks.AIR.defaultBlockState() : state;
        }

        return new NoiseColumn(Column.MIN_Y, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        RealmTerrain terrain = RealmTerrain.of(random);
        info.add(String.format("Dream Realm: %s, ground %.1f", RealmMap.part(pos.getX(), pos.getY(), pos.getZ()).id(), terrain.ground(pos.getX(), pos.getZ())));
    }
}
