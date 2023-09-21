package xyz.reknown.fastercrystals.listeners.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import xyz.reknown.fastercrystals.FasterCrystals;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CrystalPacketListener extends PacketListenerAbstract {
    private final Set<UUID> ignoreAnim;

    public CrystalPacketListener() {
        super(PacketListenerPriority.NORMAL);

        this.ignoreAnim = new HashSet<>();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

            FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");
            if (pdc.has(key) && !pdc.get(key, PersistentDataType.BOOLEAN)) return;

            ItemStack item;
            if (wrapper.getHand() == InteractionHand.MAIN_HAND) item = player.getInventory().getItemInMainHand();
            else item = player.getInventory().getItemInOffHand();

            if (item.getType() != Material.END_CRYSTAL) return;

            int entityId = wrapper.getEntityId();
            EnderCrystal entity = plugin.getCrystal(entityId);
            if (entity == null) return;

            Location blockLoc = entity.getLocation().clone().subtract(0.5, 1.0, 0.5);

            RayTraceResult result = player.rayTraceBlocks(player.getGameMode() == GameMode.CREATIVE ? 5.0 : 4.5,
                    FluidCollisionMode.NEVER);
            if (result == null || result.getHitBlock().getType() != Material.OBSIDIAN) return;
            if (!result.getHitBlock().getLocation().equals(blockLoc)) return;

            Bukkit.getScheduler().runTask(plugin, () -> plugin.spawnCrystal(entity.getLocation().clone(), player, item));
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (this.ignoreAnim.contains(player.getUniqueId())) return; // animation is for drop item/placement
            if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) return; // ignore weakness hits, tool hits are slow anyway

            FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");
            if (pdc.has(key) && !pdc.get(key, PersistentDataType.BOOLEAN)) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                RayTraceResult result = player.getWorld().rayTraceEntities(
                        player.getEyeLocation(),
                        player.getLocation().getDirection(),
                        3.0,
                        0.0,
                        entity -> {
                            if (entity.getType() != EntityType.PLAYER) return true;

                            Player p = (Player) entity;
                            return !player.getUniqueId().equals(p.getUniqueId()) && player.canSee(p);
                        }
                );
                if (result == null) return;

                Entity entity = result.getHitEntity();
                if (entity == null || entity.getType() != EntityType.ENDER_CRYSTAL) return;

                ((CraftEnderCrystal) entity).getHandle().hurt(new DamageSource(
                                Holder.direct(new DamageType("player", 0.1f)),
                                ((CraftPlayer) player).getHandle()),
                        1
                );
            });
        }

        // Listen to specific packets to ignore Animation packet for
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            if (wrapper.getAction() == DiggingAction.DROP_ITEM
                    || wrapper.getAction() == DiggingAction.DROP_ITEM_STACK) {
                this.ignoreAnim.add(player.getUniqueId());
            } else this.ignoreAnim.remove(player.getUniqueId());
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            this.ignoreAnim.add(player.getUniqueId());
        } else if (player != null) this.ignoreAnim.remove(player.getUniqueId());
    }
}
