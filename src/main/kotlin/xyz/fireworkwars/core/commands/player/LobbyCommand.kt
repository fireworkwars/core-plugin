package xyz.fireworkwars.core.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin

class LobbyCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("lobby") {
    init {
        setRequirements { it is Player }
        withPermission(CommandPermission.NONE)

        withShortDescription("Return to the lobby")
        withFullDescription("Return to the lobby")
        withAliases("l", "hub")

        executesPlayer(this::onPlayerExecution)
        register(plugin)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPlayerExecution(player: Player, args: CommandArguments) {
        plugin.fireworkWarsPluginData.getArenaLeaveCommand().executeLeaveForPlayer(player)
    }
}