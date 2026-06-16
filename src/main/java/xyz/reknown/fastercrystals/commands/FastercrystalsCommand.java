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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import xyz.reknown.fastercrystals.FasterCrystals;

import java.util.List;

public final class FastercrystalsCommand {
    private FastercrystalsCommand() {}

    public static LiteralCommandNode<CommandSourceStack> createCommand(FasterCrystals plugin) {
        final NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");
        final MiniMessage mm = MiniMessage.miniMessage();

        return Commands.literal("fastercrystals")
                .requires(src -> src.getSender().hasPermission("fastercrystals.toggle")
                        || src.getSender().hasPermission("fastercrystals.reload"))
                .executes(ctx -> defaultToggle(ctx, plugin, key, mm))
                .then(Commands.literal("reload")
                        .requires(src -> src.getSender().hasPermission("fastercrystals.reload"))
                        .executes(ctx -> reload(ctx, plugin)))
                .then(Commands.argument("state", StringArgumentType.word())
                        .requires(src -> src.getSender() instanceof Player
                                && src.getSender().hasPermission("fastercrystals.toggle"))
                        .suggests((ctx, builder) -> {
                            for (String s : List.of("on", "off", "true", "false"))
                                if (s.startsWith(builder.getRemainingLowerCase())) builder.suggest(s);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            boolean value = parseBoolean(StringArgumentType.getString(ctx, "state"));
                            return apply(player, value, plugin, key, mm);
                        }))
                .build();
    }

    private static int reload(CommandContext<CommandSourceStack> ctx, FasterCrystals plugin) {
        plugin.reloadConfig();
        ctx.getSource().getSender().sendMessage(
                Component.text("Reloaded FasterCrystals config!", NamedTextColor.GREEN)
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int defaultToggle(CommandContext<CommandSourceStack> ctx, FasterCrystals plugin, NamespacedKey key, MiniMessage mm) throws CommandSyntaxException {
        Player player = requirePlayer(ctx.getSource());
        byte def = (byte) (plugin.getConfig().getBoolean("default-state", true) ? 1 : 0);
        boolean value = player.getPersistentDataContainer()
                .getOrDefault(key, PersistentDataType.BYTE, def) == 0;
        return apply(player, value, plugin, key, mm);
    }

    private static int apply(Player player, boolean value, FasterCrystals plugin, NamespacedKey key, MiniMessage mm) {
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        var cfg = plugin.getConfig();
        player.sendMessage(mm.deserialize(
                cfg.getString("text", ""),
                Placeholder.parsed("state", cfg.getString("state." + (value ? "on" : "off"), ""))
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static Player requirePlayer(CommandSourceStack src) throws CommandSyntaxException {
        if (!(src.getSender() instanceof Player p))
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
        return p;
    }

    private static boolean parseBoolean(String input) throws CommandSyntaxException {
        return switch (input.toLowerCase()) {
            case "on", "true" -> true;
            case "off", "false" -> false;
            default -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidBool().create(input);
        };
    }
}
