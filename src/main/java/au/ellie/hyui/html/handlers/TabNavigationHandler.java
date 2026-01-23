package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.TabNavigationBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Handler for tab navigation elements in HYUIML.
 *
 * Supports:
 * - &lt;nav class="tabs"&gt; or &lt;nav class="tab-navigation"&gt;
 * - &lt;div class="tabs"&gt; or &lt;div class="tab-navigation"&gt;
 *
 * Structure:
 * <pre>
 * &lt;nav class="tabs"&gt;
 *     &lt;button data-tab="tab1"&gt;Tab 1&lt;/button&gt;
 *     &lt;button data-tab="tab2" class="active"&gt;Tab 2&lt;/button&gt;
 *     &lt;button data-tab="tab3"&gt;Tab 3&lt;/button&gt;
 * &lt;/nav&gt;
 * </pre>
 *
 * Or simplified:
 * <pre>
 * &lt;nav class="tabs" data-tabs="inventory:Inventory,stats:Statistics,settings:Settings" data-selected="inventory"&gt;
 * &lt;/nav&gt;
 * </pre>
 */
public class TabNavigationHandler implements TagHandler {

    @Override
    public boolean canHandle(Element element) {
        String tagName = element.tagName().toLowerCase();
        return (tagName.equals("nav") || tagName.equals("div")) &&
               (element.hasClass("tabs") || element.hasClass("tab-navigation"));
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        TabNavigationBuilder builder = TabNavigationBuilder.tabNavigation();

        applyCommonAttributes(builder, element);

        // Check for simplified data-tabs attribute
        if (element.hasAttr("data-tabs")) {
            String tabsAttr = element.attr("data-tabs");
            for (String tabDef : tabsAttr.split(",")) {
                String[] parts = tabDef.trim().split(":", 2);
                if (parts.length == 2) {
                    builder.addTab(parts[0].trim(), parts[1].trim());
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    // Use the same value for id and label
                    builder.addTab(parts[0].trim(), parts[0].trim());
                }
            }
        }

        // Set selected tab from data-selected attribute
        if (element.hasAttr("data-selected")) {
            builder.withSelectedTab(element.attr("data-selected"));
        }

        // Parse child button elements for tab definitions
        Elements buttons = element.select("> button, > a");
        for (Element button : buttons) {
            String tabId = button.hasAttr("data-tab") ? button.attr("data-tab") : button.attr("id");
            String label = button.text().trim();

            if (tabId != null && !tabId.isEmpty() && !label.isEmpty()) {
                builder.addTab(tabId, label);

                // Check if this tab is marked as active/selected
                if (button.hasClass("active") || button.hasClass("selected")) {
                    builder.withSelectedTab(tabId);
                }
            }
        }

        // Apply tab spacing if specified
        if (element.hasAttr("data-tab-spacing")) {
            ParseUtils.parseInt(element.attr("data-tab-spacing"))
                    .ifPresent(builder::withTabSpacing);
        }

        return builder;
    }
}
