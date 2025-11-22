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
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
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
        val project = p0.project ?: return
        val virtualFile = p0.dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val filename = virtualFile.name

        // Show file save dialog
        val descriptor = FileSaverDescriptor("Save Compressed File", "Choose location for compressed file", "zst")
        val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)

        // when virtualFile.parent is null dialog will use some default directory
        val wrapper = dialog.save(virtualFile.parent, "$filename.zst") ?: return
        val outputPath = wrapper.file.toPath()

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
                        Compressor.compress(virtualFile, outputPath) { progress ->
                            reporter.fraction(progress.toDouble())
                        }
                    }
                }

                showCompletionNotification("Compressed $filename in ${time}ms", outputPath)
            }
        }
    }

    private fun showCompletionNotification(text: String, filePath: Path) {
        val notification = Notification("notifications.debug", text, NotificationType.INFORMATION)
        notification.addAction(NotificationAction.createSimple(RevealFileAction.getActionName()) {
            RevealFileAction.openFile(filePath)
        })
        Notifications.Bus.notify(notification)
    }


}
