package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final OddsSMP plugin;
    private final AdminGUI adminGUI;
    private final WeaponGUI weaponGUI;

    public GUIListener(OddsSMP plugin, AdminGUI adminGUI, WeaponGUI weaponGUI) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
        this.weaponGUI = weaponGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if it's one of our GUIs
        if (!title.contains("OddsSMP") && !title.contains("Management") &&
                !title.contains("Select") && !title.contains("Statistics") &&
                !title.contains("Operations") && !title.contains("Settings") &&
                !title.contains("Cooldown") && !title.contains("Details") &&
                !title.contains("Encyclopedia") && !title.contains("Editor") &&
                !title.contains("Edit:") && !title.contains("Weapons") &&
                !title.contains("Custom Items") &&
                !isAttributeDetailsGUI(title)) {
            return;
        }

        // ALWAYS cancel the event for our GUIs - prevent any item movement
        event.setCancelled(true);

        // Don't process clicks on empty slots
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = clicked.getItemMeta().getDisplayName();

        // Main Menu
        if (title.contains("Admin Panel")) {
            handleMainMenu(player, itemName);
        }
        // Attribute Editor
        else if (title.equals("§6§lAttribute Editor")) {
            handleAttributeEditorMenu(player, clicked, itemName, event.getClick());
        }
        // Edit Specific Attribute
        else if (title.startsWith("§6§lEdit: ")) {
            handleAttributeValueEditor(player, title, itemName, event.getClick());
        }
        // Cooldown Adjuster
        else if (title.contains("Cooldown: ")) {
            handleCooldownAdjuster(player, title, itemName, event.getClick());
        }
        // Player Management
        else if (title.equals("§e§lPlayer Management")) {
            handlePlayerManagement(player, clicked);
        }
        // Individual Player Options
        else if (title.contains("Management") && !title.contains("Player Management")) {
            handlePlayerOptions(player, title, itemName, event.getClick());
        }
        // Attribute Selector
        else if (title.contains("Select Attribute")) {
            handleAttributeSelector(player, title, itemName, event.getClick());
        }
        // Tier Selector
        else if (title.contains("Select Tier")) {
            handleTierSelector(player, title, itemName);
        }
        // Cooldown Manager
        else if (title.contains("Cooldown Manager")) {
            handleCooldownManager(player, title, itemName, event.getClick());
        }
        // Server Stats
        else if (title.contains("Statistics")) {
            handleServerStats(player, itemName);
        }
        // Batch Operations
        else if (title.contains("Batch Operations")) {
            handleBatchOperations(player, itemName);
        }
        // Settings
        else if (title.contains("Settings")) {
            handleSettings(player, itemName, event.getClick());
        }
        // Ability Details
        else if (title.contains("Details")) {
            handleAbilityDetails(player, itemName);
        }
        // Attribute Encyclopedia
        else if (title.contains("Encyclopedia")) {
            handleAttributeEncyclopedia(player, clicked, itemName);
        }
        // Detailed Attribute Info (when viewing a specific attribute)
        else if (isAttributeDetailsGUI(title)) {
            handleDetailedAttributeInfo(player, itemName);
        }
        // Weapon Menu
        else if (title.equals(WeaponGUI.WEAPON_MENU_TITLE)) {
            handleWeaponMenu(player, clicked, itemName);
        }
        // Boss Weapon Menu
        else if (title.equals(WeaponGUI.BOSS_WEAPON_MENU_TITLE)) {
            handleBossWeaponMenu(player, clicked, itemName);
        }
        // Custom Items GUI
        else if (title.equals("§6§lCustom Items")) {
            handleCustomItemsMenu(player, clicked, itemName);
        }
    }

    private void handleMainMenu(Player player, String itemName) {
        if (itemName.contains("Player Management")) {
            adminGUI.openPlayerManagement(player);
        } else if (itemName.contains("Attribute Browser")) {
            // Future: Open attribute encyclopedia
            player.sendMessage("§e§lAttribute Browser coming soon!");
        } else if (itemName.contains("Server Statistics")) {
            adminGUI.openServerStats(player);
        } else if (itemName.contains("Batch Operations")) {
            adminGUI.openBatchOperations(player);
        } else if (itemName.contains("Attribute Editor")) {
            adminGUI.openAttributeEditor(player);
        } else if (itemName.contains("Plugin Settings")) {
            adminGUI.openSettings(player);
        }
    }

    private void handleAttributeEditorMenu(Player player, ItemStack clicked, String itemName, ClickType clickType) {
        if (itemName.contains("Back")) {
            adminGUI.openMainMenu(player);
            return;
        }

        // Don't process empty slots
        if (clicked == null || !clicked.hasItemMeta()) return;

        AttributeSettings settings = plugin.getAttributeSettings();

        // Global Cooldown Multiplier
        if (itemName.contains("Global Cooldown")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalCooldownMultiplier(1.0);
                player.sendMessage("§aReset global cooldown multiplier to §e1.0x");
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalCooldownMultiplier(settings.getGlobalCooldownMultiplier() - 0.1);
                player.sendMessage("§aGlobal cooldown multiplier: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()));
            } else if (clickType == ClickType.RIGHT) {
                settings.setGlobalCooldownMultiplier(settings.getGlobalCooldownMultiplier() + 0.1);
                player.sendMessage("§aGlobal cooldown multiplier: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()));
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeEditor(player);
            return;
        }

        // Global Damage Multiplier
        if (itemName.contains("Global Damage")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalDamageMultiplier(1.0);
                player.sendMessage("§aReset global damage multiplier to §e1.0x");
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalDamageMultiplier(settings.getGlobalDamageMultiplier() - 0.1);
                player.sendMessage("§aGlobal damage multiplier: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()));
            } else if (clickType == ClickType.RIGHT) {
                settings.setGlobalDamageMultiplier(settings.getGlobalDamageMultiplier() + 0.1);
                player.sendMessage("§aGlobal damage multiplier: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()));
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeEditor(player);
            return;
        }

        // Level Scaling
        if (itemName.contains("Level Scaling")) {
            if (clickType.isShiftClick()) {
                settings.setLevelScalingPercent(10.0);
                player.sendMessage("§aReset level scaling to §e+10%");
            } else if (clickType == ClickType.LEFT) {
                settings.setLevelScalingPercent(settings.getLevelScalingPercent() - 1.0);
                player.sendMessage("§aLevel scaling: §e+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level");
            } else if (clickType == ClickType.RIGHT) {
                settings.setLevelScalingPercent(settings.getLevelScalingPercent() + 1.0);
                player.sendMessage("§aLevel scaling: §e+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeEditor(player);
            return;
        }

        // Parse attribute from item name
        for (AttributeType attr : AttributeType.values()) {
            if (itemName.contains(attr.getDisplayName())) {
                if (clickType == ClickType.LEFT) {
                    adminGUI.openAttributeValueEditor(player, attr);
                } else if (clickType == ClickType.RIGHT) {
                    settings.applyBalancedPreset(attr);
                    player.sendMessage("§aReset " + attr.getDisplayName() + " to defaults!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    adminGUI.openAttributeEditor(player);
                }
                return;
            }
        }
    }

    private void handleAttributeValueEditor(Player player, String title, String itemName, ClickType clickType) {
        // Extract attribute name from title "§6§lEdit: AttributeName"
        String attrName = title.replace("§6§lEdit: ", "");
        AttributeType attribute = null;
        for (AttributeType attr : AttributeType.values()) {
            if (attr.getDisplayName().equals(attrName)) {
                attribute = attr;
                break;
            }
        }

        if (attribute == null) {
            player.sendMessage("§cError: Could not find attribute!");
            return;
        }

        AttributeSettings settings = plugin.getAttributeSettings();
        AttributeSettings.AttributeConfig config = settings.getConfig(attribute);

        if (itemName.contains("Back")) {
            adminGUI.openAttributeEditor(player);
            return;
        }

        if (itemName.contains("Save Changes")) {
            settings.saveConfig();
            player.sendMessage("§a§lAll changes saved to config!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            return;
        }

        if (itemName.contains("Reset to Defaults")) {
            settings.applyBalancedPreset(attribute);
            player.sendMessage("§c§lReset " + attribute.getDisplayName() + " to defaults!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.8f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Support Cooldown Modifier
        if (itemName.contains("Support Cooldown")) {
            if (clickType == ClickType.LEFT) {
                config.supportCooldownModifier = Math.max(0.1, config.supportCooldownModifier - 0.1);
            } else if (clickType == ClickType.RIGHT) {
                config.supportCooldownModifier = Math.min(3.0, config.supportCooldownModifier + 0.1);
            }
            player.sendMessage("§aSupport cooldown modifier: §e" + String.format("%.1fx", config.supportCooldownModifier));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Support Duration
        if (itemName.contains("Support Duration")) {
            if (clickType == ClickType.LEFT) {
                config.supportDuration = Math.max(1, config.supportDuration - 1);
            } else if (clickType == ClickType.RIGHT) {
                config.supportDuration = Math.min(60, config.supportDuration + 1);
            }
            player.sendMessage("§aSupport duration: §e" + config.supportDuration + "s");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Support Range
        if (itemName.contains("Support Range")) {
            if (clickType == ClickType.LEFT) {
                config.supportRange = Math.max(1.0, config.supportRange - 1.0);
            } else if (clickType == ClickType.RIGHT) {
                config.supportRange = Math.min(50.0, config.supportRange + 1.0);
            }
            player.sendMessage("§aSupport range: §e" + String.format("%.0f", config.supportRange) + " blocks");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Melee Cooldown Modifier
        if (itemName.contains("Melee Cooldown")) {
            if (clickType == ClickType.LEFT) {
                config.meleeCooldownModifier = Math.max(0.1, config.meleeCooldownModifier - 0.1);
            } else if (clickType == ClickType.RIGHT) {
                config.meleeCooldownModifier = Math.min(3.0, config.meleeCooldownModifier + 0.1);
            }
            player.sendMessage("§cMelee cooldown modifier: §e" + String.format("%.1fx", config.meleeCooldownModifier));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Melee Damage Multiplier
        if (itemName.contains("Melee Damage")) {
            if (clickType == ClickType.LEFT) {
                config.meleeDamageMultiplier = Math.max(0.1, config.meleeDamageMultiplier - 0.1);
            } else if (clickType == ClickType.RIGHT) {
                config.meleeDamageMultiplier = Math.min(5.0, config.meleeDamageMultiplier + 0.1);
            }
            player.sendMessage("§cMelee damage multiplier: §e" + String.format("%.1fx", config.meleeDamageMultiplier));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Melee Duration
        if (itemName.contains("Melee Duration")) {
            if (clickType == ClickType.LEFT) {
                config.meleeDuration = Math.max(1, config.meleeDuration - 1);
            } else if (clickType == ClickType.RIGHT) {
                config.meleeDuration = Math.min(30, config.meleeDuration + 1);
            }
            player.sendMessage("§cMelee duration: §e" + config.meleeDuration + "s");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Passive Strength
        if (itemName.contains("Passive Strength")) {
            if (clickType == ClickType.LEFT) {
                config.passiveStrength = Math.max(0.1, config.passiveStrength - 0.1);
            } else if (clickType == ClickType.RIGHT) {
                config.passiveStrength = Math.min(5.0, config.passiveStrength + 0.1);
            }
            player.sendMessage("§bPassive strength: §e" + String.format("%.1fx", config.passiveStrength));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Passive Tick Rate
        if (itemName.contains("Passive Tick Rate")) {
            if (clickType == ClickType.LEFT) {
                config.passiveTickRate = Math.max(0.25, config.passiveTickRate - 0.25);
            } else if (clickType == ClickType.RIGHT) {
                config.passiveTickRate = Math.min(5.0, config.passiveTickRate + 0.25);
            }
            player.sendMessage("§bPassive tick rate: §e" + String.format("%.2fs", config.passiveTickRate));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Stable Tier
        if (itemName.contains("Stable Tier")) {
            if (clickType.isShiftClick()) {
                double newMult = Math.max(0.5, settings.getStableMultiplier() - 0.1);
                settings.setTierMultiplier(Tier.STABLE, newMult);
                player.sendMessage("§aStable tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.LEFT) {
                int newCd = Math.max(10, settings.getStableCooldown() - 10);
                settings.setTierCooldown(Tier.STABLE, newCd);
                player.sendMessage("§aStable tier cooldown: §e" + newCd + "s");
            } else if (clickType == ClickType.RIGHT) {
                int newCd = Math.min(300, settings.getStableCooldown() + 10);
                settings.setTierCooldown(Tier.STABLE, newCd);
                player.sendMessage("§aStable tier cooldown: §e" + newCd + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Warped Tier
        if (itemName.contains("Warped Tier")) {
            if (clickType.isShiftClick()) {
                double newMult = Math.max(0.5, settings.getWarpedMultiplier() - 0.1);
                settings.setTierMultiplier(Tier.WARPED, newMult);
                player.sendMessage("§dWarped tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.LEFT) {
                int newCd = Math.max(10, settings.getWarpedCooldown() - 10);
                settings.setTierCooldown(Tier.WARPED, newCd);
                player.sendMessage("§dWarped tier cooldown: §e" + newCd + "s");
            } else if (clickType == ClickType.RIGHT) {
                int newCd = Math.min(300, settings.getWarpedCooldown() + 10);
                settings.setTierCooldown(Tier.WARPED, newCd);
                player.sendMessage("§dWarped tier cooldown: §e" + newCd + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Extreme Tier
        if (itemName.contains("Extreme Tier")) {
            if (clickType.isShiftClick()) {
                double newMult = Math.max(0.5, settings.getExtremeMultiplier() - 0.1);
                settings.setTierMultiplier(Tier.EXTREME, newMult);
                player.sendMessage("§cExtreme tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.LEFT) {
                int newCd = Math.max(10, settings.getExtremeCooldown() - 10);
                settings.setTierCooldown(Tier.EXTREME, newCd);
                player.sendMessage("§cExtreme tier cooldown: §e" + newCd + "s");
            } else if (clickType == ClickType.RIGHT) {
                int newCd = Math.min(300, settings.getExtremeCooldown() + 10);
                settings.setTierCooldown(Tier.EXTREME, newCd);
                player.sendMessage("§cExtreme tier cooldown: §e" + newCd + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        // Preset handlers
        if (itemName.contains("Preset: Balanced")) {
            if (clickType.isShiftClick()) {
                settings.applyPresetToAll("balanced");
                player.sendMessage("§a§lApplied Balanced preset to ALL attributes!");
            } else {
                settings.applyBalancedPreset(attribute);
                player.sendMessage("§a§lApplied Balanced preset to " + attribute.getDisplayName() + "!");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        if (itemName.contains("Preset: High Power")) {
            if (clickType.isShiftClick()) {
                settings.applyPresetToAll("highpower");
                player.sendMessage("§c§lApplied High Power preset to ALL attributes!");
            } else {
                settings.applyHighPowerPreset(attribute);
                player.sendMessage("§c§lApplied High Power preset to " + attribute.getDisplayName() + "!");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.8f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        if (itemName.contains("Preset: Low Power")) {
            if (clickType.isShiftClick()) {
                settings.applyPresetToAll("lowpower");
                player.sendMessage("§7§lApplied Low Power preset to ALL attributes!");
            } else {
                settings.applyLowPowerPreset(attribute);
                player.sendMessage("§7§lApplied Low Power preset to " + attribute.getDisplayName() + "!");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }

        if (itemName.contains("Preset: Chaos")) {
            if (clickType.isShiftClick()) {
                settings.applyPresetToAll("chaos");
                player.sendMessage("§d§lApplied Chaos preset to ALL attributes! Good luck!");
            } else {
                settings.applyChaosPreset(attribute);
                player.sendMessage("§d§lApplied Chaos preset to " + attribute.getDisplayName() + "! Good luck!");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.5f);
            adminGUI.openAttributeValueEditor(player, attribute);
            return;
        }
    }

    private void handleCooldownAdjuster(Player player, String title, String itemName, ClickType clickType) {
        if (itemName.contains("Back")) {
            // Extract attribute from title and return to its editor
            player.closeInventory();
            return;
        }

        if (itemName.contains("Apply to All")) {
            player.sendMessage("§b§lApplied cooldowns to all attributes!");
            return;
        }

        // Tier-specific cooldown adjustments
        player.sendMessage("§e§lCooldown adjustment coming soon!");
        player.sendMessage("§7You clicked: " + itemName);
    }

    private void handlePlayerManagement(Player player, ItemStack clicked) {
        String itemName = clicked.getItemMeta().getDisplayName();

        if (itemName.contains("Back")) {
            adminGUI.openMainMenu(player);
            return;
        }

        // Extract player name (remove color codes)
        String targetName = itemName.replace("§6", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            adminGUI.openPlayerOptions(player, target);
        }
    }

    private void handlePlayerOptions(Player player, String title, String itemName, ClickType clickType) {
        String targetName = title.replace("§e§l", "").replace(" Management", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            player.closeInventory();
            return;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());

        if (itemName.contains("Back")) {
            adminGUI.openPlayerManagement(player);
        } else if (itemName.contains("Assign Random")) {
            assignRandomAttribute(player, target);
        } else if (itemName.contains("Reroll")) {
            rerollAttribute(player, target);
        } else if (itemName.contains("Upgrade")) {
            upgradeLevel(player, target);
        } else if (itemName.contains("Downgrade")) {
            downgradeLevel(player, target);
        } else if (itemName.contains("Choose Specific")) {
            adminGUI.openAttributeSelector(player, target);
        } else if (itemName.contains("Change Tier")) {
            adminGUI.openTierSelector(player, target);
        } else if (itemName.contains("Reset Player")) {
            resetPlayer(player, target);
        } else if (itemName.contains("Remove Attribute")) {
            removeAttribute(player, target);
        } else if (itemName.contains("Manage Cooldowns")) {
            adminGUI.openCooldownManager(player, target);
        } else if (itemName.contains("Grant Dragon Egg")) {
            grantDragonEgg(player, target);
        } else if (itemName.contains("View Ability")) {
            adminGUI.openAbilityDetails(player, target);
        } else if (itemName.contains("Test Abilities")) {
            testAbilities(player, target);
        }
    }

    private void handleAttributeSelector(Player player, String title, String itemName, ClickType clickType) {
        String targetName = title.replace("§d§lSelect Attribute for ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            player.closeInventory();
            return;
        }

        if (itemName.contains("Back")) {
            adminGUI.openPlayerOptions(player, target);
            return;
        }

        // Parse attribute from item name
        for (AttributeType attr : AttributeType.values()) {
            if (itemName.contains(attr.getDisplayName())) {
                Tier tier;

                // Determine tier based on click type
                if (clickType == ClickType.SHIFT_LEFT) {
                    tier = Tier.STABLE;
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    tier = Tier.WARPED;
                } else if (clickType == ClickType.RIGHT) {
                    tier = Tier.EXTREME;
                } else {
                    tier = Tier.getRandomTier();
                }

                assignAttribute(player, target, attr, tier);
                adminGUI.openPlayerOptions(player, target);
                break;
            }
        }
    }

    private void handleTierSelector(Player player, String title, String itemName) {
        String targetName = title.replace("§6§lSelect Tier for ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            player.closeInventory();
            return;
        }

        if (itemName.contains("Back")) {
            adminGUI.openPlayerOptions(player, target);
            return;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            player.sendMessage("§cPlayer must have an attribute first!");
            return;
        }

        Tier newTier = null;
        if (itemName.contains("STABLE")) newTier = Tier.STABLE;
        else if (itemName.contains("WARPED")) newTier = Tier.WARPED;
        else if (itemName.contains("EXTREME")) newTier = Tier.EXTREME;

        if (newTier != null) {
            data.setTier(newTier);
            ParticleManager.playSupportParticles(target, data.getAttribute(), newTier, data.getLevel());
            plugin.updatePlayerTab(target);

            player.sendMessage("§aChanged " + target.getName() + "'s tier to " + newTier.getColor() + newTier.name());
            target.sendMessage("§eYour tier was changed to " + newTier.getColor() + newTier.name());

            adminGUI.openPlayerOptions(player, target);
        }
    }

    private void handleCooldownManager(Player player, String title, String itemName, ClickType clickType) {
        String targetName = title.replace("§e§lCooldown Manager: ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            player.closeInventory();
            return;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null) return;

        if (itemName.contains("Back")) {
            adminGUI.openPlayerOptions(player, target);
            return;
        }

        if (itemName.contains("Support Cooldown")) {
            if (clickType == ClickType.LEFT) {
                data.getCooldowns().remove("support");
                player.sendMessage("§aCleared support cooldown for " + target.getName());
            } else if (clickType == ClickType.RIGHT) {
                data.setCooldown("support", 30000);
                player.sendMessage("§aSet support cooldown to 30s for " + target.getName());
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                data.setCooldown("support", 60000);
                player.sendMessage("§aSet support cooldown to 60s for " + target.getName());
            }
            adminGUI.openCooldownManager(player, target);
        } else if (itemName.contains("Melee Cooldown")) {
            if (clickType == ClickType.LEFT) {
                data.getCooldowns().remove("melee");
                player.sendMessage("§aCleared melee cooldown for " + target.getName());
            } else if (clickType == ClickType.RIGHT) {
                data.setCooldown("melee", 30000);
                player.sendMessage("§aSet melee cooldown to 30s for " + target.getName());
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                data.setCooldown("melee", 60000);
                player.sendMessage("§aSet melee cooldown to 60s for " + target.getName());
            }
            adminGUI.openCooldownManager(player, target);
        } else if (itemName.contains("Clear All")) {
            data.clearCooldowns();
            player.sendMessage("§aCleared all cooldowns for " + target.getName());
            adminGUI.openCooldownManager(player, target);
        }
    }

    private void handleServerStats(Player player, String itemName) {
        if (itemName.contains("Back")) {
            adminGUI.openMainMenu(player);
        }
    }

    private void handleBatchOperations(Player player, String itemName) {
        if (itemName.contains("Back")) {
            adminGUI.openMainMenu(player);
            return;
        }

        if (itemName.contains("Assign All Players")) {
            int count = 0;
            int delay = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerData(p.getUniqueId());
                if (data == null || data.getAttribute() == null) {
                    // Get old attribute for cleanup (shouldn't have one, but just in case)
                    PlayerData oldData = plugin.getPlayerData(p.getUniqueId());
                    AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;
                    if (oldAttribute != null) {
                        plugin.getEventListener().removeAttributeEffects(p, oldAttribute);
                    }

                    // Stagger the animations slightly so they don't all happen at once
                    final int currentDelay = delay;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getEventListener().playSlotAnimationAndAssign(p);
                    }, currentDelay);
                    delay += 5; // 0.25 second stagger between players
                    count++;
                }
            }
            player.sendMessage("§aStarted attribute roll for " + count + " players!");
        } else if (itemName.contains("Reroll All")) {
            int delay = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                // Get old attribute for cleanup
                PlayerData oldData = plugin.getPlayerData(p.getUniqueId());
                AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

                // Remove old attribute effects
                if (oldAttribute != null) {
                    plugin.getEventListener().removeAttributeEffects(p, oldAttribute);
                }

                // Stagger the animations slightly so they don't all happen at once
                final int currentDelay = delay;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getEventListener().playSlotAnimationAndAssign(p);
                }, currentDelay);
                delay += 5; // 0.25 second stagger between players
            }
            player.sendMessage("§eStarted attribute reroll for all players!");
        } else if (itemName.contains("Reset All")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerData(p.getUniqueId());
                if (data != null) {
                    data.setLevel(1);
                    data.clearCooldowns();
                    plugin.getAbilityManager().removeAbilityFlags(p.getUniqueId());
                    plugin.updatePlayerTab(p);
                    p.sendMessage("§eYou were reset to level 1!");
                }
            }
            player.sendMessage("§aReset all players to level 1!");
        } else if (itemName.contains("Clear All Cooldowns")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerData(p.getUniqueId());
                if (data != null) {
                    data.clearCooldowns();
                }
            }
            player.sendMessage("§aCleared all cooldowns!");
        } else if (itemName.contains("EVERYONE DRAGON EGG")) {
            // Broadcast major announcement
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§4§l§m                                                    ");
            Bukkit.broadcastMessage("§c§l⚠⚠⚠ §6§lCHAOS MODE ACTIVATED §c§l⚠⚠⚠");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("  §c§lEVERYONE HAS RECEIVED THE DRAGON EGG!");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("  §7The world trembles as ancient power");
            Bukkit.broadcastMessage("  §7flows through all players...");
            Bukkit.broadcastMessage("§4§l§m                                                    ");
            Bukkit.broadcastMessage("");

            for (Player p : Bukkit.getOnlinePlayers()) {
                // Get old attribute for cleanup
                PlayerData oldData = plugin.getPlayerData(p.getUniqueId());
                AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

                // Remove old attribute effects
                if (oldAttribute != null) {
                    plugin.getEventListener().removeAttributeEffects(p, oldAttribute);
                }

                plugin.setPlayerData(p.getUniqueId(), new PlayerData(AttributeType.DRAGON_EGG, Tier.EXTREME));
                ParticleManager.playSupportParticles(p, AttributeType.DRAGON_EGG, Tier.EXTREME, 1);
                plugin.updatePlayerTab(p);
                plugin.getEventListener().applyDragonEggEffects(p);
                p.sendMessage("§6§l§kA§r §c§lYOU RECEIVED THE DRAGON EGG §6§l§kA");
            }
            player.sendMessage("§6§lEveryone received Dragon Egg!");
        } else if (itemName.contains("Remove All")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                // Get old attribute for cleanup
                PlayerData oldData = plugin.getPlayerData(p.getUniqueId());
                AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

                // Remove old attribute effects
                if (oldAttribute != null) {
                    plugin.getEventListener().removeAttributeEffects(p, oldAttribute);
                }

                plugin.setPlayerData(p.getUniqueId(), new PlayerData());
                plugin.updatePlayerTab(p);
                p.sendMessage("§cYour attribute was removed!");
            }
            player.sendMessage("§cRemoved all attributes!");
        }
    }

    private void handleSettings(Player player, String itemName, ClickType clickType) {
        AttributeSettings settings = plugin.getAttributeSettings();

        if (itemName.contains("Back")) {
            adminGUI.openMainMenu(player);
            return;
        }

        // Gameplay toggles
        if (itemName.contains("Particle Effects")) {
            plugin.setParticleEffectsEnabled(!plugin.isParticleEffectsEnabled());
            player.sendMessage("§aParticle effects " + (plugin.isParticleEffectsEnabled() ? "§aenabled" : "§cdisabled"));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Level Loss on Death")) {
            plugin.setLevelLossOnDeath(!plugin.isLevelLossOnDeath());
            player.sendMessage("§aLevel loss on death " + (plugin.isLevelLossOnDeath() ? "§aenabled" : "§cdisabled"));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Level Gain on Kill")) {
            plugin.setLevelGainOnKill(!plugin.isLevelGainOnKill());
            player.sendMessage("§aLevel gain on kill " + (plugin.isLevelGainOnKill() ? "§aenabled" : "§cdisabled"));
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Auto-Assign on Join")) {
            if (clickType == ClickType.LEFT) {
                plugin.setAutoAssignEnabled(!plugin.isAutoAssignEnabled());
                player.sendMessage("§aAuto-assign " + (plugin.isAutoAssignEnabled() ? "§aenabled" : "§cdisabled"));
            } else if (clickType == ClickType.RIGHT) {
                plugin.setAutoAssignDelaySeconds(plugin.getAutoAssignDelaySeconds() + 5);
                player.sendMessage("§aAuto-assign delay: §e" + plugin.getAutoAssignDelaySeconds() + "s");
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                plugin.setAutoAssignDelaySeconds(Math.max(0, plugin.getAutoAssignDelaySeconds() - 5));
                player.sendMessage("§aAuto-assign delay: §e" + plugin.getAutoAssignDelaySeconds() + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        // Global multipliers
        if (itemName.contains("Global Cooldown Multiplier")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalCooldownMultiplier(1.0);
                player.sendMessage("§aReset global cooldown multiplier to §e1.0x");
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalCooldownMultiplier(Math.max(0.1, settings.getGlobalCooldownMultiplier() - 0.1));
                player.sendMessage("§aGlobal cooldown multiplier: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()));
            } else if (clickType == ClickType.RIGHT) {
                settings.setGlobalCooldownMultiplier(Math.min(5.0, settings.getGlobalCooldownMultiplier() + 0.1));
                player.sendMessage("§aGlobal cooldown multiplier: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()));
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Global Damage Multiplier")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalDamageMultiplier(1.0);
                player.sendMessage("§aReset global damage multiplier to §e1.0x");
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalDamageMultiplier(Math.max(0.1, settings.getGlobalDamageMultiplier() - 0.1));
                player.sendMessage("§aGlobal damage multiplier: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()));
            } else if (clickType == ClickType.RIGHT) {
                settings.setGlobalDamageMultiplier(Math.min(5.0, settings.getGlobalDamageMultiplier() + 0.1));
                player.sendMessage("§aGlobal damage multiplier: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()));
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Level Scaling")) {
            if (clickType.isShiftClick()) {
                settings.setLevelScalingPercent(10.0);
                player.sendMessage("§aReset level scaling to §e+10%");
            } else if (clickType == ClickType.LEFT) {
                settings.setLevelScalingPercent(Math.max(0, settings.getLevelScalingPercent() - 1.0));
                player.sendMessage("§aLevel scaling: §e+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level");
            } else if (clickType == ClickType.RIGHT) {
                settings.setLevelScalingPercent(Math.min(50, settings.getLevelScalingPercent() + 1.0));
                player.sendMessage("§aLevel scaling: §e+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        // Tier settings
        if (itemName.contains("Stable Tier")) {
            if (clickType == ClickType.SHIFT_LEFT) {
                double newMult = Math.max(0.5, settings.getStableMultiplier() - 0.1);
                settings.setTierMultiplier(Tier.STABLE, newMult);
                player.sendMessage("§aStable tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                double newMult = Math.min(3.0, settings.getStableMultiplier() + 0.1);
                settings.setTierMultiplier(Tier.STABLE, newMult);
                player.sendMessage("§aStable tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.LEFT) {
                int newCd = Math.max(10, settings.getStableCooldown() - 10);
                settings.setTierCooldown(Tier.STABLE, newCd);
                player.sendMessage("§aStable tier cooldown: §e" + newCd + "s");
            } else if (clickType == ClickType.RIGHT) {
                int newCd = Math.min(300, settings.getStableCooldown() + 10);
                settings.setTierCooldown(Tier.STABLE, newCd);
                player.sendMessage("§aStable tier cooldown: §e" + newCd + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Warped Tier")) {
            if (clickType == ClickType.SHIFT_LEFT) {
                double newMult = Math.max(0.5, settings.getWarpedMultiplier() - 0.1);
                settings.setTierMultiplier(Tier.WARPED, newMult);
                player.sendMessage("§dWarped tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                double newMult = Math.min(3.0, settings.getWarpedMultiplier() + 0.1);
                settings.setTierMultiplier(Tier.WARPED, newMult);
                player.sendMessage("§dWarped tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.LEFT) {
                int newCd = Math.max(10, settings.getWarpedCooldown() - 10);
                settings.setTierCooldown(Tier.WARPED, newCd);
                player.sendMessage("§dWarped tier cooldown: §e" + newCd + "s");
            } else if (clickType == ClickType.RIGHT) {
                int newCd = Math.min(300, settings.getWarpedCooldown() + 10);
                settings.setTierCooldown(Tier.WARPED, newCd);
                player.sendMessage("§dWarped tier cooldown: §e" + newCd + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Extreme Tier")) {
            if (clickType == ClickType.SHIFT_LEFT) {
                double newMult = Math.max(0.5, settings.getExtremeMultiplier() - 0.1);
                settings.setTierMultiplier(Tier.EXTREME, newMult);
                player.sendMessage("§cExtreme tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                double newMult = Math.min(3.0, settings.getExtremeMultiplier() + 0.1);
                settings.setTierMultiplier(Tier.EXTREME, newMult);
                player.sendMessage("§cExtreme tier effect: §e" + (int)(newMult * 100) + "%");
            } else if (clickType == ClickType.LEFT) {
                int newCd = Math.max(10, settings.getExtremeCooldown() - 10);
                settings.setTierCooldown(Tier.EXTREME, newCd);
                player.sendMessage("§cExtreme tier cooldown: §e" + newCd + "s");
            } else if (clickType == ClickType.RIGHT) {
                int newCd = Math.min(300, settings.getExtremeCooldown() + 10);
                settings.setTierCooldown(Tier.EXTREME, newCd);
                player.sendMessage("§cExtreme tier cooldown: §e" + newCd + "s");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        // Presets
        if (itemName.contains("Balanced Preset")) {
            settings.applyPresetToAll("balanced");
            player.sendMessage("§a§lApplied Balanced preset to all attributes!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("High Power Preset")) {
            settings.applyPresetToAll("highpower");
            player.sendMessage("§c§lApplied High Power preset to all attributes!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.8f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Low Power Preset")) {
            settings.applyPresetToAll("lowpower");
            player.sendMessage("§7§lApplied Low Power preset to all attributes!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
            adminGUI.openSettings(player);
            return;
        }

        if (itemName.contains("Chaos Preset")) {
            settings.applyPresetToAll("chaos");
            player.sendMessage("§d§lApplied Chaos preset to all attributes! Good luck!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.5f);
            adminGUI.openSettings(player);
            return;
        }

        // Actions
        if (itemName.contains("Save All Settings")) {
            settings.saveConfig();
            player.sendMessage("§a§lAll settings saved to config!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            return;
        }

        if (itemName.contains("Reset to Defaults")) {
            settings.resetToDefaults();
            plugin.setLevelLossOnDeath(true);
            plugin.setLevelGainOnKill(true);
            plugin.setParticleEffectsEnabled(true);
            plugin.setAutoAssignEnabled(false);
            plugin.setAutoAssignDelaySeconds(10);
            player.sendMessage("§c§lAll settings reset to defaults!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            adminGUI.openSettings(player);
            return;
        }
    }

    private void handleAbilityDetails(Player player, String itemName) {
        if (itemName.contains("Back")) {
            player.closeInventory();
        }
    }

    private void handleAttributeEncyclopedia(Player player, ItemStack clicked, String itemName) {
        // Check for back button or info items (tier info, level info)
        if (itemName.contains("Stable") || itemName.contains("Warped") ||
                itemName.contains("Extreme") || itemName.contains("Level System")) {
            return; // Just informational, do nothing
        }

        // Parse attribute from item name
        for (AttributeType attr : AttributeType.values()) {
            if (itemName.contains(attr.getDisplayName())) {
                adminGUI.openDetailedAttributeInfo(player, attr);
                break;
            }
        }
    }

    private void handleDetailedAttributeInfo(Player player, String itemName) {
        if (itemName.contains("Back")) {
            adminGUI.openAttributeInfo(player);
        }
        // Clicking on ability info items does nothing (just display)
    }

    /**
     * Check if a title is an attribute details GUI
     */
    private boolean isAttributeDetailsGUI(String title) {
        for (AttributeType attr : AttributeType.values()) {
            if (title.contains(attr.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    // Helper methods

    private void assignRandomAttribute(Player admin, Player target) {
        // Get old attribute for cleanup
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Use slot animation
        plugin.getEventListener().playSlotAnimationAndAssign(target);
        admin.sendMessage("§aStarted attribute roll for " + target.getName() + "!");
    }

    private void assignAttribute(Player admin, Player target, AttributeType attr, Tier tier) {
        // Get old attribute for cleanup
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Assign new attribute
        PlayerData data = new PlayerData(attr, tier);
        plugin.setPlayerData(target.getUniqueId(), data);
        ParticleManager.playSupportParticles(target, attr, tier, 1);
        plugin.updatePlayerTab(target);

        // Apply Dragon Egg effects if needed
        if (attr == AttributeType.DRAGON_EGG) {
            plugin.getEventListener().applyDragonEggEffects(target);
        }

        admin.sendMessage("§aAssigned " + tier.getColor() + tier.name() + " " + attr.getDisplayName() + " §ato " + target.getName());
        target.sendMessage("§aYou received " + tier.getColor() + tier.name() + " " + attr.getIcon() + " " + attr.getDisplayName() + "§a!");
    }

    private void rerollAttribute(Player admin, Player target) {
        // Get old attribute for cleanup
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Use slot animation for reroll
        plugin.getEventListener().playSlotAnimationAndAssign(target);
        admin.sendMessage("§eStarted attribute reroll for " + target.getName() + "!");
    }

    private void upgradeLevel(Player admin, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer doesn't have an attribute!");
            return;
        }

        int oldLevel = data.getLevel();
        data.incrementLevel();
        ParticleManager.playSupportParticles(target, data.getAttribute(), data.getTier(), data.getLevel());
        plugin.updatePlayerTab(target);

        admin.sendMessage("§aUpgraded " + target.getName() + " from level " + oldLevel + " to " + data.getLevel());
        target.sendMessage("§aYou were upgraded to level " + data.getLevel() + "!");

        adminGUI.openPlayerOptions(admin, target);
    }

    private void downgradeLevel(Player admin, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer doesn't have an attribute!");
            return;
        }

        int oldLevel = data.getLevel();
        data.decrementLevel();
        plugin.updatePlayerTab(target);

        admin.sendMessage("§cDowngraded " + target.getName() + " from level " + oldLevel + " to " + data.getLevel());
        target.sendMessage("§cYou were downgraded to level " + data.getLevel());

        adminGUI.openPlayerOptions(admin, target);
    }

    private void resetPlayer(Player admin, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer doesn't have an attribute!");
            return;
        }

        data.setLevel(1);
        data.clearCooldowns();
        plugin.getAbilityManager().removeAbilityFlags(target.getUniqueId());
        target.getWorld().spawnParticle(org.bukkit.Particle.GLOW, target.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.05);
        plugin.updatePlayerTab(target);

        admin.sendMessage("§aReset " + target.getName() + " to level 1");
        target.sendMessage("§eYou were reset to level 1!");

        adminGUI.openPlayerOptions(admin, target);
    }

    private void removeAttribute(Player admin, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer doesn't have an attribute!");
            return;
        }

        AttributeType removedAttr = data.getAttribute();

        // Remove attribute effects
        plugin.getEventListener().removeAttributeEffects(target, removedAttr);

        // Remove attribute data
        plugin.setPlayerData(target.getUniqueId(), new PlayerData());
        target.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, target.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.02);
        plugin.updatePlayerTab(target);

        admin.sendMessage("§cRemoved " + removedAttr.getDisplayName() + " from " + target.getName());
        target.sendMessage("§cYour " + removedAttr.getDisplayName() + " was removed!");

        adminGUI.openPlayerOptions(admin, target);
    }

    private void grantDragonEgg(Player admin, Player target) {
        // Get old attribute for cleanup
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Grant Dragon Egg
        PlayerData data = new PlayerData(AttributeType.DRAGON_EGG, Tier.EXTREME);
        plugin.setPlayerData(target.getUniqueId(), data);
        ParticleManager.playSupportParticles(target, AttributeType.DRAGON_EGG, Tier.EXTREME, 1);
        plugin.updatePlayerTab(target);

        // Broadcast announcement
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l§m                                                    ");
        Bukkit.broadcastMessage("§c§l⚠ §6§lDRAGON EGG OBTAINED §c§l⚠");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §e" + target.getName() + " §7has been granted the");
        Bukkit.broadcastMessage("  §6§lLEGENDARY DRAGON EGG!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7An immense power now flows through them...");
        Bukkit.broadcastMessage("§6§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Apply Dragon Egg effects
        plugin.getEventListener().applyDragonEggEffects(target);

        admin.sendMessage("§6§lGranted Dragon Egg to " + target.getName() + "!");
        target.sendMessage("§6§l§kA§r §c§lYOU RECEIVED THE DRAGON EGG §6§l§kA");

        adminGUI.openPlayerOptions(admin, target);
    }

    private void testAbilities(Player admin, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer doesn't have an attribute!");
            return;
        }

        // Test all three particle types
        ParticleManager.playSupportParticles(target, data.getAttribute(), data.getTier(), data.getLevel());
        ParticleManager.playPassiveParticles(target, data.getAttribute(), data.getTier());

        // Find nearby entity for melee test
        org.bukkit.entity.Entity nearestEntity = target.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof org.bukkit.entity.LivingEntity)
                .findFirst()
                .orElse(null);

        if (nearestEntity != null) {
            ParticleManager.playMeleeParticles(target, nearestEntity, data.getAttribute(), data.getTier());
        }

        admin.sendMessage("§aTested abilities for " + target.getName());
    }

    // Weapon GUI Handlers

    private void handleWeaponMenu(Player player, ItemStack clicked, String itemName) {
        // Boss weapons button
        if (itemName.contains("Boss Weapons")) {
            weaponGUI.openBossWeaponsMenu(player);
            return;
        }

        // Give all weapons button
        if (itemName.contains("Give All Weapons")) {
            weaponGUI.giveAllWeapons(player);
            return;
        }

        // Check if clicking on a weapon
        for (AttributeWeapon weapon : AttributeWeapon.values()) {
            // Skip boss weapons (they're in the boss menu)
            if (weapon.getRequiredAttribute().isBossAttribute() ||
                weapon.getRequiredAttribute().isDragonEgg()) {
                continue;
            }

            if (itemName.contains(weapon.getName())) {
                weaponGUI.giveWeapon(player, weapon);
                return;
            }
        }
    }

    private void handleBossWeaponMenu(Player player, ItemStack clicked, String itemName) {
        // Back button
        if (itemName.contains("Back")) {
            weaponGUI.openMainMenu(player);
            return;
        }

        // Check if clicking on a boss weapon
        for (AttributeWeapon weapon : AttributeWeapon.values()) {
            // Only process boss weapons
            if (!weapon.getRequiredAttribute().isBossAttribute() &&
                !weapon.getRequiredAttribute().isDragonEgg()) {
                continue;
            }

            if (itemName.contains(weapon.getName())) {
                weaponGUI.giveWeapon(player, weapon);
                return;
            }
        }
    }

    private void handleCustomItemsMenu(Player player, ItemStack clicked, String itemName) {
        // Close button
        if (itemName.contains("Close")) {
            player.closeInventory();
            return;
        }

        // Labels don't give items
        if (itemName.contains("Special Items") || itemName.contains("Attribute Weapons")) {
            return;
        }

        // Give the clicked item (create a clean copy without the click instruction)
        ItemStack itemToGive = null;

        // Special items
        if (itemName.contains("Weapon Handle")) {
            itemToGive = WeaponAltar.createWeaponHandle();
        } else if (itemName.contains("Warden's Heart")) {
            itemToGive = WeaponAltar.createWardensHeart();
        } else if (itemName.contains("Wither Bone")) {
            itemToGive = WeaponAltar.createWitherBone();
        } else if (itemName.contains("Breeze Heart")) {
            itemToGive = WeaponAltar.createBreezeHeart();
        } else if (itemName.contains("Dragon Heart")) {
            itemToGive = WeaponAltar.createDragonHeart();
        } else if (itemName.contains("Attribute Upgrader")) {
            itemToGive = OddsSMP.createUpgrader();
        } else if (itemName.contains("Attribute Reroller")) {
            itemToGive = OddsSMP.createReroller();
        } else {
            // Check for attribute weapons
            for (AttributeWeapon weapon : AttributeWeapon.values()) {
                if (itemName.contains(weapon.getName())) {
                    itemToGive = weapon.createItem();
                    break;
                }
            }
        }

        if (itemToGive != null) {
            player.getInventory().addItem(itemToGive);
            player.sendMessage("§aYou received " + itemToGive.getItemMeta().getDisplayName() + "§a!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        }
    }
}