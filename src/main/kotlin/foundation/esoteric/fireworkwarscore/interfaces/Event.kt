package foundation.esoteric.fireworkwarscore.interfaces

import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

@Suppress("unused")
interface Event : Listener {
    fun register()

    fun unregister() {
        HandlerList.unregisterAll(this)
    }
}