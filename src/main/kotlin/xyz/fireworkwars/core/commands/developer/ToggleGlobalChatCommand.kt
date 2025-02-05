package xyz.fireworkwars.core.commands.developer

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.util.getMessage
import xyz.fireworkwars.core.util.sendMessage
import java.util.function.Predicate

class ToggleGlobalChatCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("toggle-gc") {
    init {
        this.requirements = Predicate { it.isOp }

        this.withShortDescription("Toggle global chat")
        this.withFullDescription("Toggle global chat")

        this.executesPlayer(this::executesPlayer)
        this.register(plugin)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun executesPlayer(player: Player, args: CommandArguments) {
        plugin.isGlobalChatEnabled = !plugin.isGlobalChatEnabled

        val status =
            if (plugin.isGlobalChatEnabled) player.getMessage(Message.ENABLED)
            else player.getMessage(Message.DISABLED)

        player.sendMessage(Message.GLOBAL_CHAT_STATUS, status)
    }
}