package au.ellie.hyui.html.handlers;

import au.ellie.hyui.builders.ProgressBarBuilder;
import au.ellie.hyui.builders.UIElementBuilder;
import au.ellie.hyui.html.HtmlParser;
import au.ellie.hyui.html.TagHandler;
import au.ellie.hyui.utils.ParseUtils;
import org.jsoup.nodes.Element;

public class ProgressBarHandler implements TagHandler {
    @Override
    public boolean canHandle(Element element) {
        return element.tagName().equalsIgnoreCase("progress");
    }

    @Override
    public UIElementBuilder<?> handle(Element element, HtmlParser parser) {
        boolean isCircular = element.hasClass("circular-progress") || element.hasClass("circular");
        ProgressBarBuilder builder = isCircular
                ? ProgressBarBuilder.circularProgressBar()
                : ProgressBarBuilder.progressBar();

        if (element.hasAttr("value")) {
            ParseUtils.parseFloat(element.attr("value")).ifPresent(value -> {
                // Value in a progress bar is between 0.0 and 1.0. 50 on a value attribute for html would be 0.5.
                builder.withValue(value > 1.0f ? value / 100.0f : value);
            });
        }

        if (element.hasAttr("data-hyui-effect-width")) {
            ParseUtils.parseInt(element.attr("data-hyui-effect-width"))
                    .ifPresent(builder::withEffectWidth);
        }
        if (element.hasAttr("data-hyui-effect-height")) {
            ParseUtils.parseInt(element.attr("data-hyui-effect-height"))
                    .ifPresent(builder::withEffectHeight);
        }
        if (element.hasAttr("data-hyui-effect-offset")) {
            ParseUtils.parseInt(element.attr("data-hyui-effect-offset"))
                    .ifPresent(builder::withEffectOffset);
        }

        if (element.hasAttr("data-hyui-direction")) {
            builder.withDirection(element.attr("data-hyui-direction"));
        }

        if (element.hasAttr("data-hyui-bar-texture-path")) {
            builder.withBarTexturePath(element.attr("data-hyui-bar-texture-path"));
        }
        if (element.hasAttr("data-hyui-effect-texture-path")) {
            builder.withEffectTexturePath(element.attr("data-hyui-effect-texture-path"));
        }

        if (element.hasAttr("data-hyui-mask-texture-path")) {
            builder.withMaskTexturePath(element.attr("data-hyui-mask-texture-path"));
        }

        if (element.hasAttr("data-hyui-color")) {
            builder.withColor(element.attr("data-hyui-color"));
        }

        if (element.hasAttr("data-hyui-alignment")) {
            builder.withAlignment(element.attr("data-hyui-alignment"));
        }

        applyCommonAttributes(builder, element);
        return builder;
    }
}
