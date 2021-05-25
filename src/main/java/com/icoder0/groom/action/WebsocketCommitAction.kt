package com.icoder0.groom.action

import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import javax.swing.JButton
import javax.swing.JComponent

/**
 * @author bofa1ex
 * @since  2021/5/25
 */
class WebsocketCommitAction: AnAction(), CustomComponentAction {

    override fun update(e: AnActionEvent) {
        val isCommitEnable = e.getRequiredData(WebsocketClientView.IS_COMMIT_ENABLE_KEY)
        val isCommitVisible = e.getRequiredData(WebsocketClientView.IS_COMMIT_VISIBLE_KEY)
        e.presentation.isEnabled = isCommitEnable
        e.presentation.isVisible = isCommitVisible
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent) {
        val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        websocketClientView?.fireCommit()
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return JButton(AllIcons.Actions.Commit).apply { isEnabled = false }
    }
}