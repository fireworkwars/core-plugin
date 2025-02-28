package xyz.fireworkwars.core.util

import java.util.stream.Stream

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Range(private val start: Int, private val end: Int) {
    companion object {
        @JvmStatic
        fun from(start: Int, end: Int): Range {
            return Range(start, end)
        }
    }

    fun contains(value: Int): Boolean {
        return value in (start..end)
    }

    fun toList(): List<Int> {
        return (start..end).toList()
    }

    fun toStream(): Stream<Int> {
        return toList().stream()
    }
}