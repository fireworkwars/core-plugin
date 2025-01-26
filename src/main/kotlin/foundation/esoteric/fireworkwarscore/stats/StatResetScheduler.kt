package foundation.esoteric.fireworkwarscore.stats

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StatResetScheduler(private val plugin: FireworkWarsCorePlugin) {
    private val executor = Executors.newScheduledThreadPool(1)

    private val nextDailyResetKey = "next-daily-stat-reset"
    private val nextWeeklyResetKey = "next-weekly-stat-reset"

    fun handleDailyReset() {
        val nextReset = plugin.config.getLong(nextDailyResetKey)
        val delay = (nextReset - System.currentTimeMillis()).coerceAtLeast(0)

        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                plugin.runTaskLater(0) {
                    it.dailyStats.reset()
                }
            }
        }, delay, 24, TimeUnit.HOURS)

        val nextDailyReset = delay + TimeUnit.DAYS.toMillis(1)
        plugin.config.set(nextDailyResetKey, nextDailyReset)
    }

    fun handleWeeklyReset() {
        val nextReset = plugin.config.getLong(nextWeeklyResetKey)
        val delay = (nextReset - System.currentTimeMillis()).coerceAtLeast(0)

        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                it.weeklyStats.reset()
            }
        }, delay, 7, TimeUnit.DAYS)

        val nextWeeklyReset = delay + TimeUnit.DAYS.toMillis(7)
        plugin.config.set(nextDailyResetKey, nextWeeklyReset)
    }
}