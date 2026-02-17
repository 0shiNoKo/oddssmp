package com.oddssmp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private AttributeType attribute;
    private int level;
    private int kills;
    private int deaths;
    private final Map<String, Long> cooldowns;

    // Combat tracking
    private long lastHitTime;
    private UUID lastAttacker;

    public PlayerData() {
        this.attribute = null;
        this.level = 1;
        this.kills = 0;
        this.deaths = 0;
        this.cooldowns = new HashMap<>();
        this.lastHitTime = 0;
        this.lastAttacker = null;
    }

    public PlayerData(AttributeType attribute) {
        this();
        this.attribute = attribute;
    }

    // Getters and Setters
    public AttributeType getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeType attribute) {
        this.attribute = attribute;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(5, level));
    }

    public void incrementLevel() {
        if (level < 5) {
            level++;
        }
    }

    public void decrementLevel() {
        if (level > 1) {
            level--;
        }
    }

    public int getKills() {
        return kills;
    }

    public void incrementKills() {
        kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void incrementDeaths() {
        deaths++;
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public boolean isOnCooldown(String abilityType) {
        if (!cooldowns.containsKey(abilityType)) {
            return false;
        }
        long cooldownEnd = cooldowns.get(abilityType);
        return System.currentTimeMillis() < cooldownEnd;
    }

    public void setCooldown(String abilityType, long durationMillis) {
        cooldowns.put(abilityType, System.currentTimeMillis() + durationMillis);
    }

    public long getRemainingCooldown(String abilityType) {
        if (!cooldowns.containsKey(abilityType)) {
            return 0;
        }
        long remaining = cooldowns.get(abilityType) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }

    /**
     * Calculate scaling multiplier based on level
     * Each level adds 10% to effect (Level 1 = 100%, Level 5 = 140%)
     */
    public double getLevelScaling() {
        return 1.0 + ((level - 1) * 0.1);
    }

    // Combat tracking
    public long getLastHitTime() {
        return lastHitTime;
    }

    public void setLastHitTime(long time) {
        this.lastHitTime = time;
    }

    public UUID getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(UUID attacker) {
        this.lastAttacker = attacker;
    }
}
