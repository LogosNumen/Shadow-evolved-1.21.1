package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.Godgrave;
import net.logosnumen.nightmareevolved.entity.creature.Abjuration;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The creatures of Godgrave, Great and worse. Its surface is the white sky's, so most of them live in the jungle
 * hollows inside the dead god's bones: the asuras of Condemnation guarding their ruined city and Condemnation itself,
 * the maggot beast, the jackal giant, the crimson python, the black tiger, the viper-tailed lion and the tyrant lizard,
 * the three-headed hound, blood mosquitoes and harpies, and the Guardian of Vanishing Lake. Abjuration keeps the
 * skull.
 */
public final class GodgraveKinds {
    /**
     * The asuras of Condemnation: hunched golems twice a person's height, shells of cracked stone draped in red moss
     * like a monk's robe with something mummified inside, a blank stone face with two flowers on black stalks growing
     * from its eye holes, and a mace cut from diamond. They guard the ruined city in the hollow bones.
     */
    public static final Kind ASURA = Kind.of("asura_of_condemnation", CreatureRank.GREAT, CreatureClass.BEAST)
        .size(1.4F, 3.6F).stats(600.0, 30.0, 22.0, 0.25)
        .move(Move.of("mace", Style.STRIKE, 20, 0.0, 3.2, 14, 10, 1.3F, 3.0, 12))
        .move(Move.of("crush", Style.SLAM, 28, 0.0, 3.4, 50, 5, 1.5F, 3.2, 18))
        .traits(Kind.Trait.PACK)
        .hauntNear(Region.GODGRAVE_HOLLOW, Places.MICTLAN_ZIGGURAT, 420, 18, 1, 3, Kind.Time.ANY)
        .lore().palette(Vfx.Palette.BLOOD).egg(0x8A8478, 0xB02028)
        .sounds(() -> SoundEvents.STONE_PLACE, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /**
     * Condemnation, the Cursed Tyrant, master of the asuras: a vaguely human shape built of the ruins themselves, of
     * stone and soil, bone, trees and whole fallen buildings, held together by its will, and maddening to look on.
     */
    public static final Kind CONDEMNATION = Kind.of("condemnation", CreatureRank.CURSED, CreatureClass.TYRANT)
        .size(6.0F, 24.0F).stats(6000.0, 90.0, 40.0, 0.2).follow(80.0)
        .move(Move.of("ruin", Style.SLAM, 40, 0.0, 12.0, 40, 8, 1.4F, 12.0, 28))
        .move(Move.of("sweep", Style.SWEEP, 34, 0.0, 14.0, 50, 6, 1.2F, 14.0, 22))
        .move(Move.of("rubble", Style.SHOT, 30, 6.0, 40.0, 60, 6, 1.0F, 0.0, 14, 18, 22, 26))
        .move(Move.of("call", Style.CALL, 30, 0.0, 80.0, 300, 3, 0.0F, 0.0, 15))
        .commands("asura_of_condemnation").traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.MICTLAN_ZIGGURAT, -100, 220, Godgrave.VAULT_FLOOR + 1)
        .lore().palette(Vfx.Palette.EARTH).egg(0x6A6458, 0x2A2420)
        .sounds(() -> SoundEvents.WARDEN_AMBIENT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.WARDEN_DEATH, () -> SoundEvents.WARDEN_STEP)
        .build();

    /**
     * Abjuration, the Cursed Demon of negation: a mountain of grey flesh grown over with scarlet moss, a hundred limbs,
     * countless eyes. It denies what strikes it, and looks back at whoever looks at it.
     */
    public static final Kind ABJURATION = Kind.of("abjuration", CreatureRank.CURSED, CreatureClass.DEMON)
        .size(7.0F, 9.0F).stats(5000.0, 70.0, 30.0, 0.16).follow(64.0)
        .move(Move.of("limbs", Style.SWEEP, 30, 0.0, 9.0, 30, 10, 1.2F, 9.0, 16))
        .move(Move.of("seize", Style.GRAB, 28, 0.0, 12.0, 40, 6, 1.3F, 12.0, 18))
        .move(Move.of("deny", Style.BREATH, 40, 4.0, 24.0, 120, 5, 1.5F, 24.0, 28))
        .afflicts(() -> new MobEffectInstance(MobEffects.WEAKNESS, 160, 2))
        .traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.MICTLAN_ZIGGURAT, -90, -560, Godgrave.VAULT_FLOOR + 1)
        .factory(Abjuration::new).palette(Vfx.Palette.SHADOW).egg(0x8A8A90, 0xB02028)
        .sounds(() -> SoundEvents.WARDEN_HEARTBEAT, () -> SoundEvents.WARDEN_HURT, () -> SoundEvents.WARDEN_DEATH, () -> SoundEvents.SLIME_SQUISH)
        .build();

    /** A grotesque beast whose mottled fur crawls with pale maggots, its limbs a mess of claws, chitin scythes and tentacles. */
    public static final Kind MAGGOT_BEAST = Kind.of("maggot_beast", CreatureRank.GREAT, CreatureClass.BEAST)
        .size(2.4F, 2.6F).stats(560.0, 30.0, 14.0, 0.3)
        .move(Move.of("rend", Style.STRIKE, 18, 0.0, 3.0, 12, 10, 1.3F, 2.8, 10))
        .move(Move.melee("maw", Style.BITE, 16, 10, 2.6, 1.5F, 20))
        .move(Move.of("tentacles", Style.GRAB, 24, 0.0, 5.0, 40, 6, 1.0F, 5.0, 14))
        .afflicts(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 2))
        .haunt(Region.GODGRAVE_HOLLOW, 10, 1, 1)
        .palette(Vfx.Palette.ACID).egg(0x6A5A4A, 0xE8E0D0)
        .sounds(() -> SoundEvents.RAVAGER_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.SLIME_SQUISH)
        .build();

    /** A giant with a jackal's head, its huge humanoid body a hill of scorched, rotting flesh, its fangs gleaming gold. */
    public static final Kind JACKAL_GIANT = Kind.of("jackal_giant", CreatureRank.GREAT, CreatureClass.BEAST)
        .size(3.0F, 9.0F).stats(1200.0, 40.0, 16.0, 0.26).follow(48.0)
        .move(Move.of("smash", Style.SLAM, 30, 0.0, 5.0, 40, 8, 1.4F, 5.0, 20))
        .move(Move.melee("jaws", Style.BITE, 24, 15, 4.0, 1.6F, 30))
        .move(Move.of("grab", Style.GRAB, 26, 0.0, 6.0, 50, 5, 1.2F, 6.0, 16))
        .traits(Kind.Trait.LIGHT_FALL)
        .haunt(Region.GODGRAVE_HOLLOW, 3, 1, 1)
        .palette(Vfx.Palette.FIRE).egg(0x2A1E1A, 0xD8A030)
        .sounds(() -> SoundEvents.RAVAGER_ROAR, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.RAVAGER_STEP)
        .build();

    /** A crimson python that lies among the vines of a bridge, passing for one of them until something crosses. */
    public static final Kind CRIMSON_PYTHON = Kind.of("crimson_python", CreatureRank.GREAT, CreatureClass.MONSTER)
        .size(1.6F, 1.2F).stats(520.0, 28.0, 12.0, 0.3)
        .move(Move.melee("strike", Style.BITE, 14, 8, 2.8, 1.5F, 10))
        .move(Move.of("coil", Style.GRAB, 26, 0.0, 3.6, 40, 6, 1.3F, 3.4, 16))
        .traits(Kind.Trait.LURKER)
        .haunt(Region.GODGRAVE_HOLLOW, 8, 1, 1)
        .palette(Vfx.Palette.BLOOD).egg(0xA01820, 0x3A0808)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.CAVE_VINES_STEP)
        .build();

    /** Once a majestic black tiger: its red stripes are open, festering wounds, and blood-red grass grows from the rot. */
    public static final Kind BLACK_TIGER = Kind.of("black_tiger", CreatureRank.GREAT, CreatureClass.MONSTER)
        .size(1.8F, 1.8F).stats(560.0, 30.0, 12.0, 0.34)
        .move(Move.melee("bite", Style.BITE, 14, 8, 2.2, 1.4F, 10))
        .move(Move.of("pounce", Style.POUNCE, 24, 3.0, 12.0, 40, 6, 1.6F, 2.2, 18))
        .move(Move.of("rake", Style.STRIKE, 16, 0.0, 2.6, 15, 8, 1.2F, 2.4, 9))
        .haunt(Region.GODGRAVE_HOLLOW, 8, 1, 1)
        .palette(Vfx.Palette.BLOOD).egg(0x101012, 0xC02020)
        .sounds(() -> SoundEvents.RAVAGER_AMBIENT, () -> SoundEvents.CAT_HURT, () -> SoundEvents.CAT_DEATH, () -> SoundEvents.WOLF_STEP)
        .build();

    /** Mosquitoes the size of dogs, their bellies glistening with rotten blood, in clouds that drain whatever they settle on. */
    public static final Kind BLOOD_MOSQUITO = Kind.of("blood_mosquito", CreatureRank.CORRUPTED, CreatureClass.BEAST)
        .motion(Kind.Motion.FLY).size(0.8F, 0.6F).stats(60.0, 10.0, 2.0, 0.36)
        .move(Move.melee("pierce", Style.BITE, 12, 7, 1.0, 1.0F, 8))
        .traits(Kind.Trait.PACK, Kind.Trait.DRAIN)
        .haunt(Region.GODGRAVE_HOLLOW, 14, 4, 7, Kind.Time.ANY, Kind.Ground.AIR)
        .palette(Vfx.Palette.BLOOD).egg(0x3A2A2A, 0x8A1010)
        .sounds(() -> SoundEvents.BEE_LOOP_AGGRESSIVE, () -> SoundEvents.BEE_HURT, () -> SoundEvents.BEE_DEATH, () -> SoundEvents.BEE_LOOP)
        .build();

    /** A harpy: a winged hag with a woman's withered face and an eagle's talons. */
    public static final Kind HARPY = Kind.of("harpy", CreatureRank.GREAT, CreatureClass.BEAST)
        .motion(Kind.Motion.FLY).size(1.0F, 1.8F).stats(420.0, 24.0, 6.0, 0.34)
        .move(Move.of("swoop", Style.POUNCE, 24, 2.0, 12.0, 40, 8, 1.4F, 2.0, 16))
        .move(Move.of("talons", Style.STRIKE, 14, 0.0, 2.2, 12, 10, 1.1F, 2.0, 8))
        .move(Move.of("shriek", Style.BREATH, 26, 0.0, 8.0, 90, 4, 0.6F, 8.0, 16))
        .afflicts(() -> new MobEffectInstance(MobEffects.CONFUSION, 80, 0))
        .haunt(Region.GODGRAVE_HOLLOW, 8, 2, 3, Kind.Time.ANY, Kind.Ground.AIR)
        .haunt(Region.GODGRAVE, 6, 1, 2, Kind.Time.ANY, Kind.Ground.AIR)
        .palette(Vfx.Palette.EARTH).egg(0x6A5A4A, 0xB0A090)
        .sounds(() -> SoundEvents.PHANTOM_AMBIENT, () -> SoundEvents.PHANTOM_HURT, () -> SoundEvents.PHANTOM_DEATH, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /** A hound as big as a hill, with three heads and jaws that could grind a mountain to dust. */
    public static final Kind THREE_HEADED_HOUND = Kind.of("three_headed_hound", CreatureRank.GREAT, CreatureClass.TYRANT)
        .size(5.0F, 6.0F).stats(2200.0, 50.0, 20.0, 0.3).follow(64.0)
        .move(Move.melee("jaws", Style.BITE, 20, 12, 4.4, 1.6F, 12))
        .move(Move.of("pounce", Style.POUNCE, 28, 4.0, 16.0, 50, 6, 1.6F, 4.0, 20))
        .move(Move.of("howl", Style.BREATH, 30, 0.0, 12.0, 120, 3, 0.8F, 12.0, 18))
        .afflicts(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1))
        .traits(Kind.Trait.LIGHT_FALL)
        .haunt(Region.GODGRAVE_HOLLOW, 2, 1, 1)
        .palette(Vfx.Palette.FIRE).egg(0x1A1414, 0xE06020)
        .sounds(() -> SoundEvents.WOLF_GROWL, () -> SoundEvents.WOLF_HURT, () -> SoundEvents.WOLF_DEATH, () -> SoundEvents.RAVAGER_STEP)
        .build();

    /** A horned lion with a viper for a tail, venom dripping from its fangs. */
    public static final Kind VIPER_LION = Kind.of("viper_lion", CreatureRank.GREAT, CreatureClass.MONSTER)
        .size(1.8F, 2.0F).stats(600.0, 30.0, 12.0, 0.32)
        .move(Move.melee("bite", Style.BITE, 14, 8, 2.2, 1.4F, 10))
        .move(Move.of("viper", Style.STRIKE, 18, 0.0, 3.4, 20, 8, 1.2F, 3.2, 11))
        .move(Move.of("pounce", Style.POUNCE, 24, 3.0, 12.0, 45, 6, 1.5F, 2.2, 18))
        .afflicts(() -> new MobEffectInstance(MobEffects.POISON, 140, 2))
        .haunt(Region.GODGRAVE_HOLLOW, 6, 1, 1)
        .palette(Vfx.Palette.ACID).egg(0x8A6A3A, 0x3A6A2A)
        .sounds(() -> SoundEvents.RAVAGER_AMBIENT, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.WOLF_STEP)
        .build();

    /** An enormous reptile on powerful hind legs with short, useless arms, whose footsteps shake the ground. */
    public static final Kind TYRANT_LIZARD = Kind.of("tyrant_lizard", CreatureRank.GREAT, CreatureClass.DEMON)
        .size(3.0F, 5.0F).stats(1400.0, 42.0, 16.0, 0.3).follow(48.0)
        .move(Move.melee("jaws", Style.BITE, 20, 12, 4.0, 1.7F, 12))
        .move(Move.of("stomp", Style.SLAM, 26, 0.0, 4.0, 40, 6, 1.3F, 4.0, 16))
        .move(Move.of("charge", Style.CHARGE, 30, 5.0, 16.0, 70, 5, 1.5F, 2.0, 24))
        .traits(Kind.Trait.LIGHT_FALL)
        .haunt(Region.GODGRAVE_HOLLOW, 3, 1, 1)
        .palette(Vfx.Palette.EARTH).egg(0x4A5A3A, 0x8A6A3A)
        .sounds(() -> SoundEvents.RAVAGER_ROAR, () -> SoundEvents.RAVAGER_HURT, () -> SoundEvents.RAVAGER_DEATH, () -> SoundEvents.RAVAGER_STEP)
        .build();

    /**
     * The Guardian of Vanishing Lake: a towering, vaguely human abomination in a billowing dark robe, silver armour on
     * its long arms and slender body, and six pairs of beautiful grey wings.
     */
    public static final Kind LAKE_GUARDIAN = Kind.of("guardian_of_vanishing_lake", CreatureRank.GREAT, CreatureClass.TERROR)
        .motion(Kind.Motion.HOVER).size(2.0F, 7.0F).stats(3000.0, 56.0, 26.0, 0.28).follow(64.0)
        .move(Move.of("rend", Style.STRIKE, 22, 0.0, 5.0, 14, 10, 1.3F, 4.6, 12))
        .move(Move.of("wings", Style.SWEEP, 30, 0.0, 7.0, 40, 6, 1.2F, 7.0, 18))
        .move(Move.of("descend", Style.POUNCE, 30, 4.0, 18.0, 60, 5, 1.6F, 4.0, 22))
        .traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.VANISHING_LAKE, 0, 84, Godgrave.VAULT_FLOOR + 4)
        .palette(Vfx.Palette.STEEL).egg(0x1A1A22, 0xC8CCD8)
        .sounds(() -> SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, () -> SoundEvents.IRON_GOLEM_HURT, () -> SoundEvents.WARDEN_DEATH, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /**
     * An ash worm: a grey worm of dead flesh dozens of metres long, a puppet with nothing of its own inside, crawling over
     * the ash beneath the bones. Like everything under the veil, it burns where the white sky breaks through.
     */
    public static final Kind ASH_WORM = Kind.of("ash_worm", CreatureRank.GREAT, CreatureClass.BEAST)
        .size(2.2F, 1.8F).stats(900.0, 34.0, 14.0, 0.26).follow(40.0)
        .move(Move.melee("maw", Style.BITE, 18, 10, 3.4, 1.5F, 12))
        .move(Move.of("crush", Style.SLAM, 26, 0.0, 4.0, 40, 6, 1.3F, 4.0, 16))
        .move(Move.of("lunge", Style.CHARGE, 26, 4.0, 14.0, 60, 5, 1.4F, 2.4, 20))
        .traits(Kind.Trait.SOULLESS, Kind.Trait.LIGHT_FALL)
        .haunt(Region.GODGRAVE, 5, 1, 1)
        .palette(Vfx.Palette.EARTH).egg(0x8A8A86, 0x3A3A38)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SLIME_HURT, () -> SoundEvents.SLIME_DEATH, () -> SoundEvents.SAND_STEP)
        .build();

    /**
     * The depth dweller of the Spinal Ocean: a leviathan under a carapace of clear obsidian, with black eyes and a forest
     * of limbs and antennae trailing through the dark water of the spine's canal.
     */
    public static final Kind DEPTH_DWELLER = Kind.of("depth_dweller", CreatureRank.GREAT, CreatureClass.DEMON)
        .motion(Kind.Motion.SWIM).size(5.0F, 3.0F).stats(3200.0, 50.0, 30.0, 0.3).follow(64.0)
        .move(Move.of("antennae", Style.SWEEP, 26, 0.0, 10.0, 30, 8, 1.2F, 10.0, 14))
        .move(Move.of("limbs", Style.GRAB, 30, 0.0, 6.0, 40, 6, 1.3F, 6.0, 16))
        .move(Move.melee("maw", Style.BITE, 20, 12, 4.0, 1.6F, 14))
        .afflicts(() -> new MobEffectInstance(MobEffects.BLINDNESS, 60, 0))
        .lair(Places.SPINAL_OCEAN, 0, 0, Godgrave.SPINAL_SEA - 8)
        .palette(Vfx.Palette.SHADOW).egg(0x1A1424, 0x6A5A8A)
        .sounds(() -> SoundEvents.ELDER_GUARDIAN_AMBIENT, () -> SoundEvents.ELDER_GUARDIAN_HURT, () -> SoundEvents.ELDER_GUARDIAN_DEATH, () -> SoundEvents.ELDER_GUARDIAN_FLOP)
        .build();

    public static final List<Kind> ALL = List.of(ASURA, CONDEMNATION, ABJURATION, MAGGOT_BEAST, JACKAL_GIANT, CRIMSON_PYTHON, BLACK_TIGER, BLOOD_MOSQUITO,
        HARPY, THREE_HEADED_HOUND, VIPER_LION, TYRANT_LIZARD, LAKE_GUARDIAN, ASH_WORM, DEPTH_DWELLER);

    private GodgraveKinds() {
    }
}
