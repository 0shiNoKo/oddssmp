package com.oddssmp;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleManager {

    /**
     * Play support ability particles for the given attribute
     */
    public static void playSupportParticles(Player player, AttributeType attribute, Tier tier, int level) {
        Location loc = player.getLocation().add(0, 1, 0);
        boolean isExtreme = tier == Tier.EXTREME;
        int particleCount = isExtreme ? 100 : 50;
        double radius = 6.0 * (1.0 + level * 0.15);

        switch (attribute) {
            case MELEE:
                spawnCircleParticles(loc, Particle.HAPPY_VILLAGER, radius, particleCount);
                spawnCircleParticles(loc, Particle.CRIT, radius * 0.8, particleCount / 2);
                spawnSpiralParticles(loc, Particle.SWEEP_ATTACK, radius, 40);
                if (isExtreme) {
                    spawnCircleParticles(loc, Particle.CRIT, radius * 1.2, 60);
                    spawnBurstParticles(loc, Particle.ENCHANTED_HIT, 80);
                }
                break;

            case SPEED:
                spawnColoredParticles(loc, Color.YELLOW, radius, particleCount);
                spawnColoredParticles(loc, Color.WHITE, radius * 0.8, particleCount / 2);
                spawnSpiralParticles(loc, Particle.CLOUD, radius, 60);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.FIREWORK, 100);
                }
                break;

            case PRESSURE:
                spawnCircleParticles(loc, Particle.ELECTRIC_SPARK, radius, particleCount);
                spawnColoredParticles(loc, Color.fromRGB(150, 0, 0), radius * 0.8, particleCount / 2);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.ELECTRIC_SPARK, 120);
                }
                break;

            case DISRUPTION:
                spawnColoredParticles(loc, Color.RED, radius, particleCount);
                spawnCircleParticles(loc, Particle.LAVA, radius * 0.7, particleCount / 2);
                if (isExtreme) {
                    spawnDomeParticles(loc, Color.RED, radius, 100);
                }
                break;

            case ANCHOR:
                spawnColoredParticles(loc, Color.GRAY, radius, particleCount);
                spawnColoredParticles(loc, Color.SILVER, radius * 0.7, particleCount / 2);
                spawnCircleParticles(loc, Particle.CRIT, radius, 50);
                if (isExtreme) {
                    spawnDomeParticles(loc, Color.GRAY, radius, 90);
                }
                break;

            case RISK:
                spawnCircleParticles(loc, Particle.FIREWORK, radius, particleCount);
                player.getWorld().spawnParticle(Particle.GLOW, loc, 80, radius, 1, radius, 0);
                spawnColoredParticles(loc, Color.RED, radius * 0.8, particleCount / 2);
                spawnColoredParticles(loc, Color.YELLOW, radius * 0.6, particleCount / 3);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.FIREWORK, 120);
                }
                break;

            case HEALTH:
                spawnCircleParticles(loc, Particle.HEART, radius, particleCount);
                spawnCircleParticles(loc, Particle.GLOW, radius * 0.7, particleCount / 2);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.HEART, 100);
                }
                break;

            case WEALTH:
                spawnCircleParticles(loc, Particle.GLOW, radius, particleCount);
                spawnCircleParticles(loc, Particle.END_ROD, radius * 0.8, particleCount / 2);
                player.getWorld().spawnParticle(Particle.ENCHANT, loc, 100, radius, 1, radius, 1);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.GLOW, 120);
                }
                break;

            case DEFENSE:
                spawnColoredParticles(loc, Color.BLUE, radius, particleCount);
                spawnShieldParticles(loc, Color.BLUE, radius);
                if (isExtreme) {
                    spawnDomeParticles(loc, Color.BLUE, radius, 80);
                }
                break;

            case CONTROL:
                spawnColoredParticles(loc, Color.PURPLE, radius, particleCount);
                spawnSpiralParticles(loc, Particle.WITCH, radius, 50);
                if (isExtreme) {
                    spawnDomeParticles(loc, Color.PURPLE, radius, 90);
                }
                break;

            case RANGE:
                spawnColoredParticles(loc, Color.ORANGE, radius, particleCount);
                spawnCircleParticles(loc, Particle.SWEEP_ATTACK, radius, 50);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.SWEEP_ATTACK, 90);
                }
                break;

            case TEMPO:
                spawnCircleParticles(loc, Particle.ENCHANT, radius, particleCount);
                spawnCircleParticles(loc, Particle.PORTAL, radius * 0.8, particleCount / 2);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.ENCHANT, 120);
                }
                break;

            case VISION:
                spawnCircleParticles(loc, Particle.GLOW, radius, particleCount);
                spawnCircleParticles(loc, Particle.END_ROD, radius * 0.8, particleCount / 2);
                if (isExtreme) {
                    spawnDomeParticles(loc, Color.WHITE, radius, 100);
                }
                break;

            case PERSISTENCE:
                spawnColoredParticles(loc, Color.RED, radius, particleCount);
                spawnCircleParticles(loc, Particle.TOTEM_OF_UNDYING, radius * 0.8, 40);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.TOTEM_OF_UNDYING, 80);
                }
                break;

            case TRANSFER:
                spawnColoredParticles(loc, Color.AQUA, radius, particleCount);
                spawnSpiralParticles(loc, Particle.DOLPHIN, radius, 60);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.GLOW, 100);
                }
                break;

            case WITHER:
                spawnColoredParticles(loc, Color.fromRGB(100, 0, 100), radius, particleCount * 2);
                spawnCircleParticles(loc, Particle.SMOKE, radius, particleCount);
                player.getWorld().spawnParticle(Particle.DUST, loc, 150, radius, 1, radius, 0,
                        new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.5f));
                if (isExtreme) {
                    player.getWorld().spawnParticle(Particle.DUST, loc, 100, radius * 1.2, 1.5, radius * 1.2, 0,
                            new Particle.DustOptions(Color.fromRGB(80, 0, 80), 1.5f));
                }
                break;

            case WARDEN:
                spawnColoredParticles(loc, Color.fromRGB(10, 50, 60), radius, particleCount * 2);
                spawnCircleParticles(loc, Particle.SCULK_SOUL, radius, particleCount);
                player.getWorld().spawnParticle(Particle.SCULK_CHARGE, loc, 100, radius, 1, radius, 0);
                if (isExtreme) {
                    spawnDomeParticles(loc, Color.fromRGB(10, 50, 60), radius, 100);
                }
                break;

            case BREEZE:
                spawnColoredParticles(loc, Color.YELLOW, radius, particleCount);
                spawnColoredParticles(loc, Color.WHITE, radius * 0.8, particleCount);
                spawnCircleParticles(loc, Particle.GUST, radius, 80);
                if (isExtreme) {
                    spawnBurstParticles(loc, Particle.GUST, 120);
                }
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
    public static void playMeleeParticles(Player player, Entity target, AttributeType attribute, Tier tier) {
        Location start = player.getLocation().add(0, 1, 0);
        Location end = target.getLocation().add(0, 1, 0);
        boolean isExtreme = tier == Tier.EXTREME;

        switch (attribute) {
            case MELEE:
                drawLine(start, end, Particle.CRIT, isExtreme ? 60 : 30);
                drawLine(start, end, Particle.SWEEP_ATTACK, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.ENCHANTED_HIT, end, isExtreme ? 50 : 25, 0.5, 0.5, 0.5, 0);
                if (isExtreme) {
                    target.getWorld().spawnParticle(Particle.EXPLOSION, end, 10);
                }
                break;

            case HEALTH:
                drawLine(start, end, Particle.HEART, isExtreme ? 50 : 25);
                drawColoredLine(start, end, Color.RED, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, end, isExtreme ? 40 : 20, 0.5, 0.5, 0.5, 0);
                break;

            case WEALTH:
                drawLine(start, end, Particle.GLOW, isExtreme ? 60 : 30);
                drawLine(start, end, Particle.END_ROD, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.ENCHANT, end, isExtreme ? 100 : 50, 0.5, 0.5, 0.5, 1);
                break;

            case DEFENSE:
                drawColoredLine(start, end, Color.BLUE, isExtreme ? 50 : 25);
                target.getWorld().spawnParticle(Particle.FIREWORK, end, isExtreme ? 50 : 25);
                if (isExtreme) {
                    spawnExplosionRing(end, Color.BLUE, 2.0, 40);
                }
                break;

            case CONTROL:
                drawColoredLine(start, end, Color.PURPLE, isExtreme ? 50 : 25);
                drawLine(start, end, Particle.WITCH, isExtreme ? 40 : 20);
                if (isExtreme) {
                    spawnExplosionRing(end, Color.PURPLE, 2.5, 50);
                }
                break;

            case RANGE:
                drawColoredLine(start, end, Color.ORANGE, isExtreme ? 50 : 25);
                drawLine(start, end, Particle.CRIT, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, end, isExtreme ? 60 : 30, 0.5, 0.5, 0.5, 0);
                break;

            case TEMPO:
                drawLine(start, end, Particle.ENCHANT, isExtreme ? 50 : 25);
                drawLine(start, end, Particle.PORTAL, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.GLOW, end, isExtreme ? 80 : 40, 0.5, 0.5, 0.5, 0);
                break;

            case VISION:
                drawLine(start, end, Particle.GLOW, isExtreme ? 60 : 30);
                drawLine(start, end, Particle.END_ROD, isExtreme ? 50 : 25);
                target.getWorld().spawnParticle(Particle.ENCHANT, end, isExtreme ? 100 : 50, 0.5, 0.5, 0.5, 1);
                break;

            case PERSISTENCE:
                drawColoredLine(start, end, Color.RED, isExtreme ? 50 : 25);
                drawLine(start, end, Particle.HEART, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, end, isExtreme ? 50 : 25, 0.5, 0.5, 0.5, 0);
                break;

            case TRANSFER:
                drawColoredLine(start, end, Color.AQUA, isExtreme ? 60 : 30);
                drawLine(start, end, Particle.DOLPHIN, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.GLOW, end, isExtreme ? 80 : 40, 0.5, 0.5, 0.5, 0);
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
                drawColoredLine(start, end, Color.YELLOW, isExtreme ? 60 : 30);
                drawColoredLine(start, end, Color.WHITE, isExtreme ? 40 : 20);
                drawLine(start, end, Particle.CLOUD, isExtreme ? 50 : 25);
                target.getWorld().spawnParticle(Particle.FIREWORK, end, isExtreme ? 60 : 30, 0.5, 0.5, 0.5, 0);
                break;

            case PRESSURE:
                drawLine(start, end, Particle.ELECTRIC_SPARK, isExtreme ? 60 : 30);
                drawColoredLine(start, end, Color.fromRGB(150, 0, 0), isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, end, isExtreme ? 80 : 40, 0.5, 0.5, 0.5, 0);
                break;

            case DISRUPTION:
                drawColoredLine(start, end, Color.RED, isExtreme ? 60 : 30);
                drawLine(start, end, Particle.LAVA, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.LAVA, end, isExtreme ? 60 : 30, 0.5, 0.5, 0.5, 0);
                break;

            case ANCHOR:
                drawColoredLine(start, end, Color.GRAY, isExtreme ? 60 : 30);
                drawColoredLine(start, end, Color.SILVER, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.CRIT, end, isExtreme ? 80 : 40, 0.5, 0.5, 0.5, 0);
                break;

            case RISK:
                drawLine(start, end, Particle.FIREWORK, isExtreme ? 80 : 40);
                drawColoredLine(start, end, Color.RED, isExtreme ? 60 : 30);
                drawColoredLine(start, end, Color.YELLOW, isExtreme ? 40 : 20);
                target.getWorld().spawnParticle(Particle.GLOW, end, isExtreme ? 100 : 50, 0.5, 0.5, 0.5, 0);
                break;
        }
    }

    /**
     * Play passive ability particles (ambient aura)
     */
    public static void playPassiveParticles(Player player, AttributeType attribute, Tier tier) {
        Location loc = player.getLocation().add(0, 1, 0);
        boolean isExtreme = tier == Tier.EXTREME;
        int count = isExtreme ? 25 : 12;

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

    // Helper methods
    private static void spawnCircleParticles(Location center, Particle particle, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private static void spawnSpiralParticles(Location center, Particle particle, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 4 * Math.PI * i / count;
            double currentRadius = radius * i / count;
            double x = currentRadius * Math.cos(angle);
            double z = currentRadius * Math.sin(angle);
            double y = 2.0 * i / count;
            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private static void spawnBurstParticles(Location center, Particle particle, int count) {
        center.getWorld().spawnParticle(particle, center, count, 1.5, 1.5, 1.5, 0.1);
    }

    private static void spawnShieldParticles(Location center, Color color, double radius) {
        for (int i = 0; i < 50; i++) {
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
        for (int i = 0; i < count; i++) {
            double theta = Math.acos(1 - 2.0 * i / count);
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
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(color, 1.5f));
        }
    }

    private static void spawnColoredParticles(Location center, Color color, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.0f));
        }
    }

    private static void spawnColoredParticle(Location loc, Color color, int count) {
        loc.getWorld().spawnParticle(Particle.DUST, loc, count, 0.3, 0.5, 0.3, 0,
                new Particle.DustOptions(color, 1.0f));
    }

    private static void spawnDragonParticles(Location center, double radius, int count) {
        center.getWorld().spawnParticle(Particle.FLAME, center, count, radius, 1.5, radius, 0.08);
        center.getWorld().spawnParticle(Particle.LAVA, center, count / 2, radius, 1, radius, 0);
        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, count / 2, radius, 1.5, radius, 0.05);
    }

    private static void drawLine(Location start, Location end, Particle particle, int points) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (int i = 0; i < points; i++) {
            double ratio = (double) i / points;
            Vector offset = direction.clone().multiply(length * ratio);
            Location loc = start.clone().add(offset);
            start.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private static void drawColoredLine(Location start, Location end, Color color, int points) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (int i = 0; i < points; i++) {
            double ratio = (double) i / points;
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
}