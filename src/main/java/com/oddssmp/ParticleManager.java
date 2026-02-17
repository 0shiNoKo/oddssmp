package com.oddssmp;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleManager {

    private static OddsSMP plugin;

    /**
     * Initialize the ParticleManager with plugin reference
     */
    public static void init(OddsSMP pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Get scaled particle count based on intensity setting
     */
    private static int scaleCount(int baseCount) {
        if (plugin == null) return baseCount;
        return (int) Math.max(1, baseCount * plugin.getParticleIntensity());
    }

    /**
     * Play support ability particles for the given attribute
     */
    public static void playSupportParticles(Player player, AttributeType attribute, int level) {
        Location loc = player.getLocation().add(0, 1, 0);
        int particleCount = scaleCount(50);
        double radius = 6.0 * (1.0 + level * 0.15);

        switch (attribute) {
            case MELEE:
                spawnCircleParticles(loc, Particle.HAPPY_VILLAGER, radius, particleCount);
                spawnCircleParticles(loc, Particle.CRIT, radius * 0.8, particleCount / 2);
                spawnSpiralParticles(loc, Particle.SWEEP_ATTACK, radius, scaleCount(40));
                break;

            case SPEED:
                spawnColoredParticles(loc, Color.YELLOW, radius, particleCount);
                spawnColoredParticles(loc, Color.WHITE, radius * 0.8, particleCount / 2);
                spawnSpiralParticles(loc, Particle.CLOUD, radius, scaleCount(60));
                break;

            case PRESSURE:
                spawnCircleParticles(loc, Particle.ELECTRIC_SPARK, radius, particleCount);
                spawnColoredParticles(loc, Color.fromRGB(150, 0, 0), radius * 0.8, particleCount / 2);
                break;

            case DISRUPTION:
                spawnColoredParticles(loc, Color.RED, radius, particleCount);
                spawnCircleParticles(loc, Particle.LAVA, radius * 0.7, particleCount / 2);
                break;

            case RISK:
                spawnCircleParticles(loc, Particle.FIREWORK, radius, particleCount);
                player.getWorld().spawnParticle(Particle.GLOW, loc, scaleCount(80), radius, 1, radius, 0);
                spawnColoredParticles(loc, Color.RED, radius * 0.8, particleCount / 2);
                spawnColoredParticles(loc, Color.YELLOW, radius * 0.6, particleCount / 3);
                break;

            case HEALTH:
                spawnCircleParticles(loc, Particle.HEART, radius, particleCount);
                spawnCircleParticles(loc, Particle.GLOW, radius * 0.7, particleCount / 2);
                break;

            case WEALTH:
                spawnCircleParticles(loc, Particle.GLOW, radius, particleCount);
                spawnCircleParticles(loc, Particle.END_ROD, radius * 0.8, particleCount / 2);
                player.getWorld().spawnParticle(Particle.ENCHANT, loc, 100, radius, 1, radius, 1);
                break;

            case DEFENSE:
                spawnColoredParticles(loc, Color.BLUE, radius, particleCount);
                spawnShieldParticles(loc, Color.BLUE, radius);
                break;

            case CONTROL:
                spawnColoredParticles(loc, Color.PURPLE, radius, particleCount);
                spawnSpiralParticles(loc, Particle.WITCH, radius, 50);
                break;

            case RANGE:
                spawnColoredParticles(loc, Color.ORANGE, radius, particleCount);
                spawnCircleParticles(loc, Particle.SWEEP_ATTACK, radius, 50);
                break;

            case TEMPO:
                spawnCircleParticles(loc, Particle.ENCHANT, radius, particleCount);
                spawnCircleParticles(loc, Particle.PORTAL, radius * 0.8, particleCount / 2);
                break;

            case VISION:
                spawnCircleParticles(loc, Particle.GLOW, radius, particleCount);
                spawnCircleParticles(loc, Particle.END_ROD, radius * 0.8, particleCount / 2);
                break;

            case PERSISTENCE:
                spawnColoredParticles(loc, Color.RED, radius, particleCount);
                spawnCircleParticles(loc, Particle.TOTEM_OF_UNDYING, radius * 0.8, 40);
                break;

            case TRANSFER:
                spawnColoredParticles(loc, Color.AQUA, radius, particleCount);
                spawnSpiralParticles(loc, Particle.DOLPHIN, radius, 60);
                break;

            case WITHER:
                spawnColoredParticles(loc, Color.fromRGB(100, 0, 100), radius, particleCount * 2);
                spawnCircleParticles(loc, Particle.SMOKE, radius, particleCount);
                player.getWorld().spawnParticle(Particle.DUST, loc, 150, radius, 1, radius, 0,
                        new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.5f));
                break;

            case WARDEN:
                spawnColoredParticles(loc, Color.fromRGB(10, 50, 60), radius, particleCount * 2);
                spawnCircleParticles(loc, Particle.SCULK_SOUL, radius, particleCount);
                player.getWorld().spawnParticle(Particle.SCULK_CHARGE, loc, 100, radius, 1, radius, 0);
                break;

            case BREEZE:
                spawnColoredParticles(loc, Color.YELLOW, radius, particleCount);
                spawnColoredParticles(loc, Color.WHITE, radius * 0.8, particleCount);
                spawnCircleParticles(loc, Particle.GUST, radius, 80);
                break;

            case DRAGON_EGG:
                spawnDragonParticles(loc, radius, particleCount * 3);
                spawnSpiralParticles(loc, Particle.FLAME, radius, 80);
                player.getWorld().spawnParticle(Particle.LAVA, loc, 100, radius, 2, radius, 0);
                player.getWorld().spawnParticle(Particle.FLAME, loc, 150, radius, 2, radius, 0.1);
                spawnBurstParticles(loc, Particle.FLAME, 200);
                spawnDomeParticles(loc, Color.PURPLE, radius, 120);
                break;
        }
    }

    /**
     * Play melee ability particles
     */
    public static void playMeleeParticles(Player player, Entity target, AttributeType attribute) {
        Location start = player.getLocation().add(0, 1, 0);
        Location end = target.getLocation().add(0, 1, 0);

        switch (attribute) {
            case MELEE:
                drawLine(start, end, Particle.CRIT, 30);
                drawLine(start, end, Particle.SWEEP_ATTACK, 20);
                target.getWorld().spawnParticle(Particle.ENCHANTED_HIT, end, 25, 0.5, 0.5, 0.5, 0);
                break;

            case HEALTH:
                drawLine(start, end, Particle.HEART, 25);
                drawColoredLine(start, end, Color.RED, 20);
                target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, end, 20, 0.5, 0.5, 0.5, 0);
                break;

            case WEALTH:
                drawLine(start, end, Particle.GLOW, 30);
                drawLine(start, end, Particle.END_ROD, 20);
                target.getWorld().spawnParticle(Particle.ENCHANT, end, 50, 0.5, 0.5, 0.5, 1);
                break;

            case DEFENSE:
                drawColoredLine(start, end, Color.BLUE, 25);
                target.getWorld().spawnParticle(Particle.FIREWORK, end, 25);
                break;

            case CONTROL:
                drawColoredLine(start, end, Color.PURPLE, 25);
                drawLine(start, end, Particle.WITCH, 20);
                break;

            case RANGE:
                drawColoredLine(start, end, Color.ORANGE, 25);
                drawLine(start, end, Particle.CRIT, 20);
                target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, end, 30, 0.5, 0.5, 0.5, 0);
                break;

            case TEMPO:
                drawLine(start, end, Particle.ENCHANT, 25);
                drawLine(start, end, Particle.PORTAL, 20);
                target.getWorld().spawnParticle(Particle.GLOW, end, 40, 0.5, 0.5, 0.5, 0);
                break;

            case VISION:
                drawLine(start, end, Particle.GLOW, 30);
                drawLine(start, end, Particle.END_ROD, 25);
                target.getWorld().spawnParticle(Particle.ENCHANT, end, 50, 0.5, 0.5, 0.5, 1);
                break;

            case PERSISTENCE:
                drawColoredLine(start, end, Color.RED, 25);
                drawLine(start, end, Particle.HEART, 20);
                target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, end, 25, 0.5, 0.5, 0.5, 0);
                break;

            case TRANSFER:
                drawColoredLine(start, end, Color.AQUA, 30);
                drawLine(start, end, Particle.DOLPHIN, 20);
                target.getWorld().spawnParticle(Particle.GLOW, end, 40, 0.5, 0.5, 0.5, 0);
                break;

            case WITHER:
                drawColoredLine(start, end, Color.fromRGB(100, 0, 100), 80);
                drawLine(start, end, Particle.SMOKE, 60);
                target.getWorld().spawnParticle(Particle.DUST, end, 100, 0.5, 0.5, 0.5, 0,
                        new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.5f));
                target.getWorld().spawnParticle(Particle.SMOKE, end, 80, 1, 1, 1, 0.1);
                break;

            case WARDEN:
                drawColoredLine(start, end, Color.fromRGB(10, 50, 60), 80);
                drawLine(start, end, Particle.SCULK_SOUL, 60);
                target.getWorld().spawnParticle(Particle.SONIC_BOOM, end, 5);
                target.getWorld().spawnParticle(Particle.SCULK_CHARGE, end, 50, 1, 1, 1, 0);
                break;

            case BREEZE:
                drawColoredLine(start, end, Color.YELLOW, 70);
                drawColoredLine(start, end, Color.WHITE, 50);
                drawLine(start, end, Particle.GUST, 60);
                target.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, end, 80, 0.5, 0.5, 0.5, 0);
                break;

            case DRAGON_EGG:
                drawDragonLine(start, end, 100);
                target.getWorld().spawnParticle(Particle.FLAME, end, 150, 1, 1, 1, 0.1);
                target.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, end, 100, 1, 1, 1, 0.05);
                target.getWorld().spawnParticle(Particle.LAVA, end, 80, 1, 1, 1, 0);
                spawnExplosionRing(end, Color.PURPLE, 4.0, 100);
                target.getWorld().spawnParticle(Particle.EXPLOSION, end, 20);
                break;

            case SPEED:
                drawColoredLine(start, end, Color.YELLOW, 30);
                drawColoredLine(start, end, Color.WHITE, 20);
                drawLine(start, end, Particle.CLOUD, 25);
                target.getWorld().spawnParticle(Particle.FIREWORK, end, 30, 0.5, 0.5, 0.5, 0);
                break;

            case PRESSURE:
                drawLine(start, end, Particle.ELECTRIC_SPARK, 30);
                drawColoredLine(start, end, Color.fromRGB(150, 0, 0), 20);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, end, 40, 0.5, 0.5, 0.5, 0);
                break;

            case DISRUPTION:
                drawColoredLine(start, end, Color.RED, 30);
                drawLine(start, end, Particle.LAVA, 20);
                target.getWorld().spawnParticle(Particle.LAVA, end, 30, 0.5, 0.5, 0.5, 0);
                break;

            case ANCHOR:
                drawColoredLine(start, end, Color.GRAY, 30);
                drawColoredLine(start, end, Color.SILVER, 20);
                target.getWorld().spawnParticle(Particle.CRIT, end, 40, 0.5, 0.5, 0.5, 0);
                break;

            case RISK:
                drawLine(start, end, Particle.FIREWORK, 40);
                drawColoredLine(start, end, Color.RED, 30);
                drawColoredLine(start, end, Color.YELLOW, 20);
                target.getWorld().spawnParticle(Particle.GLOW, end, 50, 0.5, 0.5, 0.5, 0);
                break;
        }
    }

    /**
     * Play passive ability particles (ambient aura)
     */
    public static void playPassiveParticles(Player player, AttributeType attribute) {
        Location loc = player.getLocation().add(0, 1, 0);
        int count = 12;

        switch (attribute) {
            case MELEE:
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, count, 0.5, 0.7, 0.5, 0);
                player.getWorld().spawnParticle(Particle.CRIT, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case HEALTH:
                player.getWorld().spawnParticle(Particle.HEART, loc, count, 0.5, 0.7, 0.5, 0);
                spawnColoredParticle(loc, Color.RED, count / 2);
                break;

            case WEALTH:
                player.getWorld().spawnParticle(Particle.GLOW, loc, count, 0.5, 0.7, 0.5, 0);
                player.getWorld().spawnParticle(Particle.ENCHANT, loc, count / 2, 0.4, 0.6, 0.4, 0.5);
                break;

            case DEFENSE:
                spawnColoredParticle(loc, Color.BLUE, count);
                spawnColoredParticle(loc, Color.AQUA, count / 2);
                break;

            case CONTROL:
                spawnColoredParticle(loc, Color.PURPLE, count);
                player.getWorld().spawnParticle(Particle.WITCH, loc, count / 3, 0.4, 0.6, 0.4, 0);
                break;

            case RANGE:
                spawnColoredParticle(loc, Color.ORANGE, count);
                player.getWorld().spawnParticle(Particle.CRIT, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case TEMPO:
                player.getWorld().spawnParticle(Particle.ENCHANT, loc, count, 0.5, 0.7, 0.5, 0.5);
                player.getWorld().spawnParticle(Particle.PORTAL, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case VISION:
                player.getWorld().spawnParticle(Particle.GLOW, loc, count, 0.5, 0.7, 0.5, 0);
                player.getWorld().spawnParticle(Particle.END_ROD, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case PERSISTENCE:
                spawnColoredParticle(loc, Color.RED, count);
                player.getWorld().spawnParticle(Particle.HEART, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case TRANSFER:
                spawnColoredParticle(loc, Color.AQUA, count);
                player.getWorld().spawnParticle(Particle.DOLPHIN, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case WITHER:
                player.getWorld().spawnParticle(Particle.SMOKE, loc, count * 2, 0.5, 0.7, 0.5, 0.02);
                spawnColoredParticle(loc, Color.fromRGB(100, 0, 100), count);
                break;

            case WARDEN:
                player.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, count, 0.3, 0.5, 0.3, 0.01);
                spawnColoredParticle(loc, Color.fromRGB(10, 50, 60), count / 2);
                break;

            case BREEZE:
                player.getWorld().spawnParticle(Particle.GUST, loc, count / 2, 0.4, 0.6, 0.4, 0);
                spawnColoredParticle(loc, Color.YELLOW, count);
                spawnColoredParticle(loc, Color.WHITE, count / 2);
                break;

            case DRAGON_EGG:
                player.getWorld().spawnParticle(Particle.FLAME, loc, count * 4, 0.5, 0.7, 0.5, 0.04);
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, count * 3, 0.4, 0.6, 0.4, 0.03);
                player.getWorld().spawnParticle(Particle.LAVA, loc, count, 0.4, 0.6, 0.4, 0);
                spawnColoredParticle(loc, Color.PURPLE, count * 2);
                break;

            case SPEED:
                spawnColoredParticle(loc, Color.YELLOW, count);
                spawnColoredParticle(loc, Color.WHITE, count / 2);
                player.getWorld().spawnParticle(Particle.CLOUD, loc, count / 2, 0.4, 0.6, 0.4, 0);
                break;

            case PRESSURE:
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, count, 0.5, 0.7, 0.5, 0);
                spawnColoredParticle(loc, Color.fromRGB(150, 0, 0), count / 2);
                break;

            case DISRUPTION:
                spawnColoredParticle(loc, Color.RED, count);
                player.getWorld().spawnParticle(Particle.LAVA, loc, count / 3, 0.4, 0.6, 0.4, 0);
                break;

            case ANCHOR:
                spawnColoredParticle(loc, Color.GRAY, count);
                spawnColoredParticle(loc, Color.SILVER, count / 2);
                break;

            case RISK:
                player.getWorld().spawnParticle(Particle.FIREWORK, loc, count, 0.5, 0.7, 0.5, 0);
                spawnColoredParticle(loc, Color.RED, count / 2);
                spawnColoredParticle(loc, Color.YELLOW, count / 3);
                break;
        }
    }

    // Helper methods - all apply intensity scaling automatically
    private static void spawnCircleParticles(Location center, Particle particle, double radius, int count) {
        int scaledCount = scaleCount(count);
        for (int i = 0; i < scaledCount; i++) {
            double angle = 2 * Math.PI * i / scaledCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private static void spawnSpiralParticles(Location center, Particle particle, double radius, int count) {
        int scaledCount = scaleCount(count);
        for (int i = 0; i < scaledCount; i++) {
            double angle = 4 * Math.PI * i / scaledCount;
            double currentRadius = radius * i / scaledCount;
            double x = currentRadius * Math.cos(angle);
            double z = currentRadius * Math.sin(angle);
            double y = 2.0 * i / scaledCount;
            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private static void spawnBurstParticles(Location center, Particle particle, int count) {
        center.getWorld().spawnParticle(particle, center, scaleCount(count), 1.5, 1.5, 1.5, 0.1);
    }

    private static void spawnShieldParticles(Location center, Color color, double radius) {
        int scaledCount = scaleCount(50);
        for (int i = 0; i < scaledCount; i++) {
            double theta = Math.random() * Math.PI;
            double phi = Math.random() * 2 * Math.PI;

            double x = radius * Math.sin(theta) * Math.cos(phi);
            double y = radius * Math.cos(theta);
            double z = radius * Math.sin(theta) * Math.sin(phi);

            if (y < 0) y = -y;

            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.0f));
        }
    }

    private static void spawnDomeParticles(Location center, Color color, double radius, int count) {
        int scaledCount = scaleCount(count);
        for (int i = 0; i < scaledCount; i++) {
            double theta = Math.acos(1 - 2.0 * i / scaledCount);
            double phi = Math.PI * (1 + Math.sqrt(5)) * i;

            double x = radius * Math.sin(theta) * Math.cos(phi);
            double y = radius * Math.cos(theta);
            double z = radius * Math.sin(theta) * Math.sin(phi);

            if (y < 0) y = -y;

            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.0f));
        }
    }

    private static void spawnExplosionRing(Location center, Color color, double radius, int count) {
        int scaledCount = scaleCount(count);
        for (int i = 0; i < scaledCount; i++) {
            double angle = 2 * Math.PI * i / scaledCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(color, 1.5f));
        }
    }

    private static void spawnColoredParticles(Location center, Color color, double radius, int count) {
        int scaledCount = scaleCount(count);
        for (int i = 0; i < scaledCount; i++) {
            double angle = 2 * Math.PI * i / scaledCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.0f));
        }
    }

    private static void spawnColoredParticle(Location loc, Color color, int count) {
        loc.getWorld().spawnParticle(Particle.DUST, loc, scaleCount(count), 0.3, 0.5, 0.3, 0,
                new Particle.DustOptions(color, 1.0f));
    }

    private static void spawnDragonParticles(Location center, double radius, int count) {
        int scaledCount = scaleCount(count);
        center.getWorld().spawnParticle(Particle.FLAME, center, scaledCount, radius, 1.5, radius, 0.08);
        center.getWorld().spawnParticle(Particle.LAVA, center, scaledCount / 2, radius, 1, radius, 0);
        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, scaledCount / 2, radius, 1.5, radius, 0.05);
    }

    private static void drawLine(Location start, Location end, Particle particle, int points) {
        int scaledPoints = scaleCount(points);
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (int i = 0; i < scaledPoints; i++) {
            double ratio = (double) i / scaledPoints;
            Vector offset = direction.clone().multiply(length * ratio);
            Location loc = start.clone().add(offset);
            start.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private static void drawColoredLine(Location start, Location end, Color color, int points) {
        int scaledPoints = scaleCount(points);
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (int i = 0; i < scaledPoints; i++) {
            double ratio = (double) i / scaledPoints;
            Vector offset = direction.clone().multiply(length * ratio);
            Location loc = start.clone().add(offset);
            start.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.0f));
        }
    }

    private static void drawDragonLine(Location start, Location end, int points) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (int i = 0; i < points; i++) {
            double ratio = (double) i / points;
            Vector offset = direction.clone().multiply(length * ratio);
            Location loc = start.clone().add(offset);
            start.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.2, 0.2, 0.2, 0.03);
            start.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 2, 0.1, 0.1, 0.1, 0.02);
            if (i % 5 == 0) {
                start.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    // ========== NEW SPECIALIZED EFFECT METHODS ==========

    /**
     * Crescent arc particles (for MELEE Power Strike)
     */
    public static void playCrescentArc(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector dir = loc.getDirection().normalize();
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        for (int i = -30; i <= 30; i += 5) {
            double angle = Math.toRadians(i);
            Vector arcPoint = dir.clone().multiply(2).add(right.clone().multiply(Math.sin(angle) * 1.5));
            arcPoint.setY(arcPoint.getY() + Math.cos(angle) * 0.5);
            Location particleLoc = loc.clone().add(arcPoint);
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.CRIT, particleLoc, 3, 0.1, 0.1, 0.1, 0);
        }
    }

    /**
     * Impact sparks (for melee hits)
     */
    public static void playImpactSparks(Location loc) {
        loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 30, 0.3, 0.3, 0.3, 0.1);
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 20, 0.2, 0.2, 0.2, 0.05);
    }

    /**
     * Blood burst particles (for MELEE Bloodlust on kill)
     */
    public static void playBloodBurst(Location loc) {
        loc.getWorld().spawnParticle(Particle.DUST, loc, 50, 0.5, 0.8, 0.5, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f));
        loc.getWorld().spawnParticle(Particle.DUST, loc, 30, 0.3, 0.5, 0.3, 0,
                new Particle.DustOptions(Color.RED, 1.0f));
    }

    /**
     * Red tether particles (for HEALTH Vampiric Hit)
     */
    public static void playVampiricTether(Location start, Location end) {
        drawColoredLine(start, end, Color.RED, 30);
        drawColoredLine(start.clone().add(0, 0.1, 0), end.clone().add(0, 0.1, 0), Color.fromRGB(139, 0, 0), 20);
        end.getWorld().spawnParticle(Particle.HEART, end, 5, 0.2, 0.2, 0.2, 0);
    }

    /**
     * Gold flash heal particles (for HEALTH on heal)
     */
    public static void playHealFlash(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().spawnParticle(Particle.HEART, loc, 10, 0.3, 0.3, 0.3, 0);
    }

    /**
     * Heart burst particles (for HEALTH Vitality on kill)
     */
    public static void playHeartBurst(Location loc) {
        loc.getWorld().spawnParticle(Particle.HEART, loc, 25, 0.5, 0.8, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.GLOW, loc, 15, 0.3, 0.5, 0.3, 0);
    }

    /**
     * Metallic flash particles (for DEFENSE Iron Response)
     */
    public static void playMetallicFlash(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.FIREWORK, loc, 40, 0.5, 0.5, 0.5, 0);
        spawnColoredParticle(loc, Color.SILVER, 30);
        spawnColoredParticle(loc, Color.GRAY, 20);
    }

    /**
     * Stone crack particles (for DEFENSE Hardened)
     */
    public static void playStoneCrack(Location loc) {
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 30, 0.3, 0.1, 0.3, 0,
                org.bukkit.Material.STONE.createBlockData());
        spawnColoredParticle(loc, Color.GRAY, 20);
    }

    /**
     * Gold spiral particles (for WEALTH Plunder Kill)
     */
    public static void playGoldSpiral(Location center) {
        for (int i = 0; i < 60; i++) {
            double angle = 4 * Math.PI * i / 60;
            double radius = 0.5 + i * 0.03;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = i * 0.05;
            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.YELLOW, 1.0f));
        }
        center.getWorld().spawnParticle(Particle.GLOW, center, 30, 0.5, 1, 0.5, 0);
    }

    /**
     * Purple static particles on legs (for CONTROL Disrupt)
     */
    public static void playDisruptStatic(Player target) {
        Location legs = target.getLocation().add(0, 0.5, 0);
        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, legs, 25, 0.2, 0.3, 0.2, 0.05);
        spawnColoredParticle(legs, Color.PURPLE, 15);
    }

    /**
     * Purple ground ring (for CONTROL Lockdown)
     */
    public static void playLockdownRing(Location center, double radius) {
        for (int i = 0; i < 60; i++) {
            double angle = 2 * Math.PI * i / 60;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0.1, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0,
                    new Particle.DustOptions(Color.PURPLE, 1.2f));
            center.getWorld().spawnParticle(Particle.WITCH, loc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Air slice particles (for RANGE Spacing Strike)
     */
    public static void playAirSlice(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector()).normalize();
        for (int i = 0; i < 20; i++) {
            Location loc = start.clone().add(dir.clone().multiply(i * 0.2));
            start.getWorld().spawnParticle(Particle.CLOUD, loc, 3, 0.1, 0.1, 0.1, 0);
            start.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Clock particles (for TEMPO Tempo Strike)
     */
    public static void playClockParticles(Location loc) {
        // Clock face
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double x = 0.8 * Math.cos(angle);
            double z = 0.8 * Math.sin(angle);
            Location tickLoc = loc.clone().add(x, 0, z);
            loc.getWorld().spawnParticle(Particle.END_ROD, tickLoc, 2, 0.05, 0.05, 0.05, 0);
        }
        // Clock hands
        loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 30, 0.3, 0.3, 0.3, 0.5);
    }

    /**
     * Speed streak particles (for SPEED abilities)
     */
    public static void playSpeedStreak(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector behind = player.getLocation().getDirection().multiply(-1).normalize();
        for (int i = 0; i < 10; i++) {
            Location trailLoc = loc.clone().add(behind.clone().multiply(i * 0.3));
            player.getWorld().spawnParticle(Particle.CLOUD, trailLoc, 3, 0.1, 0.2, 0.1, 0);
            spawnColoredParticle(trailLoc, Color.YELLOW, 2);
        }
    }

    /**
     * Afterimage particles (for SPEED Flash Step)
     */
    public static void playAfterimage(Location loc) {
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 0.3, 0.8, 0.3, 0.02);
        spawnColoredParticle(loc, Color.WHITE, 20);
        spawnColoredParticle(loc, Color.YELLOW, 15);
    }

    /**
     * Ground crack particles (for PRESSURE Crushing Blow)
     */
    public static void playGroundCrack(Location loc) {
        Location ground = loc.clone();
        ground.setY(Math.floor(ground.getY()));

        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * i / 8;
            for (int j = 1; j <= 3; j++) {
                double x = j * 0.5 * Math.cos(angle);
                double z = j * 0.5 * Math.sin(angle);
                Location crackLoc = ground.clone().add(x, 0.1, z);
                loc.getWorld().spawnParticle(Particle.BLOCK, crackLoc, 5, 0.1, 0.05, 0.1, 0,
                        org.bukkit.Material.STONE.createBlockData());
            }
        }
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 3);
    }

    /**
     * Red fog particles (for PRESSURE Intimidation Field)
     */
    public static void playRedFog(Location center, double radius) {
        for (int i = 0; i < 40; i++) {
            double x = (Math.random() - 0.5) * radius * 2;
            double z = (Math.random() - 0.5) * radius * 2;
            double y = Math.random() * 2;
            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(150, 0, 0), 1.5f));
        }
    }

    /**
     * Purple fracture particles (for DISRUPTION Fracture)
     */
    public static void playFractureParticles(Location loc) {
        loc.getWorld().spawnParticle(Particle.DUST, loc, 40, 0.4, 0.4, 0.4, 0,
                new Particle.DustOptions(Color.PURPLE, 1.2f));
        loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 20, 0.3, 0.3, 0.3, 0);
    }

    /**
     * EMP burst particles (for DISRUPTION System Jam)
     */
    public static void playEMPBurst(Location center, double radius) {
        // Expanding ring
        for (int ring = 1; ring <= 3; ring++) {
            double r = radius * ring / 3;
            for (int i = 0; i < 40; i++) {
                double angle = 2 * Math.PI * i / 40;
                double x = r * Math.cos(angle);
                double z = r * Math.sin(angle);
                Location loc = center.clone().add(x, 0.5, z);
                center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 3, 0.1, 0.1, 0.1, 0);
            }
        }
        center.getWorld().spawnParticle(Particle.FLASH, center, 1);
    }

    /**
     * Chain particles (for ANCHOR Pin)
     */
    public static void playChainParticles(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector());
        double length = dir.length();
        dir.normalize();

        for (int i = 0; i < (int)(length * 5); i++) {
            double ratio = (double) i / (length * 5);
            Vector offset = dir.clone().multiply(length * ratio);
            Location loc = start.clone().add(offset);
            // Alternating pattern for chain look
            if (i % 2 == 0) {
                start.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.GRAY, 1.5f));
            } else {
                start.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(60, 60, 60), 1.2f));
            }
        }
    }

    /**
     * Anchor ground symbol (for ANCHOR Hold the Line)
     */
    public static void playAnchorSymbol(Location center) {
        // Vertical line
        for (int i = 0; i < 10; i++) {
            Location loc = center.clone().add(0, 0.1, -0.5 + i * 0.1);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.GRAY, 1.2f));
        }
        // Horizontal line
        for (int i = 0; i < 10; i++) {
            Location loc = center.clone().add(-0.5 + i * 0.1, 0.1, 0.3);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.GRAY, 1.2f));
        }
        // Curved bottom
        for (int i = -5; i <= 5; i++) {
            double angle = Math.PI + Math.toRadians(i * 10);
            double x = 0.3 * Math.cos(angle);
            double z = 0.3 * Math.sin(angle) + 0.5;
            Location loc = center.clone().add(x, 0.1, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.GRAY, 1.2f));
        }
    }

    /**
     * Dice particles (for RISK All In)
     */
    public static void playDiceParticles(Location loc) {
        // Random colored bursts
        Color[] colors = {Color.RED, Color.YELLOW, Color.WHITE};
        for (int i = 0; i < 30; i++) {
            Color c = colors[(int)(Math.random() * colors.length)];
            loc.getWorld().spawnParticle(Particle.DUST, loc, 1, 0.5, 0.5, 0.5, 0,
                    new Particle.DustOptions(c, 1.0f));
        }
        loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.3, 0.3, 0.3, 0.05);
    }

    /**
     * Dark slash particles (for WITHER Desperation Cleave)
     */
    public static void playDarkSlash(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector dir = loc.getDirection().normalize();
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        for (int i = -40; i <= 40; i += 3) {
            double angle = Math.toRadians(i);
            Vector arcPoint = dir.clone().multiply(3).add(right.clone().multiply(Math.sin(angle) * 2));
            Location particleLoc = loc.clone().add(arcPoint);
            player.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 5, 0.1, 0.1, 0.1, 0.02);
            player.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.5f));
        }
    }

    /**
     * Shadow pull particles (for WITHER Soul Siphon)
     */
    public static void playShadowPull(Location start, Location end) {
        Vector dir = start.toVector().subtract(end.toVector()).normalize();
        for (int i = 0; i < 30; i++) {
            double ratio = (double) i / 30;
            Location loc = end.clone().add(dir.clone().multiply(start.distance(end) * ratio));
            end.getWorld().spawnParticle(Particle.SMOKE, loc, 3, 0.1, 0.1, 0.1, 0.02);
            end.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0,
                    new Particle.DustOptions(Color.fromRGB(50, 0, 50), 1.2f));
        }
    }

    /**
     * Blue shockwave particles (for WARDEN Sonic Slam)
     */
    public static void playBlueShockwave(Location center, double radius) {
        for (int ring = 1; ring <= 5; ring++) {
            double r = radius * ring / 5;
            for (int i = 0; i < 50; i++) {
                double angle = 2 * Math.PI * i / 50;
                double x = r * Math.cos(angle);
                double z = r * Math.sin(angle);
                Location loc = center.clone().add(x, 0.2, z);
                center.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);
                center.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.1, 0.1, 0.1, 0,
                        new Particle.DustOptions(Color.fromRGB(10, 50, 60), 1.5f));
            }
        }
    }

    /**
     * Sculk spread particles (for WARDEN Deep Dark Zone)
     */
    public static void playSculkSpread(Location center, double radius) {
        for (int i = 0; i < 60; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = Math.random() * radius;
            double x = dist * Math.cos(angle);
            double z = dist * Math.sin(angle);
            Location loc = center.clone().add(x, 0.1, z);
            center.getWorld().spawnParticle(Particle.SCULK_CHARGE, loc, 3, 0.2, 0.1, 0.2, 0);
            center.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    /**
     * White wind slash (for BREEZE Judging Strike)
     */
    public static void playWindSlash(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector dir = loc.getDirection().normalize();

        for (int i = 0; i < 30; i++) {
            Location particleLoc = loc.clone().add(dir.clone().multiply(i * 0.15));
            player.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 3, 0.1, 0.1, 0.1, 0);
            player.getWorld().spawnParticle(Particle.GUST, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Wind circles (for BREEZE Trial Order)
     */
    public static void playWindCircles(Location center, double radius) {
        for (int height = 0; height < 3; height++) {
            for (int i = 0; i < 40; i++) {
                double angle = 2 * Math.PI * i / 40;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location loc = center.clone().add(x, height * 0.5, z);
                center.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0, 0, 0, 0);
            }
        }
        center.getWorld().spawnParticle(Particle.GUST, center, 20, radius * 0.5, 1, radius * 0.5, 0);
    }

    /**
     * Purple flame slash (for DRAGON_EGG Rampaging Strike)
     */
    public static void playDragonSlash(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector dir = loc.getDirection().normalize();
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        for (int i = -50; i <= 50; i += 3) {
            double angle = Math.toRadians(i);
            Vector arcPoint = dir.clone().multiply(4).add(right.clone().multiply(Math.sin(angle) * 2.5));
            Location particleLoc = loc.clone().add(arcPoint);
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 5, 0.1, 0.1, 0.1, 0.03);
            player.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(Color.PURPLE, 1.8f));
        }
        player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1, 0.1);
    }

    /**
     * Aura dome (for DRAGON_EGG Dominion)
     */
    public static void playAuraDome(Location center, double radius) {
        for (int i = 0; i < 150; i++) {
            double theta = Math.acos(1 - 2.0 * i / 150);
            double phi = Math.PI * (1 + Math.sqrt(5)) * i;

            double x = radius * Math.sin(theta) * Math.cos(phi);
            double y = radius * Math.cos(theta);
            double z = radius * Math.sin(theta) * Math.sin(phi);

            if (y < 0) y = -y;

            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.PURPLE, 1.5f));
            if (i % 5 == 0) {
                center.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0.02);
            }
        }
    }

    /**
     * Draconic haze (for DRAGON_EGG Draconic Curse passive)
     */
    public static void playDraconicHaze(Location center, double radius) {
        for (int i = 0; i < 30; i++) {
            double x = (Math.random() - 0.5) * radius * 2;
            double z = (Math.random() - 0.5) * radius * 2;
            double y = Math.random() * 2;
            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.PURPLE, 1.2f));
            if (Math.random() < 0.2) {
                center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0, 0, 0, 0.01);
            }
        }
    }
}