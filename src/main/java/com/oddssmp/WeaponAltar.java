package com.oddssmp;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WeaponAltar {

    private final OddsSMP plugin;
    private final AttributeWeapon weapon;
    private final Location location;
    private Set<Location> protectedBlocks = new HashSet<>();
    private boolean active = true;
    private List<Entity> displayEntities = new ArrayList<>(); // Track spawned display entities
    private ItemDisplay weaponDisplay; // Track weapon display for rotation

    // Crafting costs for each weapon
    private static final Map<AttributeWeapon, Map<Material, Integer>> CRAFTING_COSTS = new HashMap<>();
    private static final Map<AttributeWeapon, List<String>> CUSTOM_ITEMS = new HashMap<>();

    static {
        initCraftingCosts();
    }

    public WeaponAltar(OddsSMP plugin, AttributeWeapon weapon, Location location) {
        this.plugin = plugin;
        this.weapon = weapon;
        this.location = location.clone();
    }

    /**
     * Spawn the altar structure
     */
    public void spawn() {
        World world = location.getWorld();
        if (world == null) return;

        // Spawn display entities (background, text, weapon item)
        spawnHologramText();

        // Start ambient particle effects
        startAmbientEffects();
    }

    /**
     * Build the pedestal structure
     */
    private void buildPedestal() {
        World world = location.getWorld();
        if (world == null) return;

        Location base = location.clone();

        // Bottom layer - 3x3 polished deepslate base
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                setBlock(base.clone().add(x, 0, z), Material.POLISHED_DEEPSLATE);
            }
        }

        // Middle pillar - deepslate bricks
        setBlock(base.clone().add(0, 1, 0), Material.DEEPSLATE_BRICKS);

        // Top platform - chiseled deepslate
        setBlock(base.clone().add(0, 2, 0), Material.CHISELED_DEEPSLATE);

        // Corner pillars with chains
        Location[] corners = {
            base.clone().add(-1, 1, -1),
            base.clone().add(-1, 1, 1),
            base.clone().add(1, 1, -1),
            base.clone().add(1, 1, 1)
        };

        for (Location corner : corners) {
            setBlock(corner, Material.CHAIN);
            setBlock(corner.clone().add(0, 1, 0), Material.CHAIN);
        }
    }

    private void setBlock(Location loc, Material material) {
        Block block = loc.getBlock();
        block.setType(material);
        // Track this block as protected
        protectedBlocks.add(block.getLocation());
    }

    /**
     * Spawn display entities for weapon altar (using Minecraft Display Entities)
     */
    private void spawnHologramText() {
        World world = location.getWorld();
        if (world == null) return;

        // ONE ANCHOR POINT - never change X or Z
        double anchorX = location.getBlockX() + 0.5;
        double anchorZ = location.getBlockZ() + 0.5;
        double baseY = location.getBlockY() + 1.0; // Floating above ground

        // 1. BUILD TEXT LINES
        List<String> textLines = new ArrayList<>();

        // Title
        textLines.add(weapon.getColor() + "§l" + weapon.getName());

        // Recipe requirements
        Map<Material, Integer> costs = CRAFTING_COSTS.get(weapon);
        List<String> customItems = CUSTOM_ITEMS.get(weapon);

        if (costs != null) {
            for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
                String materialName = formatMaterialName(entry.getKey());
                textLines.add("§f" + entry.getValue() + "x §7" + materialName);
            }
        }

        if (customItems != null) {
            textLines.addAll(customItems);
        }

        // 2. SPAWN TEXT DISPLAYS - same X/Z, only Y changes
        double textY = baseY + 2.4; // Start from top
        double lineSpacing = 0.3;

        for (int i = 0; i < textLines.size(); i++) {
            String line = textLines.get(i);
            double yOffset = textY - (i * lineSpacing);

            Location textLoc = new Location(world, anchorX, yOffset, anchorZ);

            TextDisplay textDisplay = world.spawn(textLoc, TextDisplay.class, display -> {
                display.setText(line);
                display.setBillboard(Display.Billboard.CENTER); // Face towards player
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // Transparent background
                display.setShadowed(true);
            });
            displayEntities.add(textDisplay);
        }

        // 3. SPAWN ITEM DISPLAY - weapon below text, centered, MUCH BIGGER
        double itemY = baseY + 0.5;
        Location itemLoc = new Location(world, anchorX, itemY, anchorZ + 0.10);

        weaponDisplay = world.spawn(itemLoc, ItemDisplay.class, display -> {
            ItemStack weaponItem = new ItemStack(weapon.getMaterial());
            display.setItemStack(weaponItem);
            display.setBillboard(Display.Billboard.FIXED);
            // Scale up the item (1.75x)
            Transformation transform = new Transformation(
                new Vector3f(0f, 0f, 0f),         // translation
                new AxisAngle4f(0, 0, 1, 0),      // left rotation (Y axis)
                new Vector3f(1.75f, 1.75f, 1.75f), // scale
                new AxisAngle4f(0, 0, 0, 1)       // right rotation
            );
            display.setTransformation(transform);
        });
        displayEntities.add(weaponDisplay);

        // 4. PLACE SOLID ANCIENT DEBRIS BLOCK - below the display
        Location blockLoc = new Location(world, location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
        setBlock(blockLoc, Material.ANCIENT_DEBRIS);

        plugin.getLogger().info("Created display entities for " + weapon.getName() + " altar");
    }

    /**
     * Start ambient effects (rotation + particles)
     */
    private void startAmbientEffects() {
        new BukkitRunnable() {
            int tick = 0;
            float rotation = 0f;

            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }

                tick++;

                // Rotate weapon display smoothly
                if (weaponDisplay != null && !weaponDisplay.isDead()) {
                    rotation += 0.05f; // Rotation speed
                    if (rotation > (float)(Math.PI * 2)) {
                        rotation -= (float)(Math.PI * 2);
                    }

                    Transformation transform = new Transformation(
                        new Vector3f(0f, 0f, 0f),                    // translation
                        new AxisAngle4f(rotation, 0, 1, 0),          // left rotation (around Y axis)
                        new Vector3f(1.75f, 1.75f, 1.75f),           // scale
                        new AxisAngle4f(0, 0, 0, 1)                  // right rotation
                    );
                    weaponDisplay.setTransformation(transform);
                }

                // Particle effects every second (around the display)
                World world = location.getWorld();
                if (world != null && tick % 20 == 0) {
                    world.spawnParticle(Particle.ENCHANT,
                        location.clone().add(0.5, 2.0, 0.5),
                        10, 0.3, 0.5, 0.3, 0.05);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Check if a block location is part of this altar
     */
    public boolean isProtectedBlock(org.bukkit.Location blockLoc) {
        for (Location protected_loc : protectedBlocks) {
            if (protected_loc.getBlockX() == blockLoc.getBlockX() &&
                protected_loc.getBlockY() == blockLoc.getBlockY() &&
                protected_loc.getBlockZ() == blockLoc.getBlockZ() &&
                protected_loc.getWorld().equals(blockLoc.getWorld())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if player has required materials
     */
    public boolean hasRequiredMaterials(Player player) {
        Map<Material, Integer> costs = CRAFTING_COSTS.get(weapon);
        if (costs == null) return false;

        for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
            if (!player.getInventory().contains(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        // Check for custom items (Weapon Handle, Heart items)
        List<String> customItems = CUSTOM_ITEMS.get(weapon);
        if (customItems != null) {
            for (String customItem : customItems) {
                if (!hasCustomItem(player, customItem)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if player has a custom item
     */
    private boolean hasCustomItem(Player player, String customItemName) {
        // Parse the custom item format: "§c1x Weapon Handle" -> quantity=1, name="Weapon Handle"
        String cleanName = ChatColor.stripColor(customItemName);
        String[] parts = cleanName.split("x ", 2);
        if (parts.length != 2) return true; // Invalid format, skip

        int quantity;
        try {
            quantity = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return true; // Skip if we can't parse
        }
        String itemName = parts[1].trim();

        int found = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if (displayName.contains(itemName)) {
                    found += item.getAmount();
                }
            }
        }

        return found >= quantity;
    }

    /**
     * Consume materials and give weapon
     */
    public void craftWeapon(Player player) {
        if (!hasRequiredMaterials(player)) {
            player.sendMessage("§cYou don't have the required materials!");
            return;
        }

        Map<Material, Integer> costs = CRAFTING_COSTS.get(weapon);

        // Remove standard materials
        if (costs != null) {
            for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
                removeItems(player, entry.getKey(), entry.getValue());
            }
        }

        // Remove custom items
        List<String> customItems = CUSTOM_ITEMS.get(weapon);
        if (customItems != null) {
            for (String customItem : customItems) {
                removeCustomItem(player, customItem);
            }
        }

        // Give the weapon
        ItemStack weaponItem = weapon.createItem();
        player.getInventory().addItem(weaponItem);

        // Effects
        World world = player.getWorld();
        Location loc = location.clone().add(0.5, 3, 0.5);

        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 100, 0.5, 1, 0.5, 0.2);
        world.spawnParticle(Particle.ENCHANT, loc, 200, 1, 2, 1, 0.5);

        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l✦ §e" + player.getName() + " §6has forged the " +
            weapon.getColor() + "§l" + weapon.getName() + "§6! §l✦");
        Bukkit.broadcastMessage("");

        player.sendMessage("§a§lWeapon forged! §7You received " + weapon.getColor() + "§l" + weapon.getName() + "§7!");
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int toRemove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - toRemove);
                remaining -= toRemove;
            }
        }
    }

    private void removeCustomItem(Player player, String customItemName) {
        String cleanName = ChatColor.stripColor(customItemName);
        String[] parts = cleanName.split("x ", 2);
        if (parts.length != 2) return;

        int quantity;
        try {
            quantity = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return;
        }
        String itemName = parts[1].trim();

        int remaining = quantity;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if (displayName.contains(itemName)) {
                    int toRemove = Math.min(item.getAmount(), remaining);
                    item.setAmount(item.getAmount() - toRemove);
                    remaining -= toRemove;
                }
            }
        }
    }

    /**
     * Remove the altar
     */
    public void remove() {
        active = false;

        // Remove all display entities
        for (Entity entity : displayEntities) {
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
        displayEntities.clear();

        // Remove protected blocks (including Ancient Debris)
        for (Location loc : protectedBlocks) {
            if (loc.getWorld() != null) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        protectedBlocks.clear();
    }

    public Location getLocation() {
        return location.clone();
    }

    public AttributeWeapon getWeapon() {
        return weapon;
    }

    public boolean isActive() {
        return active;
    }

    private static String formatMaterialName(Material material) {
        String name = material.name().replace("_", " ");
        StringBuilder result = new StringBuilder();
        for (String word : name.toLowerCase().split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Create a Weapon Handle item
     */
    public static ItemStack createWeaponHandle() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lWeapon Handle");
        List<String> lore = new ArrayList<>();
        lore.add("§7A sturdy handle for forging");
        lore.add("§7powerful attribute weapons.");
        lore.add("");
        lore.add("§8Obtained from crafting");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a Warden's Heart item
     */
    public static ItemStack createWardensHeart() {
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2§lWarden's Heart");
        List<String> lore = new ArrayList<>();
        lore.add("§7The pulsing core of a Warden.");
        lore.add("§7Resonates with ancient power.");
        lore.add("");
        lore.add("§8Dropped by Ascended Warden");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a Wither Bone item
     */
    public static ItemStack createWitherBone() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5§lWither Bone");
        List<String> lore = new ArrayList<>();
        lore.add("§7A bone infused with wither essence");
        lore.add("§7Grants the §5Wither §7attribute");
        lore.add("");
        lore.add("§c§lCANNOT BE DROPPED OR STORED");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a Breeze Heart item
     */
    public static ItemStack createBreezeHeart() {
        ItemStack item = new ItemStack(Material.WIND_CHARGE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lBreeze Heart");
        List<String> lore = new ArrayList<>();
        lore.add("§7A heart swirling with wind energy");
        lore.add("§7Grants the §bBreeze §7attribute");
        lore.add("");
        lore.add("§c§lCANNOT BE DROPPED OR STORED");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a Dragon Heart item
     */
    public static ItemStack createDragonHeart() {
        ItemStack item = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5§lDragon Heart");
        List<String> lore = new ArrayList<>();
        lore.add("§7The crystallized essence of an");
        lore.add("§7Ascended Ender Dragon.");
        lore.add("");
        lore.add("§8Dropped by Ascended Ender Dragon");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get crafting requirements as formatted strings
     */
    public static List<String> getCraftingRequirements(AttributeWeapon weapon) {
        List<String> requirements = new ArrayList<>();

        Map<Material, Integer> costs = CRAFTING_COSTS.get(weapon);
        if (costs != null) {
            for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
                requirements.add("§f" + entry.getValue() + "x §7" + formatMaterialName(entry.getKey()));
            }
        }

        List<String> customItems = CUSTOM_ITEMS.get(weapon);
        if (customItems != null) {
            requirements.addAll(customItems);
        }

        return requirements;
    }

    /**
     * Initialize crafting costs for all weapons
     */
    private static void initCraftingCosts() {
        // Standard Attribute Weapons - moderate costs

        // Breaker Blade (Melee)
        Map<Material, Integer> breakerCost = new LinkedHashMap<>();
        breakerCost.put(Material.NETHERITE_INGOT, 4);
        breakerCost.put(Material.DIAMOND_BLOCK, 8);
        breakerCost.put(Material.IRON_BLOCK, 32);
        CRAFTING_COSTS.put(AttributeWeapon.BREAKER_BLADE, breakerCost);
        CUSTOM_ITEMS.put(AttributeWeapon.BREAKER_BLADE, Arrays.asList("§c1x §lWeapon Handle"));

        // Crimson Fang (Health)
        Map<Material, Integer> crimsonCost = new LinkedHashMap<>();
        crimsonCost.put(Material.NETHERITE_INGOT, 4);
        crimsonCost.put(Material.DIAMOND_BLOCK, 8);
        crimsonCost.put(Material.GOLDEN_APPLE, 16);
        crimsonCost.put(Material.REDSTONE_BLOCK, 32);
        CRAFTING_COSTS.put(AttributeWeapon.CRIMSON_FANG, crimsonCost);
        CUSTOM_ITEMS.put(AttributeWeapon.CRIMSON_FANG, Arrays.asList("§c1x §lWeapon Handle"));

        // Bulwark Mace (Defense)
        Map<Material, Integer> bulwarkCost = new LinkedHashMap<>();
        bulwarkCost.put(Material.NETHERITE_INGOT, 6);
        bulwarkCost.put(Material.DIAMOND_BLOCK, 12);
        bulwarkCost.put(Material.IRON_BLOCK, 64);
        bulwarkCost.put(Material.OBSIDIAN, 32);
        CRAFTING_COSTS.put(AttributeWeapon.BULWARK_MACE, bulwarkCost);
        CUSTOM_ITEMS.put(AttributeWeapon.BULWARK_MACE, Arrays.asList("§c1x §lWeapon Handle"));

        // Gilded Cleaver (Wealth)
        Map<Material, Integer> gildedCost = new LinkedHashMap<>();
        gildedCost.put(Material.NETHERITE_INGOT, 4);
        gildedCost.put(Material.GOLD_BLOCK, 64);
        gildedCost.put(Material.EMERALD_BLOCK, 32);
        gildedCost.put(Material.DIAMOND_BLOCK, 8);
        CRAFTING_COSTS.put(AttributeWeapon.GILDED_CLEAVER, gildedCost);
        CUSTOM_ITEMS.put(AttributeWeapon.GILDED_CLEAVER, Arrays.asList("§c1x §lWeapon Handle"));

        // Lockspike (Control)
        Map<Material, Integer> lockspikeCost = new LinkedHashMap<>();
        lockspikeCost.put(Material.IRON_BLOCK, 32);
        lockspikeCost.put(Material.DIAMOND_BLOCK, 4);
        lockspikeCost.put(Material.CHAIN, 64);
        lockspikeCost.put(Material.COBWEB, 32);
        CRAFTING_COSTS.put(AttributeWeapon.LOCKSPIKE, lockspikeCost);
        CUSTOM_ITEMS.put(AttributeWeapon.LOCKSPIKE, Arrays.asList("§c1x §lWeapon Handle"));

        // Windcaller Pike (Range)
        Map<Material, Integer> windcallerCost = new LinkedHashMap<>();
        windcallerCost.put(Material.NETHERITE_INGOT, 4);
        windcallerCost.put(Material.DIAMOND_BLOCK, 8);
        windcallerCost.put(Material.PRISMARINE_SHARD, 64);
        windcallerCost.put(Material.FEATHER, 64);
        CRAFTING_COSTS.put(AttributeWeapon.WINDCALLER_PIKE, windcallerCost);
        CUSTOM_ITEMS.put(AttributeWeapon.WINDCALLER_PIKE, Arrays.asList("§c1x §lWeapon Handle"));

        // Chrono Saber (Tempo)
        Map<Material, Integer> chronoCost = new LinkedHashMap<>();
        chronoCost.put(Material.NETHERITE_INGOT, 4);
        chronoCost.put(Material.GOLD_BLOCK, 32);
        chronoCost.put(Material.CLOCK, 16);
        chronoCost.put(Material.DIAMOND_BLOCK, 8);
        CRAFTING_COSTS.put(AttributeWeapon.CHRONO_SABER, chronoCost);
        CUSTOM_ITEMS.put(AttributeWeapon.CHRONO_SABER, Arrays.asList("§c1x §lWeapon Handle"));

        // Watcher's Blade (Vision)
        Map<Material, Integer> watcherCost = new LinkedHashMap<>();
        watcherCost.put(Material.NETHERITE_INGOT, 4);
        watcherCost.put(Material.DIAMOND_BLOCK, 8);
        watcherCost.put(Material.ENDER_EYE, 16);
        watcherCost.put(Material.GLOWSTONE, 32);
        CRAFTING_COSTS.put(AttributeWeapon.WATCHERS_BLADE, watcherCost);
        CUSTOM_ITEMS.put(AttributeWeapon.WATCHERS_BLADE, Arrays.asList("§c1x §lWeapon Handle"));

        // Painbound Greatsword (Persistence)
        Map<Material, Integer> painboundCost = new LinkedHashMap<>();
        painboundCost.put(Material.NETHERITE_INGOT, 8);
        painboundCost.put(Material.DIAMOND_BLOCK, 16);
        painboundCost.put(Material.SOUL_SAND, 64);
        painboundCost.put(Material.WITHER_SKELETON_SKULL, 3);
        CRAFTING_COSTS.put(AttributeWeapon.PAINBOUND_GREATSWORD, painboundCost);
        CUSTOM_ITEMS.put(AttributeWeapon.PAINBOUND_GREATSWORD, Arrays.asList("§c1x §lWeapon Handle"));

        // Mirror Edge (Transfer)
        Map<Material, Integer> mirrorCost = new LinkedHashMap<>();
        mirrorCost.put(Material.DIAMOND_BLOCK, 16);
        mirrorCost.put(Material.AMETHYST_BLOCK, 32);
        mirrorCost.put(Material.GLASS, 64);
        mirrorCost.put(Material.PRISMARINE_CRYSTALS, 32);
        CRAFTING_COSTS.put(AttributeWeapon.MIRROR_EDGE, mirrorCost);
        CUSTOM_ITEMS.put(AttributeWeapon.MIRROR_EDGE, Arrays.asList("§c1x §lWeapon Handle"));

        // Flashsteel Dagger (Speed)
        Map<Material, Integer> flashsteelCost = new LinkedHashMap<>();
        flashsteelCost.put(Material.IRON_BLOCK, 32);
        flashsteelCost.put(Material.DIAMOND_BLOCK, 4);
        flashsteelCost.put(Material.SUGAR, 64);
        flashsteelCost.put(Material.FEATHER, 32);
        CRAFTING_COSTS.put(AttributeWeapon.FLASHSTEEL_DAGGER, flashsteelCost);
        CUSTOM_ITEMS.put(AttributeWeapon.FLASHSTEEL_DAGGER, Arrays.asList("§c1x §lWeapon Handle"));

        // Bonecrusher (Pressure)
        Map<Material, Integer> bonecrusherCost = new LinkedHashMap<>();
        bonecrusherCost.put(Material.NETHERITE_INGOT, 6);
        bonecrusherCost.put(Material.DIAMOND_BLOCK, 12);
        bonecrusherCost.put(Material.BONE_BLOCK, 64);
        bonecrusherCost.put(Material.SKELETON_SKULL, 6);
        CRAFTING_COSTS.put(AttributeWeapon.BONECRUSHER, bonecrusherCost);
        CUSTOM_ITEMS.put(AttributeWeapon.BONECRUSHER, Arrays.asList("§c1x §lWeapon Handle"));

        // Fracture Rod (Disruption)
        Map<Material, Integer> fractureCost = new LinkedHashMap<>();
        fractureCost.put(Material.NETHERITE_INGOT, 4);
        fractureCost.put(Material.DIAMOND_BLOCK, 8);
        fractureCost.put(Material.BLAZE_ROD, 32);
        fractureCost.put(Material.END_CRYSTAL, 4);
        CRAFTING_COSTS.put(AttributeWeapon.FRACTURE_ROD, fractureCost);
        CUSTOM_ITEMS.put(AttributeWeapon.FRACTURE_ROD, Arrays.asList("§c1x §lWeapon Handle"));

        // Ironroot Halberd (Anchor)
        Map<Material, Integer> ironrootCost = new LinkedHashMap<>();
        ironrootCost.put(Material.NETHERITE_INGOT, 6);
        ironrootCost.put(Material.IRON_BLOCK, 64);
        ironrootCost.put(Material.COPPER_BLOCK, 64);
        ironrootCost.put(Material.DIAMOND_BLOCK, 8);
        CRAFTING_COSTS.put(AttributeWeapon.IRONROOT_HALBERD, ironrootCost);
        CUSTOM_ITEMS.put(AttributeWeapon.IRONROOT_HALBERD, Arrays.asList("§c1x §lWeapon Handle"));

        // High Roller Blade (Risk)
        Map<Material, Integer> highrollerCost = new LinkedHashMap<>();
        highrollerCost.put(Material.GOLD_BLOCK, 64);
        highrollerCost.put(Material.DIAMOND_BLOCK, 8);
        highrollerCost.put(Material.EMERALD_BLOCK, 16);
        highrollerCost.put(Material.LAPIS_BLOCK, 32);
        CRAFTING_COSTS.put(AttributeWeapon.HIGH_ROLLER_BLADE, highrollerCost);
        CUSTOM_ITEMS.put(AttributeWeapon.HIGH_ROLLER_BLADE, Arrays.asList("§c1x §lWeapon Handle"));

        // ===== BOSS WEAPONS - Much higher costs =====

        // Despair Reaver (Wither)
        Map<Material, Integer> despairCost = new LinkedHashMap<>();
        despairCost.put(Material.NETHERITE_BLOCK, 4);
        despairCost.put(Material.DIAMOND_BLOCK, 32);
        despairCost.put(Material.WITHER_SKELETON_SKULL, 6);
        despairCost.put(Material.SOUL_SAND, 64);
        despairCost.put(Material.NETHER_STAR, 1);
        CRAFTING_COSTS.put(AttributeWeapon.DESPAIR_REAVER, despairCost);
        CUSTOM_ITEMS.put(AttributeWeapon.DESPAIR_REAVER, Arrays.asList("§c1x §lWeapon Handle", "§8§l1x Wither Bone"));

        // Deepcore Maul (Warden) - Based on the image
        Map<Material, Integer> deepcoreCost = new LinkedHashMap<>();
        deepcoreCost.put(Material.BONE_BLOCK, 64);
        deepcoreCost.put(Material.IRON_BLOCK, 64);
        deepcoreCost.put(Material.COPPER_BLOCK, 64);
        deepcoreCost.put(Material.SKELETON_SKULL, 6);
        deepcoreCost.put(Material.WITHER_SKELETON_SKULL, 6);
        deepcoreCost.put(Material.PLAYER_HEAD, 3);
        CRAFTING_COSTS.put(AttributeWeapon.DEEPCORE_MAUL, deepcoreCost);
        CUSTOM_ITEMS.put(AttributeWeapon.DEEPCORE_MAUL, Arrays.asList("§c1x §lWeapon Handle", "§2§l1x Warden's Heart"));

        // Verdict Lance (Breeze)
        Map<Material, Integer> verdictCost = new LinkedHashMap<>();
        verdictCost.put(Material.NETHERITE_BLOCK, 2);
        verdictCost.put(Material.DIAMOND_BLOCK, 24);
        verdictCost.put(Material.PRISMARINE_SHARD, 64);
        verdictCost.put(Material.FEATHER, 64);
        verdictCost.put(Material.PHANTOM_MEMBRANE, 32);
        CRAFTING_COSTS.put(AttributeWeapon.VERDICT_LANCE, verdictCost);
        CUSTOM_ITEMS.put(AttributeWeapon.VERDICT_LANCE, Arrays.asList("§c1x §lWeapon Handle", "§b§l1x Breeze Heart"));

        // Dominion Blade (Dragon Egg)
        Map<Material, Integer> dominionCost = new LinkedHashMap<>();
        dominionCost.put(Material.NETHERITE_BLOCK, 8);
        dominionCost.put(Material.DIAMOND_BLOCK, 64);
        dominionCost.put(Material.END_CRYSTAL, 8);
        dominionCost.put(Material.ENDER_EYE, 32);
        dominionCost.put(Material.DRAGON_BREATH, 64);
        CRAFTING_COSTS.put(AttributeWeapon.DOMINION_BLADE, dominionCost);
        CUSTOM_ITEMS.put(AttributeWeapon.DOMINION_BLADE, Arrays.asList("§c1x §lWeapon Handle", "§5§l1x Dragon Heart"));
    }
}
