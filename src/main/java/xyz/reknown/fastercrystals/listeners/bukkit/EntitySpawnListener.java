package xyz.reknown.fastercrystals.listeners.bukkit;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;

public class EntitySpawnListener implements Listener {
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.ENDER_CRYSTAL) {
            FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
            plugin.addCrystal(event.getEntity().getEntityId(), (EnderCrystal) event.getEntity());
        }
    }
}
