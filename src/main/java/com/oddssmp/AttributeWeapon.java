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
    // Standard Attribute Weapons (15 damage, 1.6 attack speed)
    BREAKER_BLADE(AttributeType.MELEE, "Breaker Blade", Material.NETHERITE_SWORD,
        15.0, 1.6, 300, "§c",
        "On hit: -40% armor for ability hit", "+6% final melee damage"),

    CRIMSON_FANG(AttributeType.HEALTH, "Crimson Fang", Material.NETHERITE_SWORD,
        15.0, 1.6, 300, "§4",
        "On hit: 10% lifesteal", "+2 max HP while held"),

    BULWARK_MACE(AttributeType.DEFENSE, "Bulwark Mace", Material.MACE,
        15.0, 1.6, 300, "§7",
        "On hit: 10% damage reduction 3s", "-10% knockback taken"),

    GILDED_CLEAVER(AttributeType.WEALTH, "Gilded Cleaver", Material.NETHERITE_AXE,
        15.0, 1.6, 300, "§6",
        "Mob kill: +50% drops", "Looting +2"),

    LOCKSPIKE(AttributeType.CONTROL, "Lockspike", Material.IRON_SWORD,
        15.0, 1.6, 300, "§5",
        "On hit: Slowness I 2s", "+10% ability duration"),

    WINDCALLER_PIKE(AttributeType.RANGE, "Windcaller Pike", Material.TRIDENT,
        15.0, 1.6, 300, "§a",
        "Reach: +1.5 blocks", "+25% knockback"),

    CHRONO_SABER(AttributeType.TEMPO, "Chrono Saber", Material.GOLDEN_SWORD,
        15.0, 1.6, 300, "§e",
        "On hit: +5s cooldown to target", "-5% own cooldowns"),

    WATCHERS_BLADE(AttributeType.VISION, "Watcher's Blade", Material.NETHERITE_SWORD,
        15.0, 1.6, 300, "§b",
        "On hit: Mark target 5s", "Glow enemies within 6 blocks"),

    MIRROR_EDGE(AttributeType.TRANSFER, "Mirror Edge", Material.DIAMOND_SWORD,
        15.0, 1.6, 300, "§d",
        "On hit: Steal positive effects 4s", "-10% debuff duration"),

    FLASHSTEEL_DAGGER(AttributeType.SPEED, "Flashsteel Dagger", Material.IRON_SWORD,
        15.0, 1.6, 300, "§f",
        "On hit: +5% move speed 3s", "+10% sprint speed"),

    BONECRUSHER(AttributeType.PRESSURE, "Bonecrusher", Material.MACE,
        15.0, 1.6, 300, "§4",
        "On hit: Target +10% damage taken 4s", "+10% damage vs low HP"),

    FRACTURE_ROD(AttributeType.DISRUPTION, "Fracture Rod", Material.BLAZE_ROD,
        15.0, 1.6, 300, "§5",
        "On hit: +10s cooldown to all abilities", "+10% debuff duration"),

    HIGH_ROLLER_BLADE(AttributeType.RISK, "High Roller Blade", Material.GOLDEN_SWORD,
        15.0, 1.6, 300, "§6",
        "On hit: +30% damage dealt, +15% taken", "+10% crit below 40% HP"),

    // Boss Weapons (16 damage, 1.6 attack speed)
    DESPAIR_REAVER(AttributeType.WITHER, "Despair Reaver", Material.NETHERITE_HOE,
        16.0, 1.6, 300, "§8",
        "+1% damage per 1% missing HP", "Healing received -20%"),

    DEEPCORE_MAUL(AttributeType.WARDEN, "Deepcore Maul", Material.MACE,
        16.0, 1.6, 300, "§3",
        "+20% damage in zones", "-20% knockback taken"),

    VERDICT_LANCE(AttributeType.BREEZE, "Verdict Lance", Material.TRIDENT,
        16.0, 1.6, 300, "§b",
        "On hit: 3 true damage", "+10% damage vs cooldown users"),

    DOMINION_BLADE(AttributeType.DRAGON_EGG, "Dominion Blade", Material.NETHERITE_SWORD,
        16.0, 1.6, 300, "§5",
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
