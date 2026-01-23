package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.elements.UIElements;
import au.ellie.hyui.theme.Theme;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating tab navigation UI elements.
 * Creates a horizontal row of tab buttons for navigation between different content sections.
 *
 * Example usage:
 * <pre>
 * TabNavigationBuilder.tabNavigation()
 *     .withId("main-tabs")
 *     .addTab("inventory", "Inventory")
 *     .addTab("stats", "Statistics")
 *     .addTab("settings", "Settings")
 *     .withSelectedTab("inventory")
 * </pre>
 */
public class TabNavigationBuilder extends UIElementBuilder<TabNavigationBuilder>
        implements LayoutModeSupported<TabNavigationBuilder>, BackgroundSupported<TabNavigationBuilder> {

    public record Tab(String id, String label, boolean selected) {
        public Tab withSelected(boolean selected) {
            return new Tab(id, label, selected);
        }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private String selectedTabId;
    private String layoutMode = "Left"; // Horizontal by default
    private HyUIPatchStyle background;
    private HyUIStyle selectedTabStyle;
    private HyUIStyle unselectedTabStyle;
    private int tabSpacing = 0;

    public TabNavigationBuilder() {
        super(UIElements.GROUP, "Group");
    }

    public TabNavigationBuilder(Theme theme) {
        super(theme, UIElements.GROUP, "Group");
    }

    /**
     * Factory method to create a new TabNavigationBuilder.
     */
    public static TabNavigationBuilder tabNavigation() {
        return new TabNavigationBuilder();
    }

    /**
     * Adds a tab to the navigation.
     *
     * @param id    Unique identifier for the tab (used in events)
     * @param label Display text for the tab button
     */
    public TabNavigationBuilder addTab(String id, String label) {
        tabs.add(new Tab(id, label, false));
        return this;
    }

    /**
     * Sets which tab is currently selected.
     */
    public TabNavigationBuilder withSelectedTab(String tabId) {
        this.selectedTabId = tabId;
        return this;
    }

    /**
     * Sets the style for selected tabs.
     */
    public TabNavigationBuilder withSelectedTabStyle(HyUIStyle style) {
        this.selectedTabStyle = style;
        return this;
    }

    /**
     * Sets the style for unselected tabs.
     */
    public TabNavigationBuilder withUnselectedTabStyle(HyUIStyle style) {
        this.unselectedTabStyle = style;
        return this;
    }

    /**
     * Sets spacing between tabs (in pixels).
     */
    public TabNavigationBuilder withTabSpacing(int spacing) {
        this.tabSpacing = spacing;
        return this;
    }

    /**
     * Gets all tabs.
     */
    public List<Tab> getTabs() {
        return new ArrayList<>(tabs);
    }

    /**
     * Gets the currently selected tab ID.
     */
    public String getSelectedTabId() {
        return selectedTabId;
    }

    @Override
    public TabNavigationBuilder withLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
        return this;
    }

    @Override
    public String getLayoutMode() {
        return this.layoutMode;
    }

    @Override
    public TabNavigationBuilder withBackground(HyUIPatchStyle background) {
        this.background = background;
        return this;
    }

    @Override
    public HyUIPatchStyle getBackground() {
        return this.background;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    private boolean tabButtonsCreated = false;

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        applyLayoutMode(commands, selector);
        applyBackground(commands, selector);

        // Only create tab buttons once (guard against multiple builds)
        if (tabButtonsCreated) return;
        tabButtonsCreated = true;

        // Create tab buttons as children
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            boolean isSelected = tab.id().equals(selectedTabId);

            ButtonBuilder tabButton = ButtonBuilder.textButton()
                    .withId(tab.id())
                    .withText(tab.label())
                    .withFlexWeight(1);

            // Apply selected/unselected styling
            if (isSelected && selectedTabStyle != null) {
                tabButton.withStyle(selectedTabStyle);
            } else if (!isSelected && unselectedTabStyle != null) {
                tabButton.withStyle(unselectedTabStyle);
            }

            this.addChild(tabButton);

            HyUIPlugin.getLog().logInfo("Added tab: " + tab.id() + " (selected: " + isSelected + ")");
        }
    }

    /**
     * Creates a default style for selected tabs (bold, highlighted).
     */
    public static HyUIStyle defaultSelectedStyle() {
        return new HyUIStyle()
                .setRenderBold(true)
                .setTextColor("#FFFFFF");
    }

    /**
     * Creates a default style for unselected tabs (normal, dimmed).
     */
    public static HyUIStyle defaultUnselectedStyle() {
        return new HyUIStyle()
                .setRenderBold(false)
                .setTextColor("#AAAAAA");
    }
}
