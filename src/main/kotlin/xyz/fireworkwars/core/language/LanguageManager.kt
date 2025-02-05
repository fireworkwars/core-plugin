package xyz.fireworkwars.core.language

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import xyz.fireworkwars.core.file.FileUtil
import xyz.fireworkwars.core.profiles.PlayerProfile
import java.io.File
import java.util.*
import java.util.Objects.requireNonNull

@Suppress("unused", "MethodCouldBePrivate", "MemberVisibilityCanBePrivate")
class LanguageManager(private val plugin: FireworkWarsCorePlugin) {
    companion object {
        @JvmStatic
        @get:NotNull
        lateinit var globalInstance: LanguageManager
    }

    private val miniMessage = MiniMessage.builder().strict(true).build()

    private val languagesFolderName = "languages"
    private val languagesFolderPath: String
    private val languagesFolder: File

    val defaultLanguage: String

    private val languages: MutableMap<String?, Map<Message, String>> = HashMap()

    val totalLanguages: Int
        get() = languages.size
    val totalMessages: Int
        get() = languages[defaultLanguage]!!.size

    fun getLanguages(): Set<String?> {
        return languages.keys
    }

    init {
        globalInstance = this

        val dataFolder: File = plugin.dataFolder

        this.languagesFolderPath = dataFolder.path + File.separator + languagesFolderName
        this.languagesFolder = File(languagesFolderPath)

        this.saveLanguageFiles()
        this.loadLanguageMessages()

        this.defaultLanguage = plugin.config.getString("default-language")!!
    }

    private fun saveLanguageFiles() {
        val languagesResourceFolderName = "$languagesFolderName/"
        val languageResourceFileNames = FileUtil.getFileNamesInFolder(languagesResourceFolderName)

        languageResourceFileNames.forEach {
            plugin.saveResource(languagesFolderName + File.separator + it, true)
        }
    }

    private fun loadLanguageMessages() {
        for (languageMessagesFile in requireNonNull(languagesFolder.listFiles())) {
            val languageName = languageMessagesFile.name.split("\\.".toRegex(), limit = 2).toTypedArray()[0]

            val languageMessagesResourcePath = languagesFolderName + File.separator + languageName + ".yaml"
            plugin.saveResource(languageMessagesResourcePath, true)

            val messagesConfiguration = YamlConfiguration.loadConfiguration(languageMessagesFile)
            val messages: MutableMap<Message, String> = EnumMap(Message::class.java)

            for (message in Message.entries) {
                val mappedResult = messagesConfiguration.getString(message.name)

                if (mappedResult != null) {
                    messages[message] = mappedResult
                }
            }

            languages[languageName] = messages
        }
    }

    fun getLanguage(commandSender: CommandSender?): String {
        var language = this.getProfileLanguage(commandSender)

        if (language == null) {
            language = this.getLocale(commandSender)
        }

        return language
    }

    fun getLanguage(uuid: UUID?): String {
        var language = this.getProfileLanguage(uuid)

        if (language == null) {
            language = this.getLocale(uuid)
        }

        return language
    }

    fun getLanguage(profile: PlayerProfile): String {
        return this.getLanguage(profile.uuid)
    }

    fun setLanguage(profile: PlayerProfile, language: String?) {
        profile.language = language ?: defaultLanguage
    }

    fun setLanguage(uuid: UUID, language: String?) {
        this.setLanguage(plugin.playerDataManager.getPlayerProfile(uuid), language)
    }

    fun setLanguage(player: Player, language: String?) {
        this.setLanguage(player.uniqueId, language)
    }

    fun getLocale(commandSender: CommandSender?): String {
        if (commandSender !is Player) {
            return defaultLanguage
        }

        val playerLocale = commandSender.locale()
        val localeDisplayName = playerLocale.displayName

        if (!this.getLanguages().contains(localeDisplayName)) {
            return defaultLanguage
        }

        return localeDisplayName
    }

    fun getLocale(uuid: UUID?): String {
        val player = Bukkit.getPlayer(uuid!!)
        return this.getLocale(player)
    }

    fun getLocale(profile: PlayerProfile): String {
        return this.getLocale(profile.uuid)
    }

    fun getProfileLanguage(profile: PlayerProfile?): String? {
        if (profile == null) {
            return null
        }

        return profile.language
    }

    fun getProfileLanguage(uuid: UUID?): String? {
        if (uuid == null) {
            return null
        }

        return this.getProfileLanguage(plugin.playerDataManager.getPlayerProfile(uuid))
    }

    fun getProfileLanguage(commandSender: CommandSender?): String? {
        return when (commandSender) {
            null -> null
            is Player -> this.getProfileLanguage(commandSender.uniqueId)
            else -> defaultLanguage
        }
    }

    fun getRawMessageString(message: Message, language: String?, fallbackOnDefaultLanguage: Boolean): String? {
        val languageMessageMap = languages[language]!!
        val miniMessageString = languageMessageMap[message]
            ?: return if (fallbackOnDefaultLanguage) this.getRawMessageString(message, defaultLanguage, false)
                      else null

        return miniMessageString
    }

    fun getRawMessageString(message: Message, language: String?): String? {
        return this.getRawMessageString(message, language, true)
    }

    private fun getMessage(
        message: Message,
        language: String?,
        fallbackOnDefaultLanguage: Boolean,
        vararg arguments: Any?
    ): Component {
        var rawString = this.getRawMessageString(message, language, fallbackOnDefaultLanguage)
            ?: throw IllegalArgumentException("Message not found: $message in language: $language")

        arguments.forEachIndexed { index, arg ->
            val pattern = Regex("\\{${index}}")

            val replacement = when (arg) {
                is Component -> miniMessage.serialize(arg)
                is String -> arg
                else -> arg.toString()
            }

            rawString = rawString.replace(pattern, replacement)
        }

        return miniMessage.deserialize(rawString)
    }

    fun getDefaultMessage(message: Message, vararg arguments: Any?): Component {
        return this.getMessage(message, defaultLanguage, true, *arguments)
    }

    fun getMessage(message: Message, language: String?, vararg arguments: Any?): Component {
        return this.getMessage(message, language, true, *arguments)
    }

    fun getMessage(
        message: Message,
        commandSender: CommandSender?,
        fallbackOnDefaultLanguage: Boolean,
        vararg arguments: Any?
    ): Component {
        return this.getMessage(message, this.getLanguage(commandSender), fallbackOnDefaultLanguage, *arguments)
    }

    fun getMessage(message: Message, commandSender: CommandSender?, vararg arguments: Any?): Component {
        return this.getMessage(message, commandSender, true, *arguments)
    }

    fun getMessage(
        message: Message,
        uuid: UUID?,
        fallbackOnDefaultLanguage: Boolean,
        vararg arguments: Any?
    ): Component {
        return this.getMessage(message, getLanguage(uuid), fallbackOnDefaultLanguage, *arguments)
    }

    fun getMessage(message: Message, uuid: UUID?, vararg arguments: Any?): Component {
        return this.getMessage(message, uuid, true, *arguments)
    }

    fun getMessage(
        message: Message,
        playerProfile: PlayerProfile,
        fallbackOnDefaultLanguage: Boolean,
        vararg arguments: Any?
    ): Component {
        return this.getMessage(message, this.getLanguage(playerProfile), fallbackOnDefaultLanguage, *arguments)
    }

    fun getMessage(message: Message, playerProfile: PlayerProfile, vararg arguments: Any?): Component {
        return this.getMessage(message, playerProfile, true, *arguments)
    }

    fun getMessages(message: Message, commandSender: CommandSender?, vararg arguments: Any?): Array<Component> {
        val rawMessage = miniMessage.serialize(getMessage(message, commandSender, *arguments))

        return rawMessage.split("\\n|<br>|<newline>|<br/>|<newline/>".toRegex())
            .asSequence()
            .filter { it.isNotEmpty() }
            .map(miniMessage::deserialize)
            .toList()
            .toTypedArray()
    }

    fun sendMessage(message: Message, commandSender: CommandSender, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?) {
        commandSender.sendMessage(this.getMessage(
            message, this.getLanguage(commandSender), fallbackOnDefaultLanguage, *arguments))
    }

    fun sendMessage(message: Message, commandSender: CommandSender, vararg arguments: Any?) {
        commandSender.sendMessage(this.getMessage(
            message, commandSender, true, *arguments))
    }

    fun sendMessage(message: Message, uuid: UUID?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?) {
        if (uuid == null) {
            return
        }

        val player = plugin.server.getPlayer(uuid) ?: return

        player.sendMessage(this.getMessage(
            message, this.getLanguage(uuid), fallbackOnDefaultLanguage, *arguments))
    }

    fun sendMessage(message: Message, uuid: UUID?, vararg arguments: Any?) {
        if (uuid == null) {
            return
        }

        val player = plugin.server.getPlayer(uuid) ?: return

        player.sendMessage(this.getMessage(
            message, uuid, true, *arguments))
    }

    fun sendMessage(message: Message, playerProfile: PlayerProfile, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?) {
        val player = plugin.server.getPlayer(playerProfile.uuid) ?: return

        player.sendMessage(this.getMessage(
            message, playerProfile, fallbackOnDefaultLanguage, *arguments))
    }

    fun sendMessage(message: Message, playerProfile: PlayerProfile, vararg arguments: Any?) {
        val player = plugin.server.getPlayer(playerProfile.uuid) ?: return

        player.sendMessage(this.getMessage(
            message, playerProfile, true, *arguments))
    }
}