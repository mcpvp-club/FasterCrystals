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
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.user.User;

public class InteractEntityListener extends SimplePacketListenerAbstract {
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        User user = plugin.getUsers().get(player);
        if (user == null || !user.isFasterCrystals()) return;

        ItemStack item;
        if (wrapper.getHand() == InteractionHand.MAIN_HAND) item = player.getInventory().getItemInMainHand();
        else item = player.getInventory().getItemInOffHand();

        if (item.getType() != Material.END_CRYSTAL) return;

        int entityId = wrapper.getEntityId();
        EnderCrystal entity = plugin.getCrystal(entityId);
        if (entity == null) return;

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        FoliaScheduler.getEntityScheduler().run(entity, plugin, task -> {
            Location blockLoc = entity.getLocation().clone().subtract(0.5, 1.0, 0.5);

            RayTraceResult result = eyeLoc.getWorld().rayTraceBlocks(eyeLoc, direction, plugin.getRange().block(player));
            if (result == null || result.getHitBlock().getType() != Material.OBSIDIAN) return;

            if (!result.getHitBlock().getLocation().equals(blockLoc)) return;

            plugin.spawnCrystal(entity.getLocation().clone(), player, item);
        }, null);
    }
}
