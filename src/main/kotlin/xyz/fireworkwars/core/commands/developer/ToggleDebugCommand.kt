package xyz.fireworkwars.core.commands.developer

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import java.util.function.Predicate

class ToggleDebugCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("toggle-debug") {
    init {
        this.requirements = Predicate { it.isOp }

        this.withShortDescription("Toggle debugging mode")
        this.withFullDescription("Toggle debugging mode")

        this.executesPlayer(this::executesPlayer)
        this.register(plugin)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun executesPlayer(player: Player, args: CommandArguments) {
        plugin.isDebugging = !plugin.isDebugging

        val status =
            if (plugin.isDebugging) text("ENABLED").color(NamedTextColor.DARK_GREEN)
            else text("DISABLED").color(NamedTextColor.RED)

        player.sendMessage(
            text("Debugging is now: ").color(NamedTextColor.GREEN).append(status)
        )
    }
}