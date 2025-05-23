package xyz.fireworkwars.core.util

import java.util.Comparator.comparingDouble
import java.util.Comparator.comparingInt
import java.util.function.ToDoubleFunction
import java.util.function.ToIntFunction

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

    fun minInt(intSupplier: ToIntFunction<T>): T {
        return this.min(comparingInt(intSupplier))
    }

    fun maxInt(intSupplier: ToIntFunction<T>): T {
        return this.max(comparingInt(intSupplier))
    }

    fun minDouble(doubleSupplier: ToDoubleFunction<T>): T {
        return this.min(comparingDouble(doubleSupplier))
    }

    fun maxDouble(doubleSupplier: ToDoubleFunction<T>): T {
        return this.max(comparingDouble(doubleSupplier))
    }
}