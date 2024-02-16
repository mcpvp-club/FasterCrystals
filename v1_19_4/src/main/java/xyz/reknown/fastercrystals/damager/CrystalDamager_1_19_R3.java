package xyz.reknown.fastercrystals.damager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSources;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.reknown.fastercrystals.api.ICrystalDamager;

public class CrystalDamager_1_19_R3 implements ICrystalDamager {
    @Override
    public void damage(Entity entity, Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        DamageSources damageSources = serverPlayer.level.damageSources();
        ((CraftEnderCrystal) entity).getHandle().hurt(damageSources.playerAttack(serverPlayer), 1);
    }
}
