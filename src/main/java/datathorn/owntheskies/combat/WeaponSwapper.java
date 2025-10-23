package datathorn.owntheskies.combat;

import datathorn.owntheskies.config.LoadoutProfile;
import datathorn.owntheskies.config.ProfileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.lang.reflect.Field;

/**
 * Handles the critical weapon and armor swapping techniques for mace PvP:
 * 1. Sword/Axe Swapping - Swap from axe/sword to mace right before hit to nearly double damage
 * 2. Elytra to Chestplate Swap - Swap from elytra to chestplate when falling to apply full mace damage
 * 3. Shield Counter - Use axe to stun shield, then swap to mace
 */
public class WeaponSwapper {
    private static boolean isInSwapSequence = false;
    private static long swapStartTime = 0;
    private static final long SWAP_TIMING_MS = 50; // Swap to mace 50ms before impact
    private static SwapType currentSwapType = SwapType.NONE;

    private static boolean hasSwappedToMace = false;
    private static int originalWeaponSlot = -1;
    private static boolean weaponSwapEnabled = true;
    private static boolean armorSwapEnabled = true;

    // Track if we're wearing elytra and need to swap to chestplate
    private static boolean needsArmorSwap = false;

    public enum SwapType {
        NONE,
        WEAPON_SWAP,    // Sword/Axe to Mace
        ARMOR_SWAP,     // Elytra to Chestplate
        SHIELD_COUNTER  // Axe stun then Mace
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        ClientPlayerEntity player = client.player;
        LoadoutProfile profile = ProfileManager.getCurrentProfile();

        // Check if we need to perform weapon swapping
        if (profile.useWeaponSwapping() && weaponSwapEnabled) {
            handleWeaponSwapping(player, profile);
        }

        // Check if we need to perform armor swapping
        if (profile.useArmorSwapping() && armorSwapEnabled) {
            handleArmorSwapping(player, profile);
        }
    }

    /**
     * Implements the sword/axe swapping technique:
     * - Hold sword/axe while approaching
     * - Swap to mace RIGHT BEFORE clicking to land the blow
     * - This transfers the damage and attack speed bonus
     */
    private static void handleWeaponSwapping(ClientPlayerEntity player, LoadoutProfile profile) {
        ItemStack mainHand = player.getMainHandStack();
        double fallSpeed = Math.abs(player.getVelocity().y);

        // If we're falling fast with a swap weapon equipped
        if (fallSpeed > 0.5 && profile.isSwapWeapon(mainHand.getItem())) {
            // Check if we're close to landing (within ~2 blocks of ground or entity)
            if (isNearTarget(player)) {
                // Initiate swap to mace
                int maceSlot = findItemInHotbar(player, profile.getPrimaryWeapons());
                if (maceSlot != -1 && !hasSwappedToMace) {
                    switchToSlot(player, maceSlot);
                    hasSwappedToMace = true;
                    currentSwapType = SwapType.WEAPON_SWAP;
                }
            }
        }

        // Reset after landing
        if (player.isOnGround() && hasSwappedToMace) {
            hasSwappedToMace = false;
            currentSwapType = SwapType.NONE;
            // Auto-switcher will handle switching back to mobility items
        }
    }

    /**
     * Implements the elytra to chestplate swapping technique:
     * - When falling with elytra, mace damage doesn't apply
     * - Swap to chestplate mid-fall to apply full accumulated damage
     * - Left-click swaps armor in Minecraft
     */
    private static void handleArmorSwapping(ClientPlayerEntity player, LoadoutProfile profile) {
        ItemStack chestArmor = player.getEquippedStack(EquipmentSlot.CHEST);
        double fallSpeed = Math.abs(player.getVelocity().y);

        // If we're falling fast while wearing elytra
        if (fallSpeed > 1.0 && chestArmor.getItem() == Items.ELYTRA) {
            // Check if we have mace equipped (ready to strike)
            ItemStack mainHand = player.getMainHandStack();
            if (profile.isPrimaryWeapon(mainHand.getItem())) {
                // Check if we're close to target
                if (isNearTarget(player)) {
                    // Find chestplate in hotbar
                    int chestplateSlot = findChestplateInHotbar(player);
                    if (chestplateSlot != -1) {
                        // Switch to chestplate slot and swap armor
                        int originalSlot = getSelectedSlot(player);
                        switchToSlot(player, chestplateSlot);

                        // Simulate left-click to swap armor (drop + pickup)
                        // Note: This requires additional packet handling
                        swapChestArmor(player, chestplateSlot);

                        // Switch back to mace
                        int maceSlot = findItemInHotbar(player, profile.getPrimaryWeapons());
                        if (maceSlot != -1) {
                            switchToSlot(player, maceSlot);
                        }

                        needsArmorSwap = false;
                        currentSwapType = SwapType.ARMOR_SWAP;
                    }
                }
            }
        }

        // Swap back to elytra after landing
        if (player.isOnGround() && currentSwapType == SwapType.ARMOR_SWAP) {
            // Find elytra in hotbar and swap back
            int elytraSlot = findElytraInHotbar(player);
            if (elytraSlot != -1) {
                int originalSlot = getSelectedSlot(player);
                switchToSlot(player, elytraSlot);
                swapChestArmor(player, elytraSlot);

                // Switch back to original item
                switchToSlot(player, originalSlot);
            }
            currentSwapType = SwapType.NONE;
        }
    }

    /**
     * Check if player is near a target (ground or entity)
     */
    private static boolean isNearTarget(ClientPlayerEntity player) {
        // Simple check: if player is within 3 blocks of ground
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return false;
        }

        // Check blocks below player
        for (int i = 1; i <= 3; i++) {
            if (!client.world.getBlockState(player.getBlockPos().down(i)).isAir()) {
                return true;
            }
        }

        // TODO: Add entity proximity check for aerial targets

        return false;
    }

    private static void swapChestArmor(ClientPlayerEntity player, int armorSlot) {
        // This would require sending appropriate packets to swap armor
        // For now, this is a placeholder - full implementation would need:
        // 1. ClientCommandC2SPacket or ClickSlotC2SPacket
        // 2. Proper slot coordination between hotbar and armor slot

        // Simplified approach: player would need to manually left-click
        // or we'd need to simulate the click packet
    }

    private static int findChestplateInHotbar(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.NETHERITE_CHESTPLATE ||
                stack.getItem() == Items.DIAMOND_CHESTPLATE) {
                return i;
            }
        }
        return -1;
    }

    private static int findElytraInHotbar(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }

    private static int findItemInHotbar(ClientPlayerEntity player, java.util.List<Item> items) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && items.contains(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private static void switchToSlot(ClientPlayerEntity player, int slot) {
        if (slot >= 0 && slot < 9) {
            try {
                Field selectedSlotField = PlayerInventory.class.getDeclaredField("selectedSlot");
                selectedSlotField.setAccessible(true);
                selectedSlotField.set(player.getInventory(), slot);
            } catch (Exception e) {
                try {
                    Field selectedSlotField = PlayerInventory.class.getDeclaredField("field_7545");
                    selectedSlotField.setAccessible(true);
                    selectedSlotField.set(player.getInventory(), slot);
                } catch (Exception ex) {
                    System.err.println("OwnTheSkies: Failed to switch slots - " + ex.getMessage());
                }
            }
        }
    }

    private static int getSelectedSlot(ClientPlayerEntity player) {
        try {
            Field selectedSlotField = PlayerInventory.class.getDeclaredField("selectedSlot");
            selectedSlotField.setAccessible(true);
            return (int) selectedSlotField.get(player.getInventory());
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isWeaponSwapEnabled() {
        return weaponSwapEnabled;
    }

    public static void toggleWeaponSwap() {
        weaponSwapEnabled = !weaponSwapEnabled;
    }

    public static boolean isArmorSwapEnabled() {
        return armorSwapEnabled;
    }

    public static void toggleArmorSwap() {
        armorSwapEnabled = !armorSwapEnabled;
    }

    public static SwapType getCurrentSwapType() {
        return currentSwapType;
    }
}

