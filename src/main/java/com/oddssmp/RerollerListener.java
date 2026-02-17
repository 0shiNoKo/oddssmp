package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class RerollerListener implements Listener {

    private final OddsSMP plugin;

    public RerollerListener(OddsSMP plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the crafting recipe
     */
    public void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "attribute_reroller");

        // Remove existing recipe if it exists
        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {}

        ShapedRecipe recipe = new ShapedRecipe(key, RerollerItem.createReroller());

        // Pattern:
        // D N D
        // N S N
        // D N D
        // D = Diamond Block, N = Netherite Ingot, S = Wither Skeleton Skull
        recipe.shape("DND", "NSN", "DND");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.WITHER_SKELETON_SKULL);

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Registered Attribute Reroller recipe!");
    }

    /**
     * Handle crafting validation
     */
    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack result = inv.getResult();

        if (result != null && RerollerItem.isReroller(result)) {
            // Verify the recipe is correct
            ItemStack[] matrix = inv.getMatrix();

            // Expected pattern indices:
            // 0 1 2
            // 3 4 5
            // 6 7 8

            boolean valid = true;

            // Check corners (diamond blocks)
            if (!isType(matrix[0], Material.DIAMOND_BLOCK) ||
                    !isType(matrix[2], Material.DIAMOND_BLOCK) ||
                    !isType(matrix[6], Material.DIAMOND_BLOCK) ||
                    !isType(matrix[8], Material.DIAMOND_BLOCK)) {
                valid = false;
            }

            // Check sides (netherite ingots)
            if (!isType(matrix[1], Material.NETHERITE_INGOT) ||
                    !isType(matrix[3], Material.NETHERITE_INGOT) ||
                    !isType(matrix[5], Material.NETHERITE_INGOT) ||
                    !isType(matrix[7], Material.NETHERITE_INGOT)) {
                valid = false;
            }

            // Check center (wither skeleton skull)
            if (!isType(matrix[4], Material.WITHER_SKELETON_SKULL)) {
                valid = false;
            }

            if (!valid) {
                inv.setResult(null);
            }
        }
    }

    /**
     * Handle right-click with reroller
     */
    @EventHandler
    public void onRerollerUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!RerollerItem.isReroller(item)) {
            return;
        }

        event.setCancelled(true);

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§cYou don't have player data!");
            return;
        }

        // Check if they already have an attribute
        if (data.getAttribute() == null) {
            player.sendMessage("§cYou don't have an attribute to reroll!");
            return;
        }

        // Save old attribute
        AttributeType oldAttribute = data.getAttribute();

        // Remove one reroller from inventory first
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Play the slot animation and assign new attribute (handles everything)
        plugin.getEventListener().playRerollerAnimation(player, oldAttribute);
    }

    /**
     * Helper method to check item type
     */
    private boolean isType(ItemStack item, Material type) {
        return item != null && item.getType() == type;
    }
}