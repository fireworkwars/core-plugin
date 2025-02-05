package xyz.fireworkwars.core.commands.developer

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.function.Predicate

class ToggleBuildCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("toggle-build") {
    init {
        this.requirements = Predicate { it.isOp }

        this.withShortDescription("Toggle building mode")
        this.withFullDescription("Toggle building mode")

        this.executesPlayer(this::executesPlayer)
        this.register(plugin)
    }

    private fun executesPlayer(player: Player, args: CommandArguments) {
        plugin.isBuildModeEnabled = !plugin.isBuildModeEnabled

        val status =
            if (plugin.isBuildModeEnabled) text("ENABLED").color(NamedTextColor.DARK_GREEN)
            else text("DISABLED").color(NamedTextColor.RED)

        player.sendMessage(
            text("Building Mode is now: ").color(NamedTextColor.GREEN).append(status))
    }
}