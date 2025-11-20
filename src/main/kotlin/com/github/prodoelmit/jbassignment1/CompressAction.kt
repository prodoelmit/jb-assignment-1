package com.github.prodoelmit.jbassignment1

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CompressAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        Notifications.Bus.notify(Notification("notifications.debug", "Some content", NotificationType.INFORMATION))
    }
}
