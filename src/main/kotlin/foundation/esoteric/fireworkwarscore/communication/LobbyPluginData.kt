package foundation.esoteric.fireworkwarscore.communication

import org.bukkit.Location
import org.bukkit.World

@Suppress("unused")
interface LobbyPluginData {
    fun getLobbySpawn(): Location
    fun isLobby(world: World): Boolean
    fun updateScoreboards()
}