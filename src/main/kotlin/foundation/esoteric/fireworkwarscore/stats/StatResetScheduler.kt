package foundation.esoteric.fireworkwarscore.stats

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StatResetScheduler(private val plugin: FireworkWarsCorePlugin) {
    private val executor = Executors.newScheduledThreadPool(1)

    private val nextDailyResetKey = "next-daily-stat-reset"
    private val nextWeeklyResetKey = "next-weekly-stat-reset"

    fun scheduleDailyReset() {
        val nextReset = plugin.config.getLong(nextDailyResetKey)
        val delay = (nextReset - System.currentTimeMillis()).coerceAtLeast(0)

        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                plugin.runTaskLater(0) {
                    it.dailyStats.reset()
                }
            }
        }, delay, 24, TimeUnit.HOURS)

        val nextDailyReset = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1) + delay

        plugin.config.set(nextDailyResetKey, nextDailyReset)
        plugin.saveConfig()
    }

    fun scheduleWeeklyReset() {
        val nextReset = plugin.config.getLong(nextWeeklyResetKey)
        val delay = (nextReset - System.currentTimeMillis()).coerceAtLeast(0)

        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                it.weeklyStats.reset()
            }
        }, delay, 7, TimeUnit.DAYS)

        val nextWeeklyReset = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7) + delay

        plugin.config.set(nextDailyResetKey, nextWeeklyReset)
        plugin.saveConfig()
    }

    fun getNextDailyReset(): Long {
        return plugin.config.getLong(nextDailyResetKey) - System.currentTimeMillis()
    }

    fun getNextWeeklyReset(): Long {
        return plugin.config.getLong(nextWeeklyResetKey) - System.currentTimeMillis()
    }
}