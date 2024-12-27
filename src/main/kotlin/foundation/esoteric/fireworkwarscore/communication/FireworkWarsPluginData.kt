package foundation.esoteric.fireworkwarscore.communication

import org.bukkit.entity.Player

@Suppress("unused")
interface FireworkWarsPluginData {
    fun getArenaJoinCommand(): ArenaJoinCommand
    fun getBarracksArenas(): List<Arena>
    fun getTownArenas(): List<Arena>
}

@Suppress("unused")
interface ArenaJoinCommand {
    fun executeForPlayer(player: Player, arenaNumber: Int)
}

@Suppress("unused")
interface Arena {
    fun getArenaNumber(): Int
    fun getName(): String
    fun getDescription(): String
    fun getMinPlayers(): Int
    fun getMaxPlayers(): Int
}

