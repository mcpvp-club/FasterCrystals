package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.*;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.user.User;

public class InteractEntityListener extends SimplePacketListenerAbstract {
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        Player player = (Player) event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        User user = plugin.getUsers().get(player);
        if (user == null || !user.isFastCrystals()) return;

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
}
