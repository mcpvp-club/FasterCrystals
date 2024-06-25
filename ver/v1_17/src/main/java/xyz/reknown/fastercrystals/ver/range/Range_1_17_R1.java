/*
 * Copyright (C) 2023-2024 Jyguy
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

package xyz.reknown.fastercrystals.ver.range;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.IRange;

public class Range_1_17_R1 implements IRange {
    @Override
    public double block(Player player) {
        return player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5;
    }

    @Override
    public double entity(Player player) {
        return player.getGameMode() == GameMode.CREATIVE ? 5.0 : 3.0;
    }
}
