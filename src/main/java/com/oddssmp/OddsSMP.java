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
    private boolean pvpOnlyAbilities = false; // Abilities only work against players

    // Comprehensive Particle Settings
    private boolean particleMasterEnabled = true; // Master toggle
    // Ability particles
    private boolean particleSupportAbility = true;
    private boolean particleMeleeAbility = true;
    private boolean particlePassiveAbility = true;
    // Combat particles
    private boolean particleDamageHit = true;
    private boolean particleCriticalHit = true;
    private boolean particleBlocking = true;
    private boolean particleHealing = true;
    private boolean particleKill = true;
    private boolean particleDeath = true;
    // Player event particles
    private boolean particleLevelUp = true;
    private boolean particleAttributeAssign = true;
    private boolean particleAttributeRemove = true;
    private boolean particleTierUp = true;
    // Boss particles
    private boolean particleBossAmbient = true;
    private boolean particleBossAbility = true;
    private boolean particleBossSpawn = true;
    private boolean particleBossDeath = true;
    // World particles
    private boolean particleAltarAmbient = true;
    private boolean particleAltarActivation = true;
    private boolean particleItemPickup = true;
    private boolean particleItemDrop = true;
    // Effect particles
    private boolean particleStatusEffect = true;
    private boolean particleBuffApplied = true;
    private boolean particleDebuffApplied = true;
    private boolean particlePotionEffect = true;
    // Special particles
    private boolean particleTeleport = true;
    private boolean particleRespawn = true;
    private boolean particleCombo = true;
    private boolean particleKillStreak = true;
    // Particle intensity
    private double particleIntensity = 1.0; // 0.25 to 2.0
    private int particleRenderDistance = 32; // blocks
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
     * Check if particle effects are enabled (master toggle + specific type)
     */
    public boolean isParticleEffectsEnabled() {
        return particleMasterEnabled;
    }

    /**
     * Set particle effects enabled (master toggle)
     */
    public void setParticleEffectsEnabled(boolean enabled) {
        this.particleMasterEnabled = enabled;
    }

    // ==================== COMPREHENSIVE PARTICLE SETTINGS ====================

    // Master toggle
    public boolean isParticleMasterEnabled() { return particleMasterEnabled; }
    public void setParticleMasterEnabled(boolean enabled) { this.particleMasterEnabled = enabled; }

    // Ability particles
    public boolean isParticleSupportAbility() { return particleMasterEnabled && particleSupportAbility; }
    public void setParticleSupportAbility(boolean enabled) { this.particleSupportAbility = enabled; }
    public boolean getParticleSupportAbilityRaw() { return particleSupportAbility; }

    public boolean isParticleMeleeAbility() { return particleMasterEnabled && particleMeleeAbility; }
    public void setParticleMeleeAbility(boolean enabled) { this.particleMeleeAbility = enabled; }
    public boolean getParticleMeleeAbilityRaw() { return particleMeleeAbility; }

    public boolean isParticlePassiveAbility() { return particleMasterEnabled && particlePassiveAbility; }
    public void setParticlePassiveAbility(boolean enabled) { this.particlePassiveAbility = enabled; }
    public boolean getParticlePassiveAbilityRaw() { return particlePassiveAbility; }

    // Combat particles
    public boolean isParticleDamageHit() { return particleMasterEnabled && particleDamageHit; }
    public void setParticleDamageHit(boolean enabled) { this.particleDamageHit = enabled; }
    public boolean getParticleDamageHitRaw() { return particleDamageHit; }

    public boolean isParticleCriticalHit() { return particleMasterEnabled && particleCriticalHit; }
    public void setParticleCriticalHit(boolean enabled) { this.particleCriticalHit = enabled; }
    public boolean getParticleCriticalHitRaw() { return particleCriticalHit; }

    public boolean isParticleBlocking() { return particleMasterEnabled && particleBlocking; }
    public void setParticleBlocking(boolean enabled) { this.particleBlocking = enabled; }
    public boolean getParticleBlockingRaw() { return particleBlocking; }

    public boolean isParticleHealing() { return particleMasterEnabled && particleHealing; }
    public void setParticleHealing(boolean enabled) { this.particleHealing = enabled; }
    public boolean getParticleHealingRaw() { return particleHealing; }

    public boolean isParticleKill() { return particleMasterEnabled && particleKill; }
    public void setParticleKill(boolean enabled) { this.particleKill = enabled; }
    public boolean getParticleKillRaw() { return particleKill; }

    public boolean isParticleDeath() { return particleMasterEnabled && particleDeath; }
    public void setParticleDeath(boolean enabled) { this.particleDeath = enabled; }
    public boolean getParticleDeathRaw() { return particleDeath; }

    // Player event particles
    public boolean isParticleLevelUp() { return particleMasterEnabled && particleLevelUp; }
    public void setParticleLevelUp(boolean enabled) { this.particleLevelUp = enabled; }
    public boolean getParticleLevelUpRaw() { return particleLevelUp; }

    public boolean isParticleAttributeAssign() { return particleMasterEnabled && particleAttributeAssign; }
    public void setParticleAttributeAssign(boolean enabled) { this.particleAttributeAssign = enabled; }
    public boolean getParticleAttributeAssignRaw() { return particleAttributeAssign; }

    public boolean isParticleAttributeRemove() { return particleMasterEnabled && particleAttributeRemove; }
    public void setParticleAttributeRemove(boolean enabled) { this.particleAttributeRemove = enabled; }
    public boolean getParticleAttributeRemoveRaw() { return particleAttributeRemove; }

    public boolean isParticleTierUp() { return particleMasterEnabled && particleTierUp; }
    public void setParticleTierUp(boolean enabled) { this.particleTierUp = enabled; }
    public boolean getParticleTierUpRaw() { return particleTierUp; }

    // Boss particles
    public boolean isParticleBossAmbient() { return particleMasterEnabled && particleBossAmbient; }
    public void setParticleBossAmbient(boolean enabled) { this.particleBossAmbient = enabled; }
    public boolean getParticleBossAmbientRaw() { return particleBossAmbient; }

    public boolean isParticleBossAbility() { return particleMasterEnabled && particleBossAbility; }
    public void setParticleBossAbility(boolean enabled) { this.particleBossAbility = enabled; }
    public boolean getParticleBossAbilityRaw() { return particleBossAbility; }

    public boolean isParticleBossSpawn() { return particleMasterEnabled && particleBossSpawn; }
    public void setParticleBossSpawn(boolean enabled) { this.particleBossSpawn = enabled; }
    public boolean getParticleBossSpawnRaw() { return particleBossSpawn; }

    public boolean isParticleBossDeath() { return particleMasterEnabled && particleBossDeath; }
    public void setParticleBossDeath(boolean enabled) { this.particleBossDeath = enabled; }
    public boolean getParticleBossDeathRaw() { return particleBossDeath; }

    // World particles
    public boolean isParticleAltarAmbient() { return particleMasterEnabled && particleAltarAmbient; }
    public void setParticleAltarAmbient(boolean enabled) { this.particleAltarAmbient = enabled; }
    public boolean getParticleAltarAmbientRaw() { return particleAltarAmbient; }

    public boolean isParticleAltarActivation() { return particleMasterEnabled && particleAltarActivation; }
    public void setParticleAltarActivation(boolean enabled) { this.particleAltarActivation = enabled; }
    public boolean getParticleAltarActivationRaw() { return particleAltarActivation; }

    public boolean isParticleItemPickup() { return particleMasterEnabled && particleItemPickup; }
    public void setParticleItemPickup(boolean enabled) { this.particleItemPickup = enabled; }
    public boolean getParticleItemPickupRaw() { return particleItemPickup; }

    public boolean isParticleItemDrop() { return particleMasterEnabled && particleItemDrop; }
    public void setParticleItemDrop(boolean enabled) { this.particleItemDrop = enabled; }
    public boolean getParticleItemDropRaw() { return particleItemDrop; }

    // Effect particles
    public boolean isParticleStatusEffect() { return particleMasterEnabled && particleStatusEffect; }
    public void setParticleStatusEffect(boolean enabled) { this.particleStatusEffect = enabled; }
    public boolean getParticleStatusEffectRaw() { return particleStatusEffect; }

    public boolean isParticleBuffApplied() { return particleMasterEnabled && particleBuffApplied; }
    public void setParticleBuffApplied(boolean enabled) { this.particleBuffApplied = enabled; }
    public boolean getParticleBuffAppliedRaw() { return particleBuffApplied; }

    public boolean isParticleDebuffApplied() { return particleMasterEnabled && particleDebuffApplied; }
    public void setParticleDebuffApplied(boolean enabled) { this.particleDebuffApplied = enabled; }
    public boolean getParticleDebuffAppliedRaw() { return particleDebuffApplied; }

    public boolean isParticlePotionEffect() { return particleMasterEnabled && particlePotionEffect; }
    public void setParticlePotionEffect(boolean enabled) { this.particlePotionEffect = enabled; }
    public boolean getParticlePotionEffectRaw() { return particlePotionEffect; }

    // Special particles
    public boolean isParticleTeleport() { return particleMasterEnabled && particleTeleport; }
    public void setParticleTeleport(boolean enabled) { this.particleTeleport = enabled; }
    public boolean getParticleTeleportRaw() { return particleTeleport; }

    public boolean isParticleRespawn() { return particleMasterEnabled && particleRespawn; }
    public void setParticleRespawn(boolean enabled) { this.particleRespawn = enabled; }
    public boolean getParticleRespawnRaw() { return particleRespawn; }

    public boolean isParticleCombo() { return particleMasterEnabled && particleCombo; }
    public void setParticleCombo(boolean enabled) { this.particleCombo = enabled; }
    public boolean getParticleComboRaw() { return particleCombo; }

    public boolean isParticleKillStreak() { return particleMasterEnabled && particleKillStreak; }
    public void setParticleKillStreak(boolean enabled) { this.particleKillStreak = enabled; }
    public boolean getParticleKillStreakRaw() { return particleKillStreak; }

    // Particle intensity and render distance
    public double getParticleIntensity() { return particleIntensity; }
    public void setParticleIntensity(double intensity) { this.particleIntensity = Math.max(0.25, Math.min(2.0, intensity)); }

    public int getParticleRenderDistance() { return particleRenderDistance; }
    public void setParticleRenderDistance(int distance) { this.particleRenderDistance = Math.max(8, Math.min(64, distance)); }

    // Utility methods for enabling/disabling all
    public void enableAllParticles() {
        particleSupportAbility = true;
        particleMeleeAbility = true;
        particlePassiveAbility = true;
        particleDamageHit = true;
        particleCriticalHit = true;
        particleBlocking = true;
        particleHealing = true;
        particleKill = true;
        particleDeath = true;
        particleLevelUp = true;
        particleAttributeAssign = true;
        particleAttributeRemove = true;
        particleTierUp = true;
        particleBossAmbient = true;
        particleBossAbility = true;
        particleBossSpawn = true;
        particleBossDeath = true;
        particleAltarAmbient = true;
        particleAltarActivation = true;
        particleItemPickup = true;
        particleItemDrop = true;
        particleStatusEffect = true;
        particleBuffApplied = true;
        particleDebuffApplied = true;
        particlePotionEffect = true;
        particleTeleport = true;
        particleRespawn = true;
        particleCombo = true;
        particleKillStreak = true;
    }

    public void disableAllParticles() {
        particleSupportAbility = false;
        particleMeleeAbility = false;
        particlePassiveAbility = false;
        particleDamageHit = false;
        particleCriticalHit = false;
        particleBlocking = false;
        particleHealing = false;
        particleKill = false;
        particleDeath = false;
        particleLevelUp = false;
        particleAttributeAssign = false;
        particleAttributeRemove = false;
        particleTierUp = false;
        particleBossAmbient = false;
        particleBossAbility = false;
        particleBossSpawn = false;
        particleBossDeath = false;
        particleAltarAmbient = false;
        particleAltarActivation = false;
        particleItemPickup = false;
        particleItemDrop = false;
        particleStatusEffect = false;
        particleBuffApplied = false;
        particleDebuffApplied = false;
        particlePotionEffect = false;
        particleTeleport = false;
        particleRespawn = false;
        particleCombo = false;
        particleKillStreak = false;
    }

    // ==================== END PARTICLE SETTINGS ====================

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