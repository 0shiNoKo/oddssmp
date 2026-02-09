package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WeaponGUI {

    private final OddsSMP plugin;

    public static final String WEAPON_MENU_TITLE = "§6§lAttribute Weapons";
    public static final String BOSS_WEAPON_MENU_TITLE = "§5§lBoss Weapons";

    public WeaponGUI(OddsSMP plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the main weapon selection menu
     */
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, WEAPON_MENU_TITLE);

        // Fill borders with glass
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(45 + i, border);
        }
        for (int i = 0; i < 54; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }

        // Standard attribute weapons (slots 10-16, 19-25, 28-34)
        int slot = 10;
        for (AttributeWeapon weapon : AttributeWeapon.values()) {
            // Skip boss weapons for main page
            if (weapon.getRequiredAttribute().isBossAttribute() ||
                weapon.getRequiredAttribute().isDragonEgg()) {
                continue;
            }

            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) break;

            inv.setItem(slot, createWeaponDisplayItem(weapon));
            slot++;
        }

        // Boss weapons button
        ItemStack bossButton = createItem(Material.WITHER_SKELETON_SKULL, "§5§lBoss Weapons",
            "§7Click to view boss weapons",
            "",
            "§8Wither, Warden, Breeze, Dragon");
        inv.setItem(40, bossButton);

        // Give all button
        ItemStack giveAllButton = createItem(Material.CHEST, "§a§lGive All Weapons",
            "§7Click to receive all standard",
            "§7attribute weapons");
        inv.setItem(49, giveAllButton);

        player.openInventory(inv);
    }

    /**
     * Open boss weapons menu
     */
    public void openBossWeaponsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, BOSS_WEAPON_MENU_TITLE);

        // Fill borders
        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(18 + i, border);
        }
        inv.setItem(9, border);
        inv.setItem(17, border);

        // Boss weapons
        inv.setItem(11, createWeaponDisplayItem(AttributeWeapon.DESPAIR_REAVER));
        inv.setItem(12, createWeaponDisplayItem(AttributeWeapon.DEEPCORE_MAUL));
        inv.setItem(14, createWeaponDisplayItem(AttributeWeapon.VERDICT_LANCE));
        inv.setItem(15, createWeaponDisplayItem(AttributeWeapon.DOMINION_BLADE));

        // Back button
        ItemStack backButton = createItem(Material.ARROW, "§c§lBack",
            "§7Return to main menu");
        inv.setItem(22, backButton);

        player.openInventory(inv);
    }

    /**
     * Create a display item for a weapon (shows info, click to get)
     */
    private ItemStack createWeaponDisplayItem(AttributeWeapon weapon) {
        ItemStack item = new ItemStack(weapon.getMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(weapon.getColor() + "§l" + weapon.getName());

        List<String> lore = new ArrayList<>();
        lore.add("§7Attribute: " + weapon.getColor() + weapon.getRequiredAttribute().getDisplayName());
        lore.add("");
        lore.add("§7Damage: §c" + weapon.getBaseDamage());
        lore.add("§7Speed: §e" + weapon.getAttackSpeed());
        lore.add("§7Durability: §a" + weapon.getDurability());
        lore.add("");
        lore.add("§6Effect: §f" + weapon.getOnHitEffect());
        lore.add("§dPassive: §f" + weapon.getPassiveBonus());
        lore.add("");
        lore.add("§e§lClick to receive!");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Give a weapon to a player
     */
    public void giveWeapon(Player player, AttributeWeapon weapon) {
        ItemStack weaponItem = weapon.createItem();
        player.getInventory().addItem(weaponItem);
        player.sendMessage("§aYou received " + weapon.getColor() + "§l" + weapon.getName() + "§a!");
    }

    /**
     * Give all standard weapons to a player
     */
    public void giveAllWeapons(Player player) {
        int count = 0;
        for (AttributeWeapon weapon : AttributeWeapon.values()) {
            if (!weapon.getRequiredAttribute().isBossAttribute() &&
                !weapon.getRequiredAttribute().isDragonEgg()) {
                player.getInventory().addItem(weapon.createItem());
                count++;
            }
        }
        player.sendMessage("§aYou received §e" + count + " §aattribute weapons!");
    }

    /**
     * Helper to create simple items
     */
    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (loreLines.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }
}
