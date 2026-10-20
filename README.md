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
- **Soul cores and shards**: slain Nightmare Creatures leave soul shards. Absorb them to fill your cores; a Sleeper or
  higher must fill every core before the next trial will take them. In the waking world, some monsters are touched by
  the Spell and come back ranked, stronger the farther they are from spawn.
- **Aspects**: after the First Nightmare the Spell grants one of five Aspects, born Dormant, Awakened, Ascended or
  (rarely) Transcendent. Each has two abilities (Z and X by default) that wake at Sleeper and Awakened, cost essence
  and rest between uses, and a Flaw it cannot shake:

  | Aspect | Abilities | Flaw |
  | --- | --- | --- |
  | Hollow Shade | Shade Step, Umbral Grasp | Light-Shy |
  | Ember Herald | Cinder Lash, Dawn Pyre | Doused |
  | Iron Penitent | Penitent's Guard, Shackle Break | Leaden |
  | Tide Weaver | Undertow, Riptide Lance | Parched |
  | Rune Scribe | Warding Sigil, Glyph Volley | Brittle |
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
Aspect, essence, soul cores and what stands between you and the next trial.

For operators:

| Command | What it does |
| --- | --- |
| `/ne soul rank <rank>` | Sets your rank. |
| `/ne soul aspect <aspect> [tier]` | Grants you an Aspect. |
| `/ne soul saturate` / `essence` / `reset` | Fills your cores; fills your essence and readies every ability; makes you an Aspirant again. |
| `/ne trial begin <trial>` / `win` | Enters any trial at once; wins the trial you are in. |
| `/ne colossus <kind> [seed]` | Raises a colossus ten blocks ahead: `cairn_colossus`, `ossuary_behemoth`, `gravewyrm` or `chainbound_colossus`. The seed it reports raises the same one again. |
| `/ne vfx <shape> <palette>` | Plays an effect in front of you. |
| `/ne pose <clip>` | Plays a body animation on you. |

Spawn eggs and soul shards are in the Nightmare Evolved creative tab.

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
2. **The core loop** (this): the Nightmare Spell, the rank ladder, Aspects, soul cores and essence, Nightmares.
3. **The world**: the Dream Realm, its regions and creatures, factions.
4. **Release**: art pass, guide, project pages.
