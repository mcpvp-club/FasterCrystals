package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.enums.AnimPackets;
import xyz.reknown.fastercrystals.user.User;

public class CrystalPacketListener extends PacketListenerAbstract {
    public CrystalPacketListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            handleInteractEntity(event);
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            handleAnimation(event);
        }

        // Handle last packet to ignore certain Animation packets
        Player player = (Player) event.getPlayer();
        if (player != null) {
            FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
            User user = plugin.getUsers().get(player);
            if (user == null) return;

            setLastPacket(user, event);
        }
    }

    private void handleInteractEntity(PacketReceiveEvent event) {
        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        Player player = (Player) event.getPlayer();
        User user = plugin.getUsers().get(player);
        if (!user.isFastCrystals()) return;

        ItemStack item;
        if (wrapper.getHand() == InteractionHand.MAIN_HAND) item = player.getInventory().getItemInMainHand();
        else item = player.getInventory().getItemInOffHand();

        if (item.getType() != Material.END_CRYSTAL) return;

        int entityId = wrapper.getEntityId();
        EnderCrystal entity = plugin.getCrystal(entityId);
        if (entity == null) return;

        Location blockLoc = entity.getLocation().clone().subtract(0.5, 1.0, 0.5);

        RayTraceResult result = player.rayTraceBlocks(player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5,
                FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock().getType() != Material.OBSIDIAN) return;
        if (!result.getHitBlock().getLocation().equals(blockLoc)) return;

        Bukkit.getScheduler().runTask(plugin, () -> plugin.spawnCrystal(entity.getLocation().clone(), player, item));
    }

    private void handleAnimation(PacketReceiveEvent event) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        Player player = (Player) event.getPlayer();
        User user = plugin.getUsers().get(player);
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) return; // ignore weakness hits, tool hits are slow anyway
        if (!user.isFastCrystals()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (user.getLastPacket() == AnimPackets.IGNORE) return; // animation is for drop item/placement/use item

            Location eyeLoc = player.getEyeLocation();
            RayTraceResult result = player.getWorld().rayTraceEntities(
                    eyeLoc,
                    player.getLocation().getDirection(),
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

            // Raytrace entity obtains position on the other side of the bounding box when the player eye location is
            //     within the bounding box. This causes the distance check to false positive.
            // Instead, ignore block raytrace checks if the crystal bounding box contains the eye vector.
            if (!entity.getBoundingBox().contains(eyeLoc.toVector())) {
                RayTraceResult bResult = player.rayTraceBlocks(player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5);
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
                        if (user.getLastPacket() != AnimPackets.START_DIGGING && user.getLastPacket() != AnimPackets.ATTACK) {
                            return;
                        }
                    }
                }
            }

            plugin.getDamager().damage(entity, player);
        });
    }

    private void setLastPacket(User user, PacketReceiveEvent event) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);

        // Initialize wrappers before leaving thread to be used in main thread
        // Could just clone the event, unsure of performance penalties
        PacketWrapper<?> genericWrapper;
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            genericWrapper = new WrapperPlayClientPlayerDigging(event);
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            genericWrapper = new WrapperPlayClientInteractEntity(event);
        } else genericWrapper = null;

        // Run on main thread since the other listeners must happen (which runs on the main thread) before this one
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging wrapper = (WrapperPlayClientPlayerDigging) genericWrapper;
                if (wrapper.getAction() == DiggingAction.DROP_ITEM
                        || wrapper.getAction() == DiggingAction.DROP_ITEM_STACK) {
                    user.setLastPacket(AnimPackets.IGNORE);
                    return;
                } else if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                    user.setLastPacket(AnimPackets.START_DIGGING);
                    return;
                }
            } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                    || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                user.setLastPacket(AnimPackets.IGNORE);
                return;
            } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity wrapper = (WrapperPlayClientInteractEntity) genericWrapper;
                if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    user.setLastPacket(AnimPackets.ATTACK);
                    return;
                }
            }

            user.setLastPacket(AnimPackets.MISC);
        });
    }
}
