package xyz.fireworkwars.core.file

import xyz.fireworkwars.core.FireworkWarsCorePlugin
import java.io.File
import java.io.IOException

@Suppress("unused")
class FileManager(private val plugin: FireworkWarsCorePlugin) {
    @JvmOverloads
    fun saveFolderToResources(folderPath: String, replace: Boolean = true): File? {
        try {
            FileUtil.getAllFilePathsRecursively(folderPath)
                .forEach { plugin.saveResource(it, replace) }

            return File(plugin.dataPath.toString() + File.separator + folderPath)
        } catch (exception: IOException) {
            exception.printStackTrace()
            return null
        }
    }
}