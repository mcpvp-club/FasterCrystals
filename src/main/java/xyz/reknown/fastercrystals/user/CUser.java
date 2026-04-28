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

package xyz.reknown.fastercrystals.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.enums.AnimPackets;

@Getter
public class CUser {

    /**
     * Cached {@link NamespacedKey} to avoid creating a new instance on every PDC lookup.
     * The key never changes at runtime, so a single static instance is sufficient.
     */
    private static final NamespacedKey FAST_CRYSTALS_KEY =
            new NamespacedKey(FasterCrystals.getInstance(), "fastcrystals");

    private final Player player;

    @Setter
    private AnimPackets lastPacket = AnimPackets.MISC;
    @Setter
    private boolean ignoreAnim;

    public CUser(Player player) {
        this.player = player;
    }

    /**
     * Checks whether FasterCrystals is enabled for this user by reading the
     * persistent data container with the cached key and default state.
     *
     * @return true if fast crystals is enabled for this user
     */
    public boolean isFasterCrystals() {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        byte defaultState = FasterCrystals.getInstance().getConfigCache().getDefaultStateByte();
        return pdc.getOrDefault(FAST_CRYSTALS_KEY, PersistentDataType.BYTE, defaultState) == 1;
    }

    /**
     * Returns the cached {@link NamespacedKey} used for the fast crystals toggle.
     *
     * @return the fast crystals namespaced key
     */
    public static NamespacedKey getFastCrystalsKey() {
        return FAST_CRYSTALS_KEY;
    }
}