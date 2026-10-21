package net.logosnumen.nightmareevolved.dream.gen;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/** Gives every place in the Dream Realm the biome of its region, or of the part of a region it lies in. */
public final class RealmBiomes extends BiomeSource {
    public static final MapCodec<RealmBiomes> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(RegistryOps.<Biome, RealmBiomes>retrieveGetter(Registries.BIOME)).apply(instance, instance.stable(RealmBiomes::new))
    );
    private final EnumMap<Region, Holder<Biome>> biomes = new EnumMap<>(Region.class);

    public RealmBiomes(HolderGetter<Biome> getter) {
        for (Region region : Region.values()) {
            this.biomes.put(region, getter.getOrThrow(region.biome()));
        }
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return List.copyOf(this.biomes.values()).stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int bx = QuartPos.toBlock(x) + 2;
        int by = QuartPos.toBlock(y) + 2;
        int bz = QuartPos.toBlock(z) + 2;
        return this.biomes.get(RealmMap.part(bx, by, bz));
    }

    @Override
    public void addDebugInfo(List<String> info, BlockPos pos, Climate.Sampler sampler) {
        info.add("Dream Realm region: " + RealmMap.part(pos.getX(), pos.getY(), pos.getZ()).id());
    }
}
