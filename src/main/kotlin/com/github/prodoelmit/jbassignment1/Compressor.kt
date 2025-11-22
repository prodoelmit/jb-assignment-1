package com.github.prodoelmit.jbassignment1

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

object Compressor {
    private const val BUFFER_SIZE = 65536 // 64KB chunks

    suspend fun compress(input: VirtualFile, outputPath: Path, reportProgress: (Float) -> Unit) {
        val inputPath = input.toNioPathOrNull()
        if (inputPath != null) {
            compressFile(Path(input.path), outputPath, reportProgress)
        } else {
            // Fall back to VirtualFile stream for non-local files
            val totalSize = input.length
            withContext(Dispatchers.IO) {
                input.inputStream.use { inputStream ->
                    compressStream(inputStream, outputPath, totalSize, reportProgress)
                }
            }
        }
    }

    suspend fun compressFile(input: Path, outputPath: Path, reportProgress: (Float) -> Unit): Path {
        val totalSize = Files.size(input)
        withContext(Dispatchers.IO) {
            Files.newInputStream(input).use { inputStream ->
                compressStream(inputStream, outputPath, totalSize, reportProgress)
            }
        }
        return outputPath
    }

    private suspend fun compressStream(
        inputStream: InputStream,
        outputPath: Path,
        totalSize: Long,
        reportProgress: (Float) -> Unit
    ) {
        val zstdBinary = NativeBinaryLoader.getZstdBinary()

        val process = ProcessBuilder(
            zstdBinary.absolutePath,
            "-19",  // compression level
            "-o", outputPath.toString()
            // reads from stdin by default when no input file specified
        )
            .redirectErrorStream(true)
            .start()

        try {
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Long = 0

            process.outputStream.use { processInput ->
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    coroutineContext.ensureActive()
                    processInput.write(buffer, 0, len)
                    bytesRead += len
                    if (totalSize > 0) {
                        reportProgress(bytesRead.toFloat() / totalSize)
                    }
                }
            }

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val errorOutput = process.inputStream.bufferedReader().readText()
                throw RuntimeException("zstd compression failed with exit code $exitCode: $errorOutput")
            }

            reportProgress(1f)
        } catch (e: CancellationException) {
            println("Cleaning up due to cancellation")
            process.destroyForcibly()
            outputPath.deleteIfExists()
            throw e
        } catch (e: Exception) {
            process.destroyForcibly()
            outputPath.deleteIfExists()
            throw e
        }
    }
}
