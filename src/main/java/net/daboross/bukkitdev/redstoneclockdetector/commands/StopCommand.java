package net.daboross.bukkitdev.redstoneclockdetector.commands;

import org.bukkit.command.CommandSender;

import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.IOutput;
import net.daboross.bukkitdev.redstoneclockdetector.utils.OutputManager;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;

public class StopCommand extends AbstractCommand {

    public StopCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }
    protected RCDPlugin plugin;

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data)
            throws UsageException {
        IOutput toSender = OutputManager.GetInstance().toSender(sender);
        if (this.plugin.stop()) {
            toSender.output("Successfully stoped.");
        } else {
            toSender.output("Already stoped.");
        }
        return true;
    }

}
