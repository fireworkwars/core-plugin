package xyz.fireworkwars.core.events

import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.interfaces.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.FoodLevelChangeEvent

class PlayerLoseHungerListener(private val plugin: FireworkWarsCorePlugin) : Event {
    override fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerLoseHunger(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }
}