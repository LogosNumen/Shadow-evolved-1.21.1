package net.logosnumen.nightmareevolved.vfx;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The effect particles: white sprites that every effect tints to its own colours. */
public final class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, NightmareEvolved.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> MOTE = tinted("mote");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> SPARK = tinted("spark");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> EMBER = tinted("ember");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> WISP = tinted("wisp");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> GLYPH = tinted("glyph");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> PULSE = tinted("pulse");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> SHARD = tinted("shard");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> RING = tinted("ring");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> HALO = tinted("halo");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> SIGIL = tinted("sigil");
    public static final DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> SWEEP = tinted("sweep");

    private ModParticles() {
    }

    private static DeferredHolder<ParticleType<?>, TintedParticleOptions.Type> tinted(String name) {
        return PARTICLE_TYPES.register(name, TintedParticleOptions.Type::new);
    }

    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
