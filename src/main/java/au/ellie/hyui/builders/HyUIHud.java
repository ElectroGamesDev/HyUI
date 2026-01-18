package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import au.ellie.hyui.events.UIContext;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A HUD for Hytale. 
 * It is important to store references to your existing HUDs to assist with updating elements.
 */
public class HyUIHud extends CustomUIHud implements UIContext {
    protected final HyUInterface delegate;

    private boolean isHidden;
    private HyUIMultiHud parentMultiHud;
    private long refreshRateMs;
    private Consumer<HyUIHud> refreshListener;
    
    public HyUIHud(PlayerRef playerRef, String uiFile, 
                   List<UIElementBuilder<?>> elements, 
                   List<Consumer<UICommandBuilder>> editCallbacks) {
        super(playerRef);
        this.delegate = new HyUInterface(uiFile, elements, editCallbacks) {};
    }
    
    @Override
    public void build(UICommandBuilder uiCommandBuilder) {
        delegate.buildFromCommandBuilder(uiCommandBuilder);
    }

    /**
     * Retrieves an element builder by its ID.
     *
     * @param id The ID of the element.
     * @return An Optional containing the element builder, or empty if not found.
     */
    public Optional<UIElementBuilder<?>> getById(String id) {
        return delegate.getById(id);
    }

    /**
     * Retrieves an element builder by its ID, cast to the specified type.
     *
     * @param id    The ID of the element.
     * @param clazz The class of the type to cast to.
     * @param <E>   The expected type of the element builder.
     * @return An Optional containing the cast element builder, or empty if not found or if casting fails.
     */
    public <E extends UIElementBuilder<E>> Optional<E> getById(String id, Class<E> clazz) {
        return delegate.getById(id, clazz);
    }

    /**
     * Updates the HUD with the provided builder.
     * The builder can be a completely new configuration.
     * 
     * @param updatedHudBuilder The builder containing updated HUD configuration.
     */
    public void update(HudBuilder updatedHudBuilder) {
        UICommandBuilder builder = configureFrom(updatedHudBuilder);
        this.update(true, builder);
    }

    /**
     * Remove the HUD from its parent multi-HUD. 
     * This will remove it from the screen for the player.
     * and stop refreshing it.
     * 
     * You can later associate it with another, or the same multi-HUD and show it.
     */
    public void removeFromMultiHud() {
        if (parentMultiHud != null) {
            parentMultiHud.removeHud(this);
        }
    }
    
    /**
     * Sets the multi-hud parent, this will show the HUD.
     *
     * @param parentMultiHud The multi-hud to associate with. If it is null, it will clear the parent.
     */
    public void showWithMultiHud(HyUIMultiHud parentMultiHud) {
        this.parentMultiHud = parentMultiHud;
        if (this.parentMultiHud != null) {
            HyUIPlugin.getLog().logInfo("REDRAW: HUD shown from single hud");
            // Redraw parent.
            this.parentMultiHud.show();
        }
    }
    
    /**
     * Hides the UI from view of player.
     */
    public void hide() {
        setVisibilityOnFirstElement(false);
    }

    /**
     * Shows the UI to the player if it has previously been hidden.
     */
    public void unhide() {
        setVisibilityOnFirstElement(true);
    }

    public long getRefreshRateMs() {
        return refreshRateMs;
    }

    public void setRefreshRateMs(long refreshRateMs) {
        this.refreshRateMs = refreshRateMs;
    }

    public void setRefreshListener(Consumer<HyUIHud> refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * Triggers the refresh listener if it exists.
     */
    public void triggerRefresh() {
        if (refreshListener != null) {
            refreshListener.accept(this);
        }
    }
    
    @Override
    public Optional<Object> getValue(String id) {
        return delegate.getValue(id);
    }

    private void setVisibilityOnFirstElement(boolean value) {
        for (UIElementBuilder<?> element : delegate.getElements()) {
            element.withVisible(value);
            break;
        }
        UICommandBuilder builder = new UICommandBuilder();
        delegate.buildFromCommandBuilder(builder);
        HyUIPlugin.getLog().logInfo("REDRAW: HUD SET VISIBILITY from single hud");
        this.update(true, builder);
        isHidden = !isHidden;
    }

    private UICommandBuilder configureFrom(HudBuilder updatedHudBuilder) {
        UICommandBuilder builder = new UICommandBuilder();
        delegate.setEditCallbacks(updatedHudBuilder.editCallbacks);
        delegate.setElements(updatedHudBuilder.getTopLevelElements());
        delegate.setUiFile(updatedHudBuilder.uiFile);
        return builder;
    }
}
