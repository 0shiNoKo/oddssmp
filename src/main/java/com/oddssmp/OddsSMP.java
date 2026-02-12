package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OddsSMP extends JavaPlugin {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private AbilityManager abilityManager;
    private CommandHandler commandHandler;
    private EventListener eventListener;
    private AdminGUI adminGUI;
    private WeaponGUI weaponGUI;
    private GUIListener guiListener;
    private AttributeSettings attributeSettings;
    private CombatLogger combatLogger;

    // Weapon altars
    private final List<WeaponAltar> activeAltars = new ArrayList<>();

    // Auto-assign settings
    private boolean autoAssignEnabled = false;
    private int autoAssignDelaySeconds = 10; // Default 10 seconds

    // Gameplay settings
    private boolean levelLossOnDeath = true;
    private boolean levelGainOnKill = true;
    private boolean particleEffectsEnabled = true;
    private boolean pvpOnlyAbilities = false; // Abilities only work against players
    private boolean friendlyFire = true; // Can abilities affect teammates
    private int maxLevel = 5;
    private int levelsLostOnDeath = 1;
    private int levelsGainedOnKill = 1;
    private boolean killStreakBonuses = false;
    private int killStreakThreshold = 3; // Kills needed for bonus

    // Broadcast settings
    private boolean broadcastAttributeAssign = true;
    private boolean broadcastLevelUp = false;
    private boolean broadcastDragonEgg = true;
    private boolean broadcastBossSpawn = true;
    private boolean broadcastBossDefeat = true;

    // Boss settings
    private double bossHealthMultiplier = 1.0;
    private double bossDamageMultiplier = 1.0;
    private double bossDropRateMultiplier = 1.0;

    // Passive settings
    private double passiveTickRate = 1.0; // Seconds between passive ticks
    private double passiveEffectStrength = 1.0;

    // Combat settings
    private double pvpDamageMultiplier = 1.0;
    private double abilityDamageMultiplier = 1.0;
    private boolean combatTagEnabled = true;
    private int combatTagDuration = 15; // Seconds

    // Death settings
    private boolean keepInventoryOnDeath = false;
    private boolean dropAttributeItemsOnDeath = true;

    // Data file
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        getLogger().info("OddsSMP Plugin Enabled!");

        // Create data folder and file
        setupDataFile();

        // Load all saved player data
        loadAllPlayerData();

        // Initialize managers
        attributeSettings = new AttributeSettings(this);
        combatLogger = new CombatLogger(this);
        abilityManager = new AbilityManager(this);
        commandHandler = new CommandHandler(this);
        eventListener = new EventListener(this);
        adminGUI = new AdminGUI(this);
        weaponGUI = new WeaponGUI(this);
        guiListener = new GUIListener(this, adminGUI, weaponGUI);

        // Register commands
        getCommand("smp").setExecutor(commandHandler);
        getCommand("smp").setTabCompleter(commandHandler);
        getCommand("admin").setExecutor(commandHandler);
        getCommand("admin").setTabCompleter(commandHandler);

        // Register events
        Bukkit.getPluginManager().registerEvents(eventListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        // Register custom recipes
        registerRecipes();

        // Start passive ability tick
        startPassiveTicker();

        // Load player data for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!playerDataMap.containsKey(player.getUniqueId())) {
                playerDataMap.put(player.getUniqueId(), new PlayerData());
            }
            updatePlayerTab(player);
        }

        getLogger().info("OddsSMP loaded with " + AttributeType.values().length + " attributes!");
    }

    @Override
    public void onDisable() {
        getLogger().info("OddsSMP Plugin Disabled!");

        // Save all player data to file
        saveAllPlayerData();

        // Save attribute settings
        if (attributeSettings != null) {
            attributeSettings.saveConfig();
        }

        // Shutdown combat logger
        if (combatLogger != null) {
            combatLogger.shutdown();
        }

        // Remove all active altars
        for (WeaponAltar altar : activeAltars) {
            altar.remove();
        }
        activeAltars.clear();
    }

    /**
     * Setup data file for persistence
     */
    private void setupDataFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        dataFile = new File(getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create playerdata.yml: " + e.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Load all player data from file
     */
    private void loadAllPlayerData() {
        if (dataConfig == null) return;

        for (String uuidStr : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String attrName = dataConfig.getString(uuidStr + ".attribute");
                String tierName = dataConfig.getString(uuidStr + ".tier");
                int level = dataConfig.getInt(uuidStr + ".level", 1);
                int kills = dataConfig.getInt(uuidStr + ".kills", 0);
                int deaths = dataConfig.getInt(uuidStr + ".deaths", 0);

                PlayerData data = new PlayerData();

                if (attrName != null && !attrName.isEmpty()) {
                    try {
                        AttributeType attr = AttributeType.valueOf(attrName);
                        Tier tier = tierName != null ? Tier.valueOf(tierName) : Tier.STABLE;
                        data = new PlayerData(attr, tier);
                        data.setLevel(level);
                        for (int i = 0; i < kills; i++) data.incrementKills();
                        for (int i = 0; i < deaths; i++) data.incrementDeaths();
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Invalid attribute/tier for " + uuidStr + ": " + attrName);
                    }
                }

                playerDataMap.put(uuid, data);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID in playerdata: " + uuidStr);
            }
        }

        getLogger().info("Loaded " + playerDataMap.size() + " player data entries.");
    }

    /**
     * Save all player data to file
     */
    private void saveAllPlayerData() {
        if (dataConfig == null || dataFile == null) return;

        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();

            String path = uuid.toString();

            if (data.getAttribute() != null) {
                dataConfig.set(path + ".attribute", data.getAttribute().name());
                dataConfig.set(path + ".tier", data.getTier().name());
                dataConfig.set(path + ".level", data.getLevel());
                dataConfig.set(path + ".kills", data.getKills());
                dataConfig.set(path + ".deaths", data.getDeaths());
            } else {
                // Clear the entry if no attribute
                dataConfig.set(path, null);
            }
        }

        try {
            dataConfig.save(dataFile);
            getLogger().info("Saved " + playerDataMap.size() + " player data entries.");
        } catch (IOException e) {
            getLogger().severe("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    /**
     * Register custom crafting recipes
     */
    private void registerRecipes() {
        // Upgrader Recipe: 4 Netherite Ingots, 4 Diamond Blocks, Wither Skull in middle
        ItemStack upgrader = createUpgrader();
        NamespacedKey upgraderKey = new NamespacedKey(this, "upgrader");
        ShapedRecipe upgraderRecipe = new ShapedRecipe(upgraderKey, upgrader);
        upgraderRecipe.shape("NDN", "DWD", "NDN");
        upgraderRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        upgraderRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        upgraderRecipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        Bukkit.addRecipe(upgraderRecipe);

        // Reroller Recipe: 4 Netherite Ingots, 4 Diamond Blocks, Nether Star in middle
        ItemStack reroller = createReroller();
        NamespacedKey rerollerKey = new NamespacedKey(this, "reroller");
        ShapedRecipe rerollerRecipe = new ShapedRecipe(rerollerKey, reroller);
        rerollerRecipe.shape("NDN", "DSN", "NDN");
        rerollerRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        rerollerRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        rerollerRecipe.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(rerollerRecipe);

        getLogger().info("Registered custom recipes: Upgrader, Reroller");
    }

    /**
     * Create Upgrader item
     */
    public static ItemStack createUpgrader() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lAttribute Upgrader");
        List<String> lore = new ArrayList<>();
        lore.add("§7Right-click to upgrade your");
        lore.add("§7attribute by one level!");
        lore.add("");
        lore.add("§e§oMax level: 5");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create Reroller item
     */
    public static ItemStack createReroller() {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§lAttribute Reroller");
        List<String> lore = new ArrayList<>();
        lore.add("§7Right-click to reroll your");
        lore.add("§7attribute to a new random one!");
        lore.add("");
        lore.add("§c§oWarning: Resets level to 1");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Check if item is an Upgrader
     */
    public static boolean isUpgrader(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) return false;
        if (!item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.contains("Attribute Upgrader");
    }

    /**
     * Check if item is a Reroller
     */
    public static boolean isReroller(ItemStack item) {
        if (item == null || item.getType() != Material.END_CRYSTAL) return false;
        if (!item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.contains("Attribute Reroller");
    }

    /**
     * Start passive ability ticker (runs every second)
     */
    private void startPassiveTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    abilityManager.handlePassiveEffects(player);
                    updateActionBar(player);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // Every second
    }

    /**
     * Update action bar with cooldown and attribute info
     */
    private void updateActionBar(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            return;
        }

        // Get cooldown info
        long supportCd = data.getRemainingCooldown("support") / 1000;
        long meleeCd = data.getRemainingCooldown("melee") / 1000;

        // Build action bar message
        StringBuilder message = new StringBuilder();

        // Attribute name with icon
        message.append(data.getTier().getColor())
                .append(data.getAttribute().getIcon())
                .append(" ")
                .append(data.getAttribute().getDisplayName())
                .append(" §7Lv.")
                .append(data.getLevel())
                .append(" §8| ");

        // Support cooldown
        if (supportCd > 0) {
            message.append("§a§lSupport: §c").append(supportCd).append("s");
        } else {
            message.append("§a§lSupport: §a✓");
        }

        message.append(" §8| ");

        // Melee cooldown
        if (meleeCd > 0) {
            message.append("§c§lMelee: §c").append(meleeCd).append("s");
        } else {
            message.append("§c§lMelee: §a✓");
        }

        // Send action bar
        player.sendActionBar(net.kyori.adventure.text.Component.text(message.toString()));
    }

    /**
     * Get player data
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    /**
     * Set player data and save to file
     */
    public void setPlayerData(UUID uuid, PlayerData data) {
        playerDataMap.put(uuid, data);
        savePlayerData(uuid); // Auto-save when data changes
    }

    /**
     * Save player data to file
     */
    public void savePlayerData(UUID uuid) {
        if (dataConfig == null || dataFile == null) return;

        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        String path = uuid.toString();

        if (data.getAttribute() != null) {
            dataConfig.set(path + ".attribute", data.getAttribute().name());
            dataConfig.set(path + ".tier", data.getTier().name());
            dataConfig.set(path + ".level", data.getLevel());
            dataConfig.set(path + ".kills", data.getKills());
            dataConfig.set(path + ".deaths", data.getDeaths());
        } else {
            dataConfig.set(path, null);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save player data for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Load player data from file
     */
    public void loadPlayerData(UUID uuid) {
        if (dataConfig == null) {
            if (!playerDataMap.containsKey(uuid)) {
                playerDataMap.put(uuid, new PlayerData());
            }
            return;
        }

        String path = uuid.toString();
        if (dataConfig.contains(path)) {
            String attrName = dataConfig.getString(path + ".attribute");
            String tierName = dataConfig.getString(path + ".tier");
            int level = dataConfig.getInt(path + ".level", 1);
            int kills = dataConfig.getInt(path + ".kills", 0);
            int deaths = dataConfig.getInt(path + ".deaths", 0);

            if (attrName != null && !attrName.isEmpty()) {
                try {
                    AttributeType attr = AttributeType.valueOf(attrName);
                    Tier tier = tierName != null ? Tier.valueOf(tierName) : Tier.STABLE;
                    PlayerData data = new PlayerData(attr, tier);
                    data.setLevel(level);
                    for (int i = 0; i < kills; i++) data.incrementKills();
                    for (int i = 0; i < deaths; i++) data.incrementDeaths();
                    playerDataMap.put(uuid, data);
                    return;
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid attribute for " + uuid + ": " + attrName);
                }
            }
        }

        // No saved data, create new
        if (!playerDataMap.containsKey(uuid)) {
            playerDataMap.put(uuid, new PlayerData());
        }
    }

    /**
     * Update player's tab display
     */
    public void updatePlayerTab(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());

        if (data == null || data.getAttribute() == null) {
            // No attribute, use default name
            player.setPlayerListName(player.getName());
            return;
        }

        // Format: [ICON] PlayerName with tier color
        String icon = data.getAttribute().getIcon();
        String color = data.getTier().getColor().toString();
        String name = player.getName();

        // Add level indicator (stars)
        String levelStars = "★".repeat(data.getLevel());

        player.setPlayerListName(color + "[" + icon + "] " + name + " " + levelStars);
    }

    /**
     * Get ability manager
     */
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    /**
     * Get command handler
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * Get event listener
     */
    public EventListener getEventListener() {
        return eventListener;
    }

    /**
     * Get admin GUI
     */
    public AdminGUI getAdminGUI() {
        return adminGUI;
    }

    /**
     * Get weapon GUI
     */
    public WeaponGUI getWeaponGUI() {
        return weaponGUI;
    }

    /**
     * Get attribute settings
     */
    public AttributeSettings getAttributeSettings() {
        return attributeSettings;
    }

    /**
     * Get combat logger
     */
    public CombatLogger getCombatLogger() {
        return combatLogger;
    }

    /**
     * Check if auto-assign is enabled
     */
    public boolean isAutoAssignEnabled() {
        return autoAssignEnabled;
    }

    /**
     * Set auto-assign enabled
     */
    public void setAutoAssignEnabled(boolean enabled) {
        this.autoAssignEnabled = enabled;
    }

    /**
     * Get auto-assign delay in seconds
     */
    public int getAutoAssignDelaySeconds() {
        return autoAssignDelaySeconds;
    }

    /**
     * Set auto-assign delay in seconds
     */
    public void setAutoAssignDelaySeconds(int seconds) {
        this.autoAssignDelaySeconds = seconds;
    }

    /**
     * Check if level loss on death is enabled
     */
    public boolean isLevelLossOnDeath() {
        return levelLossOnDeath;
    }

    /**
     * Set level loss on death
     */
    public void setLevelLossOnDeath(boolean enabled) {
        this.levelLossOnDeath = enabled;
    }

    /**
     * Check if level gain on kill is enabled
     */
    public boolean isLevelGainOnKill() {
        return levelGainOnKill;
    }

    /**
     * Set level gain on kill
     */
    public void setLevelGainOnKill(boolean enabled) {
        this.levelGainOnKill = enabled;
    }

    /**
     * Check if particle effects are enabled
     */
    public boolean isParticleEffectsEnabled() {
        return particleEffectsEnabled;
    }

    /**
     * Set particle effects enabled
     */
    public void setParticleEffectsEnabled(boolean enabled) {
        this.particleEffectsEnabled = enabled;
    }

    // PvP Only Abilities
    public boolean isPvpOnlyAbilities() { return pvpOnlyAbilities; }
    public void setPvpOnlyAbilities(boolean enabled) { this.pvpOnlyAbilities = enabled; }

    // Friendly Fire
    public boolean isFriendlyFire() { return friendlyFire; }
    public void setFriendlyFire(boolean enabled) { this.friendlyFire = enabled; }

    // Max Level
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int level) { this.maxLevel = Math.max(1, Math.min(10, level)); }

    // Levels Lost on Death
    public int getLevelsLostOnDeath() { return levelsLostOnDeath; }
    public void setLevelsLostOnDeath(int levels) { this.levelsLostOnDeath = Math.max(0, Math.min(5, levels)); }

    // Levels Gained on Kill
    public int getLevelsGainedOnKill() { return levelsGainedOnKill; }
    public void setLevelsGainedOnKill(int levels) { this.levelsGainedOnKill = Math.max(0, Math.min(5, levels)); }

    // Kill Streak Bonuses
    public boolean isKillStreakBonuses() { return killStreakBonuses; }
    public void setKillStreakBonuses(boolean enabled) { this.killStreakBonuses = enabled; }

    // Kill Streak Threshold
    public int getKillStreakThreshold() { return killStreakThreshold; }
    public void setKillStreakThreshold(int threshold) { this.killStreakThreshold = Math.max(2, Math.min(10, threshold)); }

    // Broadcast Settings
    public boolean isBroadcastAttributeAssign() { return broadcastAttributeAssign; }
    public void setBroadcastAttributeAssign(boolean enabled) { this.broadcastAttributeAssign = enabled; }

    public boolean isBroadcastLevelUp() { return broadcastLevelUp; }
    public void setBroadcastLevelUp(boolean enabled) { this.broadcastLevelUp = enabled; }

    public boolean isBroadcastDragonEgg() { return broadcastDragonEgg; }
    public void setBroadcastDragonEgg(boolean enabled) { this.broadcastDragonEgg = enabled; }

    public boolean isBroadcastBossSpawn() { return broadcastBossSpawn; }
    public void setBroadcastBossSpawn(boolean enabled) { this.broadcastBossSpawn = enabled; }

    public boolean isBroadcastBossDefeat() { return broadcastBossDefeat; }
    public void setBroadcastBossDefeat(boolean enabled) { this.broadcastBossDefeat = enabled; }

    // Boss Settings
    public double getBossHealthMultiplier() { return bossHealthMultiplier; }
    public void setBossHealthMultiplier(double mult) { this.bossHealthMultiplier = Math.max(0.1, Math.min(10.0, mult)); }

    public double getBossDamageMultiplier() { return bossDamageMultiplier; }
    public void setBossDamageMultiplier(double mult) { this.bossDamageMultiplier = Math.max(0.1, Math.min(10.0, mult)); }

    public double getBossDropRateMultiplier() { return bossDropRateMultiplier; }
    public void setBossDropRateMultiplier(double mult) { this.bossDropRateMultiplier = Math.max(0.1, Math.min(10.0, mult)); }

    // Passive Settings
    public double getPassiveTickRate() { return passiveTickRate; }
    public void setPassiveTickRate(double rate) { this.passiveTickRate = Math.max(0.5, Math.min(5.0, rate)); }

    public double getPassiveEffectStrength() { return passiveEffectStrength; }
    public void setPassiveEffectStrength(double strength) { this.passiveEffectStrength = Math.max(0.1, Math.min(5.0, strength)); }

    // Combat Settings
    public double getPvpDamageMultiplier() { return pvpDamageMultiplier; }
    public void setPvpDamageMultiplier(double mult) { this.pvpDamageMultiplier = Math.max(0.1, Math.min(5.0, mult)); }

    public double getAbilityDamageMultiplier() { return abilityDamageMultiplier; }
    public void setAbilityDamageMultiplier(double mult) { this.abilityDamageMultiplier = Math.max(0.1, Math.min(5.0, mult)); }

    public boolean isCombatTagEnabled() { return combatTagEnabled; }
    public void setCombatTagEnabled(boolean enabled) { this.combatTagEnabled = enabled; }

    public int getCombatTagDuration() { return combatTagDuration; }
    public void setCombatTagDuration(int duration) { this.combatTagDuration = Math.max(5, Math.min(60, duration)); }

    // Death Settings
    public boolean isKeepInventoryOnDeath() { return keepInventoryOnDeath; }
    public void setKeepInventoryOnDeath(boolean enabled) { this.keepInventoryOnDeath = enabled; }

    public boolean isDropAttributeItemsOnDeath() { return dropAttributeItemsOnDeath; }
    public void setDropAttributeItemsOnDeath(boolean enabled) { this.dropAttributeItemsOnDeath = enabled; }

    /**
     * Register a weapon altar
     */
    public void registerAltar(WeaponAltar altar) {
        activeAltars.add(altar);
    }

    /**
     * Get all active altars
     */
    public List<WeaponAltar> getActiveAltars() {
        return activeAltars;
    }

    /**
     * Find altar near location
     */
    public WeaponAltar findAltarNear(org.bukkit.Location location, double radius) {
        for (WeaponAltar altar : activeAltars) {
            if (altar.isActive() && altar.getLocation().distance(location) <= radius) {
                return altar;
            }
        }
        return null;
    }

    /**
     * Remove an altar
     */
    public void removeAltar(WeaponAltar altar) {
        altar.remove();
        activeAltars.remove(altar);
    }

    /**
     * Check if a block is protected by any altar
     */
    public boolean isAltarProtectedBlock(org.bukkit.Location blockLoc) {
        for (WeaponAltar altar : activeAltars) {
            if (altar.isActive() && altar.isProtectedBlock(blockLoc)) {
                return true;
            }
        }
        return false;
    }
}