package foundation.esoteric.fireworkwarscore.profiles

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

@Suppress("unused", "MemberVisibilityCanBePrivate")
enum class Rank(private val color: TextColor, private val prefixValue: String?) {
    PLAYER(NamedTextColor.GRAY, null),
    GOLD(NamedTextColor.GOLD, "[✫]");

    val prefix: Component
        get() = if (prefixValue != null) text(prefixValue) else empty()
    val coloredPrefix: Component
        get() = prefix.color(color)

    fun formatPlayerName(player: Player): Component {
        return prefix.appendSpace().append(player.name()).color(color)
    }
}