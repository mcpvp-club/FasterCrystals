package xyz.reknown.fastercrystals.pickable;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import xyz.reknown.fastercrystals.api.IPickableChecker;

public class PickableChecker_1_17_R1 implements IPickableChecker {
    @Override
    public boolean isPickable(Entity entity) {
        return ((CraftEntity) entity).getHandle().isPickable();
    }
}
