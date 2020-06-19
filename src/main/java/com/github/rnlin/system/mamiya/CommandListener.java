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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandListener implements CommandExecutor {

    private MamiyaSystemPlugin plugin;
    private WorldEditPlugin we;
    private EditSessionManage editSessionManage;
    private boolean copyEntities = true;
//    private List<EditSession> editSessionNew = new ArrayList<>();


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
//            // debug
//            if (args.length > 0) {
//                int number = Integer.parseInt(args[0]);
//                EditSession editSession = this.editSessionNew.get(number);
//                editSession.undo(editSession);
//                return true;
//            }
            Player player = (Player) sender;
            EditSession es = editSessionManage.getHistEditSession(player);
            if (es == null) {
                sender.sendMessage(ChatColor.RED + "ヒストリーがありません。通常の//undoコマンドをお試しください。");
            }
            try (EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory()
                    .getEditSession(es.getWorld(), -1)) {
                es.undo(newEditSession);
            }
            WorldEdit worldEdit = we.getWorldEdit();
            worldEdit.flushBlockBag(BukkitAdapter.adapt(player), es);
            return true;
        }

        // regen command.
        if (label.equalsIgnoreCase(MamiyaSystemPlugin.COMMANDS[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
                sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS[0]);
                return true;
            }

//            // debug
//            if (args.length > 0) {
//
//                Player player = (Player) sender;
//                LocalSession session = we.getSession(player);
//                com.sk89q.worldedit.world.World presentWorld = session.getSelectionWorld();
//                System.out.println("getEditSessionAddHistory presentWorld#Name=" + presentWorld.getName());
////                com.sk89q.worldedit.world.World presentWorld = BukkitAdapter.adapt(player.getWorld());
//                RegionSelector rs = session.getRegionSelector(presentWorld);
//
//                World originWorld = Objects.requireNonNull(Bukkit.getWorld("origin"),
//                        "origin" + " is not found.");
//                session.setRegionSelector(BukkitAdapter.adapt(originWorld), rs);
//
//                Region region = session.getRegionSelector(BukkitAdapter.adapt(originWorld)).getIncompleteRegion();
//
//                EditSession editSession = WorldEdit
//                        .getInstance()
//                        .getEditSessionFactory()
//                        .getEditSession(BukkitAdapter.adapt(originWorld), -1);
//
//                CuboidRegion region2 = new CuboidRegion(BukkitAdapter.adapt(originWorld), region.getMinimumPoint(), region.getMaximumPoint());
//                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
//
//                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
//                        editSession, region2, clipboard, region2.getMinimumPoint()
//                );
//                // configure here
//                try {
//                    Operations.complete(forwardExtentCopy);
//                    forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(player)::print);
//                } catch (WorldEditException ex) {
//                    ex.printStackTrace();
//                }
//                EditSession editSessionNew = WorldEdit.getInstance().getEditSessionFactory().getEditSession(presentWorld, -1);
//                this.editSessionNew.add(editSessionNew);
//                Operation operation = new ClipboardHolder(clipboard)
//                        .createPaste(editSessionNew)
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
//                editSessionNew.close();
//               return true;
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
            EditSession editSession = editSessionManage.getEditSessionAddHistory(player.getName());
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(region.getMinimumPoint())
                    .copyEntities(copyEntities)
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
        // configure
        forwardExtentCopy.setCopyingEntities(copyEntities);
        try {
            Operations.complete(forwardExtentCopy);
            forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(player)::print);
        } catch (WorldEditException ex) {
            ex.printStackTrace();
        }

        return clipboard;
    }
}
