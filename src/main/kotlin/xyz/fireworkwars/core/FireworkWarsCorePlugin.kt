package xyz.fireworkwars.core

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.CommandAPICommand
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import xyz.fireworkwars.core.commands.aliases.FriendListCommandAlias
import xyz.fireworkwars.core.commands.developer.ToggleBuildCommand
import xyz.fireworkwars.core.commands.developer.ToggleDebugCommand
import xyz.fireworkwars.core.commands.developer.ToggleGlobalChatCommand
import xyz.fireworkwars.core.commands.operator.SetRankCommand
import xyz.fireworkwars.core.commands.player.*
import xyz.fireworkwars.core.communication.FireworkWarsServiceProvider
import xyz.fireworkwars.core.communication.LobbyServiceProvider
import xyz.fireworkwars.core.config.PluginConfig
import xyz.fireworkwars.core.events.PlayerChatListener
import xyz.fireworkwars.core.events.PlayerInteractListener
import xyz.fireworkwars.core.events.PlayerLoseHungerListener
import xyz.fireworkwars.core.interfaces.Event
import xyz.fireworkwars.core.language.LanguageManager
import xyz.fireworkwars.core.managers.ChatChannelManager
import xyz.fireworkwars.core.managers.FriendManager
import xyz.fireworkwars.core.managers.PrivateMessageManager
import xyz.fireworkwars.core.profiles.PlayerDataManager
import xyz.fireworkwars.core.stats.StatResetScheduler

@Suppress("unused", "MemberVisibilityCanBePrivate", "UnstableApiUsage")
class FireworkWarsCorePlugin : JavaPlugin() {
    lateinit var playerDataManager: PlayerDataManager
    lateinit var languageManager: LanguageManager

    lateinit var fireworkWarsServiceProvider: FireworkWarsServiceProvider
    lateinit var lobbyServiceProvider: LobbyServiceProvider

    lateinit var pluginConfig: PluginConfig

    val channelManager = ChatChannelManager(this)
    val friendManager: FriendManager = FriendManager(this)
    val privateMessageManager = PrivateMessageManager(this)

    lateinit var friendCommand: FriendCommand
    lateinit var blockCommand: BlockCommand
    lateinit var messageCommand: MessageCommand
    lateinit var profileCommand: ProfileCommand

    val statResetScheduler = StatResetScheduler(this)

    val mm = MiniMessage.miniMessage()

    var isDebugging = false
    var isBuildModeEnabled = false
    var isGlobalChatEnabled = false

    private val commandApiConfig = CommandAPIBukkitConfig(this)

    private val commands = mutableListOf<CommandAPICommand>()
    private val events = mutableListOf<Event>()

    override fun onLoad() {
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Firework Wars Core =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        logger.info("This is the start of Firework Wars Core Plugin logs.")
        logger.info("Info: v" + pluginMeta.version + " by " + pluginMeta.website)
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= End of Plugin Info =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")

        CommandAPI.onLoad(commandApiConfig)
    }

    override fun onEnable() {
        logger.info("Starting core initialisation sequence...")
        logger.info("Creating data folder...")

        dataFolder.mkdir()
        saveDefaultConfig()

        logger.info("Saved default configurations.")
        logger.info("Loading plugin data...")

        this.pluginConfig = PluginConfig(this)

        logger.info("Successfully created data folder and loaded plugin data.")
        logger.info("Loading player profiles...")

        this.playerDataManager = PlayerDataManager(this)

        logger.info("Finished loading player profiles.")
        logger.info("Loaded ${playerDataManager.size} player profiles.")

        logger.info("Initialising language manager...")

        this.languageManager = LanguageManager(this)

        logger.info("Finished initialising language manager.")
        logger.info("Loaded ${languageManager.totalMessages} messages across ${languageManager.totalLanguages} languages.")

        logger.info("Loading commands...")

        CommandAPI.onEnable()
        CommandAPI.unregister("msg")

        this.friendCommand = FriendCommand(this)
        this.blockCommand = BlockCommand(this)
        this.messageCommand = MessageCommand(this)
        this.profileCommand = ProfileCommand(this)

        commands.add(ToggleDebugCommand(this))
        commands.add(ToggleBuildCommand(this))
        commands.add(ToggleGlobalChatCommand(this))
        commands.add(SetLanguageCommand(this))
        commands.add(SetRankCommand(this))
        commands.add(friendCommand)
        commands.add(FriendListCommandAlias(this))
        commands.add(blockCommand)
        commands.add(messageCommand)
        commands.add(ReplyCommand(this))
        commands.add(AllChatCommand(this))
        commands.add(LobbyCommand(this))
        commands.add(ShoutCommand(this))
        commands.add(profileCommand)

        logger.info("Finished loading commands.")
        logger.info("Loaded ${commands.size} commands.")

        logger.info("Registering global event listeners...")

        events.add(PlayerLoseHungerListener(this).apply { register() })
        events.add(PlayerChatListener(this).apply { register() })
        events.add(PlayerInteractListener(this).apply { register() })
        events.add(channelManager.apply { register() })

        logger.info("Finished registering global event listeners.")
        logger.info("Registered ${events.size} event listeners.")

        logger.info("Starting daily & weekly stat resetting scheduler...")

        statResetScheduler.schedule()

        logger.info("Running stat resetting scheduler.")

        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Firework Wars Core =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        logger.info("End of logs for Firework Wars Core Plugin.")
        logger.info("Finished Firework Wars Core initialisation sequence.")
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= All systems operational  =-=-=-=-=-=-=-=-=-=-=-=-=")
    }

    override fun onDisable() {
        playerDataManager.save()
    }

    fun runTask(task: Runnable) {
        server.scheduler.runTask(this, task)
    }

    fun runTaskLater(delay: Long, task: Runnable): BukkitTask {
        return server.scheduler.runTaskLater(this, task, delay)
    }

    fun registerEvent(event: Listener) {
        server.pluginManager.registerEvents(event, this)
    }

    fun logLoudly(message: String, force: Boolean = false) {
        if (isDebugging || force) {
            server.broadcast(text(message))
        }
    }
}
