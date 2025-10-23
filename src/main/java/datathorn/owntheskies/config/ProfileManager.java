package datathorn.owntheskies.config;

import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileManager {
    private static final List<LoadoutProfile> profiles = new ArrayList<>();
    private static int currentProfileIndex = 0;

    static {
        // Profile 1: Density Mace - Standard aerial combat with weapon swapping
        profiles.add(new LoadoutProfile(
            "Density (Aerial)",
            Arrays.asList(Items.MACE),
            Arrays.asList(Items.NETHERITE_AXE, Items.NETHERITE_SWORD, Items.DIAMOND_AXE),
            Arrays.asList(Items.WIND_CHARGE, Items.ENDER_PEARL, Items.FIREWORK_ROCKET),
            Arrays.asList(Items.ELYTRA, Items.NETHERITE_CHESTPLATE),
            true,  // Use weapon swapping (axe/sword to mace)
            true,  // Use armor swapping (elytra to chestplate)
            false  // Don't use shield counter
        ));

        // Profile 2: Breach Mace - Ground combat focused
        profiles.add(new LoadoutProfile(
            "Breach (Ground)",
            Arrays.asList(Items.MACE),
            Arrays.asList(Items.NETHERITE_AXE, Items.NETHERITE_SWORD),
            Arrays.asList(Items.WIND_CHARGE, Items.ENDER_PEARL),
            Arrays.asList(Items.NETHERITE_CHESTPLATE),
            true,  // Use weapon swapping for maximum damage
            false, // No armor swapping needed for ground combat
            false  // Don't use shield counter
        ));

        // Profile 3: Shield Counter - Stun slam technique
        profiles.add(new LoadoutProfile(
            "Shield Counter",
            Arrays.asList(Items.MACE),
            Arrays.asList(Items.NETHERITE_AXE), // Axe specifically for shield stun
            Arrays.asList(Items.WIND_CHARGE, Items.ENDER_PEARL, Items.FIREWORK_ROCKET),
            Arrays.asList(Items.ELYTRA, Items.NETHERITE_CHESTPLATE),
            true,  // Use weapon swapping
            true,  // Use armor swapping
            true   // Enable shield counter technique
        ));

        // Profile 4: Hybrid - Balanced for all situations
        profiles.add(new LoadoutProfile(
            "Hybrid",
            Arrays.asList(Items.MACE),
            Arrays.asList(Items.NETHERITE_AXE, Items.NETHERITE_SWORD),
            Arrays.asList(Items.WIND_CHARGE, Items.ENDER_PEARL, Items.FIREWORK_ROCKET),
            Arrays.asList(Items.ELYTRA, Items.NETHERITE_CHESTPLATE),
            true,  // Use weapon swapping
            true,  // Use armor swapping
            false  // Don't use shield counter
        ));
    }

    public static LoadoutProfile getCurrentProfile() {
        return profiles.get(currentProfileIndex);
    }

    public static void nextProfile() {
        currentProfileIndex = (currentProfileIndex + 1) % profiles.size();
    }

    public static void previousProfile() {
        currentProfileIndex = (currentProfileIndex - 1 + profiles.size()) % profiles.size();
    }

    public static List<LoadoutProfile> getAllProfiles() {
        return profiles;
    }

    public static void addProfile(LoadoutProfile profile) {
        profiles.add(profile);
    }

    public static int getCurrentProfileIndex() {
        return currentProfileIndex;
    }
}

