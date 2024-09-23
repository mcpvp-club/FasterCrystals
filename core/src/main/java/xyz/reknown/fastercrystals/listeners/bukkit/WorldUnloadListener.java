package xyz.reknown.fastercrystals.listeners.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;

public class WorldUnloadListener implements Listener {
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        plugin.getCrystalIds().entrySet().removeIf(entry -> entry.getValue().getWorld().equals(event.getWorld()));
    }
}
