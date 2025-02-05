package xyz.fireworkwars.core.managers

import org.bukkit.OfflinePlayer
import org.bukkit.scheduler.BukkitTask
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import java.util.*

class FriendManager(private val plugin: FireworkWarsCorePlugin) {
    private val requestExpiryTime = 6000L

    private val outgoingRequests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    private val receivingRequests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()

    private val expiryTasks: MutableMap<UUID, MutableMap<UUID, BukkitTask>> = mutableMapOf()

    fun addFriendRequest(
        sender: OfflinePlayer,
        receiver: OfflinePlayer,
        onExpire: (OfflinePlayer, OfflinePlayer) -> Unit
    ) {
        val senderUuid = sender.uniqueId
        val receiverUuid = receiver.uniqueId

        outgoingRequests.computeIfAbsent(senderUuid) { mutableListOf() }.add(receiverUuid)
        receivingRequests.computeIfAbsent(receiverUuid) { mutableListOf() }.add(senderUuid)

        val task = plugin.runTaskLater(requestExpiryTime) {
            removeRequestData(sender, receiver)
            onExpire(sender, receiver)
        }

        expiryTasks.computeIfAbsent(senderUuid) { mutableMapOf() }[receiverUuid] = task
    }

    fun hasMutualRequests(player1: OfflinePlayer, player2: OfflinePlayer): Boolean {
        return getOutgoingRequestUUIDs(player1).contains(player2.uniqueId) &&
                getOutgoingRequestUUIDs(player2).contains(player1.uniqueId)
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
        expiryTasks[senderUuid]?.remove(receiverUuid)?.cancel()
    }
}