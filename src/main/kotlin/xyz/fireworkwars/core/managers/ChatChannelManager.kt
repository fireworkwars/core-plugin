package xyz.fireworkwars.core.managers

import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.interfaces.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class ChatChannelManager(private val plugin: FireworkWarsCorePlugin) : Event {
    private val playerChannels = mutableMapOf<UUID, ChatChannel>()

    override fun register() {
        plugin.registerEvent(this)
    }

    fun getChannel(player: UUID): ChatChannel {
        return playerChannels[player] ?: ChatChannel.ALL
    }

    fun setChannel(player: UUID, channel: ChatChannel) {
        playerChannels[player] = channel
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        playerChannels[event.player.uniqueId] = ChatChannel.ALL
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerChannels.remove(event.player.uniqueId)
    }

    enum class ChatChannel {
        ALL,
        FRIEND,
        PARTY
    }
}