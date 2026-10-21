package net.logosnumen.nightmareevolved.dream;

import java.util.Locale;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * The regions of the Dream Realm, each with a sky and laws of its own. Some are places inside a larger region (the Sea
 * of Ash and the Land of Barrows are parts of the Forgotten Shore, the Underworld lies under the Hollow Mountains, and
 * the hollows run inside Godgrave's bones), and have a biome of their own so their look and rules can differ.
 * <p>
 * {@link #arrival} is how often a Sleeper's first sleep drops them there: never into a Death Zone. {@link #seeds} are the
 * Categories of the Seeds of Nightmare that grow there, lowest to highest; {@link #creatures} the ranks of what hunts
 * there (Dormant 0 to Unholy 6).
 */
public enum Region {
    FORGOTTEN_SHORE(Sky.SHORE, false, 25, 2, 3, 1, 4),
    SEA_OF_ASH(Sky.SHORE, false, 0, 2, 2, 1, 2),
    LAND_OF_BARROWS(Sky.BARROWS, false, 0, 2, 3, 1, 3),
    BURNED_FOREST(Sky.ASHEN, true, 0, 4, 6, 4, 6),
    NIGHTMARE_DESERT(Sky.DESERT, false, 10, 2, 4, 2, 4),
    HOLLOW_MOUNTAINS(Sky.MIST, true, 0, 4, 6, 4, 6),
    UNDERWORLD(Sky.MIST, true, 0, 5, 6, 4, 6),
    MOONRIVER_PLAIN(Sky.MOONRIVER, false, 10, 2, 3, 1, 3),
    GODGRAVE(Sky.CLOUDVEIL, true, 0, 4, 5, 4, 5),
    GODGRAVE_HOLLOW(Sky.CLOUDVEIL, true, 0, 2, 4, 3, 5),
    GLASS_HELL(Sky.GLASS, false, 8, 2, 3, 1, 3),
    CHAINED_ISLES(Sky.ISLES, false, 20, 2, 3, 0, 3),
    RAVENHEART(Sky.SNOW_ASH, false, 10, 2, 3, 1, 3),
    BONE_SEA(Sky.BONE, false, 0, 2, 3, 2, 4),
    BASTION(Sky.PLAIN, false, 10, 2, 3, 1, 3),
    WEEPING_GODDESS(Sky.PLAIN, false, 7, 2, 3, 1, 3),
    STORMSEA(Sky.STORM, false, 0, 2, 4, 2, 5);

    /** How a region's sky looks; the client draws each its own way. */
    public enum Sky {
        /** Grey by day with no sun but the Crimson Spire's; a black, starless void by night. */
        SHORE,
        /** The edge of the Shore's dead sky, where a sun is seen again. */
        BARROWS,
        /** An ashen sky over endless burned ground. */
        ASHEN,
        /** A deep azure sky with a blinding sun, never a cloud. */
        DESERT,
        /** White mist of nothingness. */
        MIST,
        /** A distant sun by day and three moons by night. */
        MOONRIVER,
        /** A grey veil of storm clouds that never lifts on its own, and no night. */
        CLOUDVEIL,
        /** A glaring sky over a plain of glass. */
        GLASS,
        /** The Sky Above, with its sun, moon and stars, and the Sky Below, with stars of its own. */
        ISLES,
        /** Grey, with snow and ash falling together. */
        SNOW_ASH,
        /** A sickly pale sky over poisoned water. */
        BONE,
        /** An ordinary sky. */
        PLAIN,
        /** Twilight and storms, with pale stars. */
        STORM
    }

    public final Sky sky;
    /** A Death Zone: the creatures here are Great, Cursed or Unholy, and few come back. */
    public final boolean deathZone;
    public final int arrival;
    public final int seedMin;
    public final int seedMax;
    public final int creatureMin;
    public final int creatureMax;

    Region(Sky sky, boolean deathZone, int arrival, int seedMin, int seedMax, int creatureMin, int creatureMax) {
        this.sky = sky;
        this.deathZone = deathZone;
        this.arrival = arrival;
        this.seedMin = seedMin;
        this.seedMax = seedMax;
        this.creatureMin = creatureMin;
        this.creatureMax = creatureMax;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("region.nightmare_evolved." + this.id());
    }

    public ResourceKey<Biome> biome() {
        return ResourceKey.create(Registries.BIOME, NightmareEvolved.id(this.id()));
    }

    /** The region a part belongs to: the Forgotten Shore for the Sea of Ash, Godgrave for its hollows, and so on. */
    public Region whole() {
        return switch (this) {
            case SEA_OF_ASH, LAND_OF_BARROWS -> FORGOTTEN_SHORE;
            case UNDERWORLD -> HOLLOW_MOUNTAINS;
            case GODGRAVE_HOLLOW -> GODGRAVE;
            default -> this;
        };
    }

    @Nullable
    public static Region byBiome(ResourceLocation biome) {
        if (!biome.getNamespace().equals(NightmareEvolved.MOD_ID)) {
            return null;
        }

        for (Region region : values()) {
            if (region.id().equals(biome.getPath())) {
                return region;
            }
        }

        return null;
    }

    @Nullable
    public static Region byId(String id) {
        for (Region region : values()) {
            if (region.id().equals(id)) {
                return region;
            }
        }

        return null;
    }
}
