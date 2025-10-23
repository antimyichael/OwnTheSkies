package datathorn.owntheskies.config;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class LoadoutProfile {
    private final String name;
    private final List<Item> primaryWeapons; // Mace variants
    private final List<Item> swapWeapons; // Sword/Axe for damage boosting
    private final List<Item> mobilityItems; // Wind charges, ender pearls, etc.
    private final List<Item> armorItems; // Elytra, chestplate
    private final boolean useWeaponSwapping; // Enable sword/axe swapping technique
    private final boolean useArmorSwapping; // Enable elytra to chestplate swap
    private final boolean useShieldCounter; // Enable shield stun technique

    public LoadoutProfile(String name) {
        this.name = name;
        this.primaryWeapons = new ArrayList<>();
        this.swapWeapons = new ArrayList<>();
        this.mobilityItems = new ArrayList<>();
        this.armorItems = new ArrayList<>();
        this.useWeaponSwapping = true;
        this.useArmorSwapping = true;
        this.useShieldCounter = false;

        // Default items
        this.primaryWeapons.add(Items.MACE);
        this.swapWeapons.add(Items.NETHERITE_AXE);
        this.swapWeapons.add(Items.NETHERITE_SWORD);
        this.mobilityItems.add(Items.WIND_CHARGE);
        this.mobilityItems.add(Items.ENDER_PEARL);
        this.armorItems.add(Items.ELYTRA);
        this.armorItems.add(Items.NETHERITE_CHESTPLATE);
    }

    public LoadoutProfile(String name, List<Item> weapons, List<Item> swapWeapons, List<Item> mobility,
                         List<Item> armor, boolean weaponSwap, boolean armorSwap, boolean shieldCounter) {
        this.name = name;
        this.primaryWeapons = new ArrayList<>(weapons);
        this.swapWeapons = new ArrayList<>(swapWeapons);
        this.mobilityItems = new ArrayList<>(mobility);
        this.armorItems = new ArrayList<>(armor);
        this.useWeaponSwapping = weaponSwap;
        this.useArmorSwapping = armorSwap;
        this.useShieldCounter = shieldCounter;
    }

    public String getName() {
        return name;
    }

    public List<Item> getPrimaryWeapons() {
        return primaryWeapons;
    }

    public List<Item> getSwapWeapons() {
        return swapWeapons;
    }

    public List<Item> getMobilityItems() {
        return mobilityItems;
    }

    public List<Item> getArmorItems() {
        return armorItems;
    }

    public boolean useWeaponSwapping() {
        return useWeaponSwapping;
    }

    public boolean useArmorSwapping() {
        return useArmorSwapping;
    }

    public boolean useShieldCounter() {
        return useShieldCounter;
    }

    public boolean isPrimaryWeapon(Item item) {
        return primaryWeapons.contains(item);
    }

    public boolean isSwapWeapon(Item item) {
        return swapWeapons.contains(item);
    }

    public boolean isMobilityItem(Item item) {
        return mobilityItems.contains(item);
    }

    public boolean isArmorItem(Item item) {
        return armorItems.contains(item);
    }
}

