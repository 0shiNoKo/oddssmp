# OddsSMP Plugin - Complete Documentation

> **Version:** 1.0.0 | **API:** Paper 1.21 | **Soft Dependency:** DecentHolograms

## Table of Contents
1. [All 17 Attributes](#1-all-17-attributes)
2. [All 17 Weapons](#2-all-17-weapons)
3. [Weapon Crafting Costs](#3-weapon-crafting-costs)
4. [Weapon Altar System](#4-weapon-altar-system)
5. [Boss Items & Mechanics](#5-boss-items--mechanics)
6. [Ascended Warden Boss](#6-ascended-warden-boss)
7. [Leveling System](#7-leveling-system)
8. [Upgrader & Reroller](#8-upgrader--reroller)
9. [All Commands](#9-all-commands)
10. [All GUIs](#10-all-guis)
11. [All Configuration](#11-all-configuration)
12. [Combat Logger](#12-combat-logger)
13. [Particle Effects](#13-particle-effects)
14. [Data Persistence](#14-data-persistence)
15. [Plugin Lifecycle](#15-plugin-lifecycle)

---

## 1. All 17 Attributes

### Attribute Table
| # | Type | Icon | Display Name | Boss? | Dragon Egg? |
|---|------|------|-------------|-------|-------------|
| 1 | MELEE | ⚔ | Melee | No | No |
| 2 | HEALTH | ❤ | Health | No | No |
| 3 | DEFENSE | 🛡 | Defense | No | No |
| 4 | WEALTH | 💰 | Wealth | No | No |
| 5 | SPEED | ⚡ | Speed | No | No |
| 6 | CONTROL | 🕹 | Control | No | No |
| 7 | RANGE | 🏹 | Range | No | No |
| 8 | PRESSURE | 🩸 | Pressure | No | No |
| 9 | TEMPO | ⏱ | Tempo | No | No |
| 10 | DISRUPTION | 🧠 | Disruption | No | No |
| 11 | VISION | 👁 | Vision | No | No |
| 12 | TRANSFER | 🔁 | Transfer | No | No |
| 13 | RISK | 🎲 | Risk | No | No |
| 14 | WITHER | 💀 | Wither | Yes | No |
| 15 | WARDEN | 🐋 | Warden | Yes | No |
| 16 | BREEZE | 🌬 | Breeze | Yes | No |
| 17 | DRAGON_EGG | 🥚 | Dragon Egg | No | Yes |

**Random Assignment Pool:** 12 standard attributes (MELEE through RISK). Boss attributes and Dragon Egg are excluded.

---

### Standard Attributes (13)

#### MELEE (⚔)
| Ability | Type | Details |
|---------|------|---------|
| Power Strike | Melee | Ignores 25% armor (+1%/level, max 30% at L5). **CD: 120s** |
| Battle Fervor | Support | +15% melee damage for 6s (+1s/level, max 11s). **CD: 150s** |
| Bloodlust | Passive | +1.5% melee damage per PvP kill. Max: 10% +1%/level (L5=15%). **Lose all stacks on death.** |

#### HEALTH (❤)
| Ability | Type | Details |
|---------|------|---------|
| Vampiric Hit | Melee | Heals 15% of damage dealt (+1%/level, max 20%) for 5s. Overheal converts to absorption. **CD: 120s** |
| Fortify | Support | Heal 3 hearts (+0.5/level, max 5.5 hearts). **CD: 120s** |
| Vitality | Passive | +1 max heart per PvP kill (max = level hearts, L5=5 hearts). Lose 1 heart per level on death. |

#### DEFENSE (🛡)
| Ability | Type | Details |
|---------|------|---------|
| Iron Response | Melee | 20% damage reduction for 4s (+0.5s/level, max 6s). **CD: 120s** |
| Shield Wall | Support | Gain 4 absorption hearts (+0.5/level, max 6.5) for 8s. **CD: 120s** |
| Hardened | Passive | Armor breaks 5% slower (+1%/level). Random chance to negate durability damage. |

#### WEALTH (💰)
| Ability | Type | Details |
|---------|------|---------|
| Plunder Kill | Melee | Disables target's held item for 7s (+1s/level, max 12s). **CD: 120s** |
| Economic Surge | Support | 100% villager discount + Fortune VII for 20s (+2s/level, max 30s). **CD: 120s** |
| Industrialist | Passive | Permanent Hero of the Village XII. Mob drops +10%/level. Mining +1 Fortune/level. |

#### SPEED (⚡)
| Ability | Type | Details |
|---------|------|---------|
| Flash Step | Melee | Summons lightning on opponent. **CD: 120s** |
| Rapid Formation | Support | Speed III for 6s. **CD: 120s** |
| Adrenaline | Passive | Double-jump boost in look direction. Velocity: direction * 1.5, Y + 0.8. Cooldown: 20s (-1s/level). |

#### CONTROL (🕹)
| Ability | Type | Details |
|---------|------|---------|
| Disrupt | Melee | Slowness III for 3s (+1s/level, max 8s). **CD: 120s** |
| Lockdown | Support | 6-block radius: enemies can't use abilities AND take **+25% damage from all sources**. 5s (+1s/level, max 10s). **CD: 120s** |
| Suppression | Passive | Players you hit get +10s cooldown (+1s/level, max 15s). Once per 30s. |

#### RANGE (🏹)
| Ability | Type | Details |
|---------|------|---------|
| Spacing Strike | Melee | Knockback target, they can't approach you for 3s (+1s/level, max 8s). **CD: 120s** |
| Zone Control | Support | Gain homing arrows (30% steering, 20-block detection, ticks every 2 ticks for 100 ticks max) for 5s (+1s/level). **CD: 120s** |
| Footwork | Passive | Bows/crossbows deal +20% damage (+1%/level, max 25%). |

#### TEMPO (⏱)
| Ability | Type | Details |
|---------|------|---------|
| Tempo Strike | Melee | Stun target (Slowness 255 + Blindness + Mining Fatigue 255 + movement blocked + attacks blocked) for 5s (+1s/level). **CD: 120s** |
| Overdrive | Support | Haste V for **15s** (flat, does not scale with level). **CD: 120s** |
| Momentum | Passive | Permanent Speed I. |

#### VISION (👁)
| Ability | Type | Details |
|---------|------|---------|
| Target Mark | Melee | Apply Glowing for 300s (+30s/level, max 450s = 7.5m). **CD: 120s** |
| True Sight | Support | Opens **Player Tracking GUI** to select a target. Tracks until tracker is within **5 chunks (80 blocks)** of target. Shows action bar with distance, compass direction (N/NE/E/SE/S/SW/W/NW), and applies Glowing to target. **CD: 120s** |
| Awareness | Passive | Players within 12 blocks glow (visible only to you). |

#### TRANSFER (🔁)
| Ability | Type | Details |
|---------|------|---------|
| Effect Swap | Melee | **Sacrifice one of YOUR positive effects** to inflict a corrupted negative on the target for **1 minute**. See conversion table below. **CD: 120s** |
| Redirection | Support | Reflect durability damage to attacker for 5s (+1s/level, max 10s). **CD: 120s** |
| Cleanse | Passive | Immune to all debuffs (Slowness, Mining Fatigue, Damage, Nausea, Blindness, Hunger, Weakness, Poison, Wither, Levitation, Unluck, Darkness). |

**Effect Swap Conversion Table:**
| Your Effect | Enemy Gets |
|------------|------------|
| Speed | Slowness |
| Haste | Mining Fatigue |
| Strength | Weakness |
| Regeneration | Poison |
| Resistance | Wither |
| Jump Boost | Slowness |
| Fire Resistance | Poison |
| Night Vision | Blindness |
| Invisibility | Glowing |
| Health Boost | Wither |
| Absorption | Weakness |
| Saturation | Hunger |
| Slow Falling | Levitation |
| Luck | Unluck |
| Dolphins Grace | Slowness |
| Conduit Power | Mining Fatigue |
| (any other) | Poison |

#### PRESSURE (🩸)
| Ability | Type | Details |
|---------|------|---------|
| Crushing Blow | Melee | +25% bonus damage, target takes +15% damage for 4s (+1s/level, max 9s). **CD: 120s** |
| Intimidation Field | Support | 6-block radius: enemies deal -15% damage (+5%/level, max -35%), take +10% damage (+5%/level, max +30%) for 6s. **CD: 120s** |
| Oppression | Passive | Enemies below 50% HP near you take +10% damage (+3%/level, max 25%). |

#### DISRUPTION (🧠)
| Ability | Type | Details |
|---------|------|---------|
| Fracture | Melee | +20s to all cooldowns, Weakness II + Blindness + Nausea for 4s (+1s/level, max 9s). **CD: 120s** |
| System Jam | Support | 6-block radius: lock abilities for 25s (+1s/level, max 30s). **Deals damage = 20% of enemy's total remaining cooldown time** (e.g. 100s remaining = 20 damage). **CD: 150s** |
| Desync | Passive | First hit per fight: +10s to both enemy cooldowns (+2s/level, max 20s). Resets every 30s. |

#### RISK (🎲)
| Ability | Type | Details |
|---------|------|---------|
| All In | Melee | +50% damage (+10%/level), you take +25% damage (-2%/level) for 5s. **CD: 120s** |
| Double Or Nothing | Support | +30% damage (+5%/level, max 55%), take +20% damage for 6s. **CD: 150s** |
| Gambler's Edge | Passive | Below 40% HP: +10% damage (+1%/level, max 15%). Disabled above 40% HP. |

---

### Boss Attributes (4)

#### WITHER (💀)
| Ability | Type | Details |
|---------|------|---------|
| Desperation Cleave | Melee | 12 damage (+1.2/level). Damage multiplied by HP: 2.0x at <20% HP, 1.6x at <40%, 1.3x at <60%, 1.2x at <80%, 1.0x at 80%+. 20% armor pen (+5%/level). Wither II for 4s (+0.5s/level). Enemy healing -40% (+5%/level). **CD: 240s** |
| Shadow Pulse | Support | 6-block AoE: 10 damage (5 hearts) + Slowness II for 3s. **CD: 240s** |
| Curse of Despair | Passive | Healing received -25% (+1%/level). Melee cooldown +10s (-1s/level). |

#### WARDEN (🐋)
| Ability | Type | Details |
|---------|------|---------|
| Sonic Slam | Melee | 14 damage (7 hearts), 5-block AoE, Slowness II + Mining Fatigue II for 3s. **CD: 240s** |
| Deep Dark Zone | Support | Radius: 12 blocks (+0.8/level, max 16). Duration: 8s (+1s/level, max 12). Enemies: no sprint, -50% jump, -30% attack speed, -50% healing. You: +15% melee damage. **CD: 180s** |
| Curse of Silence | Passive | -15% attack speed when idle. |

#### BREEZE (🌬)
| Ability | Type | Details |
|---------|------|---------|
| Judging Strike | Melee | Target takes +12% damage (+2%/level) for 10s. If target misses: +6 true damage (+1/level). **CD: 240s** |
| Trial Order | Support | -20% cooldowns, +10% damage reduction for 6s (+1s/level). **CD: 240s** |
| Curse of Judgment | Passive | Cooldowns increase out of combat (+1s/10 ticks idle). Healing -25%. Move speed -5% when idle (Slowness I applied after 3s without attacking). |

#### DRAGON EGG (🥚)
| Ability | Type | Details |
|---------|------|---------|
| Rampaging Strike | Melee | 10 damage (+2/level, max 18). 1.5x below 30% HP. 20% lifesteal (+5%/level, max 40%). Slowness III + Weakness II for 3s (+0.5s/level). **CD: 300s** |
| Dominion | Support | **REQUIRES 5 CONSECUTIVE HITS TO CHARGE** (within 3s of each other, resets if gap > 3s). +25% damage (+1%/level, max 30%). +50% cooldown reduction (+1%/level, max 55%). Duration: 8s. If not charged, cooldown is refunded. **CD: 300s** |
| Draconic Curse | Passive | Nearby enemies (8 blocks) take +15% damage (-1%/level, min 10%). Always active. |

**Dragon Egg Special Effects:**
- Permanent Glowing potion effect
- Dark Purple team color on scoreboard
- Server-wide broadcast on acquisition
- Cannot be dropped or stored in containers

---

## 2. All 17 Weapons

### Standard Weapons (13)
**All: 15 damage, 1.6 attack speed, 300 durability, Unenchantable, Unrepairable**

| # | Weapon | Attribute | Material | On-Hit Effect | Passive Bonus |
|---|--------|-----------|----------|---------------|---------------|
| 1 | Breaker Blade | Melee | Netherite Sword | -40% armor for ability hit | +6% final melee damage |
| 2 | Crimson Fang | Health | Netherite Sword | 10% lifesteal | +2 max HP while held |
| 3 | Bulwark Mace | Defense | Mace | 10% dmg reduction 3s | -10% knockback taken |
| 4 | Gilded Cleaver | Wealth | Netherite Axe | Mob kill: +50% drops | Looting +2 |
| 5 | Lockspike | Control | Iron Sword | Slowness I 2s | +10% ability duration |
| 6 | Windcaller Pike | Range | Trident | Reach: +1.5 blocks | +25% knockback |
| 7 | Chrono Saber | Tempo | Golden Sword | +5s cooldown to target | -5% own cooldowns |
| 8 | Watcher's Blade | Vision | Netherite Sword | Mark target 5s | Glow enemies 6 blocks |
| 9 | Mirror Edge | Transfer | Diamond Sword | Steal positive effects 4s | -10% debuff duration |
| 10 | Flashsteel Dagger | Speed | Iron Sword | +5% move speed 3s | +10% sprint speed |
| 11 | Bonecrusher | Pressure | Mace | +10% damage taken 4s | +10% damage vs low HP |
| 12 | Fracture Rod | Disruption | Blaze Rod | +10s all cooldowns | +10% debuff duration |
| 13 | High Roller Blade | Risk | Golden Sword | +30% dealt, +15% taken | +10% crit below 40% HP |

### Boss Weapons (4)
**All: 16 damage, 1.6 attack speed, 300 durability, Unenchantable, Unrepairable**

| # | Weapon | Attribute | Material | On-Hit Effect | Passive Bonus |
|---|--------|-----------|----------|---------------|---------------|
| 14 | Despair Reaver | Wither | Netherite Hoe | +1% dmg per 1% missing HP | Healing -20% |
| 15 | Deepcore Maul | Warden | Mace | +20% damage in zones | -20% knockback taken |
| 16 | Verdict Lance | Breeze | Trident | 3 true damage | +10% vs cooldown users |
| 17 | Dominion Blade | Dragon Egg | Netherite Sword | Allies +10% damage | Lifesteal 10% |

**Weapon Item Lore Format:**
```
[Color]§l[WeaponName]
§7Attribute Weapon: [Color][AttributeName]

§7Base Damage: §c[damage]
§7Attack Speed: §e[speed]
§7Durability: §a[durability]

§6§lOn Hit:
§7[onHitEffect]

§d§lPassive:
§7[passiveBonus]

§c§lRequires: [Color][AttributeName] §c§lattribute

§8§oUnenchantable • Unrepairable
```

---

## 3. Weapon Crafting Costs

### Standard Weapons

**Breaker Blade (Melee)**
- 4x Netherite Ingot, 8x Diamond Block, 32x Iron Block, 1x Weapon Handle

**Crimson Fang (Health)**
- 4x Netherite Ingot, 8x Diamond Block, 16x Golden Apple, 32x Redstone Block, 1x Weapon Handle

**Bulwark Mace (Defense)**
- 6x Netherite Ingot, 12x Diamond Block, 64x Iron Block, 32x Obsidian, 1x Weapon Handle

**Gilded Cleaver (Wealth)**
- 4x Netherite Ingot, 64x Gold Block, 32x Emerald Block, 8x Diamond Block, 1x Weapon Handle

**Lockspike (Control)**
- 32x Iron Block, 4x Diamond Block, 64x Chain, 32x Cobweb, 1x Weapon Handle

**Windcaller Pike (Range)**
- 4x Netherite Ingot, 8x Diamond Block, 64x Prismarine Shard, 64x Feather, 1x Weapon Handle

**Chrono Saber (Tempo)**
- 4x Netherite Ingot, 32x Gold Block, 16x Clock, 8x Diamond Block, 1x Weapon Handle

**Watcher's Blade (Vision)**
- 4x Netherite Ingot, 8x Diamond Block, 16x Ender Eye, 32x Glowstone, 1x Weapon Handle

**Mirror Edge (Transfer)**
- 16x Diamond Block, 32x Amethyst Block, 64x Glass, 32x Prismarine Crystals, 1x Weapon Handle

**Flashsteel Dagger (Speed)**
- 32x Iron Block, 4x Diamond Block, 64x Sugar, 32x Feather, 1x Weapon Handle

**Bonecrusher (Pressure)**
- 6x Netherite Ingot, 12x Diamond Block, 64x Bone Block, 6x Skeleton Skull, 1x Weapon Handle

**Fracture Rod (Disruption)**
- 4x Netherite Ingot, 8x Diamond Block, 32x Blaze Rod, 4x End Crystal, 1x Weapon Handle

**High Roller Blade (Risk)**
- 64x Gold Block, 8x Diamond Block, 16x Emerald Block, 32x Lapis Block, 1x Weapon Handle

### Boss Weapons

**Despair Reaver (Wither)**
- 4x Netherite Block, 32x Diamond Block, 6x Wither Skeleton Skull, 64x Soul Sand, 1x Nether Star, 1x Weapon Handle, 1x Wither Bone

**Deepcore Maul (Warden)**
- 64x Bone Block, 64x Iron Block, 64x Copper Block, 6x Skeleton Skull, 6x Wither Skeleton Skull, 3x Player Head, 1x Weapon Handle, 1x Warden's Heart

**Verdict Lance (Breeze)**
- 2x Netherite Block, 24x Diamond Block, 64x Prismarine Shard, 64x Feather, 32x Phantom Membrane, 1x Weapon Handle, 1x Breeze Heart

**Dominion Blade (Dragon Egg)**
- 8x Netherite Block, 64x Diamond Block, 8x End Crystal, 32x Ender Eye, 64x Dragon Breath, 1x Weapon Handle, 1x Dragon Heart

---

## 4. Weapon Altar System

### Spawning
- Command: `/admin spwe <weapon_name>` (spawns at player location)
- Stored in `activeAltars` list, cleaned up on plugin disable

### Pedestal Structure
```
Layer Y-1: ANCIENT_DEBRIS (center support)
Layer Y+0: 3x3 POLISHED_DEEPSLATE base
Layer Y+1: DEEPSLATE_BRICKS (center) + 4x CHAIN (corners)
Layer Y+2: CHISELED_DEEPSLATE (center) + 4x CHAIN (corners)
```

### Hologram Display
- **Weapon Name:** `[Color]§l[Name]` at Y+2.4
- **Material List:** Each line 0.3 blocks apart, format: `§f[qty]x §7[Material Name]`
- **Custom Items:** Pre-colored (red for Handle, green for Warden's Heart, etc.)
- **Text:** CENTER aligned, CENTER billboard, transparent background, shadow enabled

### Item Display
- Floating weapon item at Y+0.5
- Scale: 1.75x on all axes
- Smooth Y-axis rotation: +0.05 radians/tick (~6.25s per full rotation)

### Ambient Effects
- ENCHANT particles every 1 second (20 ticks)
- 10 particles per burst, spread (0.3, 0.5, 0.3), speed 0.05

### Block Protection
- All pedestal blocks tracked in `protectedBlocks` HashSet
- BlockBreakEvent cancelled if location matches
- Message: `§cThis block is part of a weapon altar and cannot be broken!`

### Crafting Process
1. Player right-clicks near altar (within 3 blocks)
2. `hasRequiredMaterials()` checks inventory
3. Materials consumed from inventory
4. `weapon.createItem()` generates ItemStack with stats + lore
5. Sound: BLOCK_BEACON_ACTIVATE (pitch 1.5) + ENTITY_PLAYER_LEVELUP (pitch 0.8)
6. Particles: 100x TOTEM_OF_UNDYING + 200x ENCHANT
7. Broadcast: `§6§l✦ §e[Player] §6has forged the [Color]§l[Weapon]§6! §l✦`
8. Player message: `§a§lWeapon forged! §7You received [Color]§l[Weapon]§7!`

### Sneaking Interaction
- Shift+right-click shows requirements list
- Regular right-click attempts to craft

### Configurable Altar Settings

**Per-Altar Settings:**
| Setting | Default | Description |
|---------|---------|-------------|
| baseBlock | ANCIENT_DEBRIS | Material for the center support block |
| weaponScale | 1.75 | Display entity scale on all axes |
| rotationSpeed | 0.05 | Radians per tick for Y-axis rotation |
| particleType | ENCHANT | Ambient particle effect type |
| particleCount | 10 | Particles per ambient burst |
| particlesEnabled | true | Toggle ambient particles |
| rotationEnabled | true | Toggle weapon rotation |

**Global Settings (static, shared across altars):**
- Same settings as per-altar but applied globally
- `applyGlobalSettings()` syncs global values to a specific altar

### Creative Mode Interactions
- **Stick + Right-click:** Removes the altar instantly
- **Shift + Right-click:** Opens the Altar Settings GUI for that altar
- Regular players: shift-right-click shows requirements, right-click crafts

### Weapon Attribute-Locking
- Weapons require the wielder to have the matching attribute
- If a player holds an attribute weapon without the correct attribute, the weapon deals **0 damage**
- Message: `§cYou cannot use this weapon! It requires the §e[Attribute] §cattribute.`

---

## 5. Boss Items & Mechanics

### Boss Drop Items
| Item | Material | Display Name | Grants Attribute |
|------|----------|-------------|------------------|
| Dragon Egg | DRAGON_EGG | (vanilla) | DRAGON_EGG |
| Wither Bone | BONE | §5§lWither Bone | WITHER |
| Warden Brain | SCULK_CATALYST | §3§lWarden Brain | WARDEN |
| Breeze Heart | WIND_CHARGE | §b§lBreeze Heart | BREEZE |

### Crafting-Only Items
| Item | Material | Display Name | Purpose |
|------|----------|-------------|---------|
| Weapon Handle | BLAZE_ROD | §c§lWeapon Handle | Required for all weapon crafting |
| Warden's Heart | ECHO_SHARD | §2§lWarden's Heart | Deepcore Maul crafting |
| Dragon Heart | DRAGON_EGG | §5§lDragon Heart | Dominion Blade crafting |

### Boss Item Rules
- **Cannot be dropped** (PlayerDropItemEvent cancelled)
- **Cannot be stored in containers** (InventoryClickEvent cancelled for non-player inventories)
- **Auto-grant on pickup** (EntityPickupItemEvent)
- **Auto-grant on login** (PlayerJoinEvent checks inventory)
- **Dropped on death** (boss items appear in death drops, attribute removed)

### Normal Boss Death Drops

**Normal Warden:**
- 1x Warden Brain (SCULK_CATALYST, grants WARDEN attribute)
- 1x Warden's Heart (crafting material for Deepcore Maul)
- Kill broadcast to all players
- Skipped if warden has custom name (Ascended variant)

**Normal Wither:**
- 1x Wither Bone (BONE, grants WITHER attribute)
- Kill broadcast to all players
- Skipped if wither has custom name (Ascended variant)

**Normal Ender Dragon:**
- 1x Dragon Egg (grants DRAGON_EGG attribute)
- 1x Dragon Heart (crafting material for Dominion Blade)
- Generates END exit portal at (0, 0)
- Kill broadcast to all players
- Skipped if dragon has custom name (Ascended variant)

### Death Broadcast Format
```
§c§l⚠ §6§lDRAGON EGG DROPPED §c§l⚠
§e[PlayerName] §7has lost the Dragon Egg!
```

---

## 6. Ascended Warden Boss

### Base Stats
| Stat | Value |
|------|-------|
| Max Health | 1600 (800 hearts) |
| Armor Reduction | 55% |
| Knockback Resistance | 100% |

### Attacks

**Deep Dark Zone (passive, every 1s)**
- Radius: 16 blocks (24 when enraged)
- Damage: 2.5/s (scales 1.0-1.5x closer to center)
- Visual: SCULK_CHARGE_POP particles (32 points)
- Effect: DARKNESS (60 ticks)

**Sonic Slam (melee, 5-block radius)**
- Damage: 13 (+6 if target airborne)
- Cooldown: 5s (3.5s enraged)
- Knockback: 1.5x + 0.5y
- Trigger: Target within 10 blocks
- Sound: ENTITY_WARDEN_SONIC_BOOM (pitch 0.7)

**Sonic Boom (ranged, every 4s)**
- Range: 30 blocks
- Damage by HP: >80%=12, >60%=15, >40%=18, >20%=22, ≤20%=26
- Effect: Slowness II (40 ticks)
- Visual: SONIC_BOOM particle line every 0.5 blocks

### Enrage (≤30% HP / 480 HP remaining)
- Zone radius: 1.5x (24 blocks)
- Slam cooldown: 0.7x (70 ticks)
- Boss bar turns RED with "[ENRAGED]"
- Sound: ENTITY_WARDEN_ROAR (pitch 0.5)
- Particles: 5 bursts of 50 SCULK_SOUL + SONIC_BOOM

### Anti-Zerg Protection
- Tracks attackers in 5-second window
- -5% damage per attacker after 5 attackers
- Max reduction: 40%

### Boss Bar
- Style: SEGMENTED_10
- Color: BLUE (RED when enraged)
- Render distance: 100 blocks
- Title: `§3§lASCENDED WARDEN`

### Rewards on Death
- 1x Warden Brain (SCULK_CATALYST, grants WARDEN attribute)
- 1x Warden's Heart (crafting material)
- 8x Echo Shard
- 4x Sculk Shrieker

---

## 7. Leveling System

### Level Mechanics
| Setting | Default |
|---------|---------|
| Max Level | 5 |
| Starting Level | 1 |
| Scaling per Level | +10% |
| Levels Lost on Death | 1 |
| Levels Gained on Kill | 1 |

### Level Scaling Formula
```
levelMultiplier = 1.0 + ((level - 1) * 0.10)
L1 = 1.00x | L2 = 1.10x | L3 = 1.20x | L4 = 1.30x | L5 = 1.40x
```

### Kill/Death Effects
**On PvP Kill (killer):**
- +1 level (if `levelGainOnKill` enabled)
- +1 kill stat
- MELEE: +1.5% Bloodlust stacks (max 10%+1%/level)
- HEALTH: +1 heart Vitality (max = level hearts)

**On Death (victim):**
- -1 level (if `levelLossOnDeath` enabled, min 1)
- +1 death stat
- HEALTH: Lose 1 heart per level
- MELEE: Lose all Bloodlust stacks
- Boss items: Dropped, attribute removed, server broadcast

### Tab Display
```
§e[ICON] PlayerName ★★★★★
```
Stars (★) = current level. Updated on join, assign, and level change.

### Action Bar (every 1s)
```
§e[ICON] AttributeName Lv.X | Support: §a✓ §7| Melee: §cXs
```
Shows cooldown as green checkmark (ready) or red countdown (on cooldown).

---

## 8. Upgrader & Reroller

### Attribute Upgrader
| Property | Value |
|----------|-------|
| Item | NETHER_STAR |
| Display Name | §6§lAttribute Upgrader |
| Lore | "Right-click to upgrade your attribute by one level! Max level: 5" |
| Effect | +1 level (cap at 5) |

**Crafting Recipe (3x3):**
```
[Netherite Ingot] [Diamond Block]  [Netherite Ingot]
[Diamond Block]   [Wither Skull]   [Diamond Block]
[Netherite Ingot] [Diamond Block]  [Netherite Ingot]
```

### Attribute Reroller
| Property | Value |
|----------|-------|
| Item | END_CRYSTAL |
| Display Name | §d§lAttribute Reroller |
| Lore | "Right-click to reroll your attribute to a new random one! Warning: Resets level to 1" |
| Effect | Random new attribute, reset to L1, clears cooldowns |

**Crafting Recipe (3x3):**
```
[Netherite Ingot] [Diamond Block]  [Netherite Ingot]
[Diamond Block]   [Nether Star]    [Diamond Block]
[Netherite Ingot] [Diamond Block]  [Netherite Ingot]
```

### Reroller Animation
- 60-tick slot machine animation
- Shows random attributes cycling through
- Slows down: 2 ticks (fast) → 4 → 6 → 10 (very slow)
- Sound: BLOCK_NOTE_BLOCK_HAT with increasing pitch
- Final: UI_TOAST_CHALLENGE_COMPLETE + ENTITY_PLAYER_LEVELUP
- Cannot reroll boss attributes
- Broadcasts to nearby players (50 blocks)

---

## 9. All Commands

### `/smp` (Player Command)
| Subcommand | Args | Permission | Description |
|------------|------|-----------|-------------|
| `support` | - | None | Activate support ability |
| `melee` | - | None | Activate melee ability |
| `info` | `[attribute\|player]` | None | Open attribute info GUI |
| `assign` | `<player> [attribute]` | oddssmp.admin | Assign attribute (random if none specified) |
| `reroll` | `<player>` | oddssmp.admin | Reroll attribute, reset to L1 |
| `upgrade` | `<player> [amount]` | oddssmp.admin | Increase level (default +1) |
| `remove` | `<player>` | oddssmp.admin | Remove attribute entirely |
| `reset` | `<player>` | oddssmp.admin | Reset to L1, clear cooldowns |
| `cooldown` | `<player> <support\|melee> <seconds>` | oddssmp.admin | Set ability cooldown |

### `/admin` (Admin Command - all require oddssmp.admin)
| Subcommand | Args | Player Only? | Description |
|------------|------|-------------|-------------|
| `gui` | - | Yes | Open admin control panel |
| `test` | `<player> <support\|melee\|passive>` | Yes | Test particle effects |
| `boss` | `<wither\|warden\|breeze\|enderdragon\|stop\|stopall>` | No (for stop) | Spawn or stop bosses |
| `weapon` | - | Yes | Open weapon GUI |
| `customitems` | - | Yes | Open custom items GUI |
| `spwe` / `spawnweapon` | `<weapon_name>` | Yes | Spawn weapon altar |
| `givehandle` | - | Yes | Give 1 Weapon Handle |
| `giveupgrader` | `[amount]` | Yes | Give upgrader(s) |
| `givereroller` | `[amount]` | Yes | Give reroller(s) |
| `autoassign` | `<on\|off> [delay_seconds]` | No | Toggle auto-assign on join |
| `assignall` | - | No | Assign to all players without attributes |
| `debugdragon` | - | Yes | Apply Dragon Egg effects for testing |
| `removealtars` / `clearaltars` | - | No | Remove all active weapon altars |
| `listaltars` / `altars` | - | No | List all active altars with coordinates |

### Tab Completion
- `/smp` first arg: `info`, `support`, `melee` (+ admin commands if op)
- `/smp info`: all attribute names (lowercase) + online player names
- `/smp assign [player]`: all attribute names
- `/admin boss`: `wither`, `warden`, `breeze`, `enderdragon`, `stop`, `stopall`
- `/admin spwe`: all weapon enum names
- `/admin autoassign`: `on`, `off`, then suggested delays: `5`, `10`, `15`, `30`, `60`

---

## 10. All GUIs

### Admin Panel (`/admin gui`)
**Title:** `§6§lOddsSMP Admin Panel` | **Size:** 27 slots

| Slot | Material | Name | Action |
|------|----------|------|--------|
| 10 | PLAYER_HEAD | Player Management | Opens player list |
| 12 | ENCHANTED_BOOK | Attribute Browser | Opens attribute encyclopedia |
| 14 | BOOK | Server Statistics | Opens stats page |
| 16 | COMMAND_BLOCK | Batch Operations | Opens bulk actions |
| 20 | ANVIL | Attribute Editor | Opens per-attribute tuning |
| 22 | COMPARATOR | Plugin Settings | Opens settings menu |

### Player Management
**Title:** `§e§lPlayer Management` | **Size:** 54 slots
- Slots 0-44: Player heads (up to 45 players) showing name, attribute, level, K/D
- Click opens individual player options

### Player Options
**Title:** `§e§l[Player] Management` | **Size:** 45 slots

| Slot | Action |
|------|--------|
| 10 | Assign Random Attribute |
| 12 | Reroll Attribute |
| 14 | Upgrade Level (+1) |
| 16 | Downgrade Level (-1) |
| 19 | Choose Specific Attribute (opens selector) |
| 23 | Reset Player (L1, clear cooldowns) |
| 25 | Remove Attribute |
| 28 | Manage Cooldowns |
| 30 | Grant Dragon Egg |
| 32 | View Ability Details |
| 34 | Test Abilities |

### Attribute Editor
**Title:** `§6§lAttribute Editor` | **Size:** 54 slots
- Slots 0-16: All 17 attributes with GUI materials
- Bottom row: Global Cooldown Multiplier (slot 45), Global Damage Multiplier (46), Level Scaling (47)
- Left/Right click: adjust values, Shift+Click: reset

### Per-Attribute Editor
**Title:** `§6§lEdit: [Attribute]` | **Size:** 54 slots
- Support settings: Cooldown Modifier (10), Duration (11), Range (12)
- Melee settings: Cooldown Modifier (19), Damage Multiplier (20), Duration (21)
- Passive settings: Strength (28), Tick Rate (29)
- Presets: Balanced (45), High Power (46), Low Power (47), Chaos (48)
- Actions: Save (50), Reset Defaults (51), Back (53)

### Plugin Settings Menu
**Title:** `§b§lPlugin Settings` | **Size:** 54 slots

| Slot | Submenu |
|------|---------|
| 10 | Gameplay Settings |
| 12 | Combat Settings |
| 14 | Boss Settings |
| 16 | Broadcast Settings |
| 30 | Multiplier Settings |
| 32 | Quick Presets |
| 34 | Death Settings |
| 37 | Particle Settings |
| 39 | Combat Log Settings |

### Gameplay Settings
- Level Loss on Death toggle, Level Gain on Kill toggle
- Auto-Assign toggle (+delay adjustment)
- PvP Only Abilities toggle, Friendly Fire toggle
- Max Level, Levels Lost/Gained adjustments
- Passive Tick Rate, Passive Effect Strength

### Combat Settings
- PvP Damage Multiplier, Ability Damage Multiplier
- Global Damage Multiplier, Global Cooldown Multiplier
- Combat Tag toggle + duration

### Particle Settings
**Title:** `§d§lParticle Settings` | **Size:** 54 slots
- Master Toggle (slot 4)
- **Ability Particles:** Support (10), Melee (11), Passive (12)
- **Combat Particles:** Damage (15), Critical (16), Blocking (17), Healing (18), Kill (19), Death (20)
- **Player Events:** Level Up (23), Attribute Assign (24), Attribute Remove (25)
- **Boss Particles:** Ambient (28), Ability (29), Spawn (30), Death (31)
- **World Particles:** Altar Ambient (33), Altar Activation (34), Item Pickup (35)
- **Effect Particles:** Status (36), Buff (37), Debuff (38), Teleport (39), Respawn (40), Combo (41), Kill Streak (42)
- Intensity (45), Render Distance (46), Enable All (50), Disable All (51)

### Attribute Encyclopedia (`/smp info`)
**Title:** `§d§lAttribute Encyclopedia` | **Size:** 54 slots
- All 17 attributes shown with materials
- Click opens detailed view with Support/Melee/Passive descriptions
- Level system info at slot 49

### Custom Items GUI (`/admin customitems`)
**Title:** `§6§lCustom Items` | **Size:** 54 slots
- Row 1: Weapon Handle, Warden's Heart, Wither Bone, Breeze Heart, Dragon Heart, Upgrader, Reroller
- Rows 2-5: All 17 attribute weapons
- Click to receive any item

### Altar Settings GUI (Creative Mode Shift+Right-click)
**Title:** `§6§lAltar Settings: [WeaponName]` | **Size:** 54 slots

| Slot | Item | Action |
|------|------|--------|
| 4 | Per-altar info head | Shows current altar's weapon name |
| 10 | Base block item | Left/Right click cycles through: ANCIENT_DEBRIS, OBSIDIAN, CRYING_OBSIDIAN, DEEPSLATE, BLACKSTONE, GILDED_BLACKSTONE, NETHERITE_BLOCK |
| 12 | SPYGLASS | Left/Right click adjusts weapon scale (0.25-5.0, step 0.25) |
| 14 | CLOCK | Left/Right click adjusts rotation speed (0.01-0.2, step 0.01) |
| 16 | BLAZE_POWDER | Left/Right click cycles particle type: ENCHANT, END_ROD, FLAME, SOUL_FIRE_FLAME, PORTAL, WITCH, GLOW |
| 19 | REDSTONE | Left/Right click adjusts particle count (1-50, step 1) |
| 21 | Particle toggle | Click toggles particles on/off |
| 23 | Rotation toggle | Click toggles rotation on/off |
| 28 | GOLDEN_APPLE | Apply global settings to this altar |
| 37 | COMPASS | Teleport to this altar |
| 39 | TOTEM_OF_UNDYING | Respawn (rebuild) this altar |
| 41 | BARRIER | Delete this altar |
| 45 | BOOK | Open global settings (same layout, edits static defaults) |
| 49 | CHEST | Open List All Altars GUI |

### Altar List GUI
**Title:** `§6§lAll Weapon Altars` | **Size:** 54 slots
- Each altar shown as its weapon's material item
- Lore: weapon name, coordinates (X, Y, Z), world
- **Left-click:** Open that altar's settings GUI
- **Right-click:** Teleport to that altar
- **Shift-click:** Delete that altar
- Slot 49: Back button to return to previous altar's settings

### Vision Player Tracking GUI
**Title:** `§b§lTrack Player` | **Size:** 54 slots
- Shows all online players (except caster) as player heads
- Lore: player name, world, distance in blocks
- Click a head to begin tracking that player
- During tracking: action bar shows `§3§lTRACKING §e[Name] §7| §f[X] blocks §7[Direction]`
- Tracking applies Glowing effect to target
- Tracking ends when tracker is within **5 chunks (80 blocks)** of target, or target goes offline

### GUI Material Icons per Attribute
| Attribute | Material |
|-----------|----------|
| Melee | IRON_SWORD |
| Health | RED_DYE |
| Defense | SHIELD |
| Wealth | GOLD_INGOT |
| Speed | FEATHER |
| Control | ENDER_EYE |
| Range | BOW |
| Pressure | LIGHTNING_ROD |
| Tempo | CLOCK |
| Disruption | TNT |
| Vision | SPYGLASS |
| Transfer | ENDER_PEARL |
| Risk | COMPARATOR |
| Wither | WITHER_SKELETON_SKULL |
| Warden | SCULK_CATALYST |
| Breeze | WIND_CHARGE |
| Dragon Egg | DRAGON_EGG |

---

## 11. All Configuration

### Gameplay
| Setting | Default | Range |
|---------|---------|-------|
| autoAssignEnabled | false | boolean |
| autoAssignDelaySeconds | 10 | 0+ |
| levelLossOnDeath | true | boolean |
| levelGainOnKill | true | boolean |
| pvpOnlyAbilities | false | boolean |
| friendlyFire | true | boolean |
| maxLevel | 5 | 1-10 |
| levelsLostOnDeath | 1 | 0-5 |
| levelsGainedOnKill | 1 | 0-5 |
| killStreakBonuses | false | boolean |
| killStreakThreshold | 3 | 1+ |

### Combat
| Setting | Default | Range |
|---------|---------|-------|
| pvpDamageMultiplier | 1.0 | 0.1-5.0 |
| abilityDamageMultiplier | 1.0 | 0.1-5.0 |
| combatTagEnabled | true | boolean |
| combatTagDuration | 15 | 5-60 seconds |

### Death
| Setting | Default |
|---------|---------|
| keepInventoryOnDeath | false |
| dropAttributeItemsOnDeath | true |

### Boss
| Setting | Default | Range |
|---------|---------|-------|
| bossHealthMultiplier | 1.0 | 0.1-10.0 |
| bossDamageMultiplier | 1.0 | 0.1-10.0 |
| bossDropRateMultiplier | 1.0 | 0.1-10.0 |

### Passive
| Setting | Default | Range |
|---------|---------|-------|
| passiveTickRate | 1.0s | 0.5-5.0 |
| passiveEffectStrength | 1.0 | 0.1-5.0 |

### Broadcast
| Setting | Default |
|---------|---------|
| broadcastAttributeAssign | true |
| broadcastLevelUp | false |
| broadcastDragonEgg | true |
| broadcastBossSpawn | true |
| broadcastBossDefeat | true |

### Particles (28 toggles)
| Category | Settings (all default: true) |
|----------|------------------------------|
| Ability | particleSupportAbility, particleMeleeAbility, particlePassiveAbility |
| Combat | particleDamageHit, particleCriticalHit, particleBlocking, particleHealing, particleKill, particleDeath |
| Events | particleLevelUp, particleAttributeAssign, particleAttributeRemove |
| Boss | particleBossAmbient, particleBossAbility, particleBossSpawn, particleBossDeath |
| World | particleAltarAmbient, particleAltarActivation, particleItemPickup, particleItemDrop |
| Effects | particleStatusEffect, particleBuffApplied, particleDebuffApplied, particlePotionEffect |
| Special | particleTeleport, particleRespawn, particleCombo, particleKillStreak |

| Numeric | Default | Range |
|---------|---------|-------|
| particleMasterEnabled | true | boolean |
| particleIntensity | 1.0 | 0.25-2.0 |
| particleRenderDistance | 32 | 8-64 blocks |

### Attribute Settings (per attribute, all default to Balanced)
| Per-Attribute Setting | Default |
|----------------------|---------|
| supportCooldownModifier | 1.0 |
| meleeCooldownModifier | 1.0 |
| supportDuration | 10s |
| supportRange | 10.0 blocks |
| meleeDamageMultiplier | 1.0 |
| meleeDuration | 5s |
| passiveStrength | 1.0 |
| passiveTickRate | 1.0 |

### Global Attribute Settings
| Setting | Default | Range |
|---------|---------|-------|
| globalCooldownMultiplier | 1.0 | 0.1-5.0 |
| globalDamageMultiplier | 1.0 | 0.1-5.0 |
| levelScalingPercent | 10% | 0-50% |
| baseCooldown | 120s | 10-300s |

### Presets
| Preset | Effects | Cooldowns |
|--------|---------|-----------|
| Balanced | 1.0x | 1.0x |
| High Power | 1.5x | 0.7x |
| Low Power | 0.7x | 1.5x |
| Chaos | Random 0.3-2.5x | Random 0.3-2.0x |
| OP | 2.0x | 0.5x |

---

## 12. Combat Logger

### Event Types
| Type | Icon | Example Format |
|------|------|----------------|
| DAMAGE | §c⚔ | "[attacker] dealt [X] dmg to [victim]" |
| ABILITY | §d✦ | "[player] used [ability] ([type]) on [target]" |
| KILL | §4☠ | "[killer] killed [victim] with [weapon]" |
| DEATH | §4☠ | "[victim] died ([cause])" |
| HEALING | §a❤ | "[player] healed +[X]❤ from [healer]" |
| COMBAT_TAG | §e⚔ | "[player] entered/left combat with [enemy]" |
| BLOCKED | §b🛡 | "[player] blocked [X] damage" |
| CRITICAL | §6⚡ | "[attacker] landed CRITICAL HIT on [victim]" |
| ENVIRONMENTAL | §7☢ | "[victim] took [X] from [source]" |
| MOB_DAMAGE | §c🐾 | "[mob] dealt [X] to [victim]" |

### Settings
| Setting | Default | Range |
|---------|---------|-------|
| enabled | true | boolean |
| logToFile | true | boolean |
| logToConsole | false | boolean |
| showToPlayers | true | boolean |
| compactMode | false | boolean |
| showDamageNumbers | true | boolean |
| showHealthBars | true | boolean |
| logDamageEvents | true | boolean |
| logAbilityEvents | true | boolean |
| logKillEvents | true | boolean |
| logHealingEvents | true | boolean |
| logCombatTagEvents | true | boolean |
| logCriticalHits | true | boolean |
| logBlockedDamage | true | boolean |
| logEnvironmentalDamage | false | boolean |
| logMobDamage | false | boolean |
| minimumDamageThreshold | 0.0 | 0.0-20.0 |
| maxLogHistory | 100 | 10-1000 |
| maxPlayerLogHistory | 50 | 10-500 |

### File Output
- Path: `plugins/OddsSMP/combat-logs/combat-[yyyy-MM-dd].log`
- Format per line: `[HH:mm:ss] [message without color codes]`

---

## 13. Particle Effects

### New Particle System (Global Limits)

| Limit | Value | Description |
|-------|-------|-------------|
| HARD_CAP_PER_PLAYER | 25 | Max active particles per player at any time |
| MAX_PER_TRIGGER | 12 | Max particles spawned in a single trigger |
| MIN_LIFETIME | 4 ticks | Minimum particle duration |
| MAX_LIFETIME | 8 ticks | Maximum particle duration |
| MAX_RADIUS | 0.5 blocks | Max spread radius for particle spawning |
| RENDER_DISTANCE | 16 blocks | Max distance for players to see particles |
| MIN_PULSE_INTERVAL | 15 ticks | Minimum time between same effect repeating |

### Core Tracking
- **Per-player active count:** `Map<UUID, Integer>` tracks how many particles each player has active
- **Pulse timing:** `Map<UUID, Map<String, Long>>` prevents the same effect from firing too frequently
- **Automatic cleanup:** `clearPlayer(UUID)` called on disconnect to free tracking data
- **Render distance filtering:** Only nearby players within 16 blocks receive particle packets

### Ability-Specific Effects

**MELEE Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playPowerStrike()` | DUST | Yellow (255,255,0) | 8 | Yellow sparks at target location |
| `playBattleFervor()` | DUST | Red (200,0,0) | 6 | Red dust ring around player |
| `playBerserk()` | DUST | Crimson (180,0,0) | 10 | Crimson burst at player location |

**DEFENSE Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playIronSkin()` | DUST | Gray (150,150,150) | 6 | Gray dust at player location |
| `playFortifyAura()` | DUST | Gold (255,200,0) | 8 | Gold dust ring around player |
| `playLastStand()` | DUST | Red (255,0,0) | 10 | Red glow at player location |

**RANGED Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playPiercingShot()` | DUST | White (255,255,255) | 8 | White trail along eye direction |
| `playVolley()` | CRIT | — | 10 | Crit particles in upward arc |

**MAGIC Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playArcaneSurge()` | DUST | Purple (160,32,240) | 8 | Purple dust at player location |
| `playManaShield()` | DUST | Cyan (0,200,200) | 10 | Cyan dust ring around player |
| `playBlink()` | DUST | Purple (128,0,128) | 12 | Purple poof at origin + destination |

**WEALTH Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playPlunderKill()` | DUST | Gold (255,215,0) | 10 | Gold coins burst at target |
| `playTreasureSense()` | DUST | Gold (255,200,0) | 6 | Gold arrows pointing up at player |

**UTILITY Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playSpeedBoost()` | DUST | White (240,240,240) | 6 | White poof at feet |
| `playStealth()` | DUST | Dark Gray (50,50,50) | 8 | Smoke at player location |
| `playRegeneration()` | DUST | Green (0,200,0) | 6 | Green hearts at player location |

**CONTROL Effects:**
| Method | Particle | Color | Count | Description |
|--------|----------|-------|-------|-------------|
| `playFreeze()` | DUST | Ice Blue (150,200,255) | 10 | Ice blue particles at target |
| `playShock()` | DUST | Electric (255,255,100) | 8 | Electric sparks at target |

### Legacy Compatibility Methods
| Method | Usage | Description |
|--------|-------|-------------|
| `playSupportParticles(player, attr, level)` | Called on support activation | Routes to attribute-specific effects |
| `playMeleeParticles(attacker, target, attr)` | Called on melee hit | Routes to attribute-specific effects |
| `playPassiveParticles(player, attr)` | Called every second by ticker | Routes to attribute-specific effects, respects pulse intervals |

### Helper Effect Methods
| Method | Description |
|--------|-------------|
| `playVampiricHeal(player)` | Red dust for Health lifesteal |
| `playLockdownZone(player)` | Purple ring for Control lockdown |
| `playSystemJam(player)` | Red dust for Disruption system jam |
| `playDominionCharge(player)` | Purple dust for Dragon Egg charge |
| `playStunEffect(target)` | Yellow sparks for Tempo stun |

---

## 14. Data Persistence

### Storage File
- Location: `plugins/OddsSMP/playerdata.yml`
- Format: YAML (Bukkit YamlConfiguration)

### Data Structure
```yaml
<UUID>:
  attribute: <ENUM_NAME>    # e.g. "MELEE", "DRAGON_EGG"
  level: <int>              # 1-5
  kills: <int>              # 0+
  deaths: <int>             # 0+
```

### Save Triggers
- Plugin disable
- Any data modification
- Player quit

### Load Triggers
- Plugin enable (all data)
- Player join (per player)

### Validation
- Invalid UUIDs: warned and skipped
- Invalid attribute names: warned, player gets empty data
- Missing fields: default to level=1, kills=0, deaths=0

---

## 15. Plugin Lifecycle

### onEnable() Sequence
1. Log: `"OddsSMP Plugin Enabled!"`
2. Setup data file (create if not exists)
3. Load all saved player data from playerdata.yml
4. Initialize managers (in order):
   - AttributeSettings, CombatLogger, ParticleManager
   - AbilityManager, CommandHandler, EventListener
   - AdminGUI, WeaponGUI, GUIListener
5. Register `/smp` and `/admin` command handlers
6. Register EventListener and GUIListener
7. Register crafting recipes (Upgrader key: `upgrader`, Reroller key: `reroller`)
8. Start passive ticker (BukkitRunnable, 20-tick period)
9. Sync online players (create data, update tab display)
10. Log: `"OddsSMP loaded with X attributes!"`

### onDisable() Sequence
1. Log: `"OddsSMP Plugin Disabled!"`
2. Save all player data to playerdata.yml
3. Save attribute settings config
4. Shutdown combat logger
5. Remove all active weapon altars (despawn entities, clear blocks)

### Passive Ticker (every 20 ticks / 1 second)
For each online player:
1. `handlePassiveEffects(player)` - Apply attribute passives
2. `updateActionBar(player)` - Show cooldown status

### plugin.yml
```yaml
name: OddSMP
version: 1.0.0
main: com.oddssmp.OddsSMP
api-version: '1.21'
softdepend: [DecentHolograms]

commands:
  smp:
    permission: oddsmp.player
    default: true
  admin:
    permission: oddsmp.admin
    default: op
```

---

## Summary

| Category | Count |
|----------|-------|
| Attributes | 17 (13 standard + 4 boss) |
| Weapons | 17 (13 standard + 4 boss) |
| Abilities | 51 (17 melee + 17 support + 17 passive) |
| GUI Menus | 25+ (admin panel, settings, editor, altar settings, altar list, vision tracking, etc.) |
| Commands | 22+ subcommands across /smp and /admin |
| Config Options | 70+ individual settings |
| Particle Effects | Strict limit system (25/player, 12/trigger), 20+ effect methods |
| Combat Log Events | 10 types |
| Normal Boss Drops | 3 bosses (Warden, Wither, Ender Dragon) with attribute items |
