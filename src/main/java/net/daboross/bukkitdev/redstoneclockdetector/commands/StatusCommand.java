package net.daboross.bukkitdev.redstoneclockdetector.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;

public class StatusCommand extends AbstractCommand {

    protected RCDPlugin plugin;
    protected String pluginInfo;

    public StatusCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin) {
        super(usage, perm, children);
        this.plugin = plugin;
        PluginDescriptionFile des = plugin.getDescription();
        this.pluginInfo = String.format("Version: " + ChatColor.YELLOW + "%s"
                + ChatColor.WHITE + ", Authors: " + ChatColor.YELLOW + "%s",
                des.getVersion(), des.getAuthors());
    }

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data) throws UsageException {
        CommandSender user = this.plugin.getUser();
        if (user != null) {
           sender.sendMessage(String.format(ChatColor.GREEN + "%s " + ChatColor.WHITE + "has started a scan, "
                    + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "seconds remaining to finish.",
                    user.getName(), this.plugin.getSecondsRemain()));
        }

        return true;
    }

}
