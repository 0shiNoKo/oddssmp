# OddsSMP Plugin - Changelog

All notable changes to the OddsSMP plugin are documented in this file.

---

## [Unreleased] - 2026-02-23

### Abilities
- **Change Vision support tracking to distance-based ending**
  - Tracking now continues until tracker is within 5 chunks (80 blocks) of target
  - No longer time-limited; ends on proximity, target going offline, or tracker going offline

### Bug Fixes
- **Fix 14 compilation errors across 3 files** (`f7aa5f5`)
  - AbilityManager.java: non-effectively-final variable in inner class
  - AdminGUI.java: 11 createItem() calls using varargs instead of List
  - AscendedWither.java: removed particle methods replaced with inline spawning

### Particle System
- **Rewrite particle system with strict global limits and tracking** (`4925290`)
  - Hard cap: 25 active particles per player, 12 max per trigger
  - Lifetime: 4-8 ticks with automatic cleanup
  - Radius: 0.5 blocks max, 16 block render distance
  - Pulse interval: minimum 15 ticks between same effect
  - Per-player tracking with `activeParticles` and `lastPulseTimes` maps
  - All ability effects: MELEE (Power Strike, Battle Fervor, Berserk), DEFENSE (Iron Skin, Fortify Aura, Last Stand), RANGED (Piercing Shot, Volley), MAGIC (Arcane Surge, Mana Shield, Blink), WEALTH (Plunder Kill, Treasure Sense), UTILITY (Speed Boost, Stealth, Regeneration), CONTROL (Freeze, Shock)
  - Legacy compatibility with `playSupportParticles`/`playMeleeParticles`/`playPassiveParticles`

### Altar System
- **Add comprehensive altar settings GUI with per-altar and global controls** (`950110c`)
  - Creative mode shift-right-click opens settings GUI for any altar
  - Per-altar settings: base block, weapon size, rotation speed, particle type/count, toggles
  - Global settings: shared defaults that can be applied to all altars
  - List All Altars GUI with edit, teleport, and delete actions
  - Apply Global to All button
- **Add /admin listaltars command to show all active altars** (`c9b00dd`)
  - Lists altar number, weapon name, coordinates, and world
- **Add altar removal: /admin removealtars command and creative+stick** (`486a4d7`)
  - `/admin removealtars` removes all active weapon altars at once
  - Creative mode + stick right-click removes individual altars

### Abilities
- **Implement Vision support player tracking GUI** (`64b058f`)
  - Vision support now opens a GUI to select which player to track
  - Shows all online players with distance and world info
  - During tracking: action bar displays target name, distance, compass direction (N/NE/E/SE/S/SW/W/NW)
  - Applies Glowing effect to tracked player for duration
- **Fix Tempo stun, set Overdrive to 15s, lock weapons to matching attribute** (`3bdd47b`)
  - Tempo melee stun now fully blocks movement (PlayerMoveEvent cancelled) and attacks (damage cancelled)
  - Tempo support Overdrive changed from 5s+level to flat 15 seconds
  - Weapons now deal 0 damage if wielder doesn't have the matching attribute

### Boss Drops
- **Fix weapon crafting: add missing material feedback and Dragon Heart drops** (`bb00378`)
  - Added `getMissingMaterials()` method showing exactly what's missing when crafting fails
  - Dragon Heart now drops from both Ascended and normal Ender Dragon
- **Add attribute item drops for normal Warden, Wither, and Ender Dragon** (`c113130`)
  - Normal Warden drops Warden Brain + Warden's Heart
  - Normal Wither drops Wither Bone
  - Normal Ender Dragon drops Dragon Egg + Dragon Heart
  - All handlers skip Ascended variants (custom name check)
- **Fix Wither Bone not granting attribute, add Warden's Heart drop** (`8515378`)
  - Fixed Wither Bone material: changed `isWitherBone()` check from COAL_BLOCK to BONE
  - Added Warden's Heart drop to normal Warden death handler

---

## [1.0.0] - Initial Documentation Baseline

### Documentation
- **Update plugin documentation with exhaustive coverage of all systems** (`6490258`)
- **Add comprehensive plugin documentation** (`0d50af2`)

### Balance
- **Nerf Range passive to 20-25%, rework Transfer melee to sacrifice/corrupt** (`14c64c7`)
- **Standardize all weapon stats: 15 dmg/1.6 speed, boss 16 dmg/1.6 speed** (`121e9b4`)
- **Balance Dragon Egg, Control, and Disruption abilities** (`372003b`)

### Abilities
- **Make support abilities self-only, remove team system** (`d277e7f`)
- **Implement missing abilities, fix bugs, add team system** (`d218c94`)
- **Remove Persistence and Anchor attributes entirely** (`095e160`)
- **Update ability implementations to match new specs** (`afc3ba7`)
- **Update all attribute descriptions to match current ability specs** (`fd88eb6`)

### GUI & Commands
- **Update /smp info to use GUI for attribute and player lookups** (`fa4bc07`)
- **Add /smp info <attribute> and /smp info <player> commands** (`d68aa5a`)
- **Fix AdminGUI to use base cooldown instead of removed tier cooldowns** (`313e77d`)
- **Add slot animation to reroller and prevent same attribute** (`cb6ae98`)

### System Changes
- **Remove tier system completely from the plugin** (`2cdeb6c`)
- **Overhaul attribute system with updated abilities, tiers, and removed attributes** (`7bfd22c`)
- **Change Transfer melee ability to 100% effect steal** (`3535af8`)
- **Add missing AbilityFlags fields** (`98b73b3`, `5646011`, `0073eae`)

### Settings & Configuration
- **Add comprehensive plugin settings GUI** (`9740b86`)
- **Add comprehensive combat logging system with GUI configuration** (`ad690c9`)
- **Add comprehensive particle settings with dedicated GUI submenu** (`c8f42a8`)
- **Fix particle intensity setting to actually work** (`8f372b4`)
- **Fix support abilities to also affect the caster** (`f7e8c1b`)

### Weapons & Altars
- **Add /admin customitems GUI for accessing all custom items** (`2dd9b6c`)
- **Reduce weapon display size to 1.75x** (`4deb65b`)
- **Fix passive effects not triggering consistently** (`47389ec`)
- **Implement missing passive effects for attributes** (`be0ff7a`)
- **Update ability descriptions and cooldowns to match specs** (`5b2c5f3`)

### Display & Visuals
- **Use Display Entities for perfectly centered altar display** (`ef4571b`)
- **Make weapon display much bigger (2.5x) with rotation** (`4dffba6`)
- **Make text face player, use solid Ancient Debris block** (`abb752e`)
- **Prevent attribute weapons from being enchanted** (`52f4d04`)
- **Add Warden's Heart drop to Ascended Warden** (`ed2137a`)

### Infrastructure
- **Use DecentHolograms for weapon display and center hologram** (`9e4cb5d`)
- **Add DecentHolograms support for weapon altar displays** (`039f885`)
- **Add pom.xml with DecentHolograms dependency** (`0af85f4`)
- **Fix attribute bonuses persisting after switching attributes** (`944a53f`)

### Early Development
- **Add 30+ specialized particle effect methods** (`8cdc258`)
- **Add sound effects for all abilities** (`715a29f`)
- **Add Ascended boss system with Wither, Warden, and Breeze bosses** (`df2454a`)
- **Add dragon egg-like logic for all boss attribute items** (`de81f06`)
- **Add auto-assign feature for automatic attribute assignment on join** (`f6bf5e7`)
- **Add file persistence for player data across server restarts** (`242e64c`)
- **Add /admin boss command for all boss types** (`955b835`)
- **Add Upgrader and Reroller crafting recipes** (`fa1e132`)
- **Add attribute weapon system with /admin weapon GUI** (`3bd4e20`)
- **Add weapon crafting altar system with /admin spwe command** (`c99b126`)
- **Add /admin giveupgrader and givereroller commands** (`5c699a3`)

### Initial Setup
- **Initial commit** (`08f42af`)
- **Fix ClassNotFoundException by correcting main class path typo** (`b7a7914`)
- **Fix main class name: OddSMP -> OddsSMP** (`ea8c555`)
- **Convert to Bukkit plugin format for command support** (`d0cee51`)
- **Dragon egg system** (`577d455` - `ab1a044`): pickup, inventory protection, death drops, join detection
- **End exit portal generation** (`f532c97` - `5da486a`): portal structure fixes
