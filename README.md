# Nightmare Evolved

A fan-made **Shadow Slave** mod for Minecraft 1.21.1 on NeoForge. Inspired by the novel by Guiltythree; not affiliated
with the author or the publisher, and free to play.

Every line of code and every texture here was made for this mod. Lore names and concepts follow the novel; all
descriptions and dialogue are original writing.

## What's in it so far

- **Colossi**: four Titan-class Nightmare Creatures many times a player's height, and no two alike. The **Cairn
  Colossus** of piled stone, the skeletal **Ossuary Behemoth**, the burrowing **Gravewyrm** and the armoured
  **Chainbound Colossus**. Each is built from a seed (colouring, size, elders, horns, heads, weapons, segments) and has
  its own attacks with readable wind-ups, ground shockwaves, erupting spikes, a rage, and a slow collapse in death.
- **Effects**: glowing particles tinted per colour palette (motes, streaking sparks, embers, smoke, runes, wave rings,
  halos, rune circles and blade arcs), with shapes such as shockwaves, pillars, domes, cones, spirals and beams.
- **Body animation**: keyframed clips that pose the player model (cuts, spins, roars, slams, guards, raised arms and
  more), seen by everyone nearby, with the first-person hand following along and big moments shaking the view.

## Commands (operators)

| Command | What it does |
| --- | --- |
| `/ne colossus <kind> [seed]` | Raises a colossus ten blocks ahead: `cairn_colossus`, `ossuary_behemoth`, `gravewyrm` or `chainbound_colossus`. The seed it reports raises the same one again. |
| `/ne vfx <shape> <palette>` | Plays an effect in front of you. |
| `/ne pose <clip>` | Plays a body animation on you. |

Spawn eggs are in the Nightmare Evolved creative tab.

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

1. **Foundations** (this): effects, body animation, the code-built creature rig, the colossi.
2. **The core loop**: the Nightmare Spell, the rank ladder, Aspects, soul cores and essence, Nightmares.
3. **The world**: the Dream Realm, its regions and creatures, factions.
4. **Release**: art pass, guide, project pages.
