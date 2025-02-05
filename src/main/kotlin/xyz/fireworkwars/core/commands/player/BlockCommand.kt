package xyz.fireworkwars.core.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandArguments
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.language.Message
import xyz.fireworkwars.core.profiles.PlayerDataManager
import xyz.fireworkwars.core.util.Util
import xyz.fireworkwars.core.util.getMessage
import xyz.fireworkwars.core.util.sendMessage
import kotlin.math.ceil

class BlockCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("block") {
    private val playerDataManager: PlayerDataManager = plugin.playerDataManager

    private val playerArgumentNodeName: String = "targetPlayer"
    private val pageArgumentNodeName: String = "page"

    private val playersPerPage = 5

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
                .executesPlayer(this::blockPlayerExecution)
        )

        this.withSubcommand(
            CommandAPICommand("remove")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Unblocks a player")
                .withFullDescription("Unblocks the specified player.")
                .withArguments(this.blockedPlayersArgumentSupplier())
                .executesPlayer(this::unblockPlayerExecution)
        )

        this.withSubcommand(
            CommandAPICommand("list")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("List blocked players")
                .withFullDescription("List all players you have blocked.")
                .withArguments(this.pageArgumentSupplier())
                .executesPlayer(this::listBlockedPlayers)
        )

        this.register(plugin)
    }

    private fun nonBlockedArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
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

    private fun blockedPlayersArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val profile = playerDataManager.getPlayerProfile(player)
            val blockedPlayers = profile.blocked
            val playerNames = blockedPlayers.mapNotNull { plugin.server.getOfflinePlayer(it).name }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun pageArgumentSupplier(): IntegerArgument {
        return IntegerArgument(pageArgumentNodeName)
            .setOptional(true) as IntegerArgument
    }

    private fun blockPlayerExecution(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as OfflinePlayer?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        this.blockPlayer(player, target)
    }

    fun blockPlayer(player: Player, target: OfflinePlayer) {
        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_BLOCK_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

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

    private fun unblockPlayerExecution(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as OfflinePlayer?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        this.unblockPlayer(player, target)
    }

    fun unblockPlayer(player: Player, target: OfflinePlayer) {
        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (!profile.blocked.contains(target.uniqueId)) {
            return player.sendMessage(Message.PLAYER_NOT_BLOCKED)
        }

        profile.blocked.remove(target.uniqueId)

        player.sendMessage(Message.UNBLOCKED_PLAYER, targetProfile.formattedName())
    }

    private fun listBlockedPlayers(player: Player, args: CommandArguments) {
        val pageArgument = args.getOrDefault(pageArgumentNodeName, 1) as Int

        val profile = playerDataManager.getPlayerProfile(player)
        val blocked = profile.blocked

        if (blocked.isEmpty()) {
            return player.sendMessage(Message.YOU_HAVE_NO_BLOCKED_PLAYERS)
        }

        val totalPages = ceil(blocked.size.toDouble() / playersPerPage).toInt()
        val page = pageArgument.coerceIn(1, totalPages)
        val blockedOnPage = Util.getPageItems(blocked, page, playersPerPage)

        var previous = text("<<", NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
        var next = text(">>", NamedTextColor.AQUA).decorate(TextDecoration.BOLD)

        if (page > 1) {
            previous = previous.clickEvent(ClickEvent.runCommand("/block list ${page - 1}"))
        }

        if (page < totalPages) {
            next = next.clickEvent(ClickEvent.runCommand("/block list ${page + 1}"))
        }

        val separator = player.getMessage(Message.BLOCK_LIST_SEPARATOR)
        val title = player.getMessage(Message.BLOCK_LIST_TITLE, page, totalPages, previous, next)

        val message = text()
            .append(separator).appendNewline()
            .append(title).appendNewline()
            .appendNewline()

        blockedOnPage.forEach { uuid ->
            val blockedProfile = playerDataManager.getPlayerProfile(uuid)

            message
                .append(text("${blockedOnPage.indexOf(uuid) + 1}. ", NamedTextColor.GRAY))
                .append(text(blockedProfile.username, NamedTextColor.YELLOW))
                .appendNewline()
        }

        message.append(separator)
        player.sendMessage(message)
    }
}