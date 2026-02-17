package com.oddssmp;

public class AbilityDescriptions {

    public static String[] getDescription(AttributeType attribute, String abilityType) {
        String key = attribute.name() + "_" + abilityType.toUpperCase();

        switch (key) {
            // MELEE
            case "MELEE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Power Strike",
                        "§7Next hit vs player ignores §e25% §7armor",
                        "§7+1% per level, Level 5: §e30%",
                        "§7Cooldown: §e120s"
                };
            case "MELEE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Battle Fervor",
                        "§7Deal §e+15% §7melee damage",
                        "§7Duration: §e6s §7+1s/level (max 11s)",
                        "§7Cooldown: §e150s"
                };
            case "MELEE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Bloodlust",
                        "§7Each PvP kill: §e+1.5% §7melee damage",
                        "§7Max: §e10% §7+1.5%/level (Lv5 = §e15%§7)",
                        "§c✗ Lose all stacks on death"
                };

            // HEALTH
            case "HEALTH_MELEE":
                return new String[]{
                        "§c§lMelee: §7Vampiric Hit",
                        "§7Next hit heals §e15% §7of damage dealt",
                        "§75s, §e+1%/level §7(max §e20%§7)",
                        "§7Overheal converts to absorption",
                        "§7Cooldown: §e120s"
                };
            case "HEALTH_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Fortify",
                        "§7Heal §e3 hearts",
                        "§7+0.5 heart/level (max §e5.5 §7heal)",
                        "§7Cooldown: §e120s"
                };
            case "HEALTH_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Vitality",
                        "§7Permanent max health per PvP kill",
                        "§e+1 heart §7per level (Lv5 = §e+5§7)",
                        "§c✗ Lose 1 heart per level on death"
                };

            // DEFENSE
            case "DEFENSE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Iron Response",
                        "§7Damage taken reduced by §e20%",
                        "§7Duration: §e4s §7+0.5s/level (max 6s)",
                        "§7Cooldown: §e120s"
                };
            case "DEFENSE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Shield Wall",
                        "§7Gain §e4 §7absorption hearts",
                        "§7+0.5/level (max §e16 §7total hearts)",
                        "§7Duration: §e8s",
                        "§7Cooldown: §e120s"
                };
            case "DEFENSE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Hardened",
                        "§7Armor breaks §e5% §7slower",
                        "§7+1% per level"
                };

            // WEALTH
            case "WEALTH_MELEE":
                return new String[]{
                        "§c§lMelee: §7Plunder Kill",
                        "§7Disables held item of target",
                        "§7(e.g. sword = can't hit or use)",
                        "§7Duration: §e7s §7+1s/level",
                        "§7Cooldown: §e120s"
                };
            case "WEALTH_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Economic Surge",
                        "§7• Villager prices reduced §e100%",
                        "§7• Fortune §eVII",
                        "§7Duration: §e20s §7+2s/level (max 30s)",
                        "§7Cooldown: §e120s"
                };
            case "WEALTH_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Industrialist",
                        "§7Mining: §e+1 Fortune §7per level",
                        "§7Mob drops: §e+10% §7per level",
                        "§7Permanent §eHero of the Village XII"
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
                        "§7Knockbacks the target",
                        "§7Duration: §e3s §7+1s/level (max 8s)",
                        "§7Cooldown: §e120s"
                };
            case "RANGE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Zone Control",
                        "§7Allies gain §ehoming arrows",
                        "§7Duration: §e5s §7+1s/level",
                        "§7Cooldown: §e120s"
                };
            case "RANGE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Footwork",
                        "§7Bows and crossbows deal §e45% §7more damage",
                        "§7+1%/level (max §e50%§7)"
                };

            // TEMPO
            case "TEMPO_MELEE":
                return new String[]{
                        "§c§lMelee: §7Tempo Strike",
                        "§7Stuns target (can't move or look)",
                        "§7Duration: §e5s §7+1s/level",
                        "§7Cooldown: §e120s"
                };
            case "TEMPO_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Overdrive",
                        "§7Gain §eHaste V",
                        "§7Duration: §e5s §7+1s/level",
                        "§7Cooldown: §e120s"
                };
            case "TEMPO_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Momentum",
                        "§7Permanent §eSpeed I"
                };

            // VISION
            case "VISION_MELEE":
                return new String[]{
                        "§c§lMelee: §7Target Mark",
                        "§7Apply glowing to hit enemy",
                        "§7Duration: §e5m §7+30s/level (max 7.5m)",
                        "§7Cooldown: §e120s"
                };
            case "VISION_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7True Sight",
                        "§7Select a player (opens GUI)",
                        "§7Track their location",
                        "§7Duration: §e5s §7+1s/level (max 10s)",
                        "§7Cooldown: §e120s"
                };
            case "VISION_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Awareness",
                        "§7Players within §e12 blocks §7glow",
                        "§7(only visible to you)"
                };

            // TRANSFER
            case "TRANSFER_MELEE":
                return new String[]{
                        "§c§lMelee: §7Effect Swap",
                        "§7Steals §e20% §7of opponent's potion effects",
                        "§7Adds onto your potions",
                        "§7+1%/level (max §e25%§7)",
                        "§7Cooldown: §e120s"
                };
            case "TRANSFER_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Redirection",
                        "§7Reflects durability damage to attacker",
                        "§7Duration: §e5s §7+1s/level (max 10s)",
                        "§7Cooldown: §e120s"
                };
            case "TRANSFER_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Cleanse",
                        "§7Immune to §edebuffs"
                };

            // SPEED
            case "SPEED_MELEE":
                return new String[]{
                        "§c§lMelee: §7Flash Step",
                        "§7Summons §elightning §7on the opponent",
                        "§7Duration: §e5s §7+1s/level",
                        "§7Cooldown: §e120s"
                };
            case "SPEED_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Rapid Formation",
                        "§7Gain §eSpeed III",
                        "§7Duration: §e6s",
                        "§7Cooldown: §e120s"
                };
            case "SPEED_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Adrenaline",
                        "§7Boosted jump in look direction",
                        "§7Cooldown: §e20s §7-1s/level",
                        "§7Activate: jump, then jump again mid-air"
                };

            // PRESSURE
            case "PRESSURE_MELEE":
                return new String[]{
                        "§c§lMelee: §7Crushing Blow",
                        "§7Deal §e+25% §7bonus damage",
                        "§7Target takes §e+15% §7damage",
                        "§7Duration: §e4s §7+1s/level (max 9s)",
                        "§7Cooldown: §e120s"
                };
            case "PRESSURE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Intimidation Field",
                        "§7Enemies within 6 blocks:",
                        "§7§e-15% §7damage dealt (+5%/level, max §e-35%§7)",
                        "§7§e+10% §7damage taken (+5%/level, max §e+30%§7)",
                        "§7Duration: §e6s"
                };
            case "PRESSURE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Oppression",
                        "§7Enemies below 50% HP near you:",
                        "§7Take §e+10% §7damage",
                        "§7+3%/level (max §e25%§7)"
                };

            // DISRUPTION
            case "DISRUPTION_MELEE":
                return new String[]{
                        "§c§lMelee: §7Fracture",
                        "§7Adds §e+20s §7to all their cooldowns",
                        "§7Applies §eWeakness II§7, Blindness, Nausea",
                        "§7Duration: §e4s §7+1s/level (max 9s)",
                        "§7Cooldown: §e120s"
                };
            case "DISRUPTION_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7System Jam",
                        "§7Enemies cannot activate abilities",
                        "§7Duration: §e25s §7+1s/level (max 30s)",
                        "§7Cooldown: §e150s"
                };
            case "DISRUPTION_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Desync",
                        "§7First hit per fight:",
                        "§7+§e15s §7to both opponent cooldowns",
                        "§7+2s/level (max §e+20s§7)"
                };

            // RISK
            case "RISK_MELEE":
                return new String[]{
                        "§c§lMelee: §7All In",
                        "§7Deal §e+50% §7damage (+10%/level)",
                        "§7You take §c+25% §7damage (-2%/level)",
                        "§7Cooldown: §e120s"
                };
            case "RISK_SUPPORT":
                return new String[]{
                        "§a§lSupport: §7Double Or Nothing",
                        "§7Allies deal §e+30% §7damage (+5%/level, max §e55%§7)",
                        "§7Allies take §c+20% §7damage",
                        "§7Duration: §e6s",
                        "§7Cooldown: §e150s"
                };
            case "RISK_PASSIVE":
                return new String[]{
                        "§b§lPassive: §7Gambler's Edge",
                        "§7Below §e40% §7HP:",
                        "§7+§e10% §7damage (+1%/level, max §e15%§7)",
                        "§7Disabled above 40% HP"
                };

            // WITHER
            case "WITHER_MELEE":
                return new String[]{
                        "§c§lMelee: §5Desperation Cleave",
                        "§7Base: §e12 §7damage (+1.2/level)",
                        "§7Cone: §e6-8 §7blocks",
                        "§7Armor pen: §e20% §7(+5%/level)",
                        "§7Wither II: §e4s §7(+0.5s/level)",
                        "§7Enemy healing: §c-40% §7(+5%/level)",
                        "§7Cooldown: §e240s"
                };
            case "WITHER_SUPPORT":
                return new String[]{
                        "§a§lSupport: §5Shadow Pulse",
                        "§7AoE §e6 §7blocks: §e10 §7damage (5 hearts)",
                        "§7Slowness II for §e3s",
                        "§7Cooldown: §e240s"
                };
            case "WITHER_PASSIVE":
                return new String[]{
                        "§b§lPassive: §c§lCurse of Despair",
                        "§c• Healing received: -25% §7(+1%/level)",
                        "§c• Melee cooldown: +10s §7(-1s/level)",
                        "§7Permanent curse debuff"
                };

            // WARDEN
            case "WARDEN_MELEE":
                return new String[]{
                        "§c§lMelee: §3Sonic Slam",
                        "§7AoE §e5 §7blocks: §e14 §7damage (7 hearts)",
                        "§7Fear: -movement & attack speed (§e3s§7)",
                        "§7Cooldown: §e240s"
                };
            case "WARDEN_SUPPORT":
                return new String[]{
                        "§a§lSupport: §3Deep Dark Zone",
                        "§7Radius: §e12-16 §7blocks (scales w/ level)",
                        "§7Duration: §e8-12s",
                        "§7Enemies: no sprint, -50% jump,",
                        "§7  -30% atk speed, -50% healing",
                        "§7Allies: +25% dmg reduction,",
                        "§7  +50% KB resistance",
                        "§7You: +15% melee damage in zone",
                        "§7Cooldown: §e180s"
                };
            case "WARDEN_PASSIVE":
                return new String[]{
                        "§b§lPassive: §c§lCurse of Silence",
                        "§c• Attack speed: -15% when idle",
                        "§7Permanent curse debuff"
                };

            // BREEZE
            case "BREEZE_MELEE":
                return new String[]{
                        "§c§lMelee: §eJudging Strike",
                        "§7Target takes §e+12% §7damage (+2%/level)",
                        "§7If target misses: §e6 §7true damage (+1/level)",
                        "§7Cooldown: §e240s"
                };
            case "BREEZE_SUPPORT":
                return new String[]{
                        "§a§lSupport: §eTrial Order",
                        "§7Allies gain cooldown & damage reduction",
                        "§7Radius: §e8 §7blocks (+1/level)",
                        "§7Duration: §e6s §7(+1s/level)",
                        "§7Cooldown: §e240s"
                };
            case "BREEZE_PASSIVE":
                return new String[]{
                        "§b§lPassive: §c§lCurse of Judgment",
                        "§c• Cooldowns increase out of combat",
                        "§c• Healing received: -25%",
                        "§c• Movement speed: -5% when idle",
                        "§7Permanent curse debuff"
                };

            // DRAGON EGG
            case "DRAGON_EGG_MELEE":
                return new String[]{
                        "§c§lMelee: §6§lRampaging Strike",
                        "§7Base: §e10 §7damage (+2/level, max 18)",
                        "§7Lifesteal: §e20% §7(+5%/level, max 40%)",
                        "§7Slowness III + Weakness II: §e3s §7(+0.5s/level)",
                        "§7Below 30% HP: §e+50% §7bonus damage",
                        "§7Cooldown: §e300s"
                };
            case "DRAGON_EGG_SUPPORT":
                return new String[]{
                        "§a§lSupport: §6§lDominion",
                        "§7Allies gain §e+25% §7damage (+1%/level, max 30%)",
                        "§7§e50% §7cooldown reduction (+1%/level, max 55%)",
                        "§7Duration: §e8s",
                        "§7Cooldown: §e300s"
                };
            case "DRAGON_EGG_PASSIVE":
                return new String[]{
                        "§b§lPassive: §6§lDraconic Curse",
                        "§7Nearby enemies take §e15% §7more damage",
                        "§7(-1%/level, min 10%)",
                        "§7Always active"
                };

            default:
                return new String[]{"§7No description available."};
        }
    }
}
