package com.oddssmp;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * New Particle System with strict global limits:
 * - Hard cap per player: 25 active
 * - Max per trigger: 12
 * - Lifetime range: 4 to 8 ticks
 * - Radius max: 0.5 blocks
 * - Distance render cap: 16 blocks
 * - No continuous emitters
 * - Pulse interval minimum: 15 ticks
 */
public class ParticleManager {

    private static OddsSMP plugin;

    // Global Limits
    public static final int HARD_CAP_PER_PLAYER = 25;
    public static final int MAX_PER_TRIGGER = 12;
    public static final int MIN_LIFETIME = 4;
    public static final int MAX_LIFETIME = 8;
    public static final double MAX_RADIUS = 0.5;
    public static final double RENDER_DISTANCE = 16.0;
    public static final int MIN_PULSE_INTERVAL = 15;

    // Track active particles per player
    private static final Map<UUID, Integer> activeParticles = new HashMap<>();
    // Track last pulse time per player per effect type
    private static final Map<UUID, Map<String, Long>> lastPulseTimes = new HashMap<>();

    public static void init(OddsSMP pluginInstance) {
        plugin = pluginInstance;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if player can receive more particles
     */
    private static boolean canSpawnFor(Player player, int count) {
        int active = activeParticles.getOrDefault(player.getUniqueId(), 0);
        return active + count <= HARD_CAP_PER_PLAYER;
    }

    /**
     * Cap particle count to limits
     */
    private static int capCount(int requested) {
        return Math.min(requested, MAX_PER_TRIGGER);
    }

    /**
     * Check if pulse is allowed (respects MIN_PULSE_INTERVAL)
     */
    private static boolean canPulse(Player player, String effectId) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> pulses = lastPulseTimes.computeIfAbsent(uuid, k -> new HashMap<>());
        long now = System.currentTimeMillis();
        long lastPulse = pulses.getOrDefault(effectId, 0L);

        if (now - lastPulse < MIN_PULSE_INTERVAL * 50) { // 50ms per tick
            return false;
        }
        pulses.put(effectId, now);
        return true;
    }

    /**
     * Spawn particles with lifetime tracking
     */
    private static void spawnTracked(Player player, Particle particle, Location loc, int count,
                                      double spreadX, double spreadY, double spreadZ, double speed, int lifetimeTicks) {
        int capped = capCount(count);
        if (!canSpawnFor(player, capped)) return;
        if (player.getLocation().distance(loc) > RENDER_DISTANCE) return;

        // Clamp lifetime
        int lifetime = Math.max(MIN_LIFETIME, Math.min(MAX_LIFETIME, lifetimeTicks));

        // Track active
        activeParticles.merge(player.getUniqueId(), capped, Integer::sum);

        // Spawn for nearby players within render distance
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getWorld().equals(loc.getWorld()) && viewer.getLocation().distance(loc) <= RENDER_DISTANCE) {
                viewer.spawnParticle(particle, loc, capped,
                        Math.min(spreadX, MAX_RADIUS),
                        Math.min(spreadY, MAX_RADIUS),
                        Math.min(spreadZ, MAX_RADIUS),
                        speed);
            }
        }

        // Schedule removal from tracking
        if (plugin != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    activeParticles.merge(player.getUniqueId(), -capped, (a, b) -> Math.max(0, a + b));
                }
            }.runTaskLater(plugin, lifetime);
        }
    }

    /**
     * Spawn dust particles with lifetime tracking
     */
    private static void spawnTrackedDust(Player player, Location loc, Color color, float size, int count,
                                          double spreadX, double spreadY, double spreadZ, int lifetimeTicks) {
        int capped = capCount(count);
        if (!canSpawnFor(player, capped)) return;
        if (player.getLocation().distance(loc) > RENDER_DISTANCE) return;

        int lifetime = Math.max(MIN_LIFETIME, Math.min(MAX_LIFETIME, lifetimeTicks));
        activeParticles.merge(player.getUniqueId(), capped, Integer::sum);

        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getWorld().equals(loc.getWorld()) && viewer.getLocation().distance(loc) <= RENDER_DISTANCE) {
                viewer.spawnParticle(Particle.DUST, loc, capped,
                        Math.min(spreadX, MAX_RADIUS),
                        Math.min(spreadY, MAX_RADIUS),
                        Math.min(spreadZ, MAX_RADIUS),
                        0, dust);
            }
        }

        if (plugin != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    activeParticles.merge(player.getUniqueId(), -capped, (a, b) -> Math.max(0, a + b));
                }
            }.runTaskLater(plugin, lifetime);
        }
    }

    // ==================== MELEE EFFECTS ====================

    /**
     * Power Strike - Trigger: Successful melee hit
     * 8 sharp crit particles, 30 degree forward cone, offset 0.4 forward
     * +4 if crit (capped at 12)
     */
    public static void playPowerStrike(Player player, Location targetLoc, boolean isCrit) {
        int count = isCrit ? 12 : 8;
        Location start = player.getLocation().add(0, 1, 0);
        Vector forward = player.getLocation().getDirection().normalize().multiply(0.4);
        Location spawnLoc = start.add(forward);

        // 30 degree forward cone
        for (int i = 0; i < capCount(count); i++) {
            double angle = Math.toRadians((Math.random() - 0.5) * 30);
            Vector dir = forward.clone().rotateAroundY(angle);
            Location particleLoc = spawnLoc.clone().add(dir.multiply(0.15));
            spawnTracked(player, Particle.CRIT, particleLoc, 1, 0.15, 0.15, 0.15, 0.05, 6);
        }
    }

    /**
     * Battle Fervor - Trigger: Buff active (pulse every 20 ticks)
     * 6 red dust particles, ring at feet, radius 0.3
     */
    public static void playBattleFervor(Player player) {
        if (!canPulse(player, "battle_fervor")) return;

        Location feet = player.getLocation();
        for (int i = 0; i < capCount(6); i++) {
            double angle = 2 * Math.PI * i / 6;
            double x = 0.3 * Math.cos(angle);
            double z = 0.3 * Math.sin(angle);
            Location loc = feet.clone().add(x, 0.1, z);
            spawnTrackedDust(player, loc, Color.RED, 1.0f, 1, 0.05, 0.05, 0.05, 5);
        }
    }

    /**
     * Berserk - Trigger: Below 30% HP (pulse every 25 ticks)
     * 5 flame particles, chest height, random 0.25 radius
     */
    public static void playBerserk(Player player) {
        if (!canPulse(player, "berserk")) return;

        Location chest = player.getLocation().add(0, 1.2, 0);
        spawnTracked(player, Particle.FLAME, chest, capCount(5), 0.25, 0.25, 0.25, 0.02, 6);
    }

    // ==================== DEFENSE EFFECTS ====================

    /**
     * Iron Skin - Trigger: Damage taken
     * 7 gray dust particles, spawn at impact side, spread 0.2
     */
    public static void playIronSkin(Player player, Vector damageDirection) {
        Location impactSide = player.getLocation().add(0, 1, 0);
        if (damageDirection != null) {
            impactSide.add(damageDirection.normalize().multiply(-0.3));
        }
        spawnTrackedDust(player, impactSide, Color.GRAY, 1.0f, capCount(7), 0.2, 0.2, 0.2, 6);
    }

    /**
     * Fortify Aura - Trigger: Active buff (pulse every 20 ticks)
     * 5 subtle enchant particles, orbit radius 0.4
     */
    public static void playFortifyAura(Player player) {
        if (!canPulse(player, "fortify_aura")) return;

        Location center = player.getLocation().add(0, 1, 0);
        for (int i = 0; i < capCount(5); i++) {
            double angle = 2 * Math.PI * i / 5;
            double x = 0.4 * Math.cos(angle);
            double z = 0.4 * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            spawnTracked(player, Particle.ENCHANT, loc, 1, 0.05, 0.05, 0.05, 0.01, 8);
        }
    }

    /**
     * Last Stand - Trigger: Fatal hit prevented
     * 6 white flash particles, torso, spread 0.1, single burst
     */
    public static void playLastStand(Player player) {
        Location torso = player.getLocation().add(0, 1, 0);
        spawnTrackedDust(player, torso, Color.WHITE, 1.5f, capCount(6), 0.1, 0.1, 0.1, 4);
    }

    // ==================== RANGED EFFECTS ====================

    /**
     * Piercing Shot - Trigger: Arrow hit
     * 6 crit particles, spawn at hit location, forward spray 20 degree
     */
    public static void playPiercingShot(Player shooter, Location hitLoc, Vector direction) {
        for (int i = 0; i < capCount(6); i++) {
            double angle = Math.toRadians((Math.random() - 0.5) * 20);
            Vector spray = direction.clone().rotateAroundY(angle).multiply(0.1);
            Location loc = hitLoc.clone().add(spray);
            spawnTracked(shooter, Particle.CRIT, loc, 1, 0.1, 0.1, 0.1, 0.05, 5);
        }
    }

    /**
     * Volley - Trigger: Multi shot activation
     * 4 particles per arrow, trail length 0.2
     */
    public static void playVolley(Player shooter, Location arrowLoc) {
        spawnTracked(shooter, Particle.CRIT, arrowLoc, capCount(4), 0.1, 0.1, 0.1, 0.02, 4);
    }

    // ==================== MAGIC/ARCANE EFFECTS ====================

    /**
     * Arcane Surge - Trigger: Spell cast
     * 8 purple dust particles, tight spiral radius 0.3, single rotation
     */
    public static void playArcaneSurge(Player player) {
        Location center = player.getLocation().add(0, 1, 0);
        for (int i = 0; i < capCount(8); i++) {
            double angle = 2 * Math.PI * i / 8;
            double x = 0.3 * Math.cos(angle);
            double z = 0.3 * Math.sin(angle);
            double y = i * 0.05;
            Location loc = center.clone().add(x, y, z);
            spawnTrackedDust(player, loc, Color.PURPLE, 1.0f, 1, 0.05, 0.05, 0.05, 6);
        }
    }

    /**
     * Mana Shield - Trigger: Shield absorbs damage
     * 6 blue particles, spawn at impact vector, spread 0.15
     */
    public static void playManaShield(Player player, Vector impactDir) {
        Location impactLoc = player.getLocation().add(0, 1, 0);
        if (impactDir != null) {
            impactLoc.add(impactDir.normalize().multiply(-0.3));
        }
        spawnTrackedDust(player, impactLoc, Color.BLUE, 1.0f, capCount(6), 0.15, 0.15, 0.15, 5);
    }

    /**
     * Blink - Trigger: Teleport
     * 10 portal particles: 5 at origin, 5 at destination, radius 0.3
     */
    public static void playBlink(Player player, Location origin, Location destination) {
        spawnTracked(player, Particle.PORTAL, origin, capCount(5), 0.3, 0.3, 0.3, 0.1, 6);
        spawnTracked(player, Particle.PORTAL, destination, capCount(5), 0.3, 0.3, 0.3, 0.1, 6);
    }

    // ==================== WEALTH EFFECTS ====================

    /**
     * Plunder Kill - Trigger: Mob kill
     * 10 gold dust particles, mob chest height, spread 0.25
     */
    public static void playPlunderKill(Player player, Location mobLoc) {
        Location chest = mobLoc.clone().add(0, 0.8, 0);
        spawnTrackedDust(player, chest, Color.YELLOW, 1.2f, capCount(10), 0.25, 0.25, 0.25, 7);
    }

    /**
     * Treasure Sense - Trigger: Near rare loot (pulse every 30 ticks)
     * 4 yellow sparkle particles, spawn above chest
     */
    public static void playTreasureSense(Player player, Location chestLoc) {
        if (!canPulse(player, "treasure_sense")) return;

        Location above = chestLoc.clone().add(0, 1.2, 0);
        spawnTracked(player, Particle.END_ROD, above, capCount(4), 0.1, 0.1, 0.1, 0.02, 6);
    }

    // ==================== UTILITY EFFECTS ====================

    /**
     * Speed Boost - Trigger: Active (pulse every 20 ticks)
     * 5 cloud particles, feet level
     */
    public static void playSpeedBoost(Player player) {
        if (!canPulse(player, "speed_boost")) return;

        Location feet = player.getLocation().add(0, 0.1, 0);
        spawnTracked(player, Particle.CLOUD, feet, capCount(5), 0.2, 0.1, 0.2, 0.01, 5);
    }

    /**
     * Stealth - Trigger: Entering stealth only (single burst)
     * 3 smoke particles, spread 0.2, no continuous emission
     */
    public static void playStealth(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        spawnTracked(player, Particle.SMOKE, loc, capCount(3), 0.2, 0.2, 0.2, 0.01, 6);
    }

    /**
     * Regeneration - Trigger: Heal tick (one pulse per heal)
     * 6 heart particles, chest height, spread 0.2
     */
    public static void playRegeneration(Player player) {
        Location chest = player.getLocation().add(0, 1.2, 0);
        spawnTracked(player, Particle.HEART, chest, capCount(6), 0.2, 0.2, 0.2, 0, 6);
    }

    // ==================== CONTROL EFFECTS ====================

    /**
     * Freeze - Trigger: Enemy slowed (pulse every 20 ticks)
     * 7 snow particles, target feet, spread 0.25
     */
    public static void playFreeze(Player caster, Player target) {
        if (!canPulse(caster, "freeze_" + target.getUniqueId())) return;

        Location feet = target.getLocation().add(0, 0.2, 0);
        spawnTracked(caster, Particle.SNOWFLAKE, feet, capCount(7), 0.25, 0.1, 0.25, 0.01, 6);
    }

    /**
     * Shock - Trigger: Lightning proc
     * 8 electric particles, zig-zag short line, length 0.4, single burst
     */
    public static void playShock(Player player, Location targetLoc) {
        Location start = targetLoc.clone().add(0, 1.5, 0);
        Location end = targetLoc.clone().add(0, 0.5, 0);
        Vector dir = end.toVector().subtract(start.toVector()).normalize();

        for (int i = 0; i < capCount(8); i++) {
            double ratio = (double) i / 8;
            double zigzag = (i % 2 == 0 ? 0.1 : -0.1);
            Location loc = start.clone().add(dir.clone().multiply(0.4 * ratio));
            loc.add(zigzag, 0, zigzag);
            spawnTracked(player, Particle.ELECTRIC_SPARK, loc, 1, 0.05, 0.05, 0.05, 0.02, 4);
        }
    }

    // ==================== LEGACY COMPATIBILITY METHODS ====================
    // These maintain API compatibility with existing code

    /**
     * Play support ability particles (simplified for new system)
     */
    public static void playSupportParticles(Player player, AttributeType attribute, int level) {
        Location loc = player.getLocation().add(0, 1, 0);

        switch (attribute) {
            case MELEE:
                playBattleFervor(player);
                break;
            case DEFENSE:
                playFortifyAura(player);
                break;
            case SPEED:
                playSpeedBoost(player);
                break;
            case HEALTH:
                playRegeneration(player);
                break;
            case WEALTH:
                playArcaneSurge(player);
                break;
            case CONTROL:
                spawnTrackedDust(player, loc, Color.PURPLE, 1.0f, 8, 0.3, 0.3, 0.3, 6);
                break;
            case RANGE:
                spawnTracked(player, Particle.CRIT, loc, 8, 0.3, 0.3, 0.3, 0.05, 6);
                break;
            case TEMPO:
                spawnTracked(player, Particle.ENCHANT, loc, 8, 0.3, 0.3, 0.3, 0.1, 6);
                break;
            case VISION:
                spawnTracked(player, Particle.END_ROD, loc, 8, 0.3, 0.3, 0.3, 0.02, 6);
                break;
            case TRANSFER:
                spawnTrackedDust(player, loc, Color.AQUA, 1.0f, 8, 0.3, 0.3, 0.3, 6);
                break;
            case PRESSURE:
                spawnTracked(player, Particle.ELECTRIC_SPARK, loc, 8, 0.3, 0.3, 0.3, 0.05, 6);
                break;
            case DISRUPTION:
                spawnTrackedDust(player, loc, Color.RED, 1.0f, 8, 0.3, 0.3, 0.3, 6);
                break;
            case RISK:
                spawnTracked(player, Particle.FIREWORK, loc, 8, 0.3, 0.3, 0.3, 0.05, 6);
                break;
            case WITHER:
                spawnTracked(player, Particle.SMOKE, loc, 8, 0.3, 0.3, 0.3, 0.02, 6);
                break;
            case WARDEN:
                spawnTracked(player, Particle.SCULK_SOUL, loc, 8, 0.3, 0.3, 0.3, 0.01, 6);
                break;
            case BREEZE:
                spawnTracked(player, Particle.GUST, loc, 6, 0.3, 0.3, 0.3, 0, 6);
                break;
            case DRAGON_EGG:
                spawnTracked(player, Particle.FLAME, loc, 10, 0.4, 0.4, 0.4, 0.05, 6);
                spawnTracked(player, Particle.SOUL_FIRE_FLAME, loc, 6, 0.3, 0.3, 0.3, 0.03, 6);
                break;
        }
    }

    /**
     * Play melee ability particles
     */
    public static void playMeleeParticles(Player player, Entity target, AttributeType attribute) {
        Location targetLoc = target.getLocation().add(0, 1, 0);
        Vector direction = targetLoc.toVector().subtract(player.getLocation().toVector()).normalize();

        switch (attribute) {
            case MELEE:
                playPowerStrike(player, targetLoc, false);
                break;
            case HEALTH:
                playRegeneration(player);
                spawnTrackedDust(player, targetLoc, Color.RED, 1.0f, 6, 0.2, 0.2, 0.2, 5);
                break;
            case DEFENSE:
                playIronSkin(player, direction);
                break;
            case CONTROL:
                playShock(player, targetLoc);
                break;
            case TEMPO:
                spawnTracked(player, Particle.ENCHANT, targetLoc, 8, 0.2, 0.2, 0.2, 0.1, 5);
                break;
            case RANGE:
                playPiercingShot(player, targetLoc, direction);
                break;
            case WEALTH:
                spawnTrackedDust(player, targetLoc, Color.YELLOW, 1.0f, 8, 0.2, 0.2, 0.2, 5);
                break;
            case SPEED:
                spawnTracked(player, Particle.CLOUD, targetLoc, 6, 0.2, 0.2, 0.2, 0.02, 5);
                break;
            case TRANSFER:
                spawnTrackedDust(player, targetLoc, Color.AQUA, 1.0f, 6, 0.2, 0.2, 0.2, 5);
                break;
            case PRESSURE:
                spawnTracked(player, Particle.ELECTRIC_SPARK, targetLoc, 8, 0.25, 0.25, 0.25, 0.05, 5);
                break;
            case DISRUPTION:
                spawnTrackedDust(player, targetLoc, Color.PURPLE, 1.0f, 8, 0.2, 0.2, 0.2, 5);
                break;
            case RISK:
                spawnTracked(player, Particle.FIREWORK, targetLoc, 8, 0.2, 0.2, 0.2, 0.05, 5);
                break;
            case VISION:
                spawnTracked(player, Particle.GLOW, targetLoc, 8, 0.2, 0.2, 0.2, 0, 5);
                break;
            case WITHER:
                spawnTracked(player, Particle.SMOKE, targetLoc, 10, 0.3, 0.3, 0.3, 0.02, 6);
                break;
            case WARDEN:
                spawnTracked(player, Particle.SCULK_SOUL, targetLoc, 8, 0.3, 0.3, 0.3, 0.01, 6);
                break;
            case BREEZE:
                spawnTracked(player, Particle.GUST, targetLoc, 6, 0.3, 0.3, 0.3, 0, 5);
                break;
            case DRAGON_EGG:
                spawnTracked(player, Particle.FLAME, targetLoc, 12, 0.4, 0.4, 0.4, 0.05, 6);
                break;
        }
    }

    /**
     * Play passive ability particles (respects pulse intervals)
     */
    public static void playPassiveParticles(Player player, AttributeType attribute) {
        if (!canPulse(player, "passive_" + attribute.name())) return;

        Location loc = player.getLocation().add(0, 1, 0);
        int count = 4; // Reduced for passive auras

        switch (attribute) {
            case MELEE:
                if (player.getHealth() < player.getMaxHealth() * 0.3) {
                    playBerserk(player);
                }
                break;
            case HEALTH:
                spawnTracked(player, Particle.HEART, loc, count, 0.3, 0.3, 0.3, 0, 5);
                break;
            case DEFENSE:
                playFortifyAura(player);
                break;
            case SPEED:
                playSpeedBoost(player);
                break;
            case WEALTH:
                spawnTracked(player, Particle.END_ROD, loc, count, 0.3, 0.3, 0.3, 0.02, 5);
                break;
            case CONTROL:
                spawnTrackedDust(player, loc, Color.PURPLE, 0.8f, count, 0.3, 0.3, 0.3, 5);
                break;
            case RANGE:
                spawnTrackedDust(player, loc, Color.ORANGE, 0.8f, count, 0.3, 0.3, 0.3, 5);
                break;
            case TEMPO:
                spawnTracked(player, Particle.ENCHANT, loc, count, 0.3, 0.3, 0.3, 0.1, 5);
                break;
            case VISION:
                spawnTracked(player, Particle.GLOW, loc, count, 0.3, 0.3, 0.3, 0, 5);
                break;
            case TRANSFER:
                spawnTrackedDust(player, loc, Color.AQUA, 0.8f, count, 0.3, 0.3, 0.3, 5);
                break;
            case PRESSURE:
                spawnTracked(player, Particle.ELECTRIC_SPARK, loc, count, 0.3, 0.3, 0.3, 0.02, 5);
                break;
            case DISRUPTION:
                spawnTrackedDust(player, loc, Color.RED, 0.8f, count, 0.3, 0.3, 0.3, 5);
                break;
            case RISK:
                spawnTracked(player, Particle.FIREWORK, loc, count, 0.3, 0.3, 0.3, 0.02, 5);
                break;
            case WITHER:
                spawnTracked(player, Particle.SMOKE, loc, count + 2, 0.4, 0.4, 0.4, 0.01, 5);
                break;
            case WARDEN:
                spawnTracked(player, Particle.SCULK_SOUL, loc, count, 0.3, 0.3, 0.3, 0.01, 5);
                break;
            case BREEZE:
                spawnTracked(player, Particle.CLOUD, loc, count, 0.3, 0.3, 0.3, 0.01, 5);
                break;
            case DRAGON_EGG:
                spawnTracked(player, Particle.FLAME, loc, count + 2, 0.4, 0.4, 0.4, 0.03, 6);
                break;
        }
    }

    // ==================== ADDITIONAL HELPER EFFECTS ====================

    /**
     * Vampiric heal effect
     */
    public static void playVampiricHeal(Player player, Location targetLoc) {
        Location start = player.getLocation().add(0, 1, 0);
        spawnTrackedDust(player, start, Color.RED, 1.0f, 4, 0.15, 0.15, 0.15, 5);
        spawnTracked(player, Particle.HEART, start, 3, 0.1, 0.1, 0.1, 0, 5);
    }

    /**
     * Lockdown zone indicator
     */
    public static void playLockdownZone(Player caster, Location center, double radius) {
        if (!canPulse(caster, "lockdown")) return;

        for (int i = 0; i < capCount(8); i++) {
            double angle = 2 * Math.PI * i / 8;
            double x = Math.min(radius, MAX_RADIUS * 2) * Math.cos(angle);
            double z = Math.min(radius, MAX_RADIUS * 2) * Math.sin(angle);
            Location loc = center.clone().add(x, 0.1, z);
            spawnTrackedDust(caster, loc, Color.PURPLE, 1.0f, 1, 0.05, 0.05, 0.05, 6);
        }
    }

    /**
     * System Jam EMP effect
     */
    public static void playSystemJam(Player caster, Location center) {
        spawnTracked(caster, Particle.ELECTRIC_SPARK, center, capCount(10), 0.4, 0.4, 0.4, 0.1, 5);
        spawnTrackedDust(caster, center, Color.RED, 1.2f, 6, 0.3, 0.3, 0.3, 5);
    }

    /**
     * Dragon Dominion charge indicator
     */
    public static void playDominionCharge(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        spawnTracked(player, Particle.FLAME, loc, 6, 0.3, 0.5, 0.3, 0.03, 5);
        spawnTrackedDust(player, loc, Color.PURPLE, 1.0f, 4, 0.2, 0.3, 0.2, 5);
    }

    /**
     * Stun effect on target
     */
    public static void playStunEffect(Player caster, Player target) {
        if (!canPulse(caster, "stun_" + target.getUniqueId())) return;

        Location head = target.getLocation().add(0, 1.8, 0);
        spawnTracked(caster, Particle.ENCHANT, head, 5, 0.2, 0.1, 0.2, 0.05, 6);
    }

    /**
     * Clear all tracking for a player (call on disconnect)
     */
    public static void clearPlayer(UUID uuid) {
        activeParticles.remove(uuid);
        lastPulseTimes.remove(uuid);
    }

    /**
     * Get current active particle count for player
     */
    public static int getActiveCount(UUID uuid) {
        return activeParticles.getOrDefault(uuid, 0);
    }
}
