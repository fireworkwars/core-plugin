package xyz.fireworkwars.core.interfaces

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Represents a very abstract form of a player wrapper for a Firework Wars game.
 */
@Suppress("unused")
interface GamePlayer {
    fun bukkitPlayer(): Player
    fun getColor(): Color
    fun getColoredName(): Component
    fun getWoolMaterial(): Material
    fun getGame(): Game
}