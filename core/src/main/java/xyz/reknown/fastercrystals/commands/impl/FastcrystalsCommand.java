package xyz.reknown.fastercrystals.commands.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

        boolean toggle = (boolean) args.getOptional(0)
                    .orElseGet(() -> pdc.has(key, PersistentDataType.BYTE) && pdc.get(key, PersistentDataType.BYTE) == 0);
        pdc.set(key, PersistentDataType.BYTE, (byte) (toggle ? 1 : 0x0));

        String stateKey = "state." + (toggle ? "on" : "off");

        player.sendMessage(MiniMessage
            .miniMessage()
            .deserialize(
                plugin.config()
                    .getString(
                        "text",
                        ""
                    )
                , Placeholder.parsed(
                    "state",
                    plugin.config()
                        .getString(
                            stateKey, ""
                        )
                )
            )
        );
    }
}
