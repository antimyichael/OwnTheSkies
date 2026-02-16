package datathorn.owntheskies.client;

import datathorn.owntheskies.combat.AutoSwitcher;
import datathorn.owntheskies.combat.ElytraLauncher;
import datathorn.owntheskies.combat.WeaponSwapper;
import datathorn.owntheskies.combat.AutoAttack;
import datathorn.owntheskies.config.ProfileManager;
import datathorn.owntheskies.config.LaunchConfig;
import datathorn.owntheskies.inventory.HotbarOrganizer;
import datathorn.owntheskies.render.LandingIndicator;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    private static KeyBinding toggleAutoSwitchKey;
    private static KeyBinding nextProfileKey;
    private static KeyBinding previousProfileKey;
    private static KeyBinding launchKey;
    private static KeyBinding toggleWeaponSwapKey;
    private static KeyBinding toggleArmorSwapKey;
    private static KeyBinding toggleAutoAttackKey;
    private static KeyBinding organizeHotbarKey;
    private static KeyBinding toggleLandingIndicatorKey;
    private static KeyBinding togglePreLaunchIndicatorKey;
    private static KeyBinding nextLaunchModeKey;
    private static KeyBinding previousLaunchModeKey;

    public static void register() {
        // Category for our keybindings (string-based)
        String category = "category.owntheskies.combat";

        toggleAutoSwitchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.toggle_auto_switch",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            category
        ));

        nextProfileKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.next_profile",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            category
        ));

        previousProfileKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.previous_profile",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            category
        ));

        launchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.launch",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            category
        ));

        toggleWeaponSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.toggle_weapon_swap",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            category
        ));

        toggleArmorSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.toggle_armor_swap",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            category
        ));

        toggleAutoAttackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.toggle_auto_attack",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            category
        ));

        organizeHotbarKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.organize_hotbar",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            category
        ));

        toggleLandingIndicatorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.toggle_landing_indicator",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            category
        ));

        togglePreLaunchIndicatorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.toggle_prelaunch_indicator",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            category
        ));

        nextLaunchModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.next_launch_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL, // + key
            category
        ));

        previousLaunchModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.owntheskies.previous_launch_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_MINUS, // - key
            category
        ));

        // Register tick event to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            // Toggle auto-switch
            if (toggleAutoSwitchKey.wasPressed()) {
                AutoSwitcher.toggleAutoSwitch();
                String status = AutoSwitcher.isAutoSwitchEnabled() ? "enabled" : "disabled";
                client.player.sendMessage(
                    Text.literal("Auto-Switch: " + status),
                    true // actionBar
                );
            }

            // Next profile
            if (nextProfileKey.wasPressed()) {
                ProfileManager.nextProfile();
                client.player.sendMessage(
                    Text.literal("Profile: " + ProfileManager.getCurrentProfile().getName()),
                    true
                );
            }

            // Previous profile
            if (previousProfileKey.wasPressed()) {
                ProfileManager.previousProfile();
                client.player.sendMessage(
                    Text.literal("Profile: " + ProfileManager.getCurrentProfile().getName()),
                    true
                );
            }

            // Launch elytra sequence
            if (launchKey.wasPressed()) {
                if (!ElytraLauncher.isLaunching()) {
                    ElytraLauncher.startLaunch();
                    client.player.sendMessage(
                        Text.literal("Launching..."),
                        true
                    );
                }
            }

            // Toggle weapon swapping (sword/axe to mace technique)
            if (toggleWeaponSwapKey.wasPressed()) {
                WeaponSwapper.toggleWeaponSwap();
                String status = WeaponSwapper.isWeaponSwapEnabled() ? "enabled" : "disabled";
                client.player.sendMessage(
                    Text.literal("Weapon Swap: " + status),
                    true
                );
            }

            // Toggle armor swapping (elytra to chestplate technique)
            if (toggleArmorSwapKey.wasPressed()) {
                WeaponSwapper.toggleArmorSwap();
                String status = WeaponSwapper.isArmorSwapEnabled() ? "enabled" : "disabled";
                client.player.sendMessage(
                    Text.literal("Armor Swap: " + status),
                    true
                );
            }

            // Toggle auto-attack (automatic swinging to prevent fall damage)
            if (toggleAutoAttackKey.wasPressed()) {
                AutoAttack.toggleAutoAttack();
                String status = AutoAttack.isAutoAttackEnabled() ? "enabled" : "disabled";
                client.player.sendMessage(
                    Text.literal("Auto-Attack: " + status),
                    true
                );
            }

            // Organize hotbar automatically
            if (organizeHotbarKey.wasPressed()) {
                HotbarOrganizer.organizeHotbar(client);
                // Confirmation message is sent by the organizer itself
            }

            // Toggle landing indicator
            if (toggleLandingIndicatorKey.wasPressed()) {
                LandingIndicator.toggle();
                String status = LandingIndicator.isEnabled() ? "enabled" : "disabled";
                client.player.sendMessage(
                    Text.literal("Landing Indicator: " + status),
                    true
                );
            }

            // Toggle pre-launch indicator mode
            if (togglePreLaunchIndicatorKey.wasPressed()) {
                LandingIndicator.togglePreLaunchMode();
                String status = LandingIndicator.isPreLaunchMode() ? "enabled" : "disabled";
                client.player.sendMessage(
                    Text.literal("§6Pre-Launch Indicator: §e" + status),
                    true
                );
                if (LandingIndicator.isPreLaunchMode()) {
                    client.player.sendMessage(
                        Text.literal("§7Landing indicator will show continuously"),
                        true
                    );
                }
            }

            // Next launch mode (increase distance)
            if (nextLaunchModeKey.wasPressed()) {
                LaunchConfig.nextMode();
                client.player.sendMessage(
                    Text.literal("§6Launch Mode: §e" + LaunchConfig.getCurrentMode().getDisplayName()),
                    true
                );
                client.player.sendMessage(
                    Text.literal("§7" + LaunchConfig.getModeDescription()),
                    true
                );
            }

            // Previous launch mode (decrease distance)
            if (previousLaunchModeKey.wasPressed()) {
                LaunchConfig.previousMode();
                client.player.sendMessage(
                    Text.literal("§6Launch Mode: §e" + LaunchConfig.getCurrentMode().getDisplayName()),
                    true
                );
                client.player.sendMessage(
                    Text.literal("§7" + LaunchConfig.getModeDescription()),
                    true
                );
            }
        });
    }
}

