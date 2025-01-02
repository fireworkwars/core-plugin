package foundation.esoteric.fireworkwarscore.commands.operator

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.profiles.Rank
import foundation.esoteric.fireworkwarscore.util.sendMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.Predicate

class SetRankCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("set-rank") {
    private val playerDataManager = plugin.playerDataManager

    private val targetPlayerArgumentNodeName = "targetPlayer"
    private val ranksArgumentNodeName = "ranks"

    init {
        this.requirements = Predicate { it.isOp }

        withArguments(PlayerArgument(targetPlayerArgumentNodeName), getRanksArgument())
        executesPlayer(this::onPlayerExecution)
        register(plugin)
    }
    
    private fun getRanksArgument(): Argument<String> {
        val rankNames = Rank.entries.map { it.toString() }
        val suggestions = ArgumentSuggestions.strings<CommandSender>(*rankNames.toTypedArray())

        return StringArgument(ranksArgumentNodeName)
            .includeSuggestions(suggestions)
            .setOptional(false)
    }

    private fun onPlayerExecution(player: Player, arguments: CommandArguments) {
        val targetPlayer = arguments[targetPlayerArgumentNodeName] as Player
        val rankArgument = arguments[ranksArgumentNodeName] as String

        if (Rank.entries.toTypedArray().none { it.toString() == rankArgument }) {
            player.sendMessage(Message.INVALID_RANK)
            return
        }

        val rank = Rank.valueOf(rankArgument.uppercase())

        val targetProfile = playerDataManager.getPlayerProfile(targetPlayer, true)!!
        targetProfile.rank = rank

        if (rank == Rank.NONE) {
            player.sendMessage(Message.REVOKED_RANK_SUCCESSFULLY, targetPlayer.name())
            targetPlayer.sendMessage(Message.RANK_REVOKED)
        } else {
            player.sendMessage(Message.GRANTED_RANK_SUCCESSFULLY, targetPlayer.name(), rank.toFormattedText())
            targetPlayer.sendMessage(Message.RANK_GRANTED, rank.toFormattedText())
        }

        rank.updateTablist(player)
        plugin.lobbyPluginData.updateScoreboards()
    }
}