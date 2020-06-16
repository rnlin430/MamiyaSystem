package com.github.rnlin.system.mamiya;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class RiptideCancellerTask extends BukkitRunnable {
    private MamiyaSystemPlugin plugin;
    public static boolean isRestricted = true;
    private CommandSender sender;

    public RiptideCancellerTask(MamiyaSystemPlugin plugin){
        this(plugin, null);
        this.plugin = plugin;
    }

    public RiptideCancellerTask(MamiyaSystemPlugin plugin, CommandSender sender){
        this.sender = sender;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        TpsDataCollector rf = new TpsDataCollector(plugin);
        double[] tps = rf.getRecentTps();

        if(isRestricted){
            if(tps[0] <= MamiyaSystemPlugin.tpsThreshold){
                if (MamiyaSystemPlugin.startMessage != null)
                plugin.getServer().broadcastMessage(MamiyaSystemPlugin.startMessage);
                isRestricted = false;
                return;
            }
        } else if(!isRestricted) {
            if (tps[0] > MamiyaSystemPlugin.tpsThreshold) {
                if (MamiyaSystemPlugin.endMessage != null) {
                    plugin.getServer().broadcastMessage(MamiyaSystemPlugin.endMessage);
                    isRestricted = true;
                }
                return;
            }
        }
    }
}
