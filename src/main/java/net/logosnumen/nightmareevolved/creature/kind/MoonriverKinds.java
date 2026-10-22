package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;

/** The creatures of the Moonriver Plain. */
public final class MoonriverKinds {
    /**
     * A gaunt maw: a tall, starved-looking thing of sinew with too many arms, a huge head with tiny bloodshot eyes, and a
     * crimson maw that takes up most of it. An Awakened Tyrant, rare on the plain and feared there.
     */
    public static final Kind GAUNT_MAW = Kind.of("gaunt_maw", CreatureRank.AWAKENED, CreatureClass.TYRANT)
        .size(1.2F, 3.2F).stats(260.0, 18.0, 8.0, 0.3).follow(40.0)
        .move(Move.of("claws", Style.SWEEP, 16, 0.0, 3.2, 14, 10, 1.1F, 3.0, 8, 11))
        .move(Move.of("seize", Style.GRAB, 24, 0.0, 3.4, 40, 6, 1.3F, 3.2, 12))
        .move(Move.melee("devour", Style.BITE, 18, 11, 2.6, 1.6F, 14))
        .haunt(Region.MOONRIVER_PLAIN, 4, 1, 1)
        .palette(Vfx.Palette.BLOOD).egg(0x8A7A6A, 0xB0202A)
        .sounds(() -> SoundEvents.WARDEN_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.ZOMBIE_STEP)
        .build();

    public static final List<Kind> ALL = List.of(GAUNT_MAW);

    private MoonriverKinds() {
    }
}
