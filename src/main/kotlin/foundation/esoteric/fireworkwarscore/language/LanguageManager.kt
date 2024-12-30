package foundation.esoteric.fireworkwarscore.language

import foundation.esoteric.fireworkwarscore.communication.BasePlugin
import foundation.esoteric.fireworkwarscore.file.FileUtil
import foundation.esoteric.fireworkwarscore.profiles.PlayerProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*
import java.util.Objects.requireNonNull


@Suppress("unused", "MethodCouldBePrivate", "MemberVisibilityCanBePrivate")
class LanguageManager(private val plugin: BasePlugin) {
    companion object {
        @JvmStatic
        @get:NotNull
        var globalInstance: LanguageManager? = null
    }

    private val miniMessage = MiniMessage.miniMessage()

    private val languagesFolderName = "languages"
    private val languagesFolderPath: String
    private val languagesFolder: File

    val defaultLanguage: String

    private val languages: MutableMap<String?, Map<Message, String>> = HashMap()

    val totalLanguages: Int
        get(): Int = languages.size
    val totalMessages: Int
        get(): Int = languages[defaultLanguage]!!.size

    fun getLanguages(): Set<String?> {
        return languages.keys
    }

    init {
        val dataFolder: File = plugin.dataFolder
        languagesFolderPath = dataFolder.path + File.separator + languagesFolderName
        languagesFolder = File(languagesFolderPath)

        saveLanguageFiles()
        loadLanguageMessages()

        defaultLanguage = plugin.getConfig().getString("default-language")!!
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
        var language = getProfileLanguage(commandSender)

        if (language == null) {
            language = getLocale(commandSender)
        }

        return language
    }

    fun getLanguage(uuid: UUID?): String {
        var language = getProfileLanguage(uuid)

        if (language == null) {
            language = getLocale(uuid)
        }

        return language
    }

    fun getLanguage(profile: PlayerProfile): String {
        return getLanguage(profile.uuid)
    }

    fun setLanguage(profile: PlayerProfile, language: String?) {
        profile.language = language ?: defaultLanguage
    }

    fun setLanguage(uuid: UUID, language: String?) {
        setLanguage(plugin.playerDataManager.getPlayerProfile(uuid), language)
    }

    fun setLanguage(player: Player, language: String?) {
        setLanguage(player.uniqueId, language)
    }

    fun getLocale(commandSender: CommandSender?): String {
        if (commandSender !is Player) {
            return defaultLanguage
        }

        val playerLocale = commandSender.locale()
        val localeDisplayName = playerLocale.displayName

        if (!getLanguages().contains(localeDisplayName)) {
            return defaultLanguage
        }

        return localeDisplayName
    }

    fun getLocale(uuid: UUID?): String {
        val player = Bukkit.getPlayer(uuid!!)
        return getLocale(player)
    }

    fun getLocale(profile: PlayerProfile): String {
        return getLocale(profile.uuid)
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

        return getProfileLanguage(plugin.playerDataManager.getPlayerProfile(uuid))
    }

    fun getProfileLanguage(commandSender: CommandSender?): String? {
        return when (commandSender) {
            null -> null
            is Player -> getProfileLanguage(commandSender.uniqueId)
            else -> defaultLanguage
        }
    }

    fun getRawMessageString(message: Message, language: String?, fallbackOnDefaultLanguage: Boolean): String? {
        val languageMessageMap =
            languages[language]!!
        val miniMessageString = languageMessageMap[message]
            ?: return if (fallbackOnDefaultLanguage) getRawMessageString(message, defaultLanguage, false) else null

        return miniMessageString
    }

    fun getRawMessageString(message: Message, language: String?): String? {
        return getRawMessageString(message, language, true)
    }

    private fun getMessage(message: Message, language: String?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component): Component {
        val miniMessageString = checkNotNull(getRawMessageString(message, language, fallbackOnDefaultLanguage))

        var result = miniMessage.deserialize(miniMessageString)

        for (i in arguments.indices) {
            result = result.replaceText(TextReplacementConfig.builder().matchLiteral("{$i}")
                .replacement { _, _ -> arguments[i] }
                .build())
        }

        return result
    }

    fun getDefaultMessage(message: Message, vararg arguments: Component?): Component {
        return getMessage(message, null as String?, arguments)
    }

    fun getDefaultMessage(message: Message, vararg arguments: Any?): Component {
        return getMessage(message, null as String?, arguments)
    }

    fun getMessage(message: Message, language: String?, vararg arguments: Component?): Component {
        return getMessage(message, language, true, *arguments)
    }

    fun getMessage(message: Message, language: String?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?): Component {
        return getMessage(message, language, fallbackOnDefaultLanguage, *toComponents(*arguments))
    }

    fun getMessage(message: Message, language: String?, vararg arguments: Any?): Component {
        return getMessage(message, language, true, *arguments)
    }

    fun getMessage(message: Message, commandSender: CommandSender?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component?): Component {
        return getMessage(message, getLanguage(commandSender), fallbackOnDefaultLanguage, *arguments)
    }

    fun getMessage(message: Message, commandSender: CommandSender?, vararg arguments: Component?): Component {
        return getMessage(message, commandSender, true, *arguments)
    }

    fun getMessage(message: Message, commandSender: CommandSender?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?): Component {
        return getMessage(message, commandSender, fallbackOnDefaultLanguage, *toComponents(*arguments))
    }

    fun getMessage(message: Message, commandSender: CommandSender?, vararg arguments: Any?): Component {
        return getMessage(message, commandSender, true, *arguments)
    }

    fun getMessage(message: Message, uuid: UUID?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component?): Component {
        return getMessage(message, getLanguage(uuid), fallbackOnDefaultLanguage, *arguments)
    }

    fun getMessage(message: Message, uuid: UUID?, vararg arguments: Component?): Component {
        return getMessage(message, uuid, true, *arguments)
    }

    fun getMessage(message: Message, uuid: UUID?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?): Component {
        return getMessage(message, uuid, fallbackOnDefaultLanguage, *toComponents(*arguments))
    }

    fun getMessage(message: Message, uuid: UUID?, vararg arguments: Any?): Component {
        return getMessage(message, uuid, true, *arguments)
    }

    fun getMessage(message: Message, playerProfile: PlayerProfile, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component?): Component {
        return getMessage(message, getLanguage(playerProfile), fallbackOnDefaultLanguage, *arguments)
    }

    fun getMessage(message: Message, playerProfile: PlayerProfile, vararg arguments: Component?): Component {
        return getMessage(message, playerProfile, true, *arguments)
    }

    fun getMessage(message: Message, playerProfile: PlayerProfile, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?): Component {
        return getMessage(message, playerProfile, fallbackOnDefaultLanguage, *toComponents(*arguments))
    }

    fun getMessage(message: Message, playerProfile: PlayerProfile, vararg arguments: Any?): Component {
        return getMessage(message, playerProfile, true, *arguments)
    }

    // rolyPolyVole start

    fun getMessages(message: Message, commandSender: CommandSender?, vararg arguments: Any?): Array<Component> {
        val rawMessage = miniMessage.serialize(getMessage(message, commandSender, *arguments))

        return rawMessage.split("\\n|<br>".toRegex())
            .asSequence()
            .filter { it.isNotEmpty() }
            .map(miniMessage::deserialize)
            .toList()
            .toTypedArray()
    }

    // rolyPolyVole end

    fun sendMessage(message: Message, commandSender: CommandSender, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component?) {
        commandSender.sendMessage(getMessage(
            message, getLanguage(commandSender), fallbackOnDefaultLanguage, *arguments))
    }

    fun sendMessage(message: Message, commandSender: CommandSender, vararg arguments: Component?) {
        commandSender.sendMessage(getMessage(
            message, commandSender, true, *arguments))
    }

    fun sendMessage(message: Message, commandSender: CommandSender, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?) {
        commandSender.sendMessage(getMessage(
            message, commandSender, fallbackOnDefaultLanguage, *toComponents(*arguments)))
    }

    fun sendMessage(message: Message, commandSender: CommandSender, vararg arguments: Any?) {
        commandSender.sendMessage(getMessage(
            message, commandSender, true, *arguments))
    }

    fun sendMessage(message: Message, uuid: UUID?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component?) {
        requireNonNull(Bukkit.getPlayer(uuid!!))?.sendMessage(
            getMessage(message, getLanguage(uuid), fallbackOnDefaultLanguage, *arguments))
    }

    fun sendMessage(message: Message, uuid: UUID?, vararg arguments: Component?) {
        requireNonNull(Bukkit.getPlayer(uuid!!))?.sendMessage(
            getMessage(message, uuid, true, *arguments))
    }

    fun sendMessage(message: Message, uuid: UUID?, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?) {
        requireNonNull(Bukkit.getPlayer(uuid!!))?.sendMessage(
            getMessage(message, uuid, fallbackOnDefaultLanguage, *toComponents(*arguments)))
    }

    fun sendMessage(message: Message, uuid: UUID?, vararg arguments: Any?) {
        requireNonNull(Bukkit.getPlayer(uuid!!))?.sendMessage(
            getMessage(message, uuid, true, *arguments))
    }

    fun sendMessage(message: Message, playerProfile: PlayerProfile, fallbackOnDefaultLanguage: Boolean, vararg arguments: Component?) {
        requireNonNull(Bukkit.getPlayer(playerProfile.uuid))?.sendMessage(
            getMessage(message, getLanguage(playerProfile), fallbackOnDefaultLanguage, *arguments))
    }

    fun sendMessage(message: Message, playerProfile: PlayerProfile, vararg arguments: Component?) {
        requireNonNull(Bukkit.getPlayer(playerProfile.uuid))?.sendMessage(
            getMessage(message, playerProfile, true, *arguments))
    }

    fun sendMessage(message: Message, playerProfile: PlayerProfile, fallbackOnDefaultLanguage: Boolean, vararg arguments: Any?) {
        requireNonNull(Bukkit.getPlayer(playerProfile.uuid))?.sendMessage(
            getMessage(message, playerProfile, fallbackOnDefaultLanguage, *toComponents(*arguments)))
    }

    fun sendMessage(message: Message, playerProfile: PlayerProfile, vararg arguments: Any?) {
        requireNonNull(Bukkit.getPlayer(playerProfile.uuid))?.sendMessage(
            getMessage(message, playerProfile, true, *arguments))
    }

    fun toComponents(vararg objects: Any?): Array<Component> {
        return objects.asSequence()
            .filterNotNull()
            .map { toComponent(it) }
            .toList()
            .toTypedArray()
    }

    fun toComponent(obj: Any): Component {
        if (obj is Component) {
            return obj
        }

        return Component.text(obj.toString())
    }
}