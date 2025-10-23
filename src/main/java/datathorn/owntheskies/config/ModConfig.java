package datathorn.owntheskies.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Main configuration class for OwnTheSkies mod
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "owntheskies.json");

    private static ModConfig INSTANCE;

    // Configuration options
    public int maxFireworks = 1;
    public boolean autoSwitchEnabled = true;
    public boolean weaponSwapEnabled = true;
    public boolean armorSwapEnabled = true;
    public boolean autoAttackEnabled = true;
    public boolean landingIndicatorEnabled = true;

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load OwnTheSkies config: " + e.getMessage());
            }
        }
        return new ModConfig();
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save OwnTheSkies config: " + e.getMessage());
        }
    }
}

