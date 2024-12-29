package foundation.esoteric.fireworkwarscore.util

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta

@Suppress("unused")
class FireworkCreator {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun createFirework(
            color: Color,
            amount: Int,
            type: FireworkEffect.Type = FireworkEffect.Type.BALL,
            flicker: Boolean = false,
            trail: Boolean = false): ItemStack
        {
            val item = ItemStack(org.bukkit.Material.FIREWORK_ROCKET)
            val meta = item.itemMeta as FireworkMeta

            for (i in (0..amount)) {
                meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.WHITE)
                    .withFade(color)
                    .with(type)
                    .flicker(flicker)
                    .trail(trail)
                    .build())
            }

            item.itemMeta = meta
            return item
        }
    }
}