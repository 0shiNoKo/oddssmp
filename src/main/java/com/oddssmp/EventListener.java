package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EventListener implements Listener {

    private final OddsSMP plugin;

    public EventListener(OddsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Initialize player data if not exists
        if (plugin.getPlayerData(player.getUniqueId()) == null) {
            plugin.setPlayerData(player.getUniqueId(), new PlayerData());
        }

        // Update tab display
        plugin.updatePlayerTab(player);

        // Check if player has Dragon Egg
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data != null && data.getAttribute() == AttributeType.DRAGON_EGG) {
            applyDragonEggEffects(player);
        }

        // Apply passive max health bonuses
        applyMaxHealthBonus(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.savePlayerData(event.getPlayer().getUniqueId());
    }

    /**
     * Handle combat damage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        PlayerData attackerData = plugin.getPlayerData(attacker.getUniqueId());
        if (attackerData == null || attackerData.getAttribute() == null) return;

        AbilityManager.AbilityFlags attackerFlags = plugin.getAbilityManager().getAbilityFlags(attacker.getUniqueId());

        // Update last hit time
        attackerData.setLastHitTime(System.currentTimeMillis());

        // Apply melee ability if ready
        plugin.getAbilityManager().applyMeleeOnHit(attacker, event.getEntity());

        // MELEE PASSIVE: Bloodlust - bonus damage from kills
        if (attackerData.getAttribute() == AttributeType.MELEE && attackerFlags.meleeBloodlustStacks > 0) {
            event.setDamage(event.getDamage() * (1.0 + attackerFlags.meleeBloodlustStacks));
        }

        // MELEE SUPPORT: Battle Fervor - bonus damage
        if (attackerFlags.meleeDamageBonus > 0) {
            event.setDamage(event.getDamage() * (1.0 + attackerFlags.meleeDamageBonus));
        }

        // TEMPO PASSIVE: Momentum - attack speed stacks
        if (attackerData.getAttribute() == AttributeType.TEMPO) {
            double maxStacks = 0.10 + (attackerData.getLevel() - 1) * 0.02;
            if (attackerFlags.momentumStacks < maxStacks) {
                attackerFlags.momentumStacks += 0.02;
            }
        }

        // SPEED PASSIVE: Adrenaline - movement speed stacks
        if (attackerData.getAttribute() == AttributeType.SPEED) {
            double maxStacks = 0.10 + (attackerData.getLevel() - 1) * 0.02; // 10% to 20%
            if (attackerFlags.speedAdrenalineStacks < maxStacks) {
                attackerFlags.speedAdrenalineStacks += 0.02;
                attackerFlags.speedLastCombatTime = System.currentTimeMillis();
            }
        }

        // DISRUPTION PASSIVE: Desync - first hit adds cooldown
        if (attackerData.getAttribute() == AttributeType.DISRUPTION && event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            PlayerData targetData = plugin.getPlayerData(target.getUniqueId());
            AbilityManager.AbilityFlags targetFlags = plugin.getAbilityManager().getAbilityFlags(target.getUniqueId());

            if (targetData != null && !targetFlags.disruptionDesyncUsed) {
                int cooldownAdd = 10 + (attackerData.getLevel() - 1) * 2; // 10s to 20s
                targetData.setCooldown("melee", targetData.getRemainingCooldown("melee") + (cooldownAdd * 1000));
                targetData.setCooldown("support", targetData.getRemainingCooldown("support") + (cooldownAdd * 1000));
                targetFlags.disruptionDesyncUsed = true;
                targetFlags.disruptionDesyncCooldown = System.currentTimeMillis() + 30000; // Reset after 30s
                target.sendMessage("§cDesync! +" + cooldownAdd + "s to next ability!");
            }
        }

        // CONTROL PASSIVE: Suppression - add cooldown once per fight
        if (attackerData.getAttribute() == AttributeType.CONTROL && event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            PlayerData targetData = plugin.getPlayerData(target.getUniqueId());
            AbilityManager.AbilityFlags targetFlags = plugin.getAbilityManager().getAbilityFlags(target.getUniqueId());

            if (targetData != null && (targetFlags.suppressionCooldownAdded == 0 ||
                    System.currentTimeMillis() - targetFlags.suppressionCooldownAdded > 60000)) {
                int cooldownAdd = 10 + attackerData.getLevel(); // 10s to 15s
                targetData.setCooldown("melee", targetData.getRemainingCooldown("melee") + (cooldownAdd * 1000));
                targetData.setCooldown("support", targetData.getRemainingCooldown("support") + (cooldownAdd * 1000));
                targetFlags.suppressionCooldownAdded = System.currentTimeMillis();
                target.sendMessage("§c+" + cooldownAdd + "s ability cooldown!");
            }
        }

        // Handle victim's defensive mechanics
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PlayerData victimData = plugin.getPlayerData(victim.getUniqueId());

            if (victimData != null && victimData.getAttribute() != null) {
                AbilityManager.AbilityFlags victimFlags = plugin.getAbilityManager().getAbilityFlags(victim.getUniqueId());

                // DEFENSE PASSIVE: Hardened - permanent damage reduction
                if (victimData.getAttribute() == AttributeType.DEFENSE) {
                    double reduction = 0.05 + (victimData.getLevel() - 1) * 0.01;
                    event.setDamage(event.getDamage() * (1.0 - reduction));
                }

                // DEFENSE MELEE: Iron Response
                if (victimFlags.ironResponse > 0) {
                    event.setDamage(event.getDamage() * (1.0 - victimFlags.ironResponse));
                }

                // DEFENSE SUPPORT: Shield Wall
                if (victimFlags.damageReduction > 0) {
                    event.setDamage(event.getDamage() * (1.0 - victimFlags.damageReduction));
                }

                // PERSISTENCE PASSIVE: Endure - damage reduction below 50% HP
                if (victimData.getAttribute() == AttributeType.PERSISTENCE) {
                    if (victim.getHealth() / victim.getMaxHealth() < 0.5) {
                        double reduction = 0.10 + (victimData.getLevel() - 1) * 0.05;
                        event.setDamage(event.getDamage() * (1.0 - reduction));
                    }
                }

                // PERSISTENCE MELEE: Stored Pain - store damage
                if (victimFlags.persistenceStorageActive) {
                    victimFlags.persistenceStoredDamage += event.getDamage() * 0.25;
                }

                // VISION MELEE: Target Mark - increased damage taken
                if (victimFlags.markedForDamage) {
                    event.setDamage(event.getDamage() * victimFlags.damageTakenMultiplier);
                }

                // Armor negation
                if (attackerFlags.armorNegation > 0) {
                    double armorDamage = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
                    event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armorDamage * (1.0 - attackerFlags.armorNegation));
                }

                // Dragon Curse - increased damage taken
                if (victimFlags.dragonCurseDamage > 1.0) {
                    event.setDamage(event.getDamage() * victimFlags.dragonCurseDamage);
                }

                // Breeze Judged - increased damage taken
                if (victimFlags.breezeJudged) {
                    event.setDamage(event.getDamage() * (1.0 + victimFlags.breezeJudgeDamageBonus));
                }

                // Warden Domain effects
                if (victimFlags.wardenDomainDamageReduction > 0) {
                    event.setDamage(event.getDamage() * (1.0 - victimFlags.wardenDomainDamageReduction));
                }

                // Breeze Trial Order damage reduction
                if (victimFlags.breezeDamageReduction > 0) {
                    event.setDamage(event.getDamage() * (1.0 - victimFlags.breezeDamageReduction));
                }

                // Dragon Dominion damage bonus
                if (attackerFlags.dragonDominionDamage > 1.0) {
                    event.setDamage(event.getDamage() * attackerFlags.dragonDominionDamage);
                }

                // PRESSURE PASSIVE: Oppression - enemies below 50% HP take more damage
                if (attackerData.getAttribute() == AttributeType.PRESSURE) {
                    if (victim.getHealth() / victim.getMaxHealth() < 0.5) {
                        double oppressionBonus = 0.10 + (attackerData.getLevel() - 1) * 0.03; // 10% to 25%
                        event.setDamage(event.getDamage() * (1.0 + oppressionBonus));
                    }
                }

                // PRESSURE SUPPORT: Intimidation Field - damage reduction
                if (victimFlags.pressureDamageDealt > 0) {
                    event.setDamage(event.getDamage() * (1.0 - victimFlags.pressureDamageDealt));
                }

                // PRESSURE MELEE/SUPPORT: Vulnerability - take more damage
                if (victimFlags.vulnerabilityMultiplier > 1.0) {
                    event.setDamage(event.getDamage() * victimFlags.vulnerabilityMultiplier);
                }
                if (victimFlags.pressureDamageTaken > 0) {
                    event.setDamage(event.getDamage() * (1.0 + victimFlags.pressureDamageTaken));
                }

                // ANCHOR PASSIVE: Immobile - damage reduction while standing still
                if (victimData.getAttribute() == AttributeType.ANCHOR) {
                    long timeSinceMove = System.currentTimeMillis() - victimFlags.anchorLastMovedTime;
                    if (timeSinceMove >= 2000) { // 2 seconds
                        double reduction = 0.10 + (victimData.getLevel() - 1) * 0.05; // 10% to 35%
                        event.setDamage(event.getDamage() * (1.0 - reduction));
                    }
                }

                // RISK PASSIVE: Gambler's Edge - bonus damage below 40% HP
                if (attackerData.getAttribute() == AttributeType.RISK) {
                    if (attacker.getHealth() / attacker.getMaxHealth() < 0.4) {
                        double gamblerBonus = 0.20 + (attackerData.getLevel() - 1) * 0.05; // 20% to 45%
                        event.setDamage(event.getDamage() * (1.0 + gamblerBonus));
                    }
                }

                // RISK SUPPORT: Double Or Nothing damage bonus/penalty
                if (attackerFlags.riskDamageBonus > 0) {
                    event.setDamage(event.getDamage() * (1.0 + attackerFlags.riskDamageBonus));
                }
                if (victimFlags.riskDamageTaken > 0) {
                    event.setDamage(event.getDamage() * (1.0 + victimFlags.riskDamageTaken));
                }

                // RISK MELEE: All In self-damage penalty
                if (attackerFlags.riskSelfDamageTaken > 0 && victim.equals(attacker)) {
                    event.setDamage(event.getDamage() * (1.0 + attackerFlags.riskSelfDamageTaken));
                }
            }
        }

        // HEALTH MELEE: Vampiric Hit - heal from damage
        if (attackerFlags.vampiricActive) {
            double healAmount = event.getDamage() * attackerFlags.vampiricPercent;

            if (attacker.getHealth() + healAmount > attacker.getMaxHealth()) {
                double overheal = (attacker.getHealth() + healAmount) - attacker.getMaxHealth();
                attacker.setHealth(attacker.getMaxHealth());
                attacker.setAbsorptionAmount(attacker.getAbsorptionAmount() + overheal);
            } else {
                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + healAmount));
            }
        }

        // RANGE PASSIVE: Footwork - prevent sprint toward attacker
        if (attackerData.getAttribute() == AttributeType.RANGE && event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            AbilityManager.AbilityFlags targetFlags = plugin.getAbilityManager().getAbilityFlags(target.getUniqueId());
            int duration = 5 + attackerData.getLevel();
            targetFlags.cannotApproach = attacker.getUniqueId();
            targetFlags.cannotApproachUntil = System.currentTimeMillis() + (duration * 1000);
        }
    }

    /**
     * Handle general damage (survival, last stand, etc.)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) return;

        AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());

        // PERSISTENCE SUPPORT: Last Stand - cannot drop below 1 heart
        if (flags.lastStand && player.getHealth() - event.getFinalDamage() < 0.5) {
            event.setCancelled(true);
            player.setHealth(0.5);
            flags.lastStand = false;
            player.sendMessage("§6Last Stand saved you!");
            return;
        }
    }

    /**
     * Handle player death
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        if (data == null) return;

        // Increment deaths and decrease level
        data.incrementDeaths();
        data.decrementLevel();

        // HEALTH PASSIVE: Vitality - lose hearts on death
        if (data.getAttribute() == AttributeType.HEALTH) {
            AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());
            if (flags.vitalityHearts > 0) {
                flags.vitalityHearts -= 2.0; // Lose 1 heart
                if (flags.vitalityHearts < 0) flags.vitalityHearts = 0;
                applyMaxHealthBonus(player);
            }
        }

        // MELEE PASSIVE: Bloodlust - lose all stacks
        if (data.getAttribute() == AttributeType.MELEE) {
            AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());
            flags.meleeBloodlustStacks = 0.0;
        }

        // TEMPO PASSIVE: Momentum - lose all stacks
        if (data.getAttribute() == AttributeType.TEMPO) {
            AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());
            flags.momentumStacks = 0.0;
        }

        // SPEED PASSIVE: Adrenaline - lose all stacks
        if (data.getAttribute() == AttributeType.SPEED) {
            AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());
            flags.speedAdrenalineStacks = 0.0;
        }

        player.sendMessage("§cYou died! Level decreased to " + data.getLevel());

        // Handle killer
        Player killer = player.getKiller();
        if (killer != null) {
            PlayerData killerData = plugin.getPlayerData(killer.getUniqueId());
            if (killerData != null) {
                killerData.incrementKills();
                killerData.incrementLevel();

                // MELEE PASSIVE: Bloodlust - gain stacks
                if (killerData.getAttribute() == AttributeType.MELEE) {
                    AbilityManager.AbilityFlags killerFlags = plugin.getAbilityManager().getAbilityFlags(killer.getUniqueId());
                    double maxStacks = 0.10 + (killerData.getLevel() - 1) * 0.02;
                    killerFlags.meleeBloodlustStacks = Math.min(maxStacks, killerFlags.meleeBloodlustStacks + 0.02);
                    killer.sendMessage("§aBloodlust: +" + (int)(killerFlags.meleeBloodlustStacks * 100) + "% damage");
                }

                // HEALTH PASSIVE: Vitality - gain hearts
                if (killerData.getAttribute() == AttributeType.HEALTH) {
                    AbilityManager.AbilityFlags killerFlags = plugin.getAbilityManager().getAbilityFlags(killer.getUniqueId());
                    double heartsPerLevel = 2.0 * killerData.getLevel();
                    killerFlags.vitalityHearts = Math.min(heartsPerLevel, killerFlags.vitalityHearts + 2.0);
                    applyMaxHealthBonus(killer);
                    killer.sendMessage("§aVitality: +" + (int)(killerFlags.vitalityHearts / 2.0) + " hearts");
                }

                killer.sendMessage("§aKill! Level increased to " + killerData.getLevel());
            }
        }

        // Update tab displays
        plugin.updatePlayerTab(player);
        if (killer != null) {
            plugin.updatePlayerTab(killer);
        }
    }

    /**
     * Handle entity death for Wealth plunder
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        PlayerData data = plugin.getPlayerData(killer.getUniqueId());
        if (data == null || data.getAttribute() != AttributeType.WEALTH) return;

        AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(killer.getUniqueId());

        // WEALTH MELEE: Plunder Kill
        if (flags.plunderKillReady && !(event.getEntity() instanceof Player)) {
            // Check if boss
            String entityType = event.getEntity().getType().toString();
            if (entityType.contains("WITHER") || entityType.contains("DRAGON") || entityType.contains("WARDEN")) {
                killer.sendMessage("§cPlunder Kill doesn't work on bosses!");
                flags.plunderKillReady = false;
                return;
            }

            double multiplier = flags.plunderMultiplier;

            // Multiply drops
            for (ItemStack drop : event.getDrops()) {
                int newAmount = (int)(drop.getAmount() * multiplier);
                drop.setAmount(newAmount);
            }

            // Rare drop extra roll
            for (ItemStack drop : new java.util.ArrayList<>(event.getDrops())) {
                if (isRareDrop(drop)) {
                    event.getDrops().add(drop.clone());
                }
            }

            killer.sendMessage("§6Plunder Kill! " + String.format("%.2f", multiplier) + "x drops!");
            flags.plunderKillReady = false;
        }

        // WEALTH PASSIVE: Industrialist - mob drop bonus
        if (!flags.plunderKillReady && !(event.getEntity() instanceof Player)) {
            double bonus = 0.10 * data.getLevel();
            for (ItemStack drop : event.getDrops()) {
                int bonusAmount = (int)Math.ceil(drop.getAmount() * bonus);
                drop.setAmount(drop.getAmount() + bonusAmount);
            }
        }
    }

    /**
     * Handle player movement
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        if (data == null || data.getAttribute() == null) return;

        AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());

        // RANGE MELEE/PASSIVE: Cannot approach attacker
        if (flags.cannotApproach != null && System.currentTimeMillis() < flags.cannotApproachUntil) {
            Player target = Bukkit.getPlayer(flags.cannotApproach);
            if (target != null && player.isSprinting()) {
                Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());
                Vector moveDirection = event.getTo().toVector().subtract(event.getFrom().toVector());

                if (toTarget.normalize().dot(moveDirection.normalize()) > 0.5) {
                    player.setSprinting(false);
                }
            }
        } else if (flags.cannotApproach != null) {
            flags.cannotApproach = null;
        }

        // BREEZE PASSIVE: Curse of Judgment - movement speed reduction
        if (data.getAttribute() == AttributeType.BREEZE) {
            long lastHit = System.currentTimeMillis() - data.getLastHitTime();
            if (lastHit > 3000) { // Not attacking for 3 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 0, true, false));
            }
        }

        // ANCHOR PASSIVE: Track last moved time
        if (data.getAttribute() == AttributeType.ANCHOR) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                flags.anchorLastMovedTime = System.currentTimeMillis();
            }
        }

        // SPEED PASSIVE: Adrenaline decay after 6s
        if (data.getAttribute() == AttributeType.SPEED) {
            long timeSinceCombat = System.currentTimeMillis() - flags.speedLastCombatTime;
            if (timeSinceCombat > 6000 && flags.speedAdrenalineStacks > 0) {
                flags.speedAdrenalineStacks -= 0.01; // Decay
                if (flags.speedAdrenalineStacks < 0) flags.speedAdrenalineStacks = 0;
            }
        }

        // ANCHOR MELEE: Rooted check
        if (flags.rooted) {
            event.setCancelled(true);
        }

        // DISRUPTION PASSIVE: Reset desync after cooldown
        if (flags.disruptionDesyncUsed && System.currentTimeMillis() > flags.disruptionDesyncCooldown) {
            flags.disruptionDesyncUsed = false;
        }
    }

    /**
     * Handle potion effects
     */
    @EventHandler
    public void onPotionEffect(org.bukkit.event.entity.EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) return;

        // TRANSFER PASSIVE: Cannot get debuffs
        if (data.getAttribute() == AttributeType.TRANSFER && event.getNewEffect() != null) {
            if (isHarmfulEffect(event.getNewEffect().getType())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Apply max health bonus from passives
     */
    private void applyMaxHealthBonus(Player player) {
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) return;

        AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());

        // HEALTH PASSIVE: Vitality
        if (data.getAttribute() == AttributeType.HEALTH) {
            double bonus = flags.vitalityHearts;
            try {
                org.bukkit.attribute.AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(20.0 + bonus);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not set max health: " + e.getMessage());
            }
        } else {
            // Reset to default
            try {
                org.bukkit.attribute.AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(20.0);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Remove attribute-specific effects when changing attributes
     */
    public void removeAttributeEffects(Player player, AttributeType oldAttribute) {
        if (oldAttribute == null) return;

        // Remove Health passive hearts
        if (oldAttribute == AttributeType.HEALTH) {
            try {
                org.bukkit.attribute.AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(20.0);
                }
                // Reset health to max (but not above 20)
                player.setHealth(Math.min(20.0, player.getHealth()));
            } catch (Exception e) {
                plugin.getLogger().warning("Could not reset health: " + e.getMessage());
            }
        }

        // Remove Dragon Egg effects
        if (oldAttribute == AttributeType.DRAGON_EGG) {
            removeDragonEggEffects(player);
        }

        // Clear ability flags
        plugin.getAbilityManager().removeAbilityFlags(player.getUniqueId());
    }

    /**
     * Apply Dragon Egg effects
     */
    public void applyDragonEggEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                player.setScoreboard(scoreboard);
            }

            org.bukkit.scoreboard.Team team = scoreboard.getTeam("dragonEgg");
            if (team == null) {
                team = scoreboard.registerNewTeam("dragonEgg");
            }
            team.setColor(org.bukkit.ChatColor.DARK_PURPLE);
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply Dragon Egg team color: " + e.getMessage());
        }
    }

    /**
     * Remove Dragon Egg effects
     */
    public void removeDragonEggEffects(Player player) {
        player.removePotionEffect(PotionEffectType.GLOWING);

        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard != null) {
                org.bukkit.scoreboard.Team team = scoreboard.getTeam("dragonEgg");
                if (team != null && team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove Dragon Egg team color: " + e.getMessage());
        }
    }

    /**
     * Check if rare drop
     */
    private boolean isRareDrop(ItemStack item) {
        Material type = item.getType();
        return type.toString().contains("SKULL") ||
                type == Material.TRIDENT ||
                type == Material.TOTEM_OF_UNDYING ||
                type == Material.ELYTRA ||
                type == Material.DRAGON_HEAD;
    }

    /**
     * Check if harmful effect
     */
    private boolean isHarmfulEffect(PotionEffectType type) {
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
                type == PotionEffectType.DARKNESS;
    }
}