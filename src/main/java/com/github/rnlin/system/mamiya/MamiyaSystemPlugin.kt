package com.github.rnlin.system.mamiya

import com.sk89q.worldedit.bukkit.WorldEditPlugin
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class MamiyaSystemPlugin : JavaPlugin() {
    private var config: FileConfiguration? = null
    private var bukkitTask: BukkitTask? = null
    private var worldEdit: WorldEditPlugin? = null
    override fun onEnable() {
        // Plugin startup logic
        initialize()
        RiptideListener(this)
        val maybewe: Any? = server.pluginManager.getPlugin("WorldEdit")
        if (maybewe is WorldEditPlugin) {
            worldEdit = maybewe
        }
        val pm = Bukkit.getPluginManager()
        val cl = CommandListener(this, worldEdit!!)
        for (command in COMMANDS) {
            getCommand(command)!!.setExecutor(cl)
        }
        instance = this
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    val siteURL: URL?
        get() {
            var url: URL? = null
            try {
                url = URL("https://github.com/rnlin430/MamiyaSystem")
            } catch (e: MalformedURLException) {
                info(ChatColor.GRAY.toString() + "未設定です。")
            }
            return url
        }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        // AdminCommands
        if (command.name.equals("wrc", ignoreCase = true)) {
            // 権限をチェック
            if (!sender.hasPermission("worldriptidecanceller.command.wr")) {
                displayInfo(sender)
                sender.sendMessage(ChatColor.DARK_RED.toString() + command.permissionMessage)
                return true
            }
            when (args.size) {
                0 -> displayInfo(sender)
                1 -> {
                    if (args[0].equals("true", ignoreCase = true)) {
                        isEnable = true
                        config = getConfig()
                        config!!["enable"] = true
                        saveConfig()
                        reloadConfig()
                        initialize()
                        sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] WorldRiptideCancellerが有効になりました。")
                        return true
                    }
                    if (args[0].equals("false", ignoreCase = true)) {
                        isEnable = false
                        config = getConfig()
                        config!!["enable"] = false
                        saveConfig()
                        reloadConfig()
                        initialize()
                        sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] WorldRiptideCancellerが無効になりました。")
                        return true
                    }
                    if (args[0].equals("info", ignoreCase = true)) {
                        reloadConfig()
                        config = getConfig()
                        initialize()
                        sender.sendMessage(ChatColor.GRAY.toString() + "[" + ChatColor.BLUE + "WRC" + ChatColor.GRAY + "]")
                        sender.sendMessage(ChatColor.GRAY.toString() + "enable: " + ChatColor.AQUA + isEnable)
                        sender.sendMessage(ChatColor.GRAY.toString() + "tps_threshold: " + ChatColor.AQUA + tpsThreshold)
                        sender.sendMessage(ChatColor.GRAY.toString() + "update_frequency: " + ChatColor.AQUA + updateFrequency)
                        sender.sendMessage(ChatColor.GRAY.toString() + "start_message: " + ChatColor.AQUA + startMessage)
                        sender.sendMessage(ChatColor.GRAY.toString() + "end_message: " + ChatColor.AQUA + endMessage)
                        sender.sendMessage(ChatColor.GRAY.toString() + "cancel_message: " + ChatColor.AQUA + cancelMessage)
                        return true
                    }
                    if (args[0].equals("reload", ignoreCase = true)) {
                        reloadConfig()
                        initialize()
                        sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] リロードしました。")
                        if (sender is ConsoleCommandSender) return true
                        info("リロードしました。")
                        return true
                    }
                    if (args[0].equals("hidetps", ignoreCase = true)) {
                        val player2 = sender as Player
                        if (bukkitIdManager.containsKey(player2)) {
                            val usb: Int = bukkitIdManager.get(player2)
                            bukkitIdManager.remove(player2)
                            server.scheduler.cancelTask(usb)
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] リロードしました。")
                            return true
                        }
                        return true
                    }
                    if (args[0].equals("commands", ignoreCase = true)) {
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc commands")
                        sender.sendMessage(ChatColor.AQUA.toString() + "コマンド一覧。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc [true|false]")
                        sender.sendMessage(ChatColor.AQUA.toString() + "trueで激流に制限がかかります。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc info")
                        sender.sendMessage(ChatColor.AQUA.toString() + "現在の設定値です。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc reload")
                        sender.sendMessage(ChatColor.AQUA.toString() + "configをリロードします。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc setts <閾値>")
                        sender.sendMessage(ChatColor.AQUA.toString() + "閾値を下回ったら制限を開始します。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc setuf <tpsスキャン頻度>")
                        sender.sendMessage(ChatColor.AQUA.toString() + "TPS")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc setstartmessage <スタートメッセージ>")
                        sender.sendMessage(ChatColor.AQUA.toString() + "制限開始時のメッセージを編集します。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc setendmessage <エンドメッセージ>")
                        sender.sendMessage(ChatColor.AQUA.toString() + "制限終了時のメッセージを編集します。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc setcancelmessage <キャンセルメッセージ>")
                        sender.sendMessage(ChatColor.AQUA.toString() + "激流キャンセル時のメッセージを編集します。")
                        sender.sendMessage(ChatColor.WHITE.toString() + "/wrc showtps <更新頻度>")
                        sender.sendMessage(ChatColor.AQUA.toString() + "現在のtpsを指定更新頻度で表示し続けます。")
                        return true
                    }
                    if (args[0].equals("showtps", ignoreCase = true)) {
                        val player = sender as Player
                        if (bukkitIdManager.containsKey(player)) {
                            val id = bukkitIdManager[player]!!
                            server.scheduler.cancelTask(id)
                            bukkitIdManager.remove(player)
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] tps表示をオフにしました。")
                            return true
                        }
                        bukkitIdManager[player] = tpsTask(sender, 200)
                        return true
                    }
                    return false
                }
                2 -> {
                    when (args[0]) {
                        "setts" -> {
                            tpsThreshold = args[1].toDouble()
                            config = getConfig()
                            config!!["tps_threshold"] = args[1].toDouble()
                            saveConfig()
                            reloadConfig()
                            initialize()
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] tpsが " + tpsThreshold + " 以下で激流付きトライデントを制限します。")
                            return true
                        }
                        "setuf" -> {
                            updateFrequency = Integer.valueOf(args[1]).toInt()
                            config = getConfig()
                            config!!["update_frequency"] = Integer.valueOf(args[1]).toInt()
                            saveConfig()
                            reloadConfig()
                            initialize()
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] tpsのスキャン頻度が " + updateFrequency + " になりました。")
                            return true
                        }
                        "setstartmessage" -> {
                            val sm = args[1].replace("&", "§")
                            if (sm.equals("none", ignoreCase = true)) {
                                config = getConfig()
                                config!!["start_message"] = null
                            } else {
                                config = getConfig()
                                config!!["start_message"] = sm
                            }
                            saveConfig()
                            initialize()
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] 開始メッセージを更新しました。")
                            return true
                        }
                        "setendmessage" -> {
                            val em = args[1].replace("&", "§")
                            if (em.equals("none", ignoreCase = true)) {
                                config = getConfig()
                                config!!["end_message"] = null
                            } else {
                                config = getConfig()
                                config!!["end_message"] = em
                            }
                            saveConfig()
                            initialize()
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] 終了メッセージを更新しました。")
                            return true
                        }
                        "setcancelmessage" -> {
                            val cm = args[1].replace("&", "§")
                            if (cm.equals("none", ignoreCase = true)) {
                                config = getConfig()
                                config!!["cancel_message"] = null
                            } else {
                                config = getConfig()
                                config!!["cancel_message"] = cm
                            }
                            saveConfig()
                            initialize()
                            sender.sendMessage(ChatColor.GRAY.toString() + "[INFO] キャンセル時のメッセージを更新しました。")
                            return true
                        }
                        "showtps" -> {
                            val player = sender as Player
                            if (args[1] == null) return true
                            if (bukkitIdManager.containsKey(player)) {
                                sender.sendMessage("""
    ${ChatColor.GRAY}[INFO] 既に表示しています。
    /wrc showtps で一度オフにしてから実行してください。
    """.trimIndent())
                                return true
                            }
                            bukkitIdManager[player] = tpsTask(sender, args[1].toInt())
                            return true
                        }
                    }
                    return false
                }
            }
            return true
        }
        return false
    }

    fun initialize() {
        // config
        saveDefaultConfig()
        reloadConfig()
        config = getConfig()
        isEnable = config!!.getBoolean("enable")
        tpsThreshold = config!!.getDouble("tps_threshold")
        updateFrequency = config!!.getInt("update_frequency")
        startMessage = config!!.getString("start_message", null)
        endMessage = config!!.getString("end_message", null)
        cancelMessage = config!!.getString("cancel_message", null)
        enableRegeneration = config!!.getBoolean("RegenerationAssist.enable", true)
        originWorldName = config!!.getString("RegenerationAssist.origin_world_name", "origin")!!
        reloadConfig()
        if (bukkitTask != null) if (!bukkitTask!!.isCancelled) bukkitTask!!.cancel()
        bukkitTask = RiptideCancellerTask(this).runTaskTimer(this, updateFrequency.toLong(), updateFrequency.toLong())
    }

    private fun tpsTask(sender: CommandSender?, cycle: Int): Int {
        return object : BukkitRunnable() {
            override fun run() {
                val rf = TpsDataCollector(instance)
                val tps = rf.recentTps
                if (sender != null) {
                    sender.sendMessage(ChatColor.GOLD.toString() + "1ms:" + ChatColor.AQUA + "" + tps[0] +
                            ChatColor.GOLD + "5ms:" + ChatColor.AQUA + "" + tps[1] +
                            ChatColor.GOLD + "15ms:" + ChatColor.AQUA + "" + tps[2])
                    return
                }
            }
        }.runTaskTimer(this, 0, cycle.toLong()).taskId
    }

    private fun displayInfo(sender: CommandSender) {
        sender.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "- WorldRiptideCanceller -")
        sender.sendMessage(ChatColor.WHITE.toString() + "SpigotAPIバージョン : " + description.apiVersion)
        sender.sendMessage(ChatColor.WHITE.toString() + "Pluginバージョン : " + description.version)
        sender.sendMessage(ChatColor.RED.toString() + "ダウンロードURL : " + siteURL)
        sender.sendMessage(ChatColor.DARK_BLUE.toString() + "Developed by rnlin(Twitter: @rnlin)")
        sender.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "--------")
    }

    fun info(s: String?) {
        logger.info(s)
    }

    companion object {
        @JvmField
        var isEnable = true
        @JvmField
        var endMessage: String? = null
        @JvmField
        var startMessage: String? = null
        @JvmField
        var cancelMessage: String? = null
        @JvmField
        var tpsThreshold = 17.0
        var updateFrequency = 20 * 60
        @JvmField
        var enableRegeneration = true
        var instance: MamiyaSystemPlugin? = null
            private set
        private val bukkitIdManager = HashMap<Player, Int?>()
        @JvmField
        var originWorldName = "origin"
        @JvmField
        var COMMANDS = arrayOf("ms")
    }
}