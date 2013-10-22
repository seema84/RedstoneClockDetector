package net.daboross.bukkitdev.redstoneclockdetector.commands;

import org.bukkit.command.CommandSender;

import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;

public class StopCommand extends AbstractCommand {

    protected RCDPlugin plugin;

    public StopCommand(AbstractCommand[] children, RCDPlugin plugin) {
        super("stop  Stop scan.", "redstoneclockdetector.stop", children);
        this.plugin = plugin;
    }

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data) {
        if (this.plugin.stop()) {
            sender.sendMessage("Successfully stopped.");
        } else {
            sender.sendMessage("Already stopped.");
        }
        return true;
    }

}
