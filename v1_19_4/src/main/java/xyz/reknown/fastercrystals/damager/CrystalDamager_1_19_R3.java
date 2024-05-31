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

package xyz.reknown.fastercrystals.damager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSources;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.ICrystalDamager;

public class CrystalDamager_1_19_R3 implements ICrystalDamager {
    @Override
    public void damage(Entity entity, Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        DamageSources damageSources = serverPlayer.level.damageSources();
        ((CraftEnderCrystal) entity).getHandle().hurt(damageSources.playerAttack(serverPlayer), 1);
    }
}
