package net.logosnumen.nightmareevolved.creature.kind;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.kind.Move.Style;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CrimsonSpire;
import net.logosnumen.nightmareevolved.entity.creature.ForsakenKnight;
import net.logosnumen.nightmareevolved.entity.creature.SoulTree;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The creatures of the Forgotten Shore: Awakened at the least, and up to Great. The carapace creatures hunt the
 * labyrinth, iron spiders web it from their nest, stone saints and blood fiends walk the Dark City, and its named
 * horrors keep their places: the Carapace Demon and the Soul Devouring Tree on the Ashen Barrow, the Black Knight in
 * the Ruined Cathedral, the Crimson Terror inside the Spire with its coral golems, the Cursed Herald circling the
 * Spire, the Lord of the Dead on Bone Ridge, the Iron Spider Matriarch in her nest.
 */
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

    /**
     * The Black Knight: over two metres of gothic plate in lusterless anthracite steel, engraved with a tale that drives
     * the reader mad, his helm crowned with curved horns that may once have been wings, two red flames burning in the
     * slit of his visor. He keeps the Ruined Cathedral of the Dark City; its darkness heals him and hides his steps.
     */
    public static final Kind FORSAKEN_KNIGHT = Kind.of("forsaken_knight", CreatureRank.FALLEN, CreatureClass.DEVIL)
        .size(0.9F, 2.4F).stats(260.0, 18.0, 18.0, 0.3)
        .move(Move.of("cleave", Style.STRIKE, 18, 0.0, 2.8, 16, 10, 1.3F, 2.6, 10))
        .move(Move.of("thrust", Style.BITE, 20, 0.5, 3.4, 40, 7, 1.7F, 3.0, 12))
        .move(Move.of("sweep", Style.SWEEP, 24, 0.0, 3.0, 60, 5, 1.1F, 3.0, 14))
        .move(Move.of("shadow_step", Style.SPECIAL, 12, 4.0, 18.0, 100, 6, 0.0F, 0.0, 6))
        .traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.DARK_CITY, -90, -70)
        .factory(ForsakenKnight::new).palette(Vfx.Palette.SHADOW).egg(0x24242A, 0xC02020)
        .sounds(() -> SoundEvents.WITHER_SKELETON_AMBIENT, () -> SoundEvents.WITHER_SKELETON_HURT, () -> SoundEvents.WITHER_SKELETON_DEATH,
            () -> SoundEvents.NETHERITE_BLOCK_STEP)
        .build();

    /**
     * A massive lion-raven: a corpse-pale body with dark, blood-stained feathers over its head and chest, two powerful
     * hind legs and six more clawed limbs jutting messily from its chest, and a long jagged black beak over needle
     * fangs and a red tongue. Spire Messengers come from the Spire and hunt the labyrinth.
     */
    public static final Kind SPIRE_MESSENGER = Kind.of("spire_messenger", CreatureRank.FALLEN, CreatureClass.BEAST)
        .motion(Kind.Motion.FLY).size(1.8F, 2.0F).stats(70.0, 9.0, 4.0, 0.34)
        .move(Move.of("swoop", Style.POUNCE, 26, 2.0, 12.0, 40, 8, 1.4F, 2.0, 18))
        .move(Move.of("rake", Style.STRIKE, 16, 0.0, 2.4, 15, 10, 1.1F, 2.2, 9))
        .move(Move.melee("peck", Style.BITE, 16, 10, 2.2, 1.4F, 25))
        .haunt(Region.FORGOTTEN_SHORE, 6, 1, 2, Kind.Time.NIGHT, Kind.Ground.AIR)
        .looks(2).palette(Vfx.Palette.BLOOD).egg(0xD8D0C8, 0x141014)
        .sounds(() -> SoundEvents.PHANTOM_AMBIENT, () -> SoundEvents.PHANTOM_HURT, () -> SoundEvents.PHANTOM_DEATH, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /** The greatest of the Spire's messengers, the Nightmare Champion of the Slayer's statue, circling the Spire. */
    public static final Kind CURSED_HERALD = Kind.of("cursed_herald", CreatureRank.FALLEN, CreatureClass.MONSTER)
        .motion(Kind.Motion.FLY).size(2.4F, 2.6F).stats(150.0, 14.0, 8.0, 0.32).follow(48.0)
        .move(Move.of("swoop", Style.POUNCE, 26, 2.0, 14.0, 40, 8, 1.5F, 2.5, 18))
        .move(Move.of("rake", Style.STRIKE, 16, 0.0, 2.8, 15, 10, 1.2F, 2.4, 9))
        .move(Move.melee("peck", Style.BITE, 18, 11, 2.6, 1.6F, 25))
        .lair(Places.CRIMSON_SPIRE, 60, 0, 250)
        .lore().palette(Vfx.Palette.BLOOD).egg(0xE8E0D8, 0x6A0A0A)
        .sounds(() -> SoundEvents.PHANTOM_AMBIENT, () -> SoundEvents.PHANTOM_HURT, () -> SoundEvents.PHANTOM_DEATH, () -> SoundEvents.PHANTOM_FLAP)
        .build();

    /**
     * The Nameless Sun: a giant made of crimson coral fused with mutilated flesh, with hundreds of human faces frozen
     * screaming, their eyes wells of darkness that blaze with annihilating light. It guards the Spire's Gateway.
     */
    public static final Kind CRIMSON_TERROR = Kind.of("crimson_terror", CreatureRank.FALLEN, CreatureClass.TERROR)
        .size(5.0F, 8.0F).stats(900.0, 28.0, 14.0, 0.2).follow(48.0)
        .move(Move.of("crush", Style.SLAM, 30, 0.0, 4.8, 50, 8, 1.5F, 4.5, 20))
        .move(Move.of("lash", Style.SWEEP, 26, 0.0, 5.2, 30, 10, 1.2F, 5.0, 15))
        .move(Move.of("sunlight", Style.BREATH, 40, 3.0, 18.0, 140, 6, 2.2F, 18.0, 28))
        .commands("crimson_golem").traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.CRIMSON_SPIRE, 0, 0, CrimsonSpire.BASE + 1)
        .lore().palette(Vfx.Palette.LIGHT).egg(0xA02028, 0xFFE0A0)
        .sounds(() -> SoundEvents.WARDEN_AMBIENT, () -> SoundEvents.WARDEN_HURT, () -> SoundEvents.WARDEN_DEATH, () -> SoundEvents.WARDEN_STEP)
        .build();

    /** Golems of crimson coral in the shapes of the people who were given to the Spire's sun. */
    public static final Kind CRIMSON_GOLEM = Kind.of("crimson_golem", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .size(1.0F, 2.6F).stats(60.0, 9.0, 10.0, 0.22)
        .move(Move.melee("slam", Style.STRIKE, 20, 12, 2.0, 1.2F, 12))
        .hauntNear(Region.FORGOTTEN_SHORE, Places.CRIMSON_SPIRE, 70, 25, 2, 4, Kind.Time.ANY)
        .palette(Vfx.Palette.BLOOD).egg(0xB02830, 0x5A0A10)
        .sounds(() -> SoundEvents.BASALT_HIT, () -> SoundEvents.BASALT_BREAK, () -> SoundEvents.BASALT_BREAK, () -> SoundEvents.BASALT_STEP)
        .build();

    /**
     * A skeletal, hunched thing at least two metres tall in a torn shroud gone brown with old blood, with long clawed
     * arms and a deformed head split by a wide mouth of razor fangs. They hunt the Dark City by night.
     */
    public static final Kind BLOOD_FIEND = Kind.of("blood_fiend", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .size(0.8F, 2.2F).stats(55.0, 9.0, 3.0, 0.32)
        .move(Move.melee("claws", Style.STRIKE, 12, 7, 1.8, 1.0F, 8))
        .move(Move.of("lunge", Style.POUNCE, 22, 2.0, 7.0, 50, 6, 1.4F, 1.6, 16))
        .move(Move.melee("bite", Style.BITE, 14, 8, 1.4, 1.3F, 20))
        .hauntNear(Region.FORGOTTEN_SHORE, Places.DARK_CITY, 320, 14, 1, 2, Kind.Time.NIGHT)
        .palette(Vfx.Palette.BLOOD).egg(0x8A6A50, 0x5A1010)
        .sounds(() -> SoundEvents.HUSK_AMBIENT, () -> SoundEvents.HUSK_HURT, () -> SoundEvents.HUSK_DEATH, () -> SoundEvents.HUSK_STEP)
        .build();

    /** A flower of red pollen that takes root in whatever breathes it and eats it from inside. */
    public static final Kind BLOOD_FLOWER = Kind.of("blood_flower", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .motion(Kind.Motion.ROOT).size(1.2F, 1.6F).stats(40.0, 6.0, 0.0, 0.0)
        .move(Move.of("pollen", Style.BREATH, 30, 0.0, 6.0, 60, 10, 0.8F, 6.0, 20))
        .afflicts(() -> new MobEffectInstance(MobEffects.POISON, 100, 1))
        .afflicts(() -> new MobEffectInstance(MobEffects.CONFUSION, 120, 0))
        .haunt(Region.FORGOTTEN_SHORE, 6, 1, 2)
        .lore().palette(Vfx.Palette.BLOOD).egg(0x3A5A2A, 0xE02030)
        .sounds(() -> SoundEvents.MOSS_PLACE, () -> SoundEvents.MOSS_HIT, () -> SoundEvents.MOSS_BREAK, () -> SoundEvents.MOSS_STEP)
        .build();

    /** An enormous leech a metre long, slimy, half translucent and the red of dried blood. It feeds as it bites. */
    public static final Kind CORPSE_EATER = Kind.of("corpse_eater", CreatureRank.FALLEN, CreatureClass.BEAST)
        .size(1.0F, 0.6F).stats(40.0, 7.0, 2.0, 0.24)
        .move(Move.melee("latch", Style.BITE, 12, 6, 1.0, 1.0F, 6))
        .traits(Kind.Trait.DRAIN)
        .haunt(Region.FORGOTTEN_SHORE, 6, 1, 3, Kind.Time.NIGHT, Kind.Ground.SURFACE)
        .lore().palette(Vfx.Palette.BLOOD).egg(0x7A1418, 0xC86060)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.SILVERFISH_STEP)
        .build();

    /** A giant locust. They come in swarms, leaping as they bite. */
    public static final Kind FLESH_REAVER = Kind.of("flesh_reaver", CreatureRank.AWAKENED, CreatureClass.BEAST)
        .size(1.0F, 0.9F).stats(22.0, 5.0, 3.0, 0.33)
        .move(Move.melee("bite", Style.BITE, 10, 6, 1.0, 1.0F, 6))
        .move(Move.of("leap", Style.POUNCE, 18, 2.0, 8.0, 30, 6, 1.2F, 1.2, 13))
        .traits(Kind.Trait.PACK, Kind.Trait.LIGHT_FALL)
        .haunt(Region.FORGOTTEN_SHORE, 12, 3, 6, Kind.Time.DAY, Kind.Ground.SURFACE)
        .lore().palette(Vfx.Palette.ACID).egg(0x6A6A2A, 0x3A2A10)
        .sounds(() -> SoundEvents.SILVERFISH_AMBIENT, () -> SoundEvents.SILVERFISH_HURT, () -> SoundEvents.SILVERFISH_DEATH, () -> SoundEvents.SILVERFISH_STEP)
        .build();

    /** A giant spider that hangs webs as strong as iron through the labyrinth to catch its prey. */
    public static final Kind IRON_SPIDER = Kind.of("iron_spider", CreatureRank.AWAKENED, CreatureClass.BEAST)
        .motion(Kind.Motion.CLIMB).size(1.6F, 1.0F).stats(38.0, 6.0, 10.0, 0.3)
        .move(Move.melee("bite", Style.BITE, 12, 7, 1.4, 1.0F, 8))
        .move(Move.of("web", Style.SHOT, 20, 3.0, 14.0, 80, 6, 0.4F, 0.0, 12))
        .afflicts(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2))
        .traits(Kind.Trait.PACK)
        .hauntNear(Region.FORGOTTEN_SHORE, Places.IRON_NEST, 260, 20, 2, 4, Kind.Time.ANY)
        .haunt(Region.FORGOTTEN_SHORE, 5, 1, 2)
        .palette(Vfx.Palette.STEEL).egg(0x5A5C66, 0x2A2C30)
        .sounds(() -> SoundEvents.SPIDER_AMBIENT, () -> SoundEvents.SPIDER_HURT, () -> SoundEvents.SPIDER_DEATH, () -> SoundEvents.SPIDER_STEP)
        .build();

    /** The mother of all the iron spiders, as big as a house and armoured like a tank, in the heart of her nest. */
    public static final Kind IRON_SPIDER_MATRIARCH = Kind.of("iron_spider_matriarch", CreatureRank.AWAKENED, CreatureClass.TYRANT)
        .size(4.2F, 3.0F).stats(380.0, 18.0, 22.0, 0.24).follow(40.0)
        .move(Move.melee("fangs", Style.BITE, 18, 11, 3.0, 1.5F, 14))
        .move(Move.of("web", Style.SHOT, 22, 4.0, 18.0, 70, 6, 0.6F, 0.0, 12, 16))
        .move(Move.of("stamp", Style.SLAM, 28, 0.0, 3.6, 60, 5, 1.3F, 3.5, 18))
        .move(Move.of("brood", Style.CALL, 24, 0.0, 30.0, 300, 3, 0.0F, 0.0, 12))
        .afflicts(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3))
        .commands("iron_spider").traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.IRON_NEST, 0, 0)
        .lore().palette(Vfx.Palette.STEEL).egg(0x3A3C44, 0x8A8C98)
        .sounds(() -> SoundEvents.SPIDER_AMBIENT, () -> SoundEvents.IRON_GOLEM_HURT, () -> SoundEvents.SPIDER_DEATH, () -> SoundEvents.IRON_GOLEM_STEP)
        .build();

    /** Literally a mountain of bones, heaving itself along, that throws bone like spears and gathers the dead to it. */
    public static final Kind LORD_OF_THE_DEAD = Kind.of("lord_of_the_dead", CreatureRank.FALLEN, CreatureClass.TYRANT)
        .size(4.5F, 5.5F).stats(600.0, 20.0, 12.0, 0.16).follow(40.0)
        .move(Move.of("crash", Style.SLAM, 32, 0.0, 4.6, 50, 8, 1.4F, 4.0, 22))
        .move(Move.of("spears", Style.SHOT, 26, 4.0, 22.0, 60, 7, 1.0F, 0.0, 12, 16, 20))
        .move(Move.of("grasp", Style.GRAB, 24, 0.0, 4.6, 40, 6, 1.2F, 4.0, 14))
        .traits(Kind.Trait.LIGHT_FALL)
        .lair(Places.BONE_RIDGE, 0, 0)
        .lore().palette(Vfx.Palette.EARTH).egg(0xE0D6C2, 0x3A3028)
        .sounds(() -> SoundEvents.SKELETON_AMBIENT, () -> SoundEvents.SKELETON_HURT, () -> SoundEvents.SKELETON_DEATH, () -> SoundEvents.BONE_BLOCK_STEP)
        .build();

    /** A boulder with a mouth of long, sharp teeth on its face. It rolls at its prey. */
    public static final Kind ROLLING_STONE = Kind.of("rolling_stone", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .size(1.4F, 1.4F).stats(70.0, 10.0, 16.0, 0.28)
        .move(Move.of("roll", Style.CHARGE, 30, 3.0, 14.0, 60, 8, 1.4F, 1.0, 26))
        .move(Move.melee("gnash", Style.BITE, 14, 8, 1.2, 1.2F, 10))
        .traits(Kind.Trait.LIGHT_FALL)
        .haunt(Region.FORGOTTEN_SHORE, 6, 1, 1)
        .lore().palette(Vfx.Palette.EARTH).egg(0x6A6460, 0xE0D8C8)
        .sounds(() -> SoundEvents.GRINDSTONE_USE, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /**
     * Living statues of stone warriors, enormously hard to break, disciplined and skilled with their arms, who fight as
     * one: they overwhelm prey far stronger than they are. They walk the Dark City in small bands.
     */
    public static final Kind STONE_SAINT = Kind.of("stone_saint", CreatureRank.AWAKENED, CreatureClass.MONSTER)
        .size(0.8F, 2.1F).stats(80.0, 10.0, 14.0, 0.27)
        .move(Move.of("cut", Style.STRIKE, 16, 0.0, 2.4, 12, 10, 1.1F, 2.2, 9))
        .move(Move.melee("bash", Style.BITE, 14, 8, 1.6, 0.8F, 30))
        .move(Move.of("thrust", Style.BITE, 18, 1.0, 3.0, 25, 6, 1.4F, 2.8, 11))
        .traits(Kind.Trait.PACK)
        .hauntNear(Region.FORGOTTEN_SHORE, Places.DARK_CITY, 320, 10, 2, 3, Kind.Time.ANY)
        .palette(Vfx.Palette.EARTH).egg(0x8A8680, 0x5A5650)
        .sounds(() -> SoundEvents.STONE_PLACE, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /**
     * The Soul Devouring Tree: a tree like a tower on the Ashen Barrow, bark as black as the Dark Sea, leaves as red as
     * blood, its sweet fruit the bait of its hexes. It makes thralls of those who come near.
     */
    public static final Kind SOUL_DEVOURING_TREE = Kind.of("soul_devouring_tree", CreatureRank.FALLEN, CreatureClass.TERROR)
        .motion(Kind.Motion.ROOT).size(3.0F, 6.0F).stats(700.0, 14.0, 10.0, 0.0).follow(32.0)
        .move(Move.of("lash", Style.SWEEP, 24, 0.0, 6.0, 30, 10, 1.2F, 6.0, 14))
        .move(Move.of("enthrall", Style.SPECIAL, 40, 0.0, 24.0, 120, 6, 0.0F, 0.0, 20))
        .lair(Places.ASHEN_BARROW, 0, 12)
        .factory(SoulTree::new).palette(Vfx.Palette.BLOOD).egg(0x141010, 0xC01818)
        .sounds(() -> SoundEvents.WOOD_PLACE, () -> SoundEvents.WOOD_HIT, () -> SoundEvents.WOOD_BREAK, () -> SoundEvents.WOOD_STEP)
        .build();

    /**
     * The Vile Thieving Bird's spawn, not yet hatched: a huge, smooth egg of stone, its greys drifting over each other
     * like clouds. It is a Great Devil inside, and takes a great deal of breaking.
     */
    public static final Kind VILE_SPAWN = Kind.of("vile_thieving_birds_spawn", CreatureRank.GREAT, CreatureClass.DEVIL)
        .motion(Kind.Motion.ROOT).size(2.0F, 2.6F).stats(900.0, 4.0, 20.0, 0.0)
        .move(Move.of("shudder", Style.SLAM, 20, 0.0, 2.0, 200, 10, 0.5F, 2.0, 10))
        .lair(Places.STONE_NEST, 0, 0)
        .lore().palette(Vfx.Palette.SHADOW).egg(0x8A8A90, 0x4A4A50)
        .sounds(() -> SoundEvents.STONE_PLACE, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.STONE_STEP)
        .build();

    /**
     * A massive four-legged beast with rotten flesh hanging from its bones like a tattered coat, which sobs in the fog
     * like a weeping woman. It prowls the outskirts of the Dark City by night.
     */
    public static final Kind WEEPING_BEAST = Kind.of("weeping_beast", CreatureRank.FALLEN, CreatureClass.BEAST)
        .size(1.6F, 1.8F).stats(90.0, 11.0, 4.0, 0.3)
        .move(Move.melee("maul", Style.STRIKE, 16, 9, 2.0, 1.2F, 12))
        .move(Move.of("lunge", Style.POUNCE, 22, 2.0, 8.0, 50, 6, 1.4F, 1.8, 16))
        .hauntNear(Region.FORGOTTEN_SHORE, Places.DARK_CITY, 600, 8, 1, 1, Kind.Time.NIGHT)
        .palette(Vfx.Palette.BLOOD).egg(0x5A4A40, 0x9A3A3A)
        .sounds(() -> SoundEvents.GHAST_AMBIENT, () -> SoundEvents.GHAST_HURT, () -> SoundEvents.GHAST_DEATH, () -> SoundEvents.WOLF_STEP)
        .build();

    public static final List<Kind> ALL = List.of(CARAPACE_SCAVENGER, CARAPACE_CENTURION, CARAPACE_DEMON, FORSAKEN_KNIGHT, SPIRE_MESSENGER, CURSED_HERALD,
        CRIMSON_TERROR, CRIMSON_GOLEM, BLOOD_FIEND, BLOOD_FLOWER, CORPSE_EATER, FLESH_REAVER, IRON_SPIDER, IRON_SPIDER_MATRIARCH, LORD_OF_THE_DEAD,
        ROLLING_STONE, STONE_SAINT, SOUL_DEVOURING_TREE, VILE_SPAWN, WEEPING_BEAST);

    private ShoreKinds() {
    }
}
