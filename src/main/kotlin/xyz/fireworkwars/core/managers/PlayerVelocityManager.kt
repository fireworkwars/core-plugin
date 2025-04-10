package xyz.fireworkwars.core.managers

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.interfaces.Event
import java.util.*

class PlayerVelocityManager(private val plugin: FireworkWarsCorePlugin) : Event {
    private val playerVelocityMap: MutableMap<UUID, Vector> = HashMap()
    private val playerPreviousPositionMap: MutableMap<UUID, Vector> = HashMap()

    fun init() {
        plugin.runTaskTimer(0L, 1L, this::updatePlayerVelocities)

        this.register()
    }

    override fun register() {
        plugin.registerEvent(this)
    }

    @Suppress("unused")
    fun getPlayerVelocity(player: Player): Vector {
        return playerVelocityMap[player.uniqueId]!!
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerVelocityMap.remove(event.player.uniqueId)
        playerPreviousPositionMap.remove(event.player.uniqueId)
    }

    private fun updatePlayerVelocities() {
        for (player in plugin.server.onlinePlayers) {
            val previousPosition = playerPreviousPositionMap[player.uniqueId]

            if (previousPosition == null) {
                playerPreviousPositionMap[player.uniqueId] = player.location.toVector()
                continue
            }

            val velocity = player.location.toVector().subtract(previousPosition)
            playerVelocityMap[player.uniqueId] = velocity
            playerPreviousPositionMap[player.uniqueId] = player.location.toVector()
        }
    }
}