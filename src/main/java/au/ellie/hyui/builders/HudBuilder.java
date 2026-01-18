package au.ellie.hyui.builders;

import au.ellie.hyui.HyUIPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Consumer;

public class HudBuilder extends InterfaceBuilder<HudBuilder> {
    private final PlayerRef playerRef;
    private long refreshRateMs = 0;
    private Consumer<HyUIHud> refreshListener;

    public HudBuilder(PlayerRef playerRef) {
        this.playerRef = playerRef;
        fromFile("Pages/EllieAU_HyUI_Placeholder.ui");
    }

    public HudBuilder() {
        this.playerRef = null;
        fromFile("Pages/EllieAU_HyUI_Placeholder.ui");
    }

    /**
     * Sets the refresh rate for the HUD in milliseconds.
     * If set to 0 (default), the HUD will not refresh periodically.
     *
     * @param ms The refresh rate in milliseconds.
     * @return The HudBuilder instance.
     */
    public HudBuilder withRefreshRate(long ms) {
        this.refreshRateMs = ms;
        return this;
    }

    /**
     * Registers a callback to be triggered when the HUD is refreshed.
     *
     * @param listener The listener callback.
     * @return The HudBuilder instance.
     */
    public HudBuilder onRefresh(Consumer<HyUIHud> listener) {
        this.refreshListener = listener;
        return this;
    }

    /**
     * Factory method to create a detached HUD builder that does not reference a player.
     * @return  the created HudBuilder instance.
     */
    public static HudBuilder detachedHud() {
        return new HudBuilder();
    }

    /**
     * Factory method to create a HUD builder for a specific player.
     * 
     * @param ref The player reference for whom the HUD should be created.
     * @return  the created HudBuilder instance.
     */
    public static HudBuilder hudForPlayer(PlayerRef ref) {
        return new HudBuilder(ref);
    }

    /**
     * Shows the HUD for the already existing player reference.
     * The playerRef must already be set, using {@code HudBuilder.hudForPlayer(PlayerRef ref)}
     * 
     * @param store The entity store containing player data.
     * @return The created HyUIHud instance.
     */
    public HyUIHud show(Store<EntityStore> store) {
        assert playerRef != null : "Player reference cannot be null.";
        return show(playerRef, store);
    }

    /**
     * Shows the HUD for the specified player using the provided entity store.
     * This also adds and manages multiple HUDs, there is no need to check the active HUD.
     * 
     * @param playerRefParam The player reference for whom the HUD should be shown.
     * @param store The entity store containing player data.
     * @return The created HyUIHud instance.
     */
    public HyUIHud show(@Nonnull PlayerRef playerRefParam, Store<EntityStore> store) {
        Player playerComponent = store.getComponent(playerRefParam.getReference(), Player.getComponentType());
        HyUIMultiHud multiHudToUse = getOrCreateMultiHud(playerComponent, playerRefParam);
        return addTo(playerRefParam, multiHudToUse, "HUD_" + System.currentTimeMillis());
    }

    
    /**
     * Registers this HUD with a multi-hud manager instead of setting it as the primary HUD.
     *
     * @param playerRefParam The player reference for whom the HUD should be shown.
     * @param multiHud The multi-hud manager to register with.
     * @param name     A unique name for this HUD component.
     * @return The built HyUIHud instance.
     */
    public HyUIHud addTo(@Nonnull PlayerRef playerRefParam, @Nonnull HyUIMultiHud multiHud, String name) {
        var hyUIHud = new HyUIHud(playerRefParam, uiFile, getTopLevelElements(), editCallbacks);
        hyUIHud.setRefreshRateMs(refreshRateMs);
        hyUIHud.setRefreshListener(refreshListener);
        HyUIPlugin.getLog().logInfo("Adding to a MultiHud: " + name);
        
        // Set HUD itself will redraw the parent and itself by proxy.
        multiHud.setHud(name, hyUIHud);
        return hyUIHud;
    }

    /**
     * You can update an existing HUD with this builder.
     * @param hudRef The HyUIHud instance to update.
     */
    public void updateExisting(@Nonnull HyUIHud hudRef) {
        hudRef.update(this);
    }

    @NonNullDecl
    private static HyUIMultiHud getOrCreateMultiHud(@Nonnull Player player,
                                                    @NonNullDecl PlayerRef playerRefParam) {
        HudManager hudManager = player.getHudManager();
        CustomUIHud currentHud = hudManager.getCustomHud();
        
        HyUIMultiHud multiHudToUse = null;

        if (currentHud instanceof HyUIMultiHud) {
            multiHudToUse = (HyUIMultiHud) currentHud;
        } else if (currentHud != null && "MultipleCustomUIHud".equals(currentHud.getClass().getSimpleName())) {
            // We support multiple hud mod by adding our own multi-hud to the existing one :3.
            try {
                Method getCustomHudsMethod = currentHud.getClass().getDeclaredMethod("getCustomHuds");
                getCustomHudsMethod.setAccessible(true);
                @SuppressWarnings("unchecked")
                HashMap<String, CustomUIHud> customHuds =
                        (HashMap<String, CustomUIHud>) getCustomHudsMethod.invoke(currentHud);

                CustomUIHud existing = customHuds.get("HyUIHUD");
                if (existing instanceof HyUIMultiHud) {
                    multiHudToUse = (HyUIMultiHud) existing;
                } else {
                    multiHudToUse = new HyUIMultiHud(playerRefParam);
                    customHuds.put("HyUIHUD", multiHudToUse);
                }
            } catch (NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // If it is simply not possible to determine what mod has the hud, overwrite it.
        // TODO: Potentially wrap that mod which has a hud currently into our own multihud solution?
        if (multiHudToUse == null) {
            multiHudToUse = new HyUIMultiHud(playerRefParam);
            hudManager.setCustomHud(playerRefParam, multiHudToUse);
        }
        return multiHudToUse;
    }
}
