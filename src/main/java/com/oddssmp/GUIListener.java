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
                !title.contains("Custom Items") && !title.contains("Combat Log") &&
                !title.contains("Particle") && !title.contains("Track") &&
                !title.contains("Altar") &&
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
        // Vision Player Tracking GUI
        else if (title.equals("§3§lSelect Player to Track")) {
            handleTrackingSelection(player, clicked);
        }
        // Altar Settings GUI
        else if (title.equals("§3§lAltar Settings")) {
            handleAltarSettings(player, clicked, itemName, event.getClick(), event.getSlot());
        }
        // All Altars list GUI
        else if (title.equals("§3§lAll Altars")) {
            handleAltarList(player, clicked, itemName, event.getClick(), event.getSlot());
        }
    }

    private void handleTrackingSelection(Player player, ItemStack clicked) {
        if (clicked.getType() != org.bukkit.Material.PLAYER_HEAD) return;

        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) clicked.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;

        Player target = meta.getOwningPlayer().getPlayer();
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cThat player is no longer online!");
            player.closeInventory();
            return;
        }

        player.closeInventory();
        plugin.getAbilityManager().startTracking(player, target);
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
                assignAttribute(player, target, attr);
                adminGUI.openPlayerOptions(player, target);
                break;
            }
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

                plugin.setPlayerData(p.getUniqueId(), new PlayerData(AttributeType.DRAGON_EGG));
                ParticleManager.playSupportParticles(p, AttributeType.DRAGON_EGG, 1);
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
        String title = player.getOpenInventory().getTitle();

        // Back button - return to appropriate menu
        if (itemName.contains("Back")) {
            if (title.equals("§b§lPlugin Settings")) {
                adminGUI.openMainMenu(player);
            } else {
                adminGUI.openSettings(player);
            }
            return;
        }

        // Main settings menu - open sub-menus
        if (title.equals("§b§lPlugin Settings")) {
            if (itemName.contains("Gameplay Settings")) {
                adminGUI.openGameplaySettings(player);
            } else if (itemName.contains("Combat Settings") && !itemName.contains("Combat Log")) {
                adminGUI.openCombatSettings(player);
            } else if (itemName.contains("Boss Settings")) {
                adminGUI.openBossSettings(player);
            } else if (itemName.contains("Broadcast Settings")) {
                adminGUI.openBroadcastSettings(player);
            } else if (itemName.contains("Multiplier Settings")) {
                adminGUI.openMultiplierSettings(player);
            } else if (itemName.contains("Quick Presets")) {
                adminGUI.openPresetSettings(player);
            } else if (itemName.contains("Death Settings")) {
                adminGUI.openDeathSettings(player);
            } else if (itemName.contains("Particle Settings")) {
                adminGUI.openParticleSettings(player);
            } else if (itemName.contains("Combat Log Settings")) {
                adminGUI.openCombatLogSettings(player);
            } else if (itemName.contains("Save All Settings")) {
                settings.saveConfig();
                player.sendMessage("§a§lAll settings saved to config!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            } else if (itemName.contains("Reset to Defaults")) {
                resetAllSettings(settings);
                player.sendMessage("§c§lAll settings reset to defaults!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                adminGUI.openSettings(player);
            }
            return;
        }

        // Gameplay Settings sub-menu
        if (title.equals("§6§lGameplay Settings")) {
            handleGameplaySettings(player, itemName, clickType);
            return;
        }

        // Combat Settings sub-menu
        if (title.equals("§c§lCombat Settings")) {
            handleCombatSettings(player, itemName, clickType, settings);
            return;
        }

        // Boss Settings sub-menu
        if (title.equals("§5§lBoss Settings")) {
            handleBossSettings(player, itemName, clickType);
            return;
        }

        // Broadcast Settings sub-menu
        if (title.equals("§e§lBroadcast Settings")) {
            handleBroadcastSettings(player, itemName);
            return;
        }

        // Multiplier Settings sub-menu
        if (title.equals("§a§lMultiplier Settings")) {
            handleMultiplierSettings(player, itemName, clickType, settings);
            return;
        }

        // Preset Settings sub-menu
        if (title.equals("§b§lQuick Presets")) {
            handlePresetSettings(player, itemName, settings);
            return;
        }

        // Death Settings sub-menu
        if (title.equals("§8§lDeath Settings")) {
            handleDeathSettings(player, itemName, clickType);
            return;
        }

        // Combat Log Settings sub-menu
        if (title.equals("§c§lCombat Log Settings")) {
            handleCombatLogSettings(player, itemName, clickType);
            return;
        }

        // Particle Settings sub-menu
        if (title.equals("§d§lParticle Settings")) {
            handleParticleSettings(player, itemName, clickType);
            return;
        }
    }

    private void handleGameplaySettings(Player player, String itemName, ClickType clickType) {
        if (itemName.contains("Particle Settings")) {
            adminGUI.openParticleSettings(player);
            return;
        } else if (itemName.contains("Level Loss on Death")) {
            plugin.setLevelLossOnDeath(!plugin.isLevelLossOnDeath());
            player.sendMessage("§aLevel loss on death " + (plugin.isLevelLossOnDeath() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Level Gain on Kill")) {
            plugin.setLevelGainOnKill(!plugin.isLevelGainOnKill());
            player.sendMessage("§aLevel gain on kill " + (plugin.isLevelGainOnKill() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Auto-Assign on Join")) {
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
        } else if (itemName.contains("PvP Only Abilities")) {
            plugin.setPvpOnlyAbilities(!plugin.isPvpOnlyAbilities());
            player.sendMessage("§aPvP only abilities " + (plugin.isPvpOnlyAbilities() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Friendly Fire")) {
            plugin.setFriendlyFire(!plugin.isFriendlyFire());
            player.sendMessage("§aFriendly fire " + (plugin.isFriendlyFire() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Kill Streak Bonuses")) {
            if (clickType == ClickType.LEFT) {
                plugin.setKillStreakBonuses(!plugin.isKillStreakBonuses());
                player.sendMessage("§aKill streak bonuses " + (plugin.isKillStreakBonuses() ? "§aenabled" : "§cdisabled"));
            } else if (clickType == ClickType.RIGHT) {
                plugin.setKillStreakThreshold(plugin.getKillStreakThreshold() + 1);
                player.sendMessage("§aKill streak threshold: §e" + plugin.getKillStreakThreshold());
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                plugin.setKillStreakThreshold(plugin.getKillStreakThreshold() - 1);
                player.sendMessage("§aKill streak threshold: §e" + plugin.getKillStreakThreshold());
            }
        } else if (itemName.contains("Max Level")) {
            if (clickType == ClickType.LEFT) {
                plugin.setMaxLevel(plugin.getMaxLevel() - 1);
            } else {
                plugin.setMaxLevel(plugin.getMaxLevel() + 1);
            }
            player.sendMessage("§aMax level: §e" + plugin.getMaxLevel());
        } else if (itemName.contains("Levels Lost on Death")) {
            if (clickType == ClickType.LEFT) {
                plugin.setLevelsLostOnDeath(plugin.getLevelsLostOnDeath() - 1);
            } else {
                plugin.setLevelsLostOnDeath(plugin.getLevelsLostOnDeath() + 1);
            }
            player.sendMessage("§aLevels lost on death: §e" + plugin.getLevelsLostOnDeath());
        } else if (itemName.contains("Levels Gained on Kill")) {
            if (clickType == ClickType.LEFT) {
                plugin.setLevelsGainedOnKill(plugin.getLevelsGainedOnKill() - 1);
            } else {
                plugin.setLevelsGainedOnKill(plugin.getLevelsGainedOnKill() + 1);
            }
            player.sendMessage("§aLevels gained on kill: §e" + plugin.getLevelsGainedOnKill());
        } else if (itemName.contains("Passive Tick Rate")) {
            if (clickType == ClickType.LEFT) {
                plugin.setPassiveTickRate(plugin.getPassiveTickRate() - 0.5);
            } else {
                plugin.setPassiveTickRate(plugin.getPassiveTickRate() + 0.5);
            }
            player.sendMessage("§aPassive tick rate: §e" + String.format("%.1f", plugin.getPassiveTickRate()) + "s");
        } else if (itemName.contains("Passive Effect Strength")) {
            if (clickType == ClickType.LEFT) {
                plugin.setPassiveEffectStrength(plugin.getPassiveEffectStrength() - 0.1);
            } else {
                plugin.setPassiveEffectStrength(plugin.getPassiveEffectStrength() + 0.1);
            }
            player.sendMessage("§aPassive effect strength: §e" + String.format("%.1fx", plugin.getPassiveEffectStrength()));
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openGameplaySettings(player);
    }

    private void handleCombatSettings(Player player, String itemName, ClickType clickType, AttributeSettings settings) {
        if (itemName.contains("PvP Damage Multiplier")) {
            if (clickType.isShiftClick()) {
                plugin.setPvpDamageMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                plugin.setPvpDamageMultiplier(plugin.getPvpDamageMultiplier() - 0.1);
            } else {
                plugin.setPvpDamageMultiplier(plugin.getPvpDamageMultiplier() + 0.1);
            }
            player.sendMessage("§aPvP damage multiplier: §e" + String.format("%.1fx", plugin.getPvpDamageMultiplier()));
        } else if (itemName.contains("Ability Damage Multiplier")) {
            if (clickType.isShiftClick()) {
                plugin.setAbilityDamageMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                plugin.setAbilityDamageMultiplier(plugin.getAbilityDamageMultiplier() - 0.1);
            } else {
                plugin.setAbilityDamageMultiplier(plugin.getAbilityDamageMultiplier() + 0.1);
            }
            player.sendMessage("§aAbility damage multiplier: §e" + String.format("%.1fx", plugin.getAbilityDamageMultiplier()));
        } else if (itemName.contains("Global Damage Multiplier")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalDamageMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalDamageMultiplier(settings.getGlobalDamageMultiplier() - 0.1);
            } else {
                settings.setGlobalDamageMultiplier(settings.getGlobalDamageMultiplier() + 0.1);
            }
            player.sendMessage("§aGlobal damage multiplier: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()));
        } else if (itemName.contains("Global Cooldown Multiplier")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalCooldownMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalCooldownMultiplier(settings.getGlobalCooldownMultiplier() - 0.1);
            } else {
                settings.setGlobalCooldownMultiplier(settings.getGlobalCooldownMultiplier() + 0.1);
            }
            player.sendMessage("§aGlobal cooldown multiplier: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()));
        } else if (itemName.contains("Combat Tag") && !itemName.contains("Duration")) {
            plugin.setCombatTagEnabled(!plugin.isCombatTagEnabled());
            player.sendMessage("§aCombat tag " + (plugin.isCombatTagEnabled() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Combat Tag Duration")) {
            if (clickType == ClickType.LEFT) {
                plugin.setCombatTagDuration(plugin.getCombatTagDuration() - 5);
            } else {
                plugin.setCombatTagDuration(plugin.getCombatTagDuration() + 5);
            }
            player.sendMessage("§aCombat tag duration: §e" + plugin.getCombatTagDuration() + "s");
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openCombatSettings(player);
    }

    private void handleBossSettings(Player player, String itemName, ClickType clickType) {
        if (itemName.contains("Boss Health Multiplier")) {
            if (clickType.isShiftClick()) {
                plugin.setBossHealthMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                plugin.setBossHealthMultiplier(plugin.getBossHealthMultiplier() - 0.25);
            } else {
                plugin.setBossHealthMultiplier(plugin.getBossHealthMultiplier() + 0.25);
            }
            player.sendMessage("§aBoss health multiplier: §e" + String.format("%.2fx", plugin.getBossHealthMultiplier()));
        } else if (itemName.contains("Boss Damage Multiplier")) {
            if (clickType.isShiftClick()) {
                plugin.setBossDamageMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                plugin.setBossDamageMultiplier(plugin.getBossDamageMultiplier() - 0.25);
            } else {
                plugin.setBossDamageMultiplier(plugin.getBossDamageMultiplier() + 0.25);
            }
            player.sendMessage("§aBoss damage multiplier: §e" + String.format("%.2fx", plugin.getBossDamageMultiplier()));
        } else if (itemName.contains("Boss Drop Rate Multiplier")) {
            if (clickType.isShiftClick()) {
                plugin.setBossDropRateMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                plugin.setBossDropRateMultiplier(plugin.getBossDropRateMultiplier() - 0.25);
            } else {
                plugin.setBossDropRateMultiplier(plugin.getBossDropRateMultiplier() + 0.25);
            }
            player.sendMessage("§aBoss drop rate multiplier: §e" + String.format("%.2fx", plugin.getBossDropRateMultiplier()));
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openBossSettings(player);
    }

    private void handleBroadcastSettings(Player player, String itemName) {
        if (itemName.contains("Attribute Assignment")) {
            plugin.setBroadcastAttributeAssign(!plugin.isBroadcastAttributeAssign());
            player.sendMessage("§aBroadcast attribute assignment " + (plugin.isBroadcastAttributeAssign() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Level Up")) {
            plugin.setBroadcastLevelUp(!plugin.isBroadcastLevelUp());
            player.sendMessage("§aBroadcast level up " + (plugin.isBroadcastLevelUp() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Dragon Egg")) {
            plugin.setBroadcastDragonEgg(!plugin.isBroadcastDragonEgg());
            player.sendMessage("§aBroadcast Dragon Egg " + (plugin.isBroadcastDragonEgg() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Boss Spawn")) {
            plugin.setBroadcastBossSpawn(!plugin.isBroadcastBossSpawn());
            player.sendMessage("§aBroadcast boss spawn " + (plugin.isBroadcastBossSpawn() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Boss Defeat")) {
            plugin.setBroadcastBossDefeat(!plugin.isBroadcastBossDefeat());
            player.sendMessage("§aBroadcast boss defeat " + (plugin.isBroadcastBossDefeat() ? "§aenabled" : "§cdisabled"));
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openBroadcastSettings(player);
    }

    private void handleMultiplierSettings(Player player, String itemName, ClickType clickType, AttributeSettings settings) {
        if (itemName.contains("Global Cooldown")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalCooldownMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalCooldownMultiplier(settings.getGlobalCooldownMultiplier() - 0.1);
            } else {
                settings.setGlobalCooldownMultiplier(settings.getGlobalCooldownMultiplier() + 0.1);
            }
            player.sendMessage("§aGlobal cooldown: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()));
        } else if (itemName.contains("Global Damage")) {
            if (clickType.isShiftClick()) {
                settings.setGlobalDamageMultiplier(1.0);
            } else if (clickType == ClickType.LEFT) {
                settings.setGlobalDamageMultiplier(settings.getGlobalDamageMultiplier() - 0.1);
            } else {
                settings.setGlobalDamageMultiplier(settings.getGlobalDamageMultiplier() + 0.1);
            }
            player.sendMessage("§aGlobal damage: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()));
        } else if (itemName.contains("Level Scaling")) {
            if (clickType.isShiftClick()) {
                settings.setLevelScalingPercent(10.0);
            } else if (clickType == ClickType.LEFT) {
                settings.setLevelScalingPercent(settings.getLevelScalingPercent() - 1.0);
            } else {
                settings.setLevelScalingPercent(settings.getLevelScalingPercent() + 1.0);
            }
            player.sendMessage("§aLevel scaling: §e+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level");
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openMultiplierSettings(player);
    }

    private void handlePresetSettings(Player player, String itemName, AttributeSettings settings) {
        if (itemName.contains("Balanced Preset")) {
            settings.applyPresetToAll("balanced");
            player.sendMessage("§a§lApplied Balanced preset!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        } else if (itemName.contains("High Power Preset")) {
            settings.applyPresetToAll("highpower");
            player.sendMessage("§c§lApplied High Power preset!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.8f);
        } else if (itemName.contains("Low Power Preset")) {
            settings.applyPresetToAll("lowpower");
            player.sendMessage("§7§lApplied Low Power preset!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
        } else if (itemName.contains("Chaos Preset")) {
            settings.applyPresetToAll("chaos");
            player.sendMessage("§d§lApplied Chaos preset!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.5f);
        } else if (itemName.contains("OP Preset")) {
            settings.setGlobalDamageMultiplier(2.0);
            settings.setGlobalCooldownMultiplier(0.5);
            for (AttributeType type : AttributeType.values()) {
                settings.applyHighPowerPreset(type);
            }
            player.sendMessage("§6§lApplied OP preset!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        }
        adminGUI.openPresetSettings(player);
    }

    private void handleDeathSettings(Player player, String itemName, ClickType clickType) {
        if (itemName.contains("Keep Inventory on Death")) {
            plugin.setKeepInventoryOnDeath(!plugin.isKeepInventoryOnDeath());
            player.sendMessage("§aKeep inventory on death " + (plugin.isKeepInventoryOnDeath() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Drop Attribute Items")) {
            plugin.setDropAttributeItemsOnDeath(!plugin.isDropAttributeItemsOnDeath());
            player.sendMessage("§aDrop attribute items " + (plugin.isDropAttributeItemsOnDeath() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Levels Lost on Death")) {
            if (clickType == ClickType.LEFT) {
                plugin.setLevelsLostOnDeath(plugin.getLevelsLostOnDeath() - 1);
            } else {
                plugin.setLevelsLostOnDeath(plugin.getLevelsLostOnDeath() + 1);
            }
            player.sendMessage("§aLevels lost on death: §e" + plugin.getLevelsLostOnDeath());
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openDeathSettings(player);
    }

    private void handleCombatLogSettings(Player player, String itemName, ClickType clickType) {
        CombatLogger logger = plugin.getCombatLogger();

        // Main toggles (row 1)
        if (itemName.contains("Combat Logging") && !itemName.contains("Log")) {
            logger.setEnabled(!logger.isEnabled());
            player.sendMessage("§aCombat logging " + (logger.isEnabled() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log to File")) {
            logger.setLogToFile(!logger.isLogToFile());
            player.sendMessage("§aLog to file " + (logger.isLogToFile() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log to Console")) {
            logger.setLogToConsole(!logger.isLogToConsole());
            player.sendMessage("§aLog to console " + (logger.isLogToConsole() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Show to Players")) {
            logger.setShowToPlayers(!logger.isShowToPlayers());
            player.sendMessage("§aShow to players " + (logger.isShowToPlayers() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Compact Mode")) {
            logger.setCompactMode(!logger.isCompactMode());
            player.sendMessage("§aCompact mode " + (logger.isCompactMode() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Show Damage Numbers")) {
            logger.setShowDamageNumbers(!logger.isShowDamageNumbers());
            player.sendMessage("§aShow damage numbers " + (logger.isShowDamageNumbers() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Show Health Bars")) {
            logger.setShowHealthBars(!logger.isShowHealthBars());
            player.sendMessage("§aShow health bars " + (logger.isShowHealthBars() ? "§aenabled" : "§cdisabled"));
        }
        // Event type toggles (row 2)
        else if (itemName.contains("Log Damage Events")) {
            logger.setLogDamageEvents(!logger.isLogDamageEvents());
            player.sendMessage("§aLog damage events " + (logger.isLogDamageEvents() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Ability Events")) {
            logger.setLogAbilityEvents(!logger.isLogAbilityEvents());
            player.sendMessage("§aLog ability events " + (logger.isLogAbilityEvents() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Kill Events")) {
            logger.setLogKillEvents(!logger.isLogKillEvents());
            player.sendMessage("§aLog kill events " + (logger.isLogKillEvents() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Healing Events")) {
            logger.setLogHealingEvents(!logger.isLogHealingEvents());
            player.sendMessage("§aLog healing events " + (logger.isLogHealingEvents() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Combat Tag Events")) {
            logger.setLogCombatTagEvents(!logger.isLogCombatTagEvents());
            player.sendMessage("§aLog combat tag events " + (logger.isLogCombatTagEvents() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Critical Hits")) {
            logger.setLogCriticalHits(!logger.isLogCriticalHits());
            player.sendMessage("§aLog critical hits " + (logger.isLogCriticalHits() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Blocked Damage")) {
            logger.setLogBlockedDamage(!logger.isLogBlockedDamage());
            player.sendMessage("§aLog blocked damage " + (logger.isLogBlockedDamage() ? "§aenabled" : "§cdisabled"));
        }
        // Additional event types (row 3)
        else if (itemName.contains("Log Environmental Damage")) {
            logger.setLogEnvironmentalDamage(!logger.isLogEnvironmentalDamage());
            player.sendMessage("§aLog environmental damage " + (logger.isLogEnvironmentalDamage() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Log Mob Damage")) {
            logger.setLogMobDamage(!logger.isLogMobDamage());
            player.sendMessage("§aLog mob damage " + (logger.isLogMobDamage() ? "§aenabled" : "§cdisabled"));
        }
        // Numeric settings
        else if (itemName.contains("Damage Threshold")) {
            if (clickType.isShiftClick()) {
                logger.setMinimumDamageThreshold(0);
                player.sendMessage("§aDamage threshold reset to §e0");
            } else if (clickType == ClickType.LEFT) {
                logger.setMinimumDamageThreshold(logger.getMinimumDamageThreshold() - 0.5);
                player.sendMessage("§aDamage threshold: §e" + String.format("%.1f", logger.getMinimumDamageThreshold()));
            } else {
                logger.setMinimumDamageThreshold(logger.getMinimumDamageThreshold() + 0.5);
                player.sendMessage("§aDamage threshold: §e" + String.format("%.1f", logger.getMinimumDamageThreshold()));
            }
        } else if (itemName.contains("Max Log History") && !itemName.contains("Player")) {
            if (clickType == ClickType.LEFT) {
                logger.setMaxLogHistory(logger.getMaxLogHistory() - 10);
            } else {
                logger.setMaxLogHistory(logger.getMaxLogHistory() + 10);
            }
            player.sendMessage("§aMax log history: §e" + logger.getMaxLogHistory());
        } else if (itemName.contains("Max Player Log History")) {
            if (clickType == ClickType.LEFT) {
                logger.setMaxPlayerLogHistory(logger.getMaxPlayerLogHistory() - 10);
            } else {
                logger.setMaxPlayerLogHistory(logger.getMaxPlayerLogHistory() + 10);
            }
            player.sendMessage("§aMax player log history: §e" + logger.getMaxPlayerLogHistory());
        }
        // Actions
        else if (itemName.contains("Clear Global Log")) {
            logger.clearGlobalLog();
            player.sendMessage("§aCleared global combat log!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.5f);
        } else if (itemName.contains("Reset to Defaults")) {
            resetCombatLogSettings(logger);
            player.sendMessage("§aReset combat log settings to defaults!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.2f);
        } else if (itemName.contains("Enable All Events")) {
            logger.setLogDamageEvents(true);
            logger.setLogAbilityEvents(true);
            logger.setLogKillEvents(true);
            logger.setLogHealingEvents(true);
            logger.setLogCombatTagEvents(true);
            logger.setLogCriticalHits(true);
            logger.setLogBlockedDamage(true);
            logger.setLogEnvironmentalDamage(true);
            logger.setLogMobDamage(true);
            player.sendMessage("§aEnabled all event logging!");
        } else if (itemName.contains("Disable All Events")) {
            logger.setLogDamageEvents(false);
            logger.setLogAbilityEvents(false);
            logger.setLogKillEvents(false);
            logger.setLogHealingEvents(false);
            logger.setLogCombatTagEvents(false);
            logger.setLogCriticalHits(false);
            logger.setLogBlockedDamage(false);
            logger.setLogEnvironmentalDamage(false);
            logger.setLogMobDamage(false);
            player.sendMessage("§cDisabled all event logging!");
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openCombatLogSettings(player);
    }

    private void handleParticleSettings(Player player, String itemName, ClickType clickType) {
        // Master toggle
        if (itemName.contains("MASTER TOGGLE")) {
            plugin.setParticleMasterEnabled(!plugin.isParticleMasterEnabled());
            player.sendMessage("§dParticle Master: " + (plugin.isParticleMasterEnabled() ? "§aENABLED" : "§cDISABLED"));
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.0f);
        }
        // Ability particles
        else if (itemName.contains("Support Ability")) {
            plugin.setParticleSupportAbility(!plugin.getParticleSupportAbilityRaw());
            player.sendMessage("§bSupport ability particles " + (plugin.getParticleSupportAbilityRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Melee Ability")) {
            plugin.setParticleMeleeAbility(!plugin.getParticleMeleeAbilityRaw());
            player.sendMessage("§cMelee ability particles " + (plugin.getParticleMeleeAbilityRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Passive Ability")) {
            plugin.setParticlePassiveAbility(!plugin.getParticlePassiveAbilityRaw());
            player.sendMessage("§aPassive ability particles " + (plugin.getParticlePassiveAbilityRaw() ? "§aenabled" : "§cdisabled"));
        }
        // Combat particles
        else if (itemName.contains("Damage Hit")) {
            plugin.setParticleDamageHit(!plugin.getParticleDamageHitRaw());
            player.sendMessage("§cDamage hit particles " + (plugin.getParticleDamageHitRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Critical Hit")) {
            plugin.setParticleCriticalHit(!plugin.getParticleCriticalHitRaw());
            player.sendMessage("§6Critical hit particles " + (plugin.getParticleCriticalHitRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Blocking")) {
            plugin.setParticleBlocking(!plugin.getParticleBlockingRaw());
            player.sendMessage("§bBlocking particles " + (plugin.getParticleBlockingRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Healing")) {
            plugin.setParticleHealing(!plugin.getParticleHealingRaw());
            player.sendMessage("§aHealing particles " + (plugin.getParticleHealingRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Kill") && !itemName.contains("Streak")) {
            plugin.setParticleKill(!plugin.getParticleKillRaw());
            player.sendMessage("§4Kill particles " + (plugin.getParticleKillRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Death") && !itemName.contains("Boss")) {
            plugin.setParticleDeath(!plugin.getParticleDeathRaw());
            player.sendMessage("§8Death particles " + (plugin.getParticleDeathRaw() ? "§aenabled" : "§cdisabled"));
        }
        // Player event particles
        else if (itemName.contains("Level Up")) {
            plugin.setParticleLevelUp(!plugin.getParticleLevelUpRaw());
            player.sendMessage("§aLevel up particles " + (plugin.getParticleLevelUpRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Attribute Assign")) {
            plugin.setParticleAttributeAssign(!plugin.getParticleAttributeAssignRaw());
            player.sendMessage("§dAttribute assign particles " + (plugin.getParticleAttributeAssignRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Attribute Remove")) {
            plugin.setParticleAttributeRemove(!plugin.getParticleAttributeRemoveRaw());
            player.sendMessage("§7Attribute remove particles " + (plugin.getParticleAttributeRemoveRaw() ? "§aenabled" : "§cdisabled"));
        }
        // Boss particles
        else if (itemName.contains("Boss Ambient")) {
            plugin.setParticleBossAmbient(!plugin.getParticleBossAmbientRaw());
            player.sendMessage("§5Boss ambient particles " + (plugin.getParticleBossAmbientRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Boss Ability")) {
            plugin.setParticleBossAbility(!plugin.getParticleBossAbilityRaw());
            player.sendMessage("§cBoss ability particles " + (plugin.getParticleBossAbilityRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Boss Spawn")) {
            plugin.setParticleBossSpawn(!plugin.getParticleBossSpawnRaw());
            player.sendMessage("§4Boss spawn particles " + (plugin.getParticleBossSpawnRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Boss Death")) {
            plugin.setParticleBossDeath(!plugin.getParticleBossDeathRaw());
            player.sendMessage("§eBoss death particles " + (plugin.getParticleBossDeathRaw() ? "§aenabled" : "§cdisabled"));
        }
        // World particles
        else if (itemName.contains("Altar Ambient")) {
            plugin.setParticleAltarAmbient(!plugin.getParticleAltarAmbientRaw());
            player.sendMessage("§9Altar ambient particles " + (plugin.getParticleAltarAmbientRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Altar Activation")) {
            plugin.setParticleAltarActivation(!plugin.getParticleAltarActivationRaw());
            player.sendMessage("§bAltar activation particles " + (plugin.getParticleAltarActivationRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Item Pickup")) {
            plugin.setParticleItemPickup(!plugin.getParticleItemPickupRaw());
            player.sendMessage("§aItem pickup particles " + (plugin.getParticleItemPickupRaw() ? "§aenabled" : "§cdisabled"));
        }
        // Effect particles
        else if (itemName.contains("Status Effects")) {
            plugin.setParticleStatusEffect(!plugin.getParticleStatusEffectRaw());
            player.sendMessage("§eStatus effect particles " + (plugin.getParticleStatusEffectRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Buff Applied")) {
            plugin.setParticleBuffApplied(!plugin.getParticleBuffAppliedRaw());
            player.sendMessage("§aBuff applied particles " + (plugin.getParticleBuffAppliedRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Debuff Applied")) {
            plugin.setParticleDebuffApplied(!plugin.getParticleDebuffAppliedRaw());
            player.sendMessage("§cDebuff applied particles " + (plugin.getParticleDebuffAppliedRaw() ? "§aenabled" : "§cdisabled"));
        }
        // Special particles
        else if (itemName.contains("Teleport")) {
            plugin.setParticleTeleport(!plugin.getParticleTeleportRaw());
            player.sendMessage("§5Teleport particles " + (plugin.getParticleTeleportRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Respawn")) {
            plugin.setParticleRespawn(!plugin.getParticleRespawnRaw());
            player.sendMessage("§fRespawn particles " + (plugin.getParticleRespawnRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Combo")) {
            plugin.setParticleCombo(!plugin.getParticleComboRaw());
            player.sendMessage("§6Combo particles " + (plugin.getParticleComboRaw() ? "§aenabled" : "§cdisabled"));
        } else if (itemName.contains("Kill Streak")) {
            plugin.setParticleKillStreak(!plugin.getParticleKillStreakRaw());
            player.sendMessage("§cKill streak particles " + (plugin.getParticleKillStreakRaw() ? "§aenabled" : "§cdisabled"));
        }
        // Numeric settings
        else if (itemName.contains("Particle Intensity")) {
            if (clickType.isShiftClick()) {
                plugin.setParticleIntensity(1.0);
                player.sendMessage("§eParticle intensity reset to §e1.0x");
            } else if (clickType == ClickType.LEFT) {
                plugin.setParticleIntensity(plugin.getParticleIntensity() - 0.25);
                player.sendMessage("§eParticle intensity: §e" + String.format("%.2fx", plugin.getParticleIntensity()));
            } else {
                plugin.setParticleIntensity(plugin.getParticleIntensity() + 0.25);
                player.sendMessage("§eParticle intensity: §e" + String.format("%.2fx", plugin.getParticleIntensity()));
            }
        } else if (itemName.contains("Render Distance")) {
            if (clickType == ClickType.LEFT) {
                plugin.setParticleRenderDistance(plugin.getParticleRenderDistance() - 8);
            } else {
                plugin.setParticleRenderDistance(plugin.getParticleRenderDistance() + 8);
            }
            player.sendMessage("§eParticle render distance: §e" + plugin.getParticleRenderDistance() + " blocks");
        }
        // Actions
        else if (itemName.contains("Enable All")) {
            plugin.enableAllParticles();
            player.sendMessage("§a§lEnabled all particle types!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        } else if (itemName.contains("Disable All")) {
            plugin.disableAllParticles();
            player.sendMessage("§c§lDisabled all particle types!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.5f);
        } else if (itemName.contains("Reset Defaults")) {
            resetParticleSettings();
            player.sendMessage("§a§lReset all particle settings to defaults!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.2f);
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        adminGUI.openParticleSettings(player);
    }

    private void resetParticleSettings() {
        plugin.setParticleMasterEnabled(true);
        plugin.enableAllParticles();
        plugin.setParticleIntensity(1.0);
        plugin.setParticleRenderDistance(32);
    }

    private void resetCombatLogSettings(CombatLogger logger) {
        logger.setEnabled(true);
        logger.setLogToFile(true);
        logger.setLogToConsole(false);
        logger.setShowToPlayers(true);
        logger.setLogDamageEvents(true);
        logger.setLogAbilityEvents(true);
        logger.setLogKillEvents(true);
        logger.setLogHealingEvents(true);
        logger.setLogCombatTagEvents(true);
        logger.setLogCriticalHits(true);
        logger.setLogBlockedDamage(true);
        logger.setLogEnvironmentalDamage(false);
        logger.setLogMobDamage(false);
        logger.setShowDamageNumbers(true);
        logger.setShowHealthBars(true);
        logger.setCompactMode(false);
        logger.setMinimumDamageThreshold(0);
        logger.setMaxLogHistory(100);
        logger.setMaxPlayerLogHistory(50);
    }

    private void resetAllSettings(AttributeSettings settings) {
        settings.resetToDefaults();
        plugin.setLevelLossOnDeath(true);
        plugin.setLevelGainOnKill(true);
        plugin.setParticleEffectsEnabled(true);
        plugin.setAutoAssignEnabled(false);
        plugin.setAutoAssignDelaySeconds(10);
        plugin.setPvpOnlyAbilities(false);
        plugin.setFriendlyFire(true);
        plugin.setMaxLevel(5);
        plugin.setLevelsLostOnDeath(1);
        plugin.setLevelsGainedOnKill(1);
        plugin.setKillStreakBonuses(false);
        plugin.setKillStreakThreshold(3);
        plugin.setBroadcastAttributeAssign(true);
        plugin.setBroadcastLevelUp(false);
        plugin.setBroadcastDragonEgg(true);
        plugin.setBroadcastBossSpawn(true);
        plugin.setBroadcastBossDefeat(true);
        plugin.setBossHealthMultiplier(1.0);
        plugin.setBossDamageMultiplier(1.0);
        plugin.setBossDropRateMultiplier(1.0);
        plugin.setPassiveTickRate(1.0);
        plugin.setPassiveEffectStrength(1.0);
        plugin.setPvpDamageMultiplier(1.0);
        plugin.setAbilityDamageMultiplier(1.0);
        plugin.setCombatTagEnabled(true);
        plugin.setCombatTagDuration(15);
        plugin.setKeepInventoryOnDeath(false);
        plugin.setDropAttributeItemsOnDeath(true);
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

    private void assignAttribute(Player admin, Player target, AttributeType attr) {
        // Get old attribute for cleanup
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Assign new attribute
        PlayerData data = new PlayerData(attr);
        plugin.setPlayerData(target.getUniqueId(), data);
        ParticleManager.playSupportParticles(target, attr, 1);
        plugin.updatePlayerTab(target);

        // Apply Dragon Egg effects if needed
        if (attr == AttributeType.DRAGON_EGG) {
            plugin.getEventListener().applyDragonEggEffects(target);
        }

        admin.sendMessage("§aAssigned §e" + attr.getDisplayName() + " §ato " + target.getName());
        target.sendMessage("§aYou received §e" + attr.getIcon() + " " + attr.getDisplayName() + "§a!");
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
        ParticleManager.playSupportParticles(target, data.getAttribute(), data.getLevel());
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
        PlayerData data = new PlayerData(AttributeType.DRAGON_EGG);
        plugin.setPlayerData(target.getUniqueId(), data);
        ParticleManager.playSupportParticles(target, AttributeType.DRAGON_EGG, 1);
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
        ParticleManager.playSupportParticles(target, data.getAttribute(), data.getLevel());
        ParticleManager.playPassiveParticles(target, data.getAttribute());

        // Find nearby entity for melee test
        org.bukkit.entity.Entity nearestEntity = target.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof org.bukkit.entity.LivingEntity)
                .findFirst()
                .orElse(null);

        if (nearestEntity != null) {
            ParticleManager.playMeleeParticles(target, nearestEntity, data.getAttribute());
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

    // ========== ALTAR SETTINGS HANDLERS ==========

    // Track which altar each player is currently editing
    private final java.util.Map<java.util.UUID, WeaponAltar> editingAltar = new java.util.HashMap<>();

    public void setEditingAltar(Player player, WeaponAltar altar) {
        if (altar != null) {
            editingAltar.put(player.getUniqueId(), altar);
        } else {
            editingAltar.remove(player.getUniqueId());
        }
    }

    private WeaponAltar getEditingAltar(Player player) {
        return editingAltar.get(player.getUniqueId());
    }

    private void handleAltarSettings(Player player, ItemStack clicked, String itemName, ClickType click, int slot) {
        WeaponAltar altar = getEditingAltar(player);

        // Per-altar settings (row 2, slots 9-17)
        if (altar != null && slot >= 9 && slot <= 17) {
            switch (slot) {
                case 9: // Base Block
                    if (click == ClickType.RIGHT) {
                        altar.setBaseBlock(org.bukkit.Material.ANCIENT_DEBRIS);
                    } else {
                        altar.setBaseBlock(AdminGUI.cycleBaseBlock(altar.getBaseBlock()));
                    }
                    altar.respawn();
                    break;
                case 10: // Weapon Size
                    float scale = altar.getWeaponScale();
                    if (click == ClickType.RIGHT) {
                        scale = Math.max(0.5f, scale - 0.25f);
                    } else {
                        scale = Math.min(5.0f, scale + 0.25f);
                    }
                    altar.setWeaponScale(scale);
                    altar.respawn();
                    break;
                case 11: // Rotation Speed
                    double rSpeed = altar.getRotationSpeed();
                    if (click == ClickType.RIGHT) {
                        rSpeed = Math.max(0.0, rSpeed - 0.01);
                    } else {
                        rSpeed = Math.min(0.5, rSpeed + 0.01);
                    }
                    altar.setRotationSpeed(rSpeed);
                    break;
                case 12: // Particle Type
                    altar.setParticleType(AdminGUI.cycleParticle(altar.getParticleType()));
                    break;
                case 13: // Particle Count
                    int pCount = altar.getParticleCount();
                    if (click == ClickType.RIGHT) {
                        pCount = Math.max(1, pCount - 5);
                    } else {
                        pCount = Math.min(100, pCount + 5);
                    }
                    altar.setParticleCount(pCount);
                    break;
                case 14: // Particles Toggle
                    altar.setParticlesEnabled(!altar.isParticlesEnabled());
                    break;
                case 15: // Rotation Toggle
                    altar.setRotationEnabled(!altar.isRotationEnabled());
                    break;
                case 17: // Delete This Altar
                    plugin.removeAltar(altar);
                    setEditingAltar(player, null);
                    player.closeInventory();
                    player.sendMessage("§aAltar removed!");
                    return;
            }
            // Refresh the GUI
            plugin.getAdminGUI().openAltarSettingsGUI(player, altar);
            return;
        }

        // Global settings (row 4, slots 27-35)
        if (slot >= 27 && slot <= 35) {
            switch (slot) {
                case 27: // Global Base Block
                    if (click == ClickType.RIGHT) {
                        WeaponAltar.setGlobalBaseBlock(org.bukkit.Material.ANCIENT_DEBRIS);
                    } else {
                        WeaponAltar.setGlobalBaseBlock(AdminGUI.cycleBaseBlock(WeaponAltar.getGlobalBaseBlock()));
                    }
                    break;
                case 28: // Global Weapon Size
                    float gScale = WeaponAltar.getGlobalWeaponScale();
                    if (click == ClickType.RIGHT) {
                        gScale = Math.max(0.5f, gScale - 0.25f);
                    } else {
                        gScale = Math.min(5.0f, gScale + 0.25f);
                    }
                    WeaponAltar.setGlobalWeaponScale(gScale);
                    break;
                case 29: // Global Rotation Speed
                    double gRSpeed = WeaponAltar.getGlobalRotationSpeed();
                    if (click == ClickType.RIGHT) {
                        gRSpeed = Math.max(0.0, gRSpeed - 0.01);
                    } else {
                        gRSpeed = Math.min(0.5, gRSpeed + 0.01);
                    }
                    WeaponAltar.setGlobalRotationSpeed(gRSpeed);
                    break;
                case 30: // Global Particle Type
                    WeaponAltar.setGlobalParticleType(AdminGUI.cycleParticle(WeaponAltar.getGlobalParticleType()));
                    break;
                case 31: // Global Particle Count
                    int gPCount = WeaponAltar.getGlobalParticleCount();
                    if (click == ClickType.RIGHT) {
                        gPCount = Math.max(1, gPCount - 5);
                    } else {
                        gPCount = Math.min(100, gPCount + 5);
                    }
                    WeaponAltar.setGlobalParticleCount(gPCount);
                    break;
                case 32: // Global Particles Toggle
                    WeaponAltar.setGlobalParticlesEnabled(!WeaponAltar.isGlobalParticlesEnabled());
                    break;
                case 33: // Global Rotation Toggle
                    WeaponAltar.setGlobalRotationEnabled(!WeaponAltar.isGlobalRotationEnabled());
                    break;
                case 35: // Apply Global to All
                    for (WeaponAltar a : plugin.getActiveAltars()) {
                        a.applyGlobalSettings();
                        a.respawn();
                    }
                    player.sendMessage("§aApplied global settings to all " + plugin.getActiveAltars().size() + " altar(s) and respawned!");
                    break;
            }
            // Refresh the GUI
            plugin.getAdminGUI().openAltarSettingsGUI(player, altar);
            return;
        }

        // Bottom row actions (slots 45-53)
        switch (slot) {
            case 45: // List All Altars
                plugin.getAdminGUI().openAltarListGUI(player);
                break;
            case 47: // Teleport
                if (altar != null) {
                    player.teleport(altar.getLocation().add(0, 1, 0));
                    player.closeInventory();
                    player.sendMessage("§aTeleported to " + altar.getWeapon().getColor() + altar.getWeapon().getName() + " §aaltar!");
                }
                break;
            case 49: // Respawn Current
                if (altar != null) {
                    altar.respawn();
                    player.sendMessage("§aAltar respawned with current settings!");
                    plugin.getAdminGUI().openAltarSettingsGUI(player, altar);
                }
                break;
            case 51: // Delete ALL Altars
                if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                    int count = plugin.getActiveAltars().size();
                    for (WeaponAltar a : new java.util.ArrayList<>(plugin.getActiveAltars())) {
                        plugin.removeAltar(a);
                    }
                    setEditingAltar(player, null);
                    player.closeInventory();
                    player.sendMessage("§cRemoved all §e" + count + " §caltar(s)!");
                } else {
                    player.sendMessage("§cShift-click to confirm deletion of all altars!");
                }
                break;
            case 53: // Close
                player.closeInventory();
                break;
        }
    }

    private void handleAltarList(Player player, ItemStack clicked, String itemName, ClickType click, int slot) {
        java.util.List<WeaponAltar> altars = plugin.getActiveAltars();

        // Back button (last slot)
        if (itemName.contains("Back")) {
            WeaponAltar editing = getEditingAltar(player);
            plugin.getAdminGUI().openAltarSettingsGUI(player, editing);
            return;
        }

        // Altar item clicked
        if (slot >= 0 && slot < altars.size()) {
            WeaponAltar altar = altars.get(slot);

            if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                // Shift-click = delete
                plugin.removeAltar(altar);
                player.sendMessage("§aRemoved " + altar.getWeapon().getColor() + altar.getWeapon().getName() + " §aaltar!");
                plugin.getAdminGUI().openAltarListGUI(player);
            } else if (click == ClickType.RIGHT) {
                // Right-click = teleport
                player.teleport(altar.getLocation().add(0, 1, 0));
                player.closeInventory();
                player.sendMessage("§aTeleported to " + altar.getWeapon().getColor() + altar.getWeapon().getName() + " §aaltar!");
            } else {
                // Left-click = edit settings
                setEditingAltar(player, altar);
                plugin.getAdminGUI().openAltarSettingsGUI(player, altar);
            }
        }
    }
}