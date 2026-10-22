package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.entity.creature.MistBeing;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The creatures of the Hollow Mountains and the Underworld beneath them. In the mist, voices; below, in Nether's City,
 * the Saints of Tainted Jade and Absence, shells of stone full of darkness, the Jade Titan on its throne, and in the
 * black river the Horror of the Lightless Current.
 */
public final class MountainKinds {
    /** Something in the mist that speaks with a human voice, coaxing whoever hears it to open their eyes. */
    public static final Kind MIST_BEING = Kind.of("mist_being", CreatureRank.FALLEN, CreatureClass.DEMON)
        .motion(Kind.Motion.HOVER).size(0.9F, 2.6F).stats(140.0, 12.0, 0.0, 0.26)
        .move(Move.of("unmake", Style.BREATH, 30, 0.0, 6.0, 60, 8, 1.0F, 6.0, 20))
        .move(Move.melee("touch", Style.BITE, 16, 10, 1.6, 1.2F, 15))
        .afflicts(() -> new MobEffectInstance(MobEffects.BLINDNESS, 60, 0))
        .haunt(Region.HOLLOW_MOUNTAINS, 10, 1, 1, Kind.Time.ANY, Kind.Ground.SURFACE)
        .factory(MistBeing::new).palette(Vfx.Palette.FROST).egg(0xC8CCD0, 0x8A9098)
        .sounds(() -> SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, () -> SoundEvents.VEX_HURT, () -> SoundEvents.VEX_DEATH, () -> SoundEvents.ALLAY_ITEM_TAKEN)
        .build();

    /** The Saints of Tainted Jade: Stone Saints corrupted into jade, fighting as one disciplined army in Nether's City. */
    public static final Kind TAINTED_SAINT = Kind.of("tainted_saint", CreatureRank.GREAT, CreatureClass.DEMON)
        .size(0.9F, 2.4F).stats(420.0, 26.0, 20.0, 0.29)
        .move(Move.of("cut", Style.STRIKE, 16, 0.0, 2.8, 12, 10, 1.1F, 2.6, 9))
        .move(Move.of("thrust", Style.BITE, 18, 1.0, 3.4, 25, 6, 1.4F, 3.0, 11))
        .move(Move.melee("bash", Style.BITE, 14, 8, 1.6, 0.8F, 30))
        .traits(Kind.Trait.PACK)
        .hauntNear(Region.UNDERWORLD, Places.NETHERS_CITY, 170, 20, 1, 2, Kind.Time.ANY)
        .palette(Vfx.Palette.ACID).egg(0x3A8A5A, 0x0A2A14)
        .sounds(() -> SoundEvents.STONE_PLACE, () -> SoundEvents.AMETHYST_BLOCK_HIT, () -> SoundEvents.AMETHYST_BLOCK_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /** Absence: a Saint of Tainted Jade grown into a Great Devil, a hollow where its face should be. */
    public static final Kind ABSENCE = Kind.of("absence", CreatureRank.GREAT, CreatureClass.DEVIL)
        .size(0.9F, 2.6F).stats(700.0, 32.0, 22.0, 0.31).follow(40.0)
        .move(Move.of("cut", Style.STRIKE, 16, 0.0, 3.0, 10, 10, 1.2F, 2.8, 9))
        .move(Move.of("sweep", Style.SWEEP, 24, 0.0, 3.4, 40, 6, 1.1F, 3.4, 14))
        .move(Move.of("void", Style.BREATH, 30, 2.0, 10.0, 100, 5, 1.4F, 10.0, 20))
        .commands("tainted_saint")
        .lair(Places.NETHERS_CITY, 40, 40, -35)
        .palette(Vfx.Palette.SHADOW).egg(0x2A6A44, 0x000000)
        .sounds(() -> SoundEvents.WARDEN_AMBIENT, () -> SoundEvents.AMETHYST_BLOCK_HIT, () -> SoundEvents.AMETHYST_BLOCK_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /** Shells of stone knights with soulless darkness inside them, moving through the Underworld. */
    public static final Kind DARK_ONE = Kind.of("dark_one", CreatureRank.GREAT, CreatureClass.MONSTER)
        .size(0.9F, 2.3F).stats(360.0, 22.0, 16.0, 0.27)
        .move(Move.of("strike", Style.STRIKE, 18, 0.0, 2.6, 14, 10, 1.2F, 2.4, 10))
        .move(Move.of("engulf", Style.GRAB, 24, 0.0, 3.0, 50, 5, 1.0F, 3.0, 14))
        .afflicts(() -> new MobEffectInstance(MobEffects.DARKNESS, 100, 0))
        .traits(Kind.Trait.SOULLESS)
        .haunt(Region.UNDERWORLD, 12, 1, 2)
        .palette(Vfx.Palette.SHADOW).egg(0x4A4A50, 0x050505)
        .sounds(() -> SoundEvents.WARDEN_AMBIENT, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /**
     * The Jade Titan, the Prince of Darkness: a towering colossus in jade armour with darkness boiling in its visor and
     * a great tendril of darkness rising from its back. Every fallen Saint of Tainted Jade is a part of it.
     */
    public static final Kind JADE_TITAN = Kind.of("jade_titan", CreatureRank.GREAT, CreatureClass.TITAN)
        .size(4.0F, 16.0F).stats(3200.0, 60.0, 30.0, 0.22).follow(64.0)
        .move(Move.of("smite", Style.SLAM, 34, 0.0, 7.0, 40, 8, 1.4F, 7.0, 22))
        .move(Move.of("sweep", Style.SWEEP, 30, 0.0, 9.0, 50, 6, 1.2F, 9.0, 18))
        .move(Move.of("darkness", Style.BREATH, 40, 4.0, 24.0, 120, 5, 1.6F, 24.0, 26))
        .move(Move.of("muster", Style.CALL, 30, 0.0, 60.0, 300, 3, 0.0F, 0.0, 15))
        .commands("tainted_saint").traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.NETHERS_CITY, 0, 0, -35)
        .lore().palette(Vfx.Palette.ACID).egg(0x2A8A54, 0x0A0A0A)
        .sounds(() -> SoundEvents.WARDEN_AMBIENT, () -> SoundEvents.IRON_GOLEM_HURT, () -> SoundEvents.WARDEN_DEATH, () -> SoundEvents.WARDEN_STEP)
        .build();

    /** The Horror of the Lightless Current: enormous tentacles rising from under the black river, spewing corrosive venom. */
    public static final Kind LIGHTLESS_HORROR = Kind.of("horror_of_the_lightless_current", CreatureRank.GREAT, CreatureClass.DEMON)
        .motion(Kind.Motion.ROOT).size(3.0F, 5.0F).stats(900.0, 30.0, 14.0, 0.0).follow(32.0)
        .move(Move.of("crush", Style.GRAB, 26, 0.0, 10.0, 30, 10, 1.3F, 10.0, 16))
        .move(Move.of("lash", Style.SWEEP, 22, 0.0, 7.0, 20, 8, 1.2F, 7.0, 12))
        .move(Move.of("venom", Style.BREATH, 30, 2.0, 12.0, 90, 6, 1.0F, 12.0, 20))
        .afflicts(() -> new MobEffectInstance(MobEffects.POISON, 120, 2))
        .afflicts(() -> new MobEffectInstance(MobEffects.WEAKNESS, 120, 1))
        .traits(Kind.Trait.HANGING)
        .lair(Places.UNDERGROUND_MINE, 0, 270, 41)
        .palette(Vfx.Palette.ACID).egg(0x0A0A10, 0x6A9A3A)
        .sounds(() -> SoundEvents.ELDER_GUARDIAN_AMBIENT, () -> SoundEvents.ELDER_GUARDIAN_HURT, () -> SoundEvents.ELDER_GUARDIAN_DEATH,
            () -> SoundEvents.ELDER_GUARDIAN_FLOP)
        .build();

    public static final List<Kind> ALL = List.of(MIST_BEING, TAINTED_SAINT, ABSENCE, DARK_ONE, JADE_TITAN, LIGHTLESS_HORROR);

    private MountainKinds() {
    }
}
