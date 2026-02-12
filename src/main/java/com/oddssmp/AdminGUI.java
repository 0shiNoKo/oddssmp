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
     * Plugin settings menu - Main page
     */
    public void openSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§b§lPlugin Settings");

        // Navigation buttons for sub-menus
        gui.setItem(10, createItem(Material.COMPARATOR, "§6§lGameplay Settings", Arrays.asList(
                "§7Core gameplay toggles",
                "§7Level system, particles, etc.",
                "",
                "§eClick to configure")));

        gui.setItem(12, createItem(Material.DIAMOND_SWORD, "§c§lCombat Settings", Arrays.asList(
                "§7PvP and damage settings",
                "§7Combat tags, multipliers",
                "",
                "§eClick to configure")));

        gui.setItem(14, createItem(Material.DRAGON_HEAD, "§5§lBoss Settings", Arrays.asList(
                "§7Boss fight configuration",
                "§7Health, damage, drops",
                "",
                "§eClick to configure")));

        gui.setItem(16, createItem(Material.BELL, "§e§lBroadcast Settings", Arrays.asList(
                "§7Server announcements",
                "§7Level ups, kills, bosses",
                "",
                "§eClick to configure")));

        gui.setItem(28, createItem(Material.NETHER_STAR, "§d§lTier Settings", Arrays.asList(
                "§7Tier configuration",
                "§7Cooldowns, effects, drawbacks",
                "",
                "§eClick to configure")));

        gui.setItem(30, createItem(Material.GOLDEN_APPLE, "§a§lMultiplier Settings", Arrays.asList(
                "§7Global multipliers",
                "§7Cooldowns, damage, scaling",
                "",
                "§eClick to configure")));

        gui.setItem(32, createItem(Material.COMMAND_BLOCK, "§b§lQuick Presets", Arrays.asList(
                "§7Apply preset configurations",
                "§7Balanced, High Power, Chaos",
                "",
                "§eClick to configure")));

        gui.setItem(34, createItem(Material.SKULL_BANNER_PATTERN, "§8§lDeath Settings", Arrays.asList(
                "§7Death and respawn options",
                "§7Keep inventory, item drops",
                "",
                "§eClick to configure")));

        gui.setItem(38, createItem(Material.WRITABLE_BOOK, "§c§lCombat Log Settings", Arrays.asList(
                "§7Configure combat logging",
                "§7Damage, kills, abilities",
                "§7File logging, notifications",
                "",
                "§eClick to configure")));

        // Bottom row actions
        gui.setItem(45, createItem(Material.WRITABLE_BOOK, "§a§lSave All Settings", Arrays.asList(
                "§7Save current settings to config",
                "§7Persists across server restarts")));

        gui.setItem(47, createItem(Material.TNT, "§c§lReset to Defaults", Arrays.asList(
                "§7Reset ALL settings to defaults",
                "§c§lCannot be undone!")));

        gui.setItem(49, createItem(Material.BARRIER, "§c§lBack", Arrays.asList("§7Return to main menu")));

        admin.openInventory(gui);
    }

    /**
     * Gameplay Settings sub-menu
     */
    public void openGameplaySettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lGameplay Settings");

        // Row 1: Core toggles
        gui.setItem(10, createItem(
                plugin.isParticleEffectsEnabled() ? Material.FIREWORK_ROCKET : Material.GUNPOWDER,
                "§e§lParticle Effects",
                Arrays.asList(
                        "§7Show ability particle effects",
                        "",
                        "§7Current: " + (plugin.isParticleEffectsEnabled() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(11, createItem(
                plugin.isLevelLossOnDeath() ? Material.SKELETON_SKULL : Material.PLAYER_HEAD,
                "§e§lLevel Loss on Death",
                Arrays.asList(
                        "§7Lose levels when you die",
                        "",
                        "§7Current: " + (plugin.isLevelLossOnDeath() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(12, createItem(
                plugin.isLevelGainOnKill() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
                "§e§lLevel Gain on Kill",
                Arrays.asList(
                        "§7Gain levels when you kill",
                        "",
                        "§7Current: " + (plugin.isLevelGainOnKill() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(13, createItem(
                plugin.isAutoAssignEnabled() ? Material.ENDER_PEARL : Material.ENDER_EYE,
                "§e§lAuto-Assign on Join",
                Arrays.asList(
                        "§7Auto-assign attributes to new players",
                        "",
                        "§7Current: " + (plugin.isAutoAssignEnabled() ? "§aEnabled" : "§cDisabled"),
                        "§7Delay: §e" + plugin.getAutoAssignDelaySeconds() + "s",
                        "",
                        "§eLeft Click: Toggle",
                        "§eRight Click: +5s delay",
                        "§eShift+Right: -5s delay")));

        gui.setItem(14, createItem(
                plugin.isPvpOnlyAbilities() ? Material.IRON_SWORD : Material.STONE_SWORD,
                "§e§lPvP Only Abilities",
                Arrays.asList(
                        "§7Abilities only work against players",
                        "",
                        "§7Current: " + (plugin.isPvpOnlyAbilities() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(15, createItem(
                plugin.isFriendlyFire() ? Material.GOLDEN_SWORD : Material.SHIELD,
                "§e§lFriendly Fire",
                Arrays.asList(
                        "§7Abilities can affect teammates",
                        "",
                        "§7Current: " + (plugin.isFriendlyFire() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(16, createItem(
                plugin.isKillStreakBonuses() ? Material.BLAZE_POWDER : Material.GUNPOWDER,
                "§e§lKill Streak Bonuses",
                Arrays.asList(
                        "§7Bonus effects for kill streaks",
                        "",
                        "§7Current: " + (plugin.isKillStreakBonuses() ? "§aEnabled" : "§cDisabled"),
                        "§7Threshold: §e" + plugin.getKillStreakThreshold() + " kills",
                        "",
                        "§eLeft Click: Toggle",
                        "§eRight Click: +1 threshold",
                        "§eShift+Right: -1 threshold")));

        // Row 2: Level settings
        gui.setItem(19, createItem(Material.EXPERIENCE_BOTTLE, "§b§lMax Level", Arrays.asList(
                "§7Maximum player level",
                "",
                "§7Current: §e" + plugin.getMaxLevel(),
                "",
                "§eLeft Click: -1",
                "§eRight Click: +1")));

        gui.setItem(20, createItem(Material.REDSTONE, "§c§lLevels Lost on Death", Arrays.asList(
                "§7Levels lost when dying",
                "",
                "§7Current: §e" + plugin.getLevelsLostOnDeath(),
                "",
                "§eLeft Click: -1",
                "§eRight Click: +1")));

        gui.setItem(21, createItem(Material.EMERALD, "§a§lLevels Gained on Kill", Arrays.asList(
                "§7Levels gained per kill",
                "",
                "§7Current: §e" + plugin.getLevelsGainedOnKill(),
                "",
                "§eLeft Click: -1",
                "§eRight Click: +1")));

        // Row 3: Passive settings
        gui.setItem(28, createItem(Material.CLOCK, "§d§lPassive Tick Rate", Arrays.asList(
                "§7Seconds between passive effects",
                "",
                "§7Current: §e" + String.format("%.1f", plugin.getPassiveTickRate()) + "s",
                "",
                "§eLeft Click: -0.5s",
                "§eRight Click: +0.5s")));

        gui.setItem(29, createItem(Material.POTION, "§d§lPassive Effect Strength", Arrays.asList(
                "§7Multiplier for passive effects",
                "",
                "§7Current: §e" + String.format("%.1fx", plugin.getPassiveEffectStrength()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Combat Settings sub-menu
     */
    public void openCombatSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§c§lCombat Settings");

        AttributeSettings settings = plugin.getAttributeSettings();

        // Row 1: Damage multipliers
        gui.setItem(10, createItem(Material.DIAMOND_SWORD, "§c§lPvP Damage Multiplier", Arrays.asList(
                "§7Multiplier for all PvP damage",
                "",
                "§7Current: §e" + String.format("%.1fx", plugin.getPvpDamageMultiplier()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(12, createItem(Material.BLAZE_ROD, "§c§lAbility Damage Multiplier", Arrays.asList(
                "§7Multiplier for ability damage",
                "",
                "§7Current: §e" + String.format("%.1fx", plugin.getAbilityDamageMultiplier()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(14, createItem(Material.NETHERITE_SWORD, "§c§lGlobal Damage Multiplier", Arrays.asList(
                "§7Multiplier for ALL damage",
                "",
                "§7Current: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(16, createItem(Material.CLOCK, "§e§lGlobal Cooldown Multiplier", Arrays.asList(
                "§7Multiplier for ALL cooldowns",
                "",
                "§7Current: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x",
                "§eShift+Click: Reset to 1.0x")));

        // Row 2: Combat tag settings
        gui.setItem(28, createItem(
                plugin.isCombatTagEnabled() ? Material.IRON_SWORD : Material.WOODEN_SWORD,
                "§e§lCombat Tag",
                Arrays.asList(
                        "§7Tag players when in combat",
                        "",
                        "§7Current: " + (plugin.isCombatTagEnabled() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(30, createItem(Material.CLOCK, "§e§lCombat Tag Duration", Arrays.asList(
                "§7Seconds in combat after hit",
                "",
                "§7Current: §e" + plugin.getCombatTagDuration() + "s",
                "",
                "§eLeft Click: -5s",
                "§eRight Click: +5s")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Boss Settings sub-menu
     */
    public void openBossSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§5§lBoss Settings");

        // Row 1: Boss multipliers
        gui.setItem(11, createItem(Material.DRAGON_HEAD, "§c§lBoss Health Multiplier", Arrays.asList(
                "§7Multiplier for boss HP",
                "",
                "§7Current: §e" + String.format("%.1fx", plugin.getBossHealthMultiplier()),
                "",
                "§eLeft Click: -0.25x",
                "§eRight Click: +0.25x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(13, createItem(Material.NETHERITE_SWORD, "§c§lBoss Damage Multiplier", Arrays.asList(
                "§7Multiplier for boss damage",
                "",
                "§7Current: §e" + String.format("%.1fx", plugin.getBossDamageMultiplier()),
                "",
                "§eLeft Click: -0.25x",
                "§eRight Click: +0.25x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(15, createItem(Material.CHEST, "§a§lBoss Drop Rate Multiplier", Arrays.asList(
                "§7Multiplier for boss drops",
                "",
                "§7Current: §e" + String.format("%.1fx", plugin.getBossDropRateMultiplier()),
                "",
                "§eLeft Click: -0.25x",
                "§eRight Click: +0.25x",
                "§eShift+Click: Reset to 1.0x")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Broadcast Settings sub-menu
     */
    public void openBroadcastSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§e§lBroadcast Settings");

        // Row 1: Broadcast toggles
        gui.setItem(10, createItem(
                plugin.isBroadcastAttributeAssign() ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e§lAttribute Assignment",
                Arrays.asList(
                        "§7Announce new attribute assignments",
                        "",
                        "§7Current: " + (plugin.isBroadcastAttributeAssign() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(11, createItem(
                plugin.isBroadcastLevelUp() ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e§lLevel Up",
                Arrays.asList(
                        "§7Announce player level ups",
                        "",
                        "§7Current: " + (plugin.isBroadcastLevelUp() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(12, createItem(
                plugin.isBroadcastDragonEgg() ? Material.DRAGON_EGG : Material.GRAY_DYE,
                "§6§lDragon Egg Obtained",
                Arrays.asList(
                        "§7Announce Dragon Egg assignments",
                        "",
                        "§7Current: " + (plugin.isBroadcastDragonEgg() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(14, createItem(
                plugin.isBroadcastBossSpawn() ? Material.WITHER_SKELETON_SKULL : Material.GRAY_DYE,
                "§5§lBoss Spawn",
                Arrays.asList(
                        "§7Announce boss spawns",
                        "",
                        "§7Current: " + (plugin.isBroadcastBossSpawn() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(15, createItem(
                plugin.isBroadcastBossDefeat() ? Material.NETHER_STAR : Material.GRAY_DYE,
                "§a§lBoss Defeat",
                Arrays.asList(
                        "§7Announce boss defeats",
                        "",
                        "§7Current: " + (plugin.isBroadcastBossDefeat() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Tier Settings sub-menu
     */
    public void openTierSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§d§lTier Settings");

        AttributeSettings settings = plugin.getAttributeSettings();

        // Row 1: Stable Tier
        gui.setItem(10, createItem(Material.LIME_WOOL, "§a§lStable Tier", Arrays.asList(
                "§7Base tier - no drawbacks",
                "",
                "§7Effect: §e" + (int)(settings.getStableMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getStableCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getStableDrawback() * 100) + "%")));

        gui.setItem(11, createItem(Material.LIME_DYE, "§a§lStable Effect", Arrays.asList(
                "§7Effect multiplier",
                "",
                "§7Current: §e" + (int)(settings.getStableMultiplier() * 100) + "%",
                "",
                "§eLeft Click: -10%",
                "§eRight Click: +10%")));

        gui.setItem(12, createItem(Material.CLOCK, "§a§lStable Cooldown", Arrays.asList(
                "§7Base cooldown in seconds",
                "",
                "§7Current: §e" + settings.getStableCooldown() + "s",
                "",
                "§eLeft Click: -10s",
                "§eRight Click: +10s")));

        // Row 2: Warped Tier
        gui.setItem(19, createItem(Material.PURPLE_WOOL, "§d§lWarped Tier", Arrays.asList(
                "§7Enhanced tier - minor drawbacks",
                "",
                "§7Effect: §e" + (int)(settings.getWarpedMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getWarpedCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getWarpedDrawback() * 100) + "%")));

        gui.setItem(20, createItem(Material.PURPLE_DYE, "§d§lWarped Effect", Arrays.asList(
                "§7Effect multiplier",
                "",
                "§7Current: §e" + (int)(settings.getWarpedMultiplier() * 100) + "%",
                "",
                "§eLeft Click: -10%",
                "§eRight Click: +10%")));

        gui.setItem(21, createItem(Material.CLOCK, "§d§lWarped Cooldown", Arrays.asList(
                "§7Base cooldown in seconds",
                "",
                "§7Current: §e" + settings.getWarpedCooldown() + "s",
                "",
                "§eLeft Click: -10s",
                "§eRight Click: +10s")));

        // Row 3: Extreme Tier
        gui.setItem(28, createItem(Material.RED_WOOL, "§c§lExtreme Tier", Arrays.asList(
                "§7Maximum tier - major drawbacks",
                "",
                "§7Effect: §e" + (int)(settings.getExtremeMultiplier() * 100) + "%",
                "§7Cooldown: §e" + settings.getExtremeCooldown() + "s",
                "§7Drawback: §e" + (int)(settings.getExtremeDrawback() * 100) + "%")));

        gui.setItem(29, createItem(Material.RED_DYE, "§c§lExtreme Effect", Arrays.asList(
                "§7Effect multiplier",
                "",
                "§7Current: §e" + (int)(settings.getExtremeMultiplier() * 100) + "%",
                "",
                "§eLeft Click: -10%",
                "§eRight Click: +10%")));

        gui.setItem(30, createItem(Material.CLOCK, "§c§lExtreme Cooldown", Arrays.asList(
                "§7Base cooldown in seconds",
                "",
                "§7Current: §e" + settings.getExtremeCooldown() + "s",
                "",
                "§eLeft Click: -10s",
                "§eRight Click: +10s")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Multiplier Settings sub-menu
     */
    public void openMultiplierSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§a§lMultiplier Settings");

        AttributeSettings settings = plugin.getAttributeSettings();

        gui.setItem(11, createItem(Material.CLOCK, "§e§lGlobal Cooldown", Arrays.asList(
                "§7Multiplier for ALL cooldowns",
                "",
                "§7Current: §e" + String.format("%.1fx", settings.getGlobalCooldownMultiplier()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(13, createItem(Material.DIAMOND_SWORD, "§c§lGlobal Damage", Arrays.asList(
                "§7Multiplier for ALL damage",
                "",
                "§7Current: §e" + String.format("%.1fx", settings.getGlobalDamageMultiplier()),
                "",
                "§eLeft Click: -0.1x",
                "§eRight Click: +0.1x",
                "§eShift+Click: Reset to 1.0x")));

        gui.setItem(15, createItem(Material.EXPERIENCE_BOTTLE, "§b§lLevel Scaling", Arrays.asList(
                "§7Bonus effect % per level",
                "",
                "§7Current: §e+" + String.format("%.0f", settings.getLevelScalingPercent()) + "% per level",
                "",
                "§eLeft Click: -1%",
                "§eRight Click: +1%",
                "§eShift+Click: Reset to 10%")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Preset Settings sub-menu
     */
    public void openPresetSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§b§lQuick Presets");

        gui.setItem(11, createItem(Material.PAPER, "§e§lBalanced Preset", Arrays.asList(
                "§7Standard competitive values",
                "§7All multipliers set to 1.0x",
                "§7Default cooldowns",
                "",
                "§eClick to apply to all")));

        gui.setItem(13, createItem(Material.REDSTONE, "§c§lHigh Power Preset", Arrays.asList(
                "§7Increased effects (1.5x)",
                "§7Lower cooldowns (0.7x)",
                "§7More intense combat",
                "",
                "§eClick to apply to all")));

        gui.setItem(15, createItem(Material.IRON_INGOT, "§7§lLow Power Preset", Arrays.asList(
                "§7Reduced effects (0.7x)",
                "§7Higher cooldowns (1.5x)",
                "§7More strategic gameplay",
                "",
                "§eClick to apply to all")));

        gui.setItem(29, createItem(Material.NETHER_STAR, "§d§lChaos Preset", Arrays.asList(
                "§7Random extreme values!",
                "§7Unpredictable gameplay",
                "§7For fun/event modes",
                "",
                "§eClick to apply to all")));

        gui.setItem(31, createItem(Material.GOLDEN_APPLE, "§6§lOP Preset", Arrays.asList(
                "§7Maximum effects (2.0x)",
                "§7Minimal cooldowns (0.5x)",
                "§7Overpowered abilities",
                "",
                "§eClick to apply to all")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Death Settings sub-menu
     */
    public void openDeathSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8§lDeath Settings");

        gui.setItem(11, createItem(
                plugin.isKeepInventoryOnDeath() ? Material.CHEST : Material.ENDER_CHEST,
                "§e§lKeep Inventory on Death",
                Arrays.asList(
                        "§7Players keep items on death",
                        "",
                        "§7Current: " + (plugin.isKeepInventoryOnDeath() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(13, createItem(
                plugin.isDropAttributeItemsOnDeath() ? Material.HOPPER : Material.BARRIER,
                "§e§lDrop Attribute Items",
                Arrays.asList(
                        "§7Drop special items on death",
                        "§7(Hearts, Bones, etc.)",
                        "",
                        "§7Current: " + (plugin.isDropAttributeItemsOnDeath() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(15, createItem(Material.EXPERIENCE_BOTTLE, "§c§lLevels Lost on Death", Arrays.asList(
                "§7How many levels lost on death",
                "",
                "§7Current: §e" + plugin.getLevelsLostOnDeath(),
                "",
                "§eLeft Click: -1",
                "§eRight Click: +1")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

        admin.openInventory(gui);
    }

    /**
     * Combat Log Settings sub-menu
     */
    public void openCombatLogSettings(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 54, "§c§lCombat Log Settings");

        CombatLogger logger = plugin.getCombatLogger();

        // Row 1: Main toggles
        gui.setItem(10, createItem(
                logger.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e§lCombat Logging",
                Arrays.asList(
                        "§7Master toggle for combat logging",
                        "",
                        "§7Current: " + (logger.isEnabled() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(11, createItem(
                logger.isLogToFile() ? Material.WRITABLE_BOOK : Material.BOOK,
                "§e§lLog to File",
                Arrays.asList(
                        "§7Save combat logs to file",
                        "§7Location: /plugins/OddsSMP/combat-logs/",
                        "",
                        "§7Current: " + (logger.isLogToFile() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(12, createItem(
                logger.isLogToConsole() ? Material.COMMAND_BLOCK : Material.CHAIN_COMMAND_BLOCK,
                "§e§lLog to Console",
                Arrays.asList(
                        "§7Print combat logs to server console",
                        "",
                        "§7Current: " + (logger.isLogToConsole() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(13, createItem(
                logger.isShowToPlayers() ? Material.PLAYER_HEAD : Material.SKELETON_SKULL,
                "§e§lShow to Players",
                Arrays.asList(
                        "§7Send combat logs to players in chat",
                        "",
                        "§7Current: " + (logger.isShowToPlayers() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(14, createItem(
                logger.isCompactMode() ? Material.PAPER : Material.MAP,
                "§e§lCompact Mode",
                Arrays.asList(
                        "§7Use shorter log messages",
                        "",
                        "§7Current: " + (logger.isCompactMode() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(15, createItem(
                logger.isShowDamageNumbers() ? Material.NAME_TAG : Material.STRING,
                "§e§lShow Damage Numbers",
                Arrays.asList(
                        "§7Display raw damage values",
                        "",
                        "§7Current: " + (logger.isShowDamageNumbers() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(16, createItem(
                logger.isShowHealthBars() ? Material.RED_DYE : Material.GRAY_DYE,
                "§e§lShow Health Bars",
                Arrays.asList(
                        "§7Display health after damage",
                        "",
                        "§7Current: " + (logger.isShowHealthBars() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        // Row 2: Event type toggles
        gui.setItem(19, createItem(
                logger.isLogDamageEvents() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
                "§c§lLog Damage Events",
                Arrays.asList(
                        "§7Log player damage dealt/taken",
                        "",
                        "§7Current: " + (logger.isLogDamageEvents() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(20, createItem(
                logger.isLogAbilityEvents() ? Material.ENCHANTED_BOOK : Material.BOOK,
                "§d§lLog Ability Events",
                Arrays.asList(
                        "§7Log ability usage",
                        "",
                        "§7Current: " + (logger.isLogAbilityEvents() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(21, createItem(
                logger.isLogKillEvents() ? Material.WITHER_SKELETON_SKULL : Material.SKELETON_SKULL,
                "§4§lLog Kill Events",
                Arrays.asList(
                        "§7Log player kills and deaths",
                        "",
                        "§7Current: " + (logger.isLogKillEvents() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(22, createItem(
                logger.isLogHealingEvents() ? Material.GOLDEN_APPLE : Material.APPLE,
                "§a§lLog Healing Events",
                Arrays.asList(
                        "§7Log healing received",
                        "",
                        "§7Current: " + (logger.isLogHealingEvents() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(23, createItem(
                logger.isLogCombatTagEvents() ? Material.IRON_SWORD : Material.STONE_SWORD,
                "§e§lLog Combat Tag Events",
                Arrays.asList(
                        "§7Log combat tag entering/leaving",
                        "",
                        "§7Current: " + (logger.isLogCombatTagEvents() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(24, createItem(
                logger.isLogCriticalHits() ? Material.GOLDEN_SWORD : Material.IRON_SWORD,
                "§6§lLog Critical Hits",
                Arrays.asList(
                        "§7Log critical hit events",
                        "",
                        "§7Current: " + (logger.isLogCriticalHits() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(25, createItem(
                logger.isLogBlockedDamage() ? Material.SHIELD : Material.IRON_NUGGET,
                "§b§lLog Blocked Damage",
                Arrays.asList(
                        "§7Log damage blocked by shields",
                        "",
                        "§7Current: " + (logger.isLogBlockedDamage() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        // Row 3: Additional event types
        gui.setItem(28, createItem(
                logger.isLogEnvironmentalDamage() ? Material.CAMPFIRE : Material.SOUL_CAMPFIRE,
                "§7§lLog Environmental Damage",
                Arrays.asList(
                        "§7Log damage from environment",
                        "§7(fire, fall, drowning, etc.)",
                        "",
                        "§7Current: " + (logger.isLogEnvironmentalDamage() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        gui.setItem(29, createItem(
                logger.isLogMobDamage() ? Material.ZOMBIE_HEAD : Material.PLAYER_HEAD,
                "§8§lLog Mob Damage",
                Arrays.asList(
                        "§7Log damage from mobs",
                        "",
                        "§7Current: " + (logger.isLogMobDamage() ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")));

        // Row 3: Numeric settings
        gui.setItem(32, createItem(Material.COMPARATOR, "§e§lDamage Threshold", Arrays.asList(
                "§7Minimum damage to log",
                "",
                "§7Current: §e" + String.format("%.1f", logger.getMinimumDamageThreshold()),
                "",
                "§eLeft Click: -0.5",
                "§eRight Click: +0.5",
                "§eShift+Click: Reset to 0")));

        gui.setItem(33, createItem(Material.CHEST, "§e§lMax Log History", Arrays.asList(
                "§7Maximum entries in global log",
                "",
                "§7Current: §e" + logger.getMaxLogHistory(),
                "",
                "§eLeft Click: -10",
                "§eRight Click: +10")));

        gui.setItem(34, createItem(Material.ENDER_CHEST, "§e§lMax Player Log History", Arrays.asList(
                "§7Maximum entries per player",
                "",
                "§7Current: §e" + logger.getMaxPlayerLogHistory(),
                "",
                "§eLeft Click: -10",
                "§eRight Click: +10")));

        // Row 4: Actions
        gui.setItem(37, createItem(Material.BUCKET, "§c§lClear Global Log", Arrays.asList(
                "§7Clear all global combat logs",
                "",
                "§eClick to clear")));

        gui.setItem(39, createItem(Material.TNT, "§c§lReset to Defaults", Arrays.asList(
                "§7Reset all combat log settings",
                "§7to default values",
                "",
                "§eClick to reset")));

        gui.setItem(41, createItem(Material.LIME_DYE, "§a§lEnable All Events", Arrays.asList(
                "§7Enable logging for all event types",
                "",
                "§eClick to enable all")));

        gui.setItem(43, createItem(Material.RED_DYE, "§c§lDisable All Events", Arrays.asList(
                "§7Disable logging for all event types",
                "",
                "§eClick to disable all")));

        // Back button
        gui.setItem(49, createItem(Material.ARROW, "§7§lBack", Arrays.asList("§7Return to settings menu")));

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