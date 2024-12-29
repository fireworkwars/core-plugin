package foundation.esoteric.fireworkwarscore.commands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.language.Message
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetLanguageCommand(plugin: FireworkWarsCorePlugin) : CommandAPICommand("set-language") {
    private val languageManager = plugin.languageManager
    private val languages = languageManager.getLanguages()
    private val languageArgumentNodeName = "language"

    init {
        withArguments(getLanguageArgument().includeSuggestions(getLanguageSuggestions()))
        executesPlayer(this::onPlayerExecution)
        register(plugin)
    }

    private fun getLanguageArgument(): Argument<String?> {
        return CustomArgument(GreedyStringArgument(languageArgumentNodeName)) {
            val selectedLanguage = it.currentInput()

            if (!languages.contains(selectedLanguage)) {
                val errorMessage = languageManager.getMessage(
                    Message.UNKNOWN_LANGUAGE, it.sender(), selectedLanguage)

                throw CustomArgumentException.fromAdventureComponent(errorMessage)
            }

            return@CustomArgument selectedLanguage
        }
    }

    private fun getLanguageSuggestions(): ArgumentSuggestions<CommandSender> {
        return ArgumentSuggestions.strings(*languages.toTypedArray())
    }

    private fun onPlayerExecution(player: Player, arguments: CommandArguments) {
        val selectedLanguage = arguments[languageArgumentNodeName] as String?
        languageManager.setLanguage(player, selectedLanguage)

        languageManager.sendMessage(Message.SET_LANGUAGE_SUCCESSFULLY, player, selectedLanguage)
    }
}