package com.icoder0.groom.component

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.sun.istack.Nullable
import icons.GroomIcons


/**
 * @author bofa1ex
 * @since  2021/5/17
 */
class NotificationManager {
    companion object {
        private val NOTIFICATION_GROUP = NotificationGroup("Custom Notification Group", NotificationDisplayType.BALLOON, true, null, GroomIcons.Logo32x)

        fun notifyError(@Nullable project: Project?, content: String?) {
            NOTIFICATION_GROUP.createNotification(content!!, NotificationType.ERROR)
                    .notify(project)
        }

        fun notifyInfo(@Nullable project: Project?, content: String?) {
            NOTIFICATION_GROUP.createNotification(content!!, NotificationType.INFORMATION)
                    .notify(project)
        }
    }
}