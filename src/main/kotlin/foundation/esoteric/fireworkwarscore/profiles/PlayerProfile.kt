package foundation.esoteric.fireworkwarscore.profiles

import java.util.*

data class PlayerProfile(
    val uuid: UUID,
    var language: String,
    var rank: Rank,
    var firstJoin: Boolean = true
)