package foundation.esoteric.fireworkwarscore

import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull

abstract class BasePlugin : JavaPlugin() {
    @get:NotNull
    abstract val playerDataManager: PlayerDataManager
    @get:NotNull
    abstract val languageManager: LanguageManager
}