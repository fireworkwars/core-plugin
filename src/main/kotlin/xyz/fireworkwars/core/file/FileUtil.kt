package xyz.fireworkwars.core.file

import java.io.IOException
import java.util.jar.JarFile
import java.util.stream.Collectors

class FileUtil {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun getFilePathsInFolder(folderPath: String): List<String> {
            val classLoader = FileUtil::class.java.classLoader

            val jarURL = classLoader.getResource(folderPath) ?: return emptyList()

            val jarPath = jarURL.path
            val exclamationMarkIndex = jarPath.indexOf("!")

            val jarPathPrefix = "file:"
            val jarFilePath = jarPath.substring(jarPathPrefix.length, exclamationMarkIndex)

            val jar = JarFile(jarFilePath)
            return jar.stream()
                .map { it.name }
                .filter { it.startsWith(folderPath) && it != folderPath }
                .map { it.substring(folderPath.length) }
                .filter { "/" != it }
                .map { folderPath + it }
                .toList()
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getAllFilePathsRecursively(folderPath: String): List<String> {
            val paths: MutableList<String> = ArrayList()

            for (resourceFilePath in getFilePathsInFolder(folderPath)) {
                val subFiles = getAllFilePathsRecursively(resourceFilePath)

                if (subFiles.isEmpty()) {
                    paths.add(resourceFilePath)
                } else {
                    paths.addAll(subFiles)
                }
            }

            return paths
        }

        @JvmStatic
        fun getFileNamesInFolder(folderPath: String): List<String> {
            val classLoader = FileUtil::class.java.classLoader

            try {
                val jarURL = classLoader.getResource(folderPath) ?: return emptyList()

                val jarPath = jarURL.path
                val exclamationMarkIndex = jarPath.indexOf("!")

                val jarPathPrefix = "file:"
                val jarFilePath = jarPath.substring(jarPathPrefix.length, exclamationMarkIndex)

                val jar = JarFile(jarFilePath)
                return jar.stream()
                    .map { it.name }
                    .filter { it.startsWith(folderPath) && it != folderPath }
                    .map { it.substring(folderPath.length) }
                    .collect(Collectors.toList())
            } catch (exception: IOException) {
                exception.printStackTrace()
                return emptyList()
            }
        }
    }
}