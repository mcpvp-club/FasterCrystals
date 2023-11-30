package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
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

        AnimPackets animPacket = getAnimPacket(event);

        // Dropping order is ANIMATION -> WINDOW_CLICK (unlike other actions where ANIMATION is last)
        // So, need to ensure ANIMATION is not processed in the *main thread* if the following packet is inv dropping
        // Even if this is MONITOR priority, it will run before the main thread task in AnimationListener
        if (user.getLastPacket() == AnimPackets.ANIMATION) {
            // If the following packet is NOT inv dropping, then don't ignore anim
            user.setIgnoreAnim(animPacket == AnimPackets.INV_DROP);
        }

        // Still required for other actions (e.g. dropping without inventory)
        user.setLastPacket(animPacket);
    }

    private AnimPackets getAnimPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            return AnimPackets.ANIMATION;
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            if (wrapper.getAction() == DiggingAction.DROP_ITEM
                    || wrapper.getAction() == DiggingAction.DROP_ITEM_STACK) {
                return AnimPackets.IGNORE;
            } else if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                return AnimPackets.START_DIGGING;
            }
        } else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            // drop item w/ Q OR clicks outside inventory
            if (wrapper.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.THROW ||
                    (wrapper.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP
                            && wrapper.getSlot() == -999)) {
                return AnimPackets.INV_DROP;
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            return AnimPackets.IGNORE;
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return AnimPackets.ATTACK;
            }
        }

        return AnimPackets.MISC;
    }
}
