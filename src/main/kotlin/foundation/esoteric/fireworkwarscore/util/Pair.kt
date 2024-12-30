package foundation.esoteric.fireworkwarscore.util

@Suppress("unused")
open class Pair<A, B>(val left: A, val right: B) {
    companion object {
        @JvmStatic
        fun <A, B> of(a: A, b: B): Pair<A, B> {
            return Pair(a, b)
        }
    }
}