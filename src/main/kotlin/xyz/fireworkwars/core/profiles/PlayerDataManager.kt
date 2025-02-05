package xyz.fireworkwars.core.profiles

import com.google.gson.GsonBuilder
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract
import xyz.fireworkwars.core.FireworkWarsCorePlugin
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

@Suppress("unused")
class PlayerDataManager(private val plugin: FireworkWarsCorePlugin) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val playerDataFolderName = "player-data"
    private val playerDataFolderPath: String = plugin.dataFolder.path + File.separator + playerDataFolderName
    private val playerDataFolder = File(playerDataFolderPath)

    private val playerData: MutableMap<UUID, PlayerProfile> = HashMap()

    val size: Int
        get() = playerData.size

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        if (!playerDataFolder.exists()) {
            return
        }

        val playerDataFiles = checkNotNull(playerDataFolder.listFiles())

        for (playerDataFile in playerDataFiles) {
            val fileName = playerDataFile.name
            val playerUuidString = fileName.split("\\.".toRegex(), limit = 2).toTypedArray()[0]

            val playerUuid = UUID.fromString(playerUuidString)
            val profile: PlayerProfile

            try {
                val reader = FileReader(playerDataFile)

                profile = gson.fromJson(reader, PlayerProfile::class.java)

                reader.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
                continue
            }

            playerData[playerUuid] = profile
        }
    }

    fun save() {
        playerDataFolder.mkdir()

        for ((uuid, profile) in playerData) {
            val file = File(playerDataFolderPath + File.separator + uuid.toString() + ".json")

            try {
                file.createNewFile()
                val writer = FileWriter(file)

                val json = gson.toJson(profile)
                writer.write(json)

                writer.flush()
                writer.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
    }

    fun getAllProfiles(): Collection<PlayerProfile> {
        return playerData.values
    }

    @Contract("_, true -> !null")
    fun getPlayerProfile(uuid: UUID, createNewProfile: Boolean): PlayerProfile? {
        return if (createNewProfile) {
            playerData.computeIfAbsent(uuid) {
                PlayerProfile.createDefault(uuid, plugin)
            }
        } else {
            playerData[uuid]
        }
    }

    fun getPlayerProfile(uuid: UUID): PlayerProfile {
        return getPlayerProfile(uuid, true)!!
    }

    @Contract("_, true -> !null")
    fun getPlayerProfile(player: Player, createNewProfile: Boolean): PlayerProfile? {
        return getPlayerProfile(player.uniqueId, createNewProfile)
    }

    fun getPlayerProfile(player: Player): PlayerProfile {
        return getPlayerProfile(player, true)!!
    }

    @Contract("_, true -> !null")
    fun getPlayerProfile(offlinePlayer: OfflinePlayer, createNewProfile: Boolean): PlayerProfile? {
        return getPlayerProfile(offlinePlayer.uniqueId, createNewProfile)
    }

    fun getPlayerProfile(offlinePlayer: OfflinePlayer): PlayerProfile {
        return getPlayerProfile(offlinePlayer, true)!!
    }
}