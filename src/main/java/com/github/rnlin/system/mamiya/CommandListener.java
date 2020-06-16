package com.github.rnlin.system.mamiya;

import com.google.gson.internal.$Gson$Preconditions;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
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

import java.util.concurrent.ExecutionException;

public class CommandListener implements CommandExecutor {

    private MamiyaSystemPlugin plugin;
    private WorldEditPlugin we;
    private EditSession editSession;

    public CommandListener(MamiyaSystemPlugin plugin, WorldEditPlugin we) {
       this.plugin = plugin;
       this.we = we;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.getServer().broadcastMessage("v=" + "4");
        if (command.getLabel().equalsIgnoreCase("//undo")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
sender.sendMessage("execute undo");
                return true;
            }
            Player player = (Player) sender;
System.out.println("editSession#MinimumPoint=" + this.editSession.toString());
            editSession.undo(editSession);
            return true;
        }
        if (label.equalsIgnoreCase("//regen")) {
            plugin.getServer().broadcastMessage("v=" + "4");
            // debug
            if (args.length > 0) {
                Player player = (Player) sender;
                LocalSession session = we.getSession(player);
//                EditSession editSession = session.createEditSession(we.wrapPlayer(player));
//                ClipboardHolder holder = null;
//                try {
//                    holder = session.getClipboard();
//                } catch (EmptyClipboardException e1) {
//                    e1.printStackTrace();
//                }
//                Clipboard clipboard = holder.getClipboard();
//                Region r = clipboard.getRegion();
//                plugin.getServer().broadcastMessage("Clipboard#Region#World#getName()=" + r.getWorld().getName());
                com.sk89q.worldedit.world.World world = session.getSelectionWorld();
sender.sendMessage("LocalSession#getSelectionWorld()=" + world.getName());

                World origin = Bukkit.getWorld("origin");
                RegionSelector rs = session.getRegionSelector(world);
                session.setRegionSelector(BukkitAdapter.adapt(origin), rs);
                com.sk89q.worldedit.world.World changedWorld = session.getSelectionWorld();
sender.sendMessage("changedWorld=" + changedWorld.getName());

                Region region = session.getRegionSelector(BukkitAdapter.adapt(origin)).getIncompleteRegion();
                int volume = region.getWidth() * region.getHeight() * region.getLength();
plugin.getServer().broadcastMessage("volume=" + volume);

                CuboidRegion region2 = new CuboidRegion(BukkitAdapter.adapt(origin), region.getMinimumPoint(), region.getMaximumPoint());
                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

                editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(origin), -1);
                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                        editSession, region2, clipboard, region2.getMinimumPoint()
                );
                // configure here
                try {
                    Operations.complete(forwardExtentCopy);
                    forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(sender)::print);
                } catch (WorldEditException ex) {
                    ex.printStackTrace();
                }

                editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(region2.getMinimumPoint())
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
            System.out.println("onCommand#if(...(\"//regen\"))");
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "ゲーム内から実行してください。");
                return true;
            }

            if (we == null) {
                System.out.println("we=null");
                return true;
            }

            Player player = (Player) sender;
            LocalSession session = we.getSession(player);
            //WorldEdit.getInstance).getEditSessionFactory().getEditSession(player.getWorld(), WE_LIMIT, player);
            if (session == null) {
                System.out.println("session=null");
//                send(ChatColor.RED, sender, "範囲を指定して下さい。");
                return true;
            }

            com.sk89q.worldedit.world.World world = session.getSelectionWorld();
            sender.sendMessage("範囲を選択したワールド " + world.getName());

            RegionSelector selector = session.getRegionSelector(world);
            if (selector == null) {
                System.out.println("selector=null");
//                send(ChatColor.RED, sender, "範囲を指定して下さい。");
                return true;
            }

            Region region = null;
            try {
                region = selector.getRegion();
            } catch (IncompleteRegionException e) {
                e.printStackTrace();
            }
            if (region == null) {
                System.out.println("region=null");
//                send(ChatColor.RED, sender, "範囲を指定して下さい。");
                return true;
            }

            if (region.getMinimumPoint() == null || region.getMaximumPoint() == null) {
                System.out.println("region.getMinimumPoint()=null || region.getMaximumPoint()=null");
//                send(ChatColor.RED, sender, "範囲を指定して下さい。");
                return true;
            }

            int volume = region.getWidth() * region.getHeight() * region.getLength();
            sender.sendMessage("選択範囲内のブロック数は " + volume + "個 です。");
//            int limit = plugin.config().getInt("Regeneration of regions.Maximum number of blocks that can be regenerated");
//            if(volume > limit){
//                send(ChatColor.RED, sender, "指定された範囲が大きすぎます(" + volume + ")。上限は" + limit + "ブロックです。");
//                return true;
//            }

            //BukkitPlayer user = we.wrapPlayer(player);

//            World origin = Bukkit.getWorld("origin");
//            session.setWorldOverride(BukkitAdapter.adapt(origin));
//            region.setWorld(BukkitAdapter.adapt(origin));

			/*BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
			try {
				clipboard.setOrigin(session.getPlacementPosition(user));
			} catch (IncompleteRegionException e) {
				e.printStackTrace();
			}
			EditSession editSession = session.createEditSession(user);E
			ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
			copy.setCopyingEntities(true);
			try {
				Operations.completeLegacy(copy);
			} catch (MaxChangedBlocksException e) {
				e.printStackTrace();
			}
			session.setClipboard(new ClipboardHolder(clipboard));

			ClipboardHolder holder = null;
			try {
				holder = session.getClipboard();
			} catch (EmptyClipboardException e1) {
				e1.printStackTrace();
			}
			//Clipboard clipboard = holder.getClipboard();
			//Region region = clipboard.getRegion();
			BlockVector3 to = clipboard.getOrigin();//atOrigin ? clipboard.getOrigin() : session.getPlacementPosition(player);
			Operation operation = holder.createPaste(editSession).to(to).ignoreAirBlocks(false).build();
			try {
				Operations.completeLegacy(operation);
			} catch (MaxChangedBlocksException e) {
				e.printStackTrace();
			}*/

            World origin = Bukkit.getWorld("origin");
            session.setRegionSelector(BukkitAdapter.adapt(origin), session.getRegionSelector(world));


            Bukkit.dispatchCommand(player, "/copy");
com.sk89q.worldedit.world.World changedWorld = session.getSelectionWorld();
plugin.getServer().broadcastMessage("chanedWorld=" + changedWorld.getName());
plugin.getServer().broadcastMessage("volume" + region.getWorld());

            World worldWorld = Bukkit.getWorld("world");
            session.setRegionSelector(BukkitAdapter.adapt(worldWorld), session.getRegionSelector(changedWorld));
com.sk89q.worldedit.world.World resultWorld = session.getSelectionWorld();
plugin.getServer().broadcastMessage("resultWorld=" + resultWorld.getName());
            Bukkit.dispatchCommand(player, "/paste");

            sender.sendMessage(ChatColor.AQUA + "指定された範囲を再生成しました1");

            return true;

//        send(ChatColor.RED, sender, "入力されたコマンドが不正です。");
        }
        return true;
    }
}
