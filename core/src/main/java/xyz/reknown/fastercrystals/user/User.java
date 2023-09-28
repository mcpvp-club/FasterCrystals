package xyz.reknown.fastercrystals.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.enums.AnimPackets;

public class User {
    @Getter private final Player player;
    @Getter @Setter private AnimPackets lastPacket;

    public User(Player player) {
        this.player = player;
    }

    public boolean isFastCrystals() {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");
        return !pdc.has(key, PersistentDataType.BYTE) || pdc.get(key, PersistentDataType.BYTE) == 1;
    }
}
