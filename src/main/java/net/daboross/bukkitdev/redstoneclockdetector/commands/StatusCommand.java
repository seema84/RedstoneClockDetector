package net.daboross.bukkitdev.redstoneclockdetector.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.IOutput;
import net.daboross.bukkitdev.redstoneclockdetector.utils.OutputManager;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;

public class StatusCommand extends AbstractCommand {

    public StatusCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
        PluginDescriptionFile des = plugin.getDescription();
        this.pluginInfo = String.format(
                "Version: " + ChatColor.YELLOW + "%s"
                + ChatColor.WHITE + ", Author: "
                + ChatColor.YELLOW + "%s",
                des.getVersion(),
                des.getAuthors().get(0));
    }

    protected RCDPlugin plugin;
    protected String pluginInfo;

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data)
            throws UsageException {
        IOutput toSender = OutputManager.GetInstance().toSender(sender);
        OutputManager.GetInstance().prefix(toSender).output(this.pluginInfo);
        CommandSender user = this.plugin.getUser();
        if (user != null) {
            toSender.output(String.format(
                    ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE
                    + "has started a scan, remaining "
                    + ChatColor.YELLOW + "%d " + ChatColor.WHITE
                    + "seconds to finish.",
                    user.getName(),
                    this.plugin.getSecondsRemain()));
        }

        return true;
    }

}
