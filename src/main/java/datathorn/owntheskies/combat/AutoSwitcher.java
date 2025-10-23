package datathorn.owntheskies.combat;

import datathorn.owntheskies.config.LoadoutProfile;
import datathorn.owntheskies.config.ProfileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;

public class AutoSwitcher {
    private static boolean autoSwitchEnabled = true;
    private static final double MIN_FALL_SPEED = 0.5; // Minimum fall speed to trigger mace switch
    private static final double OPTIMAL_FALL_SPEED = 1.5; // Optimal fall speed for mace damage
    private static final double FAST_FALL_SPEED = 2.5; // Fast fall - use spam clicking

    private static boolean wasInAir = false;
    private static long lastSwitchTime = 0;
    private static final long SWITCH_COOLDOWN = 100; // ms between switches to prevent rapid toggling
    private static boolean spamClickMode = false; // Enable spam clicking for fast falls
    private static boolean preferSwapWeapon = true; // Prefer sword/axe over direct mace

    public static void tick(MinecraftClient client) {
        if (client.player == null || !autoSwitchEnabled) {
            return;
        }

        PlayerEntity player = client.player;
        Vec3d velocity = player.getVelocity();
        boolean isInAir = !player.isOnGround();

        // Check if enough time has passed since last switch
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwitchTime < SWITCH_COOLDOWN) {
            return;
        }

        LoadoutProfile profile = ProfileManager.getCurrentProfile();

        // Check if falling fast enough to switch to mace
        if (isInAir && velocity.y < -MIN_FALL_SPEED) {
            handleFallingState(player, profile);
        }
        // Check if just landed - might want to switch back to mobility
        else if (!isInAir && wasInAir) {
            handleLandedState(player, profile);
        }
        // Check if in air but not falling (ascending or gliding) - use mobility items
        else if (isInAir && velocity.y >= -MIN_FALL_SPEED) {
            handleAscendingState(player, profile);
        }

        wasInAir = isInAir;
    }

    private static void handleFallingState(PlayerEntity player, LoadoutProfile profile) {
        ItemStack currentItem = player.getMainHandStack();
        double fallSpeed = Math.abs(player.getVelocity().y);

        // For weapon swapping: switch to sword/axe FIRST when falling
        // The WeaponSwapper will handle switching to mace at the right moment
        if (profile.useWeaponSwapping() && preferSwapWeapon) {
            // If not holding a swap weapon or mace, switch to swap weapon
            if (!profile.isPrimaryWeapon(currentItem.getItem()) &&
                !profile.isSwapWeapon(currentItem.getItem())) {

                int swapWeaponSlot = findItemInHotbar(player, profile.getSwapWeapons());
                if (swapWeaponSlot != -1) {
                    switchToSlot(player, swapWeaponSlot);
                    lastSwitchTime = System.currentTimeMillis();
                    return;
                }
            }
        }

        // Fallback: direct mace switch if no weapon swapping or fast falling
        if (fallSpeed > FAST_FALL_SPEED || !profile.useWeaponSwapping()) {
            if (!profile.isPrimaryWeapon(currentItem.getItem())) {
                int weaponSlot = findItemInHotbar(player, profile.getPrimaryWeapons());
                if (weaponSlot != -1) {
                    switchToSlot(player, weaponSlot);
                    lastSwitchTime = System.currentTimeMillis();
                }
            }
        }
    }

    private static void handleLandedState(PlayerEntity player, LoadoutProfile profile) {
        ItemStack currentItem = player.getMainHandStack();

        // After landing with mace, switch to mobility item for next launch
        if (profile.isPrimaryWeapon(currentItem.getItem())) {
            int mobilitySlot = findItemInHotbar(player, profile.getMobilityItems());
            if (mobilitySlot != -1) {
                switchToSlot(player, mobilitySlot);
                lastSwitchTime = System.currentTimeMillis();
            }
        }
    }

    private static void handleAscendingState(PlayerEntity player, LoadoutProfile profile) {
        ItemStack currentItem = player.getMainHandStack();

        // When ascending/gliding, ensure we have mobility items ready
        if (profile.isPrimaryWeapon(currentItem.getItem())) {
            int mobilitySlot = findItemInHotbar(player, profile.getMobilityItems());
            if (mobilitySlot != -1) {
                switchToSlot(player, mobilitySlot);
                lastSwitchTime = System.currentTimeMillis();
            }
        }
    }

    private static int findItemInHotbar(PlayerEntity player, java.util.List<Item> items) {
        PlayerInventory inventory = player.getInventory();

        // Search hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && items.contains(stack.getItem())) {
                return i;
            }
        }

        return -1;
    }

    private static void switchToSlot(PlayerEntity player, int slot) {
        if (slot >= 0 && slot < 9) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                try {
                    // Use reflection to access the private selectedSlot field
                    Field selectedSlotField = PlayerInventory.class.getDeclaredField("selectedSlot");
                    selectedSlotField.setAccessible(true);
                    selectedSlotField.set(client.player.getInventory(), slot);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Fallback: try different field names used in different mappings
                    try {
                        Field selectedSlotField = PlayerInventory.class.getDeclaredField("field_7545"); // Intermediary mapping
                        selectedSlotField.setAccessible(true);
                        selectedSlotField.set(client.player.getInventory(), slot);
                    } catch (NoSuchFieldException | IllegalAccessException ex) {
                        System.err.println("OwnTheSkies: Failed to switch slots - " + ex.getMessage());
                    }
                }
            }
        }
    }

    public static boolean isAutoSwitchEnabled() {
        return autoSwitchEnabled;
    }

    public static void toggleAutoSwitch() {
        autoSwitchEnabled = !autoSwitchEnabled;
    }

    public static void setAutoSwitch(boolean enabled) {
        autoSwitchEnabled = enabled;
    }

    public static void setAutoSwitchEnabled(boolean enabled) {
        autoSwitchEnabled = enabled;
    }

    public static double getMinFallSpeed() {
        return MIN_FALL_SPEED;
    }

    public static double getOptimalFallSpeed() {
        return OPTIMAL_FALL_SPEED;
    }

    public static boolean isSpamClickMode() {
        return spamClickMode;
    }

    public static void toggleSpamClickMode() {
        spamClickMode = !spamClickMode;
    }

    public static boolean isPreferSwapWeapon() {
        return preferSwapWeapon;
    }

    public static void togglePreferSwapWeapon() {
        preferSwapWeapon = !preferSwapWeapon;
    }
}

