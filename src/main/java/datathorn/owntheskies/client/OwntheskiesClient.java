package datathorn.owntheskies.client;

import datathorn.owntheskies.combat.AutoSwitcher;
import datathorn.owntheskies.combat.ElytraLauncher;
import datathorn.owntheskies.combat.WeaponSwapper;
import datathorn.owntheskies.combat.AutoAttack;
import datathorn.owntheskies.render.LandingIndicator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class OwntheskiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register keybindings
        KeybindManager.register();

        // Register tick event for auto-switching logic
        ClientTickEvents.END_CLIENT_TICK.register(AutoSwitcher::tick);

        // Register tick event for elytra launcher
        ClientTickEvents.END_CLIENT_TICK.register(ElytraLauncher::tick);

        // Register tick event for weapon swapping (sword/axe to mace technique)
        ClientTickEvents.END_CLIENT_TICK.register(WeaponSwapper::tick);

        // Register tick event for automatic attacking (prevents missing swings)
        ClientTickEvents.END_CLIENT_TICK.register(AutoAttack::tick);

        // Register tick event for landing indicator prediction updates
        ClientTickEvents.END_CLIENT_TICK.register(LandingIndicator::updatePrediction);

        // Note: 3D rendering temporarily disabled due to API compatibility
        // Landing predictions are still calculated and can be accessed programmatically
    }
}
