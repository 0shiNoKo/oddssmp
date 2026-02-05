package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OddsSMP extends JavaPlugin {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private AbilityManager abilityManager;
    private CommandHandler commandHandler;
    private EventListener eventListener;
    private AdminGUI adminGUI;
    private GUIListener guiListener;

    @Override
    public void onEnable() {
        getLogger().info("OddsSMP Plugin Enabled!");

        // Initialize managers
        abilityManager = new AbilityManager(this);
        commandHandler = new CommandHandler(this);
        eventListener = new EventListener(this);
        adminGUI = new AdminGUI(this);
        guiListener = new GUIListener(this, adminGUI);

        // Register commands
        getCommand("smp").setExecutor(commandHandler);
        getCommand("smp").setTabCompleter(commandHandler);
        getCommand("admin").setExecutor(commandHandler);
        getCommand("admin").setTabCompleter(commandHandler);

        // Register events
        Bukkit.getPluginManager().registerEvents(eventListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

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

        // Save all player data
        for (UUID uuid : playerDataMap.keySet()) {
            savePlayerData(uuid);
        }
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
     * Set player data
     */
    public void setPlayerData(UUID uuid, PlayerData data) {
        playerDataMap.put(uuid, data);
    }

    /**
     * Save player data (for persistence - can be extended to save to file/database)
     */
    public void savePlayerData(UUID uuid) {
        // TODO: Implement file/database saving if needed
        // For now, data is stored in memory only
    }

    /**
     * Load player data (for persistence - can be extended to load from file/database)
     */
    public void loadPlayerData(UUID uuid) {
        // TODO: Implement file/database loading if needed
        // For now, creates new PlayerData if not exists
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
}