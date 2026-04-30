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

package xyz.reknown.fastercrystals.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.api.FasterCrystalsAPI;
import xyz.reknown.fastercrystals.util.ConfigCache;

import java.util.List;
import java.util.Set;

public class FastercrystalsCommand implements CommandExecutor, TabCompleter {
    private static final Set<String> ON_STRINGS = Set.of("true", "on");
    private static final Set<String> OFF_STRINGS = Set.of("false", "off");

    private final FasterCrystals plugin;
    private final FasterCrystalsAPI api;

    public FastercrystalsCommand() {
        this.plugin = FasterCrystals.getInstance();
        this.api = FasterCrystalsAPI.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }

        if (!sender.hasPermission("fastercrystals.toggle")) {
            sender.sendMessage(Component.text("You do not have permissions to do this!", NamedTextColor.RED));
            return true;
        }

        boolean toggle;
        if (args.length > 0) {
            String toggleStr = args[0].toLowerCase();
            if (ON_STRINGS.contains(toggleStr)) {
                toggle = true;
            } else if (OFF_STRINGS.contains(toggleStr)) {
                toggle = false;
            } else {
                sender.sendMessage(Component.text("Invalid input: " + toggleStr, NamedTextColor.RED));
                return true;
            }
        } else {
            toggle = !api.isFastCrystalsEnabled(player);
        }

        api.setFastCrystals(player, toggle);
        sendToggleMessage(player, toggle);
        return true;
    }

    /**
     * Handles the reload subcommand, checking permissions and reloading the config.
     *
     * @param sender the command sender
     * @return always true
     */
    private boolean handleReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("fastercrystals.reload")) {
            sender.sendMessage(Component.text("You do not have permissions to do this!", NamedTextColor.RED));
            return true;
        }

        plugin.reloadPluginConfig();
        sender.sendMessage(Component.text("Reloaded FasterCrystals config!", NamedTextColor.GREEN));
        return true;
    }

    /**
     * Sends the toggle state message to the player using the cached config values.
     *
     * @param player the player to send the message to
     * @param state  the new toggle state
     */
    private void sendToggleMessage(@NotNull Player player, boolean state) {
        ConfigCache configCache = plugin.getConfigCache();
        String stateText = state ? configCache.getStateOnText() : configCache.getStateOffText();
        String text = configCache.getToggleText();

        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(text, Placeholder.parsed("state", stateText));
        player.sendMessage(component);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) return List.of();

        String input = args[0].toLowerCase();
        return List.of("reload", "on", "off").stream()
                .filter(s -> s.startsWith(input))
                .toList();
    }
}