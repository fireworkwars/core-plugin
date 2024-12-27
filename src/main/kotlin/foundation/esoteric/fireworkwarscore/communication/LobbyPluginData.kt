package foundation.esoteric.fireworkwarscore.communication

import org.bukkit.Location
import org.bukkit.World

interface LobbyPluginData {
    fun isLobby(world: World): Boolean
    fun getLobbySpawn(): Location
}