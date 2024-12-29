package foundation.esoteric.fireworkwarscore.commands.operator

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.BooleanArgument
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.util.sendMessage
import org.bukkit.entity.Player
import java.util.function.Predicate

class SetRankCommand(plugin: FireworkWarsCorePlugin) : CommandAPICommand("set-ranked") {
    private val playerDataManager = plugin.playerDataManager

    private val targetPlayerArgumentNodeName = "targetPlayer"
    private val rankedArgumentNodeName = "ranked"

    init {
        this.requirements = Predicate { it.isOp }

        withArguments(PlayerArgument(targetPlayerArgumentNodeName), BooleanArgument(rankedArgumentNodeName))
        executesPlayer(this::onPlayerExecution)
        register(plugin)
    }

    private fun onPlayerExecution(player: Player, arguments: CommandArguments) {
        val targetPlayer = arguments[targetPlayerArgumentNodeName] as Player
        val ranked = arguments[rankedArgumentNodeName] as Boolean

        val targetProfile = playerDataManager.getPlayerProfile(targetPlayer, true)!!
        targetProfile.ranked = ranked

        if (ranked) {
            player.sendMessage(Message.GRANTED_RANK_SUCCESSFULLY, targetPlayer.name())
            targetPlayer.sendMessage(Message.RANK_GRANTED)
        } else {
            player.sendMessage(Message.REVOKED_RANK_SUCCESSFULLY, targetPlayer.name())
            targetPlayer.sendMessage(Message.RANK_REVOKED)
        }
    }
}