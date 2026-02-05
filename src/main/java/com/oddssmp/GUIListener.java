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

    public GUIListener(OddsSMP plugin, AdminGUI adminGUI) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
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
                !title.contains("Edit:") && !isAttributeDetailsGUI(title)) {
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
            handleAttributeValueEditor(player, title, itemName);
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
            handleSettings(player, itemName);
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

        // Global settings
        if (itemName.contains("Global Cooldown")) {
            player.sendMessage("§e§lGlobal cooldown editing coming soon!");
            return;
        }
        if (itemName.contains("Global Damage")) {
            player.sendMessage("§e§lGlobal damage editing coming soon!");
            return;
        }
        if (itemName.contains("Level Scaling")) {
            player.sendMessage("§e§lLevel scaling editing coming soon!");
            return;
        }

        // Parse attribute from item name
        for (AttributeType attr : AttributeType.values()) {
            if (itemName.contains(attr.getDisplayName())) {
                if (clickType == ClickType.LEFT) {
                    adminGUI.openAttributeValueEditor(player, attr);
                } else if (clickType == ClickType.RIGHT) {
                    player.sendMessage("§aReset " + attr.getDisplayName() + " to defaults!");
                    // TODO: Implement reset logic
                }
                return; // Important: return after finding match
            }
        }
    }

    private void handleAttributeValueEditor(Player player, String title, String itemName) {
        if (itemName.contains("Back")) {
            adminGUI.openAttributeEditor(player);
            return;
        }

        if (itemName.contains("Save Changes")) {
            player.sendMessage("§a§lChanges saved! (Feature coming soon)");
            return;
        }

        if (itemName.contains("Reset to Defaults")) {
            player.sendMessage("§c§lReset to defaults! (Feature coming soon)");
            return;
        }

        // Preset handlers
        if (itemName.contains("Preset: Balanced")) {
            player.sendMessage("§a§lApplied Balanced preset!");
            return;
        }
        if (itemName.contains("Preset: High Power")) {
            player.sendMessage("§c§lApplied High Power preset!");
            return;
        }
        if (itemName.contains("Preset: Low Power")) {
            player.sendMessage("§7§lApplied Low Power preset!");
            return;
        }
        if (itemName.contains("Preset: Chaos")) {
            player.sendMessage("§d§lApplied Chaos preset! Good luck!");
            return;
        }

        // Value editing
        player.sendMessage("§e§lValue editing interface coming soon!");
        player.sendMessage("§7You clicked: " + itemName);
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerData(p.getUniqueId());
                if (data == null || data.getAttribute() == null) {
                    AttributeType attr = AttributeType.getRandomAttribute(false);
                    Tier tier = Tier.getRandomTier();
                    plugin.setPlayerData(p.getUniqueId(), new PlayerData(attr, tier));
                    ParticleManager.playSupportParticles(p, attr, tier, 1);
                    plugin.updatePlayerTab(p);
                    p.sendMessage("§aYou received " + tier.getColor() + attr.getDisplayName() + "§a!");
                    count++;
                }
            }
            player.sendMessage("§aAssigned attributes to " + count + " players!");
        } else if (itemName.contains("Reroll All")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                // Get old attribute for cleanup
                PlayerData oldData = plugin.getPlayerData(p.getUniqueId());
                AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

                // Remove old attribute effects
                if (oldAttribute != null) {
                    plugin.getEventListener().removeAttributeEffects(p, oldAttribute);
                }

                // Assign new attribute
                AttributeType attr = AttributeType.getRandomAttribute(false);
                Tier tier = Tier.getRandomTier();
                plugin.setPlayerData(p.getUniqueId(), new PlayerData(attr, tier));
                ParticleManager.playSupportParticles(p, attr, tier, 1);
                plugin.updatePlayerTab(p);

                // Apply Dragon Egg effects if needed
                if (attr == AttributeType.DRAGON_EGG) {
                    plugin.getEventListener().applyDragonEggEffects(p);
                }

                p.sendMessage("§eYour attribute was rerolled to " + tier.getColor() + attr.getDisplayName() + "§e!");
            }
            player.sendMessage("§aRerolled all player attributes!");
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

    private void handleSettings(Player player, String itemName) {
        if (itemName.contains("Back")) {
            adminGUI.openMainMenu(player);
        } else {
            player.sendMessage("§e§lSettings are currently view-only!");
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
        AttributeType attr = AttributeType.getRandomAttribute(false);
        Tier tier = Tier.getRandomTier();
        assignAttribute(admin, target, attr, tier);
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

        // Get new random attribute and tier
        AttributeType attr = AttributeType.getRandomAttribute(false);
        Tier tier = Tier.getRandomTier();

        // Assign new attribute
        PlayerData data = new PlayerData(attr, tier);
        plugin.setPlayerData(target.getUniqueId(), data);
        ParticleManager.playSupportParticles(target, attr, tier, 1);
        plugin.updatePlayerTab(target);

        // Apply Dragon Egg effects if needed
        if (attr == AttributeType.DRAGON_EGG) {
            plugin.getEventListener().applyDragonEggEffects(target);
        }

        admin.sendMessage("§aRerolled " + target.getName() + "'s attribute to " + tier.getColor() + tier.name() + " " + attr.getDisplayName());
        target.sendMessage("§eYour attribute was rerolled to " + tier.getColor() + tier.name() + " " + attr.getIcon() + " " + attr.getDisplayName() + "§e!");
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
}