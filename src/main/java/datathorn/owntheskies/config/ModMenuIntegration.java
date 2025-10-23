package datathorn.owntheskies.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import datathorn.owntheskies.combat.AutoSwitcher;
import datathorn.owntheskies.combat.ElytraLauncher;
import datathorn.owntheskies.combat.WeaponSwapper;
import datathorn.owntheskies.combat.AutoAttack;
import datathorn.owntheskies.render.LandingIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new OwnTheSkiesConfigScreen(parent);
    }

    public static class OwnTheSkiesConfigScreen extends Screen {
        private final Screen parent;
        private final ModConfig config;

        public OwnTheSkiesConfigScreen(Screen parent) {
            super(Text.literal("OwnTheSkies Configuration"));
            this.parent = parent;
            this.config = ModConfig.getInstance();
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int startY = 35;
            int buttonWidth = 200;
            int buttonHeight = 20;
            int spacing = 24;
            int currentY = startY;

            // ===== ELYTRA & LAUNCH SETTINGS =====
            currentY += spacing;

            // Firework Count Slider
            this.addDrawableChild(new SliderWidget(
                centerX - buttonWidth / 2,
                currentY,
                buttonWidth,
                buttonHeight,
                Text.literal("Fireworks per Launch: " + config.maxFireworks),
                (config.maxFireworks - 1) / 9.0
            ) {
                @Override
                protected void updateMessage() {
                    int fireworkCount = (int) Math.round(this.value * 9) + 1;
                    this.setMessage(Text.literal("Fireworks per Launch: " + fireworkCount));
                }

                @Override
                protected void applyValue() {
                    int fireworkCount = (int) Math.round(this.value * 9) + 1;
                    config.maxFireworks = fireworkCount;
                    ElytraLauncher.setMaxFireworks(fireworkCount);
                }
            });
            currentY += spacing + 5;

            // ===== COMBAT FEATURES =====
            currentY += spacing - 5;

            // Auto Switch Toggle (Keybind: V)
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Switch: " + (config.autoSwitchEnabled ? "§aON" : "§cOFF")),
                button -> {
                    config.autoSwitchEnabled = !config.autoSwitchEnabled;
                    AutoSwitcher.setAutoSwitchEnabled(config.autoSwitchEnabled);
                    button.setMessage(Text.literal("Auto Switch: " + (config.autoSwitchEnabled ? "§aON" : "§cOFF")));
                })
                .dimensions(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build()
            );
            currentY += spacing;

            // Weapon Swap Toggle (Keybind: B)
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Weapon Swap: " + (config.weaponSwapEnabled ? "§aON" : "§cOFF")),
                button -> {
                    config.weaponSwapEnabled = !config.weaponSwapEnabled;
                    WeaponSwapper.setWeaponSwapEnabled(config.weaponSwapEnabled);
                    button.setMessage(Text.literal("Weapon Swap: " + (config.weaponSwapEnabled ? "§aON" : "§cOFF")));
                })
                .dimensions(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build()
            );
            currentY += spacing;

            // Armor Swap Toggle (Keybind: N)
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Armor Swap: " + (config.armorSwapEnabled ? "§aON" : "§cOFF")),
                button -> {
                    config.armorSwapEnabled = !config.armorSwapEnabled;
                    WeaponSwapper.setArmorSwapEnabled(config.armorSwapEnabled);
                    button.setMessage(Text.literal("Armor Swap: " + (config.armorSwapEnabled ? "§aON" : "§cOFF")));
                })
                .dimensions(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build()
            );
            currentY += spacing;

            // Auto Attack Toggle (Keybind: M)
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Attack: " + (config.autoAttackEnabled ? "§aON" : "§cOFF")),
                button -> {
                    config.autoAttackEnabled = !config.autoAttackEnabled;
                    AutoAttack.setAutoAttackEnabled(config.autoAttackEnabled);
                    button.setMessage(Text.literal("Auto Attack: " + (config.autoAttackEnabled ? "§aON" : "§cOFF")));
                })
                .dimensions(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build()
            );
            currentY += spacing + 5;

            // ===== VISUAL FEATURES =====
            currentY += spacing - 5;

            // Landing Indicator Toggle (Keybind: L)
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Landing Indicator: " + (config.landingIndicatorEnabled ? "§aON" : "§cOFF")),
                button -> {
                    config.landingIndicatorEnabled = !config.landingIndicatorEnabled;
                    LandingIndicator.setEnabled(config.landingIndicatorEnabled);
                    button.setMessage(Text.literal("Landing Indicator: " + (config.landingIndicatorEnabled ? "§aON" : "§cOFF")));
                })
                .dimensions(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build()
            );

            // Done button
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> {
                    config.save();
                    this.close();
                })
                .dimensions(centerX - buttonWidth / 2, this.height - 30, buttonWidth, buttonHeight)
                .build()
            );
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            int centerX = this.width / 2;

            // Main title
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 15, 0xFFFFFF);

            // Section headers with colors
            int startY = 35;
            int spacing = 24;

            // Elytra & Launch section
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§6§l━━━ Elytra & Launch ━━━"),
                centerX, startY + spacing, 0xFFAA00);

            // Combat section (after firework slider and spacing)
            int combatY = startY + spacing * 3 + 10;
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§c§l━━━ Combat Features ━━━"),
                centerX, combatY, 0xFF5555);

            // Visual section
            int visualY = combatY + spacing * 5 + 5;
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§b§l━━━ Visual Features ━━━"),
                centerX, visualY, 0x55FFFF);

            // Footer with keybind info
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Keybinds can be changed in Controls settings"),
                centerX, this.height - 45, 0x888888);
        }

        @Override
        public void close() {
            if (this.client != null) {
                this.client.setScreen(parent);
            }
        }
    }
}

