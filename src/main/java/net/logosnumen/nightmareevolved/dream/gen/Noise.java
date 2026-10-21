package net.logosnumen.nightmareevolved.dream.gen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * Layered gradient noise for shaping the land: a few octaves of Perlin noise, each finer and fainter than the last.
 * Values stay roughly within -1 to 1.
 */
public final class Noise {
    private final ImprovedNoise[] octaves;
    private final double norm;

    public Noise(long seed, int octaves) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(seed);
        this.octaves = new ImprovedNoise[octaves];
        double total = 0.0;
        for (int i = 0; i < octaves; i++) {
            this.octaves[i] = new ImprovedNoise(random);
            total += 1.0 / (1 << i);
        }

        this.norm = 1.0 / total;
    }

    /** A seed for a named noise, mixed from the world's seed so each noise differs from every other. */
    public static long mix(long seed, String name) {
        long h = seed ^ 0x9E3779B97F4A7C15L;
        for (int i = 0; i < name.length(); i++) {
            h = (h ^ name.charAt(i)) * 0x100000001B3L;
            h ^= h >>> 29;
        }

        return h;
    }

    public double at(double x, double z) {
        double sum = 0.0;
        double scale = 1.0;
        for (ImprovedNoise octave : this.octaves) {
            sum += octave.noise(x * scale, 0.0, z * scale) / scale;
            scale *= 2.0;
        }

        return sum * this.norm;
    }

    public double at(double x, double y, double z) {
        double sum = 0.0;
        double scale = 1.0;
        for (ImprovedNoise octave : this.octaves) {
            sum += octave.noise(x * scale, y * scale, z * scale) / scale;
            scale *= 2.0;
        }

        return sum * this.norm;
    }

    /** Sharp crests where the noise crosses zero: 1 on a ridge line, falling away to 0. */
    public double ridge(double x, double z) {
        return 1.0 - Math.min(1.0, Math.abs(this.at(x, z)) * 2.0);
    }

    /** The noise remapped to 0 to 1. */
    public double unit(double x, double z) {
        return Mth.clamp(this.at(x, z) * 0.5 + 0.5, 0.0, 1.0);
    }

    /** A steady hash of a cell and a salt, as a number from 0 to 1. */
    public static double hash(long seed, int a, int b, int salt) {
        long h = seed + a * 0x632BE59BD9B4E019L + b * 0x9E3779B97F4A7C15L + salt * 0x94D049BB133111EBL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 11) * 0x1.0p-53;
    }

    public static double smooth(double t) {
        t = Mth.clamp(t, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}
