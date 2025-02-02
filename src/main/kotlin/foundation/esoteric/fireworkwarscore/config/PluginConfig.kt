package foundation.esoteric.fireworkwarscore.config

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin

@Suppress("unused", "MemberVisibilityCanBePrivate")
class PluginConfig(plugin: FireworkWarsCorePlugin) {
    val updateMaps: Boolean = plugin.config.getBoolean("update-maps-on-start")
    val defaultLanguage: String = plugin.config.getString("default-language")!!
    val serverIp: String = plugin.config.getString("server-ip")!!
    val discordInvite: String = plugin.config.getString("discord-invite")!!
}