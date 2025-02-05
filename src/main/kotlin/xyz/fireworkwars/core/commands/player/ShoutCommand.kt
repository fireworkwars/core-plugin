package xyz.fireworkwars.core.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.profiles.Rank
import xyz.fireworkwars.core.util.sendMessage
import org.bukkit.entity.Player
import java.util.*

class ShoutCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("shout") {
    private val playerDataManager = plugin.playerDataManager

    private val cooldowns = mutableMapOf<UUID, Int>()
    private val cooldownSeconds = 30

    private val messageArgumentNodeName = "message"

    init {
        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("Shout a message to all players")
        this.withFullDescription("Shout a message to all players.")
        this.withAliases("s")

        this.withArguments(GreedyStringArgument(messageArgumentNodeName))
        this.executesPlayer(this::onPlayerExecute)

        this.register(plugin)
    }

    private fun onPlayerExecute(player: Player, args: CommandArguments) {
        val profile = playerDataManager.getPlayerProfile(player.uniqueId)

        if (profile.rank != Rank.GOLD) {
            return player.sendMessage(Message.REQUIRES_GOLD_RANK, Rank.GOLD.toFormattedText())
        }

        val lastShout = cooldowns[player.uniqueId] ?: Int.MIN_VALUE
        val currentTick = plugin.server.currentTick

        if (lastShout + (cooldownSeconds * 20) > currentTick) {
            return player.sendMessage(Message.SHOUT_COOLDOWN, cooldownSeconds)
        } else {
            cooldowns[player.uniqueId] = currentTick
        }

        val message = args[messageArgumentNodeName] as String

        plugin.server.onlinePlayers.forEach {
            it.sendMessage(Message.SHOUT, profile.formattedName(), message)
        }
    }
}