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

package xyz.reknown.fastercrystals.commands;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {
    protected final String name;

    public AbstractCommand(String name) {
        this.name = name;
    }

    public abstract void register();

    public abstract void run(Player player, CommandArguments args) throws WrapperCommandSyntaxException;
}
