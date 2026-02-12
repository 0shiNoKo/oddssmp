package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Comprehensive Combat Logging System
 * Tracks and logs all combat-related events with customizable output
 */
public class CombatLogger {

    private final OddsSMP plugin;
    private final Deque<CombatLogEntry> globalLog = new ConcurrentLinkedDeque<>();
    private final Map<UUID, Deque<CombatLogEntry>> playerLogs = new HashMap<>();
    private final Map<UUID, Boolean> playerLogEnabled = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private PrintWriter fileWriter;
    private File logFile;

    // Combat log settings (controlled via GUI)
    private boolean enabled = true;
    private boolean logToFile = true;
    private boolean logToConsole = false;
    private boolean showToPlayers = true;
    private boolean logDamageEvents = true;
    private boolean logAbilityEvents = true;
    private boolean logKillEvents = true;
    private boolean logHealingEvents = true;
    private boolean logCombatTagEvents = true;
    private boolean logCriticalHits = true;
    private boolean logBlockedDamage = true;
    private boolean logEnvironmentalDamage = false;
    private boolean logMobDamage = false;
    private boolean showDamageNumbers = true;
    private boolean showHealthBars = true;
    private boolean compactMode = false;
    private double minimumDamageThreshold = 0.0;
    private int maxLogHistory = 100;
    private int maxPlayerLogHistory = 50;

    public CombatLogger(OddsSMP plugin) {
        this.plugin = plugin;
        setupFileLogging();
    }

    private void setupFileLogging() {
        try {
            File logsFolder = new File(plugin.getDataFolder(), "combat-logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdirs();
            }

            String fileName = "combat-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
            logFile = new File(logsFolder, fileName);

            if (logToFile) {
                fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not setup combat log file: " + e.getMessage());
        }
    }

    /**
     * Log a damage event
     */
    public void logDamage(Player attacker, Player victim, double damage, double finalDamage,
                          String damageType, boolean isCritical, boolean isAbility) {
        if (!enabled || !logDamageEvents) return;
        if (damage < minimumDamageThreshold) return;

        String attackerName = attacker != null ? attacker.getName() : "Environment";
        String message;

        if (compactMode) {
            message = String.format("%s -> %s: %.1f dmg%s",
                attackerName, victim.getName(), finalDamage,
                isCritical ? " (CRIT)" : "");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("§c⚔ ").append("§e").append(attackerName);
            sb.append(" §7dealt ");
            sb.append(isCritical ? "§c§l" : "§c").append(String.format("%.1f", finalDamage));
            sb.append("§7 damage to §e").append(victim.getName());

            if (showDamageNumbers && damage != finalDamage) {
                sb.append(" §8(").append(String.format("%.1f", damage)).append(" raw)");
            }

            if (damageType != null && !damageType.isEmpty()) {
                sb.append(" §8[").append(damageType).append("]");
            }

            if (isCritical) {
                sb.append(" §6§lCRIT!");
            }

            if (showHealthBars) {
                sb.append(" §8| §c").append(String.format("%.1f", victim.getHealth())).append("❤");
            }

            message = sb.toString();
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.DAMAGE,
            message,
            attacker != null ? attacker.getUniqueId() : null,
            victim.getUniqueId(),
            finalDamage
        );

        addEntry(entry);

        // Notify players
        if (showToPlayers) {
            if (attacker != null && isPlayerLogEnabled(attacker.getUniqueId())) {
                attacker.sendMessage(message);
            }
            if (isPlayerLogEnabled(victim.getUniqueId())) {
                victim.sendMessage(message);
            }
        }
    }

    /**
     * Log an ability usage event
     */
    public void logAbilityUse(Player player, String abilityName, String abilityType,
                              String target, double value) {
        if (!enabled || !logAbilityEvents) return;

        String message;
        if (compactMode) {
            message = String.format("%s used %s%s",
                player.getName(), abilityName,
                target != null ? " on " + target : "");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("§d✦ §e").append(player.getName());
            sb.append(" §7used §b").append(abilityName);

            if (abilityType != null) {
                sb.append(" §8(").append(abilityType).append(")");
            }

            if (target != null) {
                sb.append(" §7on §e").append(target);
            }

            if (value > 0) {
                sb.append(" §8[").append(String.format("%.1f", value)).append("]");
            }

            message = sb.toString();
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.ABILITY,
            message,
            player.getUniqueId(),
            null,
            value
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(player.getUniqueId())) {
            player.sendMessage(message);
        }
    }

    /**
     * Log a kill event
     */
    public void logKill(Player killer, Player victim, String weapon, double finalBlow) {
        if (!enabled || !logKillEvents) return;

        String message;
        if (compactMode) {
            message = String.format("%s killed %s", killer.getName(), victim.getName());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("§4☠ §c§l").append(killer.getName());
            sb.append(" §7killed §c§l").append(victim.getName());

            if (weapon != null && !weapon.isEmpty()) {
                sb.append(" §7with §6").append(weapon);
            }

            if (finalBlow > 0) {
                sb.append(" §8(").append(String.format("%.1f", finalBlow)).append(" final blow)");
            }

            message = sb.toString();
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.KILL,
            message,
            killer.getUniqueId(),
            victim.getUniqueId(),
            finalBlow
        );

        addEntry(entry);

        // Broadcast kills to all players with logging enabled
        if (showToPlayers) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isPlayerLogEnabled(p.getUniqueId())) {
                    p.sendMessage(message);
                }
            }
        }
    }

    /**
     * Log a death event (non-PvP)
     */
    public void logDeath(Player victim, String cause) {
        if (!enabled || !logKillEvents) return;

        String message;
        if (compactMode) {
            message = String.format("%s died (%s)", victim.getName(), cause);
        } else {
            message = "§4☠ §c" + victim.getName() + " §7died §8(" + cause + ")";
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.DEATH,
            message,
            null,
            victim.getUniqueId(),
            0
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(victim.getUniqueId())) {
            victim.sendMessage(message);
        }
    }

    /**
     * Log a healing event
     */
    public void logHealing(Player player, double amount, String source, Player healer) {
        if (!enabled || !logHealingEvents) return;
        if (amount < minimumDamageThreshold) return;

        String message;
        if (compactMode) {
            message = String.format("%s +%.1f HP%s",
                player.getName(), amount,
                healer != null ? " from " + healer.getName() : "");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("§a❤ §e").append(player.getName());
            sb.append(" §7healed §a+").append(String.format("%.1f", amount)).append("❤");

            if (healer != null && healer != player) {
                sb.append(" §7from §e").append(healer.getName());
            }

            if (source != null) {
                sb.append(" §8(").append(source).append(")");
            }

            if (showHealthBars) {
                sb.append(" §8| §a").append(String.format("%.1f", player.getHealth())).append("❤");
            }

            message = sb.toString();
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.HEALING,
            message,
            healer != null ? healer.getUniqueId() : null,
            player.getUniqueId(),
            amount
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(player.getUniqueId())) {
            player.sendMessage(message);
        }
    }

    /**
     * Log combat tag event
     */
    public void logCombatTag(Player player, Player enemy, boolean entering) {
        if (!enabled || !logCombatTagEvents) return;

        String message;
        if (compactMode) {
            message = String.format("%s %s combat", player.getName(), entering ? "entered" : "left");
        } else {
            if (entering) {
                message = "§c⚔ §e" + player.getName() + " §7entered combat with §e" + enemy.getName();
            } else {
                message = "§a✓ §e" + player.getName() + " §7left combat";
            }
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.COMBAT_TAG,
            message,
            player.getUniqueId(),
            enemy != null ? enemy.getUniqueId() : null,
            0
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(player.getUniqueId())) {
            player.sendMessage(message);
        }
    }

    /**
     * Log blocked damage (shields, absorption, etc.)
     */
    public void logBlockedDamage(Player player, double blockedAmount, String blockType) {
        if (!enabled || !logBlockedDamage) return;
        if (blockedAmount < minimumDamageThreshold) return;

        String message;
        if (compactMode) {
            message = String.format("%s blocked %.1f (%s)", player.getName(), blockedAmount, blockType);
        } else {
            message = "§b🛡 §e" + player.getName() + " §7blocked §b" +
                     String.format("%.1f", blockedAmount) + " §7damage §8(" + blockType + ")";
        }

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.BLOCKED,
            message,
            null,
            player.getUniqueId(),
            blockedAmount
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(player.getUniqueId())) {
            player.sendMessage(message);
        }
    }

    /**
     * Log critical hit
     */
    public void logCriticalHit(Player attacker, Player victim, double bonusDamage) {
        if (!enabled || !logCriticalHits) return;

        String message = "§6⚡ §e" + attacker.getName() + " §7landed a §6§lCRITICAL HIT §7on §e" +
                        victim.getName() + " §8(+" + String.format("%.1f", bonusDamage) + " bonus)";

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.CRITICAL,
            message,
            attacker.getUniqueId(),
            victim.getUniqueId(),
            bonusDamage
        );

        addEntry(entry);
    }

    /**
     * Log environmental damage
     */
    public void logEnvironmentalDamage(Player victim, double damage, String source) {
        if (!enabled || !logEnvironmentalDamage) return;
        if (damage < minimumDamageThreshold) return;

        String message = "§7☢ §e" + victim.getName() + " §7took §c" +
                        String.format("%.1f", damage) + " §7damage from §8" + source;

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.ENVIRONMENTAL,
            message,
            null,
            victim.getUniqueId(),
            damage
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(victim.getUniqueId())) {
            victim.sendMessage(message);
        }
    }

    /**
     * Log mob damage
     */
    public void logMobDamage(Player victim, String mobName, double damage) {
        if (!enabled || !logMobDamage) return;
        if (damage < minimumDamageThreshold) return;

        String message = "§c🐾 §8" + mobName + " §7dealt §c" +
                        String.format("%.1f", damage) + " §7damage to §e" + victim.getName();

        CombatLogEntry entry = new CombatLogEntry(
            CombatEventType.MOB_DAMAGE,
            message,
            null,
            victim.getUniqueId(),
            damage
        );

        addEntry(entry);

        if (showToPlayers && isPlayerLogEnabled(victim.getUniqueId())) {
            victim.sendMessage(message);
        }
    }

    /**
     * Log a custom event
     */
    public void logCustom(String message, CombatEventType type, UUID player1, UUID player2, double value) {
        if (!enabled) return;

        CombatLogEntry entry = new CombatLogEntry(type, message, player1, player2, value);
        addEntry(entry);
    }

    private void addEntry(CombatLogEntry entry) {
        // Add to global log
        globalLog.addFirst(entry);
        while (globalLog.size() > maxLogHistory) {
            globalLog.removeLast();
        }

        // Add to player logs
        if (entry.player1 != null) {
            addToPlayerLog(entry.player1, entry);
        }
        if (entry.player2 != null && !entry.player2.equals(entry.player1)) {
            addToPlayerLog(entry.player2, entry);
        }

        // Log to file
        if (logToFile && fileWriter != null) {
            String timestamp = fileDateFormat.format(new Date());
            fileWriter.println("[" + timestamp + "] " + stripColor(entry.message));
        }

        // Log to console
        if (logToConsole) {
            plugin.getLogger().info("[Combat] " + stripColor(entry.message));
        }
    }

    private void addToPlayerLog(UUID playerId, CombatLogEntry entry) {
        playerLogs.computeIfAbsent(playerId, k -> new ConcurrentLinkedDeque<>());
        Deque<CombatLogEntry> log = playerLogs.get(playerId);
        log.addFirst(entry);
        while (log.size() > maxPlayerLogHistory) {
            log.removeLast();
        }
    }

    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    // Player log toggle
    public boolean isPlayerLogEnabled(UUID playerId) {
        return playerLogEnabled.getOrDefault(playerId, true);
    }

    public void setPlayerLogEnabled(UUID playerId, boolean enabled) {
        playerLogEnabled.put(playerId, enabled);
    }

    public void togglePlayerLog(UUID playerId) {
        playerLogEnabled.put(playerId, !isPlayerLogEnabled(playerId));
    }

    // Get logs
    public List<CombatLogEntry> getGlobalLog(int limit) {
        List<CombatLogEntry> result = new ArrayList<>();
        int count = 0;
        for (CombatLogEntry entry : globalLog) {
            if (count >= limit) break;
            result.add(entry);
            count++;
        }
        return result;
    }

    public List<CombatLogEntry> getPlayerLog(UUID playerId, int limit) {
        Deque<CombatLogEntry> log = playerLogs.get(playerId);
        if (log == null) return new ArrayList<>();

        List<CombatLogEntry> result = new ArrayList<>();
        int count = 0;
        for (CombatLogEntry entry : log) {
            if (count >= limit) break;
            result.add(entry);
            count++;
        }
        return result;
    }

    public void clearPlayerLog(UUID playerId) {
        if (playerLogs.containsKey(playerId)) {
            playerLogs.get(playerId).clear();
        }
    }

    public void clearGlobalLog() {
        globalLog.clear();
    }

    // Getters and Setters for settings
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isLogToFile() { return logToFile; }
    public void setLogToFile(boolean logToFile) {
        this.logToFile = logToFile;
        if (logToFile && fileWriter == null) {
            setupFileLogging();
        }
    }

    public boolean isLogToConsole() { return logToConsole; }
    public void setLogToConsole(boolean logToConsole) { this.logToConsole = logToConsole; }

    public boolean isShowToPlayers() { return showToPlayers; }
    public void setShowToPlayers(boolean showToPlayers) { this.showToPlayers = showToPlayers; }

    public boolean isLogDamageEvents() { return logDamageEvents; }
    public void setLogDamageEvents(boolean logDamageEvents) { this.logDamageEvents = logDamageEvents; }

    public boolean isLogAbilityEvents() { return logAbilityEvents; }
    public void setLogAbilityEvents(boolean logAbilityEvents) { this.logAbilityEvents = logAbilityEvents; }

    public boolean isLogKillEvents() { return logKillEvents; }
    public void setLogKillEvents(boolean logKillEvents) { this.logKillEvents = logKillEvents; }

    public boolean isLogHealingEvents() { return logHealingEvents; }
    public void setLogHealingEvents(boolean logHealingEvents) { this.logHealingEvents = logHealingEvents; }

    public boolean isLogCombatTagEvents() { return logCombatTagEvents; }
    public void setLogCombatTagEvents(boolean logCombatTagEvents) { this.logCombatTagEvents = logCombatTagEvents; }

    public boolean isLogCriticalHits() { return logCriticalHits; }
    public void setLogCriticalHits(boolean logCriticalHits) { this.logCriticalHits = logCriticalHits; }

    public boolean isLogBlockedDamage() { return logBlockedDamage; }
    public void setLogBlockedDamage(boolean logBlockedDamage) { this.logBlockedDamage = logBlockedDamage; }

    public boolean isLogEnvironmentalDamage() { return logEnvironmentalDamage; }
    public void setLogEnvironmentalDamage(boolean logEnvironmentalDamage) { this.logEnvironmentalDamage = logEnvironmentalDamage; }

    public boolean isLogMobDamage() { return logMobDamage; }
    public void setLogMobDamage(boolean logMobDamage) { this.logMobDamage = logMobDamage; }

    public boolean isShowDamageNumbers() { return showDamageNumbers; }
    public void setShowDamageNumbers(boolean showDamageNumbers) { this.showDamageNumbers = showDamageNumbers; }

    public boolean isShowHealthBars() { return showHealthBars; }
    public void setShowHealthBars(boolean showHealthBars) { this.showHealthBars = showHealthBars; }

    public boolean isCompactMode() { return compactMode; }
    public void setCompactMode(boolean compactMode) { this.compactMode = compactMode; }

    public double getMinimumDamageThreshold() { return minimumDamageThreshold; }
    public void setMinimumDamageThreshold(double threshold) {
        this.minimumDamageThreshold = Math.max(0, Math.min(20, threshold));
    }

    public int getMaxLogHistory() { return maxLogHistory; }
    public void setMaxLogHistory(int max) {
        this.maxLogHistory = Math.max(10, Math.min(1000, max));
    }

    public int getMaxPlayerLogHistory() { return maxPlayerLogHistory; }
    public void setMaxPlayerLogHistory(int max) {
        this.maxPlayerLogHistory = Math.max(10, Math.min(500, max));
    }

    public void shutdown() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    /**
     * Combat log entry
     */
    public static class CombatLogEntry {
        public final CombatEventType type;
        public final String message;
        public final UUID player1;
        public final UUID player2;
        public final double value;
        public final long timestamp;

        public CombatLogEntry(CombatEventType type, String message, UUID player1, UUID player2, double value) {
            this.type = type;
            this.message = message;
            this.player1 = player1;
            this.player2 = player2;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Combat event types
     */
    public enum CombatEventType {
        DAMAGE("§c⚔"),
        ABILITY("§d✦"),
        KILL("§4☠"),
        DEATH("§4☠"),
        HEALING("§a❤"),
        COMBAT_TAG("§e⚔"),
        BLOCKED("§b🛡"),
        CRITICAL("§6⚡"),
        ENVIRONMENTAL("§7☢"),
        MOB_DAMAGE("§c🐾"),
        CUSTOM("§7•");

        public final String icon;
        CombatEventType(String icon) {
            this.icon = icon;
        }
    }
}
