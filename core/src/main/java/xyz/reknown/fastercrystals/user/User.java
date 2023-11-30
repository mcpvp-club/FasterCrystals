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

@Getter
public class User {
    private final Player player;
    @Setter private AnimPackets lastPacket;
    @Setter private boolean ignoreAnim;

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
