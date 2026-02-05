package com.oddssmp;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RerollerItem {

    /**
     * Create an Attribute Reroller item
     */
    public static ItemStack createReroller() {
        ItemStack reroller = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = reroller.getItemMeta();

        meta.setDisplayName("§5§l§kA§r §6§lAttribute Reroller §5§l§kA");

        List<String> lore = new ArrayList<>();
        lore.add("§7A mystical artifact that can");
        lore.add("§7reroll your attribute and tier.");
        lore.add("");
        lore.add("§e§lRight-Click §7to reroll your attribute!");
        lore.add("");
        lore.add("§c§lWARNING: §7This will give you a");
        lore.add("§7completely random attribute and tier!");
        lore.add("");
        lore.add("§8Crafted with immense power...");

        meta.setLore(lore);
        reroller.setItemMeta(meta);

        return reroller;
    }

    /**
     * Check if an item is a Reroller
     */
    public static boolean isReroller(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        return meta.getDisplayName().contains("Attribute Reroller");
    }
}