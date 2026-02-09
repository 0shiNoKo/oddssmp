package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final OddsSMP plugin;
    private AscendedEnderDragon activeDragon = null;
    private AscendedWither activeWither = null;
    private AscendedWarden activeWarden = null;
    private AscendedBreeze activeBreeze = null;

    public CommandHandler(OddsSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("smp")) {
            return handleSMPCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("admin")) {
            return handleAdminCommand(sender, args);
        }
        return false;
    }

    /**
     * Handle /smp commands
     */
    private boolean handleSMPCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Check permissions for admin commands (allow support, melee, info for everyone)
        if (!subCommand.equals("support") && !subCommand.equals("melee") && !subCommand.equals("info")) {
            if (!sender.hasPermission("oddssmp.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }
        }

        switch (subCommand) {
            case "assign":
                return handleAssign(sender, args);
            case "reroll":
                return handleReroll(sender, args);
            case "upgrade":
                return handleUpgrade(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "cooldown":
                return handleCooldown(sender, args);
            case "support":
                return handleSupportAbility(sender);
            case "melee":
                return handleMeleeAbility(sender);
            case "info":
                return handleInfo(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * /smp assign <player> [attribute] [tier]
     */
    private boolean handleAssign(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /smp assign <player> [attribute] [tier]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        AttributeType attribute;
        Tier tier;

        // Parse attribute
        if (args.length >= 3) {
            try {
                attribute = AttributeType.valueOf(args[2].toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid attribute! Use tab completion.");
                sender.sendMessage("§cAvailable: melee, health, wealth, defense, speed, control, range, pressure, tempo, disruption, vision, persistence, risk, anchor, transfer, wither, warden, breeze, dragon_egg");
                return true;
            }
        } else {
            // Random attribute (exclude Dragon Egg unless specified)
            attribute = AttributeType.getRandomAttribute(false);
        }

        // Parse tier
        if (args.length >= 4) {
            try {
                tier = Tier.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid tier! Options: STABLE, WARPED, EXTREME");
                return true;
            }
        } else {
            // Random tier based on probability
            if (attribute == AttributeType.DRAGON_EGG) {
                tier = Tier.EXTREME;
            } else {
                tier = Tier.getRandomTier();
            }
        }

        // Get old attribute to clean up effects
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Create and assign player data
        PlayerData data = new PlayerData(attribute, tier);
        plugin.setPlayerData(target.getUniqueId(), data);

        // Play particles
        ParticleManager.playSupportParticles(target, attribute, tier, 1);

        // Update tab
        plugin.updatePlayerTab(target);

        // Announce if Dragon Egg
        if (attribute == AttributeType.DRAGON_EGG) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§6§l§m                                                    ");
            Bukkit.broadcastMessage("§c§l⚠ §6§lDRAGON EGG OBTAINED §c§l⚠");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("  §e" + target.getName() + " §7has been granted the");
            Bukkit.broadcastMessage("  §6§lLEGENDARY DRAGON EGG!");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("  §7An immense power now flows through them...");
            Bukkit.broadcastMessage("§6§l§m                                                    ");
            Bukkit.broadcastMessage("");

            // Apply Dragon Egg effects
            plugin.getEventListener().applyDragonEggEffects(target);
        }

        sender.sendMessage("§aAssigned " + tier.getColor() + tier.name() + " " + attribute.getDisplayName() +
                " §ato " + target.getName());
        target.sendMessage("§aYou received " + tier.getColor() + tier.name() + " " +
                attribute.getIcon() + " " + attribute.getDisplayName() + "§a!");

        return true;
    }

    /**
     * /smp reroll <player>
     */
    private boolean handleReroll(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /smp reroll <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        // Get old attribute to clean up effects
        PlayerData oldData = plugin.getPlayerData(target.getUniqueId());
        AttributeType oldAttribute = oldData != null ? oldData.getAttribute() : null;

        // Remove old attribute effects
        if (oldAttribute != null) {
            plugin.getEventListener().removeAttributeEffects(target, oldAttribute);
        }

        // Get new random attribute and tier
        AttributeType attribute = AttributeType.getRandomAttribute(false);
        Tier tier = Tier.getRandomTier();

        // Create new player data
        PlayerData data = new PlayerData(attribute, tier);
        plugin.setPlayerData(target.getUniqueId(), data);

        // Play particles
        ParticleManager.playSupportParticles(target, attribute, tier, 1);

        // Update tab
        plugin.updatePlayerTab(target);

        sender.sendMessage("§aRerolled " + target.getName() + "'s attribute to " +
                tier.getColor() + tier.name() + " " + attribute.getDisplayName());
        target.sendMessage("§eYour attribute was rerolled to " + tier.getColor() + tier.name() + " " +
                attribute.getIcon() + " " + attribute.getDisplayName() + "§e!");

        return true;
    }

    /**
     * /smp upgrade <player> [amount]
     */
    private boolean handleUpgrade(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /smp upgrade <player> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            sender.sendMessage("§cPlayer doesn't have an attribute!");
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount!");
                return true;
            }
        }

        int oldLevel = data.getLevel();
        for (int i = 0; i < amount; i++) {
            data.incrementLevel();
        }

        // Play particles
        ParticleManager.playSupportParticles(target, data.getAttribute(), data.getTier(), data.getLevel());

        // Update tab
        plugin.updatePlayerTab(target);

        sender.sendMessage("§aUpgraded " + target.getName() + "'s " + data.getAttribute().getDisplayName() +
                " from level " + oldLevel + " to " + data.getLevel());
        target.sendMessage("§aYour " + data.getAttribute().getDisplayName() +
                " was upgraded to level " + data.getLevel() + "!");

        return true;
    }

    /**
     * /smp remove <player>
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /smp remove <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            sender.sendMessage("§cPlayer doesn't have an attribute!");
            return true;
        }

        AttributeType removedAttr = data.getAttribute();

        // Remove attribute effects
        plugin.getEventListener().removeAttributeEffects(target, removedAttr);

        // Remove attribute
        plugin.setPlayerData(target.getUniqueId(), new PlayerData());

        // Play fade out particles
        target.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, target.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.02);

        // Update tab
        plugin.updatePlayerTab(target);

        sender.sendMessage("§aRemoved " + removedAttr.getDisplayName() + " from " + target.getName());
        target.sendMessage("§cYour " + removedAttr.getDisplayName() + " was removed!");

        return true;
    }

    /**
     * /smp reset <player>
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /smp reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            sender.sendMessage("§cPlayer doesn't have an attribute!");
            return true;
        }

        // Reset to level 1 and clear cooldowns
        data.setLevel(1);
        data.clearCooldowns();
        plugin.getAbilityManager().removeAbilityFlags(target.getUniqueId());

        // Play reset particles
        target.getWorld().spawnParticle(org.bukkit.Particle.GLOW, target.getLocation().add(0, 1, 0),
                50, 0.5, 1, 0.5, 0.05);

        // Update tab
        plugin.updatePlayerTab(target);

        sender.sendMessage("§aReset " + target.getName() + "'s " + data.getAttribute().getDisplayName() +
                " to level 1");
        target.sendMessage("§eYour " + data.getAttribute().getDisplayName() + " was reset to level 1!");

        return true;
    }

    /**
     * /smp cooldown <player> <support|melee> <seconds>
     */
    private boolean handleCooldown(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /smp cooldown <player> <support|melee> <seconds>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage("§cPlayer doesn't have an attribute!");
            return true;
        }

        String abilityType = args[2].toLowerCase();
        if (!abilityType.equals("support") && !abilityType.equals("melee")) {
            sender.sendMessage("§cAbility type must be 'support' or 'melee'");
            return true;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number!");
            return true;
        }

        data.setCooldown(abilityType, seconds * 1000L);

        sender.sendMessage("§aSet " + target.getName() + "'s " + abilityType + " cooldown to " + seconds + " seconds");
        target.sendMessage("§eYour " + abilityType + " cooldown was set to " + seconds + " seconds");

        return true;
    }

    /**
     * /smp support - Activate support ability
     */
    private boolean handleSupportAbility(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use abilities!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getAbilityManager().activateSupport(player);
        return true;
    }

    /**
     * /smp melee - Activate melee ability
     */
    private boolean handleMeleeAbility(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use abilities!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getAbilityManager().activateMelee(player);
        return true;
    }

    /**
     * /smp info - Open attribute info GUI
     */
    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use the info GUI!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getAdminGUI().openAttributeInfo(player);
        return true;
    }

    /**
     * Handle /admin commands
     */
    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("oddssmp.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /admin <gui|test|boss|autoassign|assignall|debugdragon> [args]");
            return true;
        }

        // GUI command
        if (args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use the GUI!");
                return true;
            }
            plugin.getAdminGUI().openMainMenu((Player) sender);
            return true;
        }

        // Auto-assign command
        if (args[0].equalsIgnoreCase("autoassign")) {
            return handleAutoAssignCommand(sender, args);
        }

        // Assign all command
        if (args[0].equalsIgnoreCase("assignall")) {
            return handleAssignAllCommand(sender);
        }

        // Debug dragon command
        if (args[0].equalsIgnoreCase("debugdragon")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can test!");
                return true;
            }
            Player player = (Player) sender;
            plugin.getEventListener().applyDragonEggEffects(player);
            sender.sendMessage("§aApplied Dragon Egg effects for testing!");
            return true;
        }

        // Boss command
        if (args[0].equalsIgnoreCase("boss")) {
            return handleBossCommand(sender, args);
        }

        // Weapon command
        if (args[0].equalsIgnoreCase("weapon")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use the weapon GUI!");
                return true;
            }
            plugin.getWeaponGUI().openMainMenu((Player) sender);
            return true;
        }

        // Test command
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /admin <gui|test|boss|weapon|autoassign|assignall> [args]");
            return true;
        }

        if (!args[0].equalsIgnoreCase("test")) {
            sender.sendMessage("§cUsage: /admin <gui|test|boss|debugdragon> [args]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null || data.getAttribute() == null) {
            sender.sendMessage("§cPlayer doesn't have an attribute!");
            return true;
        }

        String abilityType = args[2].toLowerCase();

        switch (abilityType) {
            case "support":
                ParticleManager.playSupportParticles(target, data.getAttribute(), data.getTier(), data.getLevel());
                sender.sendMessage("§aPlayed support particles for " + target.getName());
                break;
            case "melee":
                // Find nearest entity for target
                org.bukkit.entity.Entity nearestEntity = target.getNearbyEntities(10, 10, 10).stream()
                        .filter(e -> e instanceof org.bukkit.entity.LivingEntity)
                        .findFirst()
                        .orElse(null);
                if (nearestEntity != null) {
                    ParticleManager.playMeleeParticles(target, nearestEntity, data.getAttribute(), data.getTier());
                    sender.sendMessage("§aPlayed melee particles for " + target.getName());
                } else {
                    sender.sendMessage("§cNo nearby entities found!");
                }
                break;
            case "passive":
                ParticleManager.playPassiveParticles(target, data.getAttribute(), data.getTier());
                sender.sendMessage("§aPlayed passive particles for " + target.getName());
                break;
            default:
                sender.sendMessage("§cAbility type must be 'support', 'melee', or 'passive'");
                break;
        }

        return true;
    }

    /**
     * Handle /admin boss commands
     */
    private boolean handleBossCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /admin boss <wither|warden|breeze|enderdragon|stop|stopall>");
            return true;
        }

        String bossType = args[1].toLowerCase();

        if (!(sender instanceof Player) && !bossType.equals("stop") && !bossType.equals("stopall")) {
            sender.sendMessage("§cOnly players can spawn bosses!");
            return true;
        }

        Player player = sender instanceof Player ? (Player) sender : null;

        switch (bossType) {
            case "wither":
                if (activeWither != null && activeWither.isActive()) {
                    sender.sendMessage("§cAscended Wither is already active!");
                    return true;
                }
                activeWither = new AscendedWither(plugin);
                activeWither.spawn(player.getLocation());
                sender.sendMessage("§8§lAscended Wither spawned!");
                return true;

            case "warden":
                if (activeWarden != null && activeWarden.isActive()) {
                    sender.sendMessage("§cAscended Warden is already active!");
                    return true;
                }
                activeWarden = new AscendedWarden(plugin);
                activeWarden.spawn(player.getLocation());
                sender.sendMessage("§3§lAscended Warden spawned!");
                return true;

            case "breeze":
                if (activeBreeze != null && activeBreeze.isActive()) {
                    sender.sendMessage("§cAscended Breeze is already active!");
                    return true;
                }
                activeBreeze = new AscendedBreeze(plugin);
                activeBreeze.spawn(player.getLocation());
                sender.sendMessage("§b§lAscended Breeze spawned!");
                return true;

            case "enderdragon":
                if (activeDragon != null && activeDragon.isActive()) {
                    sender.sendMessage("§cAscended Ender Dragon is already active!");
                    return true;
                }
                activeDragon = new AscendedEnderDragon(plugin, player.getWorld(), player.getLocation());
                activeDragon.spawn();
                sender.sendMessage("§5§lAscended Ender Dragon spawned!");
                return true;

            case "stop":
                int stopped = 0;
                if (activeDragon != null && activeDragon.isActive()) {
                    if (activeDragon.getDragon() != null && !activeDragon.getDragon().isDead()) {
                        activeDragon.getDragon().setHealth(0);
                    }
                    activeDragon = null;
                    stopped++;
                }
                if (activeWither != null && activeWither.isActive()) {
                    activeWither = null;
                    stopped++;
                }
                if (activeWarden != null && activeWarden.isActive()) {
                    activeWarden = null;
                    stopped++;
                }
                if (activeBreeze != null && activeBreeze.isActive()) {
                    activeBreeze = null;
                    stopped++;
                }
                if (stopped > 0) {
                    sender.sendMessage("§aStopped " + stopped + " boss fight(s)!");
                } else {
                    sender.sendMessage("§cNo active bosses to stop!");
                }
                return true;

            case "stopall":
                activeDragon = null;
                activeWither = null;
                activeWarden = null;
                activeBreeze = null;
                sender.sendMessage("§aAll boss fights cleared!");
                return true;

            default:
                sender.sendMessage("§cUsage: /admin boss <wither|warden|breeze|enderdragon|stop|stopall>");
                return true;
        }
    }

    /**
     * Handle /admin autoassign <on|off> [delay_seconds]
     */
    private boolean handleAutoAssignCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            boolean enabled = plugin.isAutoAssignEnabled();
            int delay = plugin.getAutoAssignDelaySeconds();
            sender.sendMessage("§6Auto-Assign Status:");
            sender.sendMessage("  §7Enabled: " + (enabled ? "§aON" : "§cOFF"));
            sender.sendMessage("  §7Delay: §e" + delay + " seconds");
            sender.sendMessage("§7Usage: /admin autoassign <on|off> [delay_seconds]");
            return true;
        }

        String toggle = args[1].toLowerCase();
        if (toggle.equals("on")) {
            plugin.setAutoAssignEnabled(true);

            // Optional delay argument
            if (args.length >= 3) {
                try {
                    int delay = Integer.parseInt(args[2]);
                    if (delay < 0) delay = 0;
                    plugin.setAutoAssignDelaySeconds(delay);
                    sender.sendMessage("§aAuto-assign §lENABLED§a with §e" + delay + "s §adelay!");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid delay! Using current: " + plugin.getAutoAssignDelaySeconds() + "s");
                    sender.sendMessage("§aAuto-assign §lENABLED§a!");
                }
            } else {
                sender.sendMessage("§aAuto-assign §lENABLED§a with §e" + plugin.getAutoAssignDelaySeconds() + "s §adelay!");
            }

            Bukkit.broadcastMessage("§6§l[OddsSMP] §aAuto-assign is now §lON§a!");
            Bukkit.broadcastMessage("§7New players will receive random attributes after " + plugin.getAutoAssignDelaySeconds() + " seconds.");

        } else if (toggle.equals("off")) {
            plugin.setAutoAssignEnabled(false);
            sender.sendMessage("§cAuto-assign §lDISABLED§c!");
            Bukkit.broadcastMessage("§6§l[OddsSMP] §cAuto-assign is now §lOFF§c!");

        } else {
            sender.sendMessage("§cUsage: /admin autoassign <on|off> [delay_seconds]");
        }

        return true;
    }

    /**
     * Handle /admin assignall - Assign random attributes to all players without one
     */
    private boolean handleAssignAllCommand(CommandSender sender) {
        int assigned = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerData(player.getUniqueId());

            // Skip if player already has an attribute
            if (data != null && data.getAttribute() != null) {
                continue;
            }

            // Assign random attribute
            AttributeType attribute = AttributeType.getRandomAttribute(false);
            Tier tier = Tier.getRandomTier();

            PlayerData newData = new PlayerData(attribute, tier);
            plugin.setPlayerData(player.getUniqueId(), newData);

            // Play particles
            ParticleManager.playSupportParticles(player, attribute, tier, 1);

            // Update tab
            plugin.updatePlayerTab(player);

            // Notify player
            player.sendMessage("§a§l✦ You received " + tier.getColor() + tier.name() + " " +
                    attribute.getIcon() + " " + attribute.getDisplayName() + "§a! ✦");

            assigned++;
        }

        if (assigned > 0) {
            Bukkit.broadcastMessage("§6§l[OddsSMP] §a" + assigned + " player(s) received random attributes!");
        }

        sender.sendMessage("§aAssigned attributes to §e" + assigned + "§a player(s).");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== OddsSMP Commands ===");
        if (sender.hasPermission("oddssmp.admin")) {
            sender.sendMessage("§e/admin gui §7- Open admin control panel");
            sender.sendMessage("§e/admin boss <type> §7- Spawn boss (wither/warden/breeze/enderdragon)");
            sender.sendMessage("§e/admin weapon §7- Open attribute weapons GUI");
            sender.sendMessage("§e/admin autoassign <on|off> [delay] §7- Toggle auto-assign on join");
            sender.sendMessage("§e/admin assignall §7- Give attributes to all players");
        }
        sender.sendMessage("§e/smp info §7- View all attributes info");
        sender.sendMessage("§e/smp support §7- Activate support ability");
        sender.sendMessage("§e/smp melee §7- Activate melee ability");
        if (sender.hasPermission("oddssmp.admin")) {
            sender.sendMessage("§e/smp assign <player> [attribute] [tier] §7- Assign attribute");
            sender.sendMessage("§e/smp reroll <player> §7- Reroll attribute");
            sender.sendMessage("§e/smp upgrade <player> [amount] §7- Upgrade level");
            sender.sendMessage("§e/smp remove <player> §7- Remove attribute");
            sender.sendMessage("§e/smp reset <player> §7- Reset to level 1");
            sender.sendMessage("§e/smp cooldown <player> <type> <seconds> §7- Set cooldown");
            sender.sendMessage("§e/admin test <player> <ability_type> §7- Test particles");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("smp")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("info", "support", "melee"));
                if (sender.hasPermission("oddssmp.admin")) {
                    completions.addAll(Arrays.asList("assign", "reroll", "upgrade", "remove", "reset", "cooldown"));
                }
            } else if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            } else if (args.length == 3 && args[0].equalsIgnoreCase("assign")) {
                for (AttributeType attr : AttributeType.values()) {
                    completions.add(attr.name().toLowerCase());
                }
            } else if (args.length == 4 && args[0].equalsIgnoreCase("assign")) {
                completions.addAll(Arrays.asList("stable", "warped", "extreme"));
            } else if (args.length == 3 && args[0].equalsIgnoreCase("cooldown")) {
                completions.addAll(Arrays.asList("support", "melee"));
            }
        } else if (command.getName().equalsIgnoreCase("admin")) {
            if (args.length == 1) {
                completions.add("gui");
                completions.add("test");
                completions.add("boss");
                completions.add("weapon");
                completions.add("autoassign");
                completions.add("assignall");
                completions.add("debugdragon");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            } else if (args.length == 3 && args[0].equalsIgnoreCase("test")) {
                completions.addAll(Arrays.asList("support", "melee", "passive"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("boss")) {
                completions.addAll(Arrays.asList("wither", "warden", "breeze", "enderdragon", "stop", "stopall"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("autoassign")) {
                completions.addAll(Arrays.asList("on", "off"));
            } else if (args.length == 3 && args[0].equalsIgnoreCase("autoassign")) {
                completions.addAll(Arrays.asList("5", "10", "15", "30", "60"));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}