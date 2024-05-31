/*
 * Copyright (C) 2023-2024 Jyguy
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

package xyz.reknown.fastercrystals.commands.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.commands.AbstractCommand;

public class FastercrystalsCommand extends AbstractCommand {
    public FastercrystalsCommand() {
        super("fastercrystals");
    }

    @Override
    public void register() {
        new CommandAPICommand(name)
                .withAliases("fastcrystals")
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("fastercrystals.reload")
                        .executesPlayer(this::runReload))
                .withOptionalArguments(new BooleanArgument("toggle"))
                .withPermission("fastercrystals.toggle")
                .executesPlayer(this::run)
                .register();
    }

    @Override
    public void run(Player player, CommandArguments args) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");

        boolean toggle = (boolean) args.getOptional(0)
                    .orElseGet(() -> pdc.has(key, PersistentDataType.BYTE) && pdc.get(key, PersistentDataType.BYTE) == 0);
        pdc.set(key, PersistentDataType.BYTE, (byte) (toggle ? 1 : 0x0));

        String stateKey = "state." + (toggle ? "on" : "off");
        String state = plugin.getConfig().getString(stateKey);
        String text = plugin.getConfig().getString("text");

        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(text, Placeholder.parsed("state", state));
        // relocating net.kyori.*, can't use sendMessage(Component)
        player.sendMessage(BungeeComponentSerializer.get().serialize(component));
    }

    public void runReload(Player player, CommandArguments args) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        plugin.reloadConfig();
        player.sendMessage(ChatColor.GREEN + "Reloaded FasterCrystals config!");
    }
}
