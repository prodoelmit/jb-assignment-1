package com.github.prodoelmit.jbassignment1

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import kotlinx.coroutines.launch
import kotlin.io.path.Path

@Suppress("UnstableApiUsage")
class CompressAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        val project = p0.project ?: return
        val virtualFile = p0.dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val filename = virtualFile.name
        val outputPath = Path(virtualFile.path + ".zst")

        currentThreadCoroutineScope().launch {
            withBackgroundProgress(
                project = project,
                title = "Compressing $filename",
            ) {
                reportRawProgress { reporter ->
                    Compressor.compress(virtualFile, outputPath) { progress ->
                        reporter.fraction(progress.toDouble())
                    }
                }
                debugBalloon("Compressed $filename")
            }
        }
    }

    fun debugBalloon(text: String) {
        Notifications.Bus.notify(Notification("notifications.debug", text, NotificationType.INFORMATION))
    }


}
