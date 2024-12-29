package foundation.esoteric.fireworkwarscore.communication

import foundation.esoteric.fireworkwarscore.language.LanguageManager
import foundation.esoteric.fireworkwarscore.profiles.PlayerDataManager
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull

abstract class BasePlugin : JavaPlugin() {
    @get:NotNull
    abstract var playerDataManager: PlayerDataManager
    @get:NotNull
    abstract var languageManager: LanguageManager
}