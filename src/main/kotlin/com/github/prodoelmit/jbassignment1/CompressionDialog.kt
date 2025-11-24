package com.github.prodoelmit.jbassignment1

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.FieldPanel
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.panel
import java.io.File
import java.nio.file.Path
import javax.swing.JComponent
import kotlin.io.path.Path

class CompressionDialog(
    private val project: Project,
    sourceFile: VirtualFile
) : DialogWrapper(project) {

    private val defaultFileName = "${sourceFile.name}.zst"
    private val pathPanel = FieldPanel(
        CompressorBundle.message("dialog.output.file"),
        null,
        { browse() },
        null
    )
    private var compressionLevel: Int = 19

    init {
        title = CompressorBundle.message("dialog.save.title")
        val outputPath = if (sourceFile.parent != null) {
            "${sourceFile.parent.path}/$defaultFileName"
        } else {
            defaultFileName
        }
        pathPanel.text = FileUtil.toSystemDependentName(outputPath)
        init()
    }

    private fun browse() {
        val descriptor = FileChooserDescriptorFactory.singleFileOrDir().apply {
            title = CompressorBundle.message("dialog.select.output.title")
            description = CompressorBundle.message("dialog.select.output.description")
            isHideIgnored = false
        }

        val initialFile = pathPanel.text?.let { path ->
            val file = File(path)
            LocalFileSystem.getInstance().findFileByIoFile(file)
                ?: file.parentFile?.let { LocalFileSystem.getInstance().findFileByIoFile(it) }
        }

        FileChooser.chooseFile(descriptor, project, pathPanel.textField, initialFile) { file ->
            val path = if (file.isDirectory) "${file.path}/$defaultFileName" else file.path
            pathPanel.text = FileUtil.toSystemDependentName(path)
        }
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                cell(pathPanel).resizableColumn()
            }
            row(CompressorBundle.message("dialog.compression.level")) {
                spinner(1..19, 1)
                    .bindIntValue(::compressionLevel)
            }
        }
    }

    fun getOutputPath(): Path = Path(FileUtil.toSystemIndependentName(pathPanel.text))

    fun getCompressionLevel(): Int = compressionLevel
}
