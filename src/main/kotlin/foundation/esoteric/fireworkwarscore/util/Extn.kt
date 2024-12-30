package foundation.esoteric.fireworkwarscore.util

import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.language.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Sound
import org.bukkit.entity.Player

@Suppress("unused")
fun Player.playSound(sound: Sound) {
    this.playSound(this, sound, 1.0F, 1.0F)
}

@Suppress("unused")
fun Player.sendMessage(message: Message, vararg args: Any?) {
    LanguageManager.globalInstance?.sendMessage(message, this, *args)
}

fun String.format(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}