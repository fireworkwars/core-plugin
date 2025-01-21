package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.managers.FriendManager
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import foundation.esoteric.fireworkwarscore.util.getMessage
import foundation.esoteric.fireworkwarscore.util.sendMessage
import net.kyori.adventure.text.Component.text
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class FriendCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("friend") {
    private val playerDataManager: PlayerDataManager = plugin.playerDataManager
    private val friendManager: FriendManager = plugin.friendManager

    private val playerArgumentNodeName: String = "targetPlayer"
    private val friendListPageArgumentNodeName: String = "page"

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
                .executesPlayer(this::addOrAcceptFriend)
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
                .executesPlayer(this::removeFriend)
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
            val playerNames = outgoingRequestUUIDs.mapNotNull { playerDataManager.getPlayerProfile(it, false)?.username }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun receivingRequestsArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player

            val receivingRequestUUIDs = friendManager.getReceivingRequestUUIDs(player)
            val playerNames = receivingRequestUUIDs.mapNotNull { playerDataManager.getPlayerProfile(it, false)?.username }

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
        return IntegerArgument(friendListPageArgumentNodeName)
            .setOptional(true) as IntegerArgument
    }

    private fun addOrAcceptFriend(player: Player, args: CommandArguments, acceptOnly: Boolean = false) {
        val target = args[playerArgumentNodeName] as OfflinePlayer

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
        return addOrAcceptFriend(player, args, true)
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

    private fun removeFriend(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as OfflinePlayer

        return removeFriend(player, target)
    }

    fun listFriends(player: Player, args: CommandArguments) {
        val pageArgument = args.getOrDefault(friendListPageArgumentNodeName, 1) as Int

        val profile = playerDataManager.getPlayerProfile(player)
        val friends = profile.friends

        if (friends.isEmpty()) {
            return player.sendMessage(Message.YOU_HAVE_NO_FRIENDS)
        }

        val friendsPerPage = 5
        val totalPages = max(1, ceil(friends.size.toDouble() / friendsPerPage).toInt())
        val page = Math.clamp(pageArgument.toLong(), 1, totalPages)

        val startIndex = (page - 1) * friendsPerPage
        val endIndex = (startIndex + friendsPerPage).coerceAtMost(friends.size)
        val friendsOnPage = friends.subList(startIndex, endIndex)

        val message = text()
            .append(player.getMessage(Message.FRIEND_LIST_SEPARATOR)).appendNewline()
            .append(player.getMessage(Message.FRIEND_LIST_TITLE)).appendNewline()

        friendsOnPage.forEach { uuid ->
            val friendProfile = playerDataManager.getPlayerProfile(uuid)

            message
                .append(friendProfile.formattedName())
                .appendSpace()
                .append(player.getMessage(this.getStatusMessage(uuid)))
                .appendNewline()
        }

        val prevText = if (page > 1) {
            plugin.mm.deserialize(" <yellow><b><click:run_command:/friend list ${page - 1}><<")
        } else {
            plugin.mm.deserialize("<aqua>-=")
        }

        val nextText = if (page < totalPages) {
            plugin.mm.deserialize(" <yellow><b><click:run_command:/friend list ${page + 1}>>")
        } else {
            plugin.mm.deserialize("<aqua>=-")
        }

        val pagingText = player.getMessage(
            Message.FRIEND_LIST_PAGING, prevText, page, totalPages, nextText)

        message.append(pagingText).appendNewline()
        message.append(player.getMessage(Message.FRIEND_LIST_SEPARATOR))

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