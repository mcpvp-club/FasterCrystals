package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import io.github.retrooper.packetevents.util.FoliaCompatUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
        Player player = (Player) event.getPlayer();
        User user = plugin.getUsers().get(player);
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) return; // ignore weakness hits, tool hits are slow anyway
        if (user == null || !user.isFastCrystals()) return;

        AnimPackets lastPacket = user.getLastPacket();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        FoliaCompatUtil.runTaskForEntity(player, plugin, () -> {
            if (lastPacket == AnimPackets.IGNORE) return; // animation is for hotbar drop item/placement/use item
            if (user.isIgnoreAnim()) return; // animation is for inventory drop item

            RayTraceResult result = eyeLoc.getWorld().rayTraceEntities(
                    eyeLoc,
                    direction,
                    3.0,
                    0.0,
                    entity -> {
                        if (!plugin.getPickableChecker().isPickable(entity)) return false;
                        if (entity.getType() != EntityType.PLAYER) return true;

                        Player p = (Player) entity;
                        if (p.getGameMode() == GameMode.SPECTATOR) return false;

                        return !player.getUniqueId().equals(p.getUniqueId()) && player.canSee(p);
                    }
            );
            if (result == null) return;

            Entity entity = result.getHitEntity();
            if (entity == null || entity.getType() != EntityType.ENDER_CRYSTAL) return;

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

            plugin.getDamager().damage(entity, player);
        }, null, 0L);
    }
}
