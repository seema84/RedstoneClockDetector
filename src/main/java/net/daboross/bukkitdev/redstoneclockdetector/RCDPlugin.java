package net.daboross.bukkitdev.redstoneclockdetector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.daboross.bukkitdev.redstoneclockdetector.commands.BreakCommand;
import net.daboross.bukkitdev.redstoneclockdetector.commands.ChunkListCommand;
import net.daboross.bukkitdev.redstoneclockdetector.commands.ListCommand;
import net.daboross.bukkitdev.redstoneclockdetector.commands.StartCommand;
import net.daboross.bukkitdev.redstoneclockdetector.commands.StatusCommand;
import net.daboross.bukkitdev.redstoneclockdetector.commands.StopCommand;
import net.daboross.bukkitdev.redstoneclockdetector.commands.TeleportCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.PermissionsException;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RCDPlugin extends JavaPlugin implements CommandExecutor, Listener {

    protected HashMap<Location, Integer> redstoneActivityTable = null;
    protected HashMap<Chunk, Integer> redstoneChunkActivityTable = null;
    protected List<Map.Entry<Location, Integer>> redstoneActivityList = null;
    protected List<Map.Entry<Chunk, Integer>> redstoneChunkActivityList = null;
    protected Worker worker = null;
    protected CommandSender userWhoIssuedLastScan = null;
    protected int taskId = Integer.MIN_VALUE;
    protected AbstractCommand topCommand = null;

    @Override
    public void onDisable() {
        this.stop();
        this.redstoneActivityTable = null;
        this.redstoneChunkActivityTable = null;
        this.redstoneActivityList = null;
        this.redstoneChunkActivityList = null;
    }

    @Override
    public void onEnable() {
        this.redstoneActivityTable = new HashMap<Location, Integer>();
        this.redstoneChunkActivityTable = new HashMap<Chunk, Integer>();
        this.redstoneActivityList = new ArrayList<Map.Entry<Location, Integer>>();
        this.redstoneChunkActivityList = new ArrayList<Map.Entry<Chunk, Integer>>();
        this.stop();
        if (!setupCommands()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    protected boolean setupCommands() {
        try {
            ListCommand listCommand = new ListCommand(null, this);
            ChunkListCommand chunklistCommand = new ChunkListCommand(null, this);
            AbstractCommand[] childCommands = new AbstractCommand[]{
                new StartCommand(null, this, listCommand),
                new StopCommand(null, this),
                listCommand,
                chunklistCommand,
                new TeleportCommand(null, this),
                new BreakCommand(null, this)};
            this.topCommand = new StatusCommand("  Status of plugin.",
                    "redstoneclockdetector", childCommands, this);
        } catch (RuntimeException ex) {
            getLogger().log(Level.SEVERE, "Failed to setup commands", ex);
            return false;
        }
        return true;
    }

    public List<Map.Entry<Location, Integer>> getRedstoneActivityList() {
        return this.redstoneActivityList;
    }

    public List<Map.Entry<Chunk, Integer>> getRedstoneChunkActivityList() {
        return this.redstoneChunkActivityList;
    }

    public CommandSender getUser() {
        return this.userWhoIssuedLastScan;
    }

    public int getSecondsRemain() {
        if (this.taskId == Integer.MIN_VALUE) {
            return -1;
        }
        return this.worker.getSecondsRemain();
    }

    public boolean start(CommandSender sender, int seconds, IProgressReporter progressReporter) {
        if (this.taskId != Integer.MIN_VALUE) {
            return false;
        }
        this.userWhoIssuedLastScan = sender;
        this.worker = new Worker(seconds, progressReporter);
        this.taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.worker, 0L, 20L);
        return true;
    }

    public boolean stop() {
        if (this.taskId != Integer.MIN_VALUE) {
            this.getServer().getScheduler().cancelTask(this.taskId);
            this.taskId = Integer.MIN_VALUE;
            this.userWhoIssuedLastScan = null;
            this.worker = null;
            this.sortChunkList();
            this.redstoneActivityTable.clear();
            this.redstoneChunkActivityTable.clear();
            return true;
        } else {
            return false;
        }
    }

    protected void sortList() {
        ValueComparator bvc = new ValueComparator(this.redstoneActivityTable);
        TreeMap<Location, Integer> sortedMap = new TreeMap<Location, Integer>(bvc);
        sortedMap.putAll(this.redstoneActivityTable);
        this.redstoneActivityList.clear();
        this.redstoneActivityList.addAll(sortedMap.entrySet());
    }

    protected void sortChunkList() {
        ValueChunkComparator bvc = new ValueChunkComparator(this.redstoneChunkActivityTable);
        TreeMap<Chunk, Integer> sortedMap = new TreeMap<Chunk, Integer>(bvc);
        sortedMap.putAll(this.redstoneChunkActivityTable);
        this.redstoneChunkActivityList.clear();
        this.redstoneChunkActivityList.addAll(sortedMap.entrySet());
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockPhysicsEvent event) {
        if (this.taskId == Integer.MIN_VALUE) {
            return;
        }
        Block block = event.getBlock();
        if (block.getBlockPower() == 0) {
            return;
        }
        Location loc = event.getBlock().getLocation();

        // Specific location counts
        int count = 1;
        if (this.redstoneActivityTable.containsKey(loc)) {
            count += this.redstoneActivityTable.get(loc);
        }
        this.redstoneActivityTable.put(loc, count);
        
        // Per-chunk counts
        count = 1;
        if (this.redstoneChunkActivityTable.containsKey(loc.getChunk())) {
            count += this.redstoneChunkActivityTable.get(loc.getChunk());
        }
        this.redstoneChunkActivityTable.put(loc.getChunk(), count);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!topCommand.execute(sender, args)) {
                topCommand.showUsage(sender, command.getName());
            }
        } catch (PermissionsException ex) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        } catch (UsageException ex) {
            sender.sendMessage("Usage: " + ChatColor.YELLOW + command.getName() + " " + ex.getUsage());
            sender.sendMessage(String.format(ChatColor.RED.toString() + ex.getMessage()));
        } catch (RuntimeException ex) {
            sender.sendMessage("There was an error executing the command. Please check the console");
            getLogger().log(Level.SEVERE, "Caught error while executing command " + command + " on sender " + sender, ex);
        }
        return true;
    }

    public interface IProgressReporter {

        public void onProgress(int secondsRemain);
    }

    @AllArgsConstructor
    public class Worker implements Runnable {

        @Getter
        protected int secondsRemain;
        protected IProgressReporter progressReporter;

        @Override
        public void run() {
            if (this.secondsRemain <= 0) {
                if (RCDPlugin.this.stop() && this.progressReporter != null) {
                    this.progressReporter.onProgress(secondsRemain);
                }
            } else {
                if (this.progressReporter != null) {
                    this.progressReporter.onProgress(secondsRemain);
                }
                this.secondsRemain--;
            }

        }

    }

    @AllArgsConstructor
    public class ValueComparator implements Comparator<Location> {

        private final Map<Location, Integer> base;

        @Override
        public int compare(Location a, Location b) {
            if (base.get(a) < base.get(b)) {
                return 1;
            } else if (base.get(a) == base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }
    @AllArgsConstructor
    public class ValueChunkComparator implements Comparator<Chunk> {

        private final Map<Chunk, Integer> base;

        @Override
        public int compare(Chunk a, Chunk b) {
            if (base.get(a) < base.get(b)) {
                return 1;
            } else if (base.get(a) == base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
