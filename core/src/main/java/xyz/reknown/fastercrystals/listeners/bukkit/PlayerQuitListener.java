package xyz.reknown.fastercrystals.listeners.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        plugin.getUsers().remove(event.getPlayer().getUniqueId());
    }
}
