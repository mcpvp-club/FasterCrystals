package xyz.reknown.fastercrystals;

import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.commands.impl.FastcrystalsCommand;
import xyz.reknown.fastercrystals.listeners.bukkit.EntityRemoveFromWorldListener;
import xyz.reknown.fastercrystals.listeners.bukkit.EntitySpawnListener;
import xyz.reknown.fastercrystals.listeners.bukkit.PlayerJoinListener;
import xyz.reknown.fastercrystals.listeners.bukkit.PlayerQuitListener;
import xyz.reknown.fastercrystals.listeners.packet.CrystalPacketListener;
import xyz.reknown.fastercrystals.user.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FasterCrystals extends JavaPlugin {
    @Getter private Users users;
    private Map<Integer, EnderCrystal> crystalIds;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        this.crystalIds = new HashMap<>();

        this.users = new Users();

        CommandAPI.onEnable();
        new FastcrystalsCommand().register();

        getServer().getPluginManager().registerEvents(new EntityRemoveFromWorldListener(), this);
        getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        PacketEvents.getAPI().getEventManager().registerListener(new CrystalPacketListener());
        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        CommandAPI.onDisable();
    }

    public void addCrystal(int entityId, EnderCrystal entity) {
        crystalIds.put(entityId, entity);
    }

    public EnderCrystal getCrystal(int entityId) {
        return crystalIds.get(entityId);
    }

    public void removeCrystal(int entityId) {
        crystalIds.remove(entityId);
    }

    public void spawnCrystal(Location loc, Player player, ItemStack item) {
        Location clonedLoc = loc.clone().subtract(0.5, 0.0, 0.5);
        if (clonedLoc.getBlock().getType() != Material.AIR) return;

        clonedLoc.add(0.5, 1.0, 0.5);
        List<Entity> nearbyEntities = new ArrayList<>(clonedLoc.getWorld().getNearbyEntities(clonedLoc, 0.5, 1, 0.5));

        if (nearbyEntities.isEmpty()) {
            loc.getWorld().spawn(clonedLoc.subtract(0.0, 1.0, 0.0), EnderCrystal.class, entity -> entity.setShowingBottom(false));

            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }
}
