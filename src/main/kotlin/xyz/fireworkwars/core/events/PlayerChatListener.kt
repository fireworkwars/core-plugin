package xyz.fireworkwars.core.events

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.interfaces.Event
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.managers.ChatChannelManager
import xyz.fireworkwars.core.util.sendMessage

class PlayerChatListener(private val plugin: FireworkWarsCorePlugin) : Listener, Event {
    private val channelManager = plugin.channelManager
    private val privateMessageManager = plugin.privateMessageManager

    override fun register() {
        plugin.registerEvent(this)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun monitorPlayerChat(event: AsyncChatEvent) {
        if (plugin.isGlobalChatEnabled) {
            event.viewers().clear()
            event.viewers().addAll(plugin.server.onlinePlayers)
        }
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
        if (privateMessageManager.playersWithExpiredChannel.contains(player.uniqueId)) {
            privateMessageManager.playersWithExpiredChannel.remove(player.uniqueId)
            event.isCancelled = true

            return player.sendMessage(Message.CHANNEL_EXPIRED)
        }
    }

    private fun handleFriendChat(event: AsyncChatEvent, player: Player) {
        val targetId = privateMessageManager.getChannelRecipient(player.uniqueId)

        if (targetId != null) {
            val target = plugin.server.getPlayer(targetId)
            if (target != null) {
                plugin.messageCommand.messagePlayer(player, target, event.signedMessage().message())
            } else {
                player.sendMessage(Message.PLAYER_NOT_ONLINE)
                privateMessageManager.removeChannel(player.uniqueId)
            }
        }

        event.isCancelled = true
    }
}