package net.logosnumen.nightmareevolved.dream;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

/**
 * The named places of the Dream Realm and where they stand. They are the same in every world, so a Gateway, a far
 * landmark seen in the sky or a Sleeper's arrival can be found without generating anything.
 */
public final class Places {
    /** The northern edge of the Forgotten Shore, where the barrows rise and the Burned Forest begins. */
    public static final int SHORE_NORTH = -4 * RealmMap.CELL;

    // The Forgotten Shore.
    public static final Place CRIMSON_SPIRE = new Place("crimson_spire", -1024, -6144);
    public static final Place DARK_CITY = new Place("dark_city", 1420, -6020);
    public static final Place ASHEN_BARROW = new Place("ashen_barrow", -3330, -6150);
    public static final Place BONE_RIDGE = new Place("bone_ridge", -260, -6960);
    /** Where the iron spiders nest in the labyrinth (the novel gives no place; this one is the mod's). */
    public static final Place IRON_NEST = new Place("iron_nest", -200, -5600);
    /** A nest of stone on a coral hill where the Vile Thieving Bird's spawn waits, unhatched (the mod's own place). */
    public static final Place STONE_NEST = new Place("stone_nest", -1900, -6700);
    // The Burned Forest.
    /** A Millipede Queen's hive deep in the deadfall (the novel's queens have hives; this one is the mod's). */
    public static final Place MILLIPEDE_HIVE = new Place("millipede_hive", -600, -11400);
    // The Hollow Mountains.
    public static final Place UNDERGROUND_MINE = new Place("underground_mine", 1350, -4200);
    public static final Place UNDERWORLD_GATE = new Place("underworld_gate", -520, -3060);
    public static final Place NETHERS_CITY = new Place("nethers_city", -520, -2960);
    public static final Place BLACK_MOUNTAIN = new Place("black_mountain", -3000, -3000);
    // The Nightmare Desert.
    public static final Place TOMB_OF_ARIEL = new Place("tomb_of_ariel", 5600, -6200);
    public static final Place BLACK_SKULL = new Place("black_skull", 3300, -5000);
    // Godgrave: the skull rests on the mountains' slopes, the body runs south.
    public static final Place GODGRAVE_SKULL = new Place("godgrave_skull", -1024, -2300);
    public static final Place VANISHING_LAKE = new Place("vanishing_lake", -1024, -150);
    public static final Place MICTLAN_ZIGGURAT = new Place("mictlan_ziggurat", -1024, -760);
    /** Where the spine's flooded canal runs beyond the ribs (the novel names the ocean, not this spot). */
    public static final Place SPINAL_OCEAN = new Place("spinal_ocean", -1024, 900);
    // The Glass Hell.
    public static final Place RED_HILL = new Place("red_hill", 1000, -260);
    // The Chained Isles.
    public static final Place TEAR = new Place("tear", 4096, -1024);
    public static final Place SANCTUARY_OF_NOCTIS = new Place("sanctuary_of_noctis", 3320, -330);
    public static final Place NORTHERN_ISLAND = new Place("northern_island", 4300, -1820);
    public static final Place IVORY_TOWER = new Place("ivory_tower", 4096, -1024);
    public static final Place EBONY_TOWER = new Place("ebony_tower", 4560, -560);
    public static final Place IRON_HAND = new Place("iron_hand", 2720, -900);
    public static final Place RECKONING = new Place("reckoning_island", 5240, -700);
    public static final Place SHIPWRECK = new Place("shipwreck_island", 3700, -1520);
    public static final Place TWISTED_TREES = new Place("forest_of_twisted_trees", 5420, -1480);
    public static final Place JAGGED_ROCKS = new Place("island_of_jagged_rocks", 2600, -1620);
    public static final Place DESECRATED_GROVE = new Place("desecrated_grove", 2980, -240);
    public static final Place CORRUPTED_ISLAND = new Place("corrupted_monster_island", 5620, -300);
    // The Sword Domain.
    public static final Place BASTION = new Place("bastion", 4096, 2048);
    // The Song Domain.
    public static final Place RAVENHEART = new Place("ravenheart", -3072, 760);
    public static final Place RAVENHEART_VOLCANO = new Place("ravenheart_volcano", -3072, 1420);
    public static final Place WEEPING_GODDESS = new Place("weeping_goddess", -3072, 2700);
    public static final Place RIVERGATE = new Place("rivergate", -3072, 3760);
    // The Stormsea.
    public static final Place NIGHT_GARDEN = new Place("night_garden", 2400, 5200);
    public static final Place BLACK_OBELISK = new Place("black_obelisk", -800, 5700);

    public static final List<Place> ALL = List.of(
        CRIMSON_SPIRE, DARK_CITY, ASHEN_BARROW, BONE_RIDGE, IRON_NEST, STONE_NEST, MILLIPEDE_HIVE, UNDERGROUND_MINE, UNDERWORLD_GATE, NETHERS_CITY,
        BLACK_MOUNTAIN, TOMB_OF_ARIEL, BLACK_SKULL, GODGRAVE_SKULL, VANISHING_LAKE, MICTLAN_ZIGGURAT, SPINAL_OCEAN, RED_HILL, TEAR, SANCTUARY_OF_NOCTIS,
        NORTHERN_ISLAND, IVORY_TOWER, EBONY_TOWER, IRON_HAND, RECKONING, SHIPWRECK, TWISTED_TREES, JAGGED_ROCKS, DESECRATED_GROVE,
        CORRUPTED_ISLAND, BASTION, RAVENHEART, RAVENHEART_VOLCANO, WEEPING_GODDESS, RIVERGATE, NIGHT_GARDEN, BLACK_OBELISK
    );

    private Places() {
    }

    /** A named place, by its middle. */
    public record Place(String id, int x, int z) {
        public Component title() {
            return Component.translatable("place.nightmare_evolved." + this.id);
        }

        public double distance(double px, double pz) {
            double dx = px - this.x;
            double dz = pz - this.z;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }

    @Nullable
    public static Place byId(String id) {
        for (Place place : ALL) {
            if (place.id().equals(id)) {
                return place;
            }
        }

        return null;
    }
}
