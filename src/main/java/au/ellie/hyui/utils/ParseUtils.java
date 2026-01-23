package au.ellie.hyui.utils;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.HyUIPluginLogger;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utility methods for safe parsing of attribute values with optional debug logging.
 *
 * <p>These methods maintain tolerant parsing behavior (invalid values are ignored)
 * while providing visibility through debug logging when {@link HyUIPluginLogger#DEBUG_ENABLED}
 * is set to true.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple parsing with debug context
 * ParseUtils.parseInt(element.attr("width"), "width attribute")
 *     .ifPresent(w -> anchor.setWidth(w));
 *
 * // Or with default fallback
 * int width = ParseUtils.parseIntOrDefault(element.attr("width"), 100, "width attribute");
 * }</pre>
 */
public final class ParseUtils {

    private ParseUtils() {
        // Utility class
    }

    /**
     * Parse a string to an integer, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @param context Description of what's being parsed (for debug messages)
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Integer> parseInt(String value, String context) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            if (HyUIPluginLogger.DEBUG_ENABLED) {
                HyUIPlugin.getLog().logDebug("Failed to parse integer for " + context + ": '" + value + "'");
            }
            return Optional.empty();
        }
    }

    /**
     * Parse a string to an integer with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @param context      Description of what's being parsed (for debug messages)
     * @return The parsed value or defaultValue if parsing failed
     */
    public static int parseIntOrDefault(String value, int defaultValue, String context) {
        return parseInt(value, context).orElse(defaultValue);
    }

    /**
     * Parse a string to an integer and apply it to a consumer if successful.
     * This is useful for builder patterns where you only want to call a setter on success.
     *
     * @param value    The string value to parse
     * @param context  Description of what's being parsed (for debug messages)
     * @param consumer Consumer to apply the parsed value to
     * @return true if parsing succeeded and consumer was called, false otherwise
     */
    public static boolean parseIntAndApply(String value, String context, Consumer<Integer> consumer) {
        return parseInt(value, context).map(v -> {
            consumer.accept(v);
            return true;
        }).orElse(false);
    }

    /**
     * Parse a string to a long, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @param context Description of what's being parsed (for debug messages)
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Long> parseLong(String value, String context) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            if (HyUIPluginLogger.DEBUG_ENABLED) {
                HyUIPlugin.getLog().logDebug("Failed to parse long for " + context + ": '" + value + "'");
            }
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a long with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @param context      Description of what's being parsed (for debug messages)
     * @return The parsed value or defaultValue if parsing failed
     */
    public static long parseLongOrDefault(String value, long defaultValue, String context) {
        return parseLong(value, context).orElse(defaultValue);
    }

    /**
     * Parse a string to a double, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @param context Description of what's being parsed (for debug messages)
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Double> parseDouble(String value, String context) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(value.trim()));
        } catch (NumberFormatException e) {
            if (HyUIPluginLogger.DEBUG_ENABLED) {
                HyUIPlugin.getLog().logDebug("Failed to parse double for " + context + ": '" + value + "'");
            }
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a double with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @param context      Description of what's being parsed (for debug messages)
     * @return The parsed value or defaultValue if parsing failed
     */
    public static double parseDoubleOrDefault(String value, double defaultValue, String context) {
        return parseDouble(value, context).orElse(defaultValue);
    }

    /**
     * Parse a string to a float, logging parse failures in debug mode.
     *
     * @param value   The string value to parse
     * @param context Description of what's being parsed (for debug messages)
     * @return Optional containing the parsed value, or empty if parsing failed
     */
    public static Optional<Float> parseFloat(String value, String context) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Float.parseFloat(value.trim()));
        } catch (NumberFormatException e) {
            if (HyUIPluginLogger.DEBUG_ENABLED) {
                HyUIPlugin.getLog().logDebug("Failed to parse float for " + context + ": '" + value + "'");
            }
            return Optional.empty();
        }
    }

    /**
     * Parse a string to a float with a default value.
     *
     * @param value        The string value to parse
     * @param defaultValue Value to return if parsing fails
     * @param context      Description of what's being parsed (for debug messages)
     * @return The parsed value or defaultValue if parsing failed
     */
    public static float parseFloatOrDefault(String value, float defaultValue, String context) {
        return parseFloat(value, context).orElse(defaultValue);
    }

    /**
     * Parse a string to an enum value, logging parse failures in debug mode.
     *
     * @param value     The string value to parse
     * @param enumClass The enum class to parse into
     * @param context   Description of what's being parsed (for debug messages)
     * @param <E>       The enum type
     * @return Optional containing the parsed enum value, or empty if parsing failed
     */
    public static <E extends Enum<E>> Optional<E> parseEnum(String value, Class<E> enumClass, String context) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumClass, value.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            if (HyUIPluginLogger.DEBUG_ENABLED) {
                HyUIPlugin.getLog().logDebug("Failed to parse enum " + enumClass.getSimpleName()
                    + " for " + context + ": '" + value + "'");
            }
            return Optional.empty();
        }
    }

    /**
     * Parse a string to an enum value with a default.
     *
     * @param value        The string value to parse
     * @param enumClass    The enum class to parse into
     * @param defaultValue Value to return if parsing fails
     * @param context      Description of what's being parsed (for debug messages)
     * @param <E>          The enum type
     * @return The parsed enum value or defaultValue if parsing failed
     */
    public static <E extends Enum<E>> E parseEnumOrDefault(String value, Class<E> enumClass,
            E defaultValue, String context) {
        return parseEnum(value, enumClass, context).orElse(defaultValue);
    }
}
