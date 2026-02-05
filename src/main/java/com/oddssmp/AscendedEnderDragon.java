package com.oddssmp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AscendedEnderDragon {

    private final OddsSMP plugin;
    private EnderDragon dragon;
    private World world;
    private Location arenaCenter;
    private BossBar bossBar;

    // Boss stats
    private final double maxHealth = 1024.0; // 512 hearts (max allowed by Minecraft)
    private int currentPhase = 1;
    private boolean isActive = false;
    private long lastPhaseShift = 0;
    private boolean hasUsedPhaseShift = false;

    // Attack cooldowns (in ticks)
    private long lastVoidDive = 0;
    private long lastRealityBreath = 0;
    private long lastTailRend = 0;
    private long lastVoidOrbs = 0;
    private long lastGravityPull = 0;
    private long lastCrystalOverload = 0;
    private long lastVoidStorm = 0;
    private long lastObliterationBeam = 0;

    // Active effects
    private Set<Location> corruptedZones = new HashSet<>();
    private Set<UUID> playersInArena = new HashSet<>();
    private boolean isCrystalOverloadActive = false;
    private boolean isPhaseShifted = false;

    public AscendedEnderDragon(OddsSMP plugin, World world, Location arenaCenter) {
        this.plugin = plugin;
        this.world = world;
        this.arenaCenter = arenaCenter;
    }

    /**
     * Spawn the Ascended Ender Dragon
     */
    public void spawn() {
        // Spawn dragon at arena center
        dragon = (EnderDragon) world.spawnEntity(arenaCenter.clone().add(0, 50, 0), EntityType.ENDER_DRAGON);

        // Configure dragon
        dragon.setCustomName("§5§l§kA§r §d§lASCENDED ENDER DRAGON §5§l§kA");
        dragon.setCustomNameVisible(true);
        dragon.setMaxHealth(maxHealth);
        dragon.setHealth(maxHealth);
        dragon.setPhase(EnderDragon.Phase.CIRCLING);
        dragon.setAI(true); // Enable AI

        // Make dragon much bigger (3x normal size)
        try {
            // Access GENERIC_SCALE directly via registry key for Paper 1.21+
            org.bukkit.attribute.AttributeInstance scaleAttr = dragon.getAttribute(
                    org.bukkit.attribute.Attribute.valueOf("GENERIC_SCALE")
            );
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(3.0);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("GENERIC_SCALE attribute not found - dragon will be normal size");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not set dragon scale: " + e.getMessage());
        }

        // Create boss bar
        bossBar = Bukkit.createBossBar(
                "§5§lASCENDED ENDER DRAGON",
                BarColor.PURPLE,
                BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        isActive = true;

        // Start boss AI
        startBossAI();

        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l§m                                                    ");
        Bukkit.broadcastMessage("§d§l⚠ ASCENDED ENDER DRAGON HAS AWAKENED ⚠");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The void trembles as an ancient power emerges...");
        Bukkit.broadcastMessage("§5§l§m                                                    ");
        Bukkit.broadcastMessage("");
    }

    /**
     * Start the boss AI loop
     */
    private void startBossAI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || dragon == null || dragon.isDead()) {
                    onDeath();
                    cancel();
                    return;
                }

                // Update phase based on health
                updatePhase();

                // Update boss bar
                updateBossBar();

                // Update players in arena
                updatePlayersInArena();

                // Apply corrupted zone damage
                tickCorruptedZones();

                // Execute attacks based on phase
                executeAttacks();
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
    }

    /**
     * Update current phase based on health
     */
    private void updatePhase() {
        double healthPercent = dragon.getHealth() / maxHealth;

        if (healthPercent > 0.70) {
            currentPhase = 1;
        } else if (healthPercent > 0.35) {
            if (currentPhase == 1) {
                currentPhase = 2;
                announcePhase(2);
            }
        } else {
            if (currentPhase == 2) {
                currentPhase = 3;
                announcePhase(3);
            }
        }
    }

    /**
     * Announce phase transition
     */
    private void announcePhase(int phase) {
        String message = phase == 2 ?
                "§c§lThe dragon grows more aggressive!" :
                "§4§lThe dragon enters its final form!";

        for (UUID uuid : playersInArena) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle("§5§lPHASE " + phase, message, 10, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
            }
        }
    }

    /**
     * Update boss bar
     */
    private void updateBossBar() {
        double healthPercent = dragon.getHealth() / maxHealth;
        bossBar.setProgress(Math.max(0, Math.min(1, healthPercent)));

        // Update title with health
        int currentHealth = (int) dragon.getHealth();
        int maxHealthInt = (int) maxHealth;
        bossBar.setTitle("§5§lASCENDED ENDER DRAGON §7- §c" + currentHealth + "§7/§c" + maxHealthInt + " HP");

        // Update color based on phase
        if (currentPhase == 1) {
            bossBar.setColor(BarColor.PURPLE);
        } else if (currentPhase == 2) {
            bossBar.setColor(BarColor.RED);
        } else {
            bossBar.setColor(BarColor.WHITE);
        }

        // Add/remove players from boss bar
        for (UUID uuid : playersInArena) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && !bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }

        // Remove players no longer in arena
        List<Player> toRemove = new ArrayList<>();
        for (Player player : bossBar.getPlayers()) {
            if (!playersInArena.contains(player.getUniqueId())) {
                toRemove.add(player);
            }
        }
        for (Player player : toRemove) {
            bossBar.removePlayer(player);
        }
    }

    /**
     * Update list of players in arena
     */
    private void updatePlayersInArena() {
        playersInArena.clear();
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(arenaCenter) <= 100) {
                playersInArena.add(player.getUniqueId());
            }
        }
    }

    /**
     * Execute attacks based on phase and cooldowns
     */
    private void executeAttacks() {
        long currentTime = System.currentTimeMillis();

        // Phase 1 attacks
        if (currentTime - lastVoidDive > 12000) {
            voidDive();
            lastVoidDive = currentTime;
        }

        if (currentTime - lastRealityBreath > 15000) {
            realityBreath();
            lastRealityBreath = currentTime;
        }

        if (currentTime - lastTailRend > 10000) {
            tailRend();
            lastTailRend = currentTime;
        }

        // Phase 2 attacks
        if (currentPhase >= 2) {
            if (currentTime - lastVoidOrbs > 14000) {
                voidOrbs();
                lastVoidOrbs = currentTime;
            }

            if (currentTime - lastGravityPull > 20000) {
                gravityPull();
                lastGravityPull = currentTime;
            }

            if (currentTime - lastCrystalOverload > 25000) {
                crystalOverload();
                lastCrystalOverload = currentTime;
            }
        }

        // Phase 3 attacks
        if (currentPhase >= 3) {
            if (!hasUsedPhaseShift) {
                phaseShift();
                hasUsedPhaseShift = true;
            }

            if (currentTime - lastVoidStorm > 30000) {
                voidStorm();
                lastVoidStorm = currentTime;
            }

            if (currentTime - lastObliterationBeam > 35000) {
                obliterationBeam();
                lastObliterationBeam = currentTime;
            }
        }
    }

    // ========== PHASE 1 ATTACKS ==========

    /**
     * VOID DIVE - Targets single player with high-speed dive
     */
    private void voidDive() {
        Player target = getRandomPlayer();
        if (target == null) return;

        Location targetLoc = target.getLocation();

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 40 || dragon == null || dragon.isDead()) {
                    cancel();
                    return;
                }

                // Dive toward target
                Vector direction = targetLoc.toVector().subtract(dragon.getLocation().toVector()).normalize();
                dragon.setVelocity(direction.multiply(2.0));

                // Check if close enough for impact
                if (dragon.getLocation().distance(targetLoc) < 8) {
                    // AoE damage
                    for (Entity entity : dragon.getNearbyEntities(8, 8, 8)) {
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            player.damage(40.0, dragon);

                            // Downward pull
                            Vector knockback = new Vector(0, -3, 0);
                            player.setVelocity(knockback);
                        }
                    }

                    // Particles
                    dragon.getWorld().spawnParticle(Particle.PORTAL, targetLoc, 200, 4, 4, 4, 0.5);
                    dragon.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * REALITY BREATH - Breath leaves black corrupted zones
     */
    private void realityBreath() {
        Location breathLoc = dragon.getEyeLocation();
        Vector direction = dragon.getLocation().getDirection();

        for (int i = 0; i < 20; i++) {
            Location zoneLoc = breathLoc.clone().add(direction.clone().multiply(i * 2));
            corruptedZones.add(zoneLoc);

            // Spawn particles
            world.spawnParticle(Particle.SQUID_INK, zoneLoc, 50, 3, 1, 3, 0.1);
        }

        // Remove zones after 6 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : new HashSet<>(corruptedZones)) {
                    corruptedZones.remove(loc);
                }
            }
        }.runTaskLater(plugin, 120L); // 6 seconds
    }

    /**
     * TAIL REND - Sweeps behind dragon
     */
    private void tailRend() {
        Location tailLoc = dragon.getLocation().clone().subtract(dragon.getLocation().getDirection().multiply(3));

        for (Entity entity : dragon.getNearbyEntities(6, 6, 6)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                // Check if behind dragon
                Vector toPlayer = player.getLocation().toVector().subtract(dragon.getLocation().toVector());
                double dot = toPlayer.normalize().dot(dragon.getLocation().getDirection());

                if (dot < 0) { // Behind dragon
                    player.damage(15.0, dragon);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                }
            }
        }

        // Particles
        world.spawnParticle(Particle.SWEEP_ATTACK, tailLoc, 30, 3, 2, 3, 0.1);
        world.playSound(tailLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.7f);
    }

    // ========== PHASE 2 ATTACKS ==========

    /**
     * VOID ORBS - Fires 3 slow-moving orbs
     */
    private void voidOrbs() {
        Location eyeLoc = dragon.getEyeLocation();
        Vector direction = dragon.getLocation().getDirection();

        for (int i = 0; i < 3; i++) {
            final int index = i;
            new BukkitRunnable() {
                Location orbLoc = eyeLoc.clone();
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 100 || !isActive) {
                        explodeOrb(orbLoc);
                        cancel();
                        return;
                    }

                    // Move orb
                    Vector offset = direction.clone().rotateAroundY(Math.toRadians(index * 120));
                    orbLoc.add(offset.multiply(0.3));

                    // Particles
                    world.spawnParticle(Particle.DRAGON_BREATH, orbLoc, 10, 0.5, 0.5, 0.5, 0.01);

                    // Check collision with players
                    for (Entity entity : world.getNearbyEntities(orbLoc, 2, 2, 2)) {
                        if (entity instanceof Player) {
                            explodeOrb(orbLoc);
                            cancel();
                            return;
                        }
                    }
                }
            }.runTaskTimer(plugin, i * 10L, 1L);
        }
    }

    private void explodeOrb(Location loc) {
        // AoE damage
        for (Entity entity : world.getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.damage(25.0, dragon);
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2));
            }
        }

        // Particles
        world.spawnParticle(Particle.EXPLOSION, loc, 20, 1.5, 1.5, 1.5, 0.1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
    }

    /**
     * GRAVITY PULL - Pulls all players toward arena center
     */
    private void gravityPull() {
        for (UUID uuid : playersInArena) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            if (player.getLocation().distance(arenaCenter) <= 12) {
                // Pull toward center
                Vector direction = arenaCenter.toVector().subtract(player.getLocation().toVector()).normalize();
                player.setVelocity(direction.multiply(0.5));

                // Disable sprinting
                player.setSprinting(false);
            }
        }

        // Apply damage over time
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 100 || !isActive) {
                    cancel();
                    return;
                }

                for (UUID uuid : playersInArena) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.getLocation().distance(arenaCenter) <= 12) {
                        if (ticks % 20 == 0) { // Every second
                            player.damage(2.0, dragon);
                        }

                        // Particles
                        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 10, 0.5, 1, 0.5, 0.1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * CRYSTAL OVERLOAD - Links to remaining end crystals
     */
    private void crystalOverload() {
        List<EnderCrystal> crystals = world.getEntitiesByClass(EnderCrystal.class).stream()
                .filter(c -> c.getLocation().distance(arenaCenter) <= 100)
                .toList();

        if (crystals.isEmpty()) return;

        isCrystalOverloadActive = true;

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 120 || !isActive) { // 6 seconds
                    isCrystalOverloadActive = false;
                    cancel();
                    return;
                }

                // Heal dragon
                if (ticks % 20 == 0 && dragon != null && !dragon.isDead()) {
                    double healAmount = maxHealth * 0.10; // 10% per second
                    dragon.setHealth(Math.min(maxHealth, dragon.getHealth() + healAmount));
                }

                // Damage nearby players
                for (EnderCrystal crystal : crystals) {
                    if (crystal.isDead()) continue;

                    // Beam effect
                    world.spawnParticle(Particle.END_ROD, crystal.getLocation(), 5, 0.2, 0.2, 0.2, 0.1);

                    for (Entity entity : crystal.getNearbyEntities(5, 5, 5)) {
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            if (ticks % 20 == 0) {
                                player.damage(6.0, dragon);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ========== PHASE 3 ATTACKS ==========

    /**
     * PHASE SHIFT - Dragon becomes untargetable and spawns phantoms
     */
    private void phaseShift() {
        isPhaseShifted = true;
        dragon.setInvulnerable(true);
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));

        // Spawn 4-6 Ender Phantoms
        int phantomCount = 4 + (int)(Math.random() * 3);
        for (int i = 0; i < phantomCount; i++) {
            Location spawnLoc = arenaCenter.clone().add(
                    (Math.random() - 0.5) * 40,
                    20,
                    (Math.random() - 0.5) * 40
            );

            Phantom phantom = (Phantom) world.spawnEntity(spawnLoc, EntityType.PHANTOM);
            phantom.setCustomName("§5Ender Phantom");
            phantom.setSize(3);
        }

        // End phase shift after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dragon != null && !dragon.isDead()) {
                    dragon.setInvulnerable(false);
                    dragon.removePotionEffect(PotionEffectType.INVISIBILITY);
                    isPhaseShifted = false;
                }
            }
        }.runTaskLater(plugin, 200L); // 10 seconds
    }

    /**
     * VOID STORM - Arena fills with falling void fragments
     */
    private void voidStorm() {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 160 || !isActive) { // 8 seconds
                    cancel();
                    return;
                }

                // Spawn fragments every 10 ticks
                if (ticks % 10 == 0) {
                    for (int i = 0; i < 5; i++) {
                        Location fragmentLoc = arenaCenter.clone().add(
                                (Math.random() - 0.5) * 50,
                                30,
                                (Math.random() - 0.5) * 50
                        );

                        spawnVoidFragment(fragmentLoc);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnVoidFragment(Location start) {
        new BukkitRunnable() {
            Location loc = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ >= 60 || loc.getY() <= arenaCenter.getY()) {
                    // Impact
                    for (Entity entity : world.getNearbyEntities(loc, 4, 4, 4)) {
                        if (entity instanceof Player) {
                            ((Player) entity).damage(20.0, dragon);
                        }
                    }

                    world.spawnParticle(Particle.EXPLOSION, loc, 15, 2, 2, 2, 0.1);
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
                    cancel();
                    return;
                }

                // Fall
                loc.subtract(0, 0.5, 0);
                world.spawnParticle(Particle.PORTAL, loc, 10, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * OBLITERATION BEAM - Charges and fires devastating beam
     */
    private void obliterationBeam() {
        Player target = getRandomPlayer();
        if (target == null) return;

        Location dragonLoc = dragon.getEyeLocation();
        Vector direction = target.getLocation().toVector().subtract(dragonLoc.toVector()).normalize();

        // Charging phase (2 seconds)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 40) {
                    // Fire beam
                    fireBeam(dragonLoc, direction);
                    cancel();
                    return;
                }

                // Charging particles
                world.spawnParticle(Particle.DRAGON_BREATH, dragonLoc, 20, 1, 1, 1, 0.1);
                world.playSound(dragonLoc, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 2.0f);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void fireBeam(Location start, Vector direction) {
        for (int i = 0; i < 30; i++) {
            Location beamLoc = start.clone().add(direction.clone().multiply(i));

            // Damage players in beam
            for (Entity entity : world.getNearbyEntities(beamLoc, 1.5, 1.5, 1.5)) {
                if (entity instanceof Player) {
                    ((Player) entity).damage(50.0, dragon);
                }
            }

            // Particles
            world.spawnParticle(Particle.DRAGON_BREATH, beamLoc, 30, 1.5, 1.5, 1.5, 0.05);

            // Lingering void fire
            spawnVoidFire(beamLoc);
        }

        world.playSound(start, Sound.ENTITY_ENDER_DRAGON_SHOOT, 2.0f, 0.5f);
    }

    private void spawnVoidFire(Location loc) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 60) { // 3 seconds
                    cancel();
                    return;
                }

                // Damage players standing in it
                for (Entity entity : world.getNearbyEntities(loc, 2, 2, 2)) {
                    if (entity instanceof Player && ticks % 20 == 0) {
                        ((Player) entity).damage(5.0, dragon);
                    }
                }

                world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 5, 1, 0.5, 1, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ========== HELPER METHODS ==========

    /**
     * Tick corrupted zones and damage players inside
     */
    private void tickCorruptedZones() {
        for (Location zoneLoc : corruptedZones) {
            // Spawn particles
            world.spawnParticle(Particle.SQUID_INK, zoneLoc, 5, 3, 1, 3, 0.05);

            // Damage players
            for (Entity entity : world.getNearbyEntities(zoneLoc, 6, 6, 6)) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;

                    // 8 damage per second (every 20 ticks)
                    if (System.currentTimeMillis() % 1000 < 50) {
                        player.damage(8.0, dragon);

                        // Disable abilities
                        PlayerData data = plugin.getPlayerData(player.getUniqueId());
                        if (data != null) {
                            data.setCooldown("support", 1000);
                            data.setCooldown("melee", 1000);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get a random player in arena
     */
    private Player getRandomPlayer() {
        if (playersInArena.isEmpty()) return null;

        List<UUID> players = new ArrayList<>(playersInArena);
        UUID randomUUID = players.get((int)(Math.random() * players.size()));
        return Bukkit.getPlayer(randomUUID);
    }

    /**
     * Handle dragon death
     */
    private void onDeath() {
        isActive = false;

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }

        // Broadcast victory
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§5§l§m                                                    ");
        Bukkit.broadcastMessage("§a§lTHE ASCENDED ENDER DRAGON HAS BEEN SLAIN!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §7The void's grip on this world has been broken...");
        Bukkit.broadcastMessage("§5§l§m                                                    ");
        Bukkit.broadcastMessage("");

        // Drop rewards
        if (dragon != null) {
            dropRewards(dragon.getLocation());
        }
    }

    /**
     * Drop rewards at location
     */
    private void dropRewards(Location loc) {
        // Dragon Core Fragment
        ItemStack core = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = core.getItemMeta();
        meta.setDisplayName("§5§lDragon Core Fragment");
        List<String> lore = new ArrayList<>();
        lore.add("§7A fragment of the Ascended Dragon's core");
        lore.add("§7Pulsing with void energy...");
        meta.setLore(lore);
        core.setItemMeta(meta);

        loc.getWorld().dropItemNaturally(loc, core);

        // Chance for Dragon Egg attribute unlock
        if (Math.random() < 0.10) { // 10% chance
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DRAGON_EGG));
        }
    }

    /**
     * Check if boss is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get the dragon entity
     */
    public EnderDragon getDragon() {
        return dragon;
    }
}