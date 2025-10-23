package datathorn.owntheskies.inventory;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Helper class to detect different types of enchanted maces
 */
public class MaceDetector {

    public enum MaceType {
        DENSITY("Density Mace"),      // For aerial combat
        BREACH("Breach Mace"),         // For ground combat
        WIND_BURST("Wind Burst Mace"), // For mobility
        PLAIN("Plain Mace"),           // No special enchantments
        UNKNOWN("Mace");               // Generic fallback

        private final String displayName;

        MaceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Detect what type of mace this is based on enchantments
     */
    public static MaceType detectMaceType(ItemStack stack) {
        if (stack.getItem() != Items.MACE) {
            return MaceType.UNKNOWN;
        }

        // Get enchantments from the item
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            return MaceType.PLAIN;
        }

        // Check for specific enchantments
        boolean hasDensity = hasEnchantment(enchantments, "density");
        boolean hasBreach = hasEnchantment(enchantments, "breach");
        boolean hasWindBurst = hasEnchantment(enchantments, "wind_burst");

        // Prioritize based on enchantment presence
        if (hasDensity) {
            return MaceType.DENSITY;
        } else if (hasBreach) {
            return MaceType.BREACH;
        } else if (hasWindBurst) {
            return MaceType.WIND_BURST;
        }

        return MaceType.PLAIN;
    }

    /**
     * Check if enchantments contain a specific enchantment by checking the key path
     */
    private static boolean hasEnchantment(ItemEnchantmentsComponent enchantments, String enchantmentName) {
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            String key = entry.getKey().map(k -> k.getValue().getPath()).orElse("");
            if (key.contains(enchantmentName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the enchantment level for a specific enchantment
     */
    public static int getEnchantmentLevel(ItemStack stack, String enchantmentName) {
        if (stack.getItem() != Items.MACE) {
            return 0;
        }

        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) {
            return 0;
        }

        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            String key = entry.getKey().map(k -> k.getValue().getPath()).orElse("");
            if (key.contains(enchantmentName.toLowerCase())) {
                return enchantments.getLevel(entry);
            }
        }

        return 0;
    }

    /**
     * Check if a mace is suitable for aerial combat (has Density)
     */
    public static boolean isAerialMace(ItemStack stack) {
        return detectMaceType(stack) == MaceType.DENSITY;
    }

    /**
     * Check if a mace is suitable for ground combat (has Breach)
     */
    public static boolean isGroundMace(ItemStack stack) {
        return detectMaceType(stack) == MaceType.BREACH;
    }

    /**
     * Get a description of the mace including enchantment levels
     */
    public static String getMaceDescription(ItemStack stack) {
        MaceType type = detectMaceType(stack);

        switch (type) {
            case DENSITY:
                int densityLevel = getEnchantmentLevel(stack, "density");
                return "Density " + densityLevel + " Mace (Aerial)";
            case BREACH:
                int breachLevel = getEnchantmentLevel(stack, "breach");
                return "Breach " + breachLevel + " Mace (Ground)";
            case WIND_BURST:
                int windLevel = getEnchantmentLevel(stack, "wind_burst");
                return "Wind Burst " + windLevel + " Mace (Mobility)";
            case PLAIN:
                return "Plain Mace";
            default:
                return "Mace";
        }
    }
}

