package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.entity.creature.MirrorBeast;
import net.logosnumen.nightmareevolved.entity.creature.MordantMimic;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The creatures of the Chained Isles, Dormant to Fallen with Corrupted horrors among them. Ravagers and dread wolves
 * prowl the islands, chain worms coil about the chains, griffins and gargoyles take the air, ants swarm, and by night
 * the chalice maidens drift about the ruins. Reckoning Island has its Mirror Beast; Shipwreck Island its mimic, its
 * sailor dolls, the Wormvine and the Blood-Nourished Root beneath; and one island a Corrupted horror all its own.
 */
public final class IsleKinds {
    /** A lean, black-furred beast on four mighty paws, with a wide maw of fangs and two red eyes burning with madness. */
    public static final Kind BLACK_RAVAGER = Kind.of("black_ravager", CreatureRank.FALLEN, CreatureClass.BEAST)
        .size(1.3F, 1.5F).stats(70.0, 10.0, 4.0, 0.34)
        .move(Move.melee("bite", Style.BITE, 12, 7, 1.6, 1.2F, 8))
        .move(Move.of("pounce", Style.POUNCE, 22, 2.0, 9.0, 40, 6, 1.4F, 1.6, 16))
        .traits(Kind.Trait.PACK)
        .haunt(Region.CHAINED_ISLES, 18, 1, 3)
        .palette(Vfx.Palette.BLOOD).egg(0x141214, 0xD02020)
        .sounds(() -> SoundEvents.WOLF_GROWL, () -> SoundEvents.WOLF_HURT, () -> SoundEvents.WOLF_DEATH, () -> SoundEvents.WOLF_STEP)
        .build();

    /**
     * A snake's body, a human-like torso with two powerful arms, a long toothy muzzle and leathery wings. It feeds on
     * metal, and coils about the Isles' chains.
     */
    public static final Kind CHAIN_WORM = Kind.of("chain_worm", CreatureRank.FALLEN, CreatureClass.DEMON)
        .motion(Kind.Motion.FLY).size(1.6F, 2.2F).stats(150.0, 13.0, 8.0, 0.3)
        .move(Move.of("claws", Style.STRIKE, 16, 0.0, 2.6, 15, 10, 1.1F, 2.4, 9))
        .move(Move.melee("bite", Style.BITE, 16, 10, 2.4, 1.5F, 20))
        .move(Move.of("coil", Style.GRAB, 26, 0.0, 3.2, 60, 5, 1.3F, 3.0, 16))
        .haunt(Region.CHAINED_ISLES, 10, 1, 2, Kind.Time.ANY, Kind.Ground.AIR)
        .palette(Vfx.Palette.STEEL).egg(0x4A3A30, 0x9A9AA0)
        .sounds(() -> SoundEvents.PHANTOM_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /** Spectres of eerie beauty with an ocean of madness and bloodlust behind it. They come out only at night. */
    public static final Kind CHALICE_MAIDEN = Kind.of("chalice_maiden", CreatureRank.FALLEN, CreatureClass.DEMON)
        .motion(Kind.Motion.HOVER).size(0.8F, 2.2F).stats(120.0, 12.0, 2.0, 0.28)
        .move(Move.of("wail", Style.BREATH, 30, 0.0, 9.0, 80, 6, 0.8F, 9.0, 20))
        .move(Move.melee("embrace", Style.BITE, 18, 11, 1.6, 1.4F, 15))
        .afflicts(() -> new MobEffectInstance(MobEffects.WEAKNESS, 120, 1))
        .afflicts(() -> new MobEffectInstance(MobEffects.WITHER, 60, 0))
        .hauntNear(Region.CHAINED_ISLES, Places.NORTHERN_ISLAND, 220, 14, 1, 3, Kind.Time.NIGHT)
        .haunt(Region.CHAINED_ISLES, 4, 1, 1, Kind.Time.NIGHT, Kind.Ground.SURFACE)
        .palette(Vfx.Palette.SOUL).egg(0xD8E0F0, 0x6A2A4A)
        .sounds(() -> SoundEvents.VEX_AMBIENT, () -> SoundEvents.VEX_HURT, () -> SoundEvents.VEX_DEATH, () -> SoundEvents.VEX_AMBIENT)
        .build();

    /**
     * A wolf the size of a small truck, its fur as tough as steel wire, its long jaws able to bite a person in half and
     * grind bone and armour to dust, its breath unbearable.
     */
    public static final Kind DREAD_WOLF = Kind.of("dread_wolf", CreatureRank.FALLEN, CreatureClass.MONSTER)
        .size(2.2F, 2.4F).stats(160.0, 15.0, 10.0, 0.32)
        .move(Move.melee("jaws", Style.BITE, 16, 10, 2.4, 1.5F, 12))
        .move(Move.of("pounce", Style.POUNCE, 24, 3.0, 10.0, 50, 6, 1.5F, 2.4, 18))
        .move(Move.of("breath", Style.BREATH, 20, 0.0, 4.0, 120, 3, 0.4F, 4.0, 10))
        .afflicts(() -> new MobEffectInstance(MobEffects.CONFUSION, 80, 0))
        .traits(Kind.Trait.PACK)
        .haunt(Region.CHAINED_ISLES, 10, 1, 2)
        .palette(Vfx.Palette.STEEL).egg(0x5A5A60, 0x2A2A2E)
        .sounds(() -> SoundEvents.WOLF_GROWL, () -> SoundEvents.WOLF_HURT, () -> SoundEvents.WOLF_DEATH, () -> SoundEvents.WOLF_STEP)
        .build();

    /** An enormous winged beast, part lion, part eagle, and part pure nightmare. */
    public static final Kind GRIFFIN = Kind.of("griffin", CreatureRank.FALLEN, CreatureClass.MONSTER)
        .motion(Kind.Motion.FLY).size(2.2F, 2.0F).stats(140.0, 13.0, 6.0, 0.34)
        .move(Move.of("swoop", Style.POUNCE, 26, 2.0, 14.0, 40, 8, 1.5F, 2.4, 18))
        .move(Move.of("talons", Style.STRIKE, 16, 0.0, 2.6, 15, 10, 1.2F, 2.4, 9))
        .move(Move.melee("beak", Style.BITE, 16, 10, 2.4, 1.4F, 20))
        .haunt(Region.CHAINED_ISLES, 8, 1, 2, Kind.Time.DAY, Kind.Ground.AIR)
        .palette(Vfx.Palette.EARTH).egg(0xB08A4A, 0xE8E0D0)
        .sounds(() -> SoundEvents.PARROT_IMITATE_RAVAGER, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /**
     * The creature of Reckoning Island, said to twist the sight of those who meet it. It becomes its foe: their look,
     * their weapon, the strength of their blows, while staying as strong as a Fallen Devil.
     */
    public static final Kind MIRROR_BEAST = Kind.of("mirror_beast", CreatureRank.FALLEN, CreatureClass.DEVIL)
        .size(0.7F, 1.9F).stats(220.0, 14.0, 10.0, 0.33)
        .move(Move.of("cut", Style.STRIKE, 14, 0.0, 2.6, 10, 10, 1.0F, 2.4, 8))
        .move(Move.of("thrust", Style.BITE, 18, 0.5, 3.2, 30, 6, 1.4F, 3.0, 11))
        .move(Move.of("lunge", Style.POUNCE, 20, 3.0, 8.0, 50, 5, 1.3F, 1.8, 14))
        .lair(Places.RECKONING, 0, 13)
        .factory(MirrorBeast::new).lore().palette(Vfx.Palette.STEEL).egg(0xC8D0E0, 0x2A2A3A)
        .sounds(() -> SoundEvents.AMETHYST_BLOCK_CHIME, () -> SoundEvents.AMETHYST_BLOCK_HIT, () -> SoundEvents.GLASS_BREAK, () -> SoundEvents.AMETHYST_BLOCK_STEP)
        .build();

    /**
     * A long chest of adamantine dark wood bound in dull metal, its lid flat, big enough to hold a man. It has jagged
     * teeth and long emaciated limbs folded inside it, and it swallows its prey whole.
     */
    public static final Kind MORDANT_MIMIC = Kind.of("mordant_mimic", CreatureRank.FALLEN, CreatureClass.DEVIL)
        .size(1.6F, 1.1F).stats(240.0, 15.0, 14.0, 0.3)
        .move(Move.of("swallow", Style.SPECIAL, 20, 0.0, 2.2, 60, 6, 0.0F, 2.0, 12))
        .move(Move.melee("snap", Style.BITE, 14, 8, 1.8, 1.4F, 10))
        .move(Move.of("rake", Style.SWEEP, 20, 0.0, 2.8, 30, 6, 1.0F, 2.6, 12))
        .traits(Kind.Trait.LURKER)
        .lair(Places.SHIPWRECK, -6, 2)
        .factory(MordantMimic::new).lore().palette(Vfx.Palette.BLOOD).egg(0x3A2A1E, 0x8A8070)
        .sounds(() -> SoundEvents.CHEST_LOCKED, () -> SoundEvents.WOOD_HIT, () -> SoundEvents.WOOD_BREAK, () -> SoundEvents.WOOD_STEP)
        .build();

    /** A tall, menacing figure that puts itself together out of splintered wood, its hands long jagged blades. */
    public static final Kind SAILOR_DOLL = Kind.of("sailor_doll", CreatureRank.FALLEN, CreatureClass.BEAST)
        .size(0.8F, 2.3F).stats(60.0, 10.0, 6.0, 0.3)
        .move(Move.of("blades", Style.STRIKE, 14, 0.0, 2.2, 10, 10, 1.1F, 2.0, 7, 10))
        .move(Move.melee("stab", Style.BITE, 16, 10, 2.0, 1.4F, 20))
        .traits(Kind.Trait.PACK)
        .hauntNear(Region.CHAINED_ISLES, Places.SHIPWRECK, 70, 30, 2, 3, Kind.Time.ANY)
        .palette(Vfx.Palette.EARTH).egg(0x6A4A30, 0x9A9AA0)
        .sounds(() -> SoundEvents.WOOD_PLACE, () -> SoundEvents.WOOD_HIT, () -> SoundEvents.WOOD_BREAK, () -> SoundEvents.WOOD_STEP)
        .build();

    /**
     * Reddish-brown vines breathing a sweet poison, sprawling over all of Shipwreck Island from a mass of pulsing moss
     * around a human shape twisted in agony. It sleeps until a vine is disturbed, and fears fire.
     */
    public static final Kind WORMVINE = Kind.of("wormvine", CreatureRank.CORRUPTED, CreatureClass.MONSTER)
        .motion(Kind.Motion.ROOT).size(3.0F, 2.6F).stats(520.0, 18.0, 8.0, 0.0).follow(24.0)
        .move(Move.of("lash", Style.SWEEP, 22, 0.0, 7.0, 20, 10, 1.2F, 7.0, 12))
        .move(Move.of("seize", Style.GRAB, 26, 0.0, 9.0, 50, 6, 1.0F, 9.0, 16))
        .move(Move.of("sweetness", Style.BREATH, 30, 0.0, 8.0, 90, 5, 0.6F, 8.0, 20))
        .afflicts(() -> new MobEffectInstance(MobEffects.POISON, 120, 1))
        .afflicts(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1))
        .traits(Kind.Trait.LURKER, Kind.Trait.FIRE_WEAK)
        .lair(Places.SHIPWRECK, 30, 0)
        .lore().palette(Vfx.Palette.ACID).egg(0x6A3A22, 0x3A5A2A)
        .sounds(() -> SoundEvents.CAVE_VINES_PLACE, () -> SoundEvents.CAVE_VINES_HIT, () -> SoundEvents.CAVE_VINES_BREAK, () -> SoundEvents.CAVE_VINES_STEP)
        .build();

    /**
     * A vast old root gone corrupt beneath Shipwreck Island, once the sacred tree that powered the ship. Its root-like
     * tentacles reach out of the dark under the isle and snatch whatever flies too near. It fears fire.
     */
    public static final Kind BLOOD_NOURISHED_ROOT = Kind.of("blood_nourished_root", CreatureRank.CORRUPTED, CreatureClass.MONSTER)
        .motion(Kind.Motion.ROOT).size(4.0F, 6.0F).stats(560.0, 18.0, 10.0, 0.0).follow(28.0)
        .move(Move.of("snatch", Style.GRAB, 24, 0.0, 12.0, 30, 10, 1.2F, 12.0, 14))
        .move(Move.of("lash", Style.SWEEP, 22, 0.0, 6.0, 20, 8, 1.3F, 6.0, 12))
        .traits(Kind.Trait.HANGING, Kind.Trait.FIRE_WEAK)
        .lair(Places.SHIPWRECK, 0, 0, Kind.Lair.BENEATH)
        .palette(Vfx.Palette.BLOOD).egg(0x4A2A1A, 0x8A1A1A)
        .sounds(() -> SoundEvents.ROOTS_PLACE, () -> SoundEvents.ROOTS_HIT, () -> SoundEvents.ROOTS_BREAK, () -> SoundEvents.ROOTS_STEP)
        .build();

    /** Giant ants that swarm over the islands. */
    public static final Kind GIANT_ANT = Kind.of("giant_ant", CreatureRank.DORMANT, CreatureClass.BEAST)
        .size(1.0F, 0.8F).stats(18.0, 4.0, 4.0, 0.32)
        .move(Move.melee("mandibles", Style.BITE, 10, 6, 1.0, 1.0F, 6))
        .traits(Kind.Trait.PACK)
        .haunt(Region.CHAINED_ISLES, 14, 3, 6, Kind.Time.DAY, Kind.Ground.SURFACE)
        .palette(Vfx.Palette.ACID).egg(0x3A2418, 0x8A4A2A)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.SILVERFISH_STEP)
        .build();

    /** Creatures like stone gargoyles that keep to the edges of the islands. */
    public static final Kind STONE_GARGOYLE = Kind.of("stone_gargoyle", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .motion(Kind.Motion.FLY).size(1.0F, 1.4F).stats(60.0, 8.0, 12.0, 0.3)
        .move(Move.of("dive", Style.POUNCE, 22, 2.0, 10.0, 40, 6, 1.3F, 1.6, 16))
        .move(Move.of("claws", Style.STRIKE, 14, 0.0, 2.0, 12, 10, 1.0F, 1.8, 8))
        .haunt(Region.CHAINED_ISLES, 8, 2, 3, Kind.Time.ANY, Kind.Ground.AIR)
        .palette(Vfx.Palette.EARTH).egg(0x7A7670, 0x3A3632)
        .sounds(() -> SoundEvents.STONE_PLACE, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /**
     * The Corrupted horror that keeps one of the two isles between the forest of twisted trees and the Sanctuary, said
     * to be dreadful past bearing. The novel never shows it; this is the mod's: a hunched mass of grey hide and bone
     * plates on four crooked limbs, with a cluster of eyes and a maw that splits its whole front.
     */
    public static final Kind ISLE_HORROR = Kind.of("isle_horror", CreatureRank.CORRUPTED, CreatureClass.MONSTER)
        .size(3.0F, 3.6F).stats(520.0, 22.0, 14.0, 0.26).follow(40.0)
        .move(Move.of("maw", Style.BITE, 20, 0.0, 3.2, 20, 10, 1.5F, 3.0, 12))
        .move(Move.of("trample", Style.CHARGE, 30, 4.0, 14.0, 70, 5, 1.4F, 1.5, 24))
        .move(Move.of("slam", Style.SLAM, 28, 0.0, 3.6, 50, 6, 1.3F, 3.6, 18))
        .lair(Places.CORRUPTED_ISLAND, 0, 0)
        .palette(Vfx.Palette.ACID).egg(0x6A6A70, 0x9CFF5A)
        .sounds(() -> SoundEvents.RAVAGER_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.RAVAGER_STEP)
        .build();

    public static final List<Kind> ALL = List.of(BLACK_RAVAGER, CHAIN_WORM, CHALICE_MAIDEN, DREAD_WOLF, GRIFFIN, MIRROR_BEAST, MORDANT_MIMIC, SAILOR_DOLL,
        WORMVINE, BLOOD_NOURISHED_ROOT, GIANT_ANT, STONE_GARGOYLE, ISLE_HORROR);

    private IsleKinds() {
    }
}
