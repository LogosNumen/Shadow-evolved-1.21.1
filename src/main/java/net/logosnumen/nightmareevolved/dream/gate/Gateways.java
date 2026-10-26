package net.logosnumen.nightmareevolved.dream.gate;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.Godgrave;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CitadelPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CrimsonSpire;
import net.logosnumen.nightmareevolved.dream.gen.landmark.DesertPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.IslePlaces;
import net.logosnumen.nightmareevolved.dream.gen.shape.WeepingShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * The Gateways of the Dream Realm, the only ways out of it: each usually stands in or by its region's most prominent
 * landmark. Most have a Citadel built around them; the Crimson Spire's does not. More appear where Seeds of Nightmare
 * are destroyed (see {@code Seeds}).
 */
public final class Gateways {
    /** A Gateway: where it is, which region it lies in, how it is used, and the Citadel around it, if any. */
    public record Gateway(String id, Region region, BlockPos pos, GatewayBlock.Form form, @Nullable String citadel, boolean great) {
        /** The Gateway's name: its own for the novel's Gateways and canon Seeds', otherwise the region a Seed was conquered in. */
        public Component title() {
            if (this.id.startsWith("seed_")) {
                return Component.translatable("gateway.nightmare_evolved.born", this.region.title());
            }

            return Component.translatable("gateway.nightmare_evolved." + this.id);
        }
    }

    public static final List<Gateway> CANON = List.of(
        new Gateway("crimson_spire", Region.FORGOTTEN_SHORE, new BlockPos(CrimsonSpire.gatewayX(), CrimsonSpire.BALCONY, CrimsonSpire.gatewayZ()),
            GatewayBlock.Form.RING, null, false),
        new Gateway("sanctuary_of_noctis", Region.CHAINED_ISLES, new BlockPos(IslePlaces.Sanctuary.altarX(), IslePlaces.Sanctuary.TOP + 1,
            IslePlaces.Sanctuary.altarZ()), GatewayBlock.Form.WHITE_ALTAR, "sanctuary_of_noctis", false),
        new Gateway("night_temple", Region.CHAINED_ISLES, new BlockPos(IslePlaces.NorthernIsland.altarX(), IslePlaces.NorthernIsland.HALL + 1,
            IslePlaces.NorthernIsland.altarZ()), GatewayBlock.Form.BLACK_ALTAR, "night_temple", false),
        new Gateway("black_skull", Region.NIGHTMARE_DESERT, new BlockPos(DesertPlaces.BlackSkull.gatewayX(), DesertPlaces.BlackSkull.GROUND + 1,
            DesertPlaces.BlackSkull.gatewayZ()), GatewayBlock.Form.HEART, "black_skull", false),
        new Gateway("vanishing_lake", Region.GODGRAVE, new BlockPos(Places.VANISHING_LAKE.x(), Godgrave.VAULT_FLOOR - 1, Places.VANISHING_LAKE.z()),
            GatewayBlock.Form.POOL, "vanishing_lake", false),
        new Gateway("red_hill", Region.GLASS_HELL, new BlockPos(Places.RED_HILL.x(), CitadelPlaces.RedHill.TOP + 1, Places.RED_HILL.z()),
            GatewayBlock.Form.HEART, "red_hill", false),
        new Gateway("ravenheart", Region.RAVENHEART, new BlockPos(CitadelPlaces.Ravenheart.gatewayX(), CitadelPlaces.Ravenheart.FLOOR + 3,
            CitadelPlaces.Ravenheart.gatewayZ() - 2), GatewayBlock.Form.HEART, "ravenheart", true),
        new Gateway("bastion", Region.BASTION, new BlockPos(Places.BASTION.x(), CitadelPlaces.Bastion.GROUND + 1, Places.BASTION.z()),
            GatewayBlock.Form.HEART, "bastion", true),
        new Gateway("sorrow", Region.WEEPING_GODDESS, new BlockPos(CitadelPlaces.Sorrow.gatewayX(), WeepingShaper.PLATEAU + 1,
            CitadelPlaces.Sorrow.gatewayZ()), GatewayBlock.Form.HEART, "sorrow", false),
        new Gateway("rivergate", Region.WEEPING_GODDESS, new BlockPos(CitadelPlaces.Rivergate.gatewayX(), WeepingShaper.LAKE + 1,
            CitadelPlaces.Rivergate.gatewayZ()), GatewayBlock.Form.HEART, "rivergate", false),
        new Gateway("night_garden", Region.STORMSEA, new BlockPos(Places.NIGHT_GARDEN.x(), CitadelPlaces.NightGarden.DECK + 1, Places.NIGHT_GARDEN.z()),
            GatewayBlock.Form.HEART, "night_garden", true),
        new Gateway("black_obelisk", Region.STORMSEA, new BlockPos(Places.BLACK_OBELISK.x(), CitadelPlaces.BlackObelisk.BASE + 1, Places.BLACK_OBELISK.z()),
            GatewayBlock.Form.HEART, "black_obelisk", false)
    );

    private Gateways() {
    }

    @Nullable
    public static Gateway byId(ServerLevel realm, String id) {
        for (Gateway gateway : all(realm)) {
            if (gateway.id.equals(id)) {
                return gateway;
            }
        }

        return null;
    }

    /** Every Gateway that stands now: the canon ones, and those left where Seeds were destroyed. */
    public static List<Gateway> all(ServerLevel realm) {
        return all(realm.getServer());
    }

    public static List<Gateway> all(MinecraftServer server) {
        GatewayRecords records = GatewayRecords.get(server);
        List<Gateway> all = new ArrayList<>(CANON);
        all.addAll(records.born());
        all.removeIf(gateway -> records.destroyed(gateway.id));
        return all;
    }

    /** The Gateway whose heart is at a place, or near it. */
    @Nullable
    public static Gateway at(ServerLevel realm, BlockPos pos) {
        Gateway best = null;
        double nearest = 6.0 * 6.0;
        for (Gateway gateway : all(realm)) {
            double d = gateway.pos.distSqr(pos);
            if (d < nearest) {
                nearest = d;
                best = gateway;
            }
        }

        return best;
    }

    /** The Gateways a Sleeper's random arrival in a region is measured from. */
    public static List<Gateway> forArrival(ServerLevel realm, Region region) {
        return forArrival(all(realm), region);
    }

    /** The same, from a given list of Gateways. The Moonriver Plain has none of its own: Ravenheart lies to its south. */
    public static List<Gateway> forArrival(List<Gateway> gateways, Region region) {
        Region by = region == Region.MOONRIVER_PLAIN ? Region.RAVENHEART : region;
        return gateways.stream().filter(gateway -> gateway.region == by).toList();
    }

    /** A place to stand beside a Gateway: solid ground under two blocks of air, nearest its heart. */
    public static BlockPos arrival(ServerLevel realm, Gateway gateway) {
        BlockPos heart = gateway.pos;
        realm.getChunk(heart);
        for (int r = 1; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }

                    for (int dy = -1; dy <= 4; dy++) {
                        BlockPos feet = heart.offset(dx, dy, dz);
                        if (standable(realm, feet)) {
                            return feet;
                        }
                    }
                }
            }
        }

        return heart.above(2);
    }

    public static boolean standable(ServerLevel realm, BlockPos feet) {
        return realm.getBlockState(feet).getCollisionShape(realm, feet).isEmpty() && realm.getFluidState(feet).isEmpty()
            && realm.getBlockState(feet.above()).getCollisionShape(realm, feet.above()).isEmpty()
            && realm.getBlockState(feet.below()).isFaceSturdy(realm, feet.below(), Direction.UP);
    }
}
