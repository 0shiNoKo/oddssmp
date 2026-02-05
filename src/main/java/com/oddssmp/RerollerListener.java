package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
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

        // Save old attribute for message
        AttributeType oldAttribute = data.getAttribute();
        Tier oldTier = data.getTier();

        // Remove old attribute effects
        plugin.getEventListener().removeAttributeEffects(player, oldAttribute);

        // Get new random attribute and tier
        AttributeType newAttribute = AttributeType.getRandomAttribute(false);
        Tier newTier = Tier.getRandomTier();

        // Apply new attribute
        data.setAttribute(newAttribute);
        data.setTier(newTier);
        data.setLevel(1); // Reset to level 1
        data.clearCooldowns();
        plugin.getAbilityManager().removeAbilityFlags(player.getUniqueId());

        // Apply new attribute effects if needed
        if (newAttribute == AttributeType.DRAGON_EGG) {
            plugin.getEventListener().applyDragonEggEffects(player);
        }

        // Remove one reroller from inventory
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Play effects
        ParticleManager.playSupportParticles(player, newAttribute, newTier, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);

        // Update tab
        plugin.updatePlayerTab(player);

        // Messages
        player.sendMessage("");
        player.sendMessage("§6§l§m                                                    ");
        player.sendMessage("§5§l⚡ ATTRIBUTE REROLLED! ⚡");
        player.sendMessage("");
        player.sendMessage("  §7Old: " + oldTier.getColor() + oldTier.name() + " " +
                oldAttribute.getIcon() + " " + oldAttribute.getDisplayName());
        player.sendMessage("  §7New: " + newTier.getColor() + newTier.name() + " " +
                newAttribute.getIcon() + " " + newAttribute.getDisplayName());
        player.sendMessage("");
        player.sendMessage("  §7Your level has been reset to §e1");
        player.sendMessage("§6§l§m                                                    ");
        player.sendMessage("");

        // Broadcast to nearby players
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= 50 && !nearby.equals(player)) {
                nearby.sendMessage("§6" + player.getName() + " §7rerolled their attribute to " +
                        newTier.getColor() + newAttribute.getDisplayName() + "§7!");
            }
        }

        plugin.getLogger().info(player.getName() + " rerolled from " + oldAttribute + " to " + newAttribute);
    }

    /**
     * Helper method to check item type
     */
    private boolean isType(ItemStack item, Material type) {
        return item != null && item.getType() == type;
    }
}