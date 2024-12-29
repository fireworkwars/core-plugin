package foundation.esoteric.fireworkwarscore.file

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarFile

/**
 * This method loops through the subfolder of the **resources** folder specified by this `Path` and returns the `Path`s of all files stored in said subfolder.
 * @return A set of all the paths of all files stored in the subfolder specified by this `Path`.
 * @return The `Path`s of all files stored in this subfolder.
 * @throws IllegalArgumentException If this `Path` does not lead to a valid resource.
 * @see Path.saveResource
 * @see Path.saveResources
 * @author Esoteric Enderman
 */
fun Path.resourceFilePaths(): Set<Path> {
    val filePaths = mutableSetOf<Path>()

    val pathString = this.toString()
    val url = object {}.javaClass.classLoader.getResource(pathString)?.toURI()

    requireNotNull(url) { "The specified resource URL could not be found." }

    when (url.scheme) {
        "file" -> {
            val folderFile = File(url)
            folderFile.walkTopDown().forEach {
                if (it.isFile) {
                    filePaths.add(Paths.get("$this/${it.relativeTo(folderFile).path}"))
                }
            }
        }
        "jar" -> {
            try {
                val jarFileUrl = url.toURL().openConnection() as java.net.JarURLConnection

                JarFile(jarFileUrl.jarFileURL.path).use {
                    val entries = it.entries()

                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()

                        if (entry.name.startsWith(pathString) && !entry.isDirectory) {
                            filePaths.add(Paths.get(entry.name))
                        }
                    }
                }
            } catch (exception: Exception) {
                throw IllegalStateException("Failed to access JAR contents.", exception)
            }
        }
    }

    return filePaths
}

/**
 * This method saves the resource in the "resources" folder specified by this `Path` to the file specified as the `outputPath`.
 * @param outputPath The `Path` to the output file.
 * @return The newly created `File`.
 * @throws IllegalArgumentException If this `Path` does not lead to a valid resource.
 * @see Path.saveResources
 * @see Path.resourceFilePaths
 * @author Esoteric Enderman
 */
fun Path.saveResource(outputPath: Path): File {
    val resourceStream: InputStream? = object {}.javaClass.classLoader.getResourceAsStream(this.toString())
    requireNotNull(resourceStream) { "Resource '$this' could not be found." }

    Files.createDirectories(outputPath.parent)

    Files.copy(resourceStream, outputPath, StandardCopyOption.REPLACE_EXISTING)
    resourceStream.close()
    return outputPath.toFile()
}

/**
 * This method saves the resource in the "resources" folder specified by this `Path` to the file specified as the `outputFile`.
 * @param outputFile The output file.
 * @return The newly created `File`.
 * @throws IllegalArgumentException If this `Path` does not lead to a valid resource.
 * @see Path.saveResources
 * @see Path.resourceFilePaths
 * @author Esoteric Enderman
 */
fun Path.saveResource(outputFile: File): File {
    return saveResource(outputFile.toPath())
}

/**
 * This method saves all resources in a subfolder of the "resources" folder specified by this `Path` to the folder specified as the `outputFolder`.
 * @param outputFolder The folder to save the resources to.
 * @return The newly created `File` folder.
 * @throws IllegalArgumentException If this `Path` does not lead to a valid resource folder.
 * @see Path.saveResource
 * @see Path.resourceFilePaths
 * @author Esoteric Enderman
 */
fun Path.saveResources(outputFolder: Path): File {
    Files.createDirectories(outputFolder)

    this.resourceFilePaths().forEach {
        val relativePath = this.relativize(it)
        val outputPath = outputFolder.resolve(relativePath.toString())

        it.saveResource(outputPath)
    }

    return outputFolder.toFile()
}

/**
 * This method saves all resources in a subfolder of the "resources" folder specified by this `Path` to the folder specified as the `outputFolder`.
 * @param outputFolder The folder to save the resources to.
 * @return The newly created `File` folder.
 * @throws IllegalArgumentException If this `Path` does not lead to a valid resource folder.
 * @see Path.saveResource
 * @see Path.resourceFilePaths
 * @author Esoteric Enderman
 */
fun Path.saveResources(outputFolder: File): File {
    return saveResources(outputFolder.toPath())
}