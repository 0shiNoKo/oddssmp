package com.oddssmp;

public enum AttributeType {
    MELEE("⚔", "Melee"),
    HEALTH("❤", "Health"),
    DEFENSE("🛡", "Defense"),
    WEALTH("💰", "Wealth"),
    SPEED("⚡", "Speed"),
    CONTROL("🕹", "Control"),
    RANGE("🏹", "Range"),
    PRESSURE("🩸", "Pressure"),
    TEMPO("⏱", "Tempo"),
    DISRUPTION("🧠", "Disruption"),
    VISION("👁", "Vision"),
    PERSISTENCE("♾", "Persistence"),
    ANCHOR("⚓", "Anchor"),
    TRANSFER("🔁", "Transfer"),
    RISK("🎲", "Risk"),
    WITHER("💀", "Wither"),
    WARDEN("🐋", "Warden"),
    BREEZE("🌬", "Breeze"),
    DRAGON_EGG("🥚", "Dragon Egg");

    private final String icon;
    private final String displayName;

    AttributeType(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get a random attribute excluding boss attributes and Dragon Egg
     */
    public static AttributeType getRandomAttribute(boolean includeDragonEgg) {
        AttributeType[] normalAttributes = {
                MELEE, HEALTH, DEFENSE, WEALTH, SPEED,
                RANGE, PRESSURE, TEMPO, DISRUPTION,
                VISION, TRANSFER, RISK
        };

        if (includeDragonEgg) {
            AttributeType[] withDragon = new AttributeType[13];
            System.arraycopy(normalAttributes, 0, withDragon, 0, 12);
            withDragon[12] = DRAGON_EGG;
            return withDragon[(int) (Math.random() * withDragon.length)];
        }

        return normalAttributes[(int) (Math.random() * normalAttributes.length)];
    }

    /**
     * Check if this is a boss attribute
     */
    public boolean isBossAttribute() {
        return this == WITHER || this == WARDEN || this == BREEZE;
    }

    /**
     * Check if this is Dragon Egg
     */
    public boolean isDragonEgg() {
        return this == DRAGON_EGG;
    }
}