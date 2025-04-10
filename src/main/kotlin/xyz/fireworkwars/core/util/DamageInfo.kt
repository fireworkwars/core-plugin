package xyz.fireworkwars.core.util

import java.util.*

@JvmRecord
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class DamageInfo(val damage: Double, val time: Int, val damager: UUID?) {
    fun affectedBy(currentTick: Int): Boolean {
        return currentTick - time < 10
    }

    fun cancels(damage: Double, currentTick: Int): Boolean {
        return damage < this.damage && affectedBy(currentTick)
    }

    companion object {
        fun of(damage: Double, time: Int, damager: UUID?): DamageInfo {
            return DamageInfo(damage, time, damager)
        }

        fun empty(): DamageInfo {
            return DamageInfo(0.0, 0, null)
        }
    }
}
