package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;

/** The creatures of the Forgotten Shore: Awakened at the least, and up to Great. */
public final class ShoreKinds {
    /**
     * Two and a half metres of demonic crab and centaur: four pairs of segmented legs ending in scythe tips, an armoured
     * human-like torso with the head set straight on it, slit eyes, slimy mandibles, and two huge pincers. They hunt the
     * labyrinth in packs.
     */
    public static final Kind CARAPACE_SCAVENGER = Kind.of("carapace_scavenger", CreatureRank.AWAKENED, CreatureClass.BEAST)
        .size(1.6F, 2.5F).stats(34.0, 6.0, 8.0, 0.27)
        .move(Move.melee("snap", Style.BITE, 14, 8, 1.6, 1.0F, 10))
        .move(Move.of("stab", Style.STRIKE, 20, 0.0, 2.4, 30, 6, 1.3F, 2.0, 12))
        .traits(Kind.Trait.PACK)
        .haunt(Region.FORGOTTEN_SHORE, 30, 2, 4)
        .lore().looks(2).palette(Vfx.Palette.BLOOD).egg(0x6A2A22, 0xD8C8B0)
        .sounds(() -> SoundEvents.SPIDER_AMBIENT, () -> SoundEvents.SPIDER_HURT, () -> SoundEvents.SPIDER_DEATH, () -> SoundEvents.SPIDER_STEP)
        .build();

    /**
     * Larger than the scavengers, spiked, its chitin black and crimson like ancient armour splashed with fresh blood,
     * and bone scythes jutting from its arm joints instead of pincers.
     */
    public static final Kind CARAPACE_CENTURION = Kind.of("carapace_centurion", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .size(1.9F, 3.0F).stats(70.0, 9.0, 11.0, 0.26)
        .move(Move.of("scythes", Style.STRIKE, 18, 0.0, 2.6, 20, 10, 1.2F, 2.4, 9, 13))
        .move(Move.of("thrust", Style.BITE, 22, 1.0, 3.2, 40, 6, 1.6F, 2.8, 14))
        .move(Move.of("rally", Style.CALL, 20, 0.0, 16.0, 400, 2, 0.0F, 0.0, 10))
        .traits(Kind.Trait.PACK).commands("carapace_scavenger")
        .haunt(Region.FORGOTTEN_SHORE, 12, 1, 2)
        .lore().palette(Vfx.Palette.BLOOD).egg(0x1A1214, 0xB0202A)
        .sounds(() -> SoundEvents.SPIDER_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.SPIDER_STEP)
        .build();

    /**
     * As large as a house, on eight legs like pillars, its carapace polished metal set with jagged spikes, so that it
     * looks like a giant knight. Its humanoid torso has four arms, two with pincers and two with scythes, and its
     * horned face is nearly human. It commands the centurions, and keeps the Ashen Barrow.
     */
    public static final Kind CARAPACE_DEMON = Kind.of("carapace_demon", CreatureRank.AWAKENED, CreatureClass.DEMON)
        .size(4.4F, 6.0F).stats(240.0, 16.0, 16.0, 0.24).follow(40.0)
        .move(Move.of("crush", Style.GRAB, 24, 0.0, 3.6, 30, 8, 1.2F, 3.2, 14))
        .move(Move.of("reap", Style.SWEEP, 28, 0.0, 4.2, 40, 8, 1.4F, 4.0, 14, 19))
        .move(Move.of("stamp", Style.SLAM, 30, 0.0, 3.6, 80, 5, 1.6F, 3.5, 18))
        .move(Move.of("command", Style.CALL, 24, 0.0, 30.0, 300, 3, 0.0F, 0.0, 12))
        .commands("carapace_centurion", "carapace_scavenger").traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.ASHEN_BARROW, 0, 70)
        .lore().palette(Vfx.Palette.STEEL).egg(0x8A8E98, 0xC8302A)
        .sounds(() -> SoundEvents.RAVAGER_AMBIENT, () -> SoundEvents.IRON_GOLEM_HURT, () -> SoundEvents.IRON_GOLEM_DEATH, () -> SoundEvents.IRON_GOLEM_STEP)
        .build();

    public static final List<Kind> ALL = List.of(CARAPACE_SCAVENGER, CARAPACE_CENTURION, CARAPACE_DEMON);

    private ShoreKinds() {
    }
}
