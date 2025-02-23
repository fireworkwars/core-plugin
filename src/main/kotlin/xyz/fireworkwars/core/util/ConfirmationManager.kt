package xyz.fireworkwars.core.util

import java.util.*

@Suppress("unused")
class ConfirmationManager {
    private val confirmed = mutableListOf<UUID>()

    fun isConfirmed(uuid: UUID): Boolean {
        return confirmed.contains(uuid)
    }

    fun addConfirmation(uuid: UUID) {
        confirmed.add(uuid)
    }

    fun removeConfirmation(uuid: UUID) {
        confirmed.remove(uuid)
    }
}