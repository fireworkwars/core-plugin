package foundation.esoteric.fireworkwarscore.commands.developer

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import org.bukkit.entity.Player
import java.util.function.Predicate

class GetListOrderInfo(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("getlistorderinfo") {
    init {
        this.requirements = Predicate { it.isOp }

        this.withShortDescription("Get info about the player tablist order")
        this.withFullDescription("Get info about the player tablist order.")

        this.executesPlayer(this::executesPlayer)
        this.register(plugin)
    }

    private fun executesPlayer(player: Player, args: CommandArguments) {
        plugin.server.onlinePlayers.forEach {
            player.sendMessage("Player: ${it.name} - Tablist Order: ${it.playerListOrder}")
        }
    }
}