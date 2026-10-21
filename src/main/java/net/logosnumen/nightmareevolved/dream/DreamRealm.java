package net.logosnumen.nightmareevolved.dream;

import com.mojang.serialization.MapCodec;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.dream.gen.RealmBiomes;
import net.logosnumen.nightmareevolved.dream.gen.RealmGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Dream Realm: the vast, dead world the Spell carries Sleepers into, a patchwork of regions with skies and laws of
 * their own. This is its dimension and the generator that makes it.
 */
public final class DreamRealm {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, NightmareEvolved.id("dream_realm"));
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR,
        NightmareEvolved.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE,
        NightmareEvolved.MOD_ID);

    static {
        GENERATORS.register("dream_realm", () -> RealmGenerator.CODEC);
        BIOME_SOURCES.register("dream_realm", () -> RealmBiomes.CODEC);
    }

    private DreamRealm() {
    }

    public static void register(IEventBus modBus) {
        GENERATORS.register(modBus);
        BIOME_SOURCES.register(modBus);
    }

    public static boolean is(Level level) {
        return level.dimension().equals(DIMENSION);
    }

    public static boolean in(Entity entity) {
        return is(entity.level());
    }
}
