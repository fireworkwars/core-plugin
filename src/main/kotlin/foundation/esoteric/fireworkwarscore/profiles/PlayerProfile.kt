package foundation.esoteric.fireworkwarscore.profiles

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Suppress("unused")
data class PlayerProfile(
    val uuid: UUID,
    var username: String,
    var language: String,
    var rank: Rank,
    val stats: PlayerStats,
    val achievements: MutableList<Any>,
    val friends: MutableList<UUID>,
    val blocked: MutableList<UUID>,
    var firstJoin: Boolean = true,
    var firstJoinDate: Long = System.currentTimeMillis(),
    var lastSeenDate: Long = System.currentTimeMillis()
) {
    fun formattedName(): Component {
        return rank.formatPlayerName(Component.text(username))
    }

    fun updateOwnTablist() {
        return rank.updateTablist(Bukkit.getPlayer(uuid)!!)
    }

    fun getOnlineFriends(): List<Player> {
        return friends.mapNotNull { Bukkit.getPlayer(it) }
    }
}

data class PlayerStats(
    var kills: Int,
    var deaths: Int,
    var totalDamageDealt: Double,
    var wins: Int,
    var losses: Int,
    var gamesPlayed: Int,
    var currentWinStreak: Int,
    var highestWinStreak: Int
) {
    companion object {
        fun default(): PlayerStats {
            return PlayerStats(0, 0, 0.0, 0, 0, 0, 0, 0)
        }
    }

    fun getKillDeathRatio(): Double {
        return if (deaths == 0) kills.toDouble()
        else kills.toDouble() / deaths.toDouble()
    }

    fun getWinLossRatio(): Double {
        return if (losses == 0) wins.toDouble()
        else wins.toDouble() / losses.toDouble()
    }

    fun getWinPercentage(): Double {
        return if (gamesPlayed == 0) 0.0
        else wins.toDouble() / gamesPlayed.toDouble() * 100
    }
}