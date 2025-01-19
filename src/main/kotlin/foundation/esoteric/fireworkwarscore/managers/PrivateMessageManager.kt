package foundation.esoteric.fireworkwarscore.managers

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.managers.ChatChannelManager.ChatChannel
import org.bukkit.scheduler.BukkitTask
import java.util.*

class PrivateMessageManager(private val plugin: FireworkWarsCorePlugin) {
    private val chatChannelManager = plugin.channelManager

    private val lastMessageFrom = mutableMapOf<UUID, UUID>()
    private val playerCurrentChannels = mutableMapOf<UUID, UUID>()
    private val currentChannelExpiryTasks = mutableMapOf<UUID, BukkitTask>()
    val playersWithExpiredChannel = mutableSetOf<UUID>()

    fun getLastMessageSender(player: UUID): UUID? {
        return lastMessageFrom[player]
    }

    /**
     * Set the last player that sent a message to the player
     * @param player The player who received the message
     * @param lastMessaged The player who sent the message
     */
    fun setLastMessageSender(player: UUID, lastMessaged: UUID) {
        this.lastMessageFrom[player] = lastMessaged
    }

    fun removeLastMessageSender(player: UUID) {
        lastMessageFrom.remove(player)
    }

    fun getChannelRecipient(player: UUID): UUID? {
        return playerCurrentChannels[player]
    }

    fun setChannelRecipient(player: UUID, channel: UUID) {
        this.playerCurrentChannels[player] = channel
        chatChannelManager.setChannel(player, ChatChannel.FRIEND)
    }

    fun removeChannel(player: UUID) {
        playerCurrentChannels.remove(player)
        currentChannelExpiryTasks[player]?.cancel()
        playersWithExpiredChannel.add(player)
        chatChannelManager.setChannel(player, ChatChannel.ALL)
    }

    fun setChannelExpiry(player: UUID, expireTicks: Int) {
        currentChannelExpiryTasks[player]?.cancel()

        this.currentChannelExpiryTasks[player] = plugin.runTaskLater({
            this.removeChannel(player)
        }, expireTicks.toLong())
    }

    fun cancelChannelExpiry(player: UUID) {
        currentChannelExpiryTasks[player]?.cancel()
    }
}