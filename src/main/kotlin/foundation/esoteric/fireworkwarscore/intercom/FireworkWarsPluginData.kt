package foundation.esoteric.fireworkwarscore.intercom

import org.bukkit.entity.Player

interface FireworkWarsPluginData {
    fun getArenaJoinCommand(): ArenaJoinCommand
    fun getBarracksArenas(): List<Arena>
    fun getTownArenas(): List<Arena>
}

interface ArenaJoinCommand {
    fun executeForPlayer(player: Player, arenaNumber: Int)
}

interface Arena {
    fun getArenaNumber(): Int
    fun getName(): String
    fun getDescription(): String
    fun getMinPlayers(): Int
    fun getMaxPlayers(): Int
}

