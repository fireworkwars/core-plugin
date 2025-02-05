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
import xyz.fireworkwars.core.managers.FriendManager
import xyz.fireworkwars.core.profiles.PlayerDataManager
import xyz.fireworkwars.core.util.Util
import xyz.fireworkwars.core.util.getMessage
import xyz.fireworkwars.core.util.sendMessage
import java.util.*
import kotlin.math.ceil

class FriendCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("friend") {
    private val playerDataManager: PlayerDataManager = plugin.playerDataManager
    private val friendManager: FriendManager = plugin.friendManager

    private val playerArgumentNodeName: String = "targetPlayer"
    private val pageArgumentNodeName: String = "page"

    private val playersPerPage = 8

    init {
        setRequirements { it is Player }
        withPermission(CommandPermission.NONE)

        withShortDescription("All friend-related commands")
        withFullDescription("Add, remove, view and manage your friends.")
        withAliases("f")

        withSubcommand(
            CommandAPICommand("add")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Adds a player as a friend")
                .withFullDescription("Sends a friend request to the specified player, or accepts their friend request if one exists.")
                .withArguments(this.nonFriendedOrBlockedArgumentSupplier())
                .executesPlayer(this::addOrAcceptFriendExecution)
        )

        withSubcommand(
            CommandAPICommand("cancel")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Cancels a friend request")
                .withFullDescription("Cancels a friend request sent to the specified player.")
                .withArguments(this.outgoingRequestsArgumentSupplier())
                .executesPlayer(this::cancelFriendRequest)
        )

        withSubcommand(
            CommandAPICommand("accept")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Accepts a friend request")
                .withFullDescription("Accepts a friend request from the specified player.")
                .withArguments(this.receivingRequestsArgumentSupplier())
                .executesPlayer(this::acceptFriend)
        )

        withSubcommand(
            CommandAPICommand("deny")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Denies a friend request")
                .withFullDescription("Denies a friend request from the specified player.")
                .withArguments(this.receivingRequestsArgumentSupplier())
                .executesPlayer(this::denyFriend)
        )

        withSubcommand(
            CommandAPICommand("remove")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Removes a friend")
                .withFullDescription("Removes the specified player from your friend list.")
                .withArguments(this.friendsArgumentSupplier())
                .executesPlayer(this::removeFriendExecution)
        )

        withSubcommand(
            CommandAPICommand("list")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Lists your friends")
                .withFullDescription("Lists all of your friends.")
                .withArguments(this.pageArgumentSupplier())
                .executesPlayer(this::listFriends)
        )

        withSubcommand(
            CommandAPICommand("party")
                .withPermission(CommandPermission.NONE)
                .withShortDescription("Create a friend party")
                .withFullDescription("Creates a party and invites all online friends.")
                .executesPlayer(this::createParty)
        )

        register(plugin)
    }

    private fun outgoingRequestsArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player

            val outgoingRequestUUIDs = friendManager.getOutgoingRequestUUIDs(player)
            val playerNames =
                outgoingRequestUUIDs.mapNotNull { playerDataManager.getPlayerProfile(it, false)?.username }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun receivingRequestsArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player

            val receivingRequestUUIDs = friendManager.getReceivingRequestUUIDs(player)
            val playerNames =
                receivingRequestUUIDs.mapNotNull { playerDataManager.getPlayerProfile(it, false)?.username }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun nonFriendedOrBlockedArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val profile = playerDataManager.getPlayerProfile(player)

            val playerNames = plugin.server.onlinePlayers.asSequence()
                .map { it.uniqueId }
                .filter { it != player.uniqueId }
                .filter { it !in profile.friends }
                .filter { it !in profile.blocked }
                .map { plugin.server.getPlayer(it)!!.name }
                .toList()

            return@strings playerNames.toTypedArray()
        })
    }

    private fun friendsArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val profile = playerDataManager.getPlayerProfile(player)

            val friendUUIDs = profile.friends
            val playerNames = friendUUIDs.mapNotNull {
                playerDataManager.getPlayerProfile(it, false)?.username
            }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun pageArgumentSupplier(): IntegerArgument {
        return IntegerArgument(pageArgumentNodeName)
            .setOptional(true) as IntegerArgument
    }

    private fun addOrAcceptFriendExecution(player: Player, args: CommandArguments, acceptOnly: Boolean = false) {
        val target = args[playerArgumentNodeName] as OfflinePlayer

        this.addOrAcceptFriend(player, target, acceptOnly)
    }

    fun addOrAcceptFriend(player: Player, target: OfflinePlayer, acceptOnly: Boolean = false) {
        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        val uuid = player.uniqueId
        val targetUuid = target.uniqueId

        if (acceptOnly && !friendManager.getReceivingRequestUUIDs(player).contains(targetUuid)) {
            return player.sendMessage(Message.NO_FRIEND_REQUESTS_FROM, targetProfile.formattedName())
        }

        if (profile.friends.contains(targetUuid)) {
            return player.sendMessage(Message.YOU_ARE_ALREADY_FRIENDS, targetProfile.formattedName())
        }

        if (profile.blocked.contains(targetUuid)) {
            return player.sendMessage(Message.YOU_BLOCKED_THAT_PLAYER)
        }

        if (targetProfile.blocked.contains(uuid)) {
            return player.sendMessage(Message.YOU_HAVE_BEEN_BLOCKED)
        }

        friendManager.addFriendRequest(player, target) { sender, receiver ->
            sender.sendMessage(Message.FRIEND_REQUEST_EXPIRED, targetProfile.formattedName())
            receiver.sendMessage(Message.FRIEND_REQUEST_FROM_EXPIRED, profile.formattedName())
        }

        if (friendManager.hasMutualRequests(player, target)) {
            profile.friends.add(targetUuid)
            targetProfile.friends.add(uuid)

            player.sendMessage(Message.YOU_ARE_NOW_FRIENDS, targetProfile.formattedName())
            target.sendMessage(Message.YOU_ARE_NOW_FRIENDS, profile.formattedName())

            friendManager.removeRequestData(sender = player, receiver = target)
            friendManager.removeRequestData(sender = target, receiver = player)

            plugin.lobbyPluginData.updateScoreboards()
        } else {
            player.sendMessage(Message.FRIEND_REQUEST_SENT, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_FROM, profile.formattedName(), profile.username)
        }
    }

    private fun cancelFriendRequest(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as OfflinePlayer

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (friendManager.getOutgoingRequestUUIDs(player).contains(target.uniqueId)) {
            friendManager.removeRequestData(sender = player, receiver = target)

            player.sendMessage(Message.FRIEND_REQUEST_CANCELLED, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_CANCELLED_BY, profile.formattedName())
        } else {
            player.sendMessage(Message.NO_FRIEND_REQUESTS_TO, targetProfile.formattedName())
        }
    }

    private fun acceptFriend(player: Player, args: CommandArguments) {
        return this.addOrAcceptFriendExecution(player, args, true)
    }

    private fun denyFriend(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as OfflinePlayer

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (friendManager.getReceivingRequestUUIDs(player).contains(target.uniqueId)) {
            friendManager.removeRequestData(sender = target, receiver = player)

            player.sendMessage(Message.FRIEND_REQUEST_DENIED, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_DENIED_BY, profile.formattedName())
        } else {
            player.sendMessage(Message.NO_FRIEND_REQUESTS_FROM, targetProfile.formattedName())
        }
    }

    private fun removeFriendExecution(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as OfflinePlayer

        return this.removeFriend(player, target)
    }

    fun removeFriend(player: Player, target: OfflinePlayer) {
        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (!profile.friends.contains(target.uniqueId)) {
            return player.sendMessage(Message.PLAYER_NOT_FRIENDED, targetProfile.formattedName())
        }

        profile.friends.remove(target.uniqueId)
        targetProfile.friends.remove(player.uniqueId)

        player.sendMessage(Message.REMOVED_FRIEND, targetProfile.formattedName())
        target.sendMessage(Message.YOU_WERE_REMOVED_AS_FRIEND, profile.formattedName())

        plugin.lobbyPluginData.updateScoreboards()
    }

    fun listFriends(player: Player, args: CommandArguments) {
        val pageArgument = args.getOrDefault(pageArgumentNodeName, 1) as Int

        val profile = playerDataManager.getPlayerProfile(player)
        val friends = profile.friends

        if (friends.isEmpty()) {
            return player.sendMessage(Message.YOU_HAVE_NO_FRIENDS)
        }

        val totalPages = ceil(friends.size.toDouble() / playersPerPage).toInt()
        val page = pageArgument.coerceIn(1, totalPages)
        val friendsOnPage = Util.getPageItems(friends, page, playersPerPage)

        var previous = text("<<", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
        var next = text(">>", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)

        if (page > 1) {
            previous = previous.clickEvent(ClickEvent.runCommand("/friend list ${page - 1}"))
        }

        if (page < totalPages) {
            next = next.clickEvent(ClickEvent.runCommand("/friend list ${page + 1}"))
        }

        val separator = player.getMessage(Message.FRIEND_LIST_SEPARATOR)
        val title = player.getMessage(Message.FRIEND_LIST_TITLE, page, totalPages, previous, next)

        val message = text()
            .append(separator).appendNewline()
            .append(title).appendNewline()
            .appendNewline()

        friendsOnPage.forEach { uuid ->
            val friendProfile = playerDataManager.getPlayerProfile(uuid)

            message
                .append(friendProfile.formattedName())
                .appendSpace()
                .append(player.getMessage(this.getStatusMessage(uuid)))
                .appendNewline()
        }

        message.append(separator)
        player.sendMessage(message)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createParty(player: Player, args: CommandArguments) {
        player.sendMessage(Message.COMING_SOON)
    }

    private fun getStatusMessage(uuid: UUID): Message {
        val offlinePlayer = plugin.server.getOfflinePlayer(uuid)
        val player = offlinePlayer.player

        if (!offlinePlayer.isOnline) {
            return Message.STATUS_OFFLINE
        }

        if (plugin.lobbyPluginData.isLobby(player!!.world)) {
            return Message.STATUS_IN_LOBBY
        }

        return Message.STATUS_PLAYING
    }
}