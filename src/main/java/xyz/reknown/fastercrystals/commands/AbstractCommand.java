package xyz.reknown.fastercrystals.commands;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {
    protected final String name;

    public AbstractCommand(String name) {
        this.name = name;
    }

    public abstract void register();

    public abstract void run(Player player, CommandArguments args) throws WrapperCommandSyntaxException;
}
