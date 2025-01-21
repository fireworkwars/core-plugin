package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.util.sendMessage
import org.bukkit.entity.Player

class MessageCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("message") {
    private val channelExpiryTicks = 6000

    private val targetArgumentNodeName = "targetPlayer"
    private val messageArgumentNodeName = "message"

    private val messageManager = plugin.privateMessageManager
    private val playerDataManager = plugin.playerDataManager

    init {
        CommandAPI.unregister("msg")

        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("Message a player")
        this.withFullDescription("Message a player and create a channel for 5 minutes")
        this.withAliases("msg")

        this.withArguments(this.friendsArgumentSupplier(), this.messageArgumentSupplier())
        this.executesPlayer(this::onPlayerExecution)

        this.register(plugin)
    }

    private fun friendsArgumentSupplier(): Argument<String> {
        return StringArgument(targetArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val profile = playerDataManager.getPlayerProfile(player)

            val friendUUIDs = profile.friends
            val playerNames = friendUUIDs.mapNotNull {
                playerDataManager.getPlayerProfile(it, false)?.username
            }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun messageArgumentSupplier(): Argument<String> {
        return GreedyStringArgument(messageArgumentNodeName).setOptional(true)
    }

    private fun onPlayerExecution(player: Player, args: CommandArguments) {
        val target = plugin.server.getPlayer(args[targetArgumentNodeName] as String)
            ?: return player.sendMessage(Message.PLAYER_NOT_ONLINE)

        val message = args.getOrDefault(messageArgumentNodeName, "") as String

        this.messagePlayer(player, target, message)
    }

    fun messagePlayer(player: Player, target: Player, message: String) {
        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_MESSAGE_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player.uniqueId)
        val targetProfile = playerDataManager.getPlayerProfile(target.uniqueId)

        if (targetProfile.blocked.contains(player.uniqueId)) {
            return player.sendMessage(Message.YOU_HAVE_BEEN_BLOCKED)
        }

        messageManager.setLastMessageSender(target.uniqueId, player.uniqueId)

        if (message.isNotEmpty()) {
            target.sendMessage(Message.MESSAGE_FROM, profile.formattedName(), message)
            player.sendMessage(Message.MESSAGE_TO, targetProfile.formattedName(), message)

            if (target.uniqueId == messageManager.getChannelRecipient(player.uniqueId)) {
                messageManager.setChannelExpiry(player.uniqueId, channelExpiryTicks)
            }
        } else {
            messageManager.setChannelRecipient(player.uniqueId, target.uniqueId)
            messageManager.setChannelExpiry(player.uniqueId, channelExpiryTicks)

            player.sendMessage(Message.CHANNEL_CREATED, targetProfile.formattedName())
        }
    }
}