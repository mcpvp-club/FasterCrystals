package xyz.reknown.fastercrystals.listeners.bukkit;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
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
            // add delay so that it is detected as a crystal when interact happens after destruction
            FoliaScheduler.getGlobalRegionScheduler().runDelayed(plugin,
                    task -> plugin.removeCrystal(event.getEntity().getEntityId()), 40L);
        }
    }
}
