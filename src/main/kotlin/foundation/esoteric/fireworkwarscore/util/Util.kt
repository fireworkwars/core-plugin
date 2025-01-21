package foundation.esoteric.fireworkwarscore.util

import kotlin.math.ceil

class Util {
    companion object {
        @JvmStatic
        fun <E> getPageItems(list: List<E>, page: Int, itemsPerPage: Int): List<E> {
            val totalPages = ceil(list.size.toDouble() / itemsPerPage).toInt()
            val clampedPage = page.coerceIn(1, totalPages)

            val startIndex = (clampedPage - 1) * itemsPerPage
            val endIndex = startIndex + itemsPerPage

            return list.subList(startIndex, endIndex.coerceAtMost(list.size))
        }
    }
}