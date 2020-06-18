package com.github.rnlin.system.mamiya;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandListener implements CommandExecutor {

    private MamiyaSystemPlugin plugin;
    private WorldEditPlugin we;
    private EditSessionManage editSessionManage;

    public CommandListener(@NotNull MamiyaSystemPlugin plugin, @NotNull WorldEditPlugin we) {
       this.plugin = plugin;
       this.we = we;
       this.editSessionManage = new EditSessionManage(we);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // undo command.
        if (command.getLabel().equalsIgnoreCase(MamiyaSystemPlugin.COMMANDS[1])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS[1]);
                return true;
            }
            Player player = (Player) sender;
            EditSession es = editSessionManage.getHistEditSession(player);
            if (es == null) {
System.out.println("es=" + es);
                return true;
            }
            es.undo(es);
            return true;
        }

        // regen command.
        if (label.equalsIgnoreCase(MamiyaSystemPlugin.COMMANDS[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
                sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS[0]);
                return true;
            }

            // debug
//            if (args.length > 0) {
//                Player player = (Player) sender;
//                LocalSession session = we.getSession(player);
////                EditSession editSession = session.createEditSession(we.wrapPlayer(player));
////                ClipboardHolder holder = null;
////                try {
////                    holder = session.getClipboard();
////                } catch (EmptyClipboardException e1) {
////                    e1.printStackTrace();
////                }
////                Clipboard clipboard = holder.getClipboard();
////                Region r = clipboard.getRegion();
////                plugin.getServer().broadcastMessage("Clipboard#Region#World#getName()=" + r.getWorld().getName());
//                com.sk89q.worldedit.world.World world = session.getSelectionWorld();
//sender.sendMessage("LocalSession#getSelectionWorld()=" + world.getName());
//
//                World origin = Bukkit.getWorld("origin");
//                RegionSelector rs = session.getRegionSelector(world);
//                session.setRegionSelector(BukkitAdapter.adapt(origin), rs);
//                com.sk89q.worldedit.world.World changedWorld = session.getSelectionWorld();
//sender.sendMessage("changedWorld=" + changedWorld.getName());
//
//                Region region = session.getRegionSelector(BukkitAdapter.adapt(origin)).getIncompleteRegion();
//                int volume = region.getWidth() * region.getHeight() * region.getLength();
//plugin.getServer().broadcastMessage("volume=" + volume);
//
//                CuboidRegion region2 = new CuboidRegion(BukkitAdapter.adapt(origin), region.getMinimumPoint(), region.getMaximumPoint());
//                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
//
//                editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin), -1);
//                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
//                        editSession, region2, clipboard, region2.getMinimumPoint()
//                );
//                // configure here
//                try {
//                    Operations.complete(forwardExtentCopy);
//                    forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(sender)::print);
//                } catch (WorldEditException ex) {
//                    ex.printStackTrace();
//                }
//
//                editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
//                Operation operation = new ClipboardHolder(clipboard)
//                        .createPaste(editSession)
//                        .to(region2.getMinimumPoint())
//                        .copyEntities(true)
//                        // configure here
//                        .build();
//
//                try {
//                    Operations.complete(operation);
//                } catch (WorldEditException e) {
//                    e.printStackTrace();
//                }
//                editSession.close();
//                return true;
//            }

            Player player = (Player) sender;
            LocalSession session = we.getSession(player);

            World originWorld;
            try {
                originWorld = Objects.requireNonNull(
                        Bukkit.getWorld(MamiyaSystemPlugin.originWorldName),
                        "World " + MamiyaSystemPlugin.originWorldName + " is not found."
                );
            } catch (NullPointerException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.YELLOW + "ワールド \"" + MamiyaSystemPlugin.originWorldName + "\" が見つかりません。");
                return true;
            }

            com.sk89q.worldedit.world.World presentWorld = session.getSelectionWorld();
            RegionSelector rs = session.getRegionSelector(presentWorld);

            // Change RegionSelector from present world to original world.
            session.setRegionSelector(BukkitAdapter.adapt(originWorld), rs);

            Region region = session.getRegionSelector(BukkitAdapter.adapt(originWorld)).getIncompleteRegion();

            // copy
           BlockArrayClipboard clipboard = copy(region, player, -1);

            // paste
//            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
System.out.println(player.getName());
            EditSession editSession = editSessionManage.getEditSessionAddHistory(player.getName());
System.out.println(editSession.getWorld().getName());
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(region.getMinimumPoint())
                    .copyEntities(true)
                    // configure here
                    .build();

            try {
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
            editSession.close();
        }
        return true;
    }

    private BlockArrayClipboard copy(Region region, Player player, int maxBlock) {
        com.sk89q.worldedit.world.World world = region.getWorld();
        EditSession editSession = WorldEdit
                .getInstance()
                .getEditSessionFactory()
                .getEditSession(world, maxBlock);

        CuboidRegion cuboidRegion = new CuboidRegion(world, region.getMinimumPoint(), region.getMaximumPoint());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                editSession, cuboidRegion, clipboard, cuboidRegion.getMinimumPoint()
        );
        // configure here

        try {
            Operations.complete(forwardExtentCopy);
            forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(player)::print);
        } catch (WorldEditException ex) {
            ex.printStackTrace();
        }

        return clipboard;
    }
}
