package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.landmark.BurnedPlaces;
import net.logosnumen.nightmareevolved.entity.creature.MillipedeQueen;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;

/** The creatures of the Burned Forest: the Black Millipede tribe, the painted elders that lead it, and a queen in her hive. */
public final class BurnedKinds {
    /**
     * A black millipede of the tribe that rules the Burned Forest: three metres and more of black armoured segments as
     * wide as a person's chest, and at the front a mouth uncannily like a human one, full of needle fangs. They pour
     * through the deadfall in tides.
     */
    public static final Kind BLACK_MILLIPEDE = Kind.of("black_millipede", CreatureRank.CORRUPTED, CreatureClass.BEAST)
        .size(1.0F, 0.8F).stats(240.0, 18.0, 14.0, 0.32)
        .move(Move.melee("bite", Style.BITE, 12, 7, 1.8, 1.2F, 8))
        .move(Move.of("rear", Style.SLAM, 20, 0.0, 2.6, 30, 6, 1.3F, 2.4, 12))
        .move(Move.of("rush", Style.CHARGE, 24, 3.0, 12.0, 60, 5, 1.2F, 1.4, 18))
        .traits(Kind.Trait.PACK)
        .haunt(Region.BURNED_FOREST, 30, 3, 6)
        .palette(Vfx.Palette.SHADOW).egg(0x141416, 0x8A3A3A)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.SILVERFISH_STEP)
        .build();

    /** One of the tribe's elders, grown great and bright: the shell of a leader is crimson, azure or gold, not black. */
    public static final Kind PAINTED_MILLIPEDE = Kind.of("painted_millipede", CreatureRank.GREAT, CreatureClass.MONSTER)
        .size(1.8F, 1.4F).stats(800.0, 32.0, 22.0, 0.3).follow(48.0)
        .move(Move.melee("bite", Style.BITE, 14, 8, 2.6, 1.4F, 10))
        .move(Move.of("rear", Style.SLAM, 22, 0.0, 3.4, 30, 6, 1.4F, 3.2, 14))
        .move(Move.of("rush", Style.CHARGE, 26, 3.0, 14.0, 60, 5, 1.3F, 2.0, 20))
        .move(Move.of("call", Style.CALL, 20, 0.0, 48.0, 200, 3, 0.0F, 0.0, 10))
        .commands("black_millipede")
        .haunt(Region.BURNED_FOREST, 4, 1, 1)
        .looks(3).palette(Vfx.Palette.BLOOD).egg(0x9A1A20, 0xB08A1A)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.SILVERFISH_STEP)
        .build();

    /**
     * A Millipede Queen: enormous, in chitin nothing pierces, bringing forth young without end. In a fight she slips
     * between two moments and back, and blows meant for her pass through empty time. She keeps to her hive.
     */
    public static final Kind MILLIPEDE_QUEEN = Kind.of("millipede_queen", CreatureRank.GREAT, CreatureClass.TYRANT)
        .size(4.0F, 3.0F).stats(4000.0, 56.0, 40.0, 0.22).follow(64.0)
        .move(Move.melee("bite", Style.BITE, 20, 12, 4.6, 1.6F, 12))
        .move(Move.of("crush", Style.SLAM, 28, 0.0, 5.0, 40, 6, 1.5F, 5.0, 18))
        .move(Move.of("brood", Style.SPECIAL, 30, 0.0, 32.0, 160, 5, 0.0F, 0.0, 18))
        .move(Move.of("call", Style.CALL, 20, 0.0, 64.0, 200, 3, 0.0F, 0.0, 10))
        .commands("black_millipede", "painted_millipede").traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.MILLIPEDE_HIVE, 0, 0, BurnedPlaces.Hive.FLOOR + 1)
        .factory(MillipedeQueen::new).palette(Vfx.Palette.RUNE).egg(0x0A0A0C, 0x5AA0C8)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.RAVAGER_STEP)
        .build();

    public static final List<Kind> ALL = List.of(BLACK_MILLIPEDE, PAINTED_MILLIPEDE, MILLIPEDE_QUEEN);

    private BurnedKinds() {
    }
}
