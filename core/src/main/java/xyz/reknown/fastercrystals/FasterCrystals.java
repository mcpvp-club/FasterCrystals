/*
 * Copyright (C) 2023-2024 Jyguy
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
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
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
import xyz.reknown.fastercrystals.api.ICrystalDamager;
import xyz.reknown.fastercrystals.api.IPickableChecker;
import xyz.reknown.fastercrystals.api.IRange;
import xyz.reknown.fastercrystals.bstats.Metrics;
import xyz.reknown.fastercrystals.commands.impl.FastercrystalsCommand;
import xyz.reknown.fastercrystals.listeners.bukkit.EntityRemoveFromWorldListener;
import xyz.reknown.fastercrystals.listeners.bukkit.EntitySpawnListener;
import xyz.reknown.fastercrystals.listeners.bukkit.PlayerJoinListener;
import xyz.reknown.fastercrystals.listeners.bukkit.PlayerQuitListener;
import xyz.reknown.fastercrystals.listeners.packet.AnimationListener;
import xyz.reknown.fastercrystals.listeners.packet.InteractEntityListener;
import xyz.reknown.fastercrystals.listeners.packet.LastPacketListener;
import xyz.reknown.fastercrystals.user.Users;
import xyz.reknown.fastercrystals.ver.damager.*;
import xyz.reknown.fastercrystals.ver.pickable.*;
import xyz.reknown.fastercrystals.ver.range.Range_1_17_R1;
import xyz.reknown.fastercrystals.ver.range.Range_1_20_R4;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FasterCrystals extends JavaPlugin {
    @Getter private ICrystalDamager damager;
    @Getter private IPickableChecker pickableChecker;
    @Getter private IRange range;
    @Getter private Users users;
    private Map<Integer, EnderCrystal> crystalIds;

    private final Set<Material> AIR_TYPES = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .reEncodeByDefault(false);
        PacketEvents.getAPI().load();

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .missingExecutorImplementationMessage("Only players can run this command."));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        range = new Range_1_17_R1();

        switch (Bukkit.getMinecraftVersion()) {
            case "1.21.1": case "1.21":
                damager = new CrystalDamager_1_21_R1();
                pickableChecker = new PickableChecker_1_21_R1();
                range = new Range_1_20_R4();
                break;
            case "1.20.6": case "1.20.5":
                damager = new CrystalDamager_1_20_R4();
                pickableChecker = new PickableChecker_1_20_R4();
                range = new Range_1_20_R4();
                break;
            case "1.20.4": case "1.20.3":
                damager = new CrystalDamager_1_20_R3();
                pickableChecker = new PickableChecker_1_20_R3();
                break;
            case "1.20.2":
                damager = new CrystalDamager_1_20_R2();
                pickableChecker = new PickableChecker_1_20_R2();
                break;
            case "1.20.1": case "1.20":
                damager = new CrystalDamager_1_20_R1();
                pickableChecker = new PickableChecker_1_20_R1();
                break;
            case "1.19.4":
                damager = new CrystalDamager_1_19_R3();
                pickableChecker = new PickableChecker_1_19_R3();
                break;
            case "1.19.3":
                damager = new CrystalDamager_1_19_R2();
                pickableChecker = new PickableChecker_1_19_R2();
                break;
            case "1.19.2": case "1.19.1": case "1.19":
                damager = new CrystalDamager_1_19_R1();
                pickableChecker = new PickableChecker_1_19_R1();
                break;
            case "1.18.2":
                damager = new CrystalDamager_1_18_R2();
                pickableChecker = new PickableChecker_1_18_R2();
                break;
            case "1.18.1": case "1.18":
                damager = new CrystalDamager_1_18_R1();
                pickableChecker = new PickableChecker_1_18_R1();
                break;
            case "1.17.1":
                damager = new CrystalDamager_1_17_R1();
                pickableChecker = new PickableChecker_1_17_R1();
                break;
            default:
                throw new RuntimeException("Invalid server version! FasterCrystals supports 1.17.1 - 1.21.1");
        }

        this.crystalIds = FoliaScheduler.isFolia() ? new ConcurrentHashMap<>() : new HashMap<>();

        this.users = new Users();

        CommandAPI.onEnable();
        new FastercrystalsCommand().register();

        getServer().getPluginManager().registerEvents(new EntityRemoveFromWorldListener(), this);
        getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        PacketEvents.getAPI().getEventManager().registerListener(new AnimationListener());
        PacketEvents.getAPI().getEventManager().registerListener(new InteractEntityListener());
        PacketEvents.getAPI().getEventManager().registerListener(new LastPacketListener());
        PacketEvents.getAPI().init();

        int pluginId = 22397;
        new Metrics(this, pluginId);
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
        if (!AIR_TYPES.contains(clonedLoc.getBlock().getType())) return;

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
