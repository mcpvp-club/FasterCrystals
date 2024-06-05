package xyz.reknown.fastercrystals.range;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.IRange;

public class Range_1_17_R1 implements IRange {
    @Override
    public double block(Player player) {
        return player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5;
    }

    @Override
    public double entity(Player player) {
        return player.getGameMode() == GameMode.CREATIVE ? 5.0 : 3.0;
    }
}
