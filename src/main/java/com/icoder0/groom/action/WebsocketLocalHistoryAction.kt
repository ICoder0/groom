package com.icoder0.groom.action

import com.icoder0.groom.util.IdeUtils
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageType

/**
 * @author bofa1ex
 * @since  2021/5/21
 */
class WebsocketLocalHistoryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.text = "Show History"
        e.presentation.icon = AllIcons.Actions.SearchWithHistory
        IdeUtils.notify("Unsupported feature", MessageType.INFO)
    }
}