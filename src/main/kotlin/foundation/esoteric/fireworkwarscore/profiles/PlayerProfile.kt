package foundation.esoteric.fireworkwarscore.profiles

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*

data class PlayerProfile(
    val uuid: UUID,
    var language: String,
    var rank: Rank,
    val achievements: MutableList<Any>,
    val friends: MutableList<UUID>,
    val blocked: MutableList<UUID>,
    var firstJoin: Boolean = true
) {
    fun formattedName(): Component {
        return rank.formatPlayerName(Bukkit.getPlayer(uuid)!!)
    }

    fun updateOwnTablist() {
        return rank.updateTablist(Bukkit.getPlayer(uuid)!!)
    }
}