# 🎭 Life Contract

<div align="center">

### *An Faction Confrontation and Profession System Mod for Minecraft*

![Version](https://img.shields.io/badge/Version-1.0-SNAPSHOT-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)
![NeoForge](https://img.shields.io/badge/NeoForge-Recommended-orange)

</div>

---

## 📖 Table of Contents

- [Core Concepts](#-core-concepts)
- [Item System](#-item-system)
- [Profession System](#-profession-system)
- [Minion System](#-minion-system)
- [Blocks & Mechanics](#-blocks--mechanics)
- [Game Events](#-game-events)
- [Command System](#-command-system)
- [HUD & Interface](#-hud--interface)

---

## 🎯 Core Concepts

### Contract System

The core gameplay of the Life Contract mod revolves around **Contracts** and **Faction Confrontation**:

| Concept | Description |
|:---:|:---|
| 📜 **Life Contract** | Sign a contract with mobs from a specific mod; they will no longer attack you. |
| 👥 **Team System** | Form a team to share contract effects; teammates cannot hurt each other. |
| ⚔️ **Faction Rivalry** | Mobs from different contract factions will automatically attack each other. |
| 💀 **Elimination** | When all team members enter Spectator Mode, that faction's mobs will stop spawning. |

### Lives

* Every participant starts a match with `5` lives (`/contract game start` resets them all).
* **Lethal damage never kills a player who still has a life.** The hit is cancelled, the player is instantly
  restored to full health, gains **Resistance IV for 5 seconds**, and loses one life. Any fire on the player is
  put out at the same time, so nobody is re-killed by the flames that just took them down.
* Damage that bypasses invulnerability - the void and `/kill` - is not intercepted. It kills normally and still
  costs a life.
* At `0` lives the next lethal hit is fatal: the player goes straight into Spectator mode, and respawning never
  brings an eliminated player back.
* The remaining count is shown on the nameplate as `LP: N` and in the bottom-left HUD.
* A teammate revived by the revive system returns in Survival with at least `1` life.

### Team Structure

* **Leader**: Holds the contract, determines the faction, and owns the team ID.
* **Member**: Shares the leader's contract and team color; can teleport to teammates.

### Protection Mechanisms

* **Faction Immunity**: Contracted mobs will not attack you.
* **Effect Immunity**: Negative potion effects from contracted mobs are negated.
* **Teammate Protection**: Players cannot damage their own teammates.
* **Player Health**: Player max health is raised to `40` (vanilla `20`).
* **Endgame Boss**: The End dragon is replaced with Spore's **Verfalldrache** (`spore:verfall` - the registry name, not `spore:verfalldrache`; falling back to the legacy `phayriosis:converted_dragon` when Spore is absent). It gets a purple vanilla boss bar that tracks its health, and is spawned with persistence so it cannot despawn out from under the fight. The team that kills it wins the match, and so does the last team with a surviving player.
* **Gun Damage**: Bullets from TaCZ-style gun mods deal only `20%` of their damage to players; other non-player damage is reduced to `40%`.
* **Aggro Transfer**: Contracted mobs automatically target hostile factions.

---

## 🎒 Item System

### 📜 Life Contract
**Core Item - Establishing Faction Connection**
* **Usage**: Right-click a mob to sign a contract with its parent mod; Shift+Right-click to dissolve the contract.
* **Effect**: Mobs from that mod become friendly and their negative effects won't affect you.

### 🎖️ Team Organizer
**Social Item - Forming Battle Teams**
* **Usage**: Right-click a player to invite; Shift+Right-click to kick them from the team.
* **Benefits**: Shared contracts, disabled friendly fire, and access to teammate teleportation.

### 🪄 Minion Wand
**Capture Tool - Managing Mobs**
* **Usage**: Right-click a mob to capture (max 9); Shift+Right-click to open the storage GUI.
* **GUI**: Displays health, name, and type; allows for summoning or releasing.

### 🥚 Mob Egg
**Summoning Tool - Releasing Captured Mobs**
* **Usage**: Right-click to spawn a stored mob; Shift+Right-click to capture (when egg is empty).
* **Traits**: Spawned mobs follow the owner, attack enemies, and are persistent.

### 🎲 Gambler's Dice
**Class Item - Gambler Exclusive**
* **Usage**: Right-click to trigger a random profession skill (3s cooldown).
* **Skill Pool**: Includes "Poisoner's Strike", "Turtle Shield", "Jungle Curse", "Ender TP", and more.

### 🏹 Instant Kill Bow (Donk Bow)
**Class Item - Donk Exclusive**
* **Traits**: Infinite ammo, Power I, and instant-shot capability.
* **Ability**: Automatically tracks enemies within 50 blocks; 25% chance for a 1.5x crit.

---

## ⚔️ Profession System

### 🟢 Open Professions

* **Poisoner**: +20% Attack Damage.
* **Turtle Guard**: +10 Armor, but suffers from Slowness I and Weakness I.
* **Jungle Ape**: 30% chance to poison enemies; automatically generates jungle logs.
* **Ender Servant**: Infinite Ender Pearls (10s CD), but takes damage in water.
* **Blaze Bringer**: Permanent Fire Resistance; leaves a trail of fire while moving.

### 🔒 Password Professions

* **Faceless** (`faceless123`): Randomly transforms into another profession every 3 minutes.
* **Gacha Master** (`gacha123`): Receives a random mob egg every 15 seconds.
* **Beast Master** (`beast123`): Can mount and control friendly mobs; mounts get stat buffs.
* **Gambler** (`gambler123`): Uses the Dice to trigger random skills from other classes.
* **Lucky Clover** (`lucky123`): Damage dealt/taken is randomized between 1 and 20.
* **Donk** (`donk123`): Uses the Homing "Instant Kill Bow".
* **Deceiver** (`deceiver123`): Gains stats from a contracted mob; **if the mob dies, you are out.**
* **Forgetter** (`forgetter123`): Periodically enters a state of total invisibility to mobs.
* **Gourmet** (`gourmet123`): Eating new food types permanently increases HP and Attack.
* **Angel** (`angel123`): Regenerates 1 HP every 5s; permanent Saturation.

---

## 🐾 Minion System

* **Capture**: Use the Minion Wand on any non-player mob.
* **Behavior**: Mobs can be set to Follow, Guard (defensive), or Attack (aggressive).
* **Attributes**: Minions will not attack their owners and will focus on the owner's targets.

---

## 🧱 Blocks & Mechanics

### Mob Drops
* Every mob death drops `1` Sublimation, and has a `50%` chance to drop `1` Gunpowder on top (player deaths do not trigger it).

### Mineral Generator
* **Types**: Iron, Gold, Diamond, Emerald.
* **Control**: Admins can set the generation interval and toggle them globally.

### Surface Placement

Anything that has to be put on the ground - the game-start spawn platforms, the safe bubbles, elite and
elimination spawns, border respawns - is placed through a single `SurfaceFinder` helper instead of trusting
the chunk heightmap. It force-loads the chunk and then validates the result against real block states, so a
stale or unavailable heightmap can no longer drop a platform into the void or leave it floating in the sky.
`scripts/verify-surface-lookup.cjs` fails the build if a raw heightmap lookup is reintroduced.

### Shop System
* **Usage**: Trade gold ingots for gear (Diamond swords, Golden Apples, etc.).
* **Sublimation Shop**: Spend Sublimation on supplies (flare gun, cooked beef), ammunition, and wool. Ammunition covers both the current contract mod's rounds and every ammo type TaCZ exposes (discovered at runtime, so new gun packs show up automatically; the section is hidden when TaCZ is absent). Pistols are the only guns on sale (the `GunTabType.PISTOL` category, every loaded gun pack included) at 150 Sublimation each. TaCZ gun attachments (scopes, grips, muzzles, magazines, stocks) are sold per piece as well. Wool costs 2 Sublimation per 16 and covers the whole `#minecraft:wool` tag, modded wool included.
* **Team Sentinel**: A stationary 1000 HP Iron Golem that guards the team base.

### Accessories

70 accessory / consumable / material items ship with the mod, all driven by
`data/life_contract/accessory_catalog.json` (item registration, effects, shop prices and recipes read that one file).

* **Categories**: pendant (10), ring (10), charm (12), crown (11), amulet (4), consumable (3), material (20).
* **Rule**: only the highest tier accessory of each category takes effect, so at most five bonuses are active at once.
* **Equipping**: accessories only work while worn in a **Curios** slot; carrying them in the inventory does nothing. Pendants and amulets go into `necklace`, rings into `ring`, charms into `charm` and crowns into `head` (item tags ship under `data/curios/tags/item/`). Because Curios grants no `head` slot by default, the mod ships `data/life_contract/curios/entities/player.json` to add one for players without replacing the other slots. Without Curios the items cannot be equipped at all, so they grant nothing and the server logs a warning at startup. Curios is still reached purely by reflection, so the mod itself loads without it.
* **Conditional effects**: every effect may carry a `when` clause and only applies while that situation holds — HP thresholds, day/night, underground, low light, in water or rain, on fire, sneaking, sprinting, hungry, recently killed, team lives <= 2, last one standing, outnumbered, near the border, Nether/End. 56 of the shipped effects are conditional, so accessories are picked for the situation, not for raw stats.
* **Faction resonance**: every wearable belongs to a faction (crimson / azure / amethyst / obsidian / verdant / gilded). Two active pieces of the same faction grant Resonance I, three grant Resonance II, stacking on top of the per-item effects — which turns "take the best tier in every slot" into a real trade-off. The catalog for it lives in `data/life_contract/accessory_resonance.json`.
* **Kill charge**: four items build a stack on killing a hostile mob or player (one per kill, capped), each stack adding a bonus. Stacks are lost when hurt, decay over time and reset on death, and every change is announced on the action bar.
* **Active skills**: eleven tier 4-5 items carry an active skill fired with **G**. The highest-tier equipped skill that is off cooldown fires; cooldowns are tracked per item and survive re-equipping. Kinds: Burst, Ward, Mend, Dash, Phase, Purge, Mark, Rally.
* **Drawbacks**: tier 5 items pair their payoff with a real penalty (negative armour, max health or movement speed) shown in red on the tooltip.
* **Inspecting**: hovering an accessory name in the shop shows the full mechanics in a tooltip; **left-clicking**
  it pins the same breakdown into a panel on the right so several pieces can be compared. The pinned card
  refreshes twice a second, so your current faction resonance tier and the item's live kill-charge stacks are
  shown as they change. Both views are built by the same `AccessoryTooltip` code that renders the inventory
  item tooltip, so the three never drift apart.
* **Obtaining**: buy them with Sublimation in the shop (tier 1-5 = 60/120/200/320/500, consumables 20-90, materials 8-35) or craft them; every item has a crafting recipe under `data/life_contract/recipe/accessory_*.json`, and materials are crafted from vanilla items.

---
## 🎲 Game Events

Events run automatically alongside a match and pause together with it. The event HUD in the top-right corner lists active events, remaining time, and safe-bubble coordinates.

| Event | Trigger | Effect |
|:---:|:---|:---|
| **Spore Surge** | Minute 5 | Spawns `12–23` infection-mod elites at random spots inside the border for `45` seconds (falls back to buffed vanilla elites when Fungal Infection: Spore is absent). |
| **Scavenger Bounty** | Every `2` eliminations | The highest K/D player is marked with a glowing outline; whoever kills them permanently gains their max health plus `+2`. |
| **Purification Rift** | Minute 9 | Spawns `3` safe bubbles of `15` blocks radius for `60` seconds; players inside keep Regeneration II and Saturation III. |
| **Endgame Overload** | `3` players left | Every player's infection is forced to level `2` and world-border damage is increased by `100%`. |
| **Spore Rain** | Random | Lasts `60` seconds; players under open sky build up infection and only recover in shelter. A yellow tint covers the screen while exposed. |

* Random draws trigger with a `30%` chance between the fixed schedule points.
* Safe bubbles render as coloured spheres in the world, visible from far away.

---

## 💻 Command System

### Player Commands
* `/contract hud`: Toggle the contract information overlay.
* `/contract highlight`: Toggle glowing outlines for teammates.
* `/contract team tp <player>`: Teleport to a teammate.

### Admin Commands (OP Level 2)
* `/contract team split <count>`: Automatically divide players into teams.
* `/contract spawn_shop`: Spawn the shop villager.
* `/contract toggle_mineral <on|off>`: Global switch for all mineral generators.
* `/contract event trigger <event>`: Force-trigger a game event (`spore_surge`, `bounty`, `purification_rift`, `endgame_overload`, `spore_rain`).
* `/contract event stop <event>`: Stop a running game event.
* `/contract perf`: Print server TPS, entity spawn/despawn rates, per-dimension entity counts, the composition of loaded entities (total / never-despawn / tagged by this mod), and the mod's internal index sizes (run twice to get a sample).
* `/contract perf on|off`: Toggle per-section timing of the mod's hot paths (off by default and zero-cost while off). While on, `/contract perf` also ranks entity types by net growth, which points at whatever is piling up. `/contract perf reset` clears the counters.
* `/contract perf watchdog on|off`: The lag watchdog (on by default) writes the same diagnosis into the server log when TPS stays under 15 for three seconds, at most once every five minutes, and switches section timing on so later reports include per-section cost.

---

## 🖥️ HUD & Interface

Every ModularUI panel (sublimation shop, mutation tree, upgrade hub, team inventory) declares a compact
design size and then clamps itself to the actual screen through `UiLayout.fitToScreen`. GUI scale is picked
automatically from the physical resolution - 1920x1080 lands on scale 4, i.e. a logical 480x270 - so a panel
larger than that would simply run off the screen. The sublimation shop keeps the product list and the
accessory detail card in the same slot and swaps between them on left-click, which is what keeps it down to
320 pixels wide. `scripts/verify-ui-fit.cjs` fails the build if a panel grows past the safe bound or stops
clamping itself.

The HUD displays the current team ID, active contract mod, current profession, and a list of online teammates. During a match it also lists the key waypoints: the End portal coordinates (with live distance and opened/closed state) and the border centre.

The bottom-left corner shows the local player's remaining lives: a row of hearts (filled for lives left, dark for lives lost) next to the exact number. The number turns red at one life, and the row reads "Eliminated" once the player is out. It stays hidden while no lives are synced, i.e. outside a match.

When you are outside the predicted safe zone, an arrow appears at the bottom of the screen. It rotates with your view and always points to the direction you should walk, next to the remaining distance and the coarse compass direction.

---

<div align="center">

*Life Contract - Write your legend in faction warfare.*

</div>
