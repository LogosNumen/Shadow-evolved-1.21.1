package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.entity.creature.ForestMaw;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;

/** The creatures of the lands around Bastion. */
public final class BastionKinds {
    /**
     * The Undetectable Forest Maw: a Fallen Monster no eye, ear or sense finds before it is too late. Whoever steps where
     * it waits finds the forest floor opening into a maw beneath them, and roots closing in.
     */
    public static final Kind FOREST_MAW = Kind.of("undetectable_forest_maw", CreatureRank.FALLEN, CreatureClass.MONSTER)
        .motion(Kind.Motion.ROOT).size(3.0F, 1.4F).stats(300.0, 16.0, 10.0, 0.0)
        .move(Move.melee("devour", Style.BITE, 18, 10, 2.4, 1.6F, 12))
        .move(Move.of("roots", Style.GRAB, 24, 0.0, 4.0, 40, 6, 1.2F, 4.0, 12))
        .move(Move.of("lash", Style.SWEEP, 16, 0.0, 4.4, 20, 8, 1.0F, 4.2, 9))
        .traits(Kind.Trait.LURKER)
        .haunt(Region.BASTION, 3, 1, 1)
        .factory(ForestMaw::new).palette(Vfx.Palette.EARTH).egg(0x3A4A2A, 0x6A2A24)
        .sounds(() -> SoundEvents.WARDEN_SNIFF, () -> SoundEvents.ROOTED_DIRT_HIT, () -> SoundEvents.WARDEN_DEATH, () -> SoundEvents.ROOTED_DIRT_STEP)
        .build();

    public static final List<Kind> ALL = List.of(FOREST_MAW);

    private BastionKinds() {
    }
}
