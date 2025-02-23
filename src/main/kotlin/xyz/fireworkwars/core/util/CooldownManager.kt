package xyz.fireworkwars.core.util

import org.bukkit.plugin.java.JavaPlugin
import java.util.*

@Suppress("unused")
class CooldownManager(private val plugin: JavaPlugin, private val cooldownTicks: Int) {
    private val cooldowns = mutableMapOf<UUID, Int>()

    fun isOnCooldown(uuid: UUID): Boolean {
        val lastUse = cooldowns[uuid] ?: Int.MIN_VALUE
        val currentTick = plugin.server.currentTick

        return lastUse + cooldownTicks > currentTick
    }

    fun setCooldown(uuid: UUID) {
        cooldowns[uuid] = plugin.server.currentTick
    }

    fun removeCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    fun remainingCooldownTicks(uuid: UUID): Int {
        val lastUse = cooldowns[uuid] ?: return 0
        val currentTick = plugin.server.currentTick

        return (lastUse + cooldownTicks) - currentTick
    }
}