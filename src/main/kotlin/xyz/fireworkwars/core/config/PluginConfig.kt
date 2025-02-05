package xyz.fireworkwars.core.config

import xyz.fireworkwars.core.FireworkWarsCorePlugin

@Suppress("unused", "MemberVisibilityCanBePrivate")
class PluginConfig(plugin: FireworkWarsCorePlugin) {
    val defaultLanguage: String = plugin.config.getString("default-language")!!
    val serverIp: String = plugin.config.getString("server-ip")!!
    val discordInvite: String = plugin.config.getString("discord-invite")!!
}