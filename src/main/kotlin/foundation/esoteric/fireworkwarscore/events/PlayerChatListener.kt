package foundation.esoteric.fireworkwarscore.events

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.interfaces.Event
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.managers.ChatChannelManager
import foundation.esoteric.fireworkwarscore.util.sendMessage
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class PlayerChatListener(private val plugin: FireworkWarsCorePlugin) : Listener, Event {
    private val channelManager = plugin.channelManager
    private val messageManager = plugin.privateMessageManager

    override fun register() {
        plugin.registerEvent(this)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player

        when (channelManager.getChannel(player.uniqueId)) {
            ChatChannelManager.ChatChannel.ALL -> this.handleAllChat(event, player)
            ChatChannelManager.ChatChannel.FRIEND -> this.handleFriendChat(event, player)
            ChatChannelManager.ChatChannel.PARTY -> {
                event.isCancelled = true
            }
        }
    }

    private fun handleAllChat(event: AsyncChatEvent, player: Player) {
        if (messageManager.playersWithExpiredChannel.contains(player.uniqueId)) {
            messageManager.playersWithExpiredChannel.remove(player.uniqueId)
            event.isCancelled = true

            return player.sendMessage(Message.CHANNEL_EXPIRED)
        }
    }

    private fun handleFriendChat(event: AsyncChatEvent, player: Player) {
        val targetId = messageManager.getChannelRecipient(player.uniqueId)

        if (targetId != null) {
            val target = plugin.server.getPlayer(targetId)
            if (target != null) {
                plugin.messageCommand.messagePlayer(player, target, event.signedMessage().message())
            } else {
                player.sendMessage(Message.PLAYER_NOT_ONLINE)
                messageManager.removeChannel(player.uniqueId)
            }
        }

        event.isCancelled = true
    }
}