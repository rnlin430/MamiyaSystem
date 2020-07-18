package com.github.rnlin.system.mamiya;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RiptideListener implements Listener {
    private MamiyaSystemPlugin plugin;

    public RiptideListener(MamiyaSystemPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerRiptide (PlayerRiptideEvent e) {
        if(!MamiyaSystemPlugin.isEnableWorldRepitiedCanceller) return;
        if(RiptideCancellerTask.isRestricted) return;
        Player player = e.getPlayer();
        Location from = player.getLocation();
        if(player.hasPermission("worldriptidecanceller.ignoreriptidecancel")) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setVelocity(new Vector(0,0,0));
                player.teleport(from);
                if (MamiyaSystemPlugin.cancelMessage == null) return;
                player.sendMessage(MamiyaSystemPlugin.cancelMessage);
            }
        }.runTaskLater(plugin, 1);
    }
}
