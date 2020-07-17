package com.github.rnlin.system.mamiya;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.internal.command.exception.WorldEditExceptionConverter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.internal.command.exception.WorldEditExceptionConverter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.enginehub.piston.exception.CommandException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CommandListener implements CommandExecutor {

    private MamiyaSystemPlugin plugin;
    private WorldEditPlugin we;
    private EditSessionManage editSessionManage;
    private boolean copyEntities = true;
    private boolean copyBiomes = true;
    private final String NO_UNDO_MESSAGE = "ヒストリーがありません。(undo)";
    private final String NO_REDO_MESSAGE = "ヒストリーがありません。(redo)";
    private WorldEditExceptionConverter exceptionConverter = null;

    public CommandListener(@NotNull MamiyaSystemPlugin plugin, @NotNull WorldEditPlugin we) {
       this.plugin = plugin;
       this.we = we;
       this.editSessionManage = new EditSessionManage(we);
       PlatformCommandManager pcm = we.getWorldEdit().getPlatformManager().getPlatformCommandManager();
        try {
            exceptionConverter = Reflection.<PlatformCommandManager, WorldEditExceptionConverter>getValue(pcm, "exceptionConverter");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!inspection(sender, command, args)) return true;
        //mamiyasystem command
        if (command.getLabel().equalsIgnoreCase(MamiyaSystemPlugin.COMMANDS[0]) ||
                command.getLabel().equalsIgnoreCase("/" + MamiyaSystemPlugin.COMMANDS[0])) {
            if(args.length == 0) {
                if (!inspection(sender, command, args)) return true;
                sender.sendMessage(ChatColor.AQUA +""+ ChatColor.BOLD + "**==----------》" + plugin.getDescription().getName() + "《----------==**");
                sender.sendMessage("*" + ChatColor.BOLD + " API Version " + ChatColor.GREEN + plugin.getDescription().getAPIVersion());
                sender.sendMessage("*" + ChatColor.BOLD + " Plugin Version " + ChatColor.GREEN + plugin.getDescription().getVersion());
                sender.sendMessage("*" + ChatColor.BOLD + " Developer " + ChatColor.GREEN + plugin.getDescription().getAuthors());
                sender.sendMessage("*" + ChatColor.BOLD + " Usage " + ChatColor.GREEN + "/ms help");
                sender.sendMessage("*" + ChatColor.BOLD + " Site " + ChatColor.GREEN + plugin.getSiteURL());
                sender.sendMessage(ChatColor.AQUA +""+ ChatColor.BOLD + "**==------------------------------------==**");
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                if (!inspection(sender, command, args)) return true;
                sender.sendMessage(ChatColor.BOLD +""+ ChatColor.AQUA + "----- コマンド一覧 -----");
                sender.sendMessage(ChatColor.GREEN + "/ms " + ChatColor.RESET + "基本コマンド");
                sender.sendMessage(ChatColor.GREEN + "/ms regen [-e] " + ChatColor.RESET + "\n木の斧で範囲を選択し、" +
                        ChatColor.AQUA + MamiyaSystemPlugin.originWorldName + ChatColor.WHITE + "ワールドを元に土地を再生成します。" +
                        "-eオプションを付けると選択範囲のEntityを無視します。");
                sender.sendMessage(ChatColor.GREEN +"/ms undo " + ChatColor.RESET + "コマンド /ms regen によって変更したブロックを元に戻します。");
                sender.sendMessage(ChatColor.GREEN +"/ms redo " + ChatColor.RESET + "コマンド /ms undo によって元に戻したブロックをやり直します。");
                sender.sendMessage(ChatColor.GREEN +"/ms permission " + ChatColor.RESET + "パーミッションノードを表示します。");
                sender.sendMessage(ChatColor.GREEN +"/ms help " + ChatColor.RESET + "このヘルプを表示します。");
                return true;
            }
            if (args[0].equalsIgnoreCase("permission")) {
                if (!inspection(sender, command, args)) return true;
                sender.sendMessage(ChatColor.AQUA +""+ ChatColor.BOLD + "----- Permissionノード一覧 -----");
                @NotNull List<org.bukkit.permissions.Permission> permissionList = plugin.getDescription().getPermissions();
                for (Permission p : permissionList) {
                    if (p.getName().contains("worldriptidecanceller")) continue;
                    sender.sendMessage(ChatColor.GREEN + p.getName());
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Description] " + p.getDescription());
                    sender.sendMessage(ChatColor.DARK_AQUA + "[Default] " + p.getDefault().toString());
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("undo")) {
                if (!inspection(sender, command, args)) return true;
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
                if (!inspection(sender, command, args)) return true;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You can only execute this command in game.");
                    sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS[0]);
                    return true;
                }
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

            if (args[0].equalsIgnoreCase("debug")) {
                Player player = (Player) sender;
player.sendMessage("debug");
System.out.println("debug");
//                LocalSession session = we.getSession(player);
//                com.sk89q.worldedit.world.World presentWorld = session.getSelectionWorld();
//                Region region = session.getRegionSelector(presentWorld).getIncompleteRegion();
//                removeEntity(region, player, -1);
                return true;
            }

            if (args[0].equalsIgnoreCase("regen")) {
                if (!inspection(sender, command, args)) return true;
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

                try {
                    session.getSelection(presentWorld);
                } catch (IncompleteRegionException e) {
                    try {
                        exceptionConverter.convert(e);
                    } catch (CommandException ce) {
                        BukkitAdapter.adapt(player).print(TextComponent.builder("")
                                .color(TextColor.RED)
                                .append(ce.getRichMessage())
                                .build());
                    }
                    return true;
                }

                // This EditSession instance is used by paste and remove entity.
                EditSession editSession = editSessionManage.getEditSessionAddHistory(player.getName());

                if (args.length >= 2) {
                    if (!args[1].equalsIgnoreCase("-e")) {
                        player.sendMessage(ChatColor.RED + "Usage: /ms regen" + ChatColor.RED + " -e\nEntityを消去しません。");
                        return true;
                    }
                } else {
                    // remove Entity
                    Region selectedRegion = session.getRegionSelector(presentWorld).getIncompleteRegion();
                    removeEntity(editSession, selectedRegion, player);
                }

                // Change RegionSelector from present world to original world.
                session.setRegionSelector(BukkitAdapter.adapt(originWorld), rs);
                Region region = session.getRegionSelector(BukkitAdapter.adapt(originWorld)).getIncompleteRegion();

                // copy
                BlockArrayClipboard clipboard = copy(region, player, -1);

                // paste
                BlockVector3 to = region.getMinimumPoint();
                paste(editSession, clipboard, to);

                session.setRegionSelector(BukkitAdapter.adapt(player.getWorld()), rs);

                return true;
            }
        }
        return true;
    }

    private boolean inspection(CommandSender sender, Command command, String[] args) {
        if (!MamiyaSystemPlugin.enableRegeneration) return false;
        String DO_NOT_EXECUTE_MESSAGE = "You are not permitted to do that. Are you in the right mode?";
        if (command.getLabel().equalsIgnoreCase("ms") && args.length == 0) {
            if (sender.hasPermission("mamiya.system.regen.command.*")) return true;
            sender.sendMessage(ChatColor.RED + DO_NOT_EXECUTE_MESSAGE);
            return false;
        }

        if (command.getLabel().equalsIgnoreCase("ms") && args.length >= 1) {
//            if (sender.hasPermission("mamiya.system.regen.command.*")) return true;
            if (args[0].equalsIgnoreCase("regen")) {
                if (sender.hasPermission("mamiya.system.regen.command.regen")) return true;
                sender.sendMessage(ChatColor.RED + DO_NOT_EXECUTE_MESSAGE);
                return false;
            } else if (args[0].equalsIgnoreCase("undo")) {
                if (sender.hasPermission("mamiya.system.regen.command.undo")) return true;
                sender.sendMessage(ChatColor.RED + DO_NOT_EXECUTE_MESSAGE);
                return false;
            } else if (args[0].equalsIgnoreCase("redo")) {
                if (sender.hasPermission("mamiya.system.regen.command.redo")) return true;
                sender.sendMessage(ChatColor.RED + DO_NOT_EXECUTE_MESSAGE);
                return false;
            } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("permission")) {
                if (sender.hasPermission("mamiya.system.regen.command.help")) return true;
                sender.sendMessage(ChatColor.RED + DO_NOT_EXECUTE_MESSAGE);
                return false;
            } else {
                return false;
            }
        }
        return false;
    }

    private EditSession removeEntity(EditSession editSession, Region region, Player player) {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                editSession, region, clipboard, region.getMinimumPoint()
        );
        // configure
        forwardExtentCopy.setRemovingEntities(true);
        try {
            Operations.complete(forwardExtentCopy);
//            forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(player)::print);
            player.sendMessage("" + forwardExtentCopy.getAffected());
        } catch (WorldEditException ex) {
            ex.printStackTrace();
        }
         return editSession;
    }

    private BlockArrayClipboard copy(Region region, Player player, int maxBlock) {
        com.sk89q.worldedit.world.World world = region.getWorld();
        EditSession editSession = WorldEdit
                .getInstance()
                .getEditSessionFactory()
                .getEditSession(world, maxBlock);

//        CuboidRegion cuboidRegion = new CuboidRegion(world, region.getMinimumPoint(), region.getMaximumPoint());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                editSession, region, clipboard, region.getMinimumPoint()
        );
        // configure
        forwardExtentCopy.setCopyingEntities(copyEntities);
        forwardExtentCopy.setCopyingBiomes(copyBiomes);
        try {
            Operations.complete(forwardExtentCopy);
            forwardExtentCopy.getStatusMessages().forEach(BukkitAdapter.adapt(player)::print);
        } catch (WorldEditException ex) {
            ex.printStackTrace();
        }

        return clipboard;
    }

    private EditSession paste(EditSession editSession, BlockArrayClipboard clipboard, BlockVector3 to) {
        Operation operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(to)
                .copyEntities(copyEntities)
                .copyBiomes(copyBiomes)
                // configure here
                .build();
        try {
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        editSession.close();
        return editSession;
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
