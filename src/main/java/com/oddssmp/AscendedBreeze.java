package com.oddssmp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AscendedBreeze implements Listener {

    private final OddsSMP plugin;
    private Breeze breeze;
    private BossBar bossBar;
    private boolean isActive = false;
    private Location arenaCenter;
    private World world;

    // Stats
    private final double maxHealth = 850.0; // 425 hearts
    private final double armorReduction = 0.25; // 25% damage reduction
    private final double knockbackResistance = 0.40; // 40%

    // Attacks
    private final double windSurgeDamage = 12.0;
    private int blinkRange = 8;

    // Anti-zerg tracking
    private final Map<UUID, Long> recentAttackers = new HashMap<>();
    private static final long ATTACK_WINDOW = 5000; // 5 seconds

    // Enrage
    private boolean isEnraged = false;
    private int attackCooldown = 60; // 3 seconds base

    public AscendedBreeze(OddsSMP plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Spawn the Ascended Breeze
     */
    public void spawn(Location location) {
        this.arenaCenter = location;
        this.world = location.getWorld();

        // Spawn breeze
        breeze = (Breeze) world.spawnEntity(location.clone().add(0, 2, 0), EntityType.BREEZE);

        // Configure breeze
        breeze.setCustomName("§8§l§kA§r §b§lASCENDED BREEZE §8§l§kA");
        breeze.setCustomNameVisible(true);
        breeze.setMaxHealth(maxHealth);
        breeze.setHealth(maxHealth);

        // Set knockback resistance
        try {
            var attr = breeze.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (attr != null) attr.setBaseValue(knockbackResistance);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set knockback resistance: " + e.getMessage());
        }

        // Create boss bar
        bossBar = Bukkit.createBossBar(
                "§b§lASCENDED BREEZE",
                BarColor.WHITE,
                BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        isActive = true;

        // Start boss AI
        startBossAI();

        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("§b§l⚠ ASCENDED BREEZE HAS AWAKENED ⚠");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The winds howl with fury...");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Play sound
        world.playSound(location, Sound.ENTITY_BREEZE_IDLE_GROUND, 2.0f, 0.8f);
    }

    /**
     * Start the boss AI loop
     */
    private void startBossAI() {
        new BukkitRunnable() {
            int tick = 0;
            int currentAttackCooldown = 0;

            @Override
            public void run() {
                if (!isActive || breeze == null || breeze.isDead()) {
                    cancel();
                    return;
                }

                // Update boss bar
                updateBossBar();

                // Add nearby players to boss bar
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distance(breeze.getLocation()) < 100) {
                        if (!bossBar.getPlayers().contains(player)) {
                            bossBar.addPlayer(player);
                        }
                    } else {
                        bossBar.removePlayer(player);
                    }
                }

                // Check enrage
                if (!isEnraged && breeze.getHealth() < maxHealth * 0.25) {
                    triggerEnrage();
                }

                // Anti-zerg blink
                checkAntiZergBlink();

                // Wind Surge attack
                int effectiveCooldown = isEnraged ? (int)(attackCooldown * 0.67) : attackCooldown;
                if (currentAttackCooldown <= 0) {
                    Player target = getNearestPlayer();
                    if (target != null) {
                        double dist = target.getLocation().distance(breeze.getLocation());
                        if (dist < 15) {
                            windSurgeAttack(target);
                            currentAttackCooldown = effectiveCooldown;
                        }
                    }
                } else {
                    currentAttackCooldown--;
                }

                // Wind Burst (AoE knockback)
                if (tick % 100 == 50) { // Offset from surge
                    windBurstAttack();
                }

                // Random blink for evasion
                if (tick % 40 == 0 && Math.random() < 0.3) {
                    randomBlink();
                }

                // Passive particles
                if (tick % 3 == 0) {
                    spawnBreezeParticles();
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Wind Surge Attack - Pull player then slam
     */
    private void windSurgeAttack(Player target) {
        if (breeze == null) return;

        Location breezeLoc = breeze.getLocation();

        // Sound
        world.playSound(breezeLoc, Sound.ENTITY_BREEZE_WIND_BURST, 1.5f, 1.2f);

        // Pull effect - pull player towards breeze
        Vector pullDirection = breezeLoc.toVector().subtract(target.getLocation().toVector()).normalize();
        target.setVelocity(pullDirection.multiply(1.5).setY(0.3));

        // Visual - wind particles towards target
        Vector toTarget = target.getLocation().toVector().subtract(breezeLoc.toVector()).normalize();
        for (double d = 0; d < breezeLoc.distance(target.getLocation()); d += 0.5) {
            Location point = breezeLoc.clone().add(toTarget.clone().multiply(d));
            world.spawnParticle(Particle.CLOUD, point, 3, 0.1, 0.1, 0.1, 0.02);
        }

        // Delayed slam damage
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (target.isOnline() && breeze != null && !breeze.isDead()) {
                double dist = target.getLocation().distance(breeze.getLocation());
                if (dist < 5) { // If pull was successful
                    target.damage(windSurgeDamage, breeze);

                    // Slam visual
                    world.spawnParticle(Particle.EXPLOSION, target.getLocation(), 5, 0.5, 0.5, 0.5, 0);
                    world.playSound(target.getLocation(), Sound.ENTITY_BREEZE_LAND, 1.5f, 0.8f);
                }
            }
        }, 15L);
    }

    /**
     * Wind Burst Attack - AoE knockback + slow
     */
    private void windBurstAttack() {
        if (breeze == null) return;

        Location loc = breeze.getLocation();
        double radius = 10.0;

        // Visual effect
        world.playSound(loc, Sound.ENTITY_BREEZE_WIND_BURST, 2.0f, 0.8f);

        // Expanding ring effect
        for (int i = 0; i < 3; i++) {
            final int ring = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double r = (ring + 1) * 3;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    Location particleLoc = loc.clone().add(x, 1, z);
                    world.spawnParticle(Particle.CLOUD, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                }
            }, i * 3L);
        }

        // Knockback nearby players
        for (Entity entity : breeze.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                // Knockback away from breeze
                Vector knockback = player.getLocation().toVector()
                        .subtract(loc.toVector()).normalize().multiply(2.0);
                knockback.setY(0.8);
                player.setVelocity(knockback);

                // Slow effect
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            }
        }
    }

    /**
     * Check for anti-zerg blink
     */
    private void checkAntiZergBlink() {
        if (breeze == null) return;

        int nearbyPlayers = 0;
        for (Entity entity : breeze.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Player) {
                nearbyPlayers++;
            }
        }

        // Blink away if more than 4 players nearby
        if (nearbyPlayers > 4) {
            antiZergBlink();
        }
    }

    /**
     * Blink away from players (anti-zerg)
     */
    private void antiZergBlink() {
        if (breeze == null) return;

        Location loc = breeze.getLocation();

        // Find direction away from most players
        Vector escapeDir = new Vector(0, 0, 0);
        for (Entity entity : breeze.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Player) {
                Vector away = loc.toVector().subtract(entity.getLocation().toVector());
                escapeDir.add(away.normalize());
            }
        }

        if (escapeDir.length() > 0) {
            escapeDir.normalize();
        } else {
            // Random direction if can't determine
            escapeDir = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).normalize();
        }

        int range = isEnraged ? blinkRange + 1 : blinkRange;

        // Find blink location
        Location blinkLoc = loc.clone().add(escapeDir.multiply(range));
        blinkLoc.setY(loc.getY() + 2); // Slightly higher

        // Ensure valid location
        if (blinkLoc.getBlock().getType().isSolid()) {
            blinkLoc.add(0, 2, 0);
        }

        // Effects
        world.spawnParticle(Particle.CLOUD, loc, 30, 0.5, 0.5, 0.5, 0.1);
        world.playSound(loc, Sound.ENTITY_BREEZE_JUMP, 1.5f, 1.5f);

        // Teleport
        breeze.teleport(blinkLoc);

        // Arrival effects
        world.spawnParticle(Particle.CLOUD, blinkLoc, 30, 0.5, 0.5, 0.5, 0.1);
        world.playSound(blinkLoc, Sound.ENTITY_BREEZE_LAND, 1.5f, 1.0f);
    }

    /**
     * Random blink for evasion
     */
    private void randomBlink() {
        if (breeze == null) return;

        Location loc = breeze.getLocation();
        int range = isEnraged ? blinkRange + 1 : blinkRange;

        // Random direction
        double angle = Math.random() * Math.PI * 2;
        double x = Math.cos(angle) * range;
        double z = Math.sin(angle) * range;

        Location blinkLoc = loc.clone().add(x, 2, z);

        // Effects
        world.spawnParticle(Particle.CLOUD, loc, 20, 0.3, 0.3, 0.3, 0.05);
        world.playSound(loc, Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.2f);

        breeze.teleport(blinkLoc);

        world.spawnParticle(Particle.CLOUD, blinkLoc, 20, 0.3, 0.3, 0.3, 0.05);
    }

    /**
     * Trigger enrage mode
     */
    private void triggerEnrage() {
        isEnraged = true;

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§b§l⚠ THE BREEZE IS ENRAGED! ⚠");
        Bukkit.broadcastMessage("§7Attacks faster, blink range increased!");
        Bukkit.broadcastMessage("");

        world.playSound(breeze.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 2.0f, 0.5f);

        // Visual effect - wind vortex
        for (int i = 0; i < 10; i++) {
            final int step = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (breeze != null) {
                    Location loc = breeze.getLocation();
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double r = step * 0.5;
                        double x = Math.cos(angle + step * 0.3) * r;
                        double z = Math.sin(angle + step * 0.3) * r;
                        world.spawnParticle(Particle.CLOUD, loc.clone().add(x, step * 0.3, z), 2, 0, 0, 0, 0);
                    }
                }
            }, i * 2L);
        }
    }

    /**
     * Handle damage to the breeze
     */
    @EventHandler
    public void onBreezeDamage(EntityDamageByEntityEvent event) {
        if (!isActive || breeze == null) return;
        if (!event.getEntity().equals(breeze)) return;

        // Track attacker
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        if (attacker != null) {
            recentAttackers.put(attacker.getUniqueId(), System.currentTimeMillis());
        }

        // Apply armor reduction
        event.setDamage(event.getDamage() * (1.0 - armorReduction));
    }

    /**
     * Handle breeze death
     */
    @EventHandler
    public void onBreezeDeath(EntityDeathEvent event) {
        if (!isActive || breeze == null) return;
        if (!event.getEntity().equals(breeze)) return;

        onDeath();
    }

    /**
     * Handle death
     */
    private void onDeath() {
        isActive = false;

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }

        // Broadcast victory
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("§a§lTHE ASCENDED BREEZE HAS BEEN DEFEATED!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The winds grow calm...");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Drop rewards
        if (breeze != null) {
            dropRewards(breeze.getLocation());
        }
    }

    /**
     * Drop rewards at location
     */
    private void dropRewards(Location loc) {
        // Breeze Heart - gives Breeze attribute
        ItemStack breezeHeart = new ItemStack(Material.WIND_CHARGE);
        ItemMeta meta = breezeHeart.getItemMeta();
        meta.setDisplayName("§b§lBreeze Heart");
        List<String> lore = new ArrayList<>();
        lore.add("§7A heart swirling with wind energy");
        lore.add("§7Grants the §bBreeze §7attribute");
        lore.add("");
        lore.add("§c§lCANNOT BE DROPPED OR STORED");
        meta.setLore(lore);
        breezeHeart.setItemMeta(meta);

        loc.getWorld().dropItemNaturally(loc, breezeHeart);

        // Additional loot
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.BREEZE_ROD, 4));

        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_DEATH, 2.0f, 1.0f);
    }

    /**
     * Spawn breeze particles
     */
    private void spawnBreezeParticles() {
        if (breeze == null) return;
        Location loc = breeze.getLocation();
        world.spawnParticle(Particle.CLOUD, loc, 5, 0.5, 0.5, 0.5, 0.02);
        world.spawnParticle(Particle.DUST, loc, 3, 0.5, 0.5, 0.5, 0,
                new Particle.DustOptions(Color.fromRGB(200, 230, 255), 1.0f));
    }

    /**
     * Get nearest player
     */
    private Player getNearestPlayer() {
        if (breeze == null) return null;

        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : world.getPlayers()) {
            double dist = player.getLocation().distance(breeze.getLocation());
            if (dist < nearestDist && dist < 50) {
                nearest = player;
                nearestDist = dist;
            }
        }

        return nearest;
    }

    /**
     * Update boss bar
     */
    private void updateBossBar() {
        if (bossBar == null || breeze == null) return;

        double progress = breeze.getHealth() / maxHealth;
        bossBar.setProgress(Math.max(0, Math.min(1, progress)));

        if (isEnraged) {
            bossBar.setTitle("§b§lASCENDED BREEZE §c§l[ENRAGED]");
            bossBar.setColor(BarColor.RED);
        }
    }

    /**
     * Check if boss is active
     */
    public boolean isActive() {
        return isActive;
    }
}
