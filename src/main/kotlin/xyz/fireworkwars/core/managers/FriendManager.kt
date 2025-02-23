package xyz.fireworkwars.core.managers

import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.OfflinePlayer
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.util.ExpiryManager
import java.util.*

class FriendManager(plugin: FireworkWarsCorePlugin) {
    private val requestExpiryTime = 6000L
    private val expiryManager = ExpiryManager(plugin, requestExpiryTime)

    private val outgoingRequests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    private val receivingRequests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()

    fun addFriendRequest(sender: OfflinePlayer, receiver: OfflinePlayer, onExpire: BiConsumer<OfflinePlayer, OfflinePlayer>) {
        val senderUuid = sender.uniqueId
        val receiverUuid = receiver.uniqueId

        outgoingRequests.computeIfAbsent(senderUuid) { mutableListOf() }.add(receiverUuid)
        receivingRequests.computeIfAbsent(receiverUuid) { mutableListOf() }.add(senderUuid)

        expiryManager.addExpiryTask(this.getId(sender, receiver)) {
            this.removeRequestData(sender, receiver)
            onExpire.accept(sender, receiver)
        }
    }

    fun hasRequest(sender: OfflinePlayer, receiver: OfflinePlayer): Boolean {
        return this.getOutgoingRequestUUIDs(sender).contains(receiver.uniqueId)
    }

    fun hasMutualRequests(player1: OfflinePlayer, player2: OfflinePlayer): Boolean {
        return this.getOutgoingRequestUUIDs(player1).contains(player2.uniqueId) &&
               this.getOutgoingRequestUUIDs(player2).contains(player1.uniqueId)
    }

    fun getOutgoingRequestUUIDs(player: OfflinePlayer): List<UUID> {
        return outgoingRequests[player.uniqueId] ?: emptyList()
    }

    fun getReceivingRequestUUIDs(player: OfflinePlayer): List<UUID> {
        return receivingRequests[player.uniqueId] ?: emptyList()
    }

    fun removeRequestData(sender: OfflinePlayer, receiver: OfflinePlayer) {
        val senderUuid = sender.uniqueId
        val receiverUuid = receiver.uniqueId

        outgoingRequests[senderUuid]?.remove(receiverUuid)
        receivingRequests[receiverUuid]?.remove(senderUuid)
        expiryManager.removeExpiryTask(this.getId(sender, receiver))
    }

    private fun getId(sender: OfflinePlayer, receiver: OfflinePlayer): String {
        return "${sender.uniqueId}-${receiver.uniqueId}"
    }
}