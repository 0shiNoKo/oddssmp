package com.oddssmp;

import org.bukkit.ChatColor;

public enum Tier {
    STABLE(ChatColor.GREEN, 1.0, 0.0, 120, 50),
    WARPED(ChatColor.LIGHT_PURPLE, 1.3, 0.15, 90, 35),
    EXTREME(ChatColor.RED, 1.6, 0.35, 60, 15);

    private final ChatColor color;
    private final double effectMultiplier;
    private final double drawbackMultiplier;
    private final int cooldownSeconds;
    private final int probability;

    Tier(ChatColor color, double effectMultiplier, double drawbackMultiplier, int cooldownSeconds, int probability) {
        this.color = color;
        this.effectMultiplier = effectMultiplier;
        this.drawbackMultiplier = drawbackMultiplier;
        this.cooldownSeconds = cooldownSeconds;
        this.probability = probability;
    }

    public ChatColor getColor() {
        return color;
    }

    public double getEffectMultiplier() {
        return effectMultiplier;
    }

    public double getDrawbackMultiplier() {
        return drawbackMultiplier;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public int getProbability() {
        return probability;
    }

    /**
     * Get a random tier based on probability weights
     * Stable: 50%, Warped: 35%, Extreme: 15%
     */
    public static Tier getRandomTier() {
        int random = (int) (Math.random() * 100);
        if (random < 50) {
            return STABLE;
        } else if (random < 85) {
            return WARPED;
        } else {
            return EXTREME;
        }
    }

    /**
     * Get Dragon Egg tier (always Extreme)
     */
    public static Tier getDragonEggTier() {
        return EXTREME;
    }
}
