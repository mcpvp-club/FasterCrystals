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

package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSources;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.enums.AnimPackets;
import xyz.reknown.fastercrystals.user.User;

public class AnimationListener extends SimplePacketListenerAbstract {
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) return;

        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        Player player = event.getPlayer();
        User user = plugin.getUsers().get(player);
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) return; // ignore weakness hits, tool hits are slow anyway
        if (user == null || !user.isFasterCrystals()) return;

        AnimPackets lastPacket = user.getLastPacket();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        FoliaScheduler.getEntityScheduler().run(player, plugin, task -> {
            if (lastPacket == AnimPackets.IGNORE) return; // animation is for hotbar drop item/placement/use item
            if (user.isIgnoreAnim()) return; // animation is for inventory drop item

            RayTraceResult result = eyeLoc.getWorld().rayTraceEntities(
                    eyeLoc,
                    direction,
                    player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).getValue(),
                    0.0,
                    entity -> {
                        if (!((CraftEntity) entity).getHandle().isPickable()) return false;
                        if (entity.getType() != EntityType.PLAYER) return true;

                        Player p = (Player) entity;
                        if (p.getGameMode() == GameMode.SPECTATOR) return false;

                        return !player.getUniqueId().equals(p.getUniqueId()) && player.canSee(p);
                    }
            );
            if (result == null) return;

            Entity entity = result.getHitEntity();
            if (entity == null || entity.getType() != EntityType.END_CRYSTAL) return;

            // Ignore if the entity was spawned in the same tick
            // This is to avoid "double popping" situations; see https://github.com/mcpvp-club/FasterCrystals/issues/3
            if (entity.getTicksLived() == 0) return;

            // Raytrace entity obtains position on the other side of the bounding box when the player eye location is
            //     within the bounding box. This causes the distance check to false positive.
            // Instead, ignore block raytrace checks if the crystal bounding box contains the eye vector.
            if (!entity.getBoundingBox().contains(eyeLoc.toVector())) {
                RayTraceResult bResult = eyeLoc.getWorld().rayTraceBlocks(eyeLoc, direction,
                        player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5);
                if (bResult != null) {
                    Block block = bResult.getHitBlock();
                    Vector eyeLocV = eyeLoc.toVector();
                    if (block != null) {
                        // Check if a block was in front of the end crystal
                        if (eyeLocV.distanceSquared(bResult.getHitPosition()) <= eyeLocV.distanceSquared(result.getHitPosition())) {
                            return;
                        }

                        // If true, it is in the middle of breaking a block
                        // We only want the beginning of left click inputs (begin mining or attack)
                        if (lastPacket != AnimPackets.START_DIGGING && lastPacket != AnimPackets.ATTACK) {
                            return;
                        }
                    }
                }
            }

            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            DamageSources damageSources = serverPlayer.level().damageSources();
            ((CraftEnderCrystal) entity).getHandle().hurt(damageSources.playerAttack(serverPlayer), 1);
        }, null);
    }
}
