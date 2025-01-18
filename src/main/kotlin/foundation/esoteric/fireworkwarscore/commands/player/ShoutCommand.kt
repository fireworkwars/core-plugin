package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.profiles.Rank
import foundation.esoteric.fireworkwarscore.util.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.*

class ShoutCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("shout") {
    private val playerDataManager = plugin.playerDataManager
    private val languageManager = plugin.languageManager

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

        val messageArg = args[messageArgumentNodeName] as String

        plugin.server.onlinePlayers.forEach {
            val prefix = languageManager.getMessage(Message.SHOUT, it)
            val rank = profile.rank.toFormattedText()
            val message = Component.text(": $messageArg", NamedTextColor.WHITE)

            it.sendMessage(prefix.appendSpace().append(rank).append(message))
        }
    }
}