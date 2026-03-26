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

package xyz.reknown.fastercrystals.listener.packet;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.api.FasterCrystalsAPI;
import xyz.reknown.fastercrystals.repository.CrystalRepository;
import xyz.reknown.fastercrystals.repository.UserRepository;
import xyz.reknown.fastercrystals.user.CUser;

import java.util.Set;

public class InteractEntityListener extends SimplePacketListenerAbstract {
    private static final Set<Material> ALLOWED_BLOCKS = Set.of(Material.OBSIDIAN, Material.BEDROCK);
    private static final Attribute BLOCK_INTERACTION_ATTRIBUTE = Attribute.PLAYER_BLOCK_INTERACTION_RANGE;

    private final FasterCrystals plugin;
    private final UserRepository userRepository;
    private final CrystalRepository crystalRepository;

    public InteractEntityListener() {
        this.plugin = FasterCrystalsAPI.getInstance().getPlugin();
        this.userRepository = plugin.getUserRepository();
        this.crystalRepository = plugin.getCrystalRepository();
    }

    @Override
    public void onPacketPlayReceive(@NonNull PacketPlayReceiveEvent event) {
        if (!FasterCrystalsAPI.isAvailable()) return;
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        CUser cUser = userRepository.get(player);
        if (cUser == null || !cUser.isFasterCrystals()) return;

        ItemStack item;
        if (packet.getHand() == InteractionHand.MAIN_HAND) item = player.getInventory().getItemInMainHand();
        else item = player.getInventory().getItemInOffHand();

        if (item.getType() != Material.END_CRYSTAL) return;

        int entityId = packet.getEntityId();
        EnderCrystal entity = crystalRepository.get(entityId);
        if (entity == null) return;

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        FoliaScheduler.getRegionScheduler().run(plugin, eyeLoc, task -> {
            AttributeInstance blockInteractionRange = player.getAttribute(BLOCK_INTERACTION_ATTRIBUTE);
            if (blockInteractionRange == null) return;

            RayTraceResult result = eyeLoc.getWorld().rayTraceBlocks(eyeLoc, direction, blockInteractionRange.getValue());
            if (result == null) return;

            Block block = result.getHitBlock();
            if (block == null) return;

            if (!ALLOWED_BLOCKS.contains(result.getHitBlock().getType())) return;

            Location blockLoc = entity.getLocation().subtract(0.5, 1.0, 0.5);
            if (!result.getHitBlock().getLocation().equals(blockLoc)) return;

            plugin.spawnCrystal(entity.getLocation(), player, item);
        });
    }
}
