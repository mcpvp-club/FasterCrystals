/*
 * Copyright (C) 2023-2025 Jyguy
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

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.reknown.fastercrystals.FasterCrystals;

public class FasterCrystalsAPI {
    private static FasterCrystalsAPI instance;

    private final FasterCrystals plugin;
    private final NamespacedKey fastCrystalsKey;

    private FasterCrystalsAPI(FasterCrystals plugin) {
        this.plugin = plugin;
        this.fastCrystalsKey = new NamespacedKey(plugin, "fastcrystals");
    }

    /**
     * Initializes the API. Must be called once during plugin enabl
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
        return pdc.getOrDefault(fastCrystalsKey, PersistentDataType.BYTE, (byte) 1) == 1;
    }

    /**
     * Toggles the FasterCrystals state for the specified player.
     * If it was enabled, it will be disabled, and vice versa.
     *
     * @param player the player whose toggle state will be flipped
     */
    public void toggleFastCrystals(Player player) {
        boolean newState = !isFastCrystalsEnabled(player);
        setFastCrystals(player, newState);
    }
}
