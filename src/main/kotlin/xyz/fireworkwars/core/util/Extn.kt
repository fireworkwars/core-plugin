package xyz.fireworkwars.core.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import xyz.fireworkwars.core.language.LanguageManager
import xyz.fireworkwars.core.language.Message
import kotlin.math.pow
import kotlin.math.round

@Suppress("unused")
fun Player.playSound(sound: Sound) {
    this.playSound(this, sound, 1.0F, 1.0F)
}

fun HumanEntity.playSound(sound: Sound) {
    (this as Player).playSound(sound)
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

fun Double.toFixed(places: Int): Double {
    require(places >= 0) { "Decimal places must be non-negative" }

    val factor = 10.0.pow(places)
    return round(this * factor) / factor
}

@Suppress("unused")
fun Player.prepareAndTeleport(location: Location) {
    this.passengers.forEach(Entity::remove)
    this.velocity = Vector(0, 0, 0)
    this.fallDistance = 0.0F
    this.teleport(location)
}

fun Matrix4f.toMinecraft(): Transformation {
    val position = Vector3f()
    val rotation = Quaternionf()
    val scale = Vector3f()

    this.getTranslation(position)
    this.getUnnormalizedRotation(rotation)
    this.getScale(scale)

    return Transformation(position, rotation, scale, rotation)
}