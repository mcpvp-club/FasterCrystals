/*
 * Copyright (C) 2023-2026 Jyguy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.reknown.fastercrystals.util;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.reknown.fastercrystals.FasterCrystals;

/**
 * Caches frequently accessed configuration values to avoid repeated
 * {@link FileConfiguration#getBoolean(String)} and {@link FileConfiguration#getString(String)}
 * lookups on every packet event.
 * <p>
 * Call {@link #reload(FasterCrystals)} after {@link FasterCrystals#reloadConfig()} to refresh cached values.
 */
@Getter
public class ConfigCache {

    private boolean defaultState;
    private byte defaultStateByte;

    private String stateOnText;
    private String stateOffText;
    private String toggleText;

    private double creativeModeReach;
    private double survivalModeReach;

    public ConfigCache(FasterCrystals plugin) {
        reload(plugin);
    }

    /**
     * Reloads all cached configuration values from the plugin's config.
     *
     * @param plugin the plugin instance to read config from
     */
    public void reload(FasterCrystals plugin) {
        FileConfiguration config = plugin.getConfig();

        this.defaultState = config.getBoolean("default-state", true);
        this.defaultStateByte = (byte) (defaultState ? 1 : 0);

        this.stateOnText = config.getString("state.on", "<yellow>on</yellow>");
        this.stateOffText = config.getString("state.off", "<yellow>off</yellow>");
        this.toggleText = config.getString("text", "<gold>Turned</gold> <state> <gold>your FasterCrystals setting.</gold>");

        this.creativeModeReach = config.getDouble("creative-mode-reach", 5.0);
        this.survivalModeReach = config.getDouble("survival-mode-reach", 4.5);
    }
}