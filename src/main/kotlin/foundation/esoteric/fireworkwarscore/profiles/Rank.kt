package foundation.esoteric.fireworkwarscore.profiles

import foundation.esoteric.fireworkwarscore.util.NMSUtil
import foundation.esoteric.fireworkwarscore.util.appendSpaceIfNotEmpty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.server.level.ServerPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused", "MemberVisibilityCanBePrivate")
enum class Rank(val color: TextColor, private val prefixValue: String?, private val listOrder: Int) {
    NONE(NamedTextColor.GRAY, null, 0),
    GOLD(NamedTextColor.GOLD, "[✫]", 1);

    val prefix: Component
        get() = if (prefixValue != null) text(prefixValue) else empty()
    val coloredPrefix: Component
        get() = prefix.color(color)

    fun formatPlayerName(player: Player): Component {
        return prefix.appendSpaceIfNotEmpty().append(player.name()).color(color).compact()
    }

    fun toFormattedText(): Component {
        return prefix.appendSpaceIfNotEmpty().append(text(capitalised())).color(color).compact()
    }

    fun updateTablist(player: Player, plugin: JavaPlugin) {
        player.playerListName(formatPlayerName(player))
        player.playerListOrder = listOrder

        val nmsPlayer = NMSUtil.toNMSEntity<ServerPlayer>(player)
        val packet = ClientboundPlayerInfoUpdatePacket(Action.UPDATE_LIST_ORDER, nmsPlayer)

        plugin.server.onlinePlayers.forEach {
            NMSUtil.toNMSEntity<ServerPlayer>(it).connection.send(packet)
        }
    }

    fun capitalised(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}