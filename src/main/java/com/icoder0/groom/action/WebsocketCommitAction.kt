package com.icoder0.groom.action

import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.AnActionButton
import com.intellij.ui.ComponentUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JButtonAction
import com.intellij.util.ui.UIUtil
import javax.swing.JButton
import javax.swing.JComponent

/**
 * @author bofa1ex
 * @since  2021/5/25
 */
class WebsocketCommitAction : DumbAwareAction("", "", AllIcons.Actions.Commit), CustomComponentAction {

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button = JButton().apply {
            isFocusable = false
            font = JBUI.Fonts.toolbarFont()
            putClientProperty("ActionToolbar.smallVariant", true)
        }.also { button ->
            button.addActionListener {
                val toolbar = ComponentUtil.getParentOfType(ActionToolbar::class.java, button)
                val dataContext = toolbar?.toolbarDataContext ?: DataManager.getInstance().getDataContext(button)
                val action = this
                val event = AnActionEvent.createFromInputEvent(null, place, presentation, dataContext)
                if (ActionUtil.lastUpdateAndCheckDumb(action, event, true)) {
                    ActionUtil.performActionDumbAware(action, event)
                }
            }
        }

        updateButtonFromPresentation(button, presentation)
        return button
    }

    fun updateButtonFromPresentation(button: JButton, presentation: Presentation) {
        button.isEnabled = presentation.isEnabled
        button.isVisible = presentation.isVisible
        button.text = presentation.text
        button.icon = presentation.icon
        button.mnemonic = presentation.mnemonic
        button.displayedMnemonicIndex = presentation.displayedMnemonicIndex
        button.toolTipText = presentation.description
    }

    override fun update(e: AnActionEvent) {
        val isCommitEnable = e.getRequiredData(WebsocketClientView.IS_COMMIT_ENABLE_KEY)
        val isCommitVisible = e.getRequiredData(WebsocketClientView.IS_COMMIT_VISIBLE_KEY)
        with(e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)) {
            this?.isEnabled = isCommitEnable
            this?.isVisible = isCommitVisible
        }
    }
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent) {
        val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        websocketClientView?.fireCommit()
//        with(e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)) {
//            this?.isEnabled = isCommitEnable
//            this?.isVisible = isCommitVisible
//        }
    }
//
//    override fun update(e: AnActionEvent) {
//        val isCommitEnable = e.getRequiredData(WebsocketClientView.IS_COMMIT_ENABLE_KEY)
//        val isCommitVisible = e.getRequiredData(WebsocketClientView.IS_COMMIT_VISIBLE_KEY)
//        with(e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)) {
//            this?.isEnabled = isCommitEnable
//            this?.isVisible = isCommitVisible
//        }
//    }
//
//
//    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
//        return JButton(AllIcons.Actions.Commit).apply {
//            addActionListener {
//                presentation
//            }
//            isEnabled = false
//        }
//    }
}