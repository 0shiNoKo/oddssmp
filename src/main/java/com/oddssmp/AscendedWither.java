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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AscendedWither implements Listener {

    private final OddsSMP plugin;
    private Wither wither;
    private BossBar bossBar;
    private boolean isActive = false;
    private Location arenaCenter;
    private World world;

    // Stats
    private final double maxHealth = 1200.0; // 600 hearts
    private final double armorReduction = 0.45; // 45% damage reduction
    private final double knockbackResistance = 0.90; // 90%
    private final double regenPerSecond = 2.0; // Below 50% HP

    // Anti-zerg tracking
    private final Map<UUID, Long> recentAttackers = new HashMap<>();
    private static final long ATTACK_WINDOW = 5000; // 5 seconds

    // Enrage
    private boolean isEnraged = false;

    public AscendedWither(OddsSMP plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Spawn the Ascended Wither
     */
    public void spawn(Location location) {
        this.arenaCenter = location;
        this.world = location.getWorld();

        // Spawn wither
        wither = (Wither) world.spawnEntity(location.clone().add(0, 5, 0), EntityType.WITHER);

        // Configure wither
        wither.setCustomName("§8§l§kA§r §5§lASCENDED WITHER §8§l§kA");
        wither.setCustomNameVisible(true);
        wither.setMaxHealth(maxHealth);
        wither.setHealth(maxHealth);

        // Set knockback resistance
        try {
            var attr = wither.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (attr != null) attr.setBaseValue(knockbackResistance);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set knockback resistance: " + e.getMessage());
        }

        // Create boss bar
        bossBar = Bukkit.createBossBar(
                "§5§lASCENDED WITHER",
                BarColor.PURPLE,
                BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        isActive = true;

        // Start boss AI
        startBossAI();
        startBlockDestruction();

        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("§5§l⚠ ASCENDED WITHER HAS AWAKENED ⚠");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The darkness consumes all...");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Play sound
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
    }

    /**
     * Start the boss AI loop
     */
    private void startBossAI() {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!isActive || wither == null || wither.isDead()) {
                    cancel();
                    return;
                }

                // Update boss bar
                updateBossBar();

                // Add nearby players to boss bar
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distance(wither.getLocation()) < 100) {
                        if (!bossBar.getPlayers().contains(player)) {
                            bossBar.addPlayer(player);
                        }
                    } else {
                        bossBar.removePlayer(player);
                    }
                }

                // Regeneration below 50% HP
                if (wither.getHealth() < maxHealth * 0.5 && wither.getHealth() > 0) {
                    double newHealth = Math.min(maxHealth, wither.getHealth() + (regenPerSecond / 20.0));
                    wither.setHealth(newHealth);
                }

                // Check enrage
                if (!isEnraged && wither.getHealth() < maxHealth * 0.15) {
                    triggerEnrage();
                }

                // Special attacks
                if (tick % 100 == 0) { // Every 5 seconds
                    shadowPulseAttack();
                }

                if (tick % 60 == 30) { // Offset from shadow pulse
                    witherSkullBarrage();
                }

                // Passive particles
                if (tick % 5 == 0) {
                    spawnWitherParticles();
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Shadow Pulse Attack - AoE damage
     */
    private void shadowPulseAttack() {
        if (wither == null) return;

        Location loc = wither.getLocation();
        double radius = 8.0;

        // Visual effect
        ParticleManager.playBlueShockwave(loc, radius);
        world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.5f);

        // Damage nearby players
        for (Entity entity : wither.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.damage(10.0, wither);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            }
        }
    }

    /**
     * Wither Skull Barrage
     */
    private void witherSkullBarrage() {
        if (wither == null) return;

        // Find nearest player
        Player target = getNearestPlayer();
        if (target == null) return;

        Location witherLoc = wither.getLocation().add(0, 2, 0);

        // Shoot multiple skulls
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (wither != null && !wither.isDead() && target.isOnline()) {
                    Vector direction = target.getLocation().add(0, 1, 0).toVector()
                            .subtract(witherLoc.toVector()).normalize();

                    // Add some spread
                    direction.add(new Vector(
                            (Math.random() - 0.5) * 0.3,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.3
                    )).normalize();

                    WitherSkull skull = wither.launchProjectile(WitherSkull.class, direction.multiply(1.5));
                    skull.setCharged(Math.random() < 0.3); // 30% chance for blue skull
                }
            }, i * 5L);
        }

        world.playSound(witherLoc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f);
    }

    /**
     * Get damage output based on HP percentage
     */
    private double getDamageOutput() {
        if (wither == null) return 14.0;

        double healthPercent = wither.getHealth() / maxHealth;

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
        Bukkit.broadcastMessage("§5§l⚠ THE WITHER IS ENRAGED! ⚠");
        Bukkit.broadcastMessage("§7Attack speed increased, immune to stun!");
        Bukkit.broadcastMessage("");

        world.playSound(wither.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.5f);

        // Visual effect
        for (int i = 0; i < 5; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (wither != null) {
                    ParticleManager.playDarkSlash((Player) null);
                    world.spawnParticle(Particle.SMOKE, wither.getLocation(), 100, 2, 2, 2, 0.1);
                }
            }, i * 10L);
        }
    }

    /**
     * Handle damage to the wither
     */
    @EventHandler
    public void onWitherDamage(EntityDamageByEntityEvent event) {
        if (!isActive || wither == null) return;
        if (!event.getEntity().equals(wither)) return;

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

        // Anti-zerg: 30% less damage if more than 6 players
        if (recentAttackers.size() > 6) {
            event.setDamage(event.getDamage() * 0.7);
        }

        // Apply armor reduction
        event.setDamage(event.getDamage() * (1.0 - armorReduction));
    }

    /**
     * Handle wither dealing damage
     */
    @EventHandler
    public void onWitherAttack(EntityDamageByEntityEvent event) {
        if (!isActive || wither == null) return;

        Entity damager = event.getDamager();
        if (damager instanceof WitherSkull) {
            WitherSkull skull = (WitherSkull) damager;
            if (skull.getShooter() != null && skull.getShooter().equals(wither)) {
                event.setDamage(getDamageOutput());
            }
        }
    }

    /**
     * Handle wither death
     */
    @EventHandler
    public void onWitherDeath(EntityDeathEvent event) {
        if (!isActive || wither == null) return;
        if (!event.getEntity().equals(wither)) return;

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
        Bukkit.broadcastMessage("§a§lTHE ASCENDED WITHER HAS BEEN DEFEATED!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The darkness recedes...");
        Bukkit.broadcastMessage("§8§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Drop rewards
        if (wither != null) {
            dropRewards(wither.getLocation());
        }
    }

    /**
     * Drop rewards at location
     */
    private void dropRewards(Location loc) {
        // Wither Bone - gives Wither attribute
        ItemStack witherBone = new ItemStack(Material.BONE);
        ItemMeta meta = witherBone.getItemMeta();
        meta.setDisplayName("§5§lWither Bone");
        List<String> lore = new ArrayList<>();
        lore.add("§7A bone infused with wither essence");
        lore.add("§7Grants the §5Wither §7attribute");
        lore.add("");
        lore.add("§c§lCANNOT BE DROPPED OR STORED");
        meta.setLore(lore);
        witherBone.setItemMeta(meta);

        loc.getWorld().dropItemNaturally(loc, witherBone);

        // Additional loot
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR));

        // Sound
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_DEATH, 2.0f, 1.0f);
    }

    /**
     * Spawn wither particles
     */
    private void spawnWitherParticles() {
        if (wither == null) return;
        Location loc = wither.getLocation();
        world.spawnParticle(Particle.SMOKE, loc, 20, 1, 1, 1, 0.02);
        world.spawnParticle(Particle.DUST, loc, 10, 1, 1, 1, 0,
                new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.5f));
    }

    /**
     * Start block destruction task
     */
    private void startBlockDestruction() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || wither == null || wither.isDead()) {
                    cancel();
                    return;
                }

                Location loc = wither.getLocation();
                int radius = 3;

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block block = loc.clone().add(x, y, z).getBlock();
                            Material type = block.getType();

                            if (type != Material.AIR &&
                                type != Material.BEDROCK &&
                                type != Material.END_PORTAL &&
                                type != Material.END_PORTAL_FRAME) {
                                block.breakNaturally();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /**
     * Get nearest player
     */
    private Player getNearestPlayer() {
        if (wither == null) return null;

        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : world.getPlayers()) {
            double dist = player.getLocation().distance(wither.getLocation());
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
        if (bossBar == null || wither == null) return;

        double progress = wither.getHealth() / maxHealth;
        bossBar.setProgress(Math.max(0, Math.min(1, progress)));

        if (isEnraged) {
            bossBar.setTitle("§5§lASCENDED WITHER §c§l[ENRAGED]");
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
