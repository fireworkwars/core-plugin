package foundation.esoteric.fireworkwarscore.profiles

import java.util.*

data class PlayerProfile(
    val uuid: UUID,
    var language: String,
    var rank: Rank,
    val achievements: List<Any>,
    val friends: List<UUID>,
    val blocked: List<UUID>,
    var firstJoin: Boolean = true
)