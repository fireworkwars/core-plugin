package foundation.esoteric.fireworkwarscore.util

import org.bukkit.Sound
import org.bukkit.entity.Player

//extend org.bukkit.player.Player and make a new playSound(Sound sound) method
fun Player.playSound(sound: Sound) {
    this.playSound(this, sound, 1.0F, 1.0F)
}