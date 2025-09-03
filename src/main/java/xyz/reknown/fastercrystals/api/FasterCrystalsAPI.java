/*
 * Copyright (C) 2023-2025 Jyguy
 *
 * This file is part of FasterCrystals.
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

/**
 * API for interacting with FasterCrystals toggle state.
 * Allows other plugins to enable, disable, or check whether
 * the FasterCrystals behavior is active for a specific player.
 *
 * @author Jyguy
 * @since 1.0
 */
public class FasterCrystalsAPI {

    private static FasterCrystals plugin;

    /**
     * Initializes the API with a reference to the main plugin.
     * Must be called during plugin enable phase.
     *
     * @param instance the FasterCrystals plugin instance
     */
    public static void init(FasterCrystals instance) {
        plugin = instance;
    }

    /**
     * Gets the internal {@link NamespacedKey} used to store the toggle state.
     *
     * @return the toggle key
     */
    private static NamespacedKey getKey() {
        return new NamespacedKey(plugin, "fastcrystals");
    }

    /**
     * Sets the FasterCrystals toggle state for a specific player.
     *
     * @param player  the player whose toggle state will be updated
     * @param enabled true to enable fast crystals, false to disable
     */
    public static void setFastCrystals(Player player, boolean enabled) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(getKey(), PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    /**
     * Checks if FasterCrystals is currently enabled for a specific player.
     *
     * @param player the player to check
     * @return true if enabled, false otherwise
     */
    public static boolean isFastCrystalsEnabled(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.getOrDefault(getKey(), PersistentDataType.BYTE, (byte) 1) == 1;
    }

    /**
     * Toggles the FasterCrystals state for the specified player.
     * If it was enabled, it will be disabled, and vice versa.
     *
     * @param player the player whose toggle state will be flipped
     */
    public static void toggleFastCrystals(Player player) {
        boolean newState = !isFastCrystalsEnabled(player);
        setFastCrystals(player, newState);
    }
}
