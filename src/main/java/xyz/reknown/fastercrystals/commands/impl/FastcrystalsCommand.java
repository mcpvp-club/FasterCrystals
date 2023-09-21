package xyz.reknown.fastercrystals.commands.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.commands.AbstractCommand;

public class FastcrystalsCommand extends AbstractCommand {
    public FastcrystalsCommand() {
        super("fastcrystals");
    }

    @Override
    public void register() {
        new CommandAPICommand(name)
                .withOptionalArguments(new BooleanArgument("toggle"))
                .withPermission("fastercrystals.fastcrystals")
                .executesPlayer(this::run)
                .register();
    }

    @Override
    public void run(Player player, CommandArguments args) {
        FasterCrystals plugin = JavaPlugin.getPlugin(FasterCrystals.class);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "fastcrystals");

        boolean toggle = (boolean) args.getOptional(0).orElse(pdc.has(key) && !pdc.get(key, PersistentDataType.BOOLEAN));
        pdc.set(key, PersistentDataType.BOOLEAN, toggle);

        player.sendMessage(Component.text("Turned ", NamedTextColor.GOLD)
                .append(Component.text(toggle ? "on" : "off", NamedTextColor.YELLOW))
                .append(Component.text(" your FastCrystals setting.", NamedTextColor.GOLD)));
    }
}
