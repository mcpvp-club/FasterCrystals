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

import lombok.AccessLevel;
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
    private static final NamespacedKey FAST_CRYSTALS_KEY =
            new NamespacedKey(FasterCrystals.getInstance(), "fastcrystals");

    private final Player player;

    @Setter
    private AnimPackets lastPacket = AnimPackets.MISC;
    private volatile boolean ignoreAnim;
    @Getter(AccessLevel.NONE)
    private PendingAnimation pendingAnimation = new PendingAnimation();

    public CUser(Player player) {
        this.player = player;
    }

    /**
     * Starts tracking a swing before any packet-specific early returns. Each queued
     * callback keeps its own token so a later swing cannot change its suppression state.
     */
    public PendingAnimation beginAnimation() {
        pendingAnimation = new PendingAnimation();
        ignoreAnim = false;
        return pendingAnimation;
    }

    public void setIgnoreAnim(boolean ignoreAnim) {
        this.ignoreAnim = ignoreAnim;
        pendingAnimation.ignored = ignoreAnim;
    }

    @Getter
    public static final class PendingAnimation {
        private volatile boolean ignored;
    }

    public boolean isFasterCrystals() {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        byte defaultState = FasterCrystals.getInstance().getConfigCache().getDefaultStateByte();
        return pdc.getOrDefault(FAST_CRYSTALS_KEY, PersistentDataType.BYTE, defaultState) == 1;
    }
}
