package datathorn.owntheskies.inventory;

import datathorn.owntheskies.config.LoadoutProfile;
import datathorn.owntheskies.config.ProfileManager;
import datathorn.owntheskies.inventory.MaceDetector.MaceType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Automatically organizes the hotbar based on the current loadout profile.
 * Pulls items from inventory and arranges them optimally for mace PvP.
 * Now includes smart mace selection based on enchantments!
 */
public class HotbarOrganizer {

    // Optimal hotbar layout positions
    private static final int SLOT_SWAP_WEAPON = 0;     // Netherite Axe (primary held weapon)
    private static final int SLOT_MACE = 1;            // Mace
    private static final int SLOT_WIND_CHARGE = 2;     // Wind Charge
    private static final int SLOT_FIREWORK = 3;        // Firework Rocket
    private static final int SLOT_ENDER_PEARL = 4;     // Ender Pearl
    private static final int SLOT_CHESTPLATE = 5;      // Netherite Chestplate
    private static final int SLOT_ELYTRA = 6;          // Elytra
    private static final int SLOT_SHIELD = 7;          // Shield
    private static final int SLOT_UTILITY = 8;         // Golden Apple / Potion

    /**
     * Organize the hotbar based on the current profile
     */
    public static void organizeHotbar(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        ClientPlayerEntity player = client.player;
        LoadoutProfile profile = ProfileManager.getCurrentProfile();
        PlayerInventory inventory = player.getInventory();

        // Build desired hotbar layout
        Map<Integer, Item> desiredLayout = buildDesiredLayout(profile);

        // Create a temporary storage for items we want in hotbar
        Map<Integer, ItemStack> tempHotbar = new HashMap<>();

        // Collect items from entire inventory that we need
        for (Map.Entry<Integer, Item> entry : desiredLayout.entrySet()) {
            int targetSlot = entry.getKey();
            Item desiredItem = entry.getValue();

            // Check if already in correct position
            ItemStack currentStack = inventory.getStack(targetSlot);
            if (!currentStack.isEmpty() && currentStack.getItem() == desiredItem) {
                tempHotbar.put(targetSlot, currentStack);
                continue;
            }

            // Find the item in inventory (search all slots)
            int sourceSlot = findItemInInventory(inventory, desiredItem);
            if (sourceSlot != -1) {
                ItemStack foundStack = inventory.getStack(sourceSlot);
                tempHotbar.put(targetSlot, foundStack.copy());
                // Mark source as empty (will be handled in placement phase)
                inventory.setStack(sourceSlot, ItemStack.EMPTY);
            }
        }

        // Clear hotbar slots that aren't in desired layout
        for (int i = 0; i < 9; i++) {
            if (!tempHotbar.containsKey(i)) {
                // Move unwanted items to main inventory
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty()) {
                    moveItemToMainInventory(inventory, stack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }

        // Place items in correct hotbar positions
        for (Map.Entry<Integer, ItemStack> entry : tempHotbar.entrySet()) {
            int slot = entry.getKey();
            ItemStack stack = entry.getValue();
            inventory.setStack(slot, stack);
        }

        // Send confirmation message
        client.player.sendMessage(
            net.minecraft.text.Text.literal("§aHotbar organized for: §e" + profile.getName()),
            true
        );
    }

    /**
     * Build the desired hotbar layout based on the profile
     */
    private static Map<Integer, Item> buildDesiredLayout(LoadoutProfile profile) {
        Map<Integer, Item> layout = new HashMap<>();

        // Slot 0: Swap weapon (Netherite Axe preferred)
        if (!profile.getSwapWeapons().isEmpty()) {
            // Prefer Netherite Axe
            if (profile.getSwapWeapons().contains(Items.NETHERITE_AXE)) {
                layout.put(SLOT_SWAP_WEAPON, Items.NETHERITE_AXE);
            } else {
                layout.put(SLOT_SWAP_WEAPON, profile.getSwapWeapons().get(0));
            }
        }

        // Slot 1: Mace
        if (!profile.getPrimaryWeapons().isEmpty()) {
            layout.put(SLOT_MACE, profile.getPrimaryWeapons().get(0));
        }

        // Slots 2-4: Mobility items
        List<Item> mobilityItems = profile.getMobilityItems();
        if (mobilityItems.contains(Items.WIND_CHARGE)) {
            layout.put(SLOT_WIND_CHARGE, Items.WIND_CHARGE);
        }
        if (mobilityItems.contains(Items.FIREWORK_ROCKET)) {
            layout.put(SLOT_FIREWORK, Items.FIREWORK_ROCKET);
        }
        if (mobilityItems.contains(Items.ENDER_PEARL)) {
            layout.put(SLOT_ENDER_PEARL, Items.ENDER_PEARL);
        }

        // Slots 5-6: Armor items
        List<Item> armorItems = profile.getArmorItems();
        if (armorItems.contains(Items.NETHERITE_CHESTPLATE)) {
            layout.put(SLOT_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        } else if (armorItems.contains(Items.DIAMOND_CHESTPLATE)) {
            layout.put(SLOT_CHESTPLATE, Items.DIAMOND_CHESTPLATE);
        }
        if (armorItems.contains(Items.ELYTRA)) {
            layout.put(SLOT_ELYTRA, Items.ELYTRA);
        }

        // Slot 7: Shield (if available)
        layout.put(SLOT_SHIELD, Items.SHIELD);

        // Slot 8: Utility (Golden Apple)
        layout.put(SLOT_UTILITY, Items.GOLDEN_APPLE);

        return layout;
    }

    /**
     * Find an item in the player's inventory (including hotbar)
     * Returns the slot number or -1 if not found
     */
    private static int findItemInInventory(PlayerInventory inventory, Item item) {
        // Search entire inventory (0-35)
        // 0-8: hotbar
        // 9-35: main inventory
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Move an item to the main inventory (slots 9-35)
     */
    private static void moveItemToMainInventory(PlayerInventory inventory, ItemStack stack) {
        // Find first empty slot in main inventory
        for (int i = 9; i < 36; i++) {
            if (inventory.getStack(i).isEmpty()) {
                inventory.setStack(i, stack);
                return;
            }
        }
        // If no empty slots, just drop it (shouldn't happen in normal cases)
    }

    /**
     * Get a summary of what items are missing from inventory
     */
    public static String getMissingItemsSummary(ClientPlayerEntity player) {
        LoadoutProfile profile = ProfileManager.getCurrentProfile();
        PlayerInventory inventory = player.getInventory();

        List<String> missingItems = new ArrayList<>();
        Map<Integer, Item> desiredLayout = buildDesiredLayout(profile);

        for (Item item : desiredLayout.values()) {
            if (findItemInInventory(inventory, item) == -1) {
                missingItems.add(item.toString().replace("minecraft:", ""));
            }
        }

        if (missingItems.isEmpty()) {
            return "§aAll items available!";
        } else {
            return "§cMissing: §e" + String.join(", ", missingItems);
        }
    }
}

