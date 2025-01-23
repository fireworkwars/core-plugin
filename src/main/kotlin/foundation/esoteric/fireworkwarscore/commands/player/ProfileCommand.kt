package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import foundation.esoteric.fireworkwarscore.profiles.PlayerProfile
import foundation.esoteric.fireworkwarscore.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import kotlin.Pair

class ProfileCommand(private val plugin: FireworkWarsCorePlugin) : CommandAPICommand("profile") {
    private val playerDataManager = plugin.playerDataManager

    private val targetArgumentNodeName = "target"

    init {
        this.setRequirements { it is Player }
        this.withPermission(CommandPermission.NONE)

        this.withShortDescription("View a player's profile")
        this.withFullDescription("View your or another player's profile")

        this.withArguments(this.playerArgumentSupplier())
        this.executesPlayer(this::onPlayerExecution)

        this.register(plugin)
    }

    private fun playerArgumentSupplier(): Argument<OfflinePlayer> {
        return OfflinePlayerArgument(targetArgumentNodeName).setOptional(true)
    }

    private fun onPlayerExecution(player: Player, args: CommandArguments) {
        val target = args.getOrDefault(targetArgumentNodeName, player) as OfflinePlayer

        this.openProfileMenu(player, target)
    }

    fun openProfileMenu(player: Player, target: OfflinePlayer) {
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return player.sendMessage(Message.UNKNOWN_PLAYER)

        val stats = targetProfile.stats

        val gui = Gui.gui()
            .title("${targetProfile.username}'s Profile".format())
            .rows(6)
            .create()

        gui.setDefaultClickAction {
            it.whoClicked.playSound(Sound.UI_BUTTON_CLICK)
            it.isCancelled = true
        }

        gui.filler.fill(ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).asGuiItem())
        gui.filler.fillBetweenPoints(2, 0, 2, 9, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).asGuiItem())

        val head = this.createHead(player, target, targetProfile)

        val ivePlayedTheseGamesBefore = this.createEnchantedItem(player, Material.FIREWORK_ROCKET,
            Message.PROFILE_GAMES_TITLE,
            Message.PROFILE_TOTAL_GAMES to stats.gamesPlayed,
            Message.PROFILE_WIN_RATE to stats.getWinPercentage(),
            null,
            Message.PROFILE_CURRENT_WIN_STREAK to stats.currentWinStreak,
            Message.PROFILE_HIGHEST_WIN_STREAK to stats.highestWinStreak)

        val wins = this.createEnchantedItem(player, Material.GOLD_INGOT,
            Message.PROFILE_WINS_TITLE,
            Message.PROFILE_TOTAL_WINS to stats.wins,
            Message.PROFILE_TOTAL_LOSSES to stats.losses,
            null,
            Message.PROFILE_WIN_LOSS_RATIO to stats.getWinLossRatio())

        val kills = this.createItemWithoutAttribute(player, Material.DIAMOND_SWORD, Attribute.ATTACK_DAMAGE,
            Message.PROFILE_KILLS_TITLE,
            Message.PROFILE_TOTAL_KILLS to stats.kills,
            Message.PROFILE_TOTAL_DEATHS to stats.deaths,
            null,
            Message.PROFILE_KILL_DEATH_RATIO to stats.getKillDeathRatio())

        val achievements = this.createEnchantedItem(player, Material.DIAMOND,
            Message.PROFILE_ACHIEVEMENTS_TITLE,
            Message.PROFILE_ACHIEVEMENTS_UNLOCKED to targetProfile.achievements.size)

        val friends = this.createItem(player, Material.WRITABLE_BOOK,
            Message.PROFILE_FRIENDS_TITLE,
            Message.PROFILE_TOTAL_FRIENDS to targetProfile.friends.size)

        gui.setItem(4, head)
        gui.setItem(20, ivePlayedTheseGamesBefore)
        gui.setItem(21, wins)
        gui.setItem(22, kills)
        gui.setItem(23, achievements)
        gui.setItem(24, friends)

        this.refreshButtons(player, target, gui)

        gui.open(player)
    }

    private fun refreshButtons(player: Player, target: OfflinePlayer, gui: Gui) {
        val profile = playerDataManager.getPlayerProfile(player)
        val targetProfile = playerDataManager.getPlayerProfile(target, false)
            ?: return gui.close(player)

        val addFriend = this.createItem(player, Material.FEATHER,
            Message.PROFILE_ADD_FRIEND,
            Message.PROFILE_ADD_FRIEND_TEXT to targetProfile.formattedName())

        val block = this.createItem(player, Material.GUNPOWDER,
            Message.PROFILE_BLOCK,
            Message.PROFILE_BLOCK_TEXT to targetProfile.formattedName())

        val removeFriend = this.createItem(player, Material.REDSTONE,
            Message.PROFILE_REMOVE_FRIEND,
            Message.PROFILE_REMOVE_FRIEND_TEXT to targetProfile.formattedName())

        val unblock = this.createItem(player, Material.SUGAR,
            Message.PROFILE_UNBLOCK,
            Message.PROFILE_UNBLOCK_TEXT to targetProfile.formattedName())

        addFriend.setAction(this.getRunCommandAction(CommandType.ADD_FRIEND, target, gui))
        removeFriend.setAction(this.getRunCommandAction(CommandType.REMOVE_FRIEND, target, gui))
        block.setAction(this.getRunCommandAction(CommandType.BLOCK, target, gui))
        unblock.setAction(this.getRunCommandAction(CommandType.UNBLOCK, target, gui))

        gui.setItem(30, addFriend)
        gui.setItem(31, block)

        if (profile.friends.contains(targetProfile.uuid)) {
            gui.setItem(30, removeFriend)
        }

        if (profile.blocked.contains(targetProfile.uuid)) {
            gui.setItem(31, unblock)
        }

        gui.update()
    }

    private fun createHead(player: Player, target: OfflinePlayer, targetProfile: PlayerProfile): GuiItem {
        val item = ItemStack(Material.PLAYER_HEAD).apply {
            this.editMeta(SkullMeta::class.java) {
                it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)

                it.owningPlayer = target
                it.playerProfile = target.playerProfile

                val firstJoin = Util.formattedTimeDifference(targetProfile.firstJoinDate, System.currentTimeMillis(), player)
                val lastSeen = Util.formattedTimeDifference(targetProfile.lastSeenDate, System.currentTimeMillis(), player)

                val lore = mutableListOf(player.getMessage(Message.PROFILE_FIRST_JOIN, firstJoin))

                if (target.isOnline) {
                    lore.add(player.getMessage(Message.PROFILE_CURRENTLY_ONLINE))
                } else {
                    lore.add(player.getMessage(Message.PROFILE_LAST_SEEN, lastSeen))
                }

                it.customName(targetProfile.formattedName().decoration(TextDecoration.ITALIC, false))
                it.lore(lore)
            }
        }

        return ItemBuilder.from(item).asGuiItem()
    }

    private fun createItem(player: Player, material: Material, title: Message, vararg text: Pair<Message, Any>?): GuiItem {
        return ItemBuilder.from(material)
            .name(player.getMessage(title))
            .lore(*text.map { this.getComponent(player, it) }.toTypedArray())
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            .asGuiItem()
    }

    @Suppress("SameParameterValue")
    private fun createItemWithoutAttribute(player: Player, material: Material, attribute: Attribute, title: Message, vararg text: Pair<Message, Any>?): GuiItem {
        val itemStack = ItemStack(material)

        itemStack.editMeta {
            val key = NamespacedKey(plugin, "attribute")
            val modifier = AttributeModifier(key, 1.0, AttributeModifier.Operation.ADD_NUMBER)

            it.addAttributeModifier(attribute, modifier)
        }

        return ItemBuilder.from(itemStack)
            .name(player.getMessage(title))
            .lore(*text.map { this.getComponent(player, it) }.toTypedArray())
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            .asGuiItem()
    }

    private fun createEnchantedItem(player: Player, material: Material, title: Message, vararg text: Pair<Message, Any>?): GuiItem {
        return ItemBuilder.from(material)
            .name(player.getMessage(title))
            .lore(*text.map { this.getComponent(player, it) }.toTypedArray())
            .glow(true)
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            .asGuiItem()
    }

    private fun getComponent(player: Player, pair: Pair<Message, Any>?): Component {
        return if (pair == null) Component.empty()
        else player.getMessage(pair.first, pair.second)
    }

    private fun getRunCommandAction(command: CommandType, target: OfflinePlayer, gui: Gui): (event: InventoryClickEvent) -> Unit {
        return {
            val player = it.whoClicked as Player

            when (command) {
                CommandType.ADD_FRIEND -> plugin.friendCommand.addOrAcceptFriend(player, target)
                CommandType.REMOVE_FRIEND -> plugin.friendCommand.removeFriend(player, target)
                CommandType.BLOCK -> plugin.blockCommand.blockPlayer(player, target)
                CommandType.UNBLOCK -> plugin.blockCommand.unblockPlayer(player, target)
            }

            this.refreshButtons(player, target, gui)
        }
    }

    private enum class CommandType {
        ADD_FRIEND,
        REMOVE_FRIEND,
        BLOCK,
        UNBLOCK
    }
}
