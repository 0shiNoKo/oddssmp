package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminGUI {

    private final OddsSMP plugin;

    public AdminGUI(OddsSMP plugin) {
        this.plugin = plugin;
    }

    /**
     * Main admin menu
     */
    public void openMainMenu(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lOddsSMP Admin Panel");

        // Player Management
        gui.setItem(10, createItem(Material.PLAYER_HEAD, "§e§lPlayer Management", Arrays.asList(
                "§7Click to manage players",
                "§7Assign, reroll, upgrade attributes")));

        // Attribute Browser
        gui.setItem(12, createItem(Material.ENCHANTED_BOOK, "§d§lAttribute Browser", Arrays.asList(
                "§7View all attributes",
                "§7See details and effects")));

        // Server Stats
        gui.setItem(14, createItem(Material.BOOK, "§a§lServer Statistics", Arrays.asList(
                "§7View server-wide stats",
                "§7Top players, tier distribution")));

        // Batch Operations
        gui.setItem(16, createItem(Material.COMMAND_BLOCK, "§c§lBatch Operations", Arrays.asList(
                "§7Perform bulk actions",
                "§7Assign all, reset all, etc.")));

        // Attribute Editor
        gui.setItem(20, createItem(Material.ANVIL, "§6§lAttribute Editor", Arrays.asList(
                "§7Edit attribute values",
                "§7Cooldowns, damage, duration",
                "§7Live server changes")));

        // Settings
        gui.setItem(22, createItem(Material.COMPARATOR, "§b§lPlugin Settings", Arrays.asList(
                "§7Configure plugin behavior",
                "§7Cooldowns, damage, durations")));

        admin.openInventory(gui);
    }

    /**
     * Attribute editor menu
     */
    public void openAttributeEditor(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lAttribute Editor");

        AttributeSettings settings = plugin.getAttributeSettings();

        AttributeType[] attributes = AttributeType.values();
        Material[] materials = {
                Material.IRON_SWORD,      // Melee
                Material.RED_DYE,          // Health
                Material.SHIELD,           // Defense
                Material.GOLD_INGOT,       // Wealth
                Material.FEATHER,          // Speed
                Material.ENDER_EYE,        // Control
                Material.BOW,              // Range
                Material.LIGHTNING_ROD,    // Pressure
                Material.CLOCK,            // Tempo
                Material.TNT,              // Disruption
                Material.SPYGLASS,         // Vision
                Material.TOTEM_OF_UNDYING, // Persistence
                Material.ANVIL,            // Anchor
                Material.ENDER_PEARL,      // Transfer
                Material.COMPARATOR,       // Risk
                Material.WITHER_SKELETON_SKULL, // Wither
                Material.SCULK_CATALYST,   // Warden
                Material.WIND_CHARGE,      // Breeze
                Material.DRAGON_EGG        // Dragon Egg
        };

        for (int i = 0; i < attributes.length; i++) {
            AttributeType attr = attributes[i];
            AttributeSettings.AttributeConfig config = settings.getConfig(attr);

            List<String> lore = new ArrayList<>();
            lore.add("§7Click to edit this attribute");
            lore.add("");
            lore.add("§7Support CD: §e" + String.format("%.1fx", config.supportCooldownModifier));
            lore.add("§7Melee CD: §e" + String.format("%.1fx", config.meleeCooldownModifier));
            lore.add("§7Damage: §e" + String.format("%.1fx", config.meleeDamageMultiplier));
            lore.add("");
            lore.add("§e§lLeft Click: §7Edit values");
            lore.add("§c§lRight Click: §7Reset to defaults");

            gui.setItem(i, createItem(materials[i], attr.getIcon() + " §e" + attr.getDisplayName(), lore));
        }

        // Global settings
        gui.setItem(45, createItem(Material.CLOCK, "§e§lGlobal Cooldown Multiplier", Arrays.asList(
                "§7Current: §a" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()),
                "",
                "§e§lLeft Click: §7-0.1x",
                "§e§lRight Click: §7+0.1x",
                "§e§lShift+Click: §7Reset to 1.0x")));

        gui.setItem(46, createItem(Material.DIAMOND_SWORD, "§c§lGlobal Damage Multiplier", Arrays.asList(
                "§7Current: §a" + String.format("%.1fx", settings.getGlobalDamageMultiplier()),
                "",
                "§e§lLeft Click: §7-0.1x",
                "§e§lRight Click: §7+0.1x",
                "§e§lShift+Click: §7Reset to 1.0x")));

        gui.setItem(47, createItem(Material.EXPERIENCE_BOTTLE, "§b§lLevel Scaling", Arrays.asList(
                "§7Current: §a+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level",
                "",
                "§e§lLeft Click: §7-1%",
                "§e§lRight Click: §7+1%",
                "§e§lShift+Click: §7Reset to 10%")));

        // Back button
        gui.setItem(49, createItem(Material.BARRIER, "§c§lBack", Arrays.asList("§7Return to main menu")));

        admin.openInventory(gui);
    }

    /**
     * Edit specific attribute values
     */
    public void openAttributeValueEditor(Player admin, AttributeType attribute) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lEdit: " + attribute.getDisplayName());

        AttributeSettings settings = plugin.getAttributeSettings();
        AttributeSettings.AttributeConfig config = settings.getConfig(attribute);

        // Support Ability Settings
        gui.setItem(10, createItem(Material.EMERALD, "§a§lSupport Cooldown Modifier", Arrays.asList(
                "§7Multiplier for support cooldown",
                "§7Current: §e" + String.format("%.1fx", config.supportCooldownModifier),
                "",
                "§7Stable: §e" + (int)(settings.getStableCooldown() * config.supportCooldownModifier) + "s",
                "§7Warped: §e" + (int)(settings.getWarpedCooldown() * config.supportCooldownModifier) + "s",
                "§7Extreme: §e" + (int)(settings.getExtremeCooldown() * config.supportCooldownModifier) + "s",
                "",
                "§e§lLeft: §7-0.1x  §e§lRight: §7+0.1x")));

        gui.setItem(11, createItem(Material.EXPERIENCE_BOTTLE, "§a§lSupport Duration", Arrays.asList(
                "§7Base duration of effect",
                "§7Current: §e" + config.supportDuration + "s",
                "",
                "§e§lLeft: §7-1s  §e§lRight: §7+1s")));

        gui.setItem(12, createItem(Material.REDSTONE, "§a§lSupport Range", Arrays.asList(
                "§7Radius of support effect",
                "§7Current: §e" + String.format("%.0f", config.supportRange) + " blocks",
                "",
                "§e§lLeft: §7-1 block  §e§lRight: §7+1 block")));

        // Melee Ability Settings
        gui.setItem(19, createItem(Material.IRON_SWORD, "§c§lMelee Cooldown Modifier", Arrays.asList(
                "§7Multiplier for melee cooldown",
                "§7Current: §e" + String.format("%.1fx", config.meleeCooldownModifier),
                "",
                "§7Stable: §e" + (int)(settings.getStableCooldown() * config.meleeCooldownModifier) + "s",
                "§7Warped: §e" + (int)(settings.getWarpedCooldown() * config.meleeCooldownModifier) + "s",
                "§7Extreme: §e" + (int)(settings.getExtremeCooldown() * config.meleeCooldownModifier) + "s",
                "",
                "§e§lLeft: §7-0.1x  §e§lRight: §7+0.1x")));

        gui.setItem(20, createItem(Material.DIAMOND_SWORD, "§c§lMelee Damage Multiplier", Arrays.asList(
                "§7Damage multiplier for this attribute",
                "§7Current: §e" + String.format("%.1fx", config.meleeDamageMultiplier),
                "",
                "§e§lLeft: §7-0.1x  §e§lRight: §7+0.1x")));

        gui.setItem(21, createItem(Material.EXPERIENCE_BOTTLE, "§c§lMelee Duration", Arrays.asList(
                "§7Effect duration after hit",
                "§7Current: §e" + config.meleeDuration + "s",
                "",
                "§e§lLeft: §7-1s  §e§lRight: §7+1s")));

        // Passive Ability Settings
        gui.setItem(28, createItem(Material.BOOK, "§b§lPassive Strength", Arrays.asList(
                "§7Passive effect multiplier",
                "§7Current: §e" + String.format("%.1fx", config.passiveStrength),
                "",
                "§e§lLeft: §7-0.1x  §e§lRight: §7+0.1x")));

        gui.setItem(29, createItem(Material.CLOCK, "§b§lPassive Tick Rate", Arrays.asList(
                "§7How often passive activates",
                "§7Current: §e" + String.format("%.1fs", config.passiveTickRate),
                "",
                "§e§lLeft: §7-0.25s  §e§lRight: §7+0.25s")));

        // Tier Multipliers (global settings)
        gui.setItem(37, createItem(Material.LIME_DYE, "§a§lStable Tier", Arrays.asList(
                "§7Effect: §e" + (int)(settings.getStableMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getStableCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getStableDrawback() * 100) + "%",
                "",
                "§e§lLeft: §7-10s CD  §e§lRight: §7+10s CD",
                "§e§lShift+Left: §7-10% effect")));

        gui.setItem(38, createItem(Material.PURPLE_DYE, "§d§lWarped Tier", Arrays.asList(
                "§7Effect: §e" + (int)(settings.getWarpedMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getWarpedCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getWarpedDrawback() * 100) + "%",
                "",
                "§e§lLeft: §7-10s CD  §e§lRight: §7+10s CD",
                "§e§lShift+Left: §7-10% effect")));

        gui.setItem(39, createItem(Material.RED_DYE, "§c§lExtreme Tier", Arrays.asList(
                "§7Effect: §e" + (int)(settings.getExtremeMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getExtremeCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getExtremeDrawback() * 100) + "%",
                "",
                "§e§lLeft: §7-10s CD  §e§lRight: §7+10s CD",
                "§e§lShift+Left: §7-10% effect")));

        // Quick presets
        gui.setItem(45, createItem(Material.PAPER, "§e§lPreset: Balanced", Arrays.asList(
                "§7Standard competitive values",
                "§7All multipliers set to 1.0x",
                "",
                "§e§lClick to apply to this attribute",
                "§e§lShift+Click to apply to ALL")));

        gui.setItem(46, createItem(Material.REDSTONE, "§c§lPreset: High Power", Arrays.asList(
                "§7Increased damage/effects (1.5x)",
                "§7Lower cooldowns (0.7x)",
                "",
                "§e§lClick to apply to this attribute",
                "§e§lShift+Click to apply to ALL")));

        gui.setItem(47, createItem(Material.IRON_INGOT, "§7§lPreset: Low Power", Arrays.asList(
                "§7Reduced effects (0.7x)",
                "§7Higher cooldowns (1.5x)",
                "",
                "§e§lClick to apply to this attribute",
                "§e§lShift+Click to apply to ALL")));

        gui.setItem(48, createItem(Material.NETHER_STAR, "§d§lPreset: Chaos", Arrays.asList(
                "§7Random extreme values!",
                "§7For fun/event game modes",
                "",
                "§e§lClick to apply to this attribute",
                "§e§lShift+Click to apply to ALL")));

        // Save and back
        gui.setItem(50, createItem(Material.WRITABLE_BOOK, "§a§lSave Changes", Arrays.asList(
                "§7Save all current values",
                "§7to config file")));

        gui.setItem(51, createItem(Material.BARRIER, "§c§lReset to Defaults", Arrays.asList(
                "§7Restore original values",
                "§7for this attribute",
                "§cCannot be undone!")));

        gui.setItem(53, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to attribute list")));

        admin.openInventory(gui);
    }

    /**
     * Cooldown adjuster GUI
     */
    public void openCooldownAdjuster(Player admin, AttributeType attribute, String abilityType) {
        Inventory gui = Bukkit.createInventory(null, 27, "§e§l" + abilityType + " Cooldown: " + attribute.getDisplayName());

        // Stable tier
        gui.setItem(10, createItem(Material.LIME_CONCRETE, "§a§lStable Tier", Arrays.asList(
                "§7Current: §e120s",
                "",
                "§e§lLeft: §7-10s",
                "§e§lRight: §7+10s",
                "§e§lShift+Left: §7-30s",
                "§e§lShift+Right: §7+30s")));

        // Warped tier
        gui.setItem(12, createItem(Material.PURPLE_CONCRETE, "§d§lWarped Tier", Arrays.asList(
                "§7Current: §e90s",
                "",
                "§e§lLeft: §7-10s",
                "§e§lRight: §7+10s",
                "§e§lShift+Left: §7-30s",
                "§e§lShift+Right: §7+30s")));

        // Extreme tier
        gui.setItem(14, createItem(Material.RED_CONCRETE, "§c§lExtreme Tier", Arrays.asList(
                "§7Current: §e60s",
                "",
                "§e§lLeft: §7-10s",
                "§e§lRight: §7+10s",
                "§e§lShift+Left: §7-30s",
                "§e§lShift+Right: §7+30s")));

        // Apply to all
        gui.setItem(16, createItem(Material.COMMAND_BLOCK, "§b§lApply to All Attributes", Arrays.asList(
                "§7Set these cooldowns for",
                "§7all attributes at once")));

        // Back
        gui.setItem(22, createItem(Material.BARRIER, "§7§lBack", Arrays.asList("§7Return to attribute editor")));

        admin.openInventory(gui);
    }

    /**
     * Player management menu
     */
    public void openPlayerManagement(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§e§lPlayer Management");

        int slot = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;

            PlayerData data = plugin.getPlayerData(player.getUniqueId());
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(player);

            List<String> lore = new ArrayList<>();
            if (data != null && data.getAttribute() != null) {
                lore.add("§7Attribute: " + data.getTier().getColor() + data.getAttribute().getDisplayName());
                lore.add("§7Tier: " + data.getTier().getColor() + data.getTier().name());
                lore.add("§7Level: §e" + data.getLevel() + "§7/§e5");
                lore.add("§7Kills: §a" + data.getKills());
                lore.add("§7Deaths: §c" + data.getDeaths());
                lore.add("");
                lore.add("§e§lLeft Click §7to manage");
            } else {
                lore.add("§7No attribute assigned");
                lore.add("");
                lore.add("§e§lLeft Click §7to assign");
            }

            meta.setDisplayName("§6" + player.getName());
            meta.setLore(lore);
            skull.setItemMeta(meta);

            gui.setItem(slot, skull);
            slot++;
        }

        // Back button
        gui.setItem(49, createItem(Material.BARRIER, "§c§lBack", Arrays.asList("§7Return to main menu")));

        admin.openInventory(gui);
    }

    /**
     * Individual player management
     */
    public void openPlayerOptions(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 45, "§e§l" + target.getName() + " Management");

        PlayerData data = plugin.getPlayerData(target.getUniqueId());

        // Current stats display
        gui.setItem(4, createPlayerStatsItem(target, data));

        // Assign Random
        gui.setItem(10, createItem(Material.ENDER_PEARL, "§a§lAssign Random Attribute", Arrays.asList(
                "§7Assigns a random attribute",
                "§7with random tier")));

        // Reroll
        gui.setItem(12, createItem(Material.BLAZE_POWDER, "§e§lReroll Attribute", Arrays.asList(
                "§7Reroll current attribute",
                "§7New random attribute + tier")));

        // Upgrade
        gui.setItem(14, createItem(Material.EXPERIENCE_BOTTLE, "§b§lUpgrade Level", Arrays.asList(
                "§7Increase level by 1",
                "§7Max level: 5")));

        // Downgrade
        gui.setItem(16, createItem(Material.GLASS_BOTTLE, "§7§lDowngrade Level", Arrays.asList(
                "§7Decrease level by 1",
                "§7Min level: 1")));

        // Choose Attribute
        gui.setItem(19, createItem(Material.ENCHANTED_BOOK, "§d§lChoose Specific Attribute", Arrays.asList(
                "§7Open attribute selector",
                "§7Pick exact attribute")));

        // Choose Tier
        gui.setItem(21, createItem(Material.NETHER_STAR, "§6§lChange Tier", Arrays.asList(
                "§7Change attribute tier",
                "§7Stable/Warped/Extreme")));

        // Reset
        gui.setItem(23, createItem(Material.BARRIER, "§c§lReset Player", Arrays.asList(
                "§7Reset to level 1",
                "§7Clear all cooldowns")));

        // Remove Attribute
        gui.setItem(25, createItem(Material.TNT, "§4§lRemove Attribute", Arrays.asList(
                "§7Completely remove attribute",
                "§cWarning: Cannot undo!")));

        // Cooldown Management
        gui.setItem(28, createItem(Material.CLOCK, "§e§lManage Cooldowns", Arrays.asList(
                "§7Clear or set cooldowns",
                "§7Support & Melee")));

        // Grant Dragon Egg
        gui.setItem(30, createItem(Material.DRAGON_EGG, "§6§l§kA§r §6§lGrant Dragon Egg §k§lA", Arrays.asList(
                "§7Give " + target.getName() + " the Dragon Egg",
                "§c§lEXTREME TIER ONLY",
                "§7The ultimate attribute")));

        // View Ability Details
        gui.setItem(32, createItem(Material.BOOK, "§a§lView Ability Details", Arrays.asList(
                "§7See full ability descriptions",
                "§7Support, Melee, Passive")));

        // Test Abilities
        gui.setItem(34, createItem(Material.REDSTONE, "§c§lTest Abilities", Arrays.asList(
                "§7Test particle effects",
                "§7Support/Melee/Passive")));

        // Back button
        gui.setItem(40, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to player list")));

        admin.openInventory(gui);
    }

    /**
     * Attribute selector menu
     */
    public void openAttributeSelector(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 54, "§d§lSelect Attribute for " + target.getName());

        AttributeType[] attributes = AttributeType.values();
        Material[] materials = {
                Material.IRON_SWORD,      // Melee
                Material.RED_DYE,          // Health
                Material.SHIELD,           // Defense
                Material.GOLD_INGOT,       // Wealth
                Material.FEATHER,          // Speed
                Material.ENDER_EYE,        // Control
                Material.BOW,              // Range
                Material.LIGHTNING_ROD,    // Pressure
                Material.CLOCK,            // Tempo
                Material.TNT,              // Disruption
                Material.SPYGLASS,         // Vision
                Material.TOTEM_OF_UNDYING, // Persistence
                Material.ANVIL,            // Anchor
                Material.ENDER_PEARL,      // Transfer
                Material.COMPARATOR,       // Risk
                Material.WITHER_SKELETON_SKULL, // Wither
                Material.SCULK_CATALYST,   // Warden
                Material.WIND_CHARGE,      // Breeze
                Material.DRAGON_EGG        // Dragon Egg
        };

        for (int i = 0; i < attributes.length; i++) {
            AttributeType attr = attributes[i];
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to assign this attribute");
            lore.add("§7Tier will be randomized");
            lore.add("");
            lore.add("§e§lLeft Click: §7Random Tier");
            lore.add("§a§lShift + Left: §7Force STABLE");
            lore.add("§d§lShift + Right: §7Force WARPED");
            lore.add("§c§lRight Click: §7Force EXTREME");

            gui.setItem(i, createItem(materials[i], attr.getIcon() + " §e" + attr.getDisplayName(), lore));
        }

        // Back button
        gui.setItem(49, createItem(Material.BARRIER, "§c§lBack", Arrays.asList("§7Return to player options")));

        admin.openInventory(gui);
    }

    /**
     * Tier selector menu
     */
    public void openTierSelector(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lSelect Tier for " + target.getName());

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer must have an attribute first!");
            return;
        }

        // Stable
        gui.setItem(11, createItem(Material.LIME_DYE, "§a§lSTABLE TIER", Arrays.asList(
                "§7Base effect, no drawback",
                "§7Cooldown: 120s",
                "§7Effect: 100%",
                "",
                "§7Current: " + data.getAttribute().getDisplayName())));

        // Warped
        gui.setItem(13, createItem(Material.PURPLE_DYE, "§d§lWARPED TIER", Arrays.asList(
                "§7Stronger + minor drawback",
                "§7Cooldown: 90s",
                "§7Effect: 130%",
                "",
                "§7Current: " + data.getAttribute().getDisplayName())));

        // Extreme
        gui.setItem(15, createItem(Material.RED_DYE, "§c§lEXTREME TIER", Arrays.asList(
                "§7Strongest + major drawback",
                "§7Cooldown: 60s",
                "§7Effect: 160%",
                "",
                "§7Current: " + data.getAttribute().getDisplayName())));

        // Back button
        gui.setItem(22, createItem(Material.BARRIER, "§7§lBack", Arrays.asList("§7Return to player options")));

        admin.openInventory(gui);
    }

    /**
     * Cooldown management menu
     */
    public void openCooldownManager(Player admin, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, "§e§lCooldown Manager: " + target.getName());

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null) return;

        // Support cooldown
        long supportCd = data.getRemainingCooldown("support") / 1000;
        gui.setItem(10, createItem(Material.EMERALD, "§a§lSupport Cooldown", Arrays.asList(
                "§7Current: " + (supportCd > 0 ? "§c" + supportCd + "s" : "§aReady"),
                "",
                "§e§lLeft Click: §7Clear cooldown",
                "§e§lRight Click: §7Set to 30s",
                "§e§lShift + Right: §7Set to 60s")));

        // Melee cooldown
        long meleeCd = data.getRemainingCooldown("melee") / 1000;
        gui.setItem(12, createItem(Material.IRON_SWORD, "§c§lMelee Cooldown", Arrays.asList(
                "§7Current: " + (meleeCd > 0 ? "§c" + meleeCd + "s" : "§aReady"),
                "",
                "§e§lLeft Click: §7Clear cooldown",
                "§e§lRight Click: §7Set to 30s",
                "§e§lShift + Right: §7Set to 60s")));

        // Clear all
        gui.setItem(14, createItem(Material.BUCKET, "§b§lClear All Cooldowns", Arrays.asList(
                "§7Remove all cooldowns",
                "§7Support + Melee")));

        // Back button
        gui.setItem(22, createItem(Material.BARRIER, "§7§lBack", Arrays.asList("§7Return to player options")));

        admin.openInventory(gui);
    }

    /**
     * Server statistics menu
     */
    public void openServerStats(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§a§lServer Statistics");

        // Count players by tier
        int stableCount = 0, warpedCount = 0, extremeCount = 0, noAttrCount = 0;
        Player topKiller = null, topLevel = null;
        int maxKills = 0, maxLevel = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerData(p.getUniqueId());
            if (data == null || data.getAttribute() == null) {
                noAttrCount++;
                continue;
            }

            switch (data.getTier()) {
                case STABLE: stableCount++; break;
                case WARPED: warpedCount++; break;
                case EXTREME: extremeCount++; break;
            }

            if (data.getKills() > maxKills) {
                maxKills = data.getKills();
                topKiller = p;
            }

            if (data.getLevel() > maxLevel) {
                maxLevel = data.getLevel();
                topLevel = p;
            }
        }

        // Tier distribution
        gui.setItem(10, createItem(Material.LIME_WOOL, "§a§lStable Tier Players", Arrays.asList(
                "§7Count: §e" + stableCount,
                "§7Percentage: §e" + (Bukkit.getOnlinePlayers().size() > 0 ?
                        (stableCount * 100 / Bukkit.getOnlinePlayers().size()) : 0) + "%")));

        gui.setItem(12, createItem(Material.PURPLE_WOOL, "§d§lWarped Tier Players", Arrays.asList(
                "§7Count: §e" + warpedCount,
                "§7Percentage: §e" + (Bukkit.getOnlinePlayers().size() > 0 ?
                        (warpedCount * 100 / Bukkit.getOnlinePlayers().size()) : 0) + "%")));

        gui.setItem(14, createItem(Material.RED_WOOL, "§c§lExtreme Tier Players", Arrays.asList(
                "§7Count: §e" + extremeCount,
                "§7Percentage: §e" + (Bukkit.getOnlinePlayers().size() > 0 ?
                        (extremeCount * 100 / Bukkit.getOnlinePlayers().size()) : 0) + "%")));

        gui.setItem(16, createItem(Material.GRAY_WOOL, "§7§lNo Attribute", Arrays.asList(
                "§7Count: §e" + noAttrCount)));

        // Top players
        if (topKiller != null) {
            gui.setItem(28, createItem(Material.DIAMOND_SWORD, "§6§lTop Killer", Arrays.asList(
                    "§7Player: §e" + topKiller.getName(),
                    "§7Kills: §a" + maxKills)));
        }

        if (topLevel != null) {
            gui.setItem(30, createItem(Material.EXPERIENCE_BOTTLE, "§b§lHighest Level", Arrays.asList(
                    "§7Player: §e" + topLevel.getName(),
                    "§7Level: §a" + maxLevel)));
        }

        // Attribute distribution
        gui.setItem(32, createItem(Material.BOOK, "§e§lAttribute Distribution", Arrays.asList(
                "§7Click to view detailed",
                "§7breakdown by attribute")));

        // Back button
        gui.setItem(49, createItem(Material.BARRIER, "§c§lBack", Arrays.asList("§7Return to main menu")));

        admin.openInventory(gui);
    }

    /**
     * Batch operations menu
     */
    public void openBatchOperations(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 27, "§c§lBatch Operations");

        // Assign all
        gui.setItem(10, createItem(Material.ENDER_PEARL, "§a§lAssign All Players", Arrays.asList(
                "§7Give random attributes to",
                "§7all players without one",
                "",
                "§c§lWarning: Affects all players!")));

        // Reroll all
        gui.setItem(12, createItem(Material.BLAZE_POWDER, "§e§lReroll All Attributes", Arrays.asList(
                "§7Reroll every player's attribute",
                "",
                "§c§lWarning: Cannot be undone!")));

        // Reset all
        gui.setItem(14, createItem(Material.BARRIER, "§c§lReset All Players", Arrays.asList(
                "§7Reset all players to level 1",
                "§7Clear all cooldowns",
                "",
                "§c§lWarning: Major change!")));

        // Clear all cooldowns
        gui.setItem(16, createItem(Material.BUCKET, "§b§lClear All Cooldowns", Arrays.asList(
                "§7Remove cooldowns from",
                "§7all online players")));

        // Give everyone Dragon Egg
        gui.setItem(20, createItem(Material.DRAGON_EGG, "§6§l§kA§r §4§lEVERYONE DRAGON EGG §6§l§kA", Arrays.asList(
                "§7Give ALL players Dragon Egg",
                "§7Extreme tier only",
                "",
                "§c§l§nEXTREME CAUTION!")));

        // Remove all attributes
        gui.setItem(22, createItem(Material.TNT, "§4§lRemove All Attributes", Arrays.asList(
                "§7Remove attributes from all",
                "",
                "§c§l§nDANGEROUS OPERATION!")));

        // Back button
        gui.setItem(24, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to main menu")));

        admin.openInventory(gui);
    }

    /**
     * Plugin settings menu
     */
    public void openSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§b§lPlugin Settings");

        AttributeSettings settings = plugin.getAttributeSettings();

        // Row 1: Gameplay toggles
        gui.setItem(0, createItem(Material.COMPARATOR, "§6§lGameplay Settings", Arrays.asList(
                "§7Toggle various gameplay features")));

        gui.setItem(2, createItem(
                plugin.isParticleEffectsEnabled() ? Material.FIREWORK_ROCKET : Material.GUNPOWDER,
                "§e§lParticle Effects",
                Arrays.asList(
                        "§7Toggle particle visibility",
                        "",
                        "§7Current: " + (plugin.isParticleEffectsEnabled() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(3, createItem(
                plugin.isLevelLossOnDeath() ? Material.SKELETON_SKULL : Material.PLAYER_HEAD,
                "§e§lLevel Loss on Death",
                Arrays.asList(
                        "§7Lose a level when you die",
                        "",
                        "§7Current: " + (plugin.isLevelLossOnDeath() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(4, createItem(
                plugin.isLevelGainOnKill() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
                "§e§lLevel Gain on Kill",
                Arrays.asList(
                        "§7Gain a level when you kill a player",
                        "",
                        "§7Current: " + (plugin.isLevelGainOnKill() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(5, createItem(
                plugin.isAutoAssignEnabled() ? Material.ENDER_PEARL : Material.ENDER_EYE,
                "§e§lAuto-Assign on Join",
                Arrays.asList(
                        "§7Automatically assign attributes to new players",
                        "",
                        "§7Current: " + (plugin.isAutoAssignEnabled() ? "§aEnabled" : "§cDisabled"),
                        "§7Delay: §e" + plugin.getAutoAssignDelaySeconds() + "s",
                        "",
                        "§eLeft Click: Toggle",
                        "§eRight Click: +5s delay",
                        "§eShift+Right: -5s delay")));

        // Row 2: Global multipliers
        gui.setItem(9, createItem(Material.GOLDEN_APPLE, "§6§lGlobal Multipliers", Arrays.asList(
                "§7Adjust global game balance")));

        gui.setItem(11, createItem(Material.CLOCK, "§e§lGlobal Cooldown Multiplier", Arrays.asList(
                "§7Multiplier for ALL ability cooldowns",
                "",
                "§7Current: §a" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()),
                "",
                "§eLeft Click: §7-0.1x",
                "§eRight Click: §7+0.1x",
                "§eShift+Click: §7Reset to 1.0x")));

        gui.setItem(13, createItem(Material.DIAMOND_SWORD, "§c§lGlobal Damage Multiplier", Arrays.asList(
                "§7Multiplier for ALL ability damage",
                "",
                "§7Current: §a" + String.format("%.1fx", settings.getGlobalDamageMultiplier()),
                "",
                "§eLeft Click: §7-0.1x",
                "§eRight Click: §7+0.1x",
                "§eShift+Click: §7Reset to 1.0x")));

        gui.setItem(15, createItem(Material.EXPERIENCE_BOTTLE, "§b§lLevel Scaling", Arrays.asList(
                "§7Bonus effect % per level",
                "",
                "§7Current: §a+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level",
                "",
                "§eLeft Click: §7-1%",
                "§eRight Click: §7+1%",
                "§eShift+Click: §7Reset to 10%")));

        // Row 3: Tier settings
        gui.setItem(18, createItem(Material.NETHER_STAR, "§6§lTier Settings", Arrays.asList(
                "§7Adjust tier cooldowns and effects")));

        gui.setItem(20, createItem(Material.LIME_DYE, "§a§lStable Tier", Arrays.asList(
                "§7Effect: §e" + (int)(settings.getStableMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getStableCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getStableDrawback() * 100) + "%",
                "",
                "§eLeft Click: §7-10s cooldown",
                "§eRight Click: §7+10s cooldown",
                "§eShift+Left: §7-10% effect",
                "§eShift+Right: §7+10% effect")));

        gui.setItem(22, createItem(Material.PURPLE_DYE, "§d§lWarped Tier", Arrays.asList(
                "§7Effect: §e" + (int)(settings.getWarpedMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getWarpedCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getWarpedDrawback() * 100) + "%",
                "",
                "§eLeft Click: §7-10s cooldown",
                "§eRight Click: §7+10s cooldown",
                "§eShift+Left: §7-10% effect",
                "§eShift+Right: §7+10% effect")));

        gui.setItem(24, createItem(Material.RED_DYE, "§c§lExtreme Tier", Arrays.asList(
                "§7Effect: §e" + (int)(settings.getExtremeMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getExtremeCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getExtremeDrawback() * 100) + "%",
                "",
                "§eLeft Click: §7-10s cooldown",
                "§eRight Click: §7+10s cooldown",
                "§eShift+Left: §7-10% effect",
                "§eShift+Right: §7+10% effect")));

        // Row 4: Presets
        gui.setItem(27, createItem(Material.COMMAND_BLOCK, "§6§lQuick Presets", Arrays.asList(
                "§7Apply preset configurations")));

        gui.setItem(29, createItem(Material.PAPER, "§e§lBalanced Preset", Arrays.asList(
                "§7Standard competitive values",
                "§7All multipliers set to 1.0x",
                "",
                "§eClick to apply to all attributes")));

        gui.setItem(31, createItem(Material.REDSTONE, "§c§lHigh Power Preset", Arrays.asList(
                "§7Increased damage/effects (1.5x)",
                "§7Lower cooldowns (0.7x)",
                "",
                "§eClick to apply to all attributes")));

        gui.setItem(33, createItem(Material.IRON_INGOT, "§7§lLow Power Preset", Arrays.asList(
                "§7Reduced effects (0.7x)",
                "§7Higher cooldowns (1.5x)",
                "",
                "§eClick to apply to all attributes")));

        gui.setItem(35, createItem(Material.NETHER_STAR, "§d§lChaos Preset", Arrays.asList(
                "§7Random extreme values!",
                "§7For fun/event game modes",
                "",
                "§eClick to apply to all attributes")));

        // Row 5: Actions
        gui.setItem(45, createItem(Material.WRITABLE_BOOK, "§a§lSave All Settings", Arrays.asList(
                "§7Save current settings to config",
                "§7Persists across server restarts")));

        gui.setItem(47, createItem(Material.TNT, "§c§lReset to Defaults", Arrays.asList(
                "§7Reset ALL settings to defaults",
                "§c§lCannot be undone!")));

        // Back button
        gui.setItem(49, createItem(Material.BARRIER, "§c§lBack", Arrays.asList("§7Return to main menu")));

        admin.openInventory(gui);
    }

    /**
     * Ability details viewer
     */
    public void openAbilityDetails(Player admin, Player target) {
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            admin.sendMessage("§cPlayer doesn't have an attribute!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, "§a§l" + data.getAttribute().getDisplayName() + " Details");

        String[] supportDesc = AbilityDescriptions.getDescription(data.getAttribute(), "support");
        String[] meleeDesc = AbilityDescriptions.getDescription(data.getAttribute(), "melee");
        String[] passiveDesc = AbilityDescriptions.getDescription(data.getAttribute(), "passive");

        List<String> supportLore = new ArrayList<>(Arrays.asList(supportDesc));
        supportLore.add("");
        supportLore.add("§7Cooldown: §e" + data.getTier().getCooldownSeconds() + "s");
        supportLore.add("§7Effect: §e" + (int)(data.getTotalMultiplier() * 100) + "%");

        List<String> meleeLore = new ArrayList<>(Arrays.asList(meleeDesc));
        meleeLore.add("");
        meleeLore.add("§7Cooldown: §e" + data.getTier().getCooldownSeconds() + "s");
        meleeLore.add("§7Effect: §e" + (int)(data.getTotalMultiplier() * 100) + "%");

        List<String> passiveLore = new ArrayList<>(Arrays.asList(passiveDesc));
        passiveLore.add("");
        passiveLore.add("§7Always active");
        passiveLore.add("§7Effect: §e" + (int)(data.getTotalMultiplier() * 100) + "%");

        // Support ability
        gui.setItem(11, createItem(Material.EMERALD, "§a§lSupport Ability", supportLore));

        // Melee ability
        gui.setItem(13, createItem(Material.IRON_SWORD, "§c§lMelee Ability", meleeLore));

        // Passive ability
        gui.setItem(15, createItem(Material.BOOK, "§b§lPassive Ability", passiveLore));

        // Back button
        gui.setItem(22, createItem(Material.BARRIER, "§7§lBack", Arrays.asList("§7Return to player options")));

        admin.openInventory(gui);
    }

    /**
     * Attribute info browser (public access)
     */
    public void openAttributeInfo(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§d§lAttribute Encyclopedia");

        AttributeType[] attributes = AttributeType.values();
        Material[] materials = {
                Material.IRON_SWORD,      // Melee
                Material.RED_DYE,          // Health
                Material.SHIELD,           // Defense
                Material.GOLD_INGOT,       // Wealth
                Material.FEATHER,          // Speed
                Material.ENDER_EYE,        // Control
                Material.BOW,              // Range
                Material.LIGHTNING_ROD,    // Pressure
                Material.CLOCK,            // Tempo
                Material.TNT,              // Disruption
                Material.SPYGLASS,         // Vision
                Material.TOTEM_OF_UNDYING, // Persistence
                Material.ANVIL,            // Anchor
                Material.ENDER_PEARL,      // Transfer
                Material.COMPARATOR,       // Risk
                Material.WITHER_SKELETON_SKULL, // Wither
                Material.SCULK_CATALYST,   // Warden
                Material.WIND_CHARGE,      // Breeze
                Material.DRAGON_EGG        // Dragon Egg
        };

        for (int i = 0; i < attributes.length; i++) {
            AttributeType attr = attributes[i];
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to view detailed info");
            lore.add("");
            lore.add("§e§lSupport §7| §e§lMelee §7| §e§lPassive");

            gui.setItem(i, createItem(materials[i], attr.getIcon() + " §e§l" + attr.getDisplayName(), lore));
        }

        // Tier info
        gui.setItem(45, createItem(Material.LIME_DYE, "§a§lStable Tier", Arrays.asList(
                "§7Probability: §a50%",
                "§7Effect: §e100%",
                "§7Cooldown: §e120s",
                "§7No drawbacks")));

        gui.setItem(46, createItem(Material.PURPLE_DYE, "§d§lWarped Tier", Arrays.asList(
                "§7Probability: §d35%",
                "§7Effect: §e130%",
                "§7Cooldown: §e90s",
                "§7Minor drawbacks")));

        gui.setItem(47, createItem(Material.RED_DYE, "§c§lExtreme Tier", Arrays.asList(
                "§7Probability: §c15%",
                "§7Effect: §e160%",
                "§7Cooldown: §e60s",
                "§c§lMajor drawbacks!")));

        // Level info
        gui.setItem(49, createItem(Material.EXPERIENCE_BOTTLE, "§b§lLevel System", Arrays.asList(
                "§7Levels: §e1-5",
                "§7Gain: §aKill players",
                "§7Lose: §cDie",
                "",
                "§7Each level adds §e+10% §7effect")));

        player.openInventory(gui);
    }

    /**
     * Detailed attribute info (when clicking an attribute)
     */
    public void openDetailedAttributeInfo(Player player, AttributeType attribute) {
        Inventory gui = Bukkit.createInventory(null, 27, attribute.getIcon() + " §e§l" + attribute.getDisplayName());

        String[] supportDesc = AbilityDescriptions.getDescription(attribute, "support");
        String[] meleeDesc = AbilityDescriptions.getDescription(attribute, "melee");
        String[] passiveDesc = AbilityDescriptions.getDescription(attribute, "passive");

        List<String> supportLore = new ArrayList<>(Arrays.asList(supportDesc));
        supportLore.add("");
        supportLore.add("§7Type: §aTeam Support");
        supportLore.add("§7Cooldown: §e60-120s");
        supportLore.add("§7Range: §e10 blocks");

        List<String> meleeLore = new ArrayList<>(Arrays.asList(meleeDesc));
        meleeLore.add("");
        meleeLore.add("§7Type: §cCombat Effect");
        meleeLore.add("§7Cooldown: §e60-120s");
        meleeLore.add("§7Trigger: §eNext hit");

        List<String> passiveLore = new ArrayList<>(Arrays.asList(passiveDesc));
        passiveLore.add("");
        passiveLore.add("§7Type: §bPassive");
        passiveLore.add("§7Cooldown: §aNone");
        passiveLore.add("§7Active: §aAlways");

        // Support ability
        gui.setItem(11, createItem(Material.EMERALD, "§a§lSupport Ability", supportLore));

        // Melee ability
        gui.setItem(13, createItem(Material.IRON_SWORD, "§c§lMelee Ability", meleeLore));

        // Passive ability
        gui.setItem(15, createItem(Material.BOOK, "§b§lPassive Ability", passiveLore));

        // Tier scaling info
        gui.setItem(22, createItem(Material.NETHER_STAR, "§6§lTier Scaling", Arrays.asList(
                "§a§lStable: §7100% effect",
                "§d§lWarped: §7130% effect",
                "§c§lExtreme: §7160% effect",
                "",
                "§7Higher tiers = stronger but",
                "§7with increased drawbacks!")));

        // Back button
        gui.setItem(18, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to attribute list")));

        player.openInventory(gui);
    }

    /**
     * Custom Items GUI - shows all custom items
     */
    public void openCustomItemsGUI(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lCustom Items");

        // Row 1: Special Items (crafting materials)
        gui.setItem(1, createClickableItem(WeaponAltar.createWeaponHandle(), "§eClick to receive"));
        gui.setItem(2, createClickableItem(WeaponAltar.createWardensHeart(), "§eClick to receive"));
        gui.setItem(3, createClickableItem(WeaponAltar.createWitherBone(), "§eClick to receive"));
        gui.setItem(4, createClickableItem(WeaponAltar.createBreezeHeart(), "§eClick to receive"));
        gui.setItem(5, createClickableItem(WeaponAltar.createDragonHeart(), "§eClick to receive"));
        gui.setItem(6, createClickableItem(OddsSMP.createUpgrader(), "§eClick to receive"));
        gui.setItem(7, createClickableItem(OddsSMP.createReroller(), "§eClick to receive"));

        // Label
        gui.setItem(0, createItem(Material.CHEST, "§e§lSpecial Items", Arrays.asList(
                "§7Crafting materials and",
                "§7utility items")));

        // Row 2-5: All 19 Attribute Weapons
        gui.setItem(9, createItem(Material.DIAMOND_SWORD, "§6§lAttribute Weapons", Arrays.asList(
                "§7All 19 unique weapons",
                "§7for each attribute")));

        AttributeWeapon[] weapons = AttributeWeapon.values();
        int slot = 10;
        for (int i = 0; i < weapons.length && slot < 45; i++) {
            AttributeWeapon weapon = weapons[i];
            ItemStack weaponItem = weapon.createItem();
            ItemMeta meta = weaponItem.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add("§eClick to receive");
            meta.setLore(lore);
            weaponItem.setItemMeta(meta);
            gui.setItem(slot, weaponItem);
            slot++;
            // Skip to next row after 8 items per row
            if ((slot - 10) % 8 == 0) {
                slot += 1; // Skip first slot of each row
            }
        }

        // Back button
        gui.setItem(49, createItem(Material.BARRIER, "§c§lClose", Arrays.asList("§7Close this menu")));

        admin.openInventory(gui);
    }

    /**
     * Create an item with click instruction added to lore
     */
    private ItemStack createClickableItem(ItemStack original, String instruction) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(instruction);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // Helper methods

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerStatsItem(Player player, PlayerData data) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);

        List<String> lore = new ArrayList<>();
        if (data != null && data.getAttribute() != null) {
            lore.add("§7Attribute: " + data.getTier().getColor() + data.getAttribute().getDisplayName());
            lore.add("§7Tier: " + data.getTier().getColor() + data.getTier().name());
            lore.add("§7Level: §e" + data.getLevel() + "§7/§e5");
            lore.add("§7Kills: §a" + data.getKills());
            lore.add("§7Deaths: §c" + data.getDeaths());
            lore.add("§7KD Ratio: §e" + (data.getDeaths() > 0 ?
                    String.format("%.2f", (double) data.getKills() / data.getDeaths()) : data.getKills()));
            lore.add("");
            lore.add("§7Support CD: " + (data.isOnCooldown("support") ?
                    "§c" + data.getRemainingCooldown("support") / 1000 + "s" : "§aReady"));
            lore.add("§7Melee CD: " + (data.isOnCooldown("melee") ?
                    "§c" + data.getRemainingCooldown("melee") / 1000 + "s" : "§aReady"));
        } else {
            lore.add("§c§lNo Attribute Assigned");
        }

        meta.setDisplayName("§6§l" + player.getName() + "'s Stats");
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }
}