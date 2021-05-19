package com.icoder0.groom.component

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.Alarm
import com.sun.istack.Nullable
import icons.GroomIcons


/**
 * @author bofa1ex
 * @since  2021/5/17
 */
class NotificationManager {
    companion object {
        private val NOTIFICATION_GROUP = NotificationGroup("Groom Notification Group", NotificationDisplayType.BALLOON, true, null, GroomIcons.Logo32x)

        fun notify(@Nullable project: Project?, type: NotificationType, content: String?) {
            notify(project, type, content, 2000)
        }

        fun notify(@Nullable project: Project?, type: NotificationType, content: String?, delayMills: Int) {
            with(NOTIFICATION_GROUP.createNotification(content!!, type)) {
                notify(project)
                Alarm(project ?: ApplicationManager.getApplication()).apply {
                    addRequest({ expire(); Disposer.dispose(this) }, delayMills)
                }
            }
        }
    }
}