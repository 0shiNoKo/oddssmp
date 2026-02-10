package com.oddssmp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages configurable settings for all attributes.
 * Stores cooldowns, damage multipliers, durations, and other values.
 */
public class AttributeSettings {

    private final OddsSMP plugin;
    private File configFile;
    private FileConfiguration config;

    // Global multipliers
    private double globalCooldownMultiplier = 1.0;
    private double globalDamageMultiplier = 1.0;
    private double levelScalingPercent = 10.0; // +10% per level

    // Per-attribute settings
    private final Map<AttributeType, AttributeConfig> attributeConfigs = new HashMap<>();

    // Tier cooldowns (in seconds)
    private int stableCooldown = 120;
    private int warpedCooldown = 90;
    private int extremeCooldown = 60;

    // Tier effect multipliers
    private double stableMultiplier = 1.0;
    private double warpedMultiplier = 1.3;
    private double extremeMultiplier = 1.6;

    // Tier drawback multipliers
    private double stableDrawback = 0.0;
    private double warpedDrawback = 0.15;
    private double extremeDrawback = 0.35;

    public AttributeSettings(OddsSMP plugin) {
        this.plugin = plugin;
        initializeDefaults();
        loadConfig();
    }

    /**
     * Initialize default values for all attributes
     */
    private void initializeDefaults() {
        for (AttributeType type : AttributeType.values()) {
            attributeConfigs.put(type, new AttributeConfig(type));
        }
    }

    /**
     * Load settings from config file
     */
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "attribute-settings.yml");

        if (!configFile.exists()) {
            saveConfig(); // Create default
            return;
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Load global settings
        globalCooldownMultiplier = config.getDouble("global.cooldown-multiplier", 1.0);
        globalDamageMultiplier = config.getDouble("global.damage-multiplier", 1.0);
        levelScalingPercent = config.getDouble("global.level-scaling-percent", 10.0);

        // Load tier settings
        stableCooldown = config.getInt("tiers.stable.cooldown", 120);
        warpedCooldown = config.getInt("tiers.warped.cooldown", 90);
        extremeCooldown = config.getInt("tiers.extreme.cooldown", 60);

        stableMultiplier = config.getDouble("tiers.stable.multiplier", 1.0);
        warpedMultiplier = config.getDouble("tiers.warped.multiplier", 1.3);
        extremeMultiplier = config.getDouble("tiers.extreme.multiplier", 1.6);

        stableDrawback = config.getDouble("tiers.stable.drawback", 0.0);
        warpedDrawback = config.getDouble("tiers.warped.drawback", 0.15);
        extremeDrawback = config.getDouble("tiers.extreme.drawback", 0.35);

        // Load per-attribute settings
        for (AttributeType type : AttributeType.values()) {
            String path = "attributes." + type.name().toLowerCase();
            AttributeConfig attrConfig = attributeConfigs.get(type);

            if (config.contains(path)) {
                attrConfig.supportCooldownModifier = config.getDouble(path + ".support-cooldown-modifier", 1.0);
                attrConfig.meleeCooldownModifier = config.getDouble(path + ".melee-cooldown-modifier", 1.0);
                attrConfig.supportDuration = config.getInt(path + ".support-duration", 10);
                attrConfig.supportRange = config.getDouble(path + ".support-range", 10.0);
                attrConfig.meleeDamageMultiplier = config.getDouble(path + ".melee-damage-multiplier", 1.0);
                attrConfig.meleeDuration = config.getInt(path + ".melee-duration", 5);
                attrConfig.passiveStrength = config.getDouble(path + ".passive-strength", 1.0);
                attrConfig.passiveTickRate = config.getDouble(path + ".passive-tick-rate", 1.0);
            }
        }
    }

    /**
     * Save settings to config file
     */
    public void saveConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "attribute-settings.yml");
        }

        config = new YamlConfiguration();

        // Save global settings
        config.set("global.cooldown-multiplier", globalCooldownMultiplier);
        config.set("global.damage-multiplier", globalDamageMultiplier);
        config.set("global.level-scaling-percent", levelScalingPercent);

        // Save tier settings
        config.set("tiers.stable.cooldown", stableCooldown);
        config.set("tiers.warped.cooldown", warpedCooldown);
        config.set("tiers.extreme.cooldown", extremeCooldown);

        config.set("tiers.stable.multiplier", stableMultiplier);
        config.set("tiers.warped.multiplier", warpedMultiplier);
        config.set("tiers.extreme.multiplier", extremeMultiplier);

        config.set("tiers.stable.drawback", stableDrawback);
        config.set("tiers.warped.drawback", warpedDrawback);
        config.set("tiers.extreme.drawback", extremeDrawback);

        // Save per-attribute settings
        for (AttributeType type : AttributeType.values()) {
            String path = "attributes." + type.name().toLowerCase();
            AttributeConfig attrConfig = attributeConfigs.get(type);

            config.set(path + ".support-cooldown-modifier", attrConfig.supportCooldownModifier);
            config.set(path + ".melee-cooldown-modifier", attrConfig.meleeCooldownModifier);
            config.set(path + ".support-duration", attrConfig.supportDuration);
            config.set(path + ".support-range", attrConfig.supportRange);
            config.set(path + ".melee-damage-multiplier", attrConfig.meleeDamageMultiplier);
            config.set(path + ".melee-duration", attrConfig.meleeDuration);
            config.set(path + ".passive-strength", attrConfig.passiveStrength);
            config.set(path + ".passive-tick-rate", attrConfig.passiveTickRate);
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save attribute settings: " + e.getMessage());
        }
    }

    /**
     * Reset all settings to defaults
     */
    public void resetToDefaults() {
        globalCooldownMultiplier = 1.0;
        globalDamageMultiplier = 1.0;
        levelScalingPercent = 10.0;

        stableCooldown = 120;
        warpedCooldown = 90;
        extremeCooldown = 60;

        stableMultiplier = 1.0;
        warpedMultiplier = 1.3;
        extremeMultiplier = 1.6;

        stableDrawback = 0.0;
        warpedDrawback = 0.15;
        extremeDrawback = 0.35;

        for (AttributeType type : AttributeType.values()) {
            attributeConfigs.put(type, new AttributeConfig(type));
        }

        saveConfig();
    }

    /**
     * Apply Balanced preset - standard competitive values
     */
    public void applyBalancedPreset(AttributeType type) {
        AttributeConfig config = attributeConfigs.get(type);
        config.supportCooldownModifier = 1.0;
        config.meleeCooldownModifier = 1.0;
        config.supportDuration = 10;
        config.supportRange = 10.0;
        config.meleeDamageMultiplier = 1.0;
        config.meleeDuration = 5;
        config.passiveStrength = 1.0;
        config.passiveTickRate = 1.0;
    }

    /**
     * Apply High Power preset - stronger effects, lower cooldowns
     */
    public void applyHighPowerPreset(AttributeType type) {
        AttributeConfig config = attributeConfigs.get(type);
        config.supportCooldownModifier = 0.7;
        config.meleeCooldownModifier = 0.7;
        config.supportDuration = 15;
        config.supportRange = 15.0;
        config.meleeDamageMultiplier = 1.5;
        config.meleeDuration = 8;
        config.passiveStrength = 1.5;
        config.passiveTickRate = 0.75;
    }

    /**
     * Apply Low Power preset - weaker effects, higher cooldowns
     */
    public void applyLowPowerPreset(AttributeType type) {
        AttributeConfig config = attributeConfigs.get(type);
        config.supportCooldownModifier = 1.5;
        config.meleeCooldownModifier = 1.5;
        config.supportDuration = 6;
        config.supportRange = 6.0;
        config.meleeDamageMultiplier = 0.7;
        config.meleeDuration = 3;
        config.passiveStrength = 0.7;
        config.passiveTickRate = 1.5;
    }

    /**
     * Apply Chaos preset - random extreme values
     */
    public void applyChaosPreset(AttributeType type) {
        AttributeConfig config = attributeConfigs.get(type);
        config.supportCooldownModifier = 0.3 + Math.random() * 1.7; // 0.3 - 2.0
        config.meleeCooldownModifier = 0.3 + Math.random() * 1.7;
        config.supportDuration = 3 + (int)(Math.random() * 20); // 3-22 seconds
        config.supportRange = 5.0 + Math.random() * 20.0; // 5-25 blocks
        config.meleeDamageMultiplier = 0.5 + Math.random() * 2.0; // 0.5-2.5x
        config.meleeDuration = 2 + (int)(Math.random() * 12); // 2-13 seconds
        config.passiveStrength = 0.5 + Math.random() * 2.0;
        config.passiveTickRate = 0.25 + Math.random() * 2.0;
    }

    /**
     * Apply preset to all attributes
     */
    public void applyPresetToAll(String preset) {
        for (AttributeType type : AttributeType.values()) {
            switch (preset.toLowerCase()) {
                case "balanced":
                    applyBalancedPreset(type);
                    break;
                case "highpower":
                case "high_power":
                    applyHighPowerPreset(type);
                    break;
                case "lowpower":
                case "low_power":
                    applyLowPowerPreset(type);
                    break;
                case "chaos":
                    applyChaosPreset(type);
                    break;
            }
        }
        saveConfig();
    }

    // ==================== GETTERS ====================

    public double getGlobalCooldownMultiplier() {
        return globalCooldownMultiplier;
    }

    public double getGlobalDamageMultiplier() {
        return globalDamageMultiplier;
    }

    public double getLevelScalingPercent() {
        return levelScalingPercent;
    }

    public int getTierCooldown(Tier tier) {
        switch (tier) {
            case STABLE: return stableCooldown;
            case WARPED: return warpedCooldown;
            case EXTREME: return extremeCooldown;
            default: return stableCooldown;
        }
    }

    public double getTierMultiplier(Tier tier) {
        switch (tier) {
            case STABLE: return stableMultiplier;
            case WARPED: return warpedMultiplier;
            case EXTREME: return extremeMultiplier;
            default: return stableMultiplier;
        }
    }

    public double getTierDrawback(Tier tier) {
        switch (tier) {
            case STABLE: return stableDrawback;
            case WARPED: return warpedDrawback;
            case EXTREME: return extremeDrawback;
            default: return stableDrawback;
        }
    }

    public AttributeConfig getConfig(AttributeType type) {
        return attributeConfigs.get(type);
    }

    /**
     * Calculate final cooldown for an ability
     */
    public long calculateCooldown(AttributeType type, Tier tier, String abilityType) {
        AttributeConfig attrConfig = attributeConfigs.get(type);
        int baseCooldown = getTierCooldown(tier);

        double modifier = abilityType.equals("support") ?
                attrConfig.supportCooldownModifier :
                attrConfig.meleeCooldownModifier;

        return (long) (baseCooldown * modifier * globalCooldownMultiplier * 1000);
    }

    /**
     * Calculate final damage for an ability
     */
    public double calculateDamage(AttributeType type, Tier tier, int level, double baseDamage) {
        AttributeConfig attrConfig = attributeConfigs.get(type);
        double tierMult = getTierMultiplier(tier);
        double levelMult = 1.0 + (level - 1) * (levelScalingPercent / 100.0);

        return baseDamage * attrConfig.meleeDamageMultiplier * tierMult * levelMult * globalDamageMultiplier;
    }

    // ==================== SETTERS ====================

    public void setGlobalCooldownMultiplier(double value) {
        this.globalCooldownMultiplier = Math.max(0.1, Math.min(5.0, value));
    }

    public void setGlobalDamageMultiplier(double value) {
        this.globalDamageMultiplier = Math.max(0.1, Math.min(5.0, value));
    }

    public void setLevelScalingPercent(double value) {
        this.levelScalingPercent = Math.max(0, Math.min(50, value));
    }

    public void setTierCooldown(Tier tier, int seconds) {
        seconds = Math.max(10, Math.min(300, seconds));
        switch (tier) {
            case STABLE: stableCooldown = seconds; break;
            case WARPED: warpedCooldown = seconds; break;
            case EXTREME: extremeCooldown = seconds; break;
        }
    }

    public void setTierMultiplier(Tier tier, double multiplier) {
        multiplier = Math.max(0.5, Math.min(3.0, multiplier));
        switch (tier) {
            case STABLE: stableMultiplier = multiplier; break;
            case WARPED: warpedMultiplier = multiplier; break;
            case EXTREME: extremeMultiplier = multiplier; break;
        }
    }

    public void setTierDrawback(Tier tier, double drawback) {
        drawback = Math.max(0, Math.min(1.0, drawback));
        switch (tier) {
            case STABLE: stableDrawback = drawback; break;
            case WARPED: warpedDrawback = drawback; break;
            case EXTREME: extremeDrawback = drawback; break;
        }
    }

    // Getters for tier values (for display)
    public int getStableCooldown() { return stableCooldown; }
    public int getWarpedCooldown() { return warpedCooldown; }
    public int getExtremeCooldown() { return extremeCooldown; }
    public double getStableMultiplier() { return stableMultiplier; }
    public double getWarpedMultiplier() { return warpedMultiplier; }
    public double getExtremeMultiplier() { return extremeMultiplier; }
    public double getStableDrawback() { return stableDrawback; }
    public double getWarpedDrawback() { return warpedDrawback; }
    public double getExtremeDrawback() { return extremeDrawback; }

    /**
     * Per-attribute configuration
     */
    public static class AttributeConfig {
        public final AttributeType type;

        // Cooldown modifiers (multiplied with tier cooldown)
        public double supportCooldownModifier = 1.0;
        public double meleeCooldownModifier = 1.0;

        // Support ability settings
        public int supportDuration = 10; // seconds
        public double supportRange = 10.0; // blocks

        // Melee ability settings
        public double meleeDamageMultiplier = 1.0;
        public int meleeDuration = 5; // seconds

        // Passive settings
        public double passiveStrength = 1.0;
        public double passiveTickRate = 1.0; // seconds

        public AttributeConfig(AttributeType type) {
            this.type = type;
        }
    }
}
