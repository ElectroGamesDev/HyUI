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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HudBuilder extends InterfaceBuilder<HudBuilder> {
    private final PlayerRef playerRef;
    private long refreshRateMs = 0;
    private Consumer<HyUIHud> refreshListener;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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
        MultiHudResult result = getOrCreateMultiHud(playerComponent, playerRefParam);
        String name = "HUD_" + System.currentTimeMillis();
        if (result.isIncompatibleMod) {
            HyUIPlugin.getLog().logInfo("Incompatible mod detected, delaying HUD addition by 3 seconds.");
            var delayedHud = new HyUIHud(playerRefParam, uiFile, getTopLevelElements(), editCallbacks);
            delayedHud.isDelayed = true;
            scheduler.schedule(() -> {
                HyUIPlugin.getLog().logInfo("Incompatible mod: Attempting to show HUD (delayed).");
                delayedHud.isDelayed = false;
                delayedHud.setRefreshRateMs(refreshRateMs);
                delayedHud.setRefreshListener(refreshListener);
                HyUIPlugin.getLog().logInfo("Adding to a MultiHud: " + name);
                
                MultiHudResult r = getOrCreateMultiHud(playerComponent, playerRefParam);
                if (r.isIncompatibleMod) {
                    HyUIPlugin.getLog().logInfo("Unable to work with incompatible mod. Abandoning...");
                    return;
                }
                // Set HUD itself will redraw the parent and itself by proxy.
                r.multiHud.setHud(name, delayedHud);
                
                HyUIPlugin.getLog().logInfo("Incompatible mod: HUD shown.");
            }, 1, TimeUnit.SECONDS);
            // We return a dummy/placeholder since it's delayed.
            return delayedHud;
        }
        return addTo(playerRefParam, result.multiHud, name);
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
    private static MultiHudResult getOrCreateMultiHud(@Nonnull Player player,
                                                    @NonNullDecl PlayerRef playerRefParam) {
        HyUIPlugin.getLog().logInfo("getOrCreateMultiHud called");
        HudManager hudManager = player.getHudManager();
        HyUIPlugin.getLog().logInfo("hudManager retrieved");
        CustomUIHud currentHud = hudManager.getCustomHud();
        HyUIPlugin.getLog().logInfo("currentHud retrieved: " + (currentHud == null ? "null" : currentHud.getClass().getName()));

        HyUIMultiHud multiHudToUse = null;
        boolean incompatibleMod = false;

        try {
            HyUIPlugin.getLog().logInfo("Attempting to find MultipleHUD class");
            Class<?> multipleHudClass = Class.forName("com.buuz135.mhud.MultipleHUD");
            HyUIPlugin.getLog().logInfo("MultipleHUD class found");
            Method getInstanceMethod = multipleHudClass.getDeclaredMethod("getInstance");
            HyUIPlugin.getLog().logInfo("getInstance method found");
            Object multipleHudInstance = getInstanceMethod.invoke(null);
            HyUIPlugin.getLog().logInfo("MultipleHUD instance retrieved");

            try {
                HyUIPlugin.getLog().logInfo("Checking if currentHud is MultipleCustomUIHud");
                if (currentHud != null && "MultipleCustomUIHud".equals(currentHud.getClass().getSimpleName())) {
                    HyUIPlugin.getLog().logInfo("currentHud is MultipleCustomUIHud, attempting to get customHuds map");
                    try {
                        Method getCustomHudsMethod = currentHud.getClass().getDeclaredMethod("getCustomHuds");
                        HyUIPlugin.getLog().logInfo("getCustomHuds method found");
                        getCustomHudsMethod.setAccessible(true);
                        HyUIPlugin.getLog().logInfo("getCustomHuds method set accessible");
                        @SuppressWarnings("unchecked")
                        HashMap<String, CustomUIHud> customHuds =
                                (HashMap<String, CustomUIHud>) getCustomHudsMethod.invoke(currentHud);
                        HyUIPlugin.getLog().logInfo("customHuds map retrieved");

                        CustomUIHud existing = customHuds.get("HyUIHUD");
                        HyUIPlugin.getLog().logInfo("Existing HyUIHUD check: " + (existing == null ? "null" : existing.getClass().getName()));
                        if (existing instanceof HyUIMultiHud) {
                            multiHudToUse = (HyUIMultiHud) existing;
                            HyUIPlugin.getLog().logInfo("Existing HyUIMultiHud found and assigned");
                        }
                    } catch (Exception ex) {
                        HyUIPlugin.getLog().logInfo("Failed to interact with MultipleCustomUIHud: " + ex.getMessage());
                    }
                } 
                if (multiHudToUse == null)
                {
                    HyUIPlugin.getLog().logInfo("multiHudToUse is null, creating new HyUIMultiHud");
                    multiHudToUse = new HyUIMultiHud(playerRefParam);
                    HyUIPlugin.getLog().logInfo("New HyUIMultiHud created");
                    Method setCustomHudMethod = multipleHudClass.getDeclaredMethod("setCustomHud", Player.class, PlayerRef.class, String.class, CustomUIHud.class);
                    HyUIPlugin.getLog().logInfo("setCustomHud method found");
                    
                    // Make sure we set the alert so we can properly show/not show.
                    HyUIMultiHud.hasHudManager = true;
                    
                    // Register simplepartyhud if it exists
                    if (currentHud != null && "PartyHud".equals(currentHud.getClass().getSimpleName())) {
                        HyUIPlugin.getLog().logInfo("Current HUD is PartyHud. Setting incompatible flag.");
                        incompatibleMod = true;
                        return new MultiHudResult(null, true);
                    }
                    setCustomHudMethod.invoke(multipleHudInstance, player, playerRefParam, "HyUIHUD", multiHudToUse);
                    HyUIPlugin.getLog().logInfo("Successfully hooked into MultipleHUD and registered HyUIMultiHUD!");
                }
                
            } catch (InvocationTargetException e) {
                HyUIPlugin.getLog().logInfo("InvocationTargetException during MultipleHUD registration: " + e.getTargetException().getMessage());
            } catch (Exception e) {
                multiHudToUse = null;
                HyUIPlugin.getLog().logInfo("Failed to register with MultipleHUD: " + e.getMessage());
            }
        } catch (ClassNotFoundException ignored) {
            HyUIPlugin.getLog().logInfo("MultipleHUD mod not present (ClassNotFoundException)");
        } catch (Exception e) {
            multiHudToUse = null;
            HyUIPlugin.getLog().logInfo("Failed to hook into MultipleHUD: " + e.getMessage());
        }
        

        // If still no multi-hud, create one
        HyUIPlugin.getLog().logInfo("Final multiHudToUse check: " + (multiHudToUse == null ? "null" : "not null"));
        if (multiHudToUse == null) {
            HyUIPlugin.getLog().logInfo("Creating fallback HyUIMultiHud");
            multiHudToUse = new HyUIMultiHud(playerRefParam);
            HyUIPlugin.getLog().logInfo("Fallback HyUIMultiHud created");
            hudManager.setCustomHud(playerRefParam, multiHudToUse);
            HyUIPlugin.getLog().logInfo("Fallback HyUIMultiHud set in hudManager");
        }

        HyUIPlugin.getLog().logInfo("getOrCreateMultiHud returning");
        return new MultiHudResult(multiHudToUse, incompatibleMod);
    }

    private static class MultiHudResult {
        final HyUIMultiHud multiHud;
        final boolean isIncompatibleMod;

        MultiHudResult(HyUIMultiHud multiHud, boolean isIncompatibleMod) {
            this.multiHud = multiHud;
            this.isIncompatibleMod = isIncompatibleMod;
        }
    }
}
