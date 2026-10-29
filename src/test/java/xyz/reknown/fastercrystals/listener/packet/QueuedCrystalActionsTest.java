/*
 * Copyright (C) 2023-2026 Jyguy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.reknown.fastercrystals.listener.packet;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.folia.EntityScheduler;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.RegionScheduler;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.api.FasterCrystalsAPI;
import xyz.reknown.fastercrystals.repository.CrystalRepository;
import xyz.reknown.fastercrystals.repository.UserRepository;
import xyz.reknown.fastercrystals.user.CUser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueuedCrystalActionsTest {
    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        CraftRegistry.setMinecraftRegistry(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }

    private final FasterCrystals plugin = mock(FasterCrystals.class);
    private final Player player = mock(Player.class);
    private final World world = mock(World.class);
    private final EnderCrystal crystal = mock(EnderCrystal.class);
    private final EntityScheduler entityScheduler = mock(EntityScheduler.class);
    private final RegionScheduler regionScheduler = mock(RegionScheduler.class);
    private final Deque<Consumer<Object>> queuedTasks = new ArrayDeque<>();

    private MockedStatic<FasterCrystals> pluginStatic;
    private MockedStatic<FasterCrystalsAPI> apiStatic;
    private MockedStatic<FoliaScheduler> schedulerStatic;
    private MockedStatic<Bukkit> bukkitStatic;
    private CUser user;
    private AnimationListener animationListener;
    private InteractEntityListener interactListener;
    private LastPacketListener lastPacketListener;
    private Location eyeLocation;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        pluginStatic = mockStatic(FasterCrystals.class);
        pluginStatic.when(FasterCrystals::getInstance).thenReturn(plugin);
        when(plugin.getName()).thenReturn("FasterCrystals");
        apiStatic = mockStatic(FasterCrystalsAPI.class);
        apiStatic.when(FasterCrystalsAPI::isAvailable).thenReturn(true);
        bukkitStatic = mockStatic(Bukkit.class);
        bukkitStatic.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class))).thenReturn(true);
        schedulerStatic = mockStatic(FoliaScheduler.class);
        schedulerStatic.when(FoliaScheduler::isFolia).thenReturn(true);
        schedulerStatic.when(FoliaScheduler::getEntityScheduler).thenReturn(entityScheduler);
        schedulerStatic.when(FoliaScheduler::getRegionScheduler).thenReturn(regionScheduler);

        doAnswer(invocation -> {
            queuedTasks.add(invocation.getArgument(2));
            return null;
        }).when(entityScheduler).run(eq(player), eq(plugin), any(), isNull());
        doAnswer(invocation -> {
            queuedTasks.add(invocation.getArgument(2));
            return null;
        }).when(regionScheduler).run(eq(plugin), any(Location.class), any());

        UserRepository users = mock(UserRepository.class);
        CrystalRepository crystals = mock(CrystalRepository.class);
        when(plugin.getUserRepository()).thenReturn(users);
        when(plugin.getCrystalRepository()).thenReturn(crystals);
        user = spy(new CUser(player));
        doReturn(true).when(user).isFasterCrystals();
        when(users.get(player)).thenReturn(user);
        when(crystals.get(42)).thenReturn(crystal);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.isOnline()).thenReturn(true);
        eyeLocation = new Location(world, 0.5, 65.62, 0.5);
        when(player.getEyeLocation()).thenAnswer(invocation -> eyeLocation.clone());

        AttributeInstance range = mock(AttributeInstance.class);
        when(range.getValue()).thenReturn(4.5);
        when(player.getAttribute(any(Attribute.class))).thenReturn(range);
        when(crystal.getType()).thenReturn(EntityType.END_CRYSTAL);
        when(crystal.getTicksLived()).thenReturn(1);
        when(crystal.getBoundingBox()).thenReturn(new BoundingBox(-0.5, 65, -0.5, 1.5, 67, 1.5));
        when(crystal.getLocation()).thenAnswer(invocation -> new Location(world, 0.5, 65, 0.5));
        when(world.rayTraceEntities(any(Location.class), any(Vector.class), anyDouble(), eq(0.0), any(Predicate.class)))
                .thenReturn(new RayTraceResult(new Vector(0.5, 65.62, 0.5), crystal));

        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.END_CRYSTAL);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getItemInMainHand()).thenReturn(item);
        when(inventory.getItemInOffHand()).thenReturn(item);
        when(player.getInventory()).thenReturn(inventory);
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.OBSIDIAN);
        when(block.getLocation()).thenReturn(new Location(world, 0, 64, 0));
        when(world.rayTraceBlocks(any(Location.class), any(Vector.class), anyDouble()))
                .thenReturn(new RayTraceResult(new Vector(0.5, 65, 0.5), block, null));

        animationListener = new AnimationListener();
        interactListener = new InteractEntityListener();
        lastPacketListener = new LastPacketListener();
    }

    @AfterEach
    void tearDown() {
        if (schedulerStatic != null) schedulerStatic.close();
        if (bukkitStatic != null) bukkitStatic.close();
        if (apiStatic != null) apiStatic.close();
        if (pluginStatic != null) pluginStatic.close();
    }

    @Test
    void laterInventoryDropDoesNotSuppressEarlierAttack() {
        receiveAnimation(false);
        receiveMisc();
        receiveAnimation(false);
        receiveDrop();

        runNextTask();
        verify(player).attack(crystal);
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void laterAttackDoesNotEnableEarlierInventoryDrop() {
        receiveAnimation(false);
        receiveDrop();
        receiveAnimation(false);
        receiveMisc();

        runNextTask();
        verify(player, never()).attack(any());
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void consecutiveAttacksHaveIndependentDropSuppression() {
        receiveAnimation(false);
        receiveAnimation(false);
        receiveDrop();

        runNextTask();
        verify(player).attack(crystal);
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void cancelledAnimationDoesNotRedirectDropToEarlierAttack() {
        receiveAnimation(false);
        receiveMisc();
        receiveAnimation(true);
        receiveDrop();

        assertEquals(1, queuedTasks.size());
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void disabledAnimationDoesNotRedirectDropToEarlierAttack() {
        receiveAnimation(false);
        receiveMisc();
        doReturn(false).when(user).isFasterCrystals();
        receiveAnimation(false);
        receiveDrop();

        assertEquals(1, queuedTasks.size());
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void spectatorAnimationDoesNotRedirectDropToEarlierAttack() {
        receiveAnimation(false);
        receiveMisc();
        when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        receiveAnimation(false);
        receiveDrop();

        assertEquals(1, queuedTasks.size());
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void creativeInventoryActionDoesNotSuppressEarlierAttack() {
        receiveAnimation(false);
        receiveMisc();
        receiveAnimation(false);
        lastPacketListener.onPacketPlayReceive(packet(PacketType.Play.Client.CREATIVE_INVENTORY_ACTION));

        runNextTask();
        verify(player).attack(crystal);
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void animationFollowsPlayerScheduler() {
        receiveAnimation(false);
        verify(entityScheduler).run(eq(player), eq(plugin), any(), isNull());
        verifyNoInteractions(regionScheduler);
    }

    @Test
    void animationSkipsOriginOutsideCurrentRegion() {
        receiveAnimation(false);
        receiveMisc();
        bukkitStatic.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class))).thenReturn(false);
        runNextTask();
        verify(player, never()).attack(any());
        verify(world, never()).rayTraceEntities(any(), any(), anyDouble(), anyDouble(), any());
    }

    @Test
    void animationSkipsWorldChange() {
        receiveAnimation(false);
        eyeLocation = new Location(mock(World.class), 0.5, 65.62, 0.5);
        runNextTask();
        verify(player, never()).attack(any());
    }

    @Test
    void animationAllowsSmallMovementWithinOwnedRegion() {
        receiveAnimation(false);
        eyeLocation.add(0.1, 0, 0);
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void animationSkipsDistantTeleportWithinSameWorld() {
        receiveAnimation(false);
        eyeLocation.add(100, 0, 0);
        runNextTask();
        verify(player, never()).attack(any());
    }

    @Test
    void paperDoesNotRequireFoliaRegionOwnership() {
        schedulerStatic.when(FoliaScheduler::isFolia).thenReturn(false);
        bukkitStatic.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class))).thenReturn(false);
        receiveAnimation(false);
        runNextTask();
        verify(player).attack(crystal);
    }

    @Test
    void animationSkipsDisconnectedPlayer() {
        receiveAnimation(false);
        when(player.isOnline()).thenReturn(false);
        runNextTask();
        verify(player, never()).attack(any());
    }

    @Test
    void interactionFollowsPlayerScheduler() {
        receiveInteraction();
        verify(entityScheduler).run(eq(player), eq(plugin), any(), isNull());
        verifyNoInteractions(regionScheduler);
    }

    @Test
    void interactionSkipsWorldChangeBeforeReadingInventory() {
        receiveInteraction();
        eyeLocation = new Location(mock(World.class), 0.5, 65.62, 0.5);
        runNextTask();
        verify(player, never()).getInventory();
        verify(plugin, never()).spawnCrystal(any(), any(), any());
    }

    @Test
    void interactionSkipsOriginOutsideCurrentRegion() {
        receiveInteraction();
        bukkitStatic.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class))).thenReturn(false);
        runNextTask();
        verify(player, never()).getInventory();
        verify(plugin, never()).spawnCrystal(any(), any(), any());
    }

    @Test
    void interactionAllowsSmallMovementWithinOwnedRegion() {
        receiveInteraction();
        eyeLocation.add(0.1, 0, 0);
        runNextTask();
        verify(plugin).spawnCrystal(eq(new Location(world, 0.5, 65, 0.5)), eq(player), any());
    }

    @Test
    void interactionSkipsDistantTeleportBeforeReadingInventory() {
        receiveInteraction();
        eyeLocation.add(100, 0, 0);
        runNextTask();
        verify(player, never()).getInventory();
        verify(plugin, never()).spawnCrystal(any(), any(), any());
    }

    @Test
    void interactionSkipsDisconnectedPlayer() {
        receiveInteraction();
        when(player.isOnline()).thenReturn(false);
        runNextTask();
        verify(player, never()).getInventory();
        verify(plugin, never()).spawnCrystal(any(), any(), any());
    }

    private PacketPlayReceiveEvent packet(PacketType.Play.Client type) {
        PacketPlayReceiveEvent event = mock(PacketPlayReceiveEvent.class);
        when(event.getPacketType()).thenReturn(type);
        when(event.getPlayer()).thenReturn(player);
        return event;
    }

    private void receiveAnimation(boolean cancelled) {
        PacketPlayReceiveEvent event = packet(PacketType.Play.Client.ANIMATION);
        when(event.isCancelled()).thenReturn(cancelled);
        animationListener.onPacketPlayReceive(event);
        lastPacketListener.onPacketPlayReceive(event);
    }

    private void receiveMisc() {
        lastPacketListener.onPacketPlayReceive(packet(PacketType.Play.Client.KEEP_ALIVE));
    }

    private void receiveDrop() {
        try (MockedConstruction<WrapperPlayClientClickWindow> ignored = mockConstruction(
                WrapperPlayClientClickWindow.class, (wrapper, context) ->
                        when(wrapper.getWindowClickType()).thenReturn(WrapperPlayClientClickWindow.WindowClickType.THROW))) {
            lastPacketListener.onPacketPlayReceive(packet(PacketType.Play.Client.CLICK_WINDOW));
        }
    }

    private void receiveInteraction() {
        try (MockedConstruction<WrapperPlayClientInteractEntity> ignored = mockConstruction(
                WrapperPlayClientInteractEntity.class, (wrapper, context) -> {
                    when(wrapper.getAction()).thenReturn(WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT);
                    when(wrapper.getEntityId()).thenReturn(42);
                    when(wrapper.getHand()).thenReturn(InteractionHand.MAIN_HAND);
                })) {
            interactListener.onPacketPlayReceive(packet(PacketType.Play.Client.INTERACT_ENTITY));
        }
    }

    private void runNextTask() {
        queuedTasks.removeFirst().accept(null);
    }
}
