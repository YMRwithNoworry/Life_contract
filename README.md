# 🎭 Life Contract

<div align="center">

### *An Faction Confrontation and Profession System Mod for Minecraft*

![Version](https://img.shields.io/badge/Version-1.0-SNAPSHOT-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)
![Forge](https://img.shields.io/badge/Forge-Recommended-orange)

</div>

---

## 📖 Table of Contents

- [Core Concepts](#-core-concepts)
- [Item System](#-item-system)
- [Profession System](#-profession-system)
- [Minion System](#-minion-system)
- [Blocks & Mechanics](#-blocks--mechanics)
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

### Team Structure

* **Leader**: Holds the contract, determines the faction, and owns the team ID.
* **Member**: Shares the leader's contract and team color; can teleport to teammates.

### Protection Mechanisms

* **Faction Immunity**: Contracted mobs will not attack you.
* **Effect Immunity**: Negative potion effects from contracted mobs are negated.
* **Teammate Protection**: Players cannot damage their own teammates.
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

### Mineral Generator
* **Types**: Iron, Gold, Diamond, Emerald.
* **Control**: Admins can set the generation interval and toggle them globally.

### Shop System
* **Usage**: Trade gold ingots for gear (Diamond swords, Golden Apples, etc.).
* **Team Sentinel**: A stationary 1000 HP Iron Golem that guards the team base.

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

---

## 🖥️ HUD & Interface

The HUD displays the current team ID, active contract mod, current profession, and a list of online teammates.

---

<div align="center">

*Life Contract - Write your legend in faction warfare.*

</div>
