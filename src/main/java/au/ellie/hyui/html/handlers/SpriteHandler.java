package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.SpriteBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import org.jsoup.nodes.Element;

public class SpriteHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("sprite");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        SpriteBuilder builder = SpriteBuilder.sprite();

        if (element.hasAttr("src")) {
            builder.withTexture(element.attr("src"));
        }

        int width = 0;
        int height = 0;
        int perRow = 0;
        int count = 0;

        if (element.hasAttr("data-hyui-frame-width")) {
            try {
                width = Integer.parseInt(element.attr("data-hyui-frame-width"));
            } catch (NumberFormatException ignored) {}
        }
        if (element.hasAttr("data-hyui-frame-height")) {
            try {
                height = Integer.parseInt(element.attr("data-hyui-frame-height"));
            } catch (NumberFormatException ignored) {}
        }
        if (element.hasAttr("data-hyui-frame-per-row")) {
            try {
                perRow = Integer.parseInt(element.attr("data-hyui-frame-per-row"));
            } catch (NumberFormatException ignored) {}
        }
        if (element.hasAttr("data-hyui-frame-count")) {
            try {
                count = Integer.parseInt(element.attr("data-hyui-frame-count"));
            } catch (NumberFormatException ignored) {}
        }

        if (width > 0 && height > 0 && perRow > 0 && count > 0) {
            builder.withFrame(width, height, perRow, count);
        }

        if (element.hasAttr("data-hyui-fps")) {
            try {
                builder.withFramesPerSecond(Integer.parseInt(element.attr("data-hyui-fps")));
            } catch (NumberFormatException ignored) {}
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
