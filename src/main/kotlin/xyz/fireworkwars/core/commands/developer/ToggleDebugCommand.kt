package xyz.fireworkwars.core.commands.developer

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.util.getMessage
import xyz.fireworkwars.core.util.sendMessage
import java.util.function.Predicate

class ToggleDebugCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("toggle-debug") {
    init {
        this.requirements = Predicate { it.isOp }

        this.withShortDescription("Toggle debugging mode")
        this.withFullDescription("Toggle debugging mode")

        this.executesPlayer(this::executesPlayer)
        this.register(plugin)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun executesPlayer(player: Player, args: CommandArguments) {
        plugin.isDebugging = !plugin.isDebugging

        val status =
            if (plugin.isDebugging) player.getMessage(Message.ENABLED)
            else player.getMessage(Message.DISABLED)

        player.sendMessage(Message.DEBUG_MODE_STATUS, status)
    }
}