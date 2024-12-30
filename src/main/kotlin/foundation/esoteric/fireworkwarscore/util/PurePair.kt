package foundation.esoteric.fireworkwarscore.util

import java.util.Comparator.comparingDouble
import java.util.Comparator.comparingInt

@Suppress("unused", "MemberVisibilityCanBePrivate")
class PurePair<T: Any>(left: T, right: T) : Pair<T, T>(left, right) {
    fun min(comparator: Comparator<T>): T {
        return if (comparator.compare(left, right) <= 0) left else right
    }

    fun max(comparator: Comparator<T>): T {
        return if (comparator.compare(left, right) >= 0) left else right
    }

    fun min(intSupplier: (T) -> Int): T {
        return min(comparingInt(intSupplier))
    }

    fun max(intSupplier: (T) -> Int): T {
        return max(comparingInt(intSupplier))
    }

    fun min(doubleSupplier: (T) -> Double): T {
        return min(comparingDouble(doubleSupplier))
    }

    fun max(doubleSupplier: (T) -> Double): T {
        return max(comparingDouble(doubleSupplier))
    }
}