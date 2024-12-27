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
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-= Firework Wars Lobby Plugin =-=-=-=-=-=-=-=-=-=-=-=-=-=")
        logger.info("This is the start of Firework Wars Lobby Plugin logs.")
        logger.info("Info: v" + pluginMeta.version + " by " + pluginMeta.website)
        logger.info("=-=-=-=-=-=-=-=-=-=-=-=-= End of Plugin Info =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")

        logger.info("Finished loading player profiles.")
        logger.info("Loaded ${playerDataManager.size} player profiles.")

        logger.info("Finished initialising language manager.")
        logger.info("Loaded ${languageManager.totalMessages} messages across ${languageManager.totalLanguages} languages.")
    }

    override fun onEnable() {
        dataFolder.mkdir()
        saveDefaultConfig()
    }

    override fun onDisable() {
        playerDataManager.save()
    }
}
