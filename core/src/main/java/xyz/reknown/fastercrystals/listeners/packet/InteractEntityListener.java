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

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        FoliaScheduler.getEntityScheduler().run(entity, plugin, task -> {
            Location blockLoc = entity.getLocation().clone().subtract(0.5, 1.0, 0.5);

            RayTraceResult result = eyeLoc.getWorld().rayTraceBlocks(eyeLoc, direction,
                    player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5);
            if (result == null || result.getHitBlock().getType() != Material.OBSIDIAN) return;

            if (!result.getHitBlock().getLocation().equals(blockLoc)) return;

            plugin.spawnCrystal(entity.getLocation().clone(), player, item);
        }, null);
    }
}
