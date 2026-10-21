# Roadmap: the story-accurate Dream Realm

Everything in Nightmare Evolved follows the web novel *Shadow Slave*: its ranks, Aspects, Flaws, Attributes, regions,
creatures and the road of Ascension. Facts are taken from the novel and its fan wiki (shadowslave.fandom.com,
CC-BY-SA); every description, tooltip and line of dialogue in the mod is original writing. No code or art comes from
other mods. Ideas from other Shadow Slave mods are welcome where the novel leaves gaps.

Each step below lands as its own pull request.

## 3a. Soul, Aspects, Flaws and Attributes

- **The soul as the novel has it.** Every human has one soul core. Its rank rises with each Nightmare, and soul
  shards saturate it. Divine Aspects (Shadow Slave, Light Bringer, Mordret's mirrors) can grow up to seven cores,
  climbing the classes as creatures do, from Beast to Titan. Creatures drop one shard per core they had (their class),
  worth more the higher their rank.
- **Every Aspect in the wiki.** All named Aspects of the novel's Awakened, each at its own Aspect rank, from Dormant to
  Divine. Each has the abilities the novel gives it at each step: Dormant (Sleeper), Awakened, Ascended (Master), the
  Transformation (Saint), and the Supreme Ability and Domain (Sovereign). Where the novel leaves a step unknown, the
  mod fills it in the Aspect's spirit and says so.
- **Every Flaw in the wiki,** each paired with the Aspect it belongs to. Aspects whose Flaw the novel never names
  take one of the Flaws that suits them.
- **Every Attribute in the wiki.** The ones a soul is born with are rolled in the First Nightmare. Others are earned,
  like Dragonslayer, or come from lineage Memories, like The Weave, The Fire and The Blood. Some evolve as a soul
  grows: Mark of Divinity to Flame of Divinity, Child of Shadows to Lord of Shadows.
- **Runes:** a screen showing what the Spell knows of you: Name, Rank, Class, cores and fragments, Aspect and its
  abilities, Flaw, and Attributes.

## 3b. Soul Sea, Memories and Echoes

- **The Soul Sea:** an inner sea under the star of each soul core, entered while the body stays aware.
  - Memories orbit there as lights, and Echoes stand there.
  - Shadow Slave keeps the shadows of the slain.
  - A Saint's claimed Citadel stands at its heart.
  - A Supreme can store items there.
- **Memories:** items of the soul, granted by the Spell for kills, rarer for rarer prey. Each has a rank (the
  creature's) and a tier (its class), and its own name and enchantments.
  - Kinds: weapons, layered armour, charms, tools, garments, consumable lineage Memories, and the Forgotten Shore's
    Shard Memories.
  - Summoning costs essence, and dismissing returns it. They mend themselves in the soul and are lost when their owner
    dies.
- **Echoes:** soulless copies of slain creatures, frozen as they died. They are summoned from the soul, fight with
  their Attributes, and heal over time.

## 3c. The Dream Realm

A dimension of its own, built region by region as the novel describes them:

- **The Forgotten Shore**
  - Black earth and an endless labyrinth of crimson coral under a starless sky.
  - The Dark Sea floods it every night.
  - Landmarks: the Ashen Barrow and its Soul Devouring Tree, the Sea of Ash, Bone Ridge, the walled Dark City (Bright
    Castle, Catacombs, Ruined Cathedral, Library Ruins), and the Crimson Spire with its Gateway.
- **The Chained Isles**
  - Floating islands bound by colossal chains between a Sky Above and a Sky Below. The islands rise, fall and crush,
    and the Tear grows at the centre.
  - The Sanctuary of Noctis and the Night Temple.
  - Altar, Iron Hand, Reckoning and Shipwreck Islands, and the Ivory and Ebony Towers.
- **The Hollow Mountains:** dragon-fang peaks wrapped in the mist of nothingness, hollow with true darkness.
  - The Underground Mine and its black river.
  - The temple-gate entrance to the Underworld, and the three layers below it, down to Nether's City.
- **The Nightmare Desert:** white dunes under a blinding sun where the Deathless rise to war each night, and the black
  pyramid of the Tomb of Ariel.
- **Godgrave:** the skeleton of a dead god under a sky that erases what it sees.
  - Vermilion grass on white bone.
  - The jungle hollows inside the bones, the Sea of Ash, the Spinal Ocean and Vanishing Lake.
- **The Stormsea:** endless storm-wracked water where space loops and only the stars guide, with its island Citadels.
- **Citadels and Bastion's lands:**
  - Bastion on its lake, ringed by the ash of the forest it burned.
  - Ravenheart's black-jade palace and giant bridge between a snowy peak and a volcano.
  - The Burned Forest.
  - The Moonriver Plain.
- **Getting there and back:**
  - A Sleeper's first sleep drops them somewhere random in the Dream Realm. Reaching a Gateway makes them Awakened and
    anchors them there.
  - Seeds of Nightmare grow in the Dream Realm and call to the Awakened. Conquering one is how the Second through
    Sixth Nightmares are reached. A Seed left alone blooms into a Gate.

## 3d. The creatures of the Dream Realm

Every named Nightmare Creature of each region, at its rank and class, with behaviour to match:

- **Forgotten Shore:**
  - Carapace Scavengers, Centurions and the Carapace Demon.
  - The Black Knight, the Cursed Herald, Stone Saints, Iron Spiders and their Matriarch.
  - The Lord of the Dead, Blood Fiends, Corpse Eaters and Flesh Reavers.
  - The Vile Thieving Bird, the Soul Devouring Tree, the Dark Sea's horrors, and the Crimson Terror.
- **Chained Isles:**
  - Black Ravagers, Chain Worms, Chalice Maidens, Dread Wolves and Griffins.
  - The Mirror Beast, Mordant Mimics, Sailor Dolls, Wormvine and the Blood-Nourished Root.
- **Hollow Mountains and the Underworld:** mist beings, corrupted Stone Saints, the Horror of the Lightless Current,
  and the Jade Titan.
- **Nightmare Desert:** the Deathless and Skinwalkers.
- **Godgrave:**
  - The Asuras of Condemnation and Condemnation itself, and Abjuration.
  - The Cloudveil, blood mosquito swarms, harpies, the three-headed hound, and the depth dweller of the Spinal Ocean.
- **Burned Forest:** the Black Millipede Tribe and its time-shifted Millipede Queens.
- **Elsewhere:** Stone Worms, Chalice Knights, and the Stormsea's serpents.

## 3e. Ascension

The parts of the earlier overhaul that were written for it, ported and fitted to the novel:

- **Saint Transformations,** on the code-built creature rig. Each matches the novel: Kai's midnight dragon, Sky Tide's
  thunderbird, Roan's winged lion, Helie's centaur, Silent Stalker's panther, Jest's horned fiend, Revel's bat-winged
  form, Effie's steel giant, Jet's mist wraith, Ki Song's tide of blood, Nephis's winged light, Morgan's living metal,
  and Sunny's Shadow avatars.
- **Movement and command:** flight for Saints, legions for Sovereigns, and the rank gap.
- **Sovereign Domains:** spread through Citadels a Saint has bound, plus each Aspect's own way: swords for Anvil, the
  dead for Ki Song, a branded word for Kai.

## Later

- Factions and the Great Clans (Valor, Song, Night) with reputation.
- Nightmares rebuilt as the novel's own stories.
- An art pass, a guide, and release.
