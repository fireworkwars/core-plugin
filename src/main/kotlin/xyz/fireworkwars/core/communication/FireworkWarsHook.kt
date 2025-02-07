package xyz.fireworkwars.core.communication

import org.bukkit.entity.Player

@Suppress("unused")
interface FireworkWarsHook {
    fun getArenaJoinExecutor(): ArenaJoinExecutor
    fun getArenaLeaveExecutor(): ArenaLeaveExecutor
    fun getBarracksArenas(): List<LiveArenaData>
    fun getTownArenas(): List<LiveArenaData>
    fun getVersionString(): String
    fun resyncPlayerVisibility()
    fun isInPlayingGame(player: Player): Boolean

    interface ArenaJoinExecutor {
        fun executeJoinForPlayer(player: Player, arenaNumber: Int)
    }

    interface ArenaLeaveExecutor {
        fun executeLeaveForPlayer(player: Player)
    }

    interface LiveArenaData {
        fun getArenaNumber(): Int
        fun getName(): String
        fun getDescription(): String
        fun getMinPlayers(): Int
        fun getMaxPlayers(): Int
        fun getCurrentPlayers(): Int
        fun isAvailable(): Boolean
    }
}

