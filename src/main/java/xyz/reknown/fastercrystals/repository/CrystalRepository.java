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

import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalRepository {
    private final Map<Integer, EnderCrystal> crystalIds = new ConcurrentHashMap<>();

    public void add(@NotNull EnderCrystal crystal) {
        crystalIds.put(crystal.getEntityId(), crystal);
    }

    public @Nullable EnderCrystal get(int id) {
        return crystalIds.get(id);
    }

    public void remove(int id) {
        crystalIds.remove(id);
    }

    /**
     * Checks whether a crystal with the given entity id is tracked.
     *
     * @param id the entity id
     * @return true if the crystal is tracked
     */
    public boolean contains(int id) {
        return crystalIds.containsKey(id);
    }

    /**
     * Returns the number of currently tracked crystals.
     *
     * @return the count of tracked crystals
     */
    public int size() {
        return crystalIds.size();
    }

    /**
     * Removes all tracked crystals belonging to the specified world.
     * <p>
     * Uses the world's {@link UUID} for comparison to avoid potential issues
     * with entity references to unloaded worlds throwing exceptions.
     *
     * @param world the world whose crystals should be removed
     */
    public void unloadWorldCrystals(@NotNull World world) {
        UUID worldUid = world.getUID();
        crystalIds.entrySet().removeIf(entry -> {
            EnderCrystal crystal = entry.getValue();
            try {
                return crystal.getWorld().getUID().equals(worldUid);
            } catch (Exception e) {
                // Entity reference is no longer valid, remove it
                return true;
            }
        });
    }

    /**
     * Removes all tracked crystals. Called during plugin disable.
     */
    public void clear() {
        crystalIds.clear();
    }
}