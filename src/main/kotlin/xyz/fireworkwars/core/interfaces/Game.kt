package xyz.fireworkwars.core.interfaces

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

/**
 * Represents a very abstract form of an instance of some kind of Firework Wars game
 */
@Suppress("unused")
interface Game {
    fun getPlayers(): List<GamePlayer>
    fun getBukkitPlayers(): List<Player>
    fun getScheduledTasks(): MutableList<BukkitTask>
}