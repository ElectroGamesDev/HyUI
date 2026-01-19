package au.ellie.hyui.elements;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.builders.HyUIPatchStyle;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

/**
 * Interface for UI elements that support backgrounds.
 */
public interface BackgroundSupported<T extends BackgroundSupported<T>> {
    
    /**
     * Sets the background for the element.
     * 
     * @param background The PatchStyle to set as background.
     * @return This builder instance for method chaining.
     */
    T withBackground(HyUIPatchStyle background);

    /**
     * Gets the current background.
     * 
     * @return The PatchStyle, or null if not set.
     */
    HyUIPatchStyle getBackground();

    /**
     * Default implementation to apply the background to the UICommandBuilder.
     * 
     * @param commands The UICommandBuilder to use.
     * @param selector The selector for the element.
     */
    default void applyBackground(UICommandBuilder commands, String selector) {
        HyUIPatchStyle background = getBackground();
        if (background != null && selector != null) {
            HyUIPlugin.getLog().logInfo("Setting Background for " + selector);
            
            // If the background only has color and that color has opacity (X.X) at end, 
            // we already applied it inline in UIElementBuilder.
            boolean hasOpacity = background.getColor() != null && background.getColor().contains("(");
            boolean hasTexture = background.getTexturePath() != null;
            
            if (hasOpacity && !hasTexture) {
                // Already handled inline
                return;
            }
            commands.setObject(selector + ".Background", background.getHytalePatchStyle());
        }
    }
}
