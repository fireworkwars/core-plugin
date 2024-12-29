package foundation.esoteric.fireworkwarscore.file

import foundation.esoteric.fireworkwarscore.communication.BasePlugin
import java.io.File
import java.io.IOException

@Suppress("unused")
class FileManager(private val plugin: BasePlugin) {
    @JvmOverloads
    fun saveFolderToResources(folderPath: String, replace: Boolean = true): File? {
        try {
            FileUtil
                .getAllFilePathsRecursively(folderPath)
                .forEach {
                    println("Saving file to resources: $it")
                    plugin.saveResource(it, replace)
                }

            val out = File(plugin.dataPath.toString() + File.separator + folderPath)
            println("Returning: $out")
            return out
        } catch (exception: IOException) {
            exception.printStackTrace()
            return null
        }
    }
}