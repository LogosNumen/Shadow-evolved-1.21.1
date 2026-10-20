package net.logosnumen.nightmareevolved.client.rig;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** What a box of a creature's body is made of, and how that is painted, pixel by pixel, from the body's colours. */
public enum Material {
    SCALE, BELLY, MEMBRANE, HORN, CLAW, EYE, FEATHER, FLAME, STONE, RUNE, CLOTH, BONE, SMOKE, FUR, PLATE, VOID, VEIN, GLOW, CRYSTAL, MOSS, BRICK;

    static int mix(int a, int b, float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return FastColor.ARGB32.color(
            Mth.lerpInt(t, FastColor.ARGB32.alpha(a), FastColor.ARGB32.alpha(b)),
            Mth.lerpInt(t, FastColor.ARGB32.red(a), FastColor.ARGB32.red(b)),
            Mth.lerpInt(t, FastColor.ARGB32.green(a), FastColor.ARGB32.green(b)),
            Mth.lerpInt(t, FastColor.ARGB32.blue(a), FastColor.ARGB32.blue(b))
        );
    }

    static int shade(int argb, float f) {
        return FastColor.ARGB32.color(
            FastColor.ARGB32.alpha(argb),
            Mth.clamp((int)(FastColor.ARGB32.red(argb) * f), 0, 255),
            Mth.clamp((int)(FastColor.ARGB32.green(argb) * f), 0, 255),
            Mth.clamp((int)(FastColor.ARGB32.blue(argb) * f), 0, 255)
        );
    }

    static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    private static int withAlpha(int argb, int alpha) {
        return alpha << 24 | argb & 0xFFFFFF;
    }

    /** A cheap repeatable noise in [0, 1). */
    static float noise(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 1442695041;
        h = (h ^ h >>> 13) * 1274126177;
        return ((h ^ h >>> 16) & 0xFFFF) / 65536.0F;
    }

    /** Rune strokes: short bars and ticks in a grid, so each face carries its own few glyphs. */
    private static boolean rune(int x, int y, int seed) {
        float pick = noise(x / 4, y / 5, seed + 31);
        int lx = x % 4;
        int ly = y % 5;
        if (pick < 0.35F) {
            return false;
        }

        return pick < 0.6F ? lx == 1 && ly < 4 : (pick < 0.8F ? ly == 1 && lx < 3 : lx == ly % 3 && ly < 4);
    }

    /** Glowing seams, as of fire showing through cracks. */
    private static boolean vein(int x, int y, int seed) {
        float wave = Mth.sin(x * 0.7F + noise(0, y / 3, seed) * 6.0F) * 2.0F;
        return Math.abs(y % 7 - 3 - wave) < 0.6F;
    }

    /** The lit part of a pixel, for the full-bright pass; transparent where nothing glows. */
    int glowColour(Look look, int x, int y, int w, int h, int seed) {
        return switch (this) {
            case EYE, GLOW, FLAME -> {
                int c = this.colour(look, x, y, w, h, seed);
                yield FastColor.ARGB32.alpha(c) == 0 ? 0 : c;
            }
            case RUNE -> rune(x, y, seed) ? opaque(look.glow()) : 0;
            case VEIN -> vein(x, y, seed) ? opaque(look.glow()) : 0;
            // Crystal glows faintly all through, brighter along its facets.
            case CRYSTAL -> shade(opaque(look.glow()), (x + y) % 5 == 0 ? 0.75F : 0.35F);
            default -> 0;
        };
    }

    /**
     * The colour at (x, y) on a face w by h pixels, shaded from its top (t = 0) to its bottom (t = 1). A transparent
     * colour leaves the pixel out.
     */
    int colour(Look look, int x, int y, int w, int h, int seed) {
        float t = h <= 1 ? 0.0F : (float)y / (h - 1);
        float n = noise(x, y, seed);
        int base = opaque(look.base());
        int dark = opaque(look.dark());
        int light = opaque(look.light());
        return switch (this) {
            case SCALE -> {
                int cx = (x + y / 2 % 2) % 3;
                int cy = y % 2;
                int c = cx == 2 && cy == 1 ? dark : (cx == 0 && cy == 0 ? light : base);
                yield shade(mix(c, dark, t * 0.35F), 0.94F + n * 0.12F);
            }
            case BELLY -> {
                int c = opaque(look.belly());
                yield shade(y % 3 == 2 ? mix(c, dark, 0.3F) : c, 0.95F + n * 0.1F);
            }
            case MEMBRANE -> {
                // Lit near the bone it hangs from, darkening toward a scalloped trailing edge, with pale ribs fanning
                // out across it.
                int c = mix(mix(opaque(look.accent()), base, 0.5F), dark, 0.2F + t * 0.5F);
                if ((x + (int)(t * 7.0F)) % 14 == 0) {
                    c = mix(c, opaque(look.horn()), 0.35F);
                } else if (n > 0.93F) {
                    c = mix(c, dark, 0.4F);
                }

                float edge = 1.0F - 0.14F * Math.abs(Mth.sin(x * Mth.PI / 16.0F));
                yield t > edge ? 0 : withAlpha(c, 215);
            }
            case HORN -> shade(mix(opaque(look.horn()), light, 0.3F - t * 0.3F), 0.92F + n * 0.12F);
            case CLAW -> shade(mix(opaque(look.horn()), dark, 0.55F), 0.95F + n * 0.1F);
            case EYE, GLOW -> mix(opaque(look.glow()), 0xFFFFFFFF, this == EYE && x == 0 && y == 0 ? 0.6F : 0.15F);
            case FEATHER -> {
                int c = mix(base, light, t * 0.6F);
                yield shade((x + y) % 4 == 0 ? mix(c, dark, 0.35F) : c, 0.95F + n * 0.1F);
            }
            case FLAME -> withAlpha(mix(opaque(look.glow()), opaque(look.accent()), t + (n - 0.5F) * 0.3F), n < 0.12F && t > 0.6F ? 0 : 240);
            case STONE -> {
                int c = noise(x / 3, y / 2, seed + 7) > 0.88F ? dark : shade(base, 0.85F + n * 0.25F);
                yield mix(c, dark, t * 0.25F);
            }
            case RUNE -> {
                int c = shade(base, 0.85F + n * 0.2F);
                yield rune(x, y, seed) ? mix(c, opaque(look.glow()), 0.8F) : c;
            }
            case CLOTH -> {
                int c = mix(base, dark, t * 0.5F);
                if (x % 4 == 0) {
                    c = mix(c, dark, 0.4F);
                }

                yield t > 0.8F && n < 0.35F ? 0 : withAlpha(shade(c, 0.95F + n * 0.08F), 245);
            }
            case BONE -> shade(opaque(look.horn()), 0.9F + n * 0.15F);
            case SMOKE -> withAlpha(mix(base, dark, n), 120 + (int)(n * 110));
            case FUR -> {
                int k = (x * 7 + y * 3) % 9;
                int c = k == 0 ? light : (k == 4 ? dark : base);
                yield shade(mix(c, dark, t * 0.3F), 0.93F + n * 0.12F);
            }
            case PLATE -> {
                int c = opaque(look.accent());
                boolean rim = y == 0 || x == 0 || x == w - 1;
                boolean rivet = (x == 1 || x == w - 2) && y % 4 == 2;
                yield shade(rim ? mix(c, 0xFFFFFFFF, 0.25F) : (rivet ? mix(c, dark, 0.6F) : c), 0.9F + n * 0.12F);
            }
            case VOID -> 0xFF06060A;
            case VEIN -> vein(x, y, seed) ? opaque(look.glow()) : shade(dark, 0.9F + n * 0.2F);
            case CRYSTAL -> {
                int c = mix(opaque(look.accent()), opaque(look.glow()), t * 0.5F + n * 0.25F);
                yield withAlpha((x + y) % 5 == 0 ? mix(c, 0xFFFFFFFF, 0.45F) : c, 215);
            }
            case MOSS -> {
                // Patchy: bare where the noise is low, so the stone under it shows through.
                float patch = noise(x / 3, y / 3, seed + 13);
                yield patch < 0.38F ? 0 : shade(mix(opaque(look.belly()), dark, n * 0.35F), 0.9F + patch * 0.2F);
            }
            case BRICK -> {
                boolean mortar = y % 5 == 4 || (x + (y / 5 % 2) * 4) % 8 == 7;
                yield mortar ? shade(dark, 0.9F + n * 0.1F) : shade(mix(light, base, 0.4F + n * 0.3F), 0.92F + n * 0.12F);
            }
        };
    }
}
