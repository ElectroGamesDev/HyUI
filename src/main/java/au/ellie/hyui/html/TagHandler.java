package au.ellie.hyui.html;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.elements.BackgroundSupported;
import au.ellie.hyui.elements.LayoutModeSupported;
import com.hypixel.hytale.server.core.Message;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for handling a specific HTML tag and converting it to a HyUI builder.
 */
public interface TagHandler {
    /**
     * Checks if this handler can handle the given element.
     *
     * @param element The Jsoup element to check.
     * @return true if this handler can process the element, false otherwise.
     */
    boolean canHandle(Element element);

    /**
     * Handles the conversion of the HTML element to a UIElementBuilder.
     *
     * @param element The Jsoup element to convert.
     * @param parser  The parser instance for recursive calls if needed.
     * @return A UIElementBuilder representing the element, or null if it should be ignored.
     */
    UIElementBuilder<?> handle(Element element, HtmlParser parser);

    /**
     * Applies common attributes like id, style, data-*, etc. to the builder.
     *
     * @param builder The builder to apply attributes to.
     * @param element The HTML element containing the attributes.
     */
    default void applyCommonAttributes(UIElementBuilder<?> builder, Element element) {
        if (element.hasAttr("id")) {
            builder.withId(element.attr("id"));
        }

        if (element.hasAttr("data-hyui-tooltiptext")) {
            builder.withTooltipTextSpan(Message.raw(element.attr("data-hyui-tooltiptext")));
        }

        if (element.hasAttr("data-hyui-flexweight")) {
            try {
                builder.withFlexWeight(Integer.parseInt(element.attr("data-hyui-flexweight")));
            } catch (NumberFormatException ignored) {}
        }

        if (element.hasAttr("style")) {
            Map<String, String> styles = parseStyleAttribute(element.attr("style"));
            applyStyles(builder, styles);
        }

        if (element.tagName().equalsIgnoreCase("img")) {
            HyUIAnchor anchor = builder.getAnchor();
            if (anchor == null) {
                anchor = new HyUIAnchor();
            }
            boolean hasImgAttr = false;
            if (element.hasAttr("width")) {
                try {
                    anchor.setWidth(Integer.parseInt(element.attr("width")));
                    hasImgAttr = true;
                } catch (NumberFormatException ignored) {}
            }
            if (element.hasAttr("height")) {
                try {
                    anchor.setHeight(Integer.parseInt(element.attr("height")));
                    hasImgAttr = true;
                } catch (NumberFormatException ignored) {}
            }
            if (hasImgAttr) {
                builder.withAnchor(anchor);
            }
        }
    }

    private Map<String, String> parseStyleAttribute(String styleAttr) {
        Map<String, String> styles = new HashMap<>();
        String[] declarations = styleAttr.split(";");
        for (String declaration : declarations) {
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2) {
                styles.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        return styles;
    }

    private void applyStyles(UIElementBuilder<?> builder, Map<String, String> styles) {
        HyUIStyle hyStyle = new HyUIStyle();
        HyUIAnchor anchor = new HyUIAnchor();
        HyUIPadding padding = new HyUIPadding();
        boolean hasStyle = false;
        boolean hasAnchor = false;
        boolean hasPadding = false;

        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "color":
                    hyStyle.setTextColor(value);
                    hasStyle = true;
                    break;
                case "font-size":
                    hyStyle.setFontSize(value);
                    hasStyle = true;
                    break;
                case "font-weight":
                    if (value.equalsIgnoreCase("bold")) {
                        hyStyle.setRenderBold(true);
                        hasStyle = true;
                    }
                    break;
                case "text-transform":
                    if (value.equalsIgnoreCase("uppercase")) {
                        hyStyle.setRenderUppercase(true);
                        hasStyle = true;
                    }
                    break;
                case "text-align":
                case "layout-mode":
                case "layout":
                    if (builder instanceof LayoutModeSupported) {
                        ((LayoutModeSupported<?>) builder).withLayoutMode(capitalize(value));
                    }
                    break;
                case "vertical-align":
                    hyStyle.setVerticalAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "horizontal-align":
                    hyStyle.setHorizontalAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "align":
                    hyStyle.setAlignment(capitalize(value));
                    hasStyle = true;
                    break;
                case "visibility":
                    if (value.equalsIgnoreCase("hidden")) {
                        builder.withVisible(false);
                    } else if (value.equalsIgnoreCase("shown")) {
                        builder.withVisible(true);
                    }
                    break;
                case "display":
                    if (value.equalsIgnoreCase("none")) {
                        builder.withVisible(false);
                    } else if (value.equalsIgnoreCase("block")) {
                        builder.withVisible(true);
                    }
                    break;
                case "flex-weight":
                    try {
                        builder.withFlexWeight(Integer.parseInt(value));
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-left":
                    try {
                        anchor.setLeft(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-right":
                    try {
                        anchor.setRight(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-top":
                    try {
                        anchor.setTop(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-bottom":
                    try {
                        anchor.setBottom(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-width":
                    try {
                        anchor.setWidth(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor-height":
                    try {
                        anchor.setHeight(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "anchor":
                    try {
                        anchor.setFull(Integer.parseInt(value));
                        hasAnchor = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "padding-left":
                    try {
                        padding.setLeft(Integer.parseInt(value));
                        hasPadding = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "padding-right":
                    try {
                        padding.setRight(Integer.parseInt(value));
                        hasPadding = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "padding-top":
                    try {
                        padding.setTop(Integer.parseInt(value));
                        hasPadding = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "padding-bottom":
                    try {
                        padding.setBottom(Integer.parseInt(value));
                        hasPadding = true;
                    } catch (NumberFormatException ignored) {}
                    break;
                case "padding":
                    String[] values = value.split("\\s+");
                    try {
                        if (values.length == 1) {
                            padding.setFull(Integer.parseInt(values[0]));
                            hasPadding = true;
                        } else if (values.length >= 2) {
                            int v = Integer.parseInt(values[0]);
                            int h = Integer.parseInt(values[1]);
                            padding.setSymmetric(v, h);
                            hasPadding = true;
                        }
                    } catch (NumberFormatException ignored) {}
                    break;
                case "background-image":
                    if (builder instanceof BackgroundSupported) {
                        String realUrl = value.replace("url(", "")
                                .replace(")", "")
                                .replace("\"", "")
                                .replace("'", "");
                        HyUIPatchStyle background = ((BackgroundSupported<?>) builder).getBackground();
                        if (background == null) {
                            HyUIPatchStyle bg = new HyUIPatchStyle().setTexturePath(realUrl);
                            ((BackgroundSupported<?>) builder).withBackground(bg);
                        } else {
                            background.setTexturePath(realUrl);
                        }
                    }
                    break;
                case "background-color":
                    if (builder instanceof BackgroundSupported) {
                        HyUIPatchStyle background = ((BackgroundSupported<?>) builder).getBackground();
                        if (background == null) {
                            HyUIPatchStyle bg = new HyUIPatchStyle().setColor(value);
                            ((BackgroundSupported<?>) builder).withBackground(bg);
                        } else {
                            background.setColor(value);
                        }
                    }
                    break;
                case "hyui-style-reference":
                    String[] styleParts = value.split("\\s+");
                    if (styleParts.length == 1) {
                        hyStyle.withStyleReference(styleParts[0].replace("\"", "").replace("'", ""));
                        hasStyle = true;
                    } else if (styleParts.length >= 2) {
                        hyStyle.withStyleReference(
                                styleParts[0].replace("\"", "").replace("'", ""),
                                styleParts[1].replace("\"", "").replace("'", "")
                        );
                        hasStyle = true;
                    }
                    break;
                case "hyui-entry-label-style":
                    builder.withSecondaryStyle("EntryLabelStyle", parseStyleReference(value));
                    break;
                case "hyui-selected-entry-label-style":
                    builder.withSecondaryStyle("SelectedEntryLabelStyle", parseStyleReference(value));
                    break;
                case "hyui-popup-style":
                    builder.withSecondaryStyle("PopupStyle", parseStyleReference(value));
                    break;
                case "hyui-number-field-style":
                    builder.withSecondaryStyle("NumberFieldStyle", parseStyleReference(value));
                    break;
                case "hyui-checked-style":
                    builder.withSecondaryStyle("CheckedStyle", parseStyleReference(value));
                    break;
                case "hyui-unchecked-style":
                    builder.withSecondaryStyle("UncheckedStyle", parseStyleReference(value));
                    break;
            }
        }

        if (hasStyle) {
            builder.withStyle(hyStyle);
        }
        if (hasAnchor) {
            builder.withAnchor(anchor);
        }
        if (hasPadding) {
            builder.withPadding(padding);
        }
    }

    private HyUIStyle parseStyleReference(String value) {
        HyUIStyle style = new HyUIStyle();
        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            style.withStyleReference(parts[0].replace("\"", "").replace("'", ""));
        } else if (parts.length >= 2) {
            style.withStyleReference(
                    parts[0].replace("\"", "").replace("'", ""),
                    parts[1].replace("\"", "").replace("'", "")
            );
        }
        return style;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        // Check for specific multi-word LayoutModes
        if (str.equalsIgnoreCase("topscrolling")) return "TopScrolling";
        if (str.equalsIgnoreCase("bottomscrolling")) return "BottomScrolling";
        if (str.equalsIgnoreCase("middlecenter")) return "MiddleCenter";
        if (str.equalsIgnoreCase("centermiddle")) return "CenterMiddle";
        if (str.equalsIgnoreCase("leftcenterwrap")) return "LeftCenterWrap";
        
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
