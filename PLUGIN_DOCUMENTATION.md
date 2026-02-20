# OddsSMP Plugin - Complete Documentation

## Table of Contents
1. [Attributes](#attributes)
2. [Weapons](#weapons)
3. [Crafting Costs](#crafting-costs)
4. [Commands](#commands)
5. [Boss Items](#boss-items)
6. [Leveling System](#leveling-system)
7. [Upgrader & Reroller](#upgrader--reroller)
8. [Configuration](#configuration)
9. [Combat Logger](#combat-logger)
10. [Additional Features](#additional-features)

---

## Attributes

### Standard Attributes (13)

#### 1. MELEE (Icon: ⚔)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Power Strike | Melee | Ignores 25% armor (+1%/level, max 30%) | 120s |
| Battle Fervor | Support | +15% melee damage for 6s (+1s/level, max 11s) | 150s |
| Bloodlust | Passive | +1.5% damage per PvP kill (max 10% +1%/level = 15% at L5). Lose all on death | - |

#### 2. HEALTH (Icon: ❤)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Vampiric Hit | Melee | Heals 15% of damage dealt (+1%/level, max 20%) for 5s. Overheal = absorption | 120s |
| Fortify | Support | Heal 3 hearts (+0.5/level, max 5.5 hearts) | 120s |
| Vitality | Passive | +1 max heart per PvP kill (max = level hearts). Lose 1 heart/level on death | - |

#### 3. DEFENSE (Icon: 🛡)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Iron Response | Melee | 20% damage reduction for 4s (+0.5s/level, max 6s) | 120s |
| Shield Wall | Support | Gain 4 absorption hearts (+0.5/level, max 6.5) for 8s | 120s |
| Hardened | Passive | Armor breaks 5% slower (+1%/level) | - |

#### 4. WEALTH (Icon: 💰)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Plunder Kill | Melee | Disables target's held item for 7s (+1s/level, max 12s) | 120s |
| Economic Surge | Support | 100% villager discount + Fortune VII for 20s (+2s/level, max 30s) | 120s |
| Industrialist | Passive | Permanent Hero of the Village XII, +10% mob drops/level, +1 Fortune/level | - |

#### 5. CONTROL (Icon: 🕹)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Disrupt | Melee | Slowness III for 3s (+1s/level, max 8s) | 120s |
| Lockdown | Support | 6-block radius: enemies can't use abilities, take +25% damage. 5s (+1s/level, max 10s) | 120s |
| Suppression | Passive | Players you hit get +10s cooldown (+1s/level, max 15s). Once per 30s | - |

#### 6. RANGE (Icon: 🏹)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Spacing Strike | Melee | Knockback, target can't approach you for 3s (+1s/level, max 8s) | 120s |
| Zone Control | Support | Gain homing arrows for 5s (+1s/level) | 120s |
| Footwork | Passive | Bows/crossbows deal +20% damage (+1%/level, max 25%) | - |

#### 7. TEMPO (Icon: ⏱)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Tempo Strike | Melee | Stun target (can't move/look) for 5s (+1s/level) | 120s |
| Overdrive | Support | Haste V for 5s (+1s/level) | 120s |
| Momentum | Passive | Permanent Speed I | - |

#### 8. VISION (Icon: 👁)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Target Mark | Melee | Apply Glowing for 5m (+30s/level, max 7.5m) | 120s |
| True Sight | Support | Track enemies with Glowing for 5s (+1s/level, max 10s) | 120s |
| Awareness | Passive | Players within 12 blocks glow (visible only to you) | - |

#### 9. TRANSFER (Icon: 🔁)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Effect Swap | Melee | Sacrifice YOUR positive effect to inflict negative on target (1 minute). Speed→Slowness, Strength→Weakness, Regen→Poison, etc. | 120s |
| Redirection | Support | Reflect durability damage to attacker for 5s (+1s/level, max 10s) | 120s |
| Cleanse | Passive | Immune to all debuffs | - |

#### 10. SPEED (Icon: ⚡)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Flash Step | Melee | Summons lightning on target | 120s |
| Rapid Formation | Support | Speed III for 6s | 120s |
| Adrenaline | Passive | Double-jump boost in look direction. Cooldown: 20s (-1s/level) | - |

#### 11. PRESSURE (Icon: 🩸)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Crushing Blow | Melee | +25% damage, target takes +15% damage for 4s (+1s/level, max 9s) | 120s |
| Intimidation Field | Support | 6-block radius: enemies deal -15% damage (+5%/level, max -35%), take +10% damage (+5%/level, max +30%) for 6s | 120s |
| Oppression | Passive | Enemies below 50% HP near you take +10% damage (+3%/level, max 25%) | - |

#### 12. DISRUPTION (Icon: 🧠)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Fracture | Melee | +20s to all cooldowns, Weakness II + Blindness + Nausea for 4s (+1s/level, max 9s) | 120s |
| System Jam | Support | 6-block radius: lock abilities for 25s (+1s/level, max 30s). Deals 20% of enemy's total cooldowns as damage | 150s |
| Desync | Passive | First hit per fight: +15s to both enemy cooldowns (+2s/level, max 20s). Resets every 30s | - |

#### 13. RISK (Icon: 🎲)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| All In | Melee | +50% damage (+10%/level), you take +25% damage (-2%/level) for 5s | 120s |
| Double Or Nothing | Support | +30% damage (+5%/level, max 55%), take +20% damage for 6s | 150s |
| Gambler's Edge | Passive | Below 40% HP: +10% damage (+1%/level, max 15%) | - |

---

### Boss Attributes (4)

#### 14. WITHER (Icon: 💀)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Desperation Cleave | Melee | 12 damage (+1.2/level), damage scales with missing HP (2x at <20%), 20% armor pen (+5%/level), Wither II for 4s (+0.5s/level), enemy healing -40% (+5%/level) | 240s |
| Shadow Pulse | Support | 6-block AoE: 10 damage + Slowness II for 3s | 240s |
| Curse of Despair | Passive | Healing received -25% (+1%/level), melee cooldown +10s (-1s/level) | - |

#### 15. WARDEN (Icon: 🐋)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Sonic Slam | Melee | 14 damage, 5-block AoE, Slowness II + Mining Fatigue II for 3s | 240s |
| Deep Dark Zone | Support | 12-16 block radius (+0.8/level), 8-12s duration (+1s/level). Enemies: no sprint, -50% jump, -30% attack speed, -50% healing. You: +15% melee damage | 180s |
| Curse of Silence | Passive | -15% attack speed when idle | - |

#### 16. BREEZE (Icon: 🌬)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Judging Strike | Melee | Target takes +12% damage (+2%/level) for 10s. If they miss: +6 true damage (+1/level) | 240s |
| Trial Order | Support | -20% cooldowns, +10% damage reduction for 6s (+1s/level) | 240s |
| Curse of Judgment | Passive | Cooldowns increase out of combat, healing -25%, move speed -5% when idle | - |

#### 17. DRAGON EGG (Icon: 🥚)
| Ability | Type | Effect | Cooldown |
|---------|------|--------|----------|
| Rampaging Strike | Melee | 10 damage (+2/level, max 18), 1.5x below 30% HP, 20% lifesteal (+5%/level, max 40%), Slowness III + Weakness II for 3s (+0.5s/level) | 300s |
| Dominion | Support | **REQUIRES 5 CONSECUTIVE HITS TO CHARGE (3s between hits)**. +25% damage (+1%/level, max 30%), +50% cooldown reduction (+1%/level, max 55%) for 8s | 300s |
| Draconic Curse | Passive | Nearby enemies (8 blocks) take +15% damage (-1%/level, min 10%) | - |

---

## Weapons

### Standard Weapons (13)
All: **15 damage, 1.6 attack speed, 300 durability**

| Weapon | Attribute | Material | On-Hit Effect | Passive |
|--------|-----------|----------|---------------|---------|
| Breaker Blade | Melee | Netherite Sword | -40% armor for ability | +6% final melee damage |
| Crimson Fang | Health | Netherite Sword | 10% lifesteal | +2 max HP while held |
| Bulwark Mace | Defense | Mace | 10% dmg reduction 3s | -10% knockback taken |
| Gilded Cleaver | Wealth | Netherite Axe | Mob kill: +50% drops | Looting +2 |
| Lockspike | Control | Iron Sword | Slowness I 2s | +10% ability duration |
| Windcaller Pike | Range | Trident | Reach: +1.5 blocks | +25% knockback |
| Chrono Saber | Tempo | Golden Sword | +5s cooldown to target | -5% own cooldowns |
| Watcher's Blade | Vision | Netherite Sword | Mark target 5s | Glow enemies 6 blocks |
| Mirror Edge | Transfer | Diamond Sword | Steal effects 4s | -10% debuff duration |
| Flashsteel Dagger | Speed | Iron Sword | +5% move speed 3s | +10% sprint speed |
| Bonecrusher | Pressure | Mace | +10% damage taken 4s | +10% vs low HP |
| Fracture Rod | Disruption | Blaze Rod | +10s all cooldowns | +10% debuff duration |
| High Roller Blade | Risk | Golden Sword | +30% dealt/+15% taken | +10% crit below 40% |

### Boss Weapons (4)
All: **16 damage, 1.6 attack speed, 300 durability**

| Weapon | Attribute | Material | On-Hit | Passive |
|--------|-----------|----------|--------|---------|
| Despair Reaver | Wither | Netherite Hoe | +1% damage per 1% missing HP | Healing -20% |
| Deepcore Maul | Warden | Mace | +20% damage in zones | -20% knockback taken |
| Verdict Lance | Breeze | Trident | 3 true damage | +10% vs cooldown users |
| Dominion Blade | Dragon Egg | Netherite Sword | Allies +10% damage | Lifesteal 10% |

---

## Crafting Costs

### Standard Weapons
| Material | Amount |
|----------|--------|
| Netherite Ingot | 4 |
| Diamond Block | 8 |
| Iron Block | 32 |
| Weapon Handle | 1 |

### Boss Weapons (Example: Deepcore Maul)
| Material | Amount |
|----------|--------|
| Bone Block | 64 |
| Iron Block | 64 |
| Copper Block | 64 |
| Skeleton Skull | 6 |
| Wither Skeleton Skull | 6 |
| Player Head | 3 |
| Weapon Handle | 1 |
| Warden's Heart | 1 |

---

## Commands

### Player Commands (`/smp`)
| Command | Description |
|---------|-------------|
| `/smp support` | Activate support ability |
| `/smp melee` | Activate melee ability |
| `/smp info` | Open attribute info GUI |
| `/smp info <attribute>` | View specific attribute details |
| `/smp info <player>` | View another player's attribute |

### Admin Commands (`/smp` - requires oddssmp.admin)
| Command | Description |
|---------|-------------|
| `/smp assign <player> [attribute]` | Assign attribute (random if not specified) |
| `/smp reroll <player>` | Get new random attribute (resets to L1) |
| `/smp upgrade <player> [amount]` | Level up player |
| `/smp remove <player>` | Remove attribute |
| `/smp reset <player>` | Reset to level 1, clear cooldowns |
| `/smp cooldown <player> <support\|melee> <seconds>` | Set cooldown |

### Admin Commands (`/admin` - requires oddssmp.admin)
| Command | Description |
|---------|-------------|
| `/admin gui` | Open admin control panel |
| `/admin boss <type>` | Spawn boss (wither/warden/breeze/enderdragon/stop/stopall) |
| `/admin weapon` | Open weapon GUI |
| `/admin customitems` | Open custom items GUI |
| `/admin spwe <weapon>` | Spawn weapon altar at location |
| `/admin givehandle` | Give Weapon Handle |
| `/admin giveupgrader [amount]` | Give upgrader(s) |
| `/admin givereroller [amount]` | Give reroller(s) |
| `/admin autoassign <on\|off> [delay]` | Toggle auto-assign |
| `/admin assignall` | Assign attributes to all players without one |
| `/admin test <player> <support\|melee\|passive>` | Test particles |

---

## Boss Items

| Item | Material | Display Name | Grants Attribute |
|------|----------|--------------|------------------|
| Dragon Egg | Dragon Egg | - | DRAGON_EGG |
| Wither Bone | Coal Block | §5§lWither Bone | WITHER |
| Warden Brain | Sculk Catalyst | §3§lWarden Brain | WARDEN |
| Breeze Heart | Wind Charge | §b§lBreeze Heart | BREEZE |
| Weapon Handle | Blaze Rod | §c§lWeapon Handle | - (crafting material) |

**Mechanics:**
- Cannot be dropped or stored in containers
- Auto-grant attribute on pickup or login
- Server broadcast on acquisition
- Dropped on death (loses attribute)

---

## Leveling System

### Level Mechanics
| Setting | Value |
|---------|-------|
| Max Level | 5 |
| Starting Level | 1 |
| Level Scaling | +10% per level |

### Level Changes
| Event | Effect |
|-------|--------|
| Kill Player | +1 level (if enabled) |
| Death | -1 level (if enabled) |

### Tab Display
Format: `[ICON] PlayerName ★★★★★` (stars = level)

---

## Upgrader & Reroller

### Attribute Upgrader
| Property | Value |
|----------|-------|
| Item | Nether Star |
| Display Name | §6§lAttribute Upgrader |
| Effect | +1 level (max 5) |
| Usage | Right-click |

### Attribute Reroller
| Property | Value |
|----------|-------|
| Item | End Crystal |
| Display Name | §d§lAttribute Reroller |
| Effect | Random new attribute, reset to L1 |
| Usage | Right-click |

---

## Configuration

### Gameplay Settings
| Setting | Default | Description |
|---------|---------|-------------|
| autoAssignEnabled | false | Auto-assign on join |
| autoAssignDelaySeconds | 10 | Delay before auto-assign |
| levelLossOnDeath | true | Lose level on death |
| levelGainOnKill | true | Gain level on kill |
| maxLevel | 5 | Maximum level (1-10) |
| levelsLostOnDeath | 1 | Levels lost per death |
| levelsGainedOnKill | 1 | Levels gained per kill |

### Combat Settings
| Setting | Default | Description |
|---------|---------|-------------|
| pvpDamageMultiplier | 1.0 | PvP damage multiplier |
| abilityDamageMultiplier | 1.0 | Ability damage multiplier |
| combatTagEnabled | true | Enable combat tagging |
| combatTagDuration | 15 | Combat tag duration (seconds) |
| friendlyFire | true | Allow friendly fire |

### Boss Settings
| Setting | Default | Description |
|---------|---------|-------------|
| bossHealthMultiplier | 1.0 | Boss HP multiplier |
| bossDamageMultiplier | 1.0 | Boss damage multiplier |
| bossDropRateMultiplier | 1.0 | Boss drop rate multiplier |

### Broadcast Settings
| Setting | Default | Description |
|---------|---------|-------------|
| broadcastAttributeAssign | true | Announce attribute assignment |
| broadcastLevelUp | false | Announce level ups |
| broadcastDragonEgg | true | Announce Dragon Egg events |
| broadcastBossSpawn | true | Announce boss spawns |
| broadcastBossDefeat | true | Announce boss defeats |

---

## Combat Logger

### Event Types Logged
- Damage dealt/taken
- Ability usage
- Kills/deaths
- Healing
- Combat tag entry/exit
- Critical hits
- Blocked damage

### Log Settings
| Setting | Default | Description |
|---------|---------|-------------|
| enabled | true | Enable combat logging |
| logToFile | true | Log to file |
| logToConsole | false | Log to console |
| showToPlayers | true | Show to players |
| showDamageNumbers | true | Show damage numbers |
| showHealthBars | true | Show health bars |

---

## Additional Features

### Weapon Altar System
- Spawned via `/admin spwe <weapon>`
- Hovering text + rotating item display
- 5x5 protected area
- Ambient enchantment particles
- Server broadcast on weapon craft

### Auto-Assign Animation
- Slot machine style animation
- 60 tick duration
- Random attribute selection
- Excludes boss attributes

### Passive Ability Ticker
- Runs every 1 second (20 ticks)
- Applies continuous effects to all players
- VISION: nearby player glow
- TRANSFER: debuff immunity
- WEALTH: Hero of the Village
- TEMPO: Speed I

### Data Persistence
- File: `plugins/OddsSMP/playerdata.yml`
- Stores: attribute, level, kills, deaths
- Auto-save on modification
- Auto-load on join

---

## Summary Statistics

| Category | Count |
|----------|-------|
| Total Attributes | 17 |
| Standard Attributes | 13 |
| Boss Attributes | 4 |
| Total Weapons | 17 |
| Standard Weapons | 13 |
| Boss Weapons | 4 |
| Max Level | 5 |
| Base Cooldowns | 120s - 300s |
| Admin Commands | 15+ |
| Player Commands | 8+ |
| Config Options | 50+ |
