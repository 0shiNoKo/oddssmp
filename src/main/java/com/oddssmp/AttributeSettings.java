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

    // Base cooldown in seconds
    private int baseCooldown = 120;

    // Per-attribute settings
    private final Map<AttributeType, AttributeConfig> attributeConfigs = new HashMap<>();

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
        baseCooldown = config.getInt("global.base-cooldown", 120);

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
        config.set("global.base-cooldown", baseCooldown);

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
        baseCooldown = 120;

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

    public int getBaseCooldown() {
        return baseCooldown;
    }

    public AttributeConfig getConfig(AttributeType type) {
        return attributeConfigs.get(type);
    }

    /**
     * Calculate final cooldown for an ability
     */
    public long calculateCooldown(AttributeType type, String abilityType) {
        AttributeConfig attrConfig = attributeConfigs.get(type);

        double modifier = abilityType.equals("support") ?
                attrConfig.supportCooldownModifier :
                attrConfig.meleeCooldownModifier;

        return (long) (baseCooldown * modifier * globalCooldownMultiplier * 1000);
    }

    /**
     * Calculate final damage for an ability
     */
    public double calculateDamage(AttributeType type, int level, double baseDamage) {
        AttributeConfig attrConfig = attributeConfigs.get(type);
        double levelMult = 1.0 + (level - 1) * (levelScalingPercent / 100.0);

        return baseDamage * attrConfig.meleeDamageMultiplier * levelMult * globalDamageMultiplier;
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

    public void setBaseCooldown(int seconds) {
        this.baseCooldown = Math.max(10, Math.min(300, seconds));
    }

    /**
     * Per-attribute configuration
     */
    public static class AttributeConfig {
        public final AttributeType type;

        // Cooldown modifiers (multiplied with base cooldown)
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
