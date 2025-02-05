package xyz.fireworkwars.core.communication

import org.bukkit.entity.Player

@Suppress("unused")
interface FireworkWarsPluginData {
    fun getArenaJoinCommand(): ArenaJoinCommand
    fun getArenaLeaveCommand(): ArenaLeaveCommand
    fun getBarracksArenas(): List<Arena>
    fun getTownArenas(): List<Arena>
    fun getVersionString(): String
    fun hidePlayersBetweenDifferentGames()
    fun isInPlayingGame(player: Player): Boolean
}

@Suppress("unused")
interface ArenaJoinCommand {
    fun executeJoinForPlayer(player: Player, arenaNumber: Int)
}

@Suppress("unused")
interface ArenaLeaveCommand {
    fun executeLeaveForPlayer(player: Player)
}

@Suppress("unused")
interface Arena {
    fun getArenaNumber(): Int
    fun getName(): String
    fun getDescription(): String
    fun getMinPlayers(): Int
    fun getMaxPlayers(): Int
    fun getCurrentPlayers(): Int
    fun isAvailable(): Boolean
}

