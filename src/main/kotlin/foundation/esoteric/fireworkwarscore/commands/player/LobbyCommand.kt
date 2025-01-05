package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import org.bukkit.entity.Player

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