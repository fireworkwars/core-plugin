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
        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                plugin.runTask {
                    it.dailyStats.reset()
                }
            }

            this.saveNextDailyResetDate()
        }, this.getNextDailyReset(), 24, TimeUnit.HOURS)
    }

    private fun scheduleWeeklyReset() {
        executor.scheduleAtFixedRate({
            plugin.playerDataManager.getAllProfiles().forEach {
                plugin.runTask {
                    it.weeklyStats.reset()
                }
            }

            this.saveNextWeeklyResetDate()
        }, this.getNextWeeklyReset(), 7, TimeUnit.DAYS)
    }

    private fun saveNextDailyResetDate() {
        val nextDailyReset = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)

        plugin.config.set(nextDailyResetKey, nextDailyReset)
        plugin.saveConfig()
    }

    private fun saveNextWeeklyResetDate() {
        val nextWeeklyReset = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)

        plugin.config.set(nextWeeklyResetKey, nextWeeklyReset)
        plugin.saveConfig()
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