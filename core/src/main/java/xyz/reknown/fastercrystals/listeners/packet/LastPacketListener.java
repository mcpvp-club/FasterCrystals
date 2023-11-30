package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.enums.AnimPackets;
import xyz.reknown.fastercrystals.user.User;

public class LastPacketListener extends SimplePacketListenerAbstract {
    public LastPacketListener() {
        // Ensure AnimationListener runs first, see below
        super(PacketListenerPriority.MONITOR);
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        Player player = (Player) event.getPlayer();
        User user = plugin.getUsers().get(player);
        if (user == null) return;

        // Must clone for main thread usage
        PacketReceiveEvent copy = event.clone();

        // Run on main thread since the animation listener must happen (which runs on the main thread) before this one
        // We want it in this order: AnimationListener -> LastPacketListener so that AnimationListener can read the
        //     packet prior to this one
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean handled = false;

            if (copy.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(copy);
                if (wrapper.getAction() == DiggingAction.DROP_ITEM
                        || wrapper.getAction() == DiggingAction.DROP_ITEM_STACK) {
                    user.setLastPacket(AnimPackets.IGNORE);
                    handled = true;
                } else if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                    user.setLastPacket(AnimPackets.START_DIGGING);
                    handled = true;
                }
            } else if (copy.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                    || copy.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                user.setLastPacket(AnimPackets.IGNORE);
                handled = true;
            } else if (copy.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(copy);
                if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    user.setLastPacket(AnimPackets.ATTACK);
                    handled = true;
                }
            }

            if (!handled) user.setLastPacket(AnimPackets.MISC);

            // Avoid memory leaks
            copy.cleanUp();
        });
    }
}
