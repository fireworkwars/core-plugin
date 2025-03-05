package xyz.fireworkwars.core.managers

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.joml.Matrix4f
import org.joml.Quaternionf
import xyz.fireworkwars.core.util.toMinecraft
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
class ScreenOverlayManager(private val plugin: Plugin) {
    private val activeOverlays = ConcurrentHashMap<UUID, OverlayData>()
    private var updateTask: BukkitTask? = null
    private var isEnabled = false

    fun enable() {
        if (isEnabled) return

        this.isEnabled = true
        this.startUpdateTask()
    }

    fun disable() {
        if (!isEnabled) return

        this.isEnabled = false
        this.stopUpdateTask()
        this.cleanup()
    }

    fun addOverlay(player: Player, color: Color) {
        val overlayData = OverlayData(player.world, color)

        activeOverlays[player.uniqueId]?.cleanup()
        activeOverlays[player.uniqueId] = overlayData
    }

    fun removeOverlay(player: Player) {
        activeOverlays.remove(player.uniqueId)?.cleanup()
    }

    fun setOverlayColor(player: Player, color: Color) {
        activeOverlays[player.uniqueId]?.color = color
    }

    fun hasOverlay(player: Player): Boolean {
        return activeOverlays.containsKey(player.uniqueId)
    }

    fun getOverlayColor(player: Player): Color? {
        return activeOverlays[player.uniqueId]?.color
    }

    private fun startUpdateTask() {
        this.updateTask = OverlayRunnable(plugin, this).start()
    }

    private fun stopUpdateTask() {
        updateTask?.cancel()
        updateTask = null
    }

    private fun updateOverlays() {
        val iterator = activeOverlays.iterator()

        while (iterator.hasNext()) {
            val (playerId, overlayData) = iterator.next()

            val player = this.getPlayerOr(playerId) {
                overlayData.cleanup()
                iterator.remove()
            } ?: continue

            overlayData.update(player.eyeLocation)
        }
    }

    private fun getPlayerOr(id: UUID, orElse: () -> Unit): Player? {
        val player = plugin.server.getPlayer(id)

        if (player == null || !player.isOnline) {
            orElse()
        }

        return player
    }

    private fun cleanup() {
        activeOverlays.values.forEach { it.cleanup() }
        activeOverlays.clear()
    }

    private class OverlayData(initialWorld: World, initialColor: Color) {
        var color: Color = initialColor

        private val panels: List<TextDisplay>
        private val transforms: List<Matrix4f>

        init {
            this.transforms = this.createTransformations()
            this.panels = this.createPanels(initialWorld)
        }

        private fun createTransformations(): List<Matrix4f> {
            val size = 2.5f
            val baseTransformation = Matrix4f()
                .translate(-0.1f + 0.5f, -0.5f + 0.5f, 0f)
                .scale(8.0f, 4.0f, 1f)

            val sides = listOf(
                Quaternionf(), // Front
                Quaternionf().rotateY(Math.PI.toFloat() / 2), // Right
                Quaternionf().rotateY(Math.PI.toFloat()), // Back
                Quaternionf().rotateY(-Math.PI.toFloat() / 2), // Left
                Quaternionf().rotateX(Math.PI.toFloat() / 2), // Top
                Quaternionf().rotateX(-Math.PI.toFloat() / 2)) // Bottom

            return sides.map {
                Matrix4f()
                    .rotate(it)
                    .scale(size, size, 1f)
                    .translate(-0.5f, -0.5f, -size / 2)
                    .mul(baseTransformation)
            }
        }

        private fun createPanels(world: World): List<TextDisplay> {
            return transforms.map {
                world.spawn(Location(world, 0.0, 0.0, 0.0), TextDisplay::class.java).apply {
                    this.text(Component.space())
                    this.brightness = Display.Brightness(15, 15)
                    this.isSeeThrough = true
                    this.backgroundColor = color
                    this.teleportDuration = 1
                    this.transformation = it.toMinecraft()
                }
            }
        }

        fun update(eyeLocation: Location) {
            panels.forEach {
                it.teleport(eyeLocation)
                it.backgroundColor = color
            }
        }

        fun cleanup() {
            panels.forEach { it.remove() }
        }
    }

    private class OverlayRunnable(private val plugin: Plugin, private val manager: ScreenOverlayManager) : BukkitRunnable() {
        fun start(): BukkitTask {
            return this.runTaskTimer(plugin, 0L, 1L)
        }

        override fun run() {
            if (!manager.isEnabled) {
                return this.cancel()
            }

            manager.updateOverlays()
        }
    }
}
