package foundation.esoteric.fireworkwarscore.communication

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

@Suppress("unused")
interface LobbyPluginData {
    fun getLobbySpawn(): Location
    fun isLobby(world: World): Boolean
    fun updateScoreboards()
    fun setTagVisibility(player: Player, visible: Boolean)
}