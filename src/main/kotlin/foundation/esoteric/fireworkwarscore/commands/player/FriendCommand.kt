package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.managers.FriendManager
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import foundation.esoteric.fireworkwarscore.util.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.max

class FriendCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("friend") {
    private val playerDataManager: PlayerDataManager = plugin.playerDataManager
    private val languageManager: LanguageManager = plugin.languageManager
    private val friendManager: FriendManager = FriendManager(plugin)

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
                .withArguments(this.playerArgumentSupplier())
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
                .withArguments(this.playerArgumentSupplier())
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

    private fun outgoingRequestsArgumentSupplier(): Argument<Player> {
        return PlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val outgoingRequests = friendManager.getOutgoingRequests(player)
            val playerNames = outgoingRequests.mapNotNull { plugin.server.getPlayer(it)?.name }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun receivingRequestsArgumentSupplier(): Argument<Player> {
        return PlayerArgument(playerArgumentNodeName).replaceSuggestions(ArgumentSuggestions.strings { info ->
            val player = info.sender as Player
            val receivingRequests = friendManager.getReceivingRequests(player)
            val playerNames = receivingRequests.mapNotNull { plugin.server.getPlayer(it)?.name }

            return@strings playerNames.toTypedArray()
        })
    }

    private fun pageArgumentSupplier(): IntegerArgument {
        return IntegerArgument(friendListPageArgumentNodeName)
            .setOptional(true) as IntegerArgument
    }

    private fun acceptFriend(player: Player, args: CommandArguments) {
        return addOrAcceptFriend(player, args, true)
    }

    private fun denyFriend(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as Player?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (friendManager.getReceivingRequests(player).contains(target.uniqueId)) {
            friendManager.removeRequestData(sender = target, receiver = player)

            player.sendMessage(Message.FRIEND_REQUEST_DENIED, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_DENIED_BY, profile.formattedName())
        } else {
            player.sendMessage(Message.NO_FRIEND_REQUESTS_FROM, targetProfile.formattedName())
        }
    }

    private fun addOrAcceptFriend(player: Player, args: CommandArguments, acceptOnly: Boolean = false) {
        val target = args[playerArgumentNodeName] as Player?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        val uuid = player.uniqueId
        val targetUuid = target.uniqueId

        if (acceptOnly && !friendManager.getReceivingRequests(player).contains(targetUuid)) {
            return player.sendMessage(Message.NO_FRIEND_REQUESTS_FROM, targetProfile.formattedName())
        }

        if (profile.friends.contains(targetUuid)) {
            return player.sendMessage(Message.YOU_ARE_ALREADY_FRIENDS, targetProfile.formattedName())
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
        } else {
            player.sendMessage(Message.FRIEND_REQUEST_SENT, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_FROM, profile.formattedName())
        }
    }

    private fun cancelFriendRequest(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as Player?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (friendManager.getOutgoingRequests(player).contains(target.uniqueId)) {
            friendManager.removeRequestData(sender = player, receiver = target)

            player.sendMessage(Message.FRIEND_REQUEST_CANCELLED, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_CANCELLED_BY, profile.formattedName())
        } else {
            player.sendMessage(Message.NO_FRIEND_REQUESTS_TO, targetProfile.formattedName())
        }
    }

    private fun removeFriend(player: Player, args: CommandArguments) {
        val target = args[playerArgumentNodeName] as Player?
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        if (target.uniqueId == player.uniqueId) {
            return player.sendMessage(Message.CANNOT_FRIEND_SELF)
        }

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (!profile.friends.contains(target.uniqueId)) {
            return player.sendMessage(Message.PLAYER_NOT_FRIENDED, targetProfile.formattedName())
        }

        profile.friends.remove(target.uniqueId)
        targetProfile.friends.remove(player.uniqueId)

        player.sendMessage(Message.REMOVED_FRIEND, targetProfile.formattedName())
        target.sendMessage(Message.YOU_WERE_REMOVED_AS_FRIEND, profile.formattedName())
    }

    private fun listFriends(player: Player, args: CommandArguments) {
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
            .append(getMessage(Message.FRIEND_LIST_SEPARATOR, player)).appendNewline()
            .append(getMessage(Message.FRIEND_LIST_TITLE, player)).appendNewline()

        friendsOnPage.forEach {
            val friendProfile = playerDataManager.getPlayerProfile(it)
            message.append(friendProfile.formattedName()).appendNewline()
        }

        val prevComponent = if (page > 1) {
            plugin.mm.deserialize(" <yellow><b><click:run_command:/friend list ${page - 1}><<")
        } else {
            plugin.mm.deserialize("<aqua>-=")
        }

        val nextComponent = if (page < totalPages) {
            plugin.mm.deserialize(" <yellow><b><click:run_command:/friend list ${page + 1}>>")
        } else {
            plugin.mm.deserialize("<aqua>=-")
        }

        message.append(getMessage(Message.FRIEND_LIST_PAGING, player, prevComponent, page, totalPages, nextComponent))
            .appendNewline()
        message.append(getMessage(Message.FRIEND_LIST_SEPARATOR, player))

        player.sendMessage(message)
    }

    private fun getMessage(message: Message, player: Player, vararg args: Any): Component {
        return languageManager.getMessage(message, player, *args)
    }
}