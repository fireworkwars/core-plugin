package xyz.fireworkwars.core.util

import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import xyz.fireworkwars.core.language.Message
import java.util.concurrent.TimeUnit
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

        @JvmStatic
        fun formattedTimeDifference(start: Long, end: Long, player: OfflinePlayer): Component {
            val millis = end - start

            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
            val days = TimeUnit.MILLISECONDS.toDays(millis) % 7
            val weeks = (TimeUnit.MILLISECONDS.toDays(millis) / 7) % 4
            val months = (TimeUnit.MILLISECONDS.toDays(millis) / 30) % 12
            val years = TimeUnit.MILLISECONDS.toDays(millis) / 365

            val secondsPl = if (seconds == 1L) "" else "s"
            val minutesPl = if (minutes == 1L) "" else "s"
            val hoursPl = if (hours == 1L) "" else "s"
            val daysPl = if (days == 1L) "" else "s"
            val weeksPl = if (weeks == 1L) "" else "s"
            val monthsPl = if (months == 1L) "" else "s"
            val yearsPl = if (years == 1L) "" else "s"

            return when {
                years > 0 -> if (months == 0L) {
                    player.getMessage(Message.TIME_YEARS, years, yearsPl)
                } else {
                    player.getMessage(Message.TIME_YEARS_AND_MONTHS, years, yearsPl, months, monthsPl)
                }

                months > 0 -> if (weeks == 0L) {
                    player.getMessage(Message.TIME_MONTHS, months, monthsPl)
                } else {
                    player.getMessage(Message.TIME_MONTHS_AND_WEEKS, months, monthsPl, weeks, weeksPl)
                }

                weeks > 0 -> if (days == 0L) {
                    player.getMessage(Message.TIME_WEEKS, weeks, weeksPl)
                } else {
                    player.getMessage(Message.TIME_WEEKS_AND_DAYS, weeks, weeksPl, days, daysPl)
                }

                days > 0 -> if (hours == 0L) {
                    player.getMessage(Message.TIME_DAYS, days, daysPl)
                } else {
                    player.getMessage(Message.TIME_DAYS_AND_HOURS, days, daysPl, hours, hoursPl)
                }

                hours > 0 -> if (minutes == 0L) {
                    player.getMessage(Message.TIME_HOURS, hours, hoursPl)
                } else {
                    player.getMessage(Message.TIME_HOURS_AND_MINUTES, hours, hoursPl, minutes, minutesPl)
                }

                minutes > 0 -> if (seconds == 0L) {
                    player.getMessage(Message.TIME_MINUTES, minutes, minutesPl)
                } else {
                    player.getMessage(Message.TIME_MINUTES_AND_SECONDS, minutes, minutesPl, seconds, secondsPl)
                }

                else -> player.getMessage(Message.TIME_SECONDS)
            }
        }
    }
}