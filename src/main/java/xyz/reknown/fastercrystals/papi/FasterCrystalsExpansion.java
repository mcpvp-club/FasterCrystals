/*
 * Copyright (C) 2023-2025 Jyguy
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

package xyz.reknown.fastercrystals.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.user.CUser;

public class FasterCrystalsExpansion extends PlaceholderExpansion {

    public final FasterCrystals plugin;

    public FasterCrystalsExpansion() {
        this.plugin = FasterCrystals.getInstance();
    }

    @Override
    public @NotNull String getAuthor() {
        return "Jyguy";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "fastercrystals";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("toggle")) {
            CUser cUser = plugin.getUserRepository().get(player);
            return cUser == null || cUser.isFasterCrystals() ? "On" : "Off";
        }

        return null;
    }
}
