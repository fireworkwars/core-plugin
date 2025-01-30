package foundation.esoteric.fireworkwarscore

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.CommandAPICommand
import foundation.esoteric.fireworkwarscore.commands.aliases.FriendListCommandAlias
import foundation.esoteric.fireworkwarscore.commands.developer.ToggleBuildCommand
import foundation.esoteric.fireworkwarscore.commands.developer.ToggleDebugCommand
import foundation.esoteric.fireworkwarscore.commands.developer.ToggleGlobalChatCommand
import foundation.esoteric.fireworkwarscore.commands.operator.SetRankCommand
import foundation.esoteric.fireworkwarscore.commands.player.*
import foundation.esoteric.fireworkwarscore.communication.FireworkWarsPluginData
import foundation.esoteric.fireworkwarscore.communication.LobbyPluginData
import foundation.esoteric.fireworkwarscore.config.PluginConfig
import foundation.esoteric.fireworkwarscore.events.PlayerChatListener
import foundation.esoteric.fireworkwarscore.events.PlayerInteractListener
import foundation.esoteric.fireworkwarscore.events.PlayerLoseHungerListener
import foundation.esoteric.fireworkwarscore.interfaces.Event
import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.managers.ChatChannelManager
import foundation.esoteric.fireworkwarscore.managers.FriendManager
import foundation.esoteric.fireworkwarscore.managers.PrivateMessageManager
import foundation.esoteric.fireworkwarscore.maps.MapManager
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import foundation.esoteric.fireworkwarscore.stats.StatResetScheduler
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FireworkWarsCorePlugin : JavaPlugin() {
    lateinit var playerDataManager: PlayerDataManager
    lateinit var languageManager: LanguageManager

    lateinit var fireworkWarsPluginData: FireworkWarsPluginData
    lateinit var lobbyPluginData: LobbyPluginData

    lateinit var pluginConfig: PluginConfig

    private val mapManager = MapManager(this)

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

    init {
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Firework Wars Core =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        logger.info("This is the start of Firework Wars Core Plugin logs.")
        @Suppress("UnstableApiUsage")
        logger.info("Info: v" + pluginMeta.version + " by " + pluginMeta.website)
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= End of Plugin Info =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")

        logger.info("Saving Firework Wars maps...")

        mapManager.saveMaps()
    }

    override fun onLoad() {
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
