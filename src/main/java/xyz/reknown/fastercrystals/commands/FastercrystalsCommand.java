/*
 * Copyright (C) 2023-2025 Jyguy
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

package xyz.reknown.fastercrystals.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.reknown.fastercrystals.FasterCrystals;

import java.util.List;
import java.util.Set;

public class FastercrystalsCommand implements CommandExecutor, TabCompleter {
    private static final Set<String> ON_STRINGS = Set.of("true", "on");
    private static final Set<String> OFF_STRINGS = Set.of("false", "off");

    private final FasterCrystals plugin;

    public FastercrystalsCommand(FasterCrystals plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("reload")) {
                if (!sender.hasPermission("fastercrystals.reload")) {
                    sender.sendMessage(Component.text("You do not have permissions to do this!", NamedTextColor.RED));
                    return true;
                }

                plugin.reloadConfig();
                sender.sendMessage(Component.text("Reloaded FasterCrystals config!", NamedTextColor.GREEN));
                return true;
            }
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }

        if (!sender.hasPermission("fastercrystals.toggle")) {
            sender.sendMessage(Component.text("You do not have permissions to do this!", NamedTextColor.RED));
            return true;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");

        boolean toggle;
        if (args.length > 0) {
            String toggleStr = args[0].toLowerCase();
            if (ON_STRINGS.contains(toggleStr)) toggle = true;
            else if (OFF_STRINGS.contains(toggleStr)) toggle = false;
            else {
                sender.sendMessage(Component.text("Invalid input: " + toggleStr, NamedTextColor.RED));
                return true;
            }
        } else {
            toggle = pdc.getOrDefault(key, PersistentDataType.BYTE, (byte) 1) == 0;
        }

        pdc.set(key, PersistentDataType.BYTE, (byte) (toggle ? 1 : 0x0));

        String stateKey = "state." + (toggle ? "on" : "off");
        String state = plugin.getConfig().getString(stateKey);
        String text = plugin.getConfig().getString("text");

        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(text, Placeholder.parsed("state", state));
        player.sendMessage(component);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("reload", "on", "off");
    }
}
