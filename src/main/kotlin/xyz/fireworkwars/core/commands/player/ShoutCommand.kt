package xyz.fireworkwars.core.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.profiles.Rank
import xyz.fireworkwars.core.util.CooldownManager
import xyz.fireworkwars.core.util.sendMessage

class ShoutCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("shout") {
    private val playerDataManager = plugin.playerDataManager

    private val cooldownTicks = 30 * 20
    private val cooldownManager = CooldownManager(plugin, cooldownTicks)

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
        val uuid = player.uniqueId
        val profile = playerDataManager.getPlayerProfile(uuid)

        if (profile.rank != Rank.GOLD) {
            return player.sendMessage(Message.REQUIRES_GOLD_RANK, Rank.GOLD.toFormattedText())
        }

        if (cooldownManager.isOnCooldown(uuid)) {
            val remainingSeconds = cooldownManager.remainingCooldownTicks(uuid) / 20
            return player.sendMessage(Message.SHOUT_COOLDOWN, remainingSeconds)
        } else {
            cooldownManager.setCooldown(uuid)
        }

        val message = args[messageArgumentNodeName] as String

        plugin.server.onlinePlayers.forEach {
            it.sendMessage(Message.SHOUT, profile.formattedName(), message)
        }
    }
}