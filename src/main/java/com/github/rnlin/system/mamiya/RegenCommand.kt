package com.github.rnlin.system.mamiya

import com.sk89q.worldedit.IncompleteRegionException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extension.platform.PlatformCommandManager
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.internal.command.exception.WorldEditExceptionConverter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.formatting.text.Component
import com.sk89q.worldedit.util.formatting.text.TextComponent
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent
import com.sk89q.worldedit.util.formatting.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.enginehub.piston.exception.CommandException
import java.util.*
import java.util.function.Consumer

class RegenCommand(private val plugin: MamiyaSystemPlugin, private val we: WorldEditPlugin) : CommandExecutor {
    private val editSessionManage: EditSessionManage
    private val copyEntities = true
    private val copyBiomes = true
    private val NO_UNDO_MESSAGE = "ヒストリーがありません。"
    private val NO_REDO_MESSAGE = "ヒストリーがありません。"
    private var exceptionConverter: WorldEditExceptionConverter? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!inspection(sender, command, args)) return true
        //mamiyasystem command
        if (command.label.equals(MamiyaSystemPlugin.COMMANDS_REGEN[0], ignoreCase = true)) {
            if (args.size == 0) {
                if (!inspection(sender, command, args)) return true
                sender.sendMessage("${ChatColor.AQUA}${ChatColor.BOLD}**=---${plugin.description.name}---==**")
                sender.sendMessage("*${ChatColor.BOLD} API Version ${ChatColor.GREEN}${plugin.description.apiVersion}")
                sender.sendMessage("*" + ChatColor.BOLD + " Plugin Version " + ChatColor.GREEN + plugin.description.version)
                sender.sendMessage("*" + ChatColor.BOLD + " Developer " + ChatColor.GREEN + plugin.description.authors)
                sender.sendMessage("*" + ChatColor.BOLD + " Usage " + ChatColor.GREEN + "/ms help")
                sender.sendMessage("*" + ChatColor.BOLD + " Site " + ChatColor.GREEN + plugin.siteURL)
                sender.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "**=------------------==**")
                return true
            }
            if (args[0].equals("help", ignoreCase = true)) {
                if (!inspection(sender, command, args)) return true
                sender.sendMessage(ChatColor.BOLD.toString() + "" + ChatColor.AQUA + "----- コマンド一覧 -----")
                sender.sendMessage(ChatColor.GREEN.toString() + "/ms " + ChatColor.RESET + "基本コマンド")
                sender.sendMessage("""${ChatColor.GREEN}/ms regen ${ChatColor.RESET}木の斧で範囲を選択し土地を再生成します。
           (${MamiyaSystemPlugin.originWorldName} ワールドからコピーします。)""")
                sender.sendMessage(ChatColor.GREEN.toString() + "/ms undo " + ChatColor.RESET + "/ms regen で変更したブロックを元に戻します。")
                sender.sendMessage(ChatColor.GREEN.toString() + "/ms redo " + ChatColor.RESET + "/ms undo で元に戻したブロックをやり直します。")
                sender.sendMessage(ChatColor.GREEN.toString() + "/ms permission " + ChatColor.RESET + "パーミッションノードを表示します。")
                sender.sendMessage(ChatColor.GREEN.toString() + "/ms help " + ChatColor.RESET + "このヘルプを表示します。")
                return true
            }
            if (args[0].equals("permission", ignoreCase = true)) {
                if (!inspection(sender, command, args)) return true
                sender.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "----- Permissionノード一覧 -----")
                val permissionList = plugin.description.permissions
                for (p in permissionList) {
                    if (p.name.contains("worldriptidecanceller")) continue
                    sender.sendMessage(ChatColor.GREEN.toString() + p.name)
                    sender.sendMessage(ChatColor.DARK_AQUA.toString() + "[Description] " + p.description)
                    sender.sendMessage(ChatColor.DARK_AQUA.toString() + "[Default] " + p.default.toString())
                }
                return true
            }
            if (args[0].equals("undo", ignoreCase = true)) {
                if (!inspection(sender, command, args)) return true
                if (sender !is Player) {
                    sender.sendMessage(ChatColor.RED.toString() + "You can only execute this command in game.")
                    sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS_REGEN[1])
                    return true
                }
                val player = sender
                if (editSessionManage.isUndo(player.name)) {
                    undo(player)
                    val times: Byte = 1
                    BukkitAdapter.adapt(player).printInfo(TranslatableComponent.of(
                            "worldedit.undo.undone", TextComponent.of(times.toInt())
                    ))
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + NO_UNDO_MESSAGE)
                }
                return true
            }
            if (args[0].equals("redo", ignoreCase = true)) {
                if (!inspection(sender, command, args)) return true
                if (sender !is Player) {
                    sender.sendMessage(ChatColor.RED.toString() + "You can only execute this command in game.")
                    sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS_REGEN[0])
                    return true
                }
                val player = sender
                if (editSessionManage.isRedo(player.name)) {
                    redo(player)
                    val times: Byte = 1
                    BukkitAdapter.adapt(player).printInfo(TranslatableComponent.of(
                            "worldedit.redo.redone", TextComponent.of(times.toInt())
                    ))
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + NO_REDO_MESSAGE)
                }
                return true
            }
            if (args[0].equals("regen", ignoreCase = true)) {
                if (!inspection(sender, command, args)) return true
                if (sender !is Player) {
                    sender.sendMessage(ChatColor.RED.toString() + "You can only execute this command in game.")
                    sender.sendMessage("execute " + MamiyaSystemPlugin.COMMANDS_REGEN[0])
                    return true
                }
                val player = sender
                val session = we.getSession(player)
                val originWorld: World?
                originWorld = Bukkit.getWorld(MamiyaSystemPlugin.originWorldName) ?: run {
                    player.sendMessage(ChatColor.YELLOW.toString() + "ワールド \"" + MamiyaSystemPlugin.originWorldName + "\" が見つかりません。")
                    return true
                }

                val presentWorld = session.selectionWorld
                val rs = session.getRegionSelector(presentWorld)
                try {
                    session.getSelection(presentWorld)
                } catch (e: IncompleteRegionException) {
                    try {
                        exceptionConverter!!.convert(e)
                    } catch (ce: CommandException) {
                        BukkitAdapter.adapt(player).print(TextComponent.builder("")
                                .color(TextColor.RED)
                                .append(ce.richMessage)
                                .build())
                    }
                    return true
                }

                // Change RegionSelector from present world to original world.
                session.setRegionSelector(BukkitAdapter.adapt(originWorld), rs)
                val region = session.getRegionSelector(BukkitAdapter.adapt(originWorld)).incompleteRegion

                // copy
                val clipboard = copy(region, player, -1)

                // paste
                val to = region.minimumPoint
                paste(clipboard, player, to)
                session.setRegionSelector(BukkitAdapter.adapt(player.world), rs)
                return true
            }
        }
        return true
    }

    private fun inspection(sender: CommandSender, command: Command, args: Array<String>): Boolean {
        if (!MamiyaSystemPlugin.enableRegeneration) return false
        val DO_NOT_EXECUTE_MESSAGE = "You are not permitted to do that. Are you in the right mode?"
        if (command.label.equals("ms", ignoreCase = true) && args.size == 0) {
            if (sender.hasPermission("mamiya.system.regen.command.*")) return true
            sender.sendMessage(ChatColor.RED.toString() + DO_NOT_EXECUTE_MESSAGE)
            return false
        }
        return if (command.label.equals("ms", ignoreCase = true) && args.size >= 1) {
//            if (sender.hasPermission("mamiya.system.regen.command.*")) return true;
            if (args[0].equals("regen", ignoreCase = true)) {
                if (sender.hasPermission("mamiya.system.regen.command.regen")) return true
                sender.sendMessage(ChatColor.RED.toString() + DO_NOT_EXECUTE_MESSAGE)
                false
            } else if (args[0].equals("undo", ignoreCase = true)) {
                if (sender.hasPermission("mamiya.system.regen.command.undo")) return true
                sender.sendMessage(ChatColor.RED.toString() + DO_NOT_EXECUTE_MESSAGE)
                false
            } else if (args[0].equals("redo", ignoreCase = true)) {
                if (sender.hasPermission("mamiya.system.regen.command.redo")) return true
                sender.sendMessage(ChatColor.RED.toString() + DO_NOT_EXECUTE_MESSAGE)
                false
            } else if (args[0].equals("help", ignoreCase = true) || args[0].equals("permission", ignoreCase = true)) {
                if (sender.hasPermission("mamiya.system.regen.command.help")) return true
                sender.sendMessage(ChatColor.RED.toString() + DO_NOT_EXECUTE_MESSAGE)
                false
            } else {
                false
            }
        } else false
    }

    private fun copy(region: Region, player: Player, maxBlock: Int): BlockArrayClipboard {
        val world = region.world
        val editSession = WorldEdit
                .getInstance()
                .editSessionFactory
                .getEditSession(world, maxBlock)

//        CuboidRegion cuboidRegion = new CuboidRegion(world, region.getMinimumPoint(), region.getMaximumPoint());
        val clipboard = BlockArrayClipboard(region)
        val forwardExtentCopy = ForwardExtentCopy(
                editSession, region, clipboard, region.minimumPoint
        )
        // configure
        forwardExtentCopy.isCopyingEntities = copyEntities
        forwardExtentCopy.isCopyingBiomes = copyBiomes
        try {
            Operations.complete(forwardExtentCopy)
            forwardExtentCopy.statusMessages.forEach(Consumer { component: Component? -> BukkitAdapter.adapt(player).print(component) })
        } catch (ex: WorldEditException) {
            ex.printStackTrace()
        }
        return clipboard
    }

    private fun paste(clipboard: BlockArrayClipboard, player: Player, to: BlockVector3) {
        val editSession = editSessionManage.getEditSessionAddHistory(player.name)
        val operation = ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(to)
                .copyEntities(copyEntities)
                .copyBiomes(copyBiomes) // configure here
                .build()
        try {
            Operations.complete(operation)
        } catch (e: WorldEditException) {
            e.printStackTrace()
        }
        editSession.close()
    }

    private fun undo(player: Player) {
        val es = editSessionManage.getHistEditSession(player)
        if (es == null) {
            player.sendMessage(ChatColor.RED.toString() + "ヒストリーがありません。通常の//undoコマンドをお試しください。")
            return
        }
        WorldEdit.getInstance().editSessionFactory
                .getEditSession(es.world, -1).use { newEditSession -> es.undo(newEditSession) }
        val worldEdit = we.worldEdit
        worldEdit.flushBlockBag(BukkitAdapter.adapt(player), es)
    }

    private fun redo(player: Player) {
        val es = editSessionManage.getLastEditSessionUndone(player)
        if (es == null) {
            player.sendMessage(ChatColor.RED.toString() + "ヒストリーがありません。通常の//redoコマンドをお試しください。")
            return
        }
        WorldEdit.getInstance().editSessionFactory
                .getEditSession(es.world, -1).use { newEditSession -> es.redo(newEditSession) }
        val worldEdit = we.worldEdit
        worldEdit.flushBlockBag(BukkitAdapter.adapt(player), es)
    }

    init {
        editSessionManage = EditSessionManage(we)
        val pcm = we.worldEdit.platformManager.platformCommandManager
        try {
            exceptionConverter = Reflection.getValue<PlatformCommandManager, WorldEditExceptionConverter>(pcm, "exceptionConverter")
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }
}