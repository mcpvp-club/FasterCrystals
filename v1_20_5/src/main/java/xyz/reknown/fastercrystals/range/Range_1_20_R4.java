package xyz.reknown.fastercrystals.range;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.IRange;

public class Range_1_20_R4 implements IRange {
    @Override
    public double block(Player player) {
        return player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).getValue();
    }

    @Override
    public double entity(Player player) {
        return player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).getValue();
    }
}
