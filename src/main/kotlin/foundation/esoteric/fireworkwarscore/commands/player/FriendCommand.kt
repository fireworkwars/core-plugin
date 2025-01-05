package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import foundation.esoteric.fireworkwarscore.util.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.ceil
import kotlin.math.max


class FriendCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("friend") {
    private val playerDataManager: PlayerDataManager = plugin.playerDataManager
    private val languageManager: LanguageManager = plugin.languageManager

    private val requests: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    private val expiryTasks: MutableMap<UUID, MutableMap<UUID, BukkitTask>> = mutableMapOf()

    private val playerArgumentNodeName: String = "targetPlayer"
    private val friendListPageArgumentNodeName: String = "page"

    init {
        setRequirements { it is Player }
        withPermission(CommandPermission.NONE)

        withAliases("f")

        withSubcommand(
            CommandAPICommand("add")
                .withPermission(CommandPermission.NONE)
                .withArguments(this.playerArgumentSupplier())
                .executesPlayer(this::addOrAcceptFriend)
                .withAliases("a"))

        withSubcommand(
            CommandAPICommand("cancel")
                .withPermission(CommandPermission.NONE)
                .withArguments(this.playerArgumentSupplier())
                .executesPlayer(this::cancelFriendRequest))

        withSubcommand(
            CommandAPICommand("accept")
                .withPermission(CommandPermission.NONE)
                .withArguments(this.playerArgumentSupplier())
                .executesPlayer(this::acceptFriend))

        withSubcommand(
            CommandAPICommand("deny")
                .withPermission(CommandPermission.NONE)
                .withArguments(this.playerArgumentSupplier())
                .executesPlayer(this::denyFriend))

        withSubcommand(
            CommandAPICommand("remove")
                .withPermission(CommandPermission.NONE)
                .withArguments(this.playerArgumentSupplier())
                .executesPlayer(this::removeFriend))

        withSubcommand(
            CommandAPICommand("list")
                .withPermission(CommandPermission.NONE)
                .withArguments(this.pageArgumentSupplier())
                .executesPlayer(this::listFriends)
                .withAliases("l"))

        withSubcommand(
            CommandAPICommand("help")
                .withPermission(CommandPermission.NONE)
                .executesPlayer(this::help))

        register(plugin)
    }

    private fun hasMutualRequests(player1: Player, player2: Player): Boolean {
        return requests.containsKey(player1.uniqueId) &&
               requests.containsKey(player2.uniqueId) &&
               requests[player1.uniqueId]!!.contains(player2.uniqueId) &&
               requests[player2.uniqueId]!!.contains(player1.uniqueId)
    }

    private fun playerArgumentSupplier(): StringArgument {
        return StringArgument(playerArgumentNodeName)
    }

    private fun pageArgumentSupplier(): IntegerArgument {
        return IntegerArgument(friendListPageArgumentNodeName)
    }

    private fun acceptFriend(player: Player, args: CommandArguments) {
        return addOrAcceptFriend(player, args, true)
    }

    private fun denyFriend(player: Player, args: CommandArguments) {
        val targetArgument = args.get(playerArgumentNodeName) as String? ?: ""
        val target = plugin.server.getPlayer(targetArgument)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (requests[target.uniqueId]?.contains(player.uniqueId) == true) {
            requests[target.uniqueId]?.remove(player.uniqueId)
            expiryTasks[target.uniqueId]?.remove(player.uniqueId)?.cancel()

            player.sendMessage(Message.FRIEND_REQUEST_DENIED, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_DENIED_BY, profile.formattedName())
        } else {
            player.sendMessage(Message.NO_FRIEND_REQUEST_FROM, targetProfile.formattedName())
        }
    }

    private fun addOrAcceptFriend(player: Player, args: CommandArguments, acceptOnly: Boolean = false) {
        val targetArgument = args.get(playerArgumentNodeName) as String? ?: ""
        val target = plugin.server.getPlayer(targetArgument)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        val uuid = player.uniqueId
        val targetUuid = target.uniqueId

        if (acceptOnly && requests[targetUuid]?.contains(uuid) != true) {
            return player.sendMessage(Message.NO_FRIEND_REQUEST_FROM, targetProfile.formattedName())
        }

        if (profile.friends.contains(targetUuid)) {
            return player.sendMessage(Message.YOU_ARE_ALREADY_FRIENDS, targetProfile.formattedName())
        }

        requests.computeIfAbsent(uuid) { mutableListOf() }.add(targetUuid)

        if (hasMutualRequests(player, target)) {
            profile.friends.add(targetUuid)
            targetProfile.friends.add(uuid)

            player.sendMessage(Message.YOU_ARE_NOW_FRIENDS, targetProfile.formattedName())
            target.sendMessage(Message.YOU_ARE_NOW_FRIENDS, profile.formattedName())

            requests[uuid]?.remove(targetUuid)
            requests[targetUuid]?.remove(uuid)

            expiryTasks[uuid]?.remove(targetUuid)?.cancel()
            expiryTasks[targetUuid]?.remove(uuid)?.cancel()
        } else {
            player.sendMessage(Message.FRIEND_REQUEST_SENT, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_FROM, profile.formattedName())

            val task = plugin.runTaskLater({
                requests[uuid]?.remove(targetUuid)
                expiryTasks[uuid]?.remove(targetUuid)

                player.sendMessage(Message.FRIEND_REQUEST_EXPIRED, targetProfile.formattedName())
                target.sendMessage(Message.FRIEND_REQUEST_FROM_EXPIRED, profile.formattedName())
            }, 6000L)

            expiryTasks.computeIfAbsent(uuid) { mutableMapOf() } [targetUuid] = task
        }
    }

    private fun cancelFriendRequest(player: Player, args: CommandArguments) {
        val targetArgument = args.get(playerArgumentNodeName) as String? ?: ""
        val target = plugin.server.getPlayer(targetArgument)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target)

        if (requests[target.uniqueId]?.contains(player.uniqueId) == true) {
            requests[target.uniqueId]?.remove(player.uniqueId)
            expiryTasks[target.uniqueId]?.remove(player.uniqueId)?.cancel()

            player.sendMessage(Message.FRIEND_REQUEST_CANCELLED, targetProfile.formattedName())
            target.sendMessage(Message.FRIEND_REQUEST_CANCELLED_BY, profile.formattedName())
        } else {
            player.sendMessage(Message.NO_FRIEND_REQUEST_TO, targetProfile.formattedName())
        }
    }

    private fun removeFriend(player: Player, args: CommandArguments) {
        val targetArgument = args.get(playerArgumentNodeName) as String? ?: ""
        val target = plugin.server.getPlayer(targetArgument)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

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
        val pageArgument = args.get(friendListPageArgumentNodeName) as Int

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

        message.append(getMessage(Message.FRIEND_LIST_PAGING, player, prevComponent, page, totalPages, nextComponent)).appendNewline()
        message.append(getMessage(Message.FRIEND_LIST_SEPARATOR, player))

        player.sendMessage(message)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun help(player: Player, args: CommandArguments) {
        player.sendMessage(Message.FRIENDS_HELP)
    }

    private fun getMessage(message: Message, player: Player, vararg args: Any): Component {
        return languageManager.getMessage(message, player, *args)
    }
}