package datathorn.owntheskies.combat;

import datathorn.owntheskies.config.LaunchConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class ElytraLauncher {
    private static LaunchState currentState = LaunchState.IDLE;
    private static long stateStartTime = 0;
    private static float originalPitch = 0;
    private static float originalYaw = 0;
    private static boolean autoLaunchEnabled = true; // Changed to true so R key works

    // Timing constants (in milliseconds)
    private static final long LOOK_UP_DURATION = 100;
    private static final long JUMP_DELAY = 150;
    private static final long GLIDE_START_DELAY = 200;
    private static final long FIREWORK_DELAY = 250;
    private static final long FIREWORK_INTERVAL = 300; // Time between firework uses
    private static int MAX_FIREWORKS = 1; // Maximum fireworks to use per launch (configurable)
    private static int fireworksUsed = 0;

    // Track if we swapped armor so we can swap back
    private static boolean didSwapToChestplate = false;

    public enum LaunchState {
        IDLE,
        LOOKING_UP,
        JUMPING,
        STARTING_GLIDE,
        USING_FIREWORKS,
        ASCENDING,
        SWAPPING_ARMOR,  // Swap elytra to chestplate at apex
        LOOKING_DOWN,
        SWAPPING_BACK,   // Swap chestplate back to elytra after landing
        COMPLETE
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || !autoLaunchEnabled) {
            return;
        }

        ClientPlayerEntity player = client.player;
        long currentTime = System.currentTimeMillis();
        long timeSinceStateStart = currentTime - stateStartTime;

        switch (currentState) {
            case IDLE:
                // Wait for launch trigger
                break;

            case LOOKING_UP:
                // Set camera angle based on launch mode
                // Vertical mode: straight up (-90°)
                // Other modes: tilted forward for horizontal distance
                float forwardTilt = LaunchConfig.getForwardPitchAdjustment();
                player.setPitch(-90.0f + forwardTilt);
                setState(LaunchState.JUMPING);
                break;

            case JUMPING:
                if (timeSinceStateStart >= JUMP_DELAY - LOOK_UP_DURATION) {
                    if (player.isOnGround()) {
                        player.jump();
                    }
                    setState(LaunchState.STARTING_GLIDE);
                }
                break;

            case STARTING_GLIDE:
                if (timeSinceStateStart >= GLIDE_START_DELAY - JUMP_DELAY) {
                    // Check if player has elytra equipped
                    ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
                    if (chestItem.getItem() == Items.ELYTRA && !isElytraFlying(player)) {
                        // Start elytra flight by sending the start flying packet
                        player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    }
                    setState(LaunchState.USING_FIREWORKS);
                }
                break;

            case USING_FIREWORKS:
                if (timeSinceStateStart >= FIREWORK_DELAY - GLIDE_START_DELAY) {
                    if (isElytraFlying(player) && fireworksUsed < MAX_FIREWORKS) {
                        useFirework(player);
                        setState(LaunchState.ASCENDING);
                    } else {
                        setState(LaunchState.ASCENDING);
                    }
                }
                break;

            case ASCENDING:
                // Continue using fireworks at intervals while ascending
                if (isElytraFlying(player) && fireworksUsed < MAX_FIREWORKS) {
                    if (timeSinceStateStart >= FIREWORK_INTERVAL) {
                        useFirework(player);
                        setState(LaunchState.ASCENDING); // Reset timer
                    }
                } else {
                    // Check if we've reached peak or started falling
                    Vec3d velocity = player.getVelocity();
                    if (velocity.y < 0 || fireworksUsed >= MAX_FIREWORKS) {
                        setState(LaunchState.SWAPPING_ARMOR);
                    }
                }
                break;

            case SWAPPING_ARMOR:
                // Swap from elytra to chestplate to apply full mace damage
                if (swapToChestplate(player)) {
                    setState(LaunchState.LOOKING_DOWN);
                } else {
                    // If swap fails, continue anyway
                    setState(LaunchState.LOOKING_DOWN);
                }
                break;

            case LOOKING_DOWN:
                // Quickly look down to ~85 degrees (faster for immediate mace strike)
                float targetPitch = 85.0f;
                float currentPitch = player.getPitch();
                if (Math.abs(currentPitch - targetPitch) > 5) {
                    float newPitch = currentPitch + (targetPitch - currentPitch); // Increased from 0.3 to 0.8 for faster adjustment
                    player.setPitch(newPitch);
                } else {
                    player.setPitch(targetPitch);
                    // Don't complete yet - wait for landing to swap back
                    if (player.isOnGround()) {
                        setState(LaunchState.SWAPPING_BACK);
                    }
                }
                break;

            case SWAPPING_BACK:
                // After landing, swap chestplate back to elytra for next launch
                if (didSwapToChestplate) {
                    swapBackToElytra(player);
                    didSwapToChestplate = false;
                }
                setState(LaunchState.COMPLETE);
                break;

            case COMPLETE:
                // Launch sequence complete, return to idle
                setState(LaunchState.IDLE);
                fireworksUsed = 0;
                didSwapToChestplate = false;
                break;
        }
    }

    private static void useFirework(ClientPlayerEntity player) {
        // Find firework in hotbar
        int fireworkSlot = findFireworkInHotbar(player);
        if (fireworkSlot != -1) {
            // Switch to firework slot
            setSelectedSlot(player, fireworkSlot);

            // Use firework
            ItemStack firework = player.getInventory().getStack(fireworkSlot);
            if (firework.getItem() == Items.FIREWORK_ROCKET) {
                // Right click with firework
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.interactionManager != null) {
                    client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                    fireworksUsed++;
                }
            }

            // The auto-switcher will handle switching back to the appropriate item
        }
    }

    private static int findFireworkInHotbar(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isElytraFlying(ClientPlayerEntity player) {
        // Check if the player is currently flying with elytra
        // Use reflection to access the protected getFlag method (flag 7 is FALL_FLYING)
        try {
            java.lang.reflect.Method getFlagMethod = net.minecraft.entity.Entity.class.getDeclaredMethod("getFlag", int.class);
            getFlagMethod.setAccessible(true);
            return (boolean) getFlagMethod.invoke(player, 7);
        } catch (Exception e) {
            // Fallback: check if player is falling and has elytra equipped
            ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
            return !player.isOnGround() && chestItem.getItem() == Items.ELYTRA && player.getVelocity().y < 0;
        }
    }

    /**
     * Swap elytra to chestplate at apex to apply full mace damage
     * Returns true if successful, false otherwise
     */
    private static boolean swapToChestplate(ClientPlayerEntity player) {
        // Check if wearing elytra
        ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chestItem.getItem() != Items.ELYTRA) {
            return false; // Already wearing chestplate or nothing
        }

        // Find chestplate in hotbar
        int chestplateSlot = findChestplateInHotbar(player);
        if (chestplateSlot == -1) {
            return false; // No chestplate in hotbar
        }

        // Get current selected slot to restore later
        int originalSlot = getSelectedSlot(player);

        // Switch to chestplate slot
        setSelectedSlot(player, chestplateSlot);

        // Swap armor by right-clicking (swap with equipped chest armor)
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null) {
            // Right-click to swap armor
            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
            didSwapToChestplate = true; // Mark that we swapped
        }

        // Switch back to original slot (or to mace if auto-switcher handles it)
        setSelectedSlot(player, originalSlot);

        return true;
    }

    /**
     * Find chestplate in hotbar
     */
    private static int findChestplateInHotbar(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            Item item = stack.getItem();
            if (item == Items.NETHERITE_CHESTPLATE ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.IRON_CHESTPLATE ||
                item == Items.CHAINMAIL_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.LEATHER_CHESTPLATE) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Swap chestplate back to elytra after landing
     */
    private static void swapBackToElytra(ClientPlayerEntity player) {
        // Check if wearing chestplate
        ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chestItem.getItem() == Items.ELYTRA) {
            return; // Already wearing elytra
        }

        // Find elytra in hotbar (it should be there from the previous swap)
        int elytraSlot = findElytraInHotbar(player);
        if (elytraSlot == -1) {
            return; // No elytra in hotbar
        }

        // Get current selected slot to restore later
        int originalSlot = getSelectedSlot(player);

        // Switch to elytra slot
        setSelectedSlot(player, elytraSlot);

        // Swap armor by right-clicking
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null) {
            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        }

        // Switch back to original slot
        setSelectedSlot(player, originalSlot);
    }

    /**
     * Find elytra in hotbar
     */
    private static int findElytraInHotbar(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the currently selected hotbar slot
     */
    private static int getSelectedSlot(ClientPlayerEntity player) {
        try {
            java.lang.reflect.Field selectedSlotField = net.minecraft.entity.player.PlayerInventory.class.getDeclaredField("selectedSlot");
            selectedSlotField.setAccessible(true);
            return (int) selectedSlotField.get(player.getInventory());
        } catch (Exception e) {
            return 0; // Default to slot 0
        }
    }


    private static void setSelectedSlot(ClientPlayerEntity player, int slot) {
        try {
            java.lang.reflect.Field selectedSlotField = net.minecraft.entity.player.PlayerInventory.class.getDeclaredField("selectedSlot");
            selectedSlotField.setAccessible(true);
            selectedSlotField.set(player.getInventory(), slot);
        } catch (Exception e) {
            try {
                java.lang.reflect.Field selectedSlotField = net.minecraft.entity.player.PlayerInventory.class.getDeclaredField("field_7545");
                selectedSlotField.setAccessible(true);
                selectedSlotField.set(player.getInventory(), slot);
            } catch (Exception ex) {
                System.err.println("OwnTheSkies: Failed to switch slots - " + ex.getMessage());
            }
        }
    }

    private static void setState(LaunchState newState) {
        currentState = newState;
        stateStartTime = System.currentTimeMillis();
    }

    public static void startLaunch() {
        if (currentState == LaunchState.IDLE) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                originalPitch = client.player.getPitch();
                originalYaw = client.player.getYaw();
                fireworksUsed = 0;
                setState(LaunchState.LOOKING_UP);
            }
        }
    }

    public static void cancelLaunch() {
        setState(LaunchState.IDLE);
        fireworksUsed = 0;
    }

    public static boolean isAutoLaunchEnabled() {
        return autoLaunchEnabled;
    }

    public static void toggleAutoLaunch() {
        autoLaunchEnabled = !autoLaunchEnabled;
    }

    public static void setAutoLaunch(boolean enabled) {
        autoLaunchEnabled = enabled;
    }

    public static void setMaxFireworks(int count) {
        // Clamp between 1 and 10
        MAX_FIREWORKS = Math.max(1, Math.min(10, count));
    }

    public static LaunchState getCurrentState() {
        return currentState;
    }

    public static boolean isLaunching() {
        return currentState != LaunchState.IDLE && currentState != LaunchState.COMPLETE;
    }
}
