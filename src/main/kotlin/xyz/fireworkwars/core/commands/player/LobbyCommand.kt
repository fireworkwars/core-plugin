package xyz.fireworkwars.core.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin

class LobbyCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("lobby") {
    init {
        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("Return to the lobby")
        this.withFullDescription("Return to the lobby")
        this.withAliases("l", "hub")

        this.executesPlayer(this::onPlayerExecution)
        this.register(plugin)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPlayerExecution(player: Player, args: CommandArguments) {
        plugin.fireworkWarsPluginData.getArenaLeaveCommand().executeLeaveForPlayer(player)
    }
}