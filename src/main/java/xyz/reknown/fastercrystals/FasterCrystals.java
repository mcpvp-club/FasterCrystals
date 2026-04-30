/*
 * Copyright (C) 2023-2026 Jyguy
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
import lombok.Getter;
import org.bstats.bukkit.Metrics;
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
import xyz.reknown.fastercrystals.commands.FastercrystalsCommand;
import xyz.reknown.fastercrystals.event.FastCrystalPlaceEvent;
import xyz.reknown.fastercrystals.listener.bukkit.CrystalStateListener;
import xyz.reknown.fastercrystals.listener.bukkit.PlayerStateListener;
import xyz.reknown.fastercrystals.listener.packet.AnimationListener;
import xyz.reknown.fastercrystals.listener.packet.InteractEntityListener;
import xyz.reknown.fastercrystals.listener.packet.LastPacketListener;
import xyz.reknown.fastercrystals.papi.FasterCrystalsExpansion;
import xyz.reknown.fastercrystals.repository.CrystalRepository;
import xyz.reknown.fastercrystals.repository.UserRepository;
import xyz.reknown.fastercrystals.util.ConfigCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class FasterCrystals extends JavaPlugin {

    private static final int B_STATS_PLUGIN_ID = 22397;
    private static final Set<Material> AIR_TYPES = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    @Getter
    private static FasterCrystals instance;

    private UserRepository userRepository;
    private CrystalRepository crystalRepository;
    private ConfigCache configCache;

    private List<SimplePacketListenerAbstract> listeners;

    public FasterCrystals() {
        FasterCrystals.instance = this;
    }

    @Override
    public void onEnable() {
        FasterCrystalsAPI.init(this);
        saveDefaultConfig();

        this.configCache = new ConfigCache(this);
        this.userRepository = new UserRepository();
        this.crystalRepository = new CrystalRepository();

        FastercrystalsCommand command = new FastercrystalsCommand();
        getCommand("fastercrystals").setExecutor(command);
        getCommand("fastercrystals").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new CrystalStateListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerStateListener(), this);

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

        // Register any players that are already online (e.g. after a reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (userRepository.get(player) == null) {
                userRepository.add(player);
            }
        }

        new Metrics(this, B_STATS_PLUGIN_ID);
    }

    @Override
    public void onDisable() {
        FasterCrystalsAPI.shutdown();

        if (this.listeners != null) {
            for (SimplePacketListenerAbstract listener : this.listeners) {
                PacketEvents.getAPI().getEventManager().unregisterListener(listener);
            }
        }

        if (this.crystalRepository != null) {
            this.crystalRepository.clear();
        }
        if (this.userRepository != null) {
            this.userRepository.clear();
        }
    }

    /**
     * Reloads the plugin configuration and refreshes the cached config values.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        configCache.reload(this);
    }

    /**
     * Attempts to spawn a crystal at the given location on behalf of the specified player.
     * <p>
     * Fires a {@link FastCrystalPlaceEvent} before spawning. If the event is cancelled,
     * the crystal will not be placed.
     *
     * @param loc    the target location (block top)
     * @param player the player placing the crystal
     * @param item   the crystal item stack to consume from
     */
    public void spawnCrystal(Location loc, Player player, ItemStack item) {
        Location blockLoc = loc.clone().subtract(0.5, 0.0, 0.5);
        if (!AIR_TYPES.contains(blockLoc.getBlock().getType())) return;

        Location spawnLoc = blockLoc.add(0.5, 1.0, 0.5);
        List<Entity> nearbyEntities = new ArrayList<>(spawnLoc.getWorld().getNearbyEntities(spawnLoc, 0.5, 1, 0.5,
                entity -> !(entity instanceof Player p) || p.getGameMode() != GameMode.SPECTATOR));

        if (nearbyEntities.isEmpty()) {
            FastCrystalPlaceEvent placeEvent = new FastCrystalPlaceEvent(player, spawnLoc.clone(), item.clone());
            Bukkit.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled()) return;

            loc.getWorld().spawn(spawnLoc.subtract(0.0, 1.0, 0.0), EnderCrystal.class,
                    entity -> entity.setShowingBottom(false));

            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }
}