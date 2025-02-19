package xyz.fireworkwars.core.util

import java.util.Comparator.comparingDouble
import java.util.Comparator.comparingInt

@Suppress("unused")
class PurePair<T : Any>(left: T, right: T) : Pair<T, T>(left, right) {
    companion object {
        @JvmStatic
        fun <T : Any> of(a: T, b: T): PurePair<T> {
            return PurePair(a, b)
        }
    }

    fun min(comparator: Comparator<T>): T {
        return if (comparator.compare(left, right) <= 0) left else right
    }

    fun max(comparator: Comparator<T>): T {
        return if (comparator.compare(left, right) >= 0) left else right
    }

    fun minInt(intSupplier: (T) -> Int): T {
        return this.min(comparingInt(intSupplier))
    }

    fun maxInt(intSupplier: (T) -> Int): T {
        return this.max(comparingInt(intSupplier))
    }

    fun minDouble(doubleSupplier: (T) -> Double): T {
        return this.min(comparingDouble(doubleSupplier))
    }

    fun maxDouble(doubleSupplier: (T) -> Double): T {
        return this.max(comparingDouble(doubleSupplier))
    }
}