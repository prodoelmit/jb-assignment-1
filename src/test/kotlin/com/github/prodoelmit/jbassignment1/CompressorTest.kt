package com.github.prodoelmit.jbassignment1

import com.github.luben.zstd.ZstdInputStream
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.deleteIfExists

class CompressorTest : BasePlatformTestCase() {

    fun testCompressFile() = runBlocking {
        val content = "Hello, World! This is test content for compression."
        val inputFile = myFixture.configureByText("test.txt", content)
        val outputPath = Files.createTempFile("compressed", ".zst")

        try {
            Compressor.compress(inputFile.virtualFile, outputPath) { }

            assertTrue("Output file should exist", Files.exists(outputPath))
            assertTrue("Output file should not be empty", Files.size(outputPath) > 0)
        } finally {
            outputPath.deleteIfExists()
        }
    }

    fun testCompressFileEmpty() = runBlocking {
        val inputFile = myFixture.configureByText("empty.txt", "")
        val outputPath = Files.createTempFile("compressed", ".zst")

        try {
            Compressor.compress(inputFile.virtualFile, outputPath) { }

            assertTrue("Output file should exist", Files.exists(outputPath))
        } finally {
            outputPath.deleteIfExists()
        }
    }

    fun testProgressReporting() = runBlocking {
        val content = "A".repeat(10000)
        val inputFile = myFixture.configureByText("progress.txt", content)
        val outputPath = Files.createTempFile("compressed", ".zst")

        val progressValues = mutableListOf<Float>()

        try {
            Compressor.compress(inputFile.virtualFile, outputPath) { progress ->
                progressValues.add(progress)
            }

            assertTrue("Should have progress updates", progressValues.isNotEmpty())
            assertEquals("Final progress should be 1.0", 1f, progressValues.last())

            // Verify progress is monotonically increasing
            for (i in 1 until progressValues.size) {
                assertTrue(
                    "Progress should increase: ${progressValues[i-1]} -> ${progressValues[i]}",
                    progressValues[i] >= progressValues[i-1]
                )
            }
        } finally {
            outputPath.deleteIfExists()
        }
    }

    fun testCompressedOutputIsDecompressible() = runBlocking {
        val content = "Test content that should be recoverable after compression and decompression."
        val inputFile = myFixture.configureByText("decompressible.txt", content)
        val outputPath = Files.createTempFile("compressed", ".zst")

        try {
            Compressor.compress(inputFile.virtualFile, outputPath) { }

            // Decompress and verify content
            val decompressed = ZstdInputStream(Files.newInputStream(outputPath)).use { zstdIn ->
                zstdIn.readBytes().decodeToString()
            }

            assertEquals("Decompressed content should match original", content, decompressed)
        } finally {
            outputPath.deleteIfExists()
        }
    }

    fun testLargeFile() = runBlocking {
        // 1MB of data
        val content = "X".repeat(1024 * 1024)
        val inputFile = myFixture.configureByText("large.txt", content)
        val outputPath = Files.createTempFile("compressed", ".zst")

        try {
            Compressor.compress(inputFile.virtualFile, outputPath) { }

            assertTrue("Output file should exist", Files.exists(outputPath))

            // Verify it's actually compressed (should be smaller due to repetitive content)
            assertTrue(
                "Compressed size should be smaller than original",
                Files.size(outputPath) < content.length
            )

            // Verify decompression
            val decompressed = ZstdInputStream(Files.newInputStream(outputPath)).use { zstdIn ->
                zstdIn.readBytes().decodeToString()
            }
            assertEquals("Decompressed content should match original", content, decompressed)
        } finally {
            outputPath.deleteIfExists()
        }
    }

    fun testCompressWithPath() = runBlocking {
        val content = "Path-based compression test"
        val inputPath = Files.createTempFile("input", ".txt")
        val outputPath = Files.createTempFile("output", ".zst")

        try {
            Files.writeString(inputPath, content)


            val result = Compressor.compressFile(inputPath, outputPath) { }

            assertEquals("Should return output path", outputPath, result)
            assertTrue("Output file should exist", Files.exists(outputPath))

            // Verify decompression
            val decompressed = ZstdInputStream(Files.newInputStream(outputPath)).use { zstdIn ->
                zstdIn.readBytes().decodeToString()
            }
            assertEquals("Decompressed content should match original", content, decompressed)
        } finally {
            inputPath.deleteIfExists()
            outputPath.deleteIfExists()
        }
    }
}
