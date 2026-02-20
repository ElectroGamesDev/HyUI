/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.ScrollbarStyleSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.events.UIContext;
import au.ellie.hyui.events.UIEventActions;
import au.ellie.hyui.types.LayoutMode;
import au.ellie.hyui.types.ScrollbarStyle;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for DynamicPaneContainer UI elements.
 * Children of this MUST be DynamicPane elements.
 */
public class DynamicPaneContainerBuilder extends UIElementBuilder<DynamicPaneContainerBuilder>
        implements ScrollbarStyleSupported<DynamicPaneContainerBuilder> {
    private LayoutMode layoutMode;
    private String scrollbarStyleReference;
    private String scrollbarStyleDocument;
    private ScrollbarStyle scrollbarStyle;

    public DynamicPaneContainerBuilder() {
        super("DynamicPaneContainer", "#HyUIDynamicPaneContainer");
        withUiFile("Pages/Elements/DynamicPaneContainer.ui");
        withWrappingGroup(true);
    }

    public static DynamicPaneContainerBuilder dynamicPaneContainer() {
        return new DynamicPaneContainerBuilder();
    }

    /**
     * Adds a DynamicPane to this container.
     * This is a specialized addChild method that ensures type safety.
     */
    public DynamicPaneContainerBuilder addPane(DynamicPaneBuilder pane) {
        return addChild(pane);
    }

    public DynamicPaneContainerBuilder withLayoutMode(LayoutMode layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    /**
     * Adds an event listener for the Validating event.
     */
    public DynamicPaneContainerBuilder onValidating(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Validating, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Dismissing event.
     */
    public DynamicPaneContainerBuilder onDismissing(Runnable callback) {
        return addEventListener(CustomUIEventBindingType.Dismissing, Void.class, v -> callback.run());
    }

    /**
     * Adds an event listener for the Scrolled event.
     */
    public DynamicPaneContainerBuilder onScrolled(Consumer<Float> callback) {
        return addEventListener(CustomUIEventBindingType.ValueChanged, Float.class, callback);
    }

    /**
     * Adds an event listener for the Scrolled event with context.
     */
    public DynamicPaneContainerBuilder onScrolled(BiConsumer<Float, UIContext> callback) {
        return addEventListenerWithContext(CustomUIEventBindingType.ValueChanged, Float.class, callback);
    }

    @Override
    public DynamicPaneContainerBuilder withScrollbarStyle(String document, String styleReference) {
        this.scrollbarStyleDocument = document;
        this.scrollbarStyleReference = styleReference;
        this.scrollbarStyle = null;
        return this;
    }

    @Override
    public DynamicPaneContainerBuilder withScrollbarStyle(ScrollbarStyle style) {
        this.scrollbarStyle = style;
        this.scrollbarStyleDocument = null;
        this.scrollbarStyleReference = null;
        return this;
    }

    @Override
    public String getScrollbarStyleReference() {
        return scrollbarStyleReference;
    }

    @Override
    public ScrollbarStyle getScrollbarStyle() {
        return scrollbarStyle;
    }

    @Override
    public String getScrollbarStyleDocument() {
        return scrollbarStyleDocument;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected boolean isStyleWhitelist() {
        return true;
    }

    @Override
    protected Set<String> getSupportedStyleProperties() {
        return Set.of();
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyScrollbarStyle(commands, selector);

        if (layoutMode != null) {
            HyUIPlugin.getLog().logFinest("Setting LayoutMode: " + layoutMode + " for " + selector);
            commands.set(selector + ".LayoutMode", layoutMode.name());
        }

        // Register event listeners
        listeners.forEach(listener -> {
            String eventId = getEffectiveId();
            if (listener.type() == CustomUIEventBindingType.Validating) {
                HyUIPlugin.getLog().logFinest("Adding Validating event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Validating, selector,
                        EventData.of("Action", UIEventActions.VALIDATING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.Dismissing) {
                HyUIPlugin.getLog().logFinest("Adding Dismissing event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.Dismissing, selector,
                        EventData.of("Action", UIEventActions.DISMISSING)
                            .append("Target", eventId), false);
            } else if (listener.type() == CustomUIEventBindingType.ValueChanged) {
                // Scrolled event uses ValueChanged type
                HyUIPlugin.getLog().logFinest("Adding Scrolled event binding for " + selector);
                events.addEventBinding(CustomUIEventBindingType.ValueChanged, selector,
                        EventData.of("Action", UIEventActions.SCROLLED)
                            .append("Target", eventId), false);
            }
        });
    }
}
