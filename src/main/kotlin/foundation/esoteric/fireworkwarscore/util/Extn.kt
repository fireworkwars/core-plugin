package foundation.esoteric.fireworkwarscore.util

import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.language.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player

@Suppress("unused")
fun Player.playSound(sound: Sound) {
    this.playSound(this, sound, 1.0F, 1.0F)
}

fun Player.sendMessage(message: Message, vararg args: Any?) {
    LanguageManager.globalInstance.sendMessage(message, this, *args)
}

fun OfflinePlayer.sendMessage(message: Message, vararg args: Any?) {
    this.player?.sendMessage(message, *args)
}

fun Player.getMessage(message: Message, vararg args: Any?): Component {
    return LanguageManager.globalInstance.getMessage(message, this, *args)
}

fun OfflinePlayer.getMessage(message: Message, vararg args: Any?): Component {
    return LanguageManager.globalInstance.getMessage(message, this.uniqueId, *args)
}

fun String.format(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}

fun Component.appendSpaceIfNotEmpty(): Component {
    val isEmpty = MiniMessage.miniMessage().serialize(this).isEmpty()

    return if (!isEmpty) {
        this.appendSpace()
    } else {
        this
    }
}