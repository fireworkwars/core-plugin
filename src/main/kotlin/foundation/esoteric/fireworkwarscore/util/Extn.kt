package foundation.esoteric.fireworkwarscore.util

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import org.bukkit.Sound
import org.bukkit.entity.Player

@Suppress("unused")
fun Player.playSound(sound: Sound) {
    this.playSound(this, sound, 1.0F, 1.0F)
}

@Suppress("unused")
fun Player.sendMessage(message: Message, vararg args: Any?) {
    FireworkWarsCorePlugin.INSTANCE.languageManager.sendMessage(message, this, *args)
}