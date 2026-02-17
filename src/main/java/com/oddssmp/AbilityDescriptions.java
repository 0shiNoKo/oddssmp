package com.oddssmp;

public class AbilityDescriptions {

    public static String[] getDescription(AttributeType attribute, String abilityType) {
        String key = attribute.name() + "_" + abilityType.toUpperCase();

        switch (key) {
            // MELEE
            case "MELEE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Power Strike",
                        "§7Next melee hit against a PLAYER:",
                        "§7Negates 40% of opponent's armor",
                        "§7(+1% per level, max 45%)"
                };
            case "MELEE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Battle Fervor",
                        "§7Allies within 3 blocks gain:",
                        "§7+15% melee damage",
                        "§7Duration: 6s +1s per level (max 11s)",
                        "§7Cooldown: 150s"
                };
            case "MELEE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Bloodlust",
                        "§7Each PvP kill: +2% melee damage",
                        "§7Max stacks: 10% +2% per level (max 20%)",
                        "§7Lost on death"
                };

            // HEALTH
            case "HEALTH_MELEE":
                return new String[]{
                        "§c§lMelee: §7Vampiric Hit",
                        "§7Next melee hit against a PLAYER:",
                        "§7Heals for 50% of damage dealt for 5s",
                        "§7+1s per level (max 10s)",
                        "§7Overheal converts to absorption"
                };
            case "HEALTH_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Fortify",
                        "§7Allies within 6 blocks gain:",
                        "§7+2 max hearts (+1 per level, max +7)",
                        "§7Duration: 15s",
                        "§7Cooldown: 120s"
                };
            case "HEALTH_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Vitality",
                        "§7Each kill: +1 heart (+1 per level)",
                        "§7Level 5 = +5 hearts",
                        "§7Lose hearts on death"
                };

            // DEFENSE
            case "DEFENSE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Iron Response",
                        "§7Next melee hit:",
                        "§7Reduces damage taken by 35% for 4s",
                        "§7+0.5s per level (max 6s)"
                };
            case "DEFENSE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Shield Wall",
                        "§7Allies within 6 blocks:",
                        "§7Take 20% less damage (+5% per level)",
                        "§7Max 40% at level 5",
                        "§7Duration: 8s"
                };
            case "DEFENSE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Hardened",
                        "§7Permanent damage reduction: 5%",
                        "§7+1% per level (max 10%)",
                        "§7Does not stack with Resistance"
                };

            // WEALTH
            case "WEALTH_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Economic Surge",
                        "§7For 20s (+2s per level, max 30s):",
                        "§7• Villager trades: 100% discount",
                        "§7• Fortune X"
                };
            case "WEALTH_MELEE":
                return new String[]{
                        "§c§lMelee: §7Plunder Kill",
                        "§7Next mob KILL (non-player, non-boss):",
                        "§7Drops: 2.0-3.0× (scales with level)",
                        "§7Looting +3, rare drops +1 roll"
                };
            case "WEALTH_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Industrialist",
                        "§7Ore mining: +1 Fortune per level (max +5)",
                        "§7Mob drops: +10% per level",
                        "§7Hero of the Village XII permanent"
                };

            // CONTROL
            case "CONTROL_MELEE":
                return new String[]{
                        "§c§lMelee: §7Disrupt",
                        "§7Next melee hit:",
                        "§7Applies Slowness III",
                        "§73s +1s per level (max 8s)"
                };
            case "CONTROL_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Lockdown",
                        "§7Enemies within 6 blocks:",
                        "§7Cannot use abilities",
                        "§7Duration: 5s +1s per level (max 10s)"
                };
            case "CONTROL_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Suppression",
                        "§7Players you hit:",
                        "§7+10s ability cooldown (once per fight)",
                        "§7+1s per level (max 15s)"
                };

            // RANGE
            case "RANGE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Spacing Strike",
                        "§7Next melee hit:",
                        "§7Knocks back and prevents approach",
                        "§73s +1s per level (max 8s)"
                };
            case "RANGE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Zone Control",
                        "§7Allies gain homing arrows",
                        "§7Duration: 5s +1s per level (max 10s)"
                };
            case "RANGE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Footwork",
                        "§7Enemies you hit:",
                        "§7Cannot sprint toward you",
                        "§75s +1s per level (max 10s)"
                };

            // TEMPO
            case "TEMPO_MELEE":
                return new String[]{
                        "§c§lMelee: §7Tempo Strike",
                        "§7Next melee hit:",
                        "§7Applies Slowness",
                        "§7+60s to their cooldowns",
                        "§7Duration: 3s +1s per level (max 8s)"
                };
            case "TEMPO_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Overdrive",
                        "§7Allies gain:",
                        "§7+20% attack speed (+5% per level)",
                        "§7Max 45% at level 5",
                        "§7Duration: 8s"
                };
            case "TEMPO_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Momentum",
                        "§7Each hit: +2% attack speed",
                        "§7Max: 10% +2% per level (max 20%)",
                        "§7Lost on death"
                };

            // VISION
            case "VISION_MELEE":
                return new String[]{
                        "§c§lMelee: §7Target Mark",
                        "§7Next melee hit:",
                        "§7• Reveals through walls",
                        "§7• Shows cooldown states",
                        "§7• +20% damage from all sources",
                        "§76s +1s per level (max 11s)"
                };
            case "VISION_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7True Sight",
                        "§7Allies can see invisible players",
                        "§7Duration: 5s +1s per level (max 10s)"
                };
            case "VISION_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Awareness",
                        "§7See all enemies around you (glowing)"
                };

            // PERSISTENCE
            case "PERSISTENCE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Stored Pain",
                        "§7For 4s (+1s per level, max 8s):",
                        "§725% of damage taken is stored",
                        "§7At end: stored damage unleashed",
                        "§7to last enemy hit"
                };
            case "PERSISTENCE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Last Stand",
                        "§7Allies cannot drop below 1 heart",
                        "§7Duration: 2s +0.5s per level (max 4.5s)"
                };
            case "PERSISTENCE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Endure",
                        "§7Below 50% HP:",
                        "§7+10% damage resistance",
                        "§7+5% per level (max 35%)"
                };

            // SPEED
            case "SPEED_MELEE":
                return new String[]{
                        "§c§lMelee: §7Flash Step",
                        "§7Next melee hit:",
                        "§7Dash through target",
                        "§7+30% movement & attack speed",
                        "§73s +1s per level (max 8s)"
                };
            case "SPEED_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Rapid Formation",
                        "§7Allies within 6 blocks:",
                        "§7+20% movement speed",
                        "§7+15% attack speed (+5% per level, max 40%)",
                        "§7Duration: 6s"
                };
            case "SPEED_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Adrenaline",
                        "§7Each hit: +2% movement speed",
                        "§7Max: 10% +2% per level (max 20%)",
                        "§7Decays after 6s without combat"
                };

            // PRESSURE
            case "PRESSURE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Crushing Blow",
                        "§7Next melee hit:",
                        "§7+25% damage",
                        "§7Vulnerability: +15% damage taken",
                        "§74s +1s per level (max 9s)"
                };
            case "PRESSURE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Intimidation Field",
                        "§7Enemies within 6 blocks:",
                        "§7-15% damage dealt (+5% per level, max -35%)",
                        "§7+10% damage taken (+5% per level, max +30%)",
                        "§7Duration: 6s"
                };
            case "PRESSURE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Oppression",
                        "§7Enemies below 50% HP near you:",
                        "§7Take +10% damage",
                        "§7+3% per level (max 25%)"
                };

            // DISRUPTION
            case "DISRUPTION_MELEE":
                return new String[]{
                        "§c§lMelee: §7Fracture",
                        "§7Next melee hit:",
                        "§7+20s to all their cooldowns",
                        "§7Weakness II: 4s +1s per level (max 9s)"
                };
            case "DISRUPTION_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7System Jam",
                        "§7Enemies within 6 blocks:",
                        "§7Cannot activate abilities",
                        "§73s +1s per level (max 8s)",
                        "§7Cooldown: 150s"
                };
            case "DISRUPTION_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Desync",
                        "§7First hit against player each fight:",
                        "§7+10s to their next ability cooldown",
                        "§7+2s per level (max +20s)"
                };

            // ANCHOR
            case "ANCHOR_MELEE":
                return new String[]{
                        "§c§lMelee: §7Pin",
                        "§7Next melee hit:",
                        "§7Prevents sprinting, jumping, and knockback",
                        "§74s +1s per level (max 9s)",
                        "§7Cooldown: 120s"
                };
            case "ANCHOR_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Hold The Line",
                        "§7Allies within 6 blocks:",
                        "§7+25% knockback resistance",
                        "§7+20% damage reduction",
                        "§7(+5% per level, max 45%)",
                        "§7Duration: 6s"
                };
            case "ANCHOR_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Immobile",
                        "§7While standing still for 2s:",
                        "§7+10% damage resistance",
                        "§7+5% per level (max 35%)",
                        "§7Lost when moving"
                };

            // RISK
            case "RISK_MELEE":
                return new String[]{
                        "§c§lMelee: §7All In",
                        "§7Next melee hit:",
                        "§7+50% damage (+10% per level, max +90%)",
                        "§7You take +25% damage (-2% per level)"
                };
            case "RISK_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Double Or Nothing",
                        "§7Allies within 6 blocks:",
                        "§7+30% damage (+5% per level, max +55%)",
                        "§7+20% damage taken",
                        "§7Duration: 6s",
                        "§7Cooldown: 150s"
                };
            case "RISK_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Gambler's Edge",
                        "§7Below 40% HP:",
                        "§7+20% damage (+5% per level, max 45%)"
                };

            // TRANSFER
            case "TRANSFER_MELEE":
                return new String[]{
                        "§c§lMelee: §7Effect Swap",
                        "§7Next melee hit:",
                        "§7Steals 100% of opponent's",
                        "§7beneficial potion effects"
                };
            case "TRANSFER_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Redirection",
                        "§7Allies: Damage split between all",
                        "§7players in radius",
                        "§7Radius: 5 blocks +1 per level"
                };
            case "TRANSFER_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Immunity",
                        "§7Cannot receive debuffs"
                };

            // WITHER
            case "WITHER_MELEE":
                return new String[]{
                        "§c§lMelee: §5Desperation Cleave",
                        "§7Cone attack (6-8 blocks)",
                        "§7Base: 12 damage (+1.2 per level)",
                        "§7Scales with LOW HP (up to 2.0× at <20% HP)",
                        "§7Armor pen: 20% (+5% per level)",
                        "§7Wither II: 4s (+0.5s per level)",
                        "§7Enemy healing: -40% (-5% per level)"
                };
            case "WITHER_SUPPORT":
                return new String[]{
                        "§a§lSupport: §5Shadow Pulse",
                        "§7AoE 6 blocks: 10 damage (5 hearts)",
                        "§7Slowness II for 3s",
                        "§7Cooldown: 240s"
                };
            case "WITHER_PASSIVE":
                return new String[]{
                        "§b§lPassive: §c§lCurse of Despair",
                        "§c• Healing received: -25% (+1% per level)",
                        "§c• Melee cooldown: +10s (-1s per level)",
                        "§7Permanent curse debuff"
                };

            // WARDEN
            case "WARDEN_MELEE":
                return new String[]{
                        "§c§lMelee: §3Sonic Slam",
                        "§7AoE 5 blocks: 14 damage (7 hearts)",
                        "§7Fear: -movement & attack speed (3s)",
                        "§7Cooldown: 240s"
                };
            case "WARDEN_SUPPORT":
                return new String[]{
                        "§a§lSupport: §3Deep Dark Zone",
                        "§7Radius: 12-16 blocks (scales with level)",
                        "§7Duration: 8-12s",
                        "§7Enemies: No sprint, -50% jump,",
                        "§7  -30% attack speed, -50% healing",
                        "§7Allies: +25% damage reduction,",
                        "§7  +50% knockback resistance",
                        "§7You: +15% melee damage in zone",
                        "§7Cooldown: 180s"
                };
            case "WARDEN_PASSIVE":
                return new String[]{
                        "§b§lPassive: §c§lCurse of Silence",
                        "§c• Attack speed: -15% when not using abilities",
                        "§7Permanent curse debuff"
                };

            // BREEZE
            case "BREEZE_MELEE":
                return new String[]{
                        "§c§lMelee: §eVerdict Strike",
                        "§7Next hit applies Judged (10s):",
                        "§7• Target takes +12% damage (+2% per level)",
                        "§7• If target misses: 6 true damage",
                        "§7  (+1 per level)",
                        "§7Cooldown: 240s"
                };
            case "BREEZE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §eTrial Order",
                        "§7AoE: 8 blocks (+1 per level)",
                        "§7Duration: 6s (+1 per level)",
                        "§7Allies: -20% cooldown, +10% damage reduction",
                        "§7Cooldown: 240s"
                };
            case "BREEZE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §c§lCurse of Judgment",
                        "§c• Abilities: +1s cooldown/10s if no enemy nearby",
                        "§c• Healing received: -25%",
                        "§c• Movement speed: -5% when not attacking",
                        "§7Permanent curse debuff"
                };

            // DRAGON EGG
            case "DRAGON_EGG_MELEE":
                return new String[]{
                        "§c§lMelee: §6§lRampaging Strike",
                        "§7Base: 10 damage (+2 per level, max 18)",
                        "§7Lifesteal: 20% (+5% per level, max 40%)",
                        "§7Slowness III: 3s (+0.5s per level)",
                        "§7Weakness II: 3s (+0.5s per level)",
                        "§7Below 30% HP: +50% bonus damage"
                };
            case "DRAGON_EGG_SUPPORT":
                return new String[]{
                        "§a§lSupport: §6§lDominion",
                        "§7For 8s, allies in 6 blocks:",
                        "§7• +25% damage (+1% per level, max 30%)",
                        "§7• 50% cooldown reduction",
                        "§7  (+1% per level, max 55%)"
                };
            case "DRAGON_EGG_PASSIVE":
                return new String[]{
                        "§b§lPassive: §6§lDraconic Curse",
                        "§7Nearby enemies take 15% more damage",
                        "§7(-1% per level, min 10%)",
                        "§7Always active"
                };

            default:
                return new String[]{"§7No description available."};
        }
    }
}