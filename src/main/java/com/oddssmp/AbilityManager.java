package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AbilityManager {

    private final OddsSMP plugin;
    private final Map<UUID, AbilityFlags> abilityFlags;

    public AbilityManager(OddsSMP plugin) {
        this.plugin = plugin;
        this.abilityFlags = new HashMap<>();
    }

    /**
     * Activate Support ability
     */
    public void activateSupport(Player player) {
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            player.sendMessage("§cYou don't have an attribute assigned!");
            return;
        }

        String cooldownKey = "support";
        if (data.isOnCooldown(cooldownKey)) {
            long remaining = data.getRemainingCooldown(cooldownKey) / 1000;
            player.sendMessage("§cSupport ability on cooldown: " + remaining + "s");
            return;
        }

        // Get cooldown based on attribute type
        int baseCooldown = getSupportCooldown(data.getAttribute());
        data.setCooldown(cooldownKey, baseCooldown * 1000L);

        // Get nearby allies
        List<Player> allies = getNearbyAllies(player, getSupportRadius(data.getAttribute()));

        // Play particles and sound
        ParticleManager.playSupportParticles(player, data.getAttribute(), data.getTier(), data.getLevel());
        playSupportSound(player, data.getAttribute());

        // Apply support effects
        applySupportEffect(player, allies, data);

        player.sendMessage("§a" + data.getAttribute().getDisplayName() + " Support activated!");
    }

    /**
     * Activate Melee ability (stored for next hit)
     */
    public void activateMelee(Player player) {
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            player.sendMessage("§cYou don't have an attribute assigned!");
            return;
        }

        String cooldownKey = "melee";
        if (data.isOnCooldown(cooldownKey)) {
            long remaining = data.getRemainingCooldown(cooldownKey) / 1000;
            player.sendMessage("§cMelee ability on cooldown: " + remaining + "s");
            return;
        }

        // Get cooldown based on attribute type
        int baseCooldown = getMeleeCooldown(data.getAttribute());

        // Apply Wither curse cooldown penalty
        if (data.getAttribute() == AttributeType.WITHER) {
            int penalty = 10 - data.getLevel(); // 10s at L1, 6s at L5
            baseCooldown += penalty;
        }

        data.setCooldown(cooldownKey, baseCooldown * 1000L);

        // Set flag for next hit
        getAbilityFlags(player.getUniqueId()).meleeReady = true;

        player.sendMessage("§e" + data.getAttribute().getDisplayName() + " Melee ready! Hit an enemy!");
    }

    /**
     * Apply melee effect on hit
     */
    public void applyMeleeOnHit(Player attacker, Entity target) {
        AbilityFlags flags = getAbilityFlags(attacker.getUniqueId());
        if (!flags.meleeReady) return;

        flags.meleeReady = false;

        PlayerData data = plugin.getPlayerData(attacker.getUniqueId());
        if (data == null || data.getAttribute() == null) return;

        // Play particles and sound
        ParticleManager.playMeleeParticles(attacker, target, data.getAttribute(), data.getTier());
        playMeleeSound(attacker, data.getAttribute());

        // Apply melee effects
        applyMeleeEffect(attacker, target, data);

        attacker.sendMessage("§e" + data.getAttribute().getDisplayName() + " Melee effect applied!");
    }

    /**
     * Get support cooldown for attribute
     */
    private int getSupportCooldown(AttributeType attr) {
        switch (attr) {
            case MELEE:
                return 150;
            case WITHER:
                return 240;
            case WARDEN:
                return 180;
            case BREEZE:
            case DRAGON_EGG:
                return 300;
            default:
                return 120;
        }
    }

    /**
     * Get melee cooldown for attribute
     */
    private int getMeleeCooldown(AttributeType attr) {
        switch (attr) {
            case WITHER:
            case BREEZE:
                return 240;
            case DRAGON_EGG:
                return 300;
            default:
                return 120;
        }
    }

    /**
     * Get support radius for attribute
     */
    private double getSupportRadius(AttributeType attr) {
        switch (attr) {
            case MELEE:
                return 3.0;
            default:
                return 6.0;
        }
    }

    /**
     * Play sound for support ability activation
     */
    private void playSupportSound(Player player, AttributeType attr) {
        switch (attr) {
            case MELEE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.2f);
                break;
            case HEALTH:
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
                break;
            case DEFENSE:
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.8f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.2f);
                break;
            case WEALTH:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
                break;
            case SPEED:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.5f);
                break;
            case CONTROL:
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.8f, 1.5f);
                break;
            case RANGE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.5f);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
                break;
            case PRESSURE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.5f);
                break;
            case TEMPO:
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                break;
            case DISRUPTION:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.8f, 1.2f);
                break;
            case VISION:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 0.5f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
                break;
            case PERSISTENCE:
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.8f, 1.2f);
                break;
            case ANCHOR:
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.8f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.8f);
                break;
            case TRANSFER:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                break;
            case RISK:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.5f);
                break;
            case WITHER:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 0.8f);
                break;
            case WARDEN:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 0.8f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                break;
            case BREEZE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 1.0f, 1.2f);
                break;
            case DRAGON_EGG:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.5f, 1.5f);
                break;
            default:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;
        }
    }

    /**
     * Play sound for melee ability hit
     */
    private void playMeleeSound(Player player, AttributeType attr) {
        switch (attr) {
            case MELEE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 0.8f, 1.0f);
                break;
            case HEALTH:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.2f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.5f, 1.5f);
                break;
            case DEFENSE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.8f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                break;
            case WEALTH:
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
                break;
            case SPEED:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.2f);
                break;
            case CONTROL:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.5f);
                break;
            case RANGE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.8f);
                break;
            case PRESSURE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1.0f, 1.0f);
                break;
            case TEMPO:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 2.0f);
                break;
            case DISRUPTION:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 2.0f);
                break;
            case VISION:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.0f, 1.2f);
                break;
            case PERSISTENCE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.8f, 1.0f);
                break;
            case ANCHOR:
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.8f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.8f, 0.8f);
                break;
            case TRANSFER:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.0f);
                break;
            case RISK:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 0.5f, 1.0f);
                break;
            case WITHER:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.8f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 1.0f, 0.8f);
                break;
            case WARDEN:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.3f, 1.5f);
                break;
            case BREEZE:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_INHALE, 1.0f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 0.8f, 1.2f);
                break;
            case DRAGON_EGG:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);
                break;
            default:
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                break;
        }
    }

    /**
     * Apply support effects based on attribute
     */
    private void applySupportEffect(Player caster, List<Player> allies, PlayerData data) {
        AttributeType attr = data.getAttribute();
        int level = data.getLevel();

        switch (attr) {
            case MELEE:
                // Battle Fervor: +15% melee damage for 6s +1s per level
                int duration = (6 + level) * 20;
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.meleeDamageBonus = 0.15;
                    ally.sendMessage("§a+15% melee damage!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.meleeDamageBonus = 0.0, duration / 20);
                }
                break;

            case SPEED:
                // Rapid Formation: +20% movement, +15% attack speed (+5% per level)
                int speedDuration = 6 * 20;
                double attackSpeedBonus = 0.15 + (level - 1) * 0.05; // 15% to 40%
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.speedBonus = 0.20;
                    flags.attackSpeedBonus = attackSpeedBonus;
                    ally.sendMessage("§a+20% movement, +" + (int)(attackSpeedBonus * 100) + "% attack speed!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> {
                        flags.speedBonus = 0.0;
                        flags.attackSpeedBonus = 0.0;
                    }, 6);
                }
                break;

            case PRESSURE:
                // Intimidation Field
                int pressureDuration = 6 * 20;
                double damageDealt = 0.15 + (level - 1) * 0.05; // -15% to -35%
                double damageTaken = 0.10 + (level - 1) * 0.05; // +10% to +30%
                applyPressureField(caster, 6.0, pressureDuration, damageDealt, damageTaken);
                break;

            case DISRUPTION:
                // System Jam: Cannot activate abilities
                int disruptDuration = (3 + level) * 20;
                lockdownNearbyEnemies(caster, 6.0, disruptDuration);
                break;

            case ANCHOR:
                // Hold The Line: +25% knockback resist, +20% damage reduction (+5% per level)
                int anchorDuration = 6 * 20;
                double anchorReduction = 0.20 + (level - 1) * 0.05; // 20% to 45%
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.knockbackResistance = 0.25;
                    flags.damageReduction = anchorReduction;
                    ally.sendMessage("§a+25% knockback resist, +" + (int)(anchorReduction * 100) + "% damage reduction!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> {
                        flags.knockbackResistance = 0.0;
                        flags.damageReduction = 0.0;
                    }, 6);
                }
                break;

            case RISK:
                // Double Or Nothing: +30% damage (+5% per level), +20% damage taken
                int riskDuration = 6 * 20;
                double riskDamageBonus = 0.30 + (level - 1) * 0.05; // 30% to 55%
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.riskDamageBonus = riskDamageBonus;
                    flags.riskDamageTaken = 0.20;
                    ally.sendMessage("§6Double Or Nothing! +" + (int)(riskDamageBonus * 100) + "% damage, +20% damage taken!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> {
                        flags.riskDamageBonus = 0.0;
                        flags.riskDamageTaken = 0.0;
                    }, 6);
                }
                break;

            case HEALTH:
                // Fortify: +2 hearts (+1 per level) for 15s
                int hearts = 2 + level;
                for (Player ally : allies) {
                    double healthBoost = hearts * 2.0;
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 300, hearts - 1));
                    ally.setHealth(Math.min(ally.getHealth() + healthBoost, ally.getMaxHealth()));
                    ally.sendMessage("§a+" + hearts + " max hearts!");
                }
                break;

            case DEFENSE:
                // Shield Wall: 20% damage reduction (+5% per level) for 8s
                double reduction = 0.20 + (level - 1) * 0.05; // 20% to 40%
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.damageReduction = reduction;
                    ally.sendMessage("§a+" + (int)(reduction * 100) + "% damage reduction!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.damageReduction = 0.0, 8);
                }
                break;

            case WEALTH:
                // Economic Surge: 100% villager discount + Fortune X for 20s (+2s per level)
                int wealthDuration = (20 + (level - 1) * 2) * 20;
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.wealthSurgeActive = true;
                    ally.sendMessage("§aEconomic Surge active!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.wealthSurgeActive = false, wealthDuration / 20);
                }
                break;

            case CONTROL:
                // Lockdown: Enemies can't use abilities for 5s +1s per level
                int lockdownDuration = (5 + level) * 20;
                lockdownNearbyEnemies(caster, 6.0, lockdownDuration);
                break;

            case RANGE:
                // Zone Control: Homing arrows for 5s +1s per level
                int rangeDuration = (5 + level) * 20;
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.homingArrows = true;
                    ally.sendMessage("§aHoming arrows active!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.homingArrows = false, rangeDuration / 20);
                }
                break;

            case TEMPO:
                // Overdrive: +20% attack speed (+5% per level) for 8s
                double tempoAttackSpeedBonus = 0.20 + (level - 1) * 0.05; // 20% to 45%
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.attackSpeedBonus = tempoAttackSpeedBonus;
                    ally.sendMessage("§a+" + (int)(tempoAttackSpeedBonus * 100) + "% attack speed!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.attackSpeedBonus = 0.0, 8);
                }
                break;

            case VISION:
                // True Sight: See invisible players for 5s +1s per level
                int visionDuration = (5 + level) * 20;
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.trueSight = true;
                    ally.sendMessage("§aTrue Sight active!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.trueSight = false, visionDuration / 20);
                }
                break;

            case PERSISTENCE:
                // Last Stand: Can't drop below 1 heart for 2s +0.5s per level
                int standDuration = (int)((2 + (level - 1) * 0.5) * 20);
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.lastStand = true;
                    ally.sendMessage("§aLast Stand active!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.lastStand = false, standDuration / 20);
                }
                break;

            case TRANSFER:
                // Redirection: Split damage for radius 5 +1 per level
                double redirectRadius = 5.0 + level;
                for (Player ally : allies) {
                    if (ally.getLocation().distance(caster.getLocation()) <= redirectRadius) {
                        AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                        flags.redirectionActive = true;
                        flags.redirectionCenter = caster.getLocation();
                        flags.redirectionRadius = redirectRadius;
                        ally.sendMessage("§aRedirection active!");
                        scheduleRemoveFlag(ally.getUniqueId(), () -> flags.redirectionActive = false, 120);
                    }
                }
                break;

            case WITHER:
                // Shadow Pulse: 6 blocks, 10 damage, Slowness II for 3s
                applyWitherShadowPulse(caster);
                break;

            case WARDEN:
                // Deep Dark Zone
                applyWardenDomain(caster, level);
                break;

            case BREEZE:
                // Trial Order
                applyBreezeTrialOrder(caster, allies, level);
                break;

            case DRAGON_EGG:
                // Dominion
                applyDragonDominion(caster, allies, level);
                break;
        }
    }

    /**
     * Apply melee effects based on attribute
     */
    private void applyMeleeEffect(Player attacker, Entity target, PlayerData data) {
        if (!(target instanceof LivingEntity)) return;
        LivingEntity livingTarget = (LivingEntity) target;

        AttributeType attr = data.getAttribute();
        int level = data.getLevel();
        AbilityFlags targetFlags = target instanceof Player ? getAbilityFlags(target.getUniqueId()) : null;

        switch (attr) {
            case MELEE:
                // Power Strike: Negate 40% (+1% per level) armor
                if (targetFlags != null) {
                    double armorNegate = 0.40 + (level - 1) * 0.01;
                    targetFlags.armorNegation = armorNegate;
                    scheduleRemoveFlag(target.getUniqueId(), () -> targetFlags.armorNegation = 0.0, 1);
                }
                break;

            case SPEED:
                // Flash Step: Dash through target, +30% movement & attack speed for 3s +1s per level
                Vector dashDirection = livingTarget.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                attacker.setVelocity(dashDirection.multiply(1.5));

                AbilityFlags speedFlags = getAbilityFlags(attacker.getUniqueId());
                int flashDuration = (3 + level) * 20;
                speedFlags.speedBonus = 0.30;
                speedFlags.attackSpeedBonus = 0.30;
                attacker.sendMessage("§eFlash Step! +30% movement & attack speed!");
                scheduleRemoveFlag(attacker.getUniqueId(), () -> {
                    speedFlags.speedBonus = 0.0;
                    speedFlags.attackSpeedBonus = 0.0;
                }, flashDuration / 20);
                break;

            case PRESSURE:
                // Crushing Blow: +25% damage, Vulnerability (+15% damage taken) for 4s +1s per level
                if (targetFlags != null) {
                    int pressureDuration = (4 + level) * 20;
                    targetFlags.vulnerabilityMultiplier = 1.15;
                    livingTarget.damage(livingTarget.getLastDamage() * 0.25, attacker);
                    scheduleRemoveFlag(target.getUniqueId(), () -> targetFlags.vulnerabilityMultiplier = 1.0, pressureDuration / 20);
                    attacker.sendMessage("§eCrushing Blow!");
                }
                break;

            case DISRUPTION:
                // Fracture: +20s to all cooldowns, Weakness II for 4s +1s per level
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    PlayerData targetData = plugin.getPlayerData(targetPlayer.getUniqueId());
                    if (targetData != null) {
                        targetData.setCooldown("melee", targetData.getRemainingCooldown("melee") + 20000);
                        targetData.setCooldown("support", targetData.getRemainingCooldown("support") + 20000);
                        targetPlayer.sendMessage("§c+20s ability cooldowns!");
                    }
                    int weaknessDuration = (4 + level) * 20;
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessDuration, 1));
                }
                break;

            case ANCHOR:
                // Pin: Root target for 4s +1s per level
                if (targetFlags != null) {
                    int rootDuration = (4 + level) * 20;
                    targetFlags.rooted = true;
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, rootDuration, 10));
                    scheduleRemoveFlag(target.getUniqueId(), () -> targetFlags.rooted = false, rootDuration / 20);
                    attacker.sendMessage("§ePin! Target rooted!");
                }
                break;

            case RISK:
                // All In: +50% damage (+10% per level), you take +25% damage (-2% per level)
                double riskDamageBonus = 0.50 + (level - 1) * 0.10; // 50% to 90%
                double riskSelfPenalty = 0.25 - (level - 1) * 0.02; // 25% to 17%

                livingTarget.damage(livingTarget.getLastDamage() * riskDamageBonus, attacker);

                AbilityFlags riskFlags = getAbilityFlags(attacker.getUniqueId());
                riskFlags.riskSelfDamageTaken = riskSelfPenalty;
                scheduleRemoveFlag(attacker.getUniqueId(), () -> riskFlags.riskSelfDamageTaken = 0.0, 5);

                attacker.sendMessage("§6All In! +" + (int)(riskDamageBonus * 100) + "% damage!");
                break;

            case HEALTH:
                // Vampiric Hit: Heal for 50% of damage for 5s (+1s per level)
                AbilityFlags attackerFlags = getAbilityFlags(attacker.getUniqueId());
                int healDuration = (5 + level) * 20;
                attackerFlags.vampiricActive = true;
                attackerFlags.vampiricPercent = 0.5;
                attacker.sendMessage("§eVampiric Hit active!");
                scheduleRemoveFlag(attacker.getUniqueId(), () -> attackerFlags.vampiricActive = false, healDuration / 20);
                break;

            case DEFENSE:
                // Iron Response: 35% damage reduction for 4s (+0.5s per level)
                AbilityFlags defenderFlags = getAbilityFlags(attacker.getUniqueId());
                int defenseDuration = (int)((4 + (level - 1) * 0.5) * 20);
                defenderFlags.ironResponse = 0.35;
                attacker.sendMessage("§eIron Response active!");
                scheduleRemoveFlag(attacker.getUniqueId(), () -> defenderFlags.ironResponse = 0.0, defenseDuration / 20);
                break;

            case WEALTH:
                // Plunder Kill: Next mob kill gets bonus drops
                AbilityFlags wealthFlags = getAbilityFlags(attacker.getUniqueId());
                wealthFlags.plunderKillReady = true;
                wealthFlags.plunderMultiplier = 2.0 + (level - 1) * 0.25;
                attacker.sendMessage("§ePlunder Kill ready! Next mob kill gets bonus loot!");
                break;

            case CONTROL:
                // Disrupt: Slowness III for 3s +1s per level
                int controlDuration = (3 + level) * 20;
                livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, controlDuration, 2));
                break;

            case RANGE:
                // Spacing Strike: Knockback and prevent approach for 3s +1s per level
                Vector knockback = livingTarget.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(2.5);
                livingTarget.setVelocity(knockback);
                if (targetFlags != null) {
                    int spacingDuration = (3 + level) * 20;
                    targetFlags.cannotApproach = attacker.getUniqueId();
                    targetFlags.cannotApproachUntil = System.currentTimeMillis() + (spacingDuration * 50);
                }
                break;

            case TEMPO:
                // Tempo Strike: Slowness + 60s cooldown penalty for 3s +1s per level
                int tempoDuration = (3 + level) * 20;
                livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, tempoDuration, 1));
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    PlayerData targetData = plugin.getPlayerData(targetPlayer.getUniqueId());
                    if (targetData != null) {
                        targetData.setCooldown("melee", targetData.getRemainingCooldown("melee") + 60000);
                        targetData.setCooldown("support", targetData.getRemainingCooldown("support") + 60000);
                        targetPlayer.sendMessage("§c+60s ability cooldown!");
                    }
                }
                break;

            case VISION:
                // Target Mark: Reveal + cooldown states + 20% damage for 6s +1s per level
                if (targetFlags != null) {
                    int visionDuration = (6 + level) * 20;
                    targetFlags.markedForDamage = true;
                    targetFlags.damageTakenMultiplier = 1.20;
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, visionDuration, 0));
                    scheduleRemoveFlag(target.getUniqueId(), () -> {
                        targetFlags.markedForDamage = false;
                        targetFlags.damageTakenMultiplier = 1.0;
                    }, visionDuration / 20);
                }
                break;

            case PERSISTENCE:
                // Stored Pain: Store 25% of damage for 4s +1s per level
                if (targetFlags != null) {
                    int storageDuration = (4 + level) * 20;
                    targetFlags.persistenceStorageActive = true;
                    targetFlags.persistenceStoredDamage = 0.0;
                    targetFlags.persistenceLastAttacker = attacker.getUniqueId();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (targetFlags.persistenceStorageActive && livingTarget.isValid()) {
                                double stored = targetFlags.persistenceStoredDamage;
                                if (stored > 0 && attacker.isOnline()) {
                                    livingTarget.damage(stored, attacker);
                                    attacker.sendMessage("§eReleased " + String.format("%.1f", stored) + " stored damage!");
                                }
                                targetFlags.persistenceStorageActive = false;
                                targetFlags.persistenceStoredDamage = 0.0;
                            }
                        }
                    }.runTaskLater(plugin, storageDuration);
                }
                break;

            case TRANSFER:
                // Effect Swap: Steal all positive effects
                boolean stolenAny = false;
                for (PotionEffect effect : livingTarget.getActivePotionEffects()) {
                    if (isBeneficialEffect(effect.getType())) {
                        attacker.addPotionEffect(effect);
                        livingTarget.removePotionEffect(effect.getType());
                        stolenAny = true;
                    }
                }
                if (stolenAny) {
                    attacker.sendMessage("§aStole positive effects!");
                } else {
                    attacker.sendMessage("§cNo effects to steal!");
                }
                break;

            case WITHER:
                // Desperation Cleave
                applyWitherCleave(attacker, livingTarget, level);
                break;

            case WARDEN:
                // Sonic Slam
                applyWardenSonicSlam(attacker, livingTarget, level);
                break;

            case BREEZE:
                // Verdict Strike
                applyBreezeVerdictStrike(attacker, livingTarget, level);
                break;

            case DRAGON_EGG:
                // Rampaging Strike
                applyDragonRampagingStrike(attacker, livingTarget, level);
                break;
        }
    }

    // Boss Attribute Implementations

    private void applyWitherCleave(Player attacker, LivingEntity target, int level) {
        // Base damage: 12 + 1.2 per level
        double baseDamage = 12.0 + (level * 1.2);

        // HP multiplier
        double hpPercent = attacker.getHealth() / attacker.getMaxHealth();
        double damageMultiplier = 1.0;
        if (hpPercent < 0.20) damageMultiplier = 2.0;
        else if (hpPercent < 0.40) damageMultiplier = 1.6;
        else if (hpPercent < 0.60) damageMultiplier = 1.3;
        else if (hpPercent < 0.80) damageMultiplier = 1.2;

        double finalDamage = baseDamage * damageMultiplier;

        // Armor penetration: 20% + 5% per level
        double armorPen = 0.20 + (level * 0.05);

        // Apply damage (cone attack handled in combat event)
        target.damage(finalDamage * (1 - armorPen), attacker);

        // Wither II for 4s + 0.5s per level
        int witherDuration = (int)((4 + level * 0.5) * 20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherDuration, 1));

        // Healing reduction: -40% - 5% per level (handled in damage event)
        if (target instanceof Player) {
            AbilityFlags targetFlags = getAbilityFlags(target.getUniqueId());
            targetFlags.healingReduction = 0.40 + (level * 0.05);
            scheduleRemoveFlag(target.getUniqueId(), () -> targetFlags.healingReduction = 0.0, witherDuration / 20);
        }

        attacker.sendMessage("§5Desperation Cleave! " + (int)(damageMultiplier * 100) + "% damage!");
    }

    private void applyWitherShadowPulse(Player caster) {
        for (Entity entity : caster.getNearbyEntities(6, 6, 6)) {
            if (entity instanceof LivingEntity && entity != caster) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(10.0, caster);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            }
        }
        caster.sendMessage("§5Shadow Pulse!");
    }

    private void applyWardenSonicSlam(Player attacker, LivingEntity target, int level) {
        // AoE 5 blocks
        for (Entity entity : target.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != attacker) {
                LivingEntity nearby = (LivingEntity) entity;
                nearby.damage(14.0, attacker);

                // Fear effect: slow movement and attack speed
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));
            }
        }
        target.damage(14.0, attacker);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));

        attacker.sendMessage("§3Sonic Slam!");
    }

    private void applyWardenDomain(Player caster, int level) {
        double radius = 12.0 + (level * 0.8); // 12-16 blocks
        int duration = (8 + level) * 20; // 8-12s

        AbilityFlags casterFlags = getAbilityFlags(caster.getUniqueId());
        casterFlags.wardenDomainActive = true;
        casterFlags.wardenDomainCenter = caster.getLocation();
        casterFlags.wardenDomainRadius = radius;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ >= duration || !casterFlags.wardenDomainActive) {
                    casterFlags.wardenDomainActive = false;
                    cancel();
                    return;
                }

                for (Player player : caster.getWorld().getPlayers()) {
                    if (player.getLocation().distance(casterFlags.wardenDomainCenter) <= radius) {
                        if (player.equals(caster)) {
                            // Caster gets +15% melee damage
                            continue;
                        }

                        AbilityFlags flags = getAbilityFlags(player.getUniqueId());

                        // Check if enemy or ally
                        boolean isAlly = false; // Implement team checking if needed

                        if (!isAlly) {
                            // Enemies
                            player.setSprinting(false);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20, -2, true, false));
                            flags.wardenDomainAttackSpeedReduction = 0.30;
                            flags.wardenDomainHealingReduction = 0.50;
                        } else {
                            // Allies
                            flags.wardenDomainDamageReduction = 0.25;
                            flags.wardenDomainKnockbackResist = 0.50;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        scheduleRemoveFlag(caster.getUniqueId(), () -> casterFlags.wardenDomainActive = false, duration / 20);
        caster.sendMessage("§3Deep Dark Zone activated!");
    }

    private void applyBreezeVerdictStrike(Player attacker, LivingEntity target, int level) {
        if (!(target instanceof Player)) return;

        AbilityFlags targetFlags = getAbilityFlags(target.getUniqueId());
        targetFlags.breezeJudged = true;
        targetFlags.breezeJudgeDamageBonus = 0.12 + (level * 0.02);
        targetFlags.breezeJudgeMissPenalty = 6.0 + level;
        targetFlags.breezeJudgeAttacker = attacker.getUniqueId();

        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));

        scheduleRemoveFlag(target.getUniqueId(), () -> {
            targetFlags.breezeJudged = false;
            targetFlags.breezeJudgeDamageBonus = 0.0;
        }, 10);

        attacker.sendMessage("§eTarget Judged!");
    }

    private void applyBreezeTrialOrder(Player caster, List<Player> allies, int level) {
        double radius = 8.0 + level;
        int duration = (6 + level) * 20;

        for (Player ally : allies) {
            if (ally.getLocation().distance(caster.getLocation()) <= radius) {
                AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                flags.breezeCooldownReduction = 0.20;
                flags.breezeDamageReduction = 0.10;

                ally.sendMessage("§eTrial Order! -20% cooldowns, +10% damage reduction!");
                scheduleRemoveFlag(ally.getUniqueId(), () -> {
                    flags.breezeCooldownReduction = 0.0;
                    flags.breezeDamageReduction = 0.0;
                }, duration / 20);
            }
        }
    }

    private void applyDragonRampagingStrike(Player attacker, LivingEntity target, int level) {
        // Base damage: 10 + 2 per level
        double baseDamage = 10.0 + (level * 2.0);

        // Bonus damage if below 30% HP
        double hpPercent = attacker.getHealth() / attacker.getMaxHealth();
        if (hpPercent < 0.30) {
            baseDamage *= 1.5;
            attacker.sendMessage("§c§lLow HP Bonus!");
        }

        target.damage(baseDamage, attacker);

        // Lifesteal: 20% + 5% per level
        double lifesteal = 0.20 + (level * 0.05);
        double healAmount = baseDamage * lifesteal;

        if (attacker.getHealth() + healAmount > attacker.getMaxHealth()) {
            // Overheal to absorption
            double overheal = (attacker.getHealth() + healAmount) - attacker.getMaxHealth();
            attacker.setHealth(attacker.getMaxHealth());
            attacker.setAbsorptionAmount(attacker.getAbsorptionAmount() + overheal);
        } else {
            attacker.setHealth(attacker.getHealth() + healAmount);
        }

        // Slowness III and Weakness II for 3s + 0.5s per level
        int debuffDuration = (int)((3 + level * 0.5) * 20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, debuffDuration, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, debuffDuration, 1));

        attacker.sendMessage("§6Rampaging Strike! Healed " + String.format("%.1f", healAmount) + " HP!");
    }

    private void applyDragonDominion(Player caster, List<Player> allies, int level) {
        // +25% damage (+1% per level), 50% cooldown reduction (+1% per level)
        double damageBonus = 0.25 + (level * 0.01);
        double cooldownReduction = 0.50 + (level * 0.01);

        for (Player ally : allies) {
            AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
            flags.dragonDominionDamage = 1.0 + damageBonus;
            flags.dragonDominionCooldown = cooldownReduction;

            ally.sendMessage("§6§lDOMINION! +" + (int)(damageBonus * 100) + "% damage, " + (int)(cooldownReduction * 100) + "% cooldown reduction!");
            scheduleRemoveFlag(ally.getUniqueId(), () -> {
                flags.dragonDominionDamage = 1.0;
                flags.dragonDominionCooldown = 0.0;
            }, 8);
        }
    }

    private void applyPressureField(Player caster, double radius, int duration, double damageDealt, double damageTaken) {
        for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
            if (entity instanceof Player && entity != caster) {
                Player enemy = (Player) entity;
                AbilityFlags flags = getAbilityFlags(enemy.getUniqueId());
                flags.pressureDamageDealt = damageDealt;
                flags.pressureDamageTaken = damageTaken;
                enemy.sendMessage("§cIntimidation Field!");
                scheduleRemoveFlag(enemy.getUniqueId(), () -> {
                    flags.pressureDamageDealt = 0.0;
                    flags.pressureDamageTaken = 0.0;
                }, duration / 20);
            }
        }
    }

    /**
     * Handle passive ability effects
     */
    public void handlePassiveEffects(Player player) {
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null || data.getAttribute() == null) return;

        // Passive abilities are handled in event listeners
        // This method can be called on a timer for passive aura effects
        if (player.getTicksLived() % 20 == 0) { // Every second
            ParticleManager.playPassiveParticles(player, data.getAttribute(), data.getTier());

            // Vision Passive: Awareness - see all enemies around you
            if (data.getAttribute() == AttributeType.VISION) {
                for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                    if (entity instanceof Player) {
                        Player nearby = (Player) entity;
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 25, 0, true, false));
                    }
                }
            }

            // Dragon Egg Passive: Draconic Curse - nearby enemies take more damage
            if (data.getAttribute() == AttributeType.DRAGON_EGG) {
                double damageIncrease = 0.15 - (data.getLevel() - 1) * 0.01; // 15% to 11% (min 10% at max would be 11%)
                for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
                    if (entity instanceof Player && entity != player) {
                        Player nearby = (Player) entity;
                        AbilityFlags flags = getAbilityFlags(nearby.getUniqueId());
                        flags.dragonCurseDamage = 1.0 + damageIncrease;
                    }
                }
            }

            // Wither Passive: Curse of Despair particles
            if (data.getAttribute() == AttributeType.WITHER) {
                player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE,
                        player.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0.01);
            }

            // Warden Passive: Curse of Silence particles
            if (data.getAttribute() == AttributeType.WARDEN) {
                player.getWorld().spawnParticle(org.bukkit.Particle.SCULK_SOUL,
                        player.getLocation(), 2, 0.3, 0, 0.3, 0.01);
            }

            // Breeze Passive: Curse of Judgment - tick cooldowns if no enemy nearby
            if (data.getAttribute() == AttributeType.BREEZE) {
                boolean enemyNearby = false;
                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (entity instanceof Player) {
                        enemyNearby = true;
                        break;
                    }
                }

                if (!enemyNearby && player.getTicksLived() % 200 == 0) { // Every 10 seconds
                    data.setCooldown("melee", data.getRemainingCooldown("melee") + 1000);
                    data.setCooldown("support", data.getRemainingCooldown("support") + 1000);
                }
            }

            // Wealth Passive: Industrialist - Hero of the Village XII permanent
            if (data.getAttribute() == AttributeType.WEALTH) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 11, true, false, false));
            }

            // Transfer Passive: Immunity - cannot receive debuffs (remove negative effects)
            if (data.getAttribute() == AttributeType.TRANSFER) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (isNegativeEffect(effect.getType())) {
                        player.removePotionEffect(effect.getType());
                    }
                }
            }

            // Persistence Passive: Endure - damage resistance below 50% HP
            if (data.getAttribute() == AttributeType.PERSISTENCE) {
                double healthPercent = player.getHealth() / player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                if (healthPercent < 0.5) {
                    int resistLevel = Math.min(data.getLevel() - 1, 4); // 0-4 for Resistance I-V
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, resistLevel, true, false, false));
                }
            }

            // Risk Passive: Gambler's Edge - damage boost below 40% HP
            if (data.getAttribute() == AttributeType.RISK) {
                double healthPercent = player.getHealth() / player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                if (healthPercent < 0.4) {
                    int strengthLevel = Math.min(data.getLevel() - 1, 4); // 0-4 for Strength I-V
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, strengthLevel, true, false, false));
                }
            }
        }
    }

    // Helper methods

    private List<Player> getNearbyAllies(Player center, double radius) {
        List<Player> allies = new ArrayList<>();
        for (Entity entity : center.getWorld().getNearbyEntities(center.getLocation(), radius, radius, radius)) {
            if (entity instanceof Player && entity != center) {
                allies.add((Player) entity);
            }
        }
        return allies;
    }

    private void lockdownNearbyEnemies(Player caster, double radius, int durationTicks) {
        for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
            if (entity instanceof Player && entity != caster) {
                Player enemy = (Player) entity;
                AbilityFlags flags = getAbilityFlags(enemy.getUniqueId());
                flags.abilitiesLocked = true;
                enemy.sendMessage("§cYour abilities are locked!");
                scheduleRemoveFlag(enemy.getUniqueId(), () -> {
                    flags.abilitiesLocked = false;
                    enemy.sendMessage("§aAbilities unlocked!");
                }, durationTicks / 20);
            }
        }
    }

    private void scheduleRemoveFlag(UUID playerId, Runnable action, int seconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                action.run();
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    public AbilityFlags getAbilityFlags(UUID playerId) {
        return abilityFlags.computeIfAbsent(playerId, k -> new AbilityFlags());
    }

    public void removeAbilityFlags(UUID playerId) {
        abilityFlags.remove(playerId);
    }

    /**
     * Check if a potion effect is beneficial
     */
    private boolean isBeneficialEffect(PotionEffectType type) {
        return type == PotionEffectType.SPEED ||
                type == PotionEffectType.HASTE ||
                type == PotionEffectType.STRENGTH ||
                type == PotionEffectType.INSTANT_HEALTH ||
                type == PotionEffectType.JUMP_BOOST ||
                type == PotionEffectType.REGENERATION ||
                type == PotionEffectType.RESISTANCE ||
                type == PotionEffectType.FIRE_RESISTANCE ||
                type == PotionEffectType.WATER_BREATHING ||
                type == PotionEffectType.INVISIBILITY ||
                type == PotionEffectType.NIGHT_VISION ||
                type == PotionEffectType.HEALTH_BOOST ||
                type == PotionEffectType.ABSORPTION ||
                type == PotionEffectType.SATURATION ||
                type == PotionEffectType.LUCK ||
                type == PotionEffectType.SLOW_FALLING ||
                type == PotionEffectType.CONDUIT_POWER ||
                type == PotionEffectType.DOLPHINS_GRACE ||
                type == PotionEffectType.HERO_OF_THE_VILLAGE;
    }

    private boolean isNegativeEffect(PotionEffectType type) {
        return type == PotionEffectType.SLOWNESS ||
                type == PotionEffectType.MINING_FATIGUE ||
                type == PotionEffectType.INSTANT_DAMAGE ||
                type == PotionEffectType.NAUSEA ||
                type == PotionEffectType.BLINDNESS ||
                type == PotionEffectType.HUNGER ||
                type == PotionEffectType.WEAKNESS ||
                type == PotionEffectType.POISON ||
                type == PotionEffectType.WITHER ||
                type == PotionEffectType.LEVITATION ||
                type == PotionEffectType.UNLUCK ||
                type == PotionEffectType.DARKNESS ||
                type == PotionEffectType.GLOWING; // Glowing can reveal position
    }

    /**
     * Flags to track ability states
     */
    public static class AbilityFlags {
        // General
        public boolean meleeReady = false;
        public boolean abilitiesLocked = false;

        // Melee
        public double meleeDamageBonus = 0.0;
        public double meleeBloodlustStacks = 0.0;

        // Health
        public boolean vampiricActive = false;
        public double vampiricPercent = 0.0;
        public double vitalityHearts = 0.0;

        // Defense
        public double damageReduction = 0.0;
        public double ironResponse = 0.0;
        public double hardenedReduction = 0.0;

        // Wealth
        public boolean wealthSurgeActive = false;
        public boolean plunderKillReady = false;
        public double plunderMultiplier = 1.0;

        // Control
        public long suppressionCooldownAdded = 0;

        // Range
        public boolean homingArrows = false;
        public UUID cannotApproach = null;
        public long cannotApproachUntil = 0;

        // Tempo
        public double attackSpeedBonus = 0.0;
        public double momentumStacks = 0.0;

        // Vision
        public boolean trueSight = false;
        public boolean markedForDamage = false;
        public double damageTakenMultiplier = 1.0;

        // Persistence
        public boolean persistenceStorageActive = false;
        public double persistenceStoredDamage = 0.0;
        public UUID persistenceLastAttacker = null;
        public boolean lastStand = false;
        public double endureReduction = 0.0;

        // Transfer
        public boolean redirectionActive = false;
        public org.bukkit.Location redirectionCenter = null;
        public double redirectionRadius = 0.0;

        // Combat
        public double armorNegation = 0.0;
        public double healingReduction = 0.0;

        // Wither
        public double witherCleaveMultiplier = 1.0;

        // Warden
        public boolean wardenDomainActive = false;
        public org.bukkit.Location wardenDomainCenter = null;
        public double wardenDomainRadius = 0.0;
        public double wardenDomainDamageReduction = 0.0;
        public double wardenDomainKnockbackResist = 0.0;
        public double wardenDomainAttackSpeedReduction = 0.0;
        public double wardenDomainHealingReduction = 0.0;

        // Breeze
        public boolean breezeJudged = false;
        public double breezeJudgeDamageBonus = 0.0;
        public double breezeJudgeMissPenalty = 0.0;
        public UUID breezeJudgeAttacker = null;
        public double breezeCooldownReduction = 0.0;
        public double breezeDamageReduction = 0.0;

        // Dragon Egg
        public double dragonDominionDamage = 1.0;
        public double dragonDominionCooldown = 0.0;
        public double dragonCurseDamage = 1.0;

        // Speed
        public double speedBonus = 0.0;
        public double speedAdrenalineStacks = 0.0;
        public long speedLastCombatTime = 0;

        // Pressure
        public double pressureDamageDealt = 0.0; // Reduction in damage dealt
        public double pressureDamageTaken = 0.0; // Increase in damage taken
        public double vulnerabilityMultiplier = 1.0;

        // Disruption
        public boolean disruptionDesyncUsed = false;
        public long disruptionDesyncCooldown = 0;

        // Anchor
        public double knockbackResistance = 0.0;
        public double anchorImmobileReduction = 0.0;
        public long anchorLastMovedTime = 0;
        public boolean rooted = false;

        // Risk
        public double riskDamageBonus = 0.0;
        public double riskDamageTaken = 0.0;
        public double riskSelfDamageTaken = 0.0;
    }
}