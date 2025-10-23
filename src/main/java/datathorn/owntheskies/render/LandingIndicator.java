package datathorn.owntheskies.render;

import datathorn.owntheskies.combat.ElytraLauncher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Uses particles to show where the player will land after an elytra launch
 */
public class LandingIndicator {
    private static boolean enabled = true;
    private static boolean preLaunchMode = false; // Show indicator before/during launch
    private static Vec3d predictedLandingPos = null;
    private static long lastPredictionTime = 0;
    private static final long PREDICTION_UPDATE_INTERVAL = 50; // Update every 50ms

    private static long lastParticleSpawnTime = 0;
    private static final long PARTICLE_SPAWN_INTERVAL = 100; // Spawn particles every 100ms
    private static final int PARTICLES_PER_SPAWN = 20; // Number of particles to spawn

    /**
     * Spawn particles at the predicted landing location
     */
    public static void spawnParticles(MinecraftClient client) {
        if (!enabled || predictedLandingPos == null || client.world == null) {
            return;
        }

        // Only show during launch sequence (or in pre-launch mode, show always)
        if (!ElytraLauncher.isLaunching() && !preLaunchMode) {
            predictedLandingPos = null;
            return;
        }

        // Throttle particle spawning
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastParticleSpawnTime < PARTICLE_SPAWN_INTERVAL) {
            return;
        }
        lastParticleSpawnTime = currentTime;

        // Spawn a circle of particles at the landing spot
        double x = predictedLandingPos.x;
        double y = predictedLandingPos.y;
        double z = predictedLandingPos.z;

        // Spawn particles in a circle pattern
        for (int i = 0; i < PARTICLES_PER_SPAWN; i++) {
            double angle = (i * 2.0 * Math.PI) / PARTICLES_PER_SPAWN;
            double radius = 1.5; // Circle radius

            double particleX = x + Math.cos(angle) * radius;
            double particleZ = z + Math.sin(angle) * radius;

            // Spawn flame particles in a circle using particle manager
            client.particleManager.addParticle(
                ParticleTypes.FLAME,
                particleX,
                y + 0.1,
                particleZ,
                0.0, // velocityX
                0.02, // velocityY (slight upward drift)
                0.0  // velocityZ
            );
        }

        // Spawn some happy villager particles (green) for visibility
        for (int i = 0; i < 5; i++) {
            double angle = (i * 2.0 * Math.PI) / 5;
            double radius = 1.0;

            double particleX = x + Math.cos(angle) * radius;
            double particleZ = z + Math.sin(angle) * radius;

            client.particleManager.addParticle(
                ParticleTypes.HAPPY_VILLAGER,
                particleX,
                y + 0.5,
                particleZ,
                0.0,
                0.0,
                0.0
            );
        }

        // Spawn vertical beam particles
        for (int i = 0; i < 10; i++) {
            double yOffset = i * 2.0; // Every 2 blocks up to 20 blocks high

            client.particleManager.addParticle(
                ParticleTypes.END_ROD,
                x,
                y + yOffset,
                z,
                0.0,
                0.05,
                0.0
            );
        }
    }

    /**
     * Update the predicted landing position based on player trajectory
     */
    public static void updatePrediction(MinecraftClient client) {
        if (!enabled || client.player == null || client.world == null) {
            return;
        }

        // Only update during launch (or always in pre-launch mode)
        if (!ElytraLauncher.isLaunching() && !preLaunchMode) {
            predictedLandingPos = null;
            return;
        }

        // Throttle updates
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPredictionTime < PREDICTION_UPDATE_INTERVAL) {
            return;
        }
        lastPredictionTime = currentTime;

        // Predict landing position based on simple physics
        Vec3d playerPos = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
        Vec3d velocity = client.player.getVelocity();

        // Simulate trajectory
        Vec3d simPos = playerPos;
        Vec3d simVel = velocity;

        int maxIterations = 200; // Prevent infinite loops
        int iteration = 0;

        while (iteration < maxIterations) {
            iteration++;

            // Apply gravity and drag (simplified)
            simVel = simVel.add(0, -0.08, 0); // Gravity
            simVel = simVel.multiply(0.98); // Air resistance

            // Move position
            simPos = simPos.add(simVel);

            // Check if hit ground
            BlockPos blockPos = BlockPos.ofFloored(simPos.x, simPos.y - 0.5, simPos.z);
            if (!client.world.getBlockState(blockPos).isAir()) {
                // Found ground
                predictedLandingPos = new Vec3d(simPos.x, blockPos.getY() + 1, simPos.z);

                // Spawn particles immediately when prediction updates
                spawnParticles(client);
                return;
            }

            // Stop if too far or too low
            if (simPos.y < client.world.getBottomY() || simPos.distanceTo(playerPos) > 200) {
                break;
            }
        }

        // If no ground found, use last known position
        if (predictedLandingPos == null && iteration > 0) {
            predictedLandingPos = simPos;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        LandingIndicator.enabled = enabled;
        if (!enabled) {
            predictedLandingPos = null;
        }
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            predictedLandingPos = null;
        }
    }

    public static Vec3d getPredictedLandingPos() {
        return predictedLandingPos;
    }

    public static boolean isPreLaunchMode() {
        return preLaunchMode;
    }

    public static void togglePreLaunchMode() {
        preLaunchMode = !preLaunchMode;
        if (!preLaunchMode) {
            predictedLandingPos = null; // Clear prediction when disabling
        }
    }

    public static void setPreLaunchMode(boolean enabled) {
        preLaunchMode = enabled;
        if (!preLaunchMode) {
            predictedLandingPos = null;
        }
    }
}

