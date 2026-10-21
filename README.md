# Nightmare Evolved

A fan-made **Shadow Slave** mod for Minecraft 1.21.1 on NeoForge. Inspired by the novel by Guiltythree; not affiliated
with the author or the publisher, and free to play.

Every line of code and every texture here was made for this mod. Lore names and concepts follow the novel; all
descriptions and dialogue are original writing.

## What's in it so far

- **The Nightmare Spell**: it finds every player as an Aspirant and speaks to them in its own voice. Sleep in a bed
  (or answer it with `/spell accept`) to be taken into your next trial.
- **The rank ladder**: Aspirant, Sleeper, Awakened, Master, Saint, Sovereign, Sacred and Divine. Each rank raises
  health, strength, armour and speed, and holds more essence. A rank above your foe softens its blows on you; a rank
  below makes them land harder.
- **Nightmares**: seven trials in a dimension of their own, each on a freshly built floating island. Survive waves of
  Nightmare Creatures and destroy the Nightmare's master (from the Fourth Nightmare on, a colossus) to climb a rank.
  The Awakening is a road to a Gateway instead. Dying in a trial loses nothing you carry but shakes the soul for a
  while, and costs half of what your cores held.
- **The soul, as the novel has it**: every Awakened has one soul core holding a thousand soul fragments; slain
  creatures leave one shard for each core they had (one for a Beast, seven for a Titan), worth more the higher their
  Rank and far less from creatures below you. A fuller core makes the body stronger and holds more essence. Only the
  Divine Aspects grow more cores, up to seven, climbing the classes from Beast to Titan. Press **R** to read your runes.
- **Every Aspect in the novel**: 34 Aspects of its Awakened, from Sunny's Shadow Slave, Nephis's Light Bringer and
  Mordret's mirrors (the three Divine Aspects) to Cassie's, Jet's, Effie's, Kai's, Morgan's, Anvil's, Ki Song's and
  her daughters', the Saints of Valor, and the Irregulars of Antarctica. Each wakes its abilities as you rise: the
  Dormant Ability (**Z**), Awakened (**X**), Ascended (**C**), the Saint's Transformation (**V**) and the Supreme
  Ability (**B**). Some are pressed, some held, some always with you.
- **Every Flaw in the novel**: each of the 25 comes with the Aspect it belongs to, and does what it does in the story:
  Sunny cannot leave a question unanswered, Nephis burns when she uses her power, Cassie is blind, Rain cannot kill,
  Jet's dead core feeds only on killing. Aspects whose Flaw the novel never names roll one that suits them.
- **Every Attribute in the novel**: 50 of them. A soul is born with one to three (or with its Aspect's own, as Sunny
  was born Fated, with the Mark of Divinity and Child of Shadows); some grow as you rise, the Mark of Divinity into a
  Flame, Child of Shadows into Lord of Shadows. Others come from lineages, relics, Echoes, or deeds such as slaying a
  dragon.

  The full list, with every ability, is in [docs/ASPECTS.md](docs/ASPECTS.md).
- **Memories**: the novel's own, 55 of them, from the seven Shard Memories of the Forgotten Shore (Midnight Shard,
  Zenith, Dawn, Dusk, Moonlight, Sunlight and Starlight) to Puppeteer's Shroud, Dark Wing, Cruel Sight, the Covetous
  Coffer, Sin of Solace, the Undying Chain, the Crown of Twilight, the Weaver's Mask and the Shadow Lantern, each with
  the Rank, Tier and enchantments it has in the novel. Other prey leaves Memories the Spell weaves for it, named for the
  creature and rolled for their Tier. Summon one with essence and dismiss it to get the essence back; it mends while it
  rests in the soul. Memories die with their owner, as in the novel, all but the Shard Memories (the `keepSoulMemories`
  game rule keeps them), and seven Shards together make the Oath Keys. Press **J** to tend them, **G** to dismiss the
  one in your hand.
- **Echoes and Shadows**: frozen copies of slain creatures that fight for you, heal at rest, and die for good. Sunny can
  make an Echo into a Shadow when its kind stands among the shadows of the slain in his Soul Sea.
- **The Soul Sea**: press **K** to meditate and sink into your own sea, while your body sits where you left it (strike
  it, and you are torn back). Your cores burn over the water as suns, your Memories circle as lights, your Echoes stand
  in a ring. Each sea is its owner's: Sunny's is black and silent, Nephis's gold, Mordret's lost in mist. A Saint's bound
  Citadel stands at its heart, and a Sovereign's sea keeps things for them.

  Every Memory, enchantment and Echo Attribute is in [docs/MEMORIES.md](docs/MEMORIES.md).
- **Colossi**: four Titan-class Nightmare Creatures many times a player's height, and no two alike. The **Cairn
  Colossus** of piled stone, the skeletal **Ossuary Behemoth**, the burrowing **Gravewyrm** and the armoured
  **Chainbound Colossus**. Each is built from a seed (colouring, size, elders, horns, heads, weapons, segments) and has
  its own attacks with readable wind-ups, ground shockwaves, erupting spikes, a rage, and a slow collapse in death.
- **Effects**: glowing particles tinted per colour palette (motes, streaking sparks, embers, smoke, runes, wave rings,
  halos, rune circles and blade arcs), with shapes such as shockwaves, pillars, domes, cones, spirals and beams.
- **Body animation**: keyframed clips that pose the player model (cuts, spins, roars, slams, guards, raised arms and
  more), seen by everyone nearby, with the first-person hand following along and big moments shaking the view.

## Commands

`/spell accept` answers the Spell and enters your next trial when you are ready; `/spell status` shows your rank,
essence, soul cores and what stands between you and the next trial; `/spell runes` recites your runes in chat.

For operators:

| Command | What it does |
| --- | --- |
| `/ne soul rank <rank>` | Sets your rank. |
| `/ne soul aspect <aspect> [flaw]` | Grants you an Aspect, with its own Flaw or the one named. |
| `/ne soul flaw <flaw>` / `attribute add\|remove <attribute>` | Sets your Flaw; gives or takes an Attribute. |
| `/ne soul cores <n>` / `fragments <n>` | Sets your soul cores (up to seven) or fragments. |
| `/ne soul saturate` / `essence` / `reset` | Fills your cores; fills your essence and readies every ability; makes you an Aspirant again. |
| `/ne trial begin <trial>` / `win` | Enters any trial at once; wins the trial you are in. |
| `/ne memory give <memory> [rank] [tier]` | Puts one of the novel's Memories in your soul. |
| `/ne memory weave <shape> <family> <rank> <tier>` | Has the Spell weave a Memory, as for a creature of that kind. |
| `/ne memory summon <memory>\|all` / `dismiss` / `list` / `clear` / `shards` | Summons, dismisses, lists or clears your Memories; gives the seven Shards. |
| `/ne echo give <creature> [rank class]` / `summon` / `list` / `clear` | Freezes a creature of that kind into an Echo; calls out, lists or clears your Echoes. |
| `/ne sea enter\|leave` | Goes down into your Soul Sea at once, or comes back. |
| `/ne sea citadel <citadel>\|none` / `shadow <creature>` | Binds a Citadel to your soul; stands the shadow of a slain creature in your sea. |
| `/ne colossus <kind> [seed]` | Raises a colossus ten blocks ahead: `cairn_colossus`, `ossuary_behemoth`, `gravewyrm` or `chainbound_colossus`. The seed it reports raises the same one again. |
| `/ne vfx <shape> <palette>` | Plays an effect in front of you. |
| `/ne pose <clip>` | Plays a body animation on you. |

Spawn eggs, soul shards and the novel's Memories are in the Nightmare Evolved creative tab.

## Building

Needs JDK 21.

```bash
./gradlew build
```

```bash
./gradlew runClient
```

```bash
./gradlew runGameTestServer
```

## Roadmap

1. **Foundations**: effects, body animation, the code-built creature rig, the colossi.
2. **The core loop**: the Nightmare Spell, the rank ladder, soul cores and essence, Nightmares.
3. **The novel's own** (this, under way; see [docs/ROADMAP.md](docs/ROADMAP.md)): its Aspects, Flaws and Attributes;
   the Soul Sea, Memories and Echoes; the Dream Realm's regions and creatures; and Ascension.
4. **Release**: factions and the Great Clans, the Nightmares as the novel tells them, an art pass, a guide, and
   project pages.
