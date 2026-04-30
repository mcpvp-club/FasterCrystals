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

package xyz.reknown.fastercrystals.repository;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.reknown.fastercrystals.user.CUser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private final Map<UUID, CUser> users = new ConcurrentHashMap<>();

    public void add(@NotNull Player player) {
        users.put(player.getUniqueId(), new CUser(player));
    }

    @Nullable
    public CUser get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

    @Nullable
    public CUser get(@NotNull UUID uuid) {
        return users.get(uuid);
    }

    public void remove(@NotNull UUID uuid) {
        users.remove(uuid);
    }

    /**
     * Checks whether a user with the given UUID is currently tracked.
     *
     * @param uuid the player's UUID
     * @return true if the user is tracked
     */
    public boolean contains(@NotNull UUID uuid) {
        return users.containsKey(uuid);
    }

    /**
     * Returns the number of currently tracked users.
     *
     * @return the count of tracked users
     */
    public int size() {
        return users.size();
    }

    /**
     * Removes all tracked users. Called during plugin disable.
     */
    public void clear() {
        users.clear();
    }
}