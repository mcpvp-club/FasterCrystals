package xyz.reknown.fastercrystals.damager;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.ICrystalDamager;

public class CrystalDamager_1_20_R2 implements ICrystalDamager {
    @Override
    public void damage(Entity entity, Player player) {
        ((CraftEnderCrystal) entity).getHandle().hurt(new DamageSource(
                        Holder.direct(new DamageType("player", 0.1f)),
                        ((CraftPlayer) player).getHandle()),
                1
        );
    }
}
