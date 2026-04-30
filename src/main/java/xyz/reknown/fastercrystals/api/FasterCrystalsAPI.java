/*
 * Copyright (C) 2023-2026 Jyguy
 *
 * FasterCrystals is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FasterCrystals is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.reknown.fastercrystals.api;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.user.CUser;

public class FasterCrystalsAPI {
    private static FasterCrystalsAPI instance;

    @Getter
    private final FasterCrystals plugin;
    private final NamespacedKey fastCrystalsKey;

    private FasterCrystalsAPI(FasterCrystals plugin) {
        this.plugin = plugin;
        this.fastCrystalsKey = new NamespacedKey(plugin, "fastcrystals");
    }

    /**
     * Initializes the API. Must be called once during plugin enable
     *
     * @param plugin the FasterCrystals plugin instance
     */
    public static void init(FasterCrystals plugin) {
        if (instance != null) {
            throw new IllegalStateException("FasterCrystalsAPI is already initialized.");
        }
        instance = new FasterCrystalsAPI(plugin);
    }

    /**
     * Shuts down the API. Should be called during plugin disable
     */
    public static void shutdown() {
        instance = null;
    }

    /**
     * Checks if the API is available.
     *
     * @return true if initialized and not shut down, false otherwise
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    /**
     * Gets the singleton instance of the API.
     *
     * @return the API instance
     * @throws IllegalStateException if not initialised yet
     */
    public static FasterCrystalsAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FasterCrystalsAPI has not been initialized yet.");
        }
        return instance;
    }

    /**
     * Returns the {@link NamespacedKey} used for the fast crystals PDC toggle.
     *
     * @return the namespaced key
     */
    public NamespacedKey getFastCrystalsKey() {
        return fastCrystalsKey;
    }

    /**
     * Sets the FasterCrystals toggle state for a specific player.
     *
     * @param player  the player whose toggle state will be updated
     * @param enabled true to enable fast crystals, false to disable
     */
    public void setFastCrystals(Player player, boolean enabled) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(fastCrystalsKey, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    /**
     * Checks if FasterCrystals is currently enabled for a specific player.
     *
     * @param player the player to check
     * @return true if enabled, false otherwise
     */
    public boolean isFastCrystalsEnabled(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        byte defaultState = plugin.getConfigCache().getDefaultStateByte();
        return pdc.getOrDefault(fastCrystalsKey, PersistentDataType.BYTE, defaultState) == 1;
    }

    /**
     * Toggles the FasterCrystals state for the specified player.
     * If it was enabled, it will be disabled, and vice versa.
     *
     * @param player the player whose toggle state will be flipped
     * @return the new toggle state (true = enabled, false = disabled)
     */
    public boolean toggleFastCrystals(Player player) {
        boolean newState = !isFastCrystalsEnabled(player);
        setFastCrystals(player, newState);
        return newState;
    }

    /**
     * Checks if FasterCrystals is currently enabled for a specific player
     * using the cached {@link CUser} object from the user repository.
     * <p>
     * This method is faster than {@link #isFastCrystalsEnabled(Player)} when the
     * CUser is already available, as it avoids an additional map lookup.
     *
     * @param cUser the cached user object
     * @return true if enabled, false otherwise
     */
    public boolean isFastCrystalsEnabled(CUser cUser) {
        return cUser.isFasterCrystals();
    }
}