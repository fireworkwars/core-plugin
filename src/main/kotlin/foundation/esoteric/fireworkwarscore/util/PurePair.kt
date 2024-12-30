package foundation.esoteric.fireworkwarscore.util

@Suppress("unused")
class PurePair<T: Any>(left: T, right: T) : Pair<T, T>(left, right) {
    fun min(comparator: Comparator<T>): T {
        return if (comparator.compare(left, right) <= 0) left else right
    }

    fun max(comparator: Comparator<T>): T {
        return if (comparator.compare(left, right) >= 0) left else right
    }
}