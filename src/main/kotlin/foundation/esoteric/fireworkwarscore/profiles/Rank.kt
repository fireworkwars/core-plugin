package foundation.esoteric.fireworkwarscore.profiles

import foundation.esoteric.fireworkwarscore.util.appendSpaceIfNotEmpty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

@Suppress("unused", "MemberVisibilityCanBePrivate")
enum class Rank(val listOrder: Int, val color: TextColor, private val prefixValue: String?) {
    PLAYER(1, NamedTextColor.GRAY, null),
    GOLD(0, NamedTextColor.GOLD, "[✫]");

    val prefix: Component
        get() = if (prefixValue != null) text(prefixValue) else empty()
    val coloredPrefix: Component
        get() = prefix.color(color)

    fun formatPlayerName(player: Player): Component {
        return prefix.appendSpaceIfNotEmpty().append(player.name()).color(color)
    }

    fun toFormattedText(): Component {
        return prefix.appendSpaceIfNotEmpty().append(text(toString())).color(color)
    }

    override fun toString(): String {
        return name.lowercase()
    }
}