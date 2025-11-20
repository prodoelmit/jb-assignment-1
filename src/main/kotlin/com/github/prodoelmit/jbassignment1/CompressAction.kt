package com.github.prodoelmit.jbassignment1

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.progress.sleepCancellable
import kotlinx.coroutines.launch

@Suppress("UnstableApiUsage")
class CompressAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        checkNotNull(p0.project)
        val project = p0.project!!
        val filename = p0.dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)?.name ?: "the file"
        debugBalloon("1")
        currentThreadCoroutineScope().launch {
            withBackgroundProgress(
                project = project,
                title = "Compressing ${filename}",
            ) {
                reportSequentialProgress { reporter ->
                    reporter.indeterminateStep("Compressing ${filename}")
                    reporter.nextStep(endFraction = 20, "Booping") {
                        sleepCancellable(3000)
                        debugBalloon("Booping done")
                    }
                    reporter.nextStep(endFraction = 60, "Flipflopping") {
                        sleepCancellable(3000)
                        debugBalloon("Flipflopping done")
                    }
                    reporter.nextStep(endFraction = 100, "Finishing") {
                        sleepCancellable(3000)
                        debugBalloon("Finished")
                    }
                }
            }
        }
    }

    fun debugBalloon(text: String) {
        Notifications.Bus.notify(Notification("notifications.debug", text, NotificationType.INFORMATION))
    }

    class CompressTask(project: Project): Task.Backgroundable(project, "Compressing", true) {
        override fun run(p0: ProgressIndicator) {
            p0.text = "Compressing"


        }
    }
}
