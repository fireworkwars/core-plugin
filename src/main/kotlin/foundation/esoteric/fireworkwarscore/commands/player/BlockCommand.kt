package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import foundation.esoteric.fireworkwarscore.util.sendMessage
import org.bukkit.entity.Player

class BlockCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("block") {
    private val playerDataManager: PlayerDataManager = plugin.playerDataManager

    private val playerArgumentNodeName: String = "targetPlayer"

    init {
        setRequirements { it is Player }
        withPermission(CommandPermission.NONE)

        withShortDescription("All block-related commands")
        withFullDescription("Add or remove players from your block list.")

        withSubcommand(
            CommandAPICommand("add")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Blocks a player")
                .withFullDescription("Blocks the specified player.")
                .withArguments(this.playerArgumentSupplier())
                .executesPlayer(this::blockPlayer)
        )

        withSubcommand(
            CommandAPICommand("remove")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Unblocks a player")
                .withFullDescription("Unblocks the specified player.")
                .withArguments(this.blockedPlayersArgumentSupplier())
                .executesPlayer(this::unblockPlayer)
        )

        register(plugin)
    }

    private fun playerArgumentSupplier(): Argument<Player> {
        return PlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val playerNames = plugin.server.onlinePlayers
                .map { it.name }
                .filter { it != info.sender.name }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun blockedPlayersArgumentSupplier(): Argument<Player> {
        return PlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val profile = playerDataManager.getPlayerProfile(player)
            val blockedPlayers = profile.blocked
            val playerNames = blockedPlayers.mapNotNull { plugin.server.getOfflinePlayer(it).name }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun blockPlayer(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as Player?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_BLOCK_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (profile.blocked.contains(target.uniqueId)) {
            return player.sendMessage(Message.PLAYER_ALREADY_BLOCKED)
        }

        profile.blocked.add(target.uniqueId)

        player.sendMessage(Message.BLOCKED_PLAYER, targetProfile.formattedName())
    }

    private fun unblockPlayer(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as Player?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (!profile.blocked.contains(target.uniqueId)) {
            return player.sendMessage(Message.PLAYER_NOT_BLOCKED)
        }

        profile.blocked.remove(target.uniqueId)

        player.sendMessage(Message.UNBLOCKED_PLAYER, targetProfile.formattedName())
    }
}