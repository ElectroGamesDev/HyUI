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
import au.ellie.hyui.elements.UIElements;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

/**
 * Builder for BackgroundImage UI elements.
 */
public class BackgroundImageBuilder extends UIElementBuilder<BackgroundImageBuilder> {
    private String image;
    private String imageUw;

    public BackgroundImageBuilder() {
        super(UIElements.BACKGROUND_IMAGE, "#HyUIBackgroundImage");
        withWrappingGroup(true);
    }

    public static BackgroundImageBuilder backgroundImage() {
        return new BackgroundImageBuilder();
    }

    public BackgroundImageBuilder withImage(String image) {
        this.image = image;
        return this;
    }

    public BackgroundImageBuilder withImageUw(String imageUw) {
        this.imageUw = imageUw;
        return this;
    }

    @Override
    protected boolean supportsStyling() {
        return false;
    }

    @Override
    protected void onBuild(UICommandBuilder commands, UIEventBuilder events) {
        String selector = getSelector();
        if (selector == null) return;

        if (image != null) {
            HyUIPlugin.getLog().logFinest("Setting Image: " + image + " for " + selector);
            commands.set(selector + ".Image", image);
        }
        if (imageUw != null) {
            HyUIPlugin.getLog().logFinest("Setting ImageUW: " + imageUw + " for " + selector);
            commands.set(selector + ".ImageUW", imageUw);
        }
    }
}
