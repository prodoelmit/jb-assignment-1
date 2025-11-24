package com.github.prodoelmit.jbassignment1

import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

object NativeBinaryLoader {
    private var cachedBinaryPath: File? = null
    private var cachedBinaryHash: ByteArray? = null

    @Synchronized
    fun getZstdBinary(): File {
        cachedBinaryPath?.let { cached ->
            if (cached.exists() && cached.canExecute()) {
                val currentHash = computeHash(cached)
                if (currentHash.contentEquals(cachedBinaryHash)) {
                    return cached
                }
                // Hash mismatch - re-extract
            }
        }

        val os = detectOs()
        val arch = detectArchitecture()

        val binaryName = if (os == "win") "zstd.exe" else "zstd"
        val resourcePath = "/native/$os/$arch/$binaryName"

        val stream = javaClass.getResourceAsStream(resourcePath)
            ?: error(CompressorBundle.message("error.binary.not.found", resourcePath, os, arch))

        val tempDir = Files.createTempDirectory("zstd-native").toFile()
        tempDir.deleteOnExit()

        val tempFile = File(tempDir, binaryName)
        tempFile.deleteOnExit()

        stream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (os != "win") {
            tempFile.setExecutable(true)
        }

        cachedBinaryPath = tempFile
        cachedBinaryHash = computeHash(tempFile)
        return tempFile
    }

    private fun computeHash(file: File): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest()
    }

    private fun detectOs(): String {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("linux") -> "linux"
            osName.contains("mac") || osName.contains("darwin") -> "darwin"
            osName.contains("win") -> "win"
            else -> error(CompressorBundle.message("error.unsupported.os", osName))
        }
    }

    private fun detectArchitecture(): String {
        val osArch = System.getProperty("os.arch")
        return when (osArch) {
            "amd64", "x86_64" -> "x86_64"
            "aarch64", "arm64" -> "aarch64"
            else -> error(CompressorBundle.message("error.unsupported.arch", osArch))
        }
    }
}
