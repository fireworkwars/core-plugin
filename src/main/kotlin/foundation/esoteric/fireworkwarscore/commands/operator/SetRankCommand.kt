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

class SetRankCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("set-rank") {
    private val playerDataManager = plugin.playerDataManager

    private val targetPlayerArgumentNodeName = "targetPlayer"
    private val ranksArgumentNodeName = "rank"

    init {
        this.setRequirements { it is Player && it.isOp }

        this.withShortDescription("Set a player's rank")
        this.withFullDescription("Set a player's rank")
        this.withAliases("rank")

        this.withArguments(PlayerArgument(targetPlayerArgumentNodeName), this.getRanksArgument())
        this.executesPlayer(this::onPlayerExecution)
        this.register(plugin)
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

        val targetProfile = playerDataManager.getPlayerProfile(targetPlayer)
        targetProfile.rank = rank

        if (rank == Rank.NONE) {
            player.sendMessage(Message.REVOKED_RANK_SUCCESSFULLY, targetProfile.formattedName())
            targetPlayer.sendMessage(Message.RANK_REVOKED)
        } else {
            player.sendMessage(Message.GRANTED_RANK_SUCCESSFULLY, targetProfile.formattedName(), rank.toFormattedText())
            targetPlayer.sendMessage(Message.RANK_GRANTED, rank.toFormattedText())
        }

        if (!plugin.fireworkWarsPluginData.isInPlayingGame(targetPlayer)) {
            targetProfile.updateOwnTablist()
        }

        plugin.lobbyPluginData.updateScoreboards()
    }
}