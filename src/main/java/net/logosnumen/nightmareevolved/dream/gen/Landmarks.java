package net.logosnumen.nightmareevolved.dream.gen;

import java.util.List;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CitadelPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CrimsonSpire;
import net.logosnumen.nightmareevolved.dream.gen.landmark.DesertPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.GodgravePlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.IslePlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.MountainPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.ShorePlaces;

/** Every landmark built into the Dream Realm. */
public final class Landmarks {
    private Landmarks() {
    }

    static void addAll(RealmTerrain terrain, List<Landmark> into) {
        into.add(new CrimsonSpire());
        into.add(new ShorePlaces.AshenBarrow());
        into.add(new ShorePlaces.BoneRidge());
        into.add(new ShorePlaces.DarkCity());
        into.add(new ShorePlaces.UndergroundMine());
        into.add(new IslePlaces.Sanctuary());
        into.add(new IslePlaces.NorthernIsland());
        into.add(new IslePlaces.IvoryTower());
        into.add(new IslePlaces.EbonyTower());
        into.add(new IslePlaces.Lesser(Places.IRON_HAND));
        into.add(new IslePlaces.Lesser(Places.RECKONING));
        into.add(new IslePlaces.Lesser(Places.SHIPWRECK));
        into.add(new IslePlaces.Lesser(Places.DESECRATED_GROVE));
        into.add(new DesertPlaces.TombOfAriel());
        into.add(new DesertPlaces.BlackSkull());
        into.add(new DesertPlaces.Gauntlet());
        into.add(new DesertPlaces.MiragePath());
        into.add(new GodgravePlaces.Ruins());
        into.add(new GodgravePlaces.VanishingLake());
        into.add(new GodgravePlaces.Ziggurat());
        into.add(new MountainPlaces.UnderworldGate());
        into.add(new MountainPlaces.NethersCity());
        into.add(new MountainPlaces.BlackMountain());
        into.add(new CitadelPlaces.Bastion());
        into.add(new CitadelPlaces.Ravenheart());
        into.add(new CitadelPlaces.Sorrow());
        into.add(new CitadelPlaces.Rivergate());
        into.add(new CitadelPlaces.RedHill());
        into.add(new CitadelPlaces.NightGarden());
        into.add(new CitadelPlaces.BlackObelisk());
    }
}
