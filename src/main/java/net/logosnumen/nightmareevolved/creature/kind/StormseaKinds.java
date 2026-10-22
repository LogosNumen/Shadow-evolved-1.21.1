package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** The creatures of the Stormsea: the serpents of its waves, and Old Tom. */
public final class StormseaKinds {
    /** A sea serpent of the Stormsea: long, scaled the grey-green of storm water, finned, with a mouth full of hooks. */
    public static final Kind SEA_SERPENT = Kind.of("sea_serpent", CreatureRank.CORRUPTED, CreatureClass.BEAST)
        .motion(Kind.Motion.SWIM).size(1.6F, 1.2F).stats(420.0, 24.0, 10.0, 0.4).follow(40.0)
        .move(Move.melee("bite", Style.BITE, 14, 8, 2.6, 1.4F, 10))
        .move(Move.of("coil", Style.GRAB, 26, 0.0, 3.2, 40, 6, 1.3F, 3.0, 14))
        .move(Move.of("lunge", Style.CHARGE, 22, 3.0, 12.0, 50, 6, 1.3F, 1.6, 16))
        .haunt(Region.STORMSEA, 10, 1, 2)
        .palette(Vfx.Palette.FROST).egg(0x3A5A5A, 0xC8D8D0)
        .sounds(() -> SoundEvents.GUARDIAN_AMBIENT, () -> SoundEvents.GUARDIAN_HURT, () -> SoundEvents.GUARDIAN_DEATH, () -> SoundEvents.GUARDIAN_FLOP)
        .build();

    /**
     * Old Tom, as the soldiers of the Night Garden call it: something like an oyster, its shell the size of an island,
     * gold beneath a forest of black growths, and more tentacles reaching out of it than a shell that size could ever
     * hold, bleeding oily black. They have driven it off before. It always comes back. (The novel gives no rank; Great
     * Terror is the mod's.)
     */
    public static final Kind OLD_TOM = Kind.of("old_tom", CreatureRank.GREAT, CreatureClass.TERROR)
        .motion(Kind.Motion.SWIM).size(10.0F, 5.0F).stats(6000.0, 60.0, 34.0, 0.0).follow(48.0)
        .move(Move.of("tentacles", Style.SWEEP, 30, 0.0, 16.0, 30, 10, 1.2F, 16.0, 16))
        .move(Move.of("crush", Style.GRAB, 36, 0.0, 14.0, 50, 6, 1.5F, 14.0, 20))
        .move(Move.of("deluge", Style.SLAM, 34, 0.0, 12.0, 70, 4, 1.4F, 12.0, 22))
        .afflicts(() -> new MobEffectInstance(MobEffects.BLINDNESS, 60, 0))
        .lair(Places.NIGHT_GARDEN, 0, 80, Ground.SEA_LEVEL - 4)
        .palette(Vfx.Palette.SHADOW).egg(0xC8A040, 0x101010)
        .sounds(() -> SoundEvents.ELDER_GUARDIAN_AMBIENT, () -> SoundEvents.ELDER_GUARDIAN_HURT, () -> SoundEvents.ELDER_GUARDIAN_DEATH, () -> SoundEvents.ELDER_GUARDIAN_FLOP)
        .build();

    public static final List<Kind> ALL = List.of(SEA_SERPENT, OLD_TOM);

    private StormseaKinds() {
    }
}
