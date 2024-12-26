package foundation.esoteric.fireworkwarscore.util

@Suppress("unused")
class Pair<A, B>(val left: A, val right: B) {
    companion object {
        fun <A, B> of(a: A, b: B): Pair<A, B> {
            return Pair(a, b)
        }
    }
}