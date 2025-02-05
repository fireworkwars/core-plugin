package xyz.fireworkwars.core.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.CommandArguments
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.util.sendMessage
import org.bukkit.entity.Player

class AllChatCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("allchat") {
    private val messageManager = plugin.privateMessageManager

    init {
        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("Leave private message channel")
        this.withFullDescription("Leave the current private message channel and return to all chat.")
        this.withAliases("ac")

        this.executesPlayer(this::onPlayerExecution)

        this.register(plugin)
    }

    private fun onPlayerExecution(player: Player, args: CommandArguments) {
        val currentChannel = messageManager.getChannelRecipient(player.uniqueId)

        if (currentChannel != null) {
            messageManager.removeChannel(player.uniqueId)
            messageManager.playersWithExpiredChannel.remove(player.uniqueId)
        }

        player.sendMessage(Message.SWITCHED_TO_ALL_CHAT)
    }
}