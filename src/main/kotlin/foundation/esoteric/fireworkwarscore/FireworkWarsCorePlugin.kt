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

    override fun onEnable() {
        // Plugin startup logic
    }
}
