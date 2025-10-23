package datathorn.owntheskies.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Handles automatic attack execution when mace is equipped and falling.
 * Prevents missing the swing and taking unnecessary fall damage.
 */
public class AutoAttack {
    private static boolean autoAttackEnabled = true;
    private static long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 50; // 50ms between attacks
    private static final double ATTACK_RANGE = 4.0; // Blocks

    private static boolean hasAttacked = false;
    private static boolean wasHoldingMace = false;

    public static void tick(MinecraftClient client) {
        if (client.player == null || !autoAttackEnabled) {
            return;
        }

        ClientPlayerEntity player = client.player;
        boolean isHoldingMace = player.getMainHandStack().getItem().toString().toLowerCase().contains("mace");

        // Detect when we just switched to mace
        if (isHoldingMace && !wasHoldingMace) {
            hasAttacked = false; // Reset attack flag when switching to mace
        }

        wasHoldingMace = isHoldingMace;

        // Only auto-attack if holding mace and falling
        if (!isHoldingMace || player.isOnGround()) {
            hasAttacked = false; // Reset when on ground
            return;
        }

        double fallSpeed = Math.abs(player.getVelocity().y);

        // Check if falling fast enough and haven't attacked yet this fall
        if (fallSpeed > 0.5 && !hasAttacked) {
            // Check if there's a target in range
            Entity target = findTargetInRange(client, player);

            if (target != null) {
                // Check attack cooldown
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAttackTime >= ATTACK_COOLDOWN) {
                    performAttack(client, player, target);
                    lastAttackTime = currentTime;
                    hasAttacked = true; // Mark that we've attacked this fall
                }
            } else {
                // If close to ground (within 2 blocks), attack anyway to prevent fall damage
                if (isCloseToGround(client, player)) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastAttackTime >= ATTACK_COOLDOWN && !hasAttacked) {
                        performAttack(client, player, null);
                        lastAttackTime = currentTime;
                        hasAttacked = true;
                    }
                }
            }
        }
    }

    /**
     * Find the closest entity within attack range
     */
    private static Entity findTargetInRange(MinecraftClient client, ClientPlayerEntity player) {
        if (client.world == null) {
            return null;
        }

        Vec3d playerPos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d targetPos = playerPos.add(lookVec.multiply(ATTACK_RANGE));

        // Create a bounding box for entity search
        Box searchBox = new Box(playerPos, targetPos).expand(2.0);

        // Find entities in range
        List<Entity> entities = client.world.getOtherEntities(player, searchBox);

        Entity closestEntity = null;
        double closestDistance = ATTACK_RANGE;

        for (Entity entity : entities) {
            // Check if entity is attackable (not dead, not spectator, etc.)
            if (!entity.isAttackable()) {
                continue;
            }

            double distance = player.distanceTo(entity);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    /**
     * Check if player is close to the ground (within 2 blocks)
     */
    private static boolean isCloseToGround(MinecraftClient client, ClientPlayerEntity player) {
        if (client.world == null) {
            return false;
        }

        // Check blocks below player
        for (int i = 1; i <= 2; i++) {
            if (!client.world.getBlockState(player.getBlockPos().down(i)).isAir()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Perform the attack
     */
    private static void performAttack(MinecraftClient client, ClientPlayerEntity player, Entity target) {
        if (client.interactionManager == null) {
            return;
        }

        // Attack the target if specified
        if (target != null) {
            client.interactionManager.attackEntity(player, target);
        } else {
            // Swing even without a target to trigger mace mechanics
            player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }

        // Visual feedback - play swing animation
        player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
    }

    public static boolean isAutoAttackEnabled() {
        return autoAttackEnabled;
    }

    public static void toggleAutoAttack() {
        autoAttackEnabled = !autoAttackEnabled;
    }

    public static void setAutoAttack(boolean enabled) {
        autoAttackEnabled = enabled;
    }

    public static void setAutoAttackEnabled(boolean enabled) {
        autoAttackEnabled = enabled;
    }
}

