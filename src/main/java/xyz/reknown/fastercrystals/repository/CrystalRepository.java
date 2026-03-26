package xyz.reknown.fastercrystals.repository;

import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class CrystalRepository {
    private final ConcurrentHashMap<Integer, EnderCrystal> crystalIds = new ConcurrentHashMap<>();

    public void add(@NotNull EnderCrystal crystal) {
        crystalIds.put(crystal.getEntityId(), crystal);
    }

    public @Nullable EnderCrystal get(int id) {
        return crystalIds.get(id);
    }

    public void remove(int id) {
        crystalIds.remove(id);
    }

    public void unloadWorldCrystals(@NotNull World world) {
        crystalIds.entrySet().removeIf(entry -> entry.getValue().getWorld().equals(world));
    }
}
