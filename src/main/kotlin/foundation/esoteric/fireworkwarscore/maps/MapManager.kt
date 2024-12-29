package foundation.esoteric.fireworkwarscore.maps

import foundation.esoteric.fireworkwarscore.FireworkWarsCorePlugin
import foundation.esoteric.fireworkwarscore.file.FileManager
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import javax.annotation.ParametersAreNonnullByDefault

class MapManager(private val plugin: FireworkWarsCorePlugin) {
    private val fileManager: FileManager = FileManager(plugin)

    private val mapsDirectory: Path = Paths.get("plugins/FireworkWarsPlugin/maps")
    private val rootDirectory: Path = Paths.get("").toAbsolutePath()

    private val defaultWorld: String = "world"
    private val barracksWorld: String = "barracks"
    private val townWorld: String = "town"

    @Throws(IOException::class)
    fun saveMaps() {
        if (File(barracksWorld).exists() && File(townWorld).exists()) {
            plugin.logger.info("Worlds already exist, skipping saving procedure.")
        } else {
            plugin.logger.info("Deleting default world...")

            FileUtils.deleteDirectory(File(defaultWorld))

            plugin.logger.info("Finished deleting default world.")
            plugin.logger.info("Moving barracks map...")

            fileManager.saveFolderToResources("maps/barracks")
            plugin.saveResource("maps/barracks/level.dat", true)

            plugin.logger.info("Finished moving barracks map.")
            plugin.logger.info("Moving town map...")

            fileManager.saveFolderToResources("maps/town")
            plugin.saveResource("maps/town/level.dat", true)

            plugin.logger.info("Finished moving town map.")
            plugin.logger.info("Moving lobby map...")

            fileManager.saveFolderToResources("maps/world")
            plugin.saveResource("maps/world/level.dat", true)

            plugin.logger.info("Finished moving lobby map.")
            plugin.logger.info("Moving maps to root server directory.")

            moveMapsToRoot()

            plugin.logger.info("Successfully moved maps to root server directory.")
            plugin.logger.info("Successfully saved Firework Wars maps.")
        }
    }

    @Throws(IOException::class)
    private fun moveMapsToRoot() {
        moveFolderToRoot(mapsDirectory)

        File("world/playerdata").mkdir()
    }

    @Throws(IOException::class)
    private fun moveFolderToRoot(path: Path) {
        if (Files.exists(path)) {
            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                @ParametersAreNonnullByDefault
                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val relativePath: Path = mapsDirectory.relativize(file)
                    val targetPath: Path = rootDirectory.resolve(relativePath)

                    Files.createDirectories(targetPath.parent)
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING)

                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }
            })

            plugin.logger.info("All files moved successfully!")
        } else {
            plugin.logger.info("Directory does not exist.")
        }
    }
}