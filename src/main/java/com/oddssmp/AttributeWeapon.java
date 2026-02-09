package com.oddssmp;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum AttributeWeapon {
    // Standard Attribute Weapons
    BREAKER_BLADE(AttributeType.MELEE, "Breaker Blade", Material.NETHERITE_SWORD,
        9.0, 1.6, 1800, "§c",
        "On hit: -40% armor for ability hit", "+6% final melee damage"),

    CRIMSON_FANG(AttributeType.HEALTH, "Crimson Fang", Material.NETHERITE_SWORD,
        8.0, 1.6, 1650, "§4",
        "On hit: 10% lifesteal", "+2 max HP while held"),

    BULWARK_MACE(AttributeType.DEFENSE, "Bulwark Mace", Material.MACE,
        8.5, 1.2, 2200, "§7",
        "On hit: 10% damage reduction 3s", "-10% knockback taken"),

    GILDED_CLEAVER(AttributeType.WEALTH, "Gilded Cleaver", Material.NETHERITE_AXE,
        9.5, 1.0, 1400, "§6",
        "Mob kill: +50% drops", "Looting +2"),

    LOCKSPIKE(AttributeType.CONTROL, "Lockspike", Material.IRON_SWORD,
        6.5, 2.2, 1200, "§5",
        "On hit: Slowness I 2s", "+10% ability duration"),

    WINDCALLER_PIKE(AttributeType.RANGE, "Windcaller Pike", Material.TRIDENT,
        8.0, 1.3, 1700, "§a",
        "Reach: +1.5 blocks", "+25% knockback"),

    CHRONO_SABER(AttributeType.TEMPO, "Chrono Saber", Material.GOLDEN_SWORD,
        7.5, 1.8, 1600, "§e",
        "On hit: +5s cooldown to target", "-5% own cooldowns"),

    WATCHERS_BLADE(AttributeType.VISION, "Watcher's Blade", Material.NETHERITE_SWORD,
        7.0, 1.6, 1500, "§b",
        "On hit: Mark target 5s", "Glow enemies within 6 blocks"),

    PAINBOUND_GREATSWORD(AttributeType.PERSISTENCE, "Painbound Greatsword", Material.NETHERITE_SWORD,
        10.5, 0.9, 2400, "§8",
        "On hit: Release stored damage", "Store 10% damage taken"),

    MIRROR_EDGE(AttributeType.TRANSFER, "Mirror Edge", Material.DIAMOND_SWORD,
        7.5, 1.6, 1550, "§d",
        "On hit: Steal positive effects 4s", "-10% debuff duration"),

    FLASHSTEEL_DAGGER(AttributeType.SPEED, "Flashsteel Dagger", Material.IRON_SWORD,
        6.0, 2.4, 1300, "§f",
        "On hit: +5% move speed 3s", "+10% sprint speed"),

    BONECRUSHER(AttributeType.PRESSURE, "Bonecrusher", Material.MACE,
        10.0, 1.0, 2100, "§4",
        "On hit: Target +10% damage taken 4s", "+10% damage vs low HP"),

    FRACTURE_ROD(AttributeType.DISRUPTION, "Fracture Rod", Material.BLAZE_ROD,
        6.5, 1.4, 1500, "§5",
        "On hit: +10s cooldown to all abilities", "+10% debuff duration"),

    IRONROOT_HALBERD(AttributeType.ANCHOR, "Ironroot Halberd", Material.NETHERITE_AXE,
        9.0, 1.1, 2300, "§7",
        "On hit: No sprint/jump 3s", "+25% knockback resistance"),

    HIGH_ROLLER_BLADE(AttributeType.RISK, "High Roller Blade", Material.GOLDEN_SWORD,
        11.0, 1.6, 1400, "§6",
        "On hit: +30% damage dealt, +15% taken", "+10% crit below 40% HP"),

    // Boss Weapons
    DESPAIR_REAVER(AttributeType.WITHER, "Despair Reaver", Material.NETHERITE_HOE,
        12.0, 1.0, 2600, "§8",
        "+1% damage per 1% missing HP", "Healing received -20%"),

    DEEPCORE_MAUL(AttributeType.WARDEN, "Deepcore Maul", Material.MACE,
        13.0, 0.8, 3000, "§3",
        "+20% damage in zones", "-20% knockback taken"),

    VERDICT_LANCE(AttributeType.BREEZE, "Verdict Lance", Material.TRIDENT,
        9.5, 1.5, 2000, "§b",
        "On hit: 3 true damage", "+10% damage vs cooldown users"),

    DOMINION_BLADE(AttributeType.DRAGON_EGG, "Dominion Blade", Material.NETHERITE_SWORD,
        13.5, 1.0, 3200, "§5",
        "Allies gain +10% damage", "Lifesteal 10%");

    private final AttributeType requiredAttribute;
    private final String name;
    private final Material material;
    private final double baseDamage;
    private final double attackSpeed;
    private final int durability;
    private final String color;
    private final String onHitEffect;
    private final String passiveBonus;

    AttributeWeapon(AttributeType requiredAttribute, String name, Material material,
                    double baseDamage, double attackSpeed, int durability, String color,
                    String onHitEffect, String passiveBonus) {
        this.requiredAttribute = requiredAttribute;
        this.name = name;
        this.material = material;
        this.baseDamage = baseDamage;
        this.attackSpeed = attackSpeed;
        this.durability = durability;
        this.color = color;
        this.onHitEffect = onHitEffect;
        this.passiveBonus = passiveBonus;
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Set display name
        meta.setDisplayName(color + "§l" + name);

        // Set lore
        List<String> lore = new ArrayList<>();
        lore.add("§7Attribute Weapon: " + color + requiredAttribute.getDisplayName());
        lore.add("");
        lore.add("§7Base Damage: §c" + baseDamage);
        lore.add("§7Attack Speed: §e" + attackSpeed);
        lore.add("§7Durability: §a" + durability);
        lore.add("");
        lore.add("§6§lOn Hit:");
        lore.add("§7" + onHitEffect);
        lore.add("");
        lore.add("§d§lPassive:");
        lore.add("§7" + passiveBonus);
        lore.add("");
        lore.add("§c§lRequires: " + color + requiredAttribute.getDisplayName() + " §c§lattribute");
        lore.add("");
        lore.add("§8§oUnenchantable • Unrepairable");
        meta.setLore(lore);

        // Set unbreakable false but track durability manually
        meta.setUnbreakable(false);

        // Set attack damage and speed via attributes
        try {
            // Using older API for compatibility
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(UUID.randomUUID(), "weapon_damage",
                    baseDamage - 1, AttributeModifier.Operation.ADD_NUMBER));

            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(UUID.randomUUID(), "weapon_speed",
                    attackSpeed - 4, AttributeModifier.Operation.ADD_NUMBER));
        } catch (Exception e) {
            // Fallback for API differences
        }

        item.setItemMeta(meta);

        // Set durability
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
            org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) item.getItemMeta();
            damageable.setMaxDamage(durability);
            item.setItemMeta((ItemMeta) damageable);
        }

        return item;
    }

    public AttributeType getRequiredAttribute() {
        return requiredAttribute;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public int getDurability() {
        return durability;
    }

    public String getColor() {
        return color;
    }

    public String getOnHitEffect() {
        return onHitEffect;
    }

    public String getPassiveBonus() {
        return passiveBonus;
    }

    public static AttributeWeapon getByAttribute(AttributeType type) {
        for (AttributeWeapon weapon : values()) {
            if (weapon.requiredAttribute == type) {
                return weapon;
            }
        }
        return null;
    }

    public static boolean isAttributeWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        if (name == null) return false;

        for (AttributeWeapon weapon : values()) {
            if (name.contains(weapon.name)) {
                return true;
            }
        }
        return false;
    }

    public static AttributeWeapon getFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String name = item.getItemMeta().getDisplayName();
        if (name == null) return null;

        for (AttributeWeapon weapon : values()) {
            if (name.contains(weapon.name)) {
                return weapon;
            }
        }
        return null;
    }
}
