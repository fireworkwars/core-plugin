package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.util.sendMessage
import org.bukkit.entity.Player

class ReplyCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("reply") {
    private val messageArgumentNodeName = "message"

    private val messageManager = plugin.privateMessageManager
    private val playerDataManager = plugin.playerDataManager

    init {
        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("Reply to the last player")
        this.withFullDescription("Reply to the last player who messaged you")
        this.withAliases("r")

        this.withArguments(this.messageArgumentSupplier())
        this.executesPlayer(this::onPlayerExecution)

        this.register(plugin)
    }

    private fun messageArgumentSupplier(): GreedyStringArgument {
        return GreedyStringArgument(messageArgumentNodeName)
    }

    private fun onPlayerExecution(player: Player, args: CommandArguments) {
        val lastMessaged = messageManager.getLastMessageSender(player.uniqueId)
            ?: return player.sendMessage(Message.NO_ONE_TO_REPLY_TO)

        val target = plugin.server.getPlayer(lastMessaged)
            ?: return player.sendMessage(Message.PLAYER_NOT_ONLINE)

        val message = args[messageArgumentNodeName] as String

        val profile = playerDataManager.getPlayerProfile(player.uniqueId)
        val targetProfile = playerDataManager.getPlayerProfile(target.uniqueId)

        target.sendMessage(Message.MESSAGE_FROM, profile.formattedName(), message)
        player.sendMessage(Message.MESSAGE_TO, targetProfile.formattedName(), message)

        messageManager.setLastMessageSender(target.uniqueId, player.uniqueId)
    }
}