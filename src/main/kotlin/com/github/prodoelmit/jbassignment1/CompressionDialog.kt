package com.github.prodoelmit.jbassignment1

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.nio.file.Path
import javax.swing.JComponent
import kotlin.io.path.Path

class CompressionDialog(
    private val project: Project,
    sourceFile: VirtualFile
) : DialogWrapper(project) {

    private var outputPath: String = if (sourceFile.parent != null) {
        "${sourceFile.parent.path}/${sourceFile.name}.zst"
    } else {
        "${sourceFile.name}.zst"
    }
    private var compressionLevel: Int = 19

    init {
        title = "Save Compressed File"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Output file:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                        .withTitle("Select Output File"),
                    project
                ).bindText(::outputPath)
                 .resizableColumn()
            }
            row("Compression level:") {
                spinner(1..19, 1)
                    .bindIntValue(::compressionLevel)
            }
        }
    }

    fun getOutputPath(): Path = Path(outputPath)

    fun getCompressionLevel(): Int = compressionLevel
}
