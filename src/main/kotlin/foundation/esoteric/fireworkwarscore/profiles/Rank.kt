package foundation.esoteric.fireworkwarscore.profiles

import foundation.esoteric.fireworkwarscore.util.appendSpaceIfNotEmpty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

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

    fun formatPlayerName(name: Component): Component {
        return prefix.appendSpaceIfNotEmpty().append(name).color(color).compact()
    }

    fun toFormattedText(): Component {
        return prefix.appendSpaceIfNotEmpty().append(text(capitalised())).color(color).compact()
    }

    fun updateTablist(player: Player) {
        player.playerListName(this.formatPlayerName(player))
        player.playerListOrder = listOrder
    }

    fun capitalised(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}