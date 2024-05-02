package xyz.reknown.fastercrystals.listeners.bukkit;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.retrooper.packetevents.util.FoliaCompatUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;

public class EntityRemoveFromWorldListener implements Listener {
    @EventHandler
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        if (event.getEntityType() == EntityType.ENDER_CRYSTAL) {
            FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
            FoliaCompatUtil.runTaskForEntity(event.getEntity(), plugin,
                    () -> plugin.removeCrystal(event.getEntity().getEntityId()),
                    () -> plugin.removeCrystal(event.getEntity().getEntityId()), // TODO wait for PE's improved folia util
                    40L); // add delay so that it is detected as a crystal when interact happens after destruction
        }
    }
}
