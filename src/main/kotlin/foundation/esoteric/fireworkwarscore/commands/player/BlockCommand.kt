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
        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("All block-related commands")
        this.withFullDescription("Add or remove players from your block list.")

        this.withSubcommand(
            CommandAPICommand("add")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Blocks a player")
                .withFullDescription("Blocks the specified player.")
                .withArguments(this.nonBlockedArgumentSupplier())
                .executesPlayer(this::blockPlayer)
        )

        this.withSubcommand(
            CommandAPICommand("remove")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Unblocks a player")
                .withFullDescription("Unblocks the specified player.")
                .withArguments(this.blockedPlayersArgumentSupplier())
                .executesPlayer(this::unblockPlayer)
        )

        this.register(plugin)
    }

    private fun nonBlockedArgumentSupplier(): Argument<Player> {
        return PlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val profile = playerDataManager.getPlayerProfile(player)

            val playerNames = plugin.server.onlinePlayers
                .asSequence()
                .map { it.uniqueId }
                .filter { it != player.uniqueId }
                .filter { !profile.blocked.contains(it) }
                .map { plugin.server.getPlayer(it)!!.name }
                .toList()

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

        if (profile.friends.contains(target.uniqueId)) {
            plugin.friendCommand.removeFriend(player, target)
        }

        profile.blocked.add(target.uniqueId)

        plugin.friendManager.removeRequestData(sender = player, receiver = target)
        plugin.friendManager.removeRequestData(sender = target, receiver = player)

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