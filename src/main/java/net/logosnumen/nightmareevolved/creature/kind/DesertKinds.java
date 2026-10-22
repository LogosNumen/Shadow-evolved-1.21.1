package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.entity.creature.Deathless;
import net.logosnumen.nightmareevolved.entity.creature.Skinwalker;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;

/** The creatures of the Nightmare Desert: the Deathless that rise from the dunes each night, and the Skinwalker's vessels. */
public final class DesertKinds {
    /**
     * The Deathless: black skeletons in rusted armour, most once human, rising from beneath the dunes each sunset to
     * fight each other forever. They cannot be destroyed, and have no souls. Even the weakest are a match for a powerful
     * Corrupted creature.
     */
    public static final Kind DEATHLESS = Kind.of("deathless", CreatureRank.CORRUPTED, CreatureClass.MONSTER)
        .size(0.8F, 2.0F).stats(220.0, 20.0, 12.0, 0.28)
        .move(Move.of("cut", Style.STRIKE, 16, 0.0, 2.6, 12, 10, 1.1F, 2.4, 9))
        .move(Move.of("thrust", Style.BITE, 18, 1.0, 3.2, 25, 6, 1.4F, 3.0, 11))
        .move(Move.of("volley", Style.SHOT, 20, 5.0, 18.0, 80, 3, 0.8F, 0.0, 12))
        .traits(Kind.Trait.SOULLESS, Kind.Trait.LIGHT_FALL)
        .haunt(Region.NIGHTMARE_DESERT, 40, 3, 6, Kind.Time.NIGHT, Kind.Ground.SURFACE)
        .factory(Deathless::new).looks(3).palette(Vfx.Palette.SHADOW).egg(0x1A1A1E, 0x8A6A4A)
        .sounds(() -> SoundEvents.SKELETON_AMBIENT, () -> SoundEvents.SKELETON_HURT, () -> SoundEvents.SKELETON_DEATH, () -> SoundEvents.SKELETON_STEP)
        .build();

    /** The Deathless giants: skeletons of things far larger than people, standing like monuments, closer to the Tomb. */
    public static final Kind DEATHLESS_GIANT = Kind.of("deathless_giant", CreatureRank.GREAT, CreatureClass.DEMON)
        .size(2.2F, 6.0F).stats(900.0, 38.0, 18.0, 0.24).follow(48.0)
        .move(Move.of("smash", Style.SLAM, 30, 0.0, 4.4, 40, 8, 1.4F, 4.2, 20))
        .move(Move.of("cleave", Style.SWEEP, 26, 0.0, 4.8, 30, 8, 1.2F, 4.6, 15))
        .traits(Kind.Trait.SOULLESS, Kind.Trait.LIGHT_FALL)
        .hauntNear(Region.NIGHTMARE_DESERT, Places.TOMB_OF_ARIEL, 1200, 8, 1, 1, Kind.Time.NIGHT)
        .factory(Deathless::new).palette(Vfx.Palette.SHADOW).egg(0x101012, 0xB08A5A)
        .sounds(() -> SoundEvents.SKELETON_AMBIENT, () -> SoundEvents.SKELETON_HURT, () -> SoundEvents.SKELETON_DEATH, () -> SoundEvents.BONE_BLOCK_STEP)
        .build();

    /**
     * A Skinwalker's vessel: the body of an Awakened whose soul core has gone dark, worn by a great abomination that knows
     * all the body knew. It passes for a traveller until it finds someone alone.
     */
    public static final Kind SKINWALKER = Kind.of("skinwalker", CreatureRank.CORRUPTED, CreatureClass.MONSTER)
        .size(0.6F, 1.8F).stats(200.0, 18.0, 8.0, 0.3)
        .move(Move.of("cut", Style.STRIKE, 14, 0.0, 2.4, 10, 10, 1.1F, 2.2, 8))
        .move(Move.melee("grasp", Style.GRAB, 20, 12, 2.0, 1.2F, 30))
        .move(Move.of("leap", Style.POUNCE, 20, 3.0, 9.0, 40, 6, 1.3F, 1.6, 14))
        .haunt(Region.NIGHTMARE_DESERT, 3, 1, 1, Kind.Time.DAY, Kind.Ground.SURFACE)
        .factory(Skinwalker::new).palette(Vfx.Palette.SHADOW).egg(0xC89A7A, 0x1A0A0A)
        .sounds(() -> SoundEvents.PLAYER_BREATH, () -> SoundEvents.PLAYER_HURT, () -> SoundEvents.PLAYER_DEATH, () -> SoundEvents.SAND_STEP)
        .build();

    public static final List<Kind> ALL = List.of(DEATHLESS, DEATHLESS_GIANT, SKINWALKER);

    private DesertKinds() {
    }
}
