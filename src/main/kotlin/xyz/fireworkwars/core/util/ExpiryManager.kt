package xyz.fireworkwars.core.util

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class ExpiryManager(private val plugin: JavaPlugin, private val expiryDuration: Long) {
    private val expiryTasks: MutableMap<String, BukkitTask> = mutableMapOf()

    fun addExpiryTask(id: String, onExpire: Runnable) {
        if (expiryTasks.containsKey(id)) {
            throw IllegalArgumentException("Expiry task with id $id already exists")
        }

        val task = plugin.server.scheduler.runTaskLater(plugin, Runnable {
            onExpire.run()
            expiryTasks.remove(id)
        }, expiryDuration)

        expiryTasks[id] = task
    }

    fun removeExpiryTask(id: String) {
        expiryTasks.remove(id)?.cancel()
    }
}