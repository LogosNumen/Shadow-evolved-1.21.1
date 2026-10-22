package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.entity.creature.ChaliceKnight;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;

/** The creatures around Ravenheart in the Song Domain: the Chalice Knights and the stone worms. */
public final class RavenheartKinds {
    /**
     * A Chalice Knight: two and a half metres of rust-green armour under a helm shaped like a beast's head, fighting with
     * a heavy blade. Pressed hard, it splits open, and the beast that was inside the armour all along bursts out.
     */
    public static final Kind CHALICE_KNIGHT = Kind.of("chalice_knight", CreatureRank.AWAKENED, CreatureClass.DEMON)
        .size(0.9F, 2.5F).stats(170.0, 14.0, 14.0, 0.27)
        .move(Move.of("cut", Style.STRIKE, 16, 0.0, 2.6, 12, 10, 1.1F, 2.4, 9))
        .move(Move.of("thrust", Style.BITE, 18, 1.0, 3.4, 25, 6, 1.4F, 3.2, 11))
        .move(Move.of("cleave", Style.SLAM, 24, 0.0, 3.0, 40, 5, 1.5F, 2.8, 15))
        .move(Move.of("rend", Style.STRIKE, 12, 0.0, 2.4, 8, 10, 1.1F, 2.2, 6))
        .move(Move.melee("maul", Style.BITE, 14, 8, 2.0, 1.4F, 10))
        .move(Move.of("pounce", Style.POUNCE, 20, 3.0, 10.0, 40, 6, 1.4F, 1.8, 14))
        .traits(Kind.Trait.PACK)
        .haunt(Region.RAVENHEART, 10, 1, 3)
        .factory(ChaliceKnight::new).palette(Vfx.Palette.BLOOD).egg(0x4A5A3A, 0x8A4A2A)
        .sounds(() -> SoundEvents.VINDICATOR_AMBIENT, () -> SoundEvents.IRON_GOLEM_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.NETHERITE_BLOCK_STEP)
        .build();

    /**
     * A stone worm: a scaled worm on six thin limbs that end in dagger claws, with a round mouth ringed in needles. Its
     * hide makes good scale mail.
     */
    public static final Kind STONE_WORM = Kind.of("stone_worm", CreatureRank.DORMANT, CreatureClass.BEAST)
        .size(0.9F, 0.7F).stats(40.0, 6.0, 8.0, 0.28)
        .move(Move.melee("bite", Style.BITE, 12, 7, 1.4, 1.2F, 8))
        .move(Move.of("claws", Style.STRIKE, 14, 0.0, 1.8, 15, 8, 1.1F, 1.6, 8))
        .move(Move.of("lunge", Style.POUNCE, 18, 2.0, 7.0, 40, 5, 1.2F, 1.2, 12))
        .traits(Kind.Trait.PACK)
        .haunt(Region.RAVENHEART, 18, 2, 4)
        .palette(Vfx.Palette.EARTH).egg(0x6A6460, 0x3A3632)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.STONE_STEP)
        .build();

    public static final List<Kind> ALL = List.of(CHALICE_KNIGHT, STONE_WORM);

    private RavenheartKinds() {
    }
}
