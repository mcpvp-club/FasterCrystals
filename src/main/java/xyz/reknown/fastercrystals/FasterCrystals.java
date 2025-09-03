/*
 * Copyright (C) 2023-2025 Jyguy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.reknown.fastercrystals;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.api.FasterCrystalsAPI;
import xyz.reknown.fastercrystals.bstats.Metrics;
import xyz.reknown.fastercrystals.commands.FastercrystalsCommand;
import xyz.reknown.fastercrystals.listeners.bukkit.*;
import xyz.reknown.fastercrystals.listeners.packet.AnimationListener;
import xyz.reknown.fastercrystals.listeners.packet.InteractEntityListener;
import xyz.reknown.fastercrystals.listeners.packet.LastPacketListener;
import xyz.reknown.fastercrystals.papi.FasterCrystalsExpansion;
import xyz.reknown.fastercrystals.user.Users;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class FasterCrystals extends JavaPlugin {
    private List<SimplePacketListenerAbstract> listeners;
    private Users users;
    private Map<Integer, EnderCrystal> crystalIds;

    private static final Set<Material> AIR_TYPES = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    @Override
    public void onEnable() {
        FasterCrystalsAPI.init(this);
        saveDefaultConfig();

        this.crystalIds = FoliaScheduler.isFolia() ? new ConcurrentHashMap<>() : new HashMap<>();
        this.users = new Users();

        FastercrystalsCommand command = new FastercrystalsCommand(this);
        getCommand("fastercrystals").setExecutor(command);
        getCommand("fastercrystals").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new EntityRemoveFromWorldListener(), this);
        getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new WorldUnloadListener(), this);

        this.listeners = List.of(
                new AnimationListener(),
                new InteractEntityListener(),
                new LastPacketListener()
        );
        for (SimplePacketListenerAbstract listener : listeners) {
            PacketEvents.getAPI().getEventManager().registerListener(listener);
        }

        // Register PlaceholderAPI expansions
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new FasterCrystalsExpansion().register();
        }

        int pluginId = 22397;
        new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        for (SimplePacketListenerAbstract listener : this.listeners) {
            PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        }
    }

    public void spawnCrystal(Location loc, Player player, ItemStack item) {
        Location clonedLoc = loc.clone().subtract(0.5, 0.0, 0.5);
        if (!AIR_TYPES.contains(clonedLoc.getBlock().getType())) return;

        clonedLoc.add(0.5, 1.0, 0.5);
        List<Entity> nearbyEntities = new ArrayList<>(clonedLoc.getWorld().getNearbyEntities(clonedLoc, 0.5, 1, 0.5,
                entity -> !(entity instanceof Player p) || p.getGameMode() != GameMode.SPECTATOR));

        if (nearbyEntities.isEmpty()) {
            loc.getWorld().spawn(clonedLoc.subtract(0.0, 1.0, 0.0), EnderCrystal.class, entity -> entity.setShowingBottom(false));

            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }
}
