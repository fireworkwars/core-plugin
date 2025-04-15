package xyz.fireworkwars.core.managers

import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.interfaces.GamePlayer
import java.util.function.Function

@Suppress("unused")
class GameManager(private val plugin: FireworkWarsCorePlugin) {
    private val gamePlayerSources = mutableListOf<Function<Player, GamePlayer?>>()

    fun addGamePlayerSource(source: Function<Player, GamePlayer?>) {
        gamePlayerSources.add(source)
    }

    fun getGamePlayer(player: Player): GamePlayer? {
        return gamePlayerSources
            .mapNotNull { it.apply(player) }
            .getOrNull(0)
    }
}