package foundation.esoteric.fireworkwarscore

import foundation.esoteric.fireworkwarscore.communication.FireworkWarsPluginData
import foundation.esoteric.fireworkwarscore.communication.LobbyPluginData
import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager

@Suppress("unused")
class FireworkWarsCorePlugin : BasePlugin() {
    override val playerDataManager: PlayerDataManager = PlayerDataManager(this)
    override val languageManager: LanguageManager = LanguageManager(this)

    lateinit var fireworkWarsPluginData: FireworkWarsPluginData
    lateinit var lobbyPluginData: LobbyPluginData

    @Suppress("UnstableApiUsage")
    override fun onLoad() {
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Firework Wars Core =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        logger.info("This is the start of Firework Wars Core Plugin logs.")
        logger.info("Info: v" + pluginMeta.version + " by " + pluginMeta.website)
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= End of Plugin Info =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
    }

    override fun onEnable() {
        logger.info("Starting core initialisation sequence...")
        logger.info("Creating data folder...")

        dataFolder.mkdir()
        saveDefaultConfig()

        logger.info("Successfully created data folder and loaded default configurations.")
        logger.info("Loading player profiles...")

        this.playerDataManager = PlayerDataManager(this)

        logger.info("Finished loading player profiles.")
        logger.info("Loaded ${playerDataManager.size} player profiles.")

        logger.info("Initialising language manager...")

        this.languageManager = LanguageManager(this)

        logger.info("Finished initialising language manager.")
        logger.info("Loaded ${languageManager.totalMessages} messages across ${languageManager.totalLanguages} languages.")

        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Firework Wars Core =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        logger.info("End of logs for Firework Wars Core Plugin.")
        logger.info("Finished Firework Wars Core initialisation sequence.")
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= All systems operational  =-=-=-=-=-=-=-=-=-=-=-=-=")
    }

    override fun onDisable() {
        playerDataManager.save()
    }
}
