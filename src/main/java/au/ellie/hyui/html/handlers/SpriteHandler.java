package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.SpriteBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
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

        int width = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-width"), 0);
        int height = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-height"), 0);
        int perRow = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-per-row"), 0);
        int count = ParseUtils.parseIntOrDefault(element.attr("data-hyui-frame-count"), 0);

        if (width > 0 && height > 0 && perRow > 0 && count > 0) {
            builder.withFrame(width, height, perRow, count);
        }

        if (element.hasAttr("data-hyui-fps")) {
            ParseUtils.parseInt(element.attr("data-hyui-fps"))
                    .ifPresent(builder::withFramesPerSecond);
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
