package foundation.esoteric.fireworkwarscore.managers

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*

class FriendManager(private val plugin: FireworkWarsCorePlugin) {
    private val requestExpiryTime = 6000L

    private val outgoingRequests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    private val receivingRequests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()

    private val expiryTasks: MutableMap<UUID, MutableMap<UUID, BukkitTask>> = mutableMapOf()

    fun addFriendRequest(sender: Player, receiver: Player, onExpire: (Player, Player) -> Unit) {
        val senderUuid = sender.uniqueId
        val receiverUuid = receiver.uniqueId

        outgoingRequests.computeIfAbsent(senderUuid) { mutableListOf() }.add(receiverUuid)
        receivingRequests.computeIfAbsent(receiverUuid) { mutableListOf() }.add(senderUuid)

        val task = plugin.runTaskLater({
            removeRequestData(sender, receiver)
            onExpire(sender, receiver)
        }, requestExpiryTime)

        expiryTasks.computeIfAbsent(senderUuid) { mutableMapOf() } [receiverUuid] = task
    }

    fun hasMutualRequests(player1: Player, player2: Player): Boolean {
        return getOutgoingRequests(player1).contains(player2.uniqueId) &&
               getOutgoingRequests(player2).contains(player1.uniqueId)
    }

    fun getOutgoingRequests(player: Player): List<UUID> {
        return outgoingRequests[player.uniqueId] ?: emptyList()
    }

    fun getReceivingRequests(player: Player): List<UUID> {
        return receivingRequests[player.uniqueId] ?: emptyList()
    }

    fun removeRequestData(sender: Player, receiver: Player) {
        val senderUuid = sender.uniqueId
        val receiverUuid = receiver.uniqueId

        outgoingRequests[senderUuid]?.remove(receiverUuid)
        receivingRequests[receiverUuid]?.remove(senderUuid)
        expiryTasks[senderUuid]?.remove(receiverUuid)?.cancel()
    }
}