package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
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

        // Load player data from file (creates new if not exists)
        plugin.loadPlayerData(player.getUniqueId());

        // Update tab display
        plugin.updatePlayerTab(player);

        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        // Check if player has boss items in inventory but not the attribute
        if (data != null) {
            // Dragon Egg
            if (data.getAttribute() != AttributeType.DRAGON_EGG && player.getInventory().contains(Material.DRAGON_EGG)) {
                grantBossAttribute(player, AttributeType.DRAGON_EGG, "DRAGON EGG", "§6");
                return;
            }

            // Wither Bone (Coal Block with special name)
            if (data.getAttribute() != AttributeType.WITHER && hasWitherBone(player)) {
                grantBossAttribute(player, AttributeType.WITHER, "WITHER BONE", "§8");
                return;
            }

            // Warden Brain (Sculk Catalyst with special name)
            if (data.getAttribute() != AttributeType.WARDEN && hasWardenBrain(player)) {
                grantBossAttribute(player, AttributeType.WARDEN, "WARDEN BRAIN", "§3");
                return;
            }

            // Breeze Heart (Wind Charge with special name)
            if (data.getAttribute() != AttributeType.BREEZE && hasBreezeHeart(player)) {
                grantBossAttribute(player, AttributeType.BREEZE, "BREEZE HEART", "§b");
                return;
            }
        }

        // Check if player has Dragon Egg attribute
        if (data != null && data.getAttribute() == AttributeType.DRAGON_EGG) {
            applyDragonEggEffects(player);
        }

        // Apply passive max health bonuses
        applyMaxHealthBonus(player);

        // Auto-assign attribute if enabled and player doesn't have one (INSTANT with animation)
        if (plugin.isAutoAssignEnabled() && (data == null || data.getAttribute() == null)) {
            // Run slot animation and assign
            playSlotAnimationAndAssign(player);
        }
    }

    /**
     * Play slot machine animation and assign random attribute
     */
    public void playSlotAnimationAndAssign(Player player) {
        // Pre-determine the final result
        AttributeType finalAttribute = AttributeType.getRandomAttribute(false);
        Tier finalTier = Tier.getRandomTier();

        // Get all possible attributes for animation
        AttributeType[] allAttributes = AttributeType.values();

        new org.bukkit.scheduler.BukkitRunnable() {
            int tick = 0;
            int delay = 1; // Start fast
            int nextSwitch = 0;
            int currentIndex = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                tick++;

                // Check if it's time to switch attribute
                if (tick >= nextSwitch) {
                    // Show current attribute
                    AttributeType showAttr;
                    Tier showTier;

                    // After 40 ticks, start slowing down and eventually show final
                    if (tick > 60) {
                        // Final reveal
                        showAttr = finalAttribute;
                        showTier = finalTier;

                        // Show final result
                        player.sendTitle(
                            showTier.getColor() + "§l" + showAttr.getIcon() + " " + showAttr.getDisplayName(),
                            "§7Your attribute has been chosen!",
                            0, 60, 20
                        );

                        // Play success sound
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

                        // Actually assign the attribute
                        PlayerData newData = new PlayerData(finalAttribute, finalTier);
                        plugin.setPlayerData(player.getUniqueId(), newData);

                        // Play particles
                        ParticleManager.playSupportParticles(player, finalAttribute, finalTier, 1);

                        // Update tab
                        plugin.updatePlayerTab(player);

                        // Send chat message
                        player.sendMessage("");
                        player.sendMessage("§6§l✦ §aYou have been assigned an attribute! §6§l✦");
                        player.sendMessage("  " + finalTier.getColor() + finalTier.name() + " " + finalAttribute.getIcon() + " " + finalAttribute.getDisplayName());
                        player.sendMessage("");

                        cancel();
                        return;
                    }

                    // Still spinning - show random attribute
                    currentIndex = (currentIndex + 1) % allAttributes.length;
                    // Skip boss attributes in animation
                    while (allAttributes[currentIndex].isBossAttribute() || allAttributes[currentIndex].isDragonEgg()) {
                        currentIndex = (currentIndex + 1) % allAttributes.length;
                    }

                    showAttr = allAttributes[currentIndex];
                    showTier = Tier.values()[(int)(Math.random() * 3)];

                    // Show spinning title
                    player.sendTitle(
                        showTier.getColor() + showAttr.getIcon() + " " + showAttr.getDisplayName(),
                        "§e§l« ROLLING »",
                        0, 10, 0
                    );

                    // Play tick sound
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f + (tick * 0.01f));

                    // Calculate next switch time (slows down over time)
                    if (tick < 20) {
                        delay = 2; // Fast
                    } else if (tick < 35) {
                        delay = 4; // Medium
                    } else if (tick < 50) {
                        delay = 6; // Slow
                    } else {
                        delay = 10; // Very slow
                    }

                    nextSwitch = tick + delay;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Grant boss attribute and broadcast
     */
    private void grantBossAttribute(Player player, AttributeType attribute, String itemName, String color) {
        PlayerData newData = new PlayerData(attribute, Tier.EXTREME);
        plugin.setPlayerData(player.getUniqueId(), newData);

        ParticleManager.playSupportParticles(player, attribute, Tier.EXTREME, 1);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§c§l⚠ " + color + "§l" + itemName + " OBTAINED §c§l⚠");
        Bukkit.broadcastMessage("§e" + player.getName() + " §7has claimed the");
        Bukkit.broadcastMessage("  " + color + "§lLEGENDARY " + itemName + "!");
        Bukkit.broadcastMessage("");

        if (attribute == AttributeType.DRAGON_EGG) {
            applyDragonEggEffects(player);
        }

        plugin.updatePlayerTab(player);
        player.sendMessage(color + "§l§kA§r §c§lYOU RECEIVED THE " + itemName + " " + color + "§l§kA");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.savePlayerData(event.getPlayer().getUniqueId());
    }

    /**
     * Prevent breaking altar blocks
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.isAltarProtectedBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cThis block is part of a weapon altar and cannot be broken!");
        }
    }

    /**
     * Handle right-click with Upgrader/Reroller items
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Check for weapon altar interaction (right-click block near altar)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Location clickLoc = event.getClickedBlock().getLocation();
            WeaponAltar altar = plugin.findAltarNear(clickLoc, 3.0);

            if (altar != null) {
                event.setCancelled(true);

                // Check if sneaking to view requirements, otherwise try to craft
                if (player.isSneaking()) {
                    // Show requirements
                    player.sendMessage("");
                    player.sendMessage(altar.getWeapon().getColor() + "§l" + altar.getWeapon().getName() + " §7Requirements:");
                    for (String req : WeaponAltar.getCraftingRequirements(altar.getWeapon())) {
                        player.sendMessage("  " + req);
                    }
                    player.sendMessage("");
                    player.sendMessage("§7Right-click (without sneaking) to craft!");
                } else {
                    // Try to craft
                    if (altar.hasRequiredMaterials(player)) {
                        altar.craftWeapon(player);
                    } else {
                        player.sendMessage("§cYou don't have the required materials!");
                        player.sendMessage("§7Hold SHIFT and right-click to see requirements.");
                    }
                }
                return;
            }
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        // Handle Upgrader
        if (OddsSMP.isUpgrader(item)) {
            event.setCancelled(true);

            if (data == null || data.getAttribute() == null) {
                player.sendMessage("§cYou don't have an attribute to upgrade!");
                return;
            }

            if (data.getLevel() >= 5) {
                player.sendMessage("§cYour attribute is already at max level (5)!");
                return;
            }

            // Consume item
            item.setAmount(item.getAmount() - 1);

            // Upgrade level
            int oldLevel = data.getLevel();
            data.incrementLevel();

            // Effects
            ParticleManager.playSupportParticles(player, data.getAttribute(), data.getTier(), data.getLevel());
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

            // Save and update
            plugin.savePlayerData(player.getUniqueId());
            plugin.updatePlayerTab(player);

            player.sendMessage("§a§l✦ UPGRADED! §7" + data.getAttribute().getDisplayName() +
                    " §aLevel " + oldLevel + " → " + data.getLevel() + "!");
            return;
        }

        // Handle Tier Upgrader
        if (OddsSMP.isTierUpgrader(item)) {
            event.setCancelled(true);

            if (data == null || data.getAttribute() == null) {
                player.sendMessage("§cYou don't have an attribute to upgrade!");
                return;
            }

            Tier currentTier = data.getTier();
            Tier newTier = null;

            // Determine next tier
            if (currentTier == Tier.STABLE) {
                newTier = Tier.WARPED;
            } else if (currentTier == Tier.WARPED) {
                newTier = Tier.EXTREME;
            } else {
                player.sendMessage("§cYour attribute is already at the highest tier (Extreme)!");
                return;
            }

            // Consume item
            item.setAmount(item.getAmount() - 1);

            // Upgrade tier
            data.setTier(newTier);

            // Effects
            ParticleManager.playSupportParticles(player, data.getAttribute(), newTier, data.getLevel());
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 1.5f);

            // Save and update
            plugin.savePlayerData(player.getUniqueId());
            plugin.updatePlayerTab(player);

            player.sendMessage("");
            player.sendMessage("§5§l§m                                                    ");
            player.sendMessage("§5§l⚡ TIER UPGRADED! ⚡");
            player.sendMessage("");
            player.sendMessage("  §7" + data.getAttribute().getIcon() + " " + data.getAttribute().getDisplayName());
            player.sendMessage("  " + currentTier.getColor() + currentTier.name() + " §7→ " + newTier.getColor() + "§l" + newTier.name());
            player.sendMessage("");
            player.sendMessage("§5§l§m                                                    ");
            player.sendMessage("");
            return;
        }

        // Handle Reroller
        if (OddsSMP.isReroller(item)) {
            event.setCancelled(true);

            if (data == null || data.getAttribute() == null) {
                player.sendMessage("§cYou don't have an attribute to reroll!");
                return;
            }

            // Don't allow rerolling boss attributes
            if (data.getAttribute().isBossAttribute() || data.getAttribute().isDragonEgg()) {
                player.sendMessage("§cYou cannot reroll boss attributes!");
                return;
            }

            // Consume item
            item.setAmount(item.getAmount() - 1);

            // Get old attribute for message
            AttributeType oldAttr = data.getAttribute();

            // Remove old effects
            removeAttributeEffects(player, oldAttr);

            // Get new random attribute
            AttributeType newAttr = AttributeType.getRandomAttribute(false);
            Tier newTier = Tier.getRandomTier();

            // Set new data (level resets to 1)
            PlayerData newData = new PlayerData(newAttr, newTier);
            plugin.setPlayerData(player.getUniqueId(), newData);

            // Effects
            ParticleManager.playSupportParticles(player, newAttr, newTier, 1);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 1.2f);

            // Update tab
            plugin.updatePlayerTab(player);

            player.sendMessage("§d§l✦ REROLLED! §7" + oldAttr.getDisplayName() + " §d→ " +
                    newTier.getColor() + newTier.name() + " " + newAttr.getIcon() + " " + newAttr.getDisplayName() + "§d!");
            return;
        }
    }

    /**
     * Handle boss item pickup - grants respective attribute
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        // Dragon Egg
        if (item.getType() == Material.DRAGON_EGG) {
            grantBossAttribute(player, AttributeType.DRAGON_EGG, "DRAGON EGG", "§6");
            applyDragonEggEffects(player);
            return;
        }

        // Wither Bone
        if (isWitherBone(item)) {
            grantBossAttribute(player, AttributeType.WITHER, "WITHER BONE", "§8");
            return;
        }

        // Warden Brain
        if (isWardenBrain(item)) {
            grantBossAttribute(player, AttributeType.WARDEN, "WARDEN BRAIN", "§3");
            return;
        }

        // Breeze Heart
        if (isBreezeHeart(item)) {
            grantBossAttribute(player, AttributeType.BREEZE, "BREEZE HEART", "§b");
            return;
        }
    }

    /**
     * Prevent dropping boss items
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop the Dragon Egg!");
            return;
        }

        if (isWitherBone(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop the Wither Bone!");
            return;
        }

        if (isWardenBrain(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop the Warden Brain!");
            return;
        }

        if (isBreezeHeart(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop the Breeze Heart!");
        }
    }

    /**
     * Prevent putting boss items in containers
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Only check if clicking in a non-player inventory
        InventoryType topType = event.getView().getTopInventory().getType();
        if (topType == InventoryType.PLAYER || topType == InventoryType.CRAFTING) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Shift-clicking boss item from player inventory into container
        if (event.isShiftClick() && current != null && event.getClickedInventory() == player.getInventory()) {
            if (isBossItem(current)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot store this item!");
                return;
            }
        }

        // Placing boss item directly into container slot
        if (cursor != null && isBossItem(cursor)) {
            if (event.getClickedInventory() != null && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot store this item!");
            }
        }
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

        // RISK PASSIVE: Gambler's Edge - bonus damage below 40% HP
        if (attackerFlags.riskPassiveDamageBonus > 0) {
            event.setDamage(event.getDamage() * (1.0 + attackerFlags.riskPassiveDamageBonus));
        }

        // DISRUPTION PASSIVE: Desync - first hit per fight adds cooldowns (+10s base +2s/level, max 20s)
        if (attackerData.getAttribute() == AttributeType.DISRUPTION && event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            PlayerData targetData = plugin.getPlayerData(target.getUniqueId());
            AbilityManager.AbilityFlags targetFlags = plugin.getAbilityManager().getAbilityFlags(target.getUniqueId());

            if (targetData != null && !targetFlags.disruptionDesyncUsed) {
                int cooldownAdd = Math.min(20, 10 + attackerData.getLevel() * 2);
                targetData.setCooldown("melee", targetData.getRemainingCooldown("melee") + (cooldownAdd * 1000));
                targetData.setCooldown("support", targetData.getRemainingCooldown("support") + (cooldownAdd * 1000));
                targetFlags.disruptionDesyncUsed = true;
                targetFlags.disruptionDesyncCooldown = System.currentTimeMillis() + 30000;
                target.sendMessage("§cDesync! +" + cooldownAdd + "s to abilities!");
            }
        }

        // PRESSURE PASSIVE: Oppression - bonus damage to low HP targets
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            AbilityManager.AbilityFlags victimFlags = plugin.getAbilityManager().getAbilityFlags(victim.getUniqueId());
            if (victimFlags.oppressionDamageBonus > 0) {
                event.setDamage(event.getDamage() * (1.0 + victimFlags.oppressionDamageBonus));
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

    }

    /**
     * Handle player death
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        // Handle boss item drops
        if (data != null) {
            // Dragon Egg
            if (data.getAttribute() == AttributeType.DRAGON_EGG) {
                boolean droppingEgg = event.getDrops().stream()
                        .anyMatch(item -> item.getType() == Material.DRAGON_EGG);
                if (droppingEgg) {
                    removeDragonEggEffects(player);
                    plugin.setPlayerData(player.getUniqueId(), new PlayerData());
                    player.sendMessage("§c§lYou have lost the Dragon Egg!");
                    Bukkit.broadcastMessage("§c§l⚠ §6§lDRAGON EGG DROPPED §c§l⚠");
                    Bukkit.broadcastMessage("§e" + player.getName() + " §7has lost the Dragon Egg!");
                }
            }

            // Wither Bone
            if (data.getAttribute() == AttributeType.WITHER) {
                boolean droppingBone = event.getDrops().stream().anyMatch(this::isWitherBone);
                if (droppingBone) {
                    plugin.setPlayerData(player.getUniqueId(), new PlayerData());
                    player.sendMessage("§c§lYou have lost the Wither Bone!");
                    Bukkit.broadcastMessage("§c§l⚠ §8§lWITHER BONE DROPPED §c§l⚠");
                    Bukkit.broadcastMessage("§e" + player.getName() + " §7has lost the Wither Bone!");
                }
            }

            // Warden Brain
            if (data.getAttribute() == AttributeType.WARDEN) {
                boolean droppingBrain = event.getDrops().stream().anyMatch(this::isWardenBrain);
                if (droppingBrain) {
                    plugin.setPlayerData(player.getUniqueId(), new PlayerData());
                    player.sendMessage("§c§lYou have lost the Warden Brain!");
                    Bukkit.broadcastMessage("§c§l⚠ §3§lWARDEN BRAIN DROPPED §c§l⚠");
                    Bukkit.broadcastMessage("§e" + player.getName() + " §7has lost the Warden Brain!");
                }
            }

            // Breeze Heart
            if (data.getAttribute() == AttributeType.BREEZE) {
                boolean droppingHeart = event.getDrops().stream().anyMatch(this::isBreezeHeart);
                if (droppingHeart) {
                    plugin.setPlayerData(player.getUniqueId(), new PlayerData());
                    player.sendMessage("§c§lYou have lost the Breeze Heart!");
                    Bukkit.broadcastMessage("§c§l⚠ §b§lBREEZE HEART DROPPED §c§l⚠");
                    Bukkit.broadcastMessage("§e" + player.getName() + " §7has lost the Breeze Heart!");
                }
            }
        }

        if (data == null) return;

        // Increment deaths and decrease level (if enabled)
        data.incrementDeaths();
        if (plugin.isLevelLossOnDeath()) {
            data.decrementLevel();
        }

        // HEALTH PASSIVE: Vitality - lose 1 heart per level on death
        if (data.getAttribute() == AttributeType.HEALTH) {
            AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());
            if (flags.vitalityHearts > 0) {
                double heartsLost = data.getLevel() * 2.0; // Lose (level) hearts
                flags.vitalityHearts = Math.max(0, flags.vitalityHearts - heartsLost);
                applyMaxHealthBonus(player);
            }
        }

        // MELEE PASSIVE: Bloodlust - lose all stacks on death
        if (data.getAttribute() == AttributeType.MELEE) {
            AbilityManager.AbilityFlags flags = plugin.getAbilityManager().getAbilityFlags(player.getUniqueId());
            flags.meleeBloodlustStacks = 0.0;
        }

        if (plugin.isLevelLossOnDeath()) {
            player.sendMessage("§cYou died! Level decreased to " + data.getLevel());
        } else {
            player.sendMessage("§cYou died!");
        }

        // Handle killer
        Player killer = player.getKiller();
        if (killer != null) {
            PlayerData killerData = plugin.getPlayerData(killer.getUniqueId());
            if (killerData != null) {
                killerData.incrementKills();
                if (plugin.isLevelGainOnKill()) {
                    killerData.incrementLevel();
                }

                // MELEE PASSIVE: Bloodlust - +1.5% per PvP kill, max 10% +1%/level (L5=15%)
                if (killerData.getAttribute() == AttributeType.MELEE) {
                    AbilityManager.AbilityFlags killerFlags = plugin.getAbilityManager().getAbilityFlags(killer.getUniqueId());
                    double maxStacks = 0.10 + killerData.getLevel() * 0.01; // 11% at L1, 15% at L5
                    killerFlags.meleeBloodlustStacks = Math.min(maxStacks, killerFlags.meleeBloodlustStacks + 0.015);
                    killer.sendMessage("§aBloodlust: +" + String.format("%.1f", killerFlags.meleeBloodlustStacks * 100) + "% damage");
                }

                // HEALTH PASSIVE: Vitality - +1 heart per PvP kill, max = level hearts
                if (killerData.getAttribute() == AttributeType.HEALTH) {
                    AbilityManager.AbilityFlags killerFlags = plugin.getAbilityManager().getAbilityFlags(killer.getUniqueId());
                    double maxHearts = killerData.getLevel() * 2.0; // L1=1 heart, L5=5 hearts (in half-hearts)
                    killerFlags.vitalityHearts = Math.min(maxHearts, killerFlags.vitalityHearts + 2.0);
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
     * Handle Ender Dragon death - spawn exit portal and egg
     */
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        World world = event.getEntity().getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;

        // Generate the exit portal structure after short delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            generateEndExitPortal(world);

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§5§l⚠ §d§lTHE DRAGON EGG HAS APPEARED §5§l⚠");
            Bukkit.broadcastMessage("");
        }, 20L);
    }

    /**
     * Generate the End exit portal structure at 0, 0
     */
    private void generateEndExitPortal(World world) {
        int centerX = 0;
        int centerZ = 0;
        int baseY = 70;

        // Clear area first
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = baseY; y <= baseY + 5; y++) {
                    world.getBlockAt(centerX + x, y, centerZ + z).setType(Material.AIR);
                }
            }
        }

        // Place end portal blocks - center 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.getBlockAt(centerX + x, baseY, centerZ + z).setType(Material.END_PORTAL);
            }
        }

        // North arm - portal (3 wide, 1 deep) at z=-2
        for (int x = -1; x <= 1; x++) {
            world.getBlockAt(centerX + x, baseY, centerZ - 2).setType(Material.END_PORTAL);
        }
        // North arm - bedrock (3 wide, 1 deep) at z=-3
        for (int x = -1; x <= 1; x++) {
            world.getBlockAt(centerX + x, baseY, centerZ - 3).setType(Material.BEDROCK);
        }

        // South arm - portal (3 wide, 1 deep) at z=2
        for (int x = -1; x <= 1; x++) {
            world.getBlockAt(centerX + x, baseY, centerZ + 2).setType(Material.END_PORTAL);
        }
        // South arm - bedrock (3 wide, 1 deep) at z=3
        for (int x = -1; x <= 1; x++) {
            world.getBlockAt(centerX + x, baseY, centerZ + 3).setType(Material.BEDROCK);
        }

        // West arm - portal (1 wide, 3 deep) at x=-2
        for (int z = -1; z <= 1; z++) {
            world.getBlockAt(centerX - 2, baseY, centerZ + z).setType(Material.END_PORTAL);
        }
        // West arm - bedrock (1 wide, 3 deep) at x=-3
        for (int z = -1; z <= 1; z++) {
            world.getBlockAt(centerX - 3, baseY, centerZ + z).setType(Material.BEDROCK);
        }

        // East arm - portal (1 wide, 3 deep) at x=2
        for (int z = -1; z <= 1; z++) {
            world.getBlockAt(centerX + 2, baseY, centerZ + z).setType(Material.END_PORTAL);
        }
        // East arm - bedrock (1 wide, 3 deep) at x=3
        for (int z = -1; z <= 1; z++) {
            world.getBlockAt(centerX + 3, baseY, centerZ + z).setType(Material.BEDROCK);
        }

        // Corner bedrock blocks
        world.getBlockAt(centerX - 2, baseY, centerZ - 2).setType(Material.BEDROCK);
        world.getBlockAt(centerX + 2, baseY, centerZ - 2).setType(Material.BEDROCK);
        world.getBlockAt(centerX - 2, baseY, centerZ + 2).setType(Material.BEDROCK);
        world.getBlockAt(centerX + 2, baseY, centerZ + 2).setType(Material.BEDROCK);

        // Build bedrock pillar in center (y=71 to y=73)
        for (int y = baseY + 1; y <= baseY + 3; y++) {
            world.getBlockAt(centerX, y, centerZ).setType(Material.BEDROCK);
        }

        // Place dragon egg on top (y=74)
        world.getBlockAt(centerX, baseY + 4, centerZ).setType(Material.DRAGON_EGG);
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
     * Prevent enchanting attribute weapons via enchantment table
     */
    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        if (AttributeWeapon.isAttributeWeapon(item)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent enchanting attribute weapons via enchantment table (backup)
     */
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        if (AttributeWeapon.isAttributeWeapon(item)) {
            event.setCancelled(true);
            event.getEnchanter().sendMessage("§cAttribute weapons cannot be enchanted!");
        }
    }

    /**
     * Prevent enchanting/repairing attribute weapons via anvil
     */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        // Cancel if trying to modify an attribute weapon
        if (AttributeWeapon.isAttributeWeapon(firstItem) || AttributeWeapon.isAttributeWeapon(secondItem)) {
            event.setResult(null);
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

        // Always reset max health to default (20.0) when switching attributes
        try {
            org.bukkit.attribute.AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(20.0);
            }
            // Reset health to max (but not above 20)
            if (player.getHealth() > 20.0) {
                player.setHealth(20.0);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not reset health: " + e.getMessage());
        }

        // Remove all attribute-related potion effects
        player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);

        // Remove Dragon Egg effects
        if (oldAttribute == AttributeType.DRAGON_EGG) {
            removeDragonEggEffects(player);
        }

        // Clear ability flags (this resets vitalityHearts and all other bonuses)
        plugin.getAbilityManager().removeAbilityFlags(player.getUniqueId());

        // Clear cooldowns for fresh start
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data != null) {
            data.clearCooldowns();
        }
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

    // ========== BOSS ITEM HELPERS ==========

    /**
     * Check if item is a boss item (cannot be dropped/stored)
     */
    private boolean isBossItem(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.DRAGON_EGG ||
                isWitherBone(item) ||
                isWardenBrain(item) ||
                isBreezeHeart(item);
    }

    /**
     * Check if item is Wither Bone
     */
    private boolean isWitherBone(ItemStack item) {
        if (item == null || item.getType() != Material.COAL_BLOCK) return false;
        if (!item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.contains("Wither Bone");
    }

    /**
     * Check if player has Wither Bone in inventory
     */
    private boolean hasWitherBone(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isWitherBone(item)) return true;
        }
        return false;
    }

    /**
     * Check if item is Warden Brain
     */
    private boolean isWardenBrain(ItemStack item) {
        if (item == null || item.getType() != Material.SCULK_CATALYST) return false;
        if (!item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.contains("Warden Brain");
    }

    /**
     * Check if player has Warden Brain in inventory
     */
    private boolean hasWardenBrain(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isWardenBrain(item)) return true;
        }
        return false;
    }

    /**
     * Check if item is Breeze Heart
     */
    private boolean isBreezeHeart(ItemStack item) {
        if (item == null || item.getType() != Material.WIND_CHARGE) return false;
        if (!item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.contains("Breeze Heart");
    }

    /**
     * Check if player has Breeze Heart in inventory
     */
    private boolean hasBreezeHeart(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isBreezeHeart(item)) return true;
        }
        return false;
    }
}