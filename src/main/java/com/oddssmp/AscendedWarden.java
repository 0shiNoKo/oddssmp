package com.oddssmp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
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

public class AscendedWarden implements Listener {

    private final OddsSMP plugin;
    private Warden warden;
    private BossBar bossBar;
    private boolean isActive = false;
    private Location arenaCenter;
    private World world;

    // Stats
    private final double maxHealth = 1600.0; // 800 hearts
    private final double armorReduction = 0.55; // 55% damage reduction
    private final double knockbackResistance = 1.0; // 100%

    // Deep Dark Zone
    private double zoneRadius = 16.0;
    private final double zoneDamage = 3.0; // per second

    // Sonic Slam
    private final double slamDamage = 16.0;
    private final double slamAirborneDamage = 8.0; // extra damage
    private int slamCooldownTicks = 100; // 5 seconds base

    // Anti-zerg tracking
    private final Map<UUID, Long> recentAttackers = new HashMap<>();
    private static final long ATTACK_WINDOW = 5000; // 5 seconds

    // Enrage
    private boolean isEnraged = false;

    public AscendedWarden(OddsSMP plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Spawn the Ascended Warden
     */
    public void spawn(Location location) {
        this.arenaCenter = location;
        this.world = location.getWorld();

        // Spawn warden
        warden = (Warden) world.spawnEntity(location.clone().add(0, 1, 0), EntityType.WARDEN);

        // Configure warden
        warden.setCustomName("§8§l§kA§r §3§lASCENDED WARDEN §8§l§kA");
        warden.setCustomNameVisible(true);
        warden.setMaxHealth(maxHealth);
        warden.setHealth(maxHealth);

        // Set knockback resistance
        try {
            var attr = warden.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (attr != null) attr.setBaseValue(knockbackResistance);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set knockback resistance: " + e.getMessage());
        }

        // Create boss bar
        bossBar = Bukkit.createBossBar(
                "§3§lASCENDED WARDEN",
                BarColor.BLUE,
                BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        isActive = true;

        // Start boss AI
        startBossAI();
        startDeepDarkZone();

        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("§3§l⚠ ASCENDED WARDEN HAS EMERGED ⚠");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The deep dark hungers...");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Play sound
        world.playSound(location, Sound.ENTITY_WARDEN_EMERGE, 2.0f, 0.8f);
    }

    /**
     * Start the boss AI loop
     */
    private void startBossAI() {
        new BukkitRunnable() {
            int tick = 0;
            int slamCooldown = 0;

            @Override
            public void run() {
                if (!isActive || warden == null || warden.isDead()) {
                    cancel();
                    return;
                }

                // Update boss bar
                updateBossBar();

                // Add nearby players to boss bar
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distance(warden.getLocation()) < 100) {
                        if (!bossBar.getPlayers().contains(player)) {
                            bossBar.addPlayer(player);
                        }
                    } else {
                        bossBar.removePlayer(player);
                    }
                }

                // Check enrage
                if (!isEnraged && warden.getHealth() < maxHealth * 0.30) {
                    triggerEnrage();
                }

                // Sonic Slam attack
                if (slamCooldown <= 0) {
                    Player target = getNearestPlayer();
                    if (target != null && target.getLocation().distance(warden.getLocation()) < 10) {
                        sonicSlamAttack(target);
                        slamCooldown = isEnraged ? (int)(slamCooldownTicks * 0.7) : slamCooldownTicks;
                    }
                } else {
                    slamCooldown--;
                }

                // Sonic Boom (ranged attack)
                if (tick % 80 == 0) { // Every 4 seconds
                    sonicBoomAttack();
                }

                // Passive particles
                if (tick % 5 == 0) {
                    spawnWardenParticles();
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Deep Dark Zone - AoE damage zone around warden
     */
    private void startDeepDarkZone() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || warden == null || warden.isDead()) {
                    cancel();
                    return;
                }

                Location loc = warden.getLocation();
                double currentRadius = isEnraged ? zoneRadius * 1.5 : zoneRadius;

                // Visual effect - ring of darkness
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;
                    Location particleLoc = loc.clone().add(x, 0.5, z);
                    world.spawnParticle(Particle.SCULK_CHARGE_POP, particleLoc, 1, 0, 0, 0, 0);
                }

                // Damage players in zone
                for (Entity entity : warden.getNearbyEntities(currentRadius, currentRadius, currentRadius)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        double distance = player.getLocation().distance(loc);
                        if (distance <= currentRadius) {
                            // More damage closer to center
                            double damageMultiplier = 1.0 + (1.0 - (distance / currentRadius)) * 0.5;
                            player.damage(zoneDamage * damageMultiplier, warden);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
    }

    /**
     * Sonic Slam Attack - melee range AoE
     */
    private void sonicSlamAttack(Player target) {
        if (warden == null) return;

        Location loc = warden.getLocation();

        // Visual effect
        world.spawnParticle(Particle.SONIC_BOOM, loc.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 1, 0), 10, 1, 1, 1, 0.1);

        // Sound
        world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.7f);

        // Damage nearby players
        for (Entity entity : warden.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                // Extra damage if airborne
                double damage = slamDamage;
                if (!player.isOnGround()) {
                    damage += slamAirborneDamage;
                }

                player.damage(damage, warden);

                // Knockback
                Vector knockback = player.getLocation().toVector()
                        .subtract(loc.toVector()).normalize().multiply(1.5);
                knockback.setY(0.5);
                player.setVelocity(knockback);
            }
        }
    }

    /**
     * Sonic Boom Attack - ranged
     */
    private void sonicBoomAttack() {
        if (warden == null) return;

        Player target = getNearestPlayer();
        if (target == null || target.getLocation().distance(warden.getLocation()) > 30) return;

        Location wardenLoc = warden.getLocation().add(0, 1.5, 0);
        Location targetLoc = target.getLocation().add(0, 1, 0);

        // Sound
        world.playSound(wardenLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);

        // Particle beam
        Vector direction = targetLoc.toVector().subtract(wardenLoc.toVector()).normalize();
        for (double d = 0; d < wardenLoc.distance(targetLoc); d += 0.5) {
            Location point = wardenLoc.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
        }

        // Damage target
        target.damage(getDamageOutput(), warden);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
    }

    /**
     * Get damage output based on HP percentage
     */
    private double getDamageOutput() {
        if (warden == null) return 14.0;

        double healthPercent = warden.getHealth() / maxHealth;

        if (healthPercent > 0.8) return 14.0;
        if (healthPercent > 0.6) return 18.0;
        if (healthPercent > 0.4) return 22.0;
        if (healthPercent > 0.2) return 26.0;
        return 32.0;
    }

    /**
     * Trigger enrage mode
     */
    private void triggerEnrage() {
        isEnraged = true;

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§3§l⚠ THE WARDEN IS ENRAGED! ⚠");
        Bukkit.broadcastMessage("§7Deep Dark Zone expanded, Slam cooldown reduced!");
        Bukkit.broadcastMessage("");

        world.playSound(warden.getLocation(), Sound.ENTITY_WARDEN_ROAR, 2.0f, 0.5f);

        // Visual effect
        for (int i = 0; i < 5; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (warden != null) {
                    world.spawnParticle(Particle.SCULK_SOUL, warden.getLocation(), 50, 2, 2, 2, 0.1);
                    world.spawnParticle(Particle.SONIC_BOOM, warden.getLocation().add(0, 1, 0), 3, 1, 1, 1, 0);
                }
            }, i * 10L);
        }
    }

    /**
     * Handle damage to the warden
     */
    @EventHandler
    public void onWardenDamage(EntityDamageByEntityEvent event) {
        if (!isActive || warden == null) return;
        if (!event.getEntity().equals(warden)) return;

        // Track attacker for anti-zerg
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

        // Clean old attackers
        long now = System.currentTimeMillis();
        recentAttackers.entrySet().removeIf(e -> now - e.getValue() > ATTACK_WINDOW);

        // Anti-zerg: 5% reduction per attacker after 5, max 40%
        int extraAttackers = Math.max(0, recentAttackers.size() - 5);
        double reduction = Math.min(0.40, extraAttackers * 0.05);
        if (reduction > 0) {
            event.setDamage(event.getDamage() * (1.0 - reduction));
        }

        // Apply armor reduction
        event.setDamage(event.getDamage() * (1.0 - armorReduction));
    }

    /**
     * Handle warden death
     */
    @EventHandler
    public void onWardenDeath(EntityDeathEvent event) {
        if (!isActive || warden == null) return;
        if (!event.getEntity().equals(warden)) return;

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
        Bukkit.broadcastMessage("§a§lTHE ASCENDED WARDEN HAS BEEN DEFEATED!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The deep dark falls silent...");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Drop rewards
        if (warden != null) {
            dropRewards(warden.getLocation());
        }
    }

    /**
     * Drop rewards at location
     */
    private void dropRewards(Location loc) {
        // Warden Brain - gives Warden attribute
        ItemStack wardenBrain = new ItemStack(Material.SCULK_CATALYST);
        ItemMeta meta = wardenBrain.getItemMeta();
        meta.setDisplayName("§3§lWarden Brain");
        List<String> lore = new ArrayList<>();
        lore.add("§7A brain pulsing with sculk energy");
        lore.add("§7Grants the §3Warden §7attribute");
        lore.add("");
        lore.add("§c§lCANNOT BE DROPPED OR STORED");
        meta.setLore(lore);
        wardenBrain.setItemMeta(meta);

        loc.getWorld().dropItemNaturally(loc, wardenBrain);

        // Additional loot
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.ECHO_SHARD, 8));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.SCULK_SHRIEKER, 4));

        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_DEATH, 2.0f, 1.0f);
    }

    /**
     * Spawn warden particles
     */
    private void spawnWardenParticles() {
        if (warden == null) return;
        Location loc = warden.getLocation();
        world.spawnParticle(Particle.SCULK_SOUL, loc, 10, 1, 1, 1, 0.02);
        world.spawnParticle(Particle.DUST, loc, 5, 1, 1, 1, 0,
                new Particle.DustOptions(Color.fromRGB(0, 100, 100), 1.5f));
    }

    /**
     * Get nearest player
     */
    private Player getNearestPlayer() {
        if (warden == null) return null;

        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : world.getPlayers()) {
            double dist = player.getLocation().distance(warden.getLocation());
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
        if (bossBar == null || warden == null) return;

        double progress = warden.getHealth() / maxHealth;
        bossBar.setProgress(Math.max(0, Math.min(1, progress)));

        if (isEnraged) {
            bossBar.setTitle("§3§lASCENDED WARDEN §c§l[ENRAGED]");
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
