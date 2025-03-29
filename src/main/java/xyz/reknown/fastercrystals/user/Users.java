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

package xyz.reknown.fastercrystals.user;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Users {
    private final Map<UUID, User> users;

    public Users() {
        this.users = new ConcurrentHashMap<>();
    }

    public void add(User user) {
        users.put(user.getPlayer().getUniqueId(), user);
    }

    @Nullable
    public User get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

    @Nullable
    public User get(@NotNull UUID uuid) {
        return users.get(uuid);
    }

    public void remove(@NotNull UUID uuid) {
        users.remove(uuid);
    }
}
