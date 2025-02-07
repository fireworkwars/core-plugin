package xyz.fireworkwars.core.events

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.interfaces.Event

class PlayerInteractListener(private val plugin: FireworkWarsCorePlugin) : Event {
    override fun register() {
        plugin.registerEvent(this)
    }

    @EventHandler
    fun onPlayerRightClick(event: PlayerInteractAtEntityEvent) {
        val player = event.player

        if (!plugin.lobbyHook.isLobby(player.world)) {
            return
        }

        if (event.rightClicked !is Player) {
            return
        }

        if (event.hand != EquipmentSlot.HAND) {
            return
        }

        if (!player.isSneaking) {
            return
        }

        val target = event.rightClicked as Player

        plugin.profileCommand.openProfileMenu(player, target)
    }
}