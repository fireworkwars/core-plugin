package xyz.fireworkwars.core.util

import org.bukkit.scheduler.BukkitTask
import xyz.fireworkwars.core.FireworkWarsCorePlugin

class ExpiryManager(private val plugin: FireworkWarsCorePlugin, private val expiryDuration: Long) {
    private val expiryTasks: MutableMap<String, BukkitTask> = mutableMapOf()

    fun addExpiryTask(id: String, onExpire: Runnable) {
        val task = plugin.runTaskLater(expiryDuration) {
            onExpire.run()
            expiryTasks.remove(id)
        }

        expiryTasks[id] = task
    }

    fun removeExpiryTask(id: String) {
        expiryTasks.remove(id)?.cancel()
    }
}