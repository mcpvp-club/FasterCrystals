package xyz.reknown.fastercrystals.damager;

import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.ICrystalDamager;

public class CrystalDamager_1_19_R1 implements ICrystalDamager {
    @Override
    public void damage(Entity entity, Player player) {
        ((CraftEnderCrystal) entity).getHandle().hurt(
                DamageSource.playerAttack(((CraftPlayer) player).getHandle()),
                1
        );
    }
}
