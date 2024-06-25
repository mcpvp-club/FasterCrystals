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

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.IRange;

public class Range_1_20_R4 implements IRange {
    @Override
    public double block(Player player) {
        return player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).getValue();
    }

    @Override
    public double entity(Player player) {
        return player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).getValue();
    }
}
