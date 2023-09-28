package xyz.reknown.fastercrystals.user;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Users {
    private final Map<UUID, User> users = new HashMap<>();

    public void add(User user) {
        users.put(user.getPlayer().getUniqueId(), user);
    }

    @Nullable
    public User get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

    @Nullable
    public User get(@NotNull UUID uuid) {
        return users.get(uuid);
    }

    public void remove(@NotNull UUID uuid) {
        users.remove(uuid);
    }
}
