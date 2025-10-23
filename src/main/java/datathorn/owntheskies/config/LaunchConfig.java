package datathorn.owntheskies.config;

/**
 * Configuration for elytra launch distance control.
 * Allows players to adjust how far they travel horizontally during launch.
 */
public class LaunchConfig {
    private static LaunchMode currentMode = LaunchMode.MEDIUM;

    public enum LaunchMode {
        VERTICAL("Vertical Only", 0.0f, 0),      // Straight up and down
        SHORT("Short Distance", 0.3f, 1),        // Minimal horizontal movement
        MEDIUM("Medium Distance", 0.6f, 2),       // Balanced
        LONG("Long Distance", 0.9f, 3),          // Maximum glide distance
        CUSTOM("Custom", 0.5f, -1);              // User-defined

        private final String displayName;
        private final float glideModifier;       // How much to tilt forward (0 = vertical, 1 = horizontal)
        private final int index;

        LaunchMode(String displayName, float glideModifier, int index) {
            this.displayName = displayName;
            this.glideModifier = glideModifier;
            this.index = index;
        }

        public String getDisplayName() {
            return displayName;
        }

        public float getGlideModifier() {
            return glideModifier;
        }

        public int getIndex() {
            return index;
        }
    }

    public static LaunchMode getCurrentMode() {
        return currentMode;
    }

    public static void setMode(LaunchMode mode) {
        currentMode = mode;
    }

    public static void nextMode() {
        LaunchMode[] modes = LaunchMode.values();
        int currentIndex = -1;

        // Find current mode index
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }

        // Move to next mode (skip CUSTOM)
        do {
            currentIndex = (currentIndex + 1) % modes.length;
        } while (modes[currentIndex] == LaunchMode.CUSTOM);

        currentMode = modes[currentIndex];
    }

    public static void previousMode() {
        LaunchMode[] modes = LaunchMode.values();
        int currentIndex = -1;

        // Find current mode index
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }

        // Move to previous mode (skip CUSTOM)
        do {
            currentIndex = (currentIndex - 1 + modes.length) % modes.length;
        } while (modes[currentIndex] == LaunchMode.CUSTOM);

        currentMode = modes[currentIndex];
    }

    /**
     * Get the forward pitch adjustment based on current mode.
     * This determines how much the player tilts forward during ascent.
     *
     * @return Pitch adjustment in degrees (0 = straight up, positive = tilted forward)
     */
    public static float getForwardPitchAdjustment() {
        float modifier = currentMode.getGlideModifier();
        // Convert modifier to pitch: 0.0 = 0°, 1.0 = 45°
        return modifier * 45.0f;
    }

    /**
     * Get a description of what the current mode does
     */
    public static String getModeDescription() {
        switch (currentMode) {
            case VERTICAL:
                return "Straight up and down - lands at starting position";
            case SHORT:
                return "Minimal glide - lands ~10-15 blocks away";
            case MEDIUM:
                return "Balanced glide - lands ~20-30 blocks away";
            case LONG:
                return "Maximum glide - lands ~40-50 blocks away";
            default:
                return currentMode.getDisplayName();
        }
    }
}

