package xyz.fireworkwars.core.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import kotlin.math.max
import kotlin.math.min

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Cuboid : MutableIterator<Block?>, Iterable<Block?> {
    var world: World

    var minX: Int
    var minY: Int
    var minZ: Int
    var maxX: Int
    var maxY: Int
    var maxZ: Int

    private var iteratorX: Int
    private var iteratorY: Int
    private var iteratorZ: Int

    var topLeft: Location
        get() = Location(world, minX.toDouble(), minY.toDouble(), minZ.toDouble())
        set(topLeft) {
            require(topLeft.world == world) { "Location must be in the same world" }

            this.minX = topLeft.blockX
            this.minY = topLeft.blockY
            this.minZ = topLeft.blockZ
        }

    var bottomRight: Location
        get() = Location(world, maxX.toDouble(), maxY.toDouble(), maxZ.toDouble())
        set(bottomRight) {
            require(bottomRight.world == world) { "Location must be in the same world" }

            this.maxX = bottomRight.blockX
            this.maxY = bottomRight.blockY
            this.maxZ = bottomRight.blockZ
        }

    constructor(topLeft: Location, bottomRight: Location) {
        require(topLeft.world == bottomRight.world) { "Locations must be in the same world" }

        this.world = topLeft.world
        this.minX = min(topLeft.blockX.toDouble(), bottomRight.blockX.toDouble()).toInt()
        this.minY = min(topLeft.blockY.toDouble(), bottomRight.blockY.toDouble()).toInt()
        this.minZ = min(topLeft.blockZ.toDouble(), bottomRight.blockZ.toDouble()).toInt()
        this.maxX = max(topLeft.blockX.toDouble(), bottomRight.blockX.toDouble()).toInt()
        this.maxY = max(topLeft.blockY.toDouble(), bottomRight.blockY.toDouble()).toInt()
        this.maxZ = max(topLeft.blockZ.toDouble(), bottomRight.blockZ.toDouble()).toInt()

        this.iteratorX = minX
        this.iteratorY = minY
        this.iteratorZ = minZ
    }

    constructor(topLeft: Location, width: Int, height: Int, depth: Int) : this(
        topLeft.world,
        topLeft.blockX,
        topLeft.blockY,
        topLeft.blockZ,
        topLeft.blockX + width,
        topLeft.blockY + height,
        topLeft.blockZ + depth
    )

    constructor(world: World, x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) {
        this.world = world
        this.minX = min(x1.toDouble(), x2.toDouble()).toInt()
        this.minY = min(y1.toDouble(), y2.toDouble()).toInt()
        this.minZ = min(z1.toDouble(), z2.toDouble()).toInt()
        this.maxX = max(x1.toDouble(), x2.toDouble()).toInt()
        this.maxY = max(y1.toDouble(), y2.toDouble()).toInt()
        this.maxZ = max(z1.toDouble(), z2.toDouble()).toInt()

        this.iteratorX = minX
        this.iteratorY = minY
        this.iteratorZ = minZ
    }

    override fun hasNext(): Boolean {
        return iteratorX <= maxX && iteratorY <= maxY && iteratorZ <= maxZ
    }

    override fun next(): Block {
        if (!this.hasNext()) {
            throw NoSuchElementException()
        }

        val block = world.getBlockAt(iteratorX, iteratorY, iteratorZ)

        if (++iteratorZ > maxZ) {
            iteratorZ = minZ

            if (++iteratorY > maxY) {
                iteratorY = minY

                iteratorX++
            }
        }

        return block
    }

    override fun remove() {
        throw UnsupportedOperationException("Cannot remove blocks from a Cuboid")
    }

    override fun iterator(): Iterator<Block?> {
        return this
    }

    override fun toString(): String {
        val topLeftX = topLeft.blockX
        val topLeftY = topLeft.blockY
        val topLeftZ = topLeft.blockZ

        val bottomRightX = bottomRight.blockX
        val bottomRightY = bottomRight.blockY
        val bottomRightZ = bottomRight.blockZ

        val volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)

        return String.format(
            "Rectangle from (%d, %d, %d) to (%d, %d, %d) with volume %d",
            topLeftX, topLeftY, topLeftZ, bottomRightX, bottomRightY, bottomRightZ, volume)
    }
}