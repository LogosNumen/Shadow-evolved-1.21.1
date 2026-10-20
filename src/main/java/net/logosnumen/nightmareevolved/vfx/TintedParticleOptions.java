package net.logosnumen.nightmareevolved.vfx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One of the mod's effect particles, tinted. The sprites are white; the colour, a size multiplier and an optional
 * lifetime (0 keeps the particle's own) come with each particle, so one sprite serves every Element. Flat particles
 * (rings, rune circles, blade arcs) also take a facing: {@code yaw} turns them about the vertical, in radians, the
 * way an entity's yaw does, and {@code roll} tips them about their forward axis, so 0 lies flat and a quarter turn
 * stands them upright.
 */
public record TintedParticleOptions(ParticleType<TintedParticleOptions> type, int colour, float scale, int lifetime, float yaw, float roll)
    implements ParticleOptions {
    public TintedParticleOptions(ParticleType<TintedParticleOptions> type, int colour, float scale, int lifetime) {
        this(type, colour, scale, lifetime, 0.0F, 0.0F);
    }

    public static MapCodec<TintedParticleOptions> codec(ParticleType<TintedParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.INT.fieldOf("colour").forGetter(TintedParticleOptions::colour),
                Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(TintedParticleOptions::scale),
                Codec.INT.optionalFieldOf("lifetime", 0).forGetter(TintedParticleOptions::lifetime),
                Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(TintedParticleOptions::yaw),
                Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(TintedParticleOptions::roll)
            )
            .apply(i, (colour, scale, lifetime, yaw, roll) -> new TintedParticleOptions(type, colour, scale, lifetime, yaw, roll)));
    }

    public static StreamCodec<ByteBuf, TintedParticleOptions> streamCodec(ParticleType<TintedParticleOptions> type) {
        return StreamCodec.composite(
            ByteBufCodecs.INT, TintedParticleOptions::colour,
            ByteBufCodecs.FLOAT, TintedParticleOptions::scale,
            ByteBufCodecs.VAR_INT, TintedParticleOptions::lifetime,
            ByteBufCodecs.FLOAT, TintedParticleOptions::yaw,
            ByteBufCodecs.FLOAT, TintedParticleOptions::roll,
            (colour, scale, lifetime, yaw, roll) -> new TintedParticleOptions(type, colour, scale, lifetime, yaw, roll)
        );
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    /** A particle type whose options are tinted ones. */
    public static final class Type extends ParticleType<TintedParticleOptions> {
        private final MapCodec<TintedParticleOptions> codec = TintedParticleOptions.codec(this);
        private final StreamCodec<ByteBuf, TintedParticleOptions> streamCodec = TintedParticleOptions.streamCodec(this);

        public Type() {
            super(false);
        }

        @Override
        public MapCodec<TintedParticleOptions> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<ByteBuf, TintedParticleOptions> streamCodec() {
            return this.streamCodec;
        }
    }
}
