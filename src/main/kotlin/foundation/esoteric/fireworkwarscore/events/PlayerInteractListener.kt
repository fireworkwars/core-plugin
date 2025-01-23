package foundation.esoteric.fireworkwarscore.events

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.interfaces.Event
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class PlayerInteractListener(private val plugin: FireworkWarsCorePlugin) : Event {
    override fun register() {
        plugin.registerEvent(this)
    }

    @EventHandler
    fun onPlayerRightClick(event: PlayerInteractAtEntityEvent) {
        val player = event.player

        if (event.rightClicked !is Player) {
            return
        }

        if (!player.isSneaking) {
            return
        }

        val target = event.rightClicked as Player

        plugin.profileCommand.openProfileMenu(player, target)
    }
}