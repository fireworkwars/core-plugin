package foundation.esoteric.fireworkwarscore.profiles

import java.util.*

data class PlayerProfile(
    val uuid: UUID,
    var language: String,
    var ranked: Boolean,
    var firstJoin: Boolean = true
)