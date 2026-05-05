package axthrix.world.util;

import arc.util.Strings;
import axthrix.AxthrixLoader;

public class TempUnit {
    // Physical constants
    public static final float absZeroC = -273.15f;
    public static final float absZeroF = -459.67f;
    public static final float neutralF = 32f;
    public static final float neutralK = 273.15f;

    public static String format(float value, float limit) {
        float safeLimit = Math.max(limit, 1f);

        // 1. Determine if we should use Scaling Mode
        // Only scale if Caps are ON AND the block's capacity is deeper than Absolute Zero
        boolean shouldScale = AxthrixLoader.followRealCaps && safeLimit > 273.15f;

        // 2. Purple color triggers ONLY in Raw Logic mode when passing Absolute Zero (-273.15C)
        String color = (!AxthrixLoader.followRealCaps && value < absZeroC) ? "[#bf92f9]" : "";

        switch (AxthrixLoader.tempUnit) {
            case 1: // Fahrenheit
                float f;
                if (value >= 0 || !shouldScale) {
                    f = value * 1.8f + 32;
                    // Hard clamp if caps are on but we aren't scaling
                    if (AxthrixLoader.followRealCaps) f = Math.max(absZeroF, f);
                } else {
                    // Scaling Mode: Map [0 to -Limit] -> [32 to -459.67]
                    float ratio = Math.min(Math.abs(value) / safeLimit, 1f);
                    f = neutralF + (absZeroF - neutralF) * ratio;
                }
                return color + Math.round(f) + "°F";

            case 2: // Kelvin
                float k;
                if (value >= 0 || !shouldScale) {
                    k = value + 273.15f;
                    // Hard clamp if caps are on but we aren't scaling
                    if (AxthrixLoader.followRealCaps) k = Math.max(0f, k);
                } else {
                    // Scaling Mode: Map [0 to -Limit] -> [273.15 to 0]
                    float ratio = Math.min(Math.abs(value) / safeLimit, 1f);
                    k = neutralK * (1f - ratio);
                }
                return color + Math.round(k) + "K";

            default: // Celsius
                float c;
                if (value >= 0 || !shouldScale) {
                    c = value;
                    // Hard clamp if caps are on but we aren't scaling
                    if (AxthrixLoader.followRealCaps) c = Math.max(absZeroC, c);
                } else {
                    // Scaling Mode: Map [0 to -Limit] -> [0 to -273.15]
                    float ratio = Math.min(Math.abs(value) / safeLimit, 1f);
                    c = absZeroC * ratio;
                }
                return color + Math.round(c) + "°C";
        }
    }

    /** Overload for simpler calls (Defaults to 100 limit) */
    public static String format(float value) {
        return format(value, 100f);
    }

    /** Rates of change (Per Second) - Always raw magnitude */
    public static String formatDelta(float value) {
        switch (AxthrixLoader.tempUnit) {
            case 1: return Strings.fixed(value * 1.8f, 1) + "°F";
            case 2: return Strings.fixed(value, 1) + "K";
            default: return Strings.fixed(value, 1) + "°C";
        }
    }
}
