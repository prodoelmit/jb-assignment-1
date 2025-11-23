package com.github.prodoelmit.jbassignment1

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import java.nio.file.Path
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@Suppress("UnstableApiUsage")
class CompressAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        val project = p0.project
        if (project == null) {
            showErrorNotification("No project available")
            return
        }

        val virtualFile = p0.dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)
        if (virtualFile == null) {
            showErrorNotification("No file selected")
            return
        }

        val filename = virtualFile.name

        // Show compression dialog
        val dialog = CompressionDialog(project, virtualFile)
        if (!dialog.showAndGet()) return  // User cancelled - silent return is expected
        val outputPath = dialog.getOutputPath()
        val compressionLevel = dialog.getCompressionLevel()

        currentThreadCoroutineScope().launch {
            withBackgroundProgress(
                project = project,
                title = "Compressing $filename",
            ) {
                // Flush unsaved changes before compression
                // if there's real file behind this virtual one
                if (virtualFile.toNioPathOrNull() != null) {
                    writeAction {
                        FileDocumentManager.getInstance().saveAllDocuments()
                    }
                }

                val time = measureTimeMillis {
                    reportRawProgress { reporter ->
                        Compressor.compress(virtualFile, outputPath, compressionLevel) { progress ->
                            reporter.fraction(progress.toDouble())
                        }
                    }
                }

                showCompletionNotification("Compressed $filename in ${time}ms", outputPath)
            }
        }
    }

    private fun showCompletionNotification(text: String, filePath: Path) {
        val notification = Notification("notifications.compressor", text, NotificationType.INFORMATION)
        notification.addAction(NotificationAction.createSimple(RevealFileAction.getActionName()) {
            RevealFileAction.openFile(filePath)
        })
        Notifications.Bus.notify(notification)
    }

    private fun showErrorNotification(text: String) {
        val notification = Notification("notifications.compressor", text, NotificationType.ERROR)
        Notifications.Bus.notify(notification)
    }


}
