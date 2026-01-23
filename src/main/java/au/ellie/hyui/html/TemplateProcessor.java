package au.ellie.hyui.html;

import au.ellie.hyui.HyUIPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocessor for HyUIML templates that supports variable interpolation and component inclusion.
 *
 * <h2>Variable Interpolation</h2>
 * Use <code>{{$variableName}}</code> syntax for variable substitution:
 * <pre>
 * &lt;p&gt;Hello, {{$playerName}}!&lt;/p&gt;
 * </pre>
 *
 * <h2>Default Values</h2>
 * Use the pipe syntax for default values:
 * <pre>
 * &lt;p&gt;Score: {{$score|0}}&lt;/p&gt;
 * </pre>
 *
 * <h2>Component Inclusion</h2>
 * Include reusable components with {@code {{&#64;component}}}:
 * <pre>
 * {{&#64;component/button:text=Click Me,id=myBtn}}
 * </pre>
 *
 * <h2>Filters</h2>
 * Apply transformations with filters:
 * <pre>
 * {{$name|upper}}     - Uppercase
 * {{$name|lower}}     - Lowercase
 * {{$value|number}}   - Format as number
 * {{$value|percent}}  - Format as percentage
 * </pre>
 */
public class TemplateProcessor {

    // Pattern for {{$variable}} or {{$variable|default}} or {{$variable|filter}}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\$([a-zA-Z_][a-zA-Z0-9_]*)(?:\\|([^}]*))?\\}\\}");

    // Pattern for {{@component/name:param1=value1,param2=value2}}
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("\\{\\{@([a-zA-Z_][a-zA-Z0-9_/]*)(?::([^}]*))?\\}\\}");

    private final Map<String, String> variables = new HashMap<>();
    private final Map<String, String> components = new HashMap<>();
    private final Map<String, Function<String, String>> filters = new HashMap<>();

    public TemplateProcessor() {
        // Register default filters
        registerFilter("upper", String::toUpperCase);
        registerFilter("lower", String::toLowerCase);
        registerFilter("trim", String::trim);
        registerFilter("capitalize", this::capitalize);
        registerFilter("number", this::formatNumber);
        registerFilter("percent", this::formatPercent);
    }

    /**
     * Sets a template variable.
     *
     * @param name  Variable name (without $)
     * @param value Variable value
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, String value) {
        variables.put(name, value);
        return this;
    }

    /**
     * Sets a template variable from any object.
     *
     * @param name  Variable name (without $)
     * @param value Variable value (will be converted to string)
     * @return This processor for chaining
     */
    public TemplateProcessor setVariable(String name, Object value) {
        variables.put(name, value != null ? value.toString() : "");
        return this;
    }

    /**
     * Sets multiple variables at once.
     *
     * @param vars Map of variable names to values
     * @return This processor for chaining
     */
    public TemplateProcessor setVariables(Map<String, ?> vars) {
        for (Map.Entry<String, ?> entry : vars.entrySet()) {
            setVariable(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Registers a reusable component template.
     *
     * @param name     Component name (e.g., "button", "card")
     * @param template Component HTML template
     * @return This processor for chaining
     */
    public TemplateProcessor registerComponent(String name, String template) {
        components.put(name, template);
        return this;
    }

    /**
     * Registers a custom filter function.
     *
     * @param name   Filter name
     * @param filter Filter function
     * @return This processor for chaining
     */
    public TemplateProcessor registerFilter(String name, Function<String, String> filter) {
        filters.put(name, filter);
        return this;
    }

    /**
     * Processes the template, substituting variables and including components.
     *
     * @param template The template string
     * @return Processed HTML string
     */
    public String process(String template) {
        String result = template;

        // Process components first (they may contain variables)
        result = processComponents(result);

        // Then process variables
        result = processVariables(result);

        return result;
    }

    private String processVariables(String template) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String filterOrDefault = matcher.group(2);

            String value = variables.getOrDefault(varName, "");

            // Apply filter or use default value
            if (filterOrDefault != null && !filterOrDefault.isEmpty()) {
                if (filters.containsKey(filterOrDefault)) {
                    // It's a filter
                    value = filters.get(filterOrDefault).apply(value);
                } else if (value.isEmpty()) {
                    // It's a default value
                    value = filterOrDefault;
                }
            }

            HyUIPlugin.getLog().logInfo("Template variable: $" + varName + " = " + value);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String processComponents(String template) {
        Matcher matcher = COMPONENT_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String componentName = matcher.group(1);
            String paramsStr = matcher.group(2);

            String componentHtml = components.get(componentName);
            if (componentHtml == null) {
                HyUIPlugin.getLog().logInfo("Unknown component: @" + componentName);
                matcher.appendReplacement(result, "<!-- Unknown component: " + componentName + " -->");
                continue;
            }

            // Parse component parameters and substitute them
            if (paramsStr != null && !paramsStr.isEmpty()) {
                Map<String, String> params = parseParams(paramsStr);
                for (Map.Entry<String, String> param : params.entrySet()) {
                    componentHtml = componentHtml.replace("{{$" + param.getKey() + "}}", param.getValue());
                }
            }

            HyUIPlugin.getLog().logInfo("Including component: @" + componentName);
            matcher.appendReplacement(result, Matcher.quoteReplacement(componentHtml));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private Map<String, String> parseParams(String paramsStr) {
        Map<String, String> params = new HashMap<>();
        for (String param : paramsStr.split(",")) {
            String[] parts = param.trim().split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0].trim(), parts[1].trim());
            }
        }
        return params;
    }

    // Default filters
    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private String formatNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            if (num == (long) num) {
                return String.format("%,d", (long) num);
            }
            return String.format("%,.2f", num);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private String formatPercent(String value) {
        try {
            double num = Double.parseDouble(value);
            return String.format("%.0f%%", num * 100);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * Creates a new TemplateProcessor with common game-related variables.
     *
     * @param playerName The player's name
     * @return A new TemplateProcessor with player variable set
     */
    public static TemplateProcessor forPlayer(String playerName) {
        return new TemplateProcessor().setVariable("playerName", playerName);
    }
}
