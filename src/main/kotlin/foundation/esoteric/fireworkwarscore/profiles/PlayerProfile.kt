package foundation.esoteric.fireworkwarscore.profiles

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.util.toFixed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
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
    val dailyStats: PlayerStats,
    val weeklyStats: PlayerStats,
    val achievements: MutableList<Any>,
    val friends: MutableList<UUID>,
    val blocked: MutableList<UUID>,
    var firstJoin: Boolean = true,
    var firstJoinDate: Long = System.currentTimeMillis(),
    var lastSeenDate: Long = System.currentTimeMillis()
) {
    companion object {
        fun createDefault(uuid: UUID, plugin: FireworkWarsCorePlugin): PlayerProfile {
            return PlayerProfile(
                uuid = uuid,
                username = plugin.server.getOfflinePlayer(uuid).name ?: "Unknown",
                language = plugin.languageManager.defaultLanguage,
                rank = Rank.NONE,
                stats = PlayerStats.default(),
                dailyStats = PlayerStats.default(),
                weeklyStats = PlayerStats.default(),
                achievements = mutableListOf(),
                friends = mutableListOf(),
                blocked = mutableListOf(),
                firstJoin = true,
                firstJoinDate = System.currentTimeMillis(),
                lastSeenDate = System.currentTimeMillis())
        }
    }

    fun formattedName(): Component {
        val text = rank.formatPlayerName(username)
            .appendNewline()
            .append(text("Click to view ", NamedTextColor.GRAY))
            .append(rank.colorPlayerName(username))
            .append(text("'s profile", NamedTextColor.GRAY))

        return rank
            .formatPlayerName(username)
            .clickEvent(ClickEvent.runCommand("/profile $username"))
            .hoverEvent(HoverEvent.showText(text))
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
        return if (deaths == 0) kills.toDouble().toFixed(2)
        else (kills.toDouble() / deaths.toDouble()).toFixed(2)
    }

    fun getWinLossRatio(): Double {
        return if (losses == 0) wins.toDouble().toFixed(2)
        else (wins.toDouble() / losses.toDouble()).toFixed(2)
    }

    fun getWinPercentage(): Double {
        return if (gamesPlayed == 0) 0.0
        else (wins.toDouble() / gamesPlayed.toDouble() * 100).toFixed(2)
    }
}