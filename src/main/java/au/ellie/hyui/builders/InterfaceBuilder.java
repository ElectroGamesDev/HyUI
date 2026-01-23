package au.ellie.hyui.builders;

import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class InterfaceBuilder<T extends InterfaceBuilder<T>> {
    protected final Map<String, UIElementBuilder<?>> elementRegistry = new LinkedHashMap<>();
    protected final List<Consumer<UICommandBuilder>> editCallbacks = new ArrayList<>();
    protected String uiFile;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T fromFile(String uiFile) {
        this.uiFile = uiFile;
        return self();
    }

    public T fromHtml(String html) {
        new HtmlParser().parseToInterface(this, html);
        return self();
    }

    /**
     * Loads and processes an HTML template with variable substitution.
     *
     * <p>Example usage:</p>
     * <pre>
     * builder.fromTemplate("""
     *     &lt;p&gt;Hello, {{$playerName}}!&lt;/p&gt;
     *     &lt;p&gt;Score: {{$score|0}}&lt;/p&gt;
     *     """, new TemplateProcessor()
     *         .setVariable("playerName", player.getName())
     *         .setVariable("score", 1500));
     * </pre>
     *
     * @param html     The HTML template with {{$variable}} placeholders
     * @param template The template processor with variables set
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, TemplateProcessor template) {
        HtmlParser parser = new HtmlParser();
        parser.setTemplateProcessor(template);
        parser.parseToInterface(this, html);
        return self();
    }

    /**
     * Loads and processes an HTML template with a map of variables.
     *
     * @param html      The HTML template with {{$variable}} placeholders
     * @param variables Map of variable names to values
     * @return This builder instance for method chaining
     */
    public T fromTemplate(String html, Map<String, ?> variables) {
        return fromTemplate(html, new TemplateProcessor().setVariables(variables));
    }

    public T addElement(UIElementBuilder<?> element) {
        element.inside("#HyUIRoot");
        registerElement(element);
        return self();
    }

    protected void registerElement(UIElementBuilder<?> element) {
        if (element.getId() != null) {
            this.elementRegistry.put(element.getId(), element);
        }
        for (UIElementBuilder<?> child : element.children) {
            registerElement(child);
        }
    }

    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        UIElementBuilder<?> builder = elementRegistry.get(id);
        if (builder != null && clazz.isInstance(builder)) {
            return Optional.of(clazz.cast(builder));
        }
        return Optional.empty();
    }

    /**
     * Adds an event listener to an element by its ID.
     * Note: This only works for elements created using the builder API or via HYUIML (.fromHtml).
     * It does not work for elements defined in a .ui file loaded via .fromFile.
     *
     * @param id         The ID of the element.
     * @param type       The type of event to listen for.
     * @param valueClass The class of the value associated with the event.
     * @param callback   The callback to execute when the event occurs.
     * @param <V>        The type of the value.
     * @return This builder instance for method chaining.
     */
    public <V> T addEventListener(String id, CustomUIEventBindingType type, Class<V> valueClass, Consumer<V> callback) {
        UIElementBuilder<?> element = elementRegistry.get(id);
        if (element == null) {
            throw new IllegalArgumentException("No element found with ID '" + id + "'.");
        }
        element.addEventListener(type, valueClass, callback);
        return self();
    }

    public T addEventListener(String id, CustomUIEventBindingType type, Consumer<Object> callback) {
        return addEventListener(id, type, Object.class, callback);
    }

    /**
     * Adds an event listener with access to the UI context.
     * Note: This only works for elements created using the builder API or via HYUIML (.fromHtml).
     *
     * @param id         The ID of the element.
     * @param type       The type of event to listen for.
     * @param valueClass The class of the value associated with the event.
     * @param callback   The callback to execute when the event occurs.
     * @param <V>        The type of the value.
     * @return This builder instance for method chaining.
     */
    public <V> T addEventListener(String id, CustomUIEventBindingType type, Class<V> valueClass, BiConsumer<V, UIContext> callback) {
        UIElementBuilder<?> element = elementRegistry.get(id);
        if (element == null) {
            throw new IllegalArgumentException("No element found with ID '" + id + "'.");
        }
        element.addEventListenerWithContext(type, valueClass, callback);
        return self();
    }

    public T addEventListener(String id, CustomUIEventBindingType type, BiConsumer<Object, UIContext> callback) {
        return addEventListener(id, type, Object.class, callback);
    }

    public T editElement(Consumer<UICommandBuilder> callback) {
        this.editCallbacks.add(callback);
        return self();
    }

    /**
     * Retrieves the top-level elements of the interface, which are elements with the parent selector "#HyUIRoot".
     * @return A list of top-level UIElementBuilder instances for use in other builders.
     */
    public List<UIElementBuilder<?>> getTopLevelElements() {
        List<UIElementBuilder<?>> topLevel = new ArrayList<>();
        for (UIElementBuilder<?> element : elementRegistry.values()) {
            if ("#HyUIRoot".equals(element.parentSelector)) {
                topLevel.add(element);
            }
        }
        return topLevel;
    }

    /**
     * Get all elements in the element registry for this builder.
     * 
     * @return a list of all elements, irrespective of top-level.
     */
    public List<UIElementBuilder<?>> getElements() {
        return elementRegistry.values().stream().toList();        
    }
}
