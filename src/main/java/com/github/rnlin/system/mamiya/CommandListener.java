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
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
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
    private boolean copyEntities = true;
    private final String NO_UNDO_MESSAGE = "ヒストリーがありません。";
    private final String NO_REDO_MESSAGE = "ヒストリーがありません。";

    public CommandListener(@NotNull MamiyaSystemPlugin plugin, @NotNull WorldEditPlugin we) {
       this.plugin = plugin;
       this.we = we;
       this.editSessionManage = new EditSessionManage(we);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //mamiyasystem command
        if (command.getLabel().equalsIgnoreCase(MamiyaSystemPlugin.COMMANDS[0])) {
            if(args.length != 1) return true;
            if (args[0].equalsIgnoreCase("undo")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
                    sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS[1]);
                    return true;
                }
                Player player = (Player) sender;
                if (editSessionManage.isUndo(player.getName())) {
                    undo(player);
                    byte times = 1;
                    BukkitAdapter.adapt(player).printInfo(TranslatableComponent.of(
                            "worldedit.undo.undone", TextComponent.of(times)
                            ));
                } else {
                    sender.sendMessage(ChatColor.RED + NO_UNDO_MESSAGE);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("redo")) {
                Player player = (Player) sender;
                if (editSessionManage.isRedo(player.getName())) {
                    redo(player);
                    byte times = 1;
                    BukkitAdapter.adapt(player).printInfo(TranslatableComponent.of(
                            "worldedit.redo.redone", TextComponent.of(times)
                    ));
                } else {
                    sender.sendMessage(ChatColor.RED + NO_REDO_MESSAGE);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("regen")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
                    sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS[0]);
                    return true;
                }

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
                session.setRegionSelector(BukkitAdapter.adapt(player.getWorld()), rs);
                return true;
            }
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

    private void undo(Player player) {
        EditSession es = editSessionManage.getHistEditSession(player);
        if (es == null) {
            player.sendMessage(ChatColor.RED + "ヒストリーがありません。通常の//undoコマンドをお試しください。");
            return;
        }
        try (EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(es.getWorld(), -1)) {
            es.undo(newEditSession);
        }
        WorldEdit worldEdit = we.getWorldEdit();
        worldEdit.flushBlockBag(BukkitAdapter.adapt(player), es);
    }

    private void redo(Player player) {
        EditSession es = editSessionManage.getLastEditSessionUndone(player);
        if (es == null) {
            player.sendMessage(ChatColor.RED + "ヒストリーがありません。通常の//redoコマンドをお試しください。");
            return;
        }
        try (EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(es.getWorld(), -1)) {
            es.redo(newEditSession);
        }
        WorldEdit worldEdit = we.getWorldEdit();
        worldEdit.flushBlockBag(BukkitAdapter.adapt(player), es);
    }
}
