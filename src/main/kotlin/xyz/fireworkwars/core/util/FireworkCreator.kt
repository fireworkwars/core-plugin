package xyz.fireworkwars.core.util

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.Firework
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import kotlin.random.Random

@Suppress("unused")
class FireworkCreator {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun createColoredFirework(color: Color, fade: Color, amount: Int, type: FireworkEffect.Type = FireworkEffect.Type.BALL, flicker: Boolean = false, trail: Boolean = false): ItemStack {
            val item = ItemStack(org.bukkit.Material.FIREWORK_ROCKET)
            val meta = item.itemMeta as FireworkMeta

            for (i in (1..amount)) {
                meta.addEffect(FireworkEffect.builder()
                    .withColor(color)
                    .withFade(fade)
                    .with(type)
                    .flicker(flicker)
                    .trail(trail)
                    .build())
            }

            item.itemMeta = meta
            return item

        }

        @JvmStatic
        @JvmOverloads
        fun createFirework(color: Color, amount: Int, type: FireworkEffect.Type = FireworkEffect.Type.BALL, flicker: Boolean = false, trail: Boolean = false): ItemStack {
            return createColoredFirework(Color.WHITE, color, amount, type, flicker, trail)
        }

        @JvmStatic
        fun randomSupplyDropFirework(): ItemStack {
            return this.createFirework(
                listOf(Color.BLUE, Color.PURPLE, Color.AQUA, Color.RED).random(),
                (1..4).random(),
                listOf(FireworkEffect.Type.BURST, FireworkEffect.Type.STAR).random(),
                Random.nextBoolean(),
                Random.nextBoolean())
        }

        @JvmStatic
        fun randomLobbyFirework(): ItemStack {
            return this.createColoredFirework(
                listOf(Color.BLUE, Color.PURPLE, Color.AQUA, Color.RED, Color.ORANGE, Color.LIME, Color.GREEN).random(),
                listOf(Color.BLUE, Color.PURPLE, Color.AQUA, Color.RED, Color.WHITE).random(),
                (1..4).random(),
                listOf(FireworkEffect.Type.BURST, FireworkEffect.Type.STAR).random(),
                Random.nextBoolean(),
                Random.nextBoolean())
        }

        @JvmStatic
        fun sendSupplyDropFirework(location: Location, flightTicks: Int) {
            val supplyDropFirework = this.randomSupplyDropFirework()
            val meta = supplyDropFirework.itemMeta as FireworkMeta

            location.world.spawn(location, Firework::class.java).apply {
                this.fireworkMeta = meta
                this.ticksToDetonate = flightTicks
            }
        }

        @JvmStatic
        fun sendLobbyFirework(location: Location, flightTicks: Int) {
            val lobbyFirework = this.randomLobbyFirework()
            val meta = lobbyFirework.itemMeta as FireworkMeta

            location.world.spawn(location, Firework::class.java).apply {
                this.fireworkMeta = meta
                this.ticksToDetonate = flightTicks
            }
        }
    }
}