package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

        // Check ability lock
        AbilityFlags playerFlags = getAbilityFlags(player.getUniqueId());
        if (playerFlags.abilitiesLocked) {
            player.sendMessage("§cYour abilities are locked!");
            return;
        }

        // Get cooldown based on attribute type
        int baseCooldown = getSupportCooldown(data.getAttribute());
        data.setCooldown(cooldownKey, baseCooldown * 1000L);

        // Get nearby allies (includes self)
        List<Player> allies = getNearbyAllies(player, getSupportRadius(data.getAttribute()));
        allies.add(0, player);

        // Play particles and sound
        if (plugin.isParticleSupportAbility()) {
            ParticleManager.playSupportParticles(player, data.getAttribute(), data.getTier(), data.getLevel());
        }
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

        // Check ability lock
        AbilityFlags playerFlags = getAbilityFlags(player.getUniqueId());
        if (playerFlags.abilitiesLocked) {
            player.sendMessage("§cYour abilities are locked!");
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
        if (plugin.isParticleMeleeAbility()) {
            ParticleManager.playMeleeParticles(attacker, target, data.getAttribute(), data.getTier());
        }
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
            case DISRUPTION:
                return 150;
            case RISK:
                return 150;
            case WITHER:
            case BREEZE:
                return 240;
            case WARDEN:
                return 180;
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
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.2f);
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
            case MELEE: {
                // Battle Fervor: +15% melee damage for 6s +1s per level, max 11s
                int duration = Math.min(11, 6 + level);
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.meleeDamageBonus = 0.15;
                    ally.sendMessage("§a+15% melee damage for " + duration + "s!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.meleeDamageBonus = 0.0, duration);
                }
                break;
            }

            case HEALTH: {
                // Fortify: Heal 3 hearts +0.5 per level, max 5.5
                double heartsHeal = Math.min(5.5, 3.0 + (level - 1) * 0.5);
                double healthHeal = heartsHeal * 2.0;
                for (Player ally : allies) {
                    double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    ally.setHealth(Math.min(ally.getHealth() + healthHeal, maxHealth));
                    ally.sendMessage("§aHealed " + heartsHeal + " hearts!");
                }
                break;
            }

            case DEFENSE: {
                // Shield Wall: 4 absorption hearts +0.5 per level, max 6.5 hearts, 8s duration
                double absHearts = Math.min(6.5, 4.0 + (level - 1) * 0.5);
                double absAmount = absHearts * 2.0;
                for (Player ally : allies) {
                    ally.setAbsorptionAmount(ally.getAbsorptionAmount() + absAmount);
                    ally.sendMessage("§a+" + absHearts + " absorption hearts!");
                    // Remove absorption after 8s
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (ally.isOnline()) {
                                ally.setAbsorptionAmount(Math.max(0, ally.getAbsorptionAmount() - absAmount));
                            }
                        }
                    }.runTaskLater(plugin, 8 * 20L);
                }
                break;
            }

            case WEALTH: {
                // Economic Surge: 100% villager discount + Fortune 7, 20s +2s per level, max 30s
                int wealthDuration = Math.min(30, 20 + (level - 1) * 2);
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.wealthSurgeActive = true;
                    ally.sendMessage("§aEconomic Surge! Villager discount + Fortune 7 for " + wealthDuration + "s!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.wealthSurgeActive = false, wealthDuration);
                }
                break;
            }

            case SPEED: {
                // Rapid Formation: Speed III for 6s
                for (Player ally : allies) {
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6 * 20, 2, true, true));
                    ally.sendMessage("§aSpeed III for 6s!");
                }
                break;
            }

            case RANGE: {
                // Zone Control: Homing arrows, 5s +1s per level
                int rangeDuration = 5 + level;
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.homingArrows = true;
                    ally.sendMessage("§aHoming arrows for " + rangeDuration + "s!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.homingArrows = false, rangeDuration);
                }
                break;
            }

            case PRESSURE: {
                // Intimidation Field: Enemies deal -15% damage, take +10% damage, +5% per level, max -35%/+30%, 6s
                double damageDealt = Math.min(0.35, 0.15 + (level - 1) * 0.05);
                double damageTaken = Math.min(0.30, 0.10 + (level - 1) * 0.05);
                applyPressureField(caster, 6.0, 6 * 20, damageDealt, damageTaken);
                break;
            }

            case TEMPO: {
                // Overdrive: Haste V for 5s +1s per level
                int tempoDuration = 5 + level;
                for (Player ally : allies) {
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, tempoDuration * 20, 4, true, true));
                    ally.sendMessage("§aHaste V for " + tempoDuration + "s!");
                }
                break;
            }

            case DISRUPTION: {
                // System Jam: Enemies cannot activate abilities, 25s +1s per level, max 30s
                int disruptDuration = Math.min(30, 25 + level);
                lockdownNearbyEnemies(caster, 6.0, disruptDuration * 20);
                break;
            }

            case VISION: {
                // True Sight: Point to a player (apply glowing), 5s +1s per level, max 10s
                int visionDuration = Math.min(10, 5 + level);
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.trueSight = true;
                    ally.sendMessage("§aTrue Sight for " + visionDuration + "s!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.trueSight = false, visionDuration);
                }
                break;
            }

            case TRANSFER: {
                // Redirection: Reflect durability damage for 5s +1s per level, max 10s
                int redirectDuration = Math.min(10, 5 + level);
                for (Player ally : allies) {
                    AbilityFlags flags = getAbilityFlags(ally.getUniqueId());
                    flags.redirectionActive = true;
                    ally.sendMessage("§aRedirection active for " + redirectDuration + "s!");
                    scheduleRemoveFlag(ally.getUniqueId(), () -> flags.redirectionActive = false, redirectDuration);
                }
                break;
            }

            case RISK: {
                // Double or Nothing: +30% damage +5%/level max 55%, +20% damage taken, 6s
                double riskDamageBonus = Math.min(0.55, 0.30 + (level - 1) * 0.05);
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
            }

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
            case MELEE: {
                // Power Strike: Next hit ignores 25% armor +1% per level (max 30% at L5)
                if (targetFlags != null) {
                    double armorNegate = 0.25 + (level - 1) * 0.01; // 25% to 29% (30% at L5 by rounding intent)
                    if (level >= 5) armorNegate = 0.30;
                    targetFlags.armorNegation = armorNegate;
                    scheduleRemoveFlag(target.getUniqueId(), () -> targetFlags.armorNegation = 0.0, 1);
                }
                break;
            }

            case HEALTH: {
                // Vampiric Hit: Heal 15% of damage dealt +1% per level, max 20%, overheal to absorption
                double vampPercent = Math.min(0.20, 0.15 + (level - 1) * 0.01);
                AbilityFlags attackerFlags = getAbilityFlags(attacker.getUniqueId());
                int healDuration = 5;
                attackerFlags.vampiricActive = true;
                attackerFlags.vampiricPercent = vampPercent;
                attacker.sendMessage("§eVampiric Hit! " + (int)(vampPercent * 100) + "% lifesteal for " + healDuration + "s!");
                scheduleRemoveFlag(attacker.getUniqueId(), () -> {
                    attackerFlags.vampiricActive = false;
                    attackerFlags.vampiricPercent = 0.0;
                }, healDuration);
                break;
            }

            case DEFENSE: {
                // Iron Response: 20% damage reduction for 4s +0.5s per level, max 6s
                AbilityFlags defenderFlags = getAbilityFlags(attacker.getUniqueId());
                double durationSecs = Math.min(6.0, 4.0 + (level - 1) * 0.5);
                int durationTicks = (int)(durationSecs * 20);
                defenderFlags.damageReduction = 0.20;
                attacker.sendMessage("§eIron Response! -20% damage taken for " + durationSecs + "s!");
                scheduleRemoveFlag(attacker.getUniqueId(), () -> defenderFlags.damageReduction = 0.0, durationTicks / 20);
                break;
            }

            case WEALTH: {
                // Plunder Kill: Disable the item the person is holding for 7s +1s per level
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    int disableDuration = 7 + level;
                    AbilityFlags tFlags = getAbilityFlags(targetPlayer.getUniqueId());
                    ItemStack heldItem = targetPlayer.getInventory().getItemInMainHand();
                    if (heldItem != null && heldItem.getType() != org.bukkit.Material.AIR) {
                        tFlags.itemDisabled = true;
                        tFlags.disabledItemSlot = targetPlayer.getInventory().getHeldItemSlot();
                        targetPlayer.sendMessage("§cYour held item has been disabled for " + disableDuration + "s!");
                        attacker.sendMessage("§eDisabled their " + heldItem.getType().name() + " for " + disableDuration + "s!");
                        scheduleRemoveFlag(targetPlayer.getUniqueId(), () -> {
                            tFlags.itemDisabled = false;
                            tFlags.disabledItemSlot = -1;
                            targetPlayer.sendMessage("§aYour item is no longer disabled!");
                        }, disableDuration);
                    }
                }
                break;
            }

            case SPEED: {
                // Flash Step: Summon lightning on the opponent for 5s +1s per level
                int lightningDuration = 5 + level;
                livingTarget.getWorld().strikeLightningEffect(livingTarget.getLocation());
                livingTarget.damage(4.0, attacker);
                if (targetFlags != null) {
                    targetFlags.lightningStruck = true;
                    // Apply repeated lightning ticks
                    new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (ticks++ >= lightningDuration || !livingTarget.isValid()) {
                                if (targetFlags != null) targetFlags.lightningStruck = false;
                                cancel();
                                return;
                            }
                            livingTarget.getWorld().strikeLightningEffect(livingTarget.getLocation());
                            livingTarget.damage(2.0, attacker);
                        }
                    }.runTaskTimer(plugin, 20L, 20L);
                }
                attacker.sendMessage("§eFlash Step! Lightning for " + lightningDuration + "s!");
                break;
            }

            case RANGE: {
                // Spacing Strike: Knockback, 3s +1s per level, max 8s
                int spacingDuration = Math.min(8, 3 + level);
                Vector knockback = livingTarget.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(2.5);
                livingTarget.setVelocity(knockback);
                if (targetFlags != null) {
                    targetFlags.cannotApproach = attacker.getUniqueId();
                    targetFlags.cannotApproachUntil = System.currentTimeMillis() + (spacingDuration * 1000L);
                }
                attacker.sendMessage("§eSpacing Strike! Knockback for " + spacingDuration + "s!");
                break;
            }

            case PRESSURE: {
                // Crushing Blow: +25% damage, target takes +15% damage, 4s +1s per level, max 9s
                if (targetFlags != null) {
                    int pressureDuration = Math.min(9, 4 + level);
                    targetFlags.vulnerabilityMultiplier = 1.15;
                    livingTarget.damage(livingTarget.getLastDamage() * 0.25, attacker);
                    scheduleRemoveFlag(target.getUniqueId(), () -> targetFlags.vulnerabilityMultiplier = 1.0, pressureDuration);
                    attacker.sendMessage("§eCrushing Blow! +15% vulnerability for " + pressureDuration + "s!");
                }
                break;
            }

            case TEMPO: {
                // Tempo Strike: Stun (can't move or look), 5s +1s per level
                int stunDuration = 5 + level;
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    AbilityFlags tFlags = getAbilityFlags(targetPlayer.getUniqueId());
                    tFlags.stunned = true;
                    targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunDuration * 20, 255, true, false));
                    targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, stunDuration * 20, 0, true, false));
                    targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, stunDuration * 20, 255, true, false));
                    targetPlayer.sendMessage("§cYou are stunned for " + stunDuration + "s!");
                    scheduleRemoveFlag(targetPlayer.getUniqueId(), () -> {
                        tFlags.stunned = false;
                        targetPlayer.sendMessage("§aYou are no longer stunned!");
                    }, stunDuration);
                }
                attacker.sendMessage("§eTempo Strike! Stunned for " + stunDuration + "s!");
                break;
            }

            case DISRUPTION: {
                // Fracture: +20s cooldown, Weakness II, Blindness, Nausea, 4s +1s per level, max 9s
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    PlayerData targetData = plugin.getPlayerData(targetPlayer.getUniqueId());
                    if (targetData != null) {
                        targetData.setCooldown("melee", targetData.getRemainingCooldown("melee") + 20000);
                        targetData.setCooldown("support", targetData.getRemainingCooldown("support") + 20000);
                        targetPlayer.sendMessage("§c+20s ability cooldowns!");
                    }
                    int debuffDuration = Math.min(9, 4 + level);
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, debuffDuration * 20, 1));
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, debuffDuration * 20, 0));
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, debuffDuration * 20, 0));
                }
                attacker.sendMessage("§eFracture!");
                break;
            }

            case VISION: {
                // Target Mark: Glowing for 5 minutes +30s per level, max 7.5m
                int visionSeconds = Math.min(450, 300 + (level - 1) * 30);
                livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, visionSeconds * 20, 0, true, false));
                attacker.sendMessage("§eTarget marked for " + (visionSeconds / 60) + "m " + (visionSeconds % 60) + "s!");
                break;
            }

            case TRANSFER: {
                // Effect Swap: Steal 45% +1% per level (max 50%) of opponent's potion effects
                double stealPercent = Math.min(0.50, 0.45 + (level - 1) * 0.01);
                boolean stolenAny = false;
                List<PotionEffect> effectsToSteal = new ArrayList<>();
                for (PotionEffect effect : livingTarget.getActivePotionEffects()) {
                    if (isBeneficialEffect(effect.getType())) {
                        effectsToSteal.add(effect);
                    }
                }
                for (PotionEffect effect : effectsToSteal) {
                    // Scale the duration by steal percent
                    int newDuration = (int)(effect.getDuration() * stealPercent);
                    if (newDuration > 0) {
                        attacker.addPotionEffect(new PotionEffect(effect.getType(), newDuration, effect.getAmplifier()));
                    }
                    // Reduce target's effect duration
                    livingTarget.removePotionEffect(effect.getType());
                    int remainingDuration = (int)(effect.getDuration() * (1.0 - stealPercent));
                    if (remainingDuration > 0) {
                        livingTarget.addPotionEffect(new PotionEffect(effect.getType(), remainingDuration, effect.getAmplifier()));
                    }
                    stolenAny = true;
                }
                if (stolenAny) {
                    attacker.sendMessage("§aStole " + (int)(stealPercent * 100) + "% of positive effects!");
                } else {
                    attacker.sendMessage("§cNo effects to steal!");
                }
                break;
            }

            case RISK: {
                // All In: +50% damage +10%/level, +25% taken -2%/level
                double riskDamageBonus = 0.50 + (level - 1) * 0.10;
                double riskSelfPenalty = 0.25 - (level - 1) * 0.02;

                livingTarget.damage(livingTarget.getLastDamage() * riskDamageBonus, attacker);

                AbilityFlags riskFlags = getAbilityFlags(attacker.getUniqueId());
                riskFlags.riskSelfDamageTaken = riskSelfPenalty;
                scheduleRemoveFlag(attacker.getUniqueId(), () -> riskFlags.riskSelfDamageTaken = 0.0, 5);

                attacker.sendMessage("§6All In! +" + (int)(riskDamageBonus * 100) + "% damage!");
                break;
            }

            case WITHER:
                applyWitherCleave(attacker, livingTarget, level);
                break;

            case WARDEN:
                applyWardenSonicSlam(attacker, livingTarget, level);
                break;

            case BREEZE:
                applyBreezeVerdictStrike(attacker, livingTarget, level);
                break;

            case DRAGON_EGG:
                applyDragonRampagingStrike(attacker, livingTarget, level);
                break;
        }
    }

    // Boss Attribute Implementations

    private void applyWitherCleave(Player attacker, LivingEntity target, int level) {
        double baseDamage = 12.0 + (level * 1.2);

        double hpPercent = attacker.getHealth() / attacker.getMaxHealth();
        double damageMultiplier = 1.0;
        if (hpPercent < 0.20) damageMultiplier = 2.0;
        else if (hpPercent < 0.40) damageMultiplier = 1.6;
        else if (hpPercent < 0.60) damageMultiplier = 1.3;
        else if (hpPercent < 0.80) damageMultiplier = 1.2;

        double finalDamage = baseDamage * damageMultiplier;

        double armorPen = 0.20 + (level * 0.05);
        target.damage(finalDamage * (1 - armorPen), attacker);

        int witherDuration = (int)((4 + level * 0.5) * 20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherDuration, 1));

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
        for (Entity entity : target.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != attacker) {
                LivingEntity nearby = (LivingEntity) entity;
                nearby.damage(14.0, attacker);
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
        double radius = 12.0 + (level * 0.8);
        int duration = (8 + level) * 20;

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
                        if (player.equals(caster)) continue;

                        AbilityFlags flags = getAbilityFlags(player.getUniqueId());
                        boolean isAlly = false;

                        if (!isAlly) {
                            player.setSprinting(false);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20, -2, true, false));
                            flags.wardenDomainAttackSpeedReduction = 0.30;
                            flags.wardenDomainHealingReduction = 0.50;
                        } else {
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
        double baseDamage = 10.0 + (level * 2.0);

        double hpPercent = attacker.getHealth() / attacker.getMaxHealth();
        if (hpPercent < 0.30) {
            baseDamage *= 1.5;
            attacker.sendMessage("§c§lLow HP Bonus!");
        }

        target.damage(baseDamage, attacker);

        double lifesteal = 0.20 + (level * 0.05);
        double healAmount = baseDamage * lifesteal;

        if (attacker.getHealth() + healAmount > attacker.getMaxHealth()) {
            double overheal = (attacker.getHealth() + healAmount) - attacker.getMaxHealth();
            attacker.setHealth(attacker.getMaxHealth());
            attacker.setAbsorptionAmount(attacker.getAbsorptionAmount() + overheal);
        } else {
            attacker.setHealth(attacker.getHealth() + healAmount);
        }

        int debuffDuration = (int)((3 + level * 0.5) * 20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, debuffDuration, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, debuffDuration, 1));

        attacker.sendMessage("§6Rampaging Strike! Healed " + String.format("%.1f", healAmount) + " HP!");
    }

    private void applyDragonDominion(Player caster, List<Player> allies, int level) {
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
     * Handle passive ability effects (called every second by ticker)
     */
    public void handlePassiveEffects(Player player) {
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null || data.getAttribute() == null) return;

        if (plugin.isParticlePassiveAbility()) {
            ParticleManager.playPassiveParticles(player, data.getAttribute(), data.getTier());
        }

        int level = data.getLevel();

        // VISION Passive: Awareness - players within 12 blocks have glowing (only to you)
        if (data.getAttribute() == AttributeType.VISION) {
            for (Entity entity : player.getNearbyEntities(12, 12, 12)) {
                if (entity instanceof Player) {
                    Player nearby = (Player) entity;
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 25, 0, true, false));
                }
            }
        }

        // DRAGON EGG Passive: Draconic Curse - nearby enemies take increased damage
        if (data.getAttribute() == AttributeType.DRAGON_EGG) {
            double damageIncrease = 0.15 - (level - 1) * 0.01;
            for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
                if (entity instanceof Player && entity != player) {
                    Player nearby = (Player) entity;
                    AbilityFlags flags = getAbilityFlags(nearby.getUniqueId());
                    flags.dragonCurseDamage = 1.0 + damageIncrease;
                }
            }
        }

        // WITHER Passive: Curse of Despair - healing -25% +1% per level, cooldowns +10s -1s per level
        if (data.getAttribute() == AttributeType.WITHER) {
            player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE,
                    player.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0.01);
            AbilityFlags flags = getAbilityFlags(player.getUniqueId());
            flags.healingReduction = 0.25 - (level - 1) * 0.01;
        }

        // WARDEN Passive: Curse of Silence - -15% attack speed when idle
        if (data.getAttribute() == AttributeType.WARDEN) {
            player.getWorld().spawnParticle(org.bukkit.Particle.SCULK_SOUL,
                    player.getLocation(), 2, 0.3, 0, 0.3, 0.01);
        }

        // BREEZE Passive: Curse of Judgment - cooldowns increase out of combat, -25% healing, -5% move speed idle
        if (data.getAttribute() == AttributeType.BREEZE) {
            boolean enemyNearby = false;
            for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                if (entity instanceof Player) {
                    enemyNearby = true;
                    break;
                }
            }
            if (!enemyNearby && player.getTicksLived() % 200 == 0) {
                data.setCooldown("melee", data.getRemainingCooldown("melee") + 1000);
                data.setCooldown("support", data.getRemainingCooldown("support") + 1000);
            }
        }

        // WEALTH Passive: Industrialist - permanent Hero of the Village 12
        if (data.getAttribute() == AttributeType.WEALTH) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 11, true, false, false));
        }

        // TRANSFER Passive: Cleanse - immune to debuffs
        if (data.getAttribute() == AttributeType.TRANSFER) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (isNegativeEffect(effect.getType())) {
                    player.removePotionEffect(effect.getType());
                }
            }
        }

        // TEMPO Passive: Momentum - permanent Speed I
        if (data.getAttribute() == AttributeType.TEMPO) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
        }

        // RISK Passive: Gambler's Edge - below 40% HP gain +10% +1%/level max 15% damage
        if (data.getAttribute() == AttributeType.RISK) {
            double healthPercent = player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            AbilityFlags flags = getAbilityFlags(player.getUniqueId());
            if (healthPercent < 0.4) {
                flags.riskPassiveDamageBonus = Math.min(0.15, 0.10 + (level - 1) * 0.01);
            } else {
                flags.riskPassiveDamageBonus = 0.0;
            }
        }

        // PRESSURE Passive: Oppression - nearby enemies below 50% HP take more damage
        if (data.getAttribute() == AttributeType.PRESSURE) {
            double bonusDamage = Math.min(0.25, 0.10 + (level - 1) * 0.03);
            for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
                if (entity instanceof Player && entity != player) {
                    Player nearby = (Player) entity;
                    double nearbyHealthPercent = nearby.getHealth() / nearby.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    AbilityFlags flags = getAbilityFlags(nearby.getUniqueId());
                    if (nearbyHealthPercent < 0.5) {
                        flags.oppressionDamageBonus = bonusDamage;
                    } else {
                        flags.oppressionDamageBonus = 0.0;
                    }
                }
            }
        }

        // RANGE Passive: Footwork - bows/crossbows do 45% +1%/level max 50% more damage
        // (Handled in damage event via rangePassiveDamageBonus flag)
        if (data.getAttribute() == AttributeType.RANGE) {
            AbilityFlags flags = getAbilityFlags(player.getUniqueId());
            flags.rangePassiveDamageBonus = Math.min(0.50, 0.45 + (level - 1) * 0.01);
        }

        // DEFENSE Passive: Hardened - armor breaks slower (5% +1%/level)
        // (Handled in damage event via hardenedReduction flag)
        if (data.getAttribute() == AttributeType.DEFENSE) {
            AbilityFlags flags = getAbilityFlags(player.getUniqueId());
            flags.hardenedReduction = 0.05 + (level - 1) * 0.01;
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
                type == PotionEffectType.GLOWING;
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
        public boolean itemDisabled = false;
        public int disabledItemSlot = -1;

        // Range
        public boolean homingArrows = false;
        public UUID cannotApproach = null;
        public long cannotApproachUntil = 0;
        public double rangePassiveDamageBonus = 0.0;

        // Tempo
        public double attackSpeedBonus = 0.0;
        public boolean stunned = false;

        // Vision
        public boolean trueSight = false;

        // Transfer
        public boolean redirectionActive = false;
        public org.bukkit.Location redirectionCenter = null;
        public double redirectionRadius = 0.0;

        // Speed
        public double speedBonus = 0.0;
        public boolean lightningStruck = false;
        public long speedAdrenalineCooldown = 0;

        // Combat
        public double armorNegation = 0.0;
        public double healingReduction = 0.0;

        // Pressure
        public double pressureDamageDealt = 0.0;
        public double pressureDamageTaken = 0.0;
        public double vulnerabilityMultiplier = 1.0;
        public double oppressionDamageBonus = 0.0;

        // Disruption
        public boolean disruptionDesyncUsed = false;
        public long disruptionDesyncCooldown = 0;

        // Risk
        public double riskDamageBonus = 0.0;
        public double riskDamageTaken = 0.0;
        public double riskSelfDamageTaken = 0.0;
        public double riskPassiveDamageBonus = 0.0;

        // Knockback
        public double knockbackResistance = 0.0;

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

        // Marked damage multiplier
        public double damageTakenMultiplier = 1.0;
        public boolean markedForDamage = false;
    }
}
