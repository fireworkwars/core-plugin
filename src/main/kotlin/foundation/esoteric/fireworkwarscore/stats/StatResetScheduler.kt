package foundation.esoteric.fireworkwarscore.stats

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class StatResetScheduler(private val plugin: FireworkWarsCorePlugin) {
    private val executor = Executors.newScheduledThreadPool(1)

    private val nextDailyResetKey = "next-daily-stat-reset"
    private val nextWeeklyResetKey = "next-weekly-stat-reset"

    fun schedule() {
        this.scheduleDailyReset()
        this.scheduleWeeklyReset()

        plugin.saveConfig()
    }

    private fun scheduleDailyReset() {
        val delay = this.getNextDailyReset()

        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                plugin.runTaskLater(0) {
                    it.dailyStats.reset()
                }
            }
        }, delay, 24, TimeUnit.HOURS)

        val nextDailyReset = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1) + delay
        plugin.config.set(nextDailyResetKey, nextDailyReset)
    }

    private fun scheduleWeeklyReset() {
        val delay = this.getNextWeeklyReset()

        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                it.weeklyStats.reset()
            }
        }, delay, 7, TimeUnit.DAYS)

        val nextWeeklyReset = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7) + delay
        plugin.config.set(nextWeeklyResetKey, nextWeeklyReset)
    }

    fun getNextDailyReset(): Long {
        val difference = plugin.config.getLong(nextDailyResetKey) - System.currentTimeMillis()
        return difference.coerceAtLeast(0)
    }

    fun getNextWeeklyReset(): Long {
        val difference = plugin.config.getLong(nextWeeklyResetKey) - System.currentTimeMillis()
        return difference.coerceAtLeast(0)
    }
}