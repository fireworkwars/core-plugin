package foundation.esoteric.fireworkwarscore

import foundation.esoteric.fireworkwarscore.intercom.FireworkWarsPluginData
import foundation.esoteric.fireworkwarscore.intercom.LobbyPluginData
import org.bukkit.plugin.java.JavaPlugin

class FireworkWarsCorePlugin : JavaPlugin() {
    lateinit var fireworkWarsPluginData: FireworkWarsPluginData
    lateinit var lobbyPluginData: LobbyPluginData

    override fun onEnable() {
        // Plugin startup logic
    }
}
