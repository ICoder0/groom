package com.icoder0.groom.action

import com.icoder0.groom.ui.CompositeEditorDataKey
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.roots.ui.configuration.actions.IconWithTextAction
import com.intellij.util.ui.EmptyIcon
import icons.GroomIcons
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author bofa1ex
 * @since  2021/5/24
 */
class EditorLanguageComboBoxAction : ComboBoxAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val project = e.getData(CommonDataKeys.PROJECT)
        val selectedIcon = e.getData(CompositeEditorDataKey.SELECTED_ICON)
        val selectedName = e.getData(CompositeEditorDataKey.SELECTED_NAME)
        val isEditorVisible = e.getRequiredData(CompositeEditorDataKey.IS_EDITOR_VISIBLE_KEY)
        if (project == null || project.isDisposed || !project.isOpen) {
            presentation.isEnabled = false
            presentation.text = "Unknown"
            presentation.icon = EmptyIcon.ICON_13
        } else {
            presentation.text = selectedName
            presentation.icon = selectedIcon
            presentation.isEnabled = true
        }
        presentation.isVisible = isEditorVisible
    }

    override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
        return DefaultActionGroup(
            ComboBoxInternalAction("json", GroomIcons.fileJson),
            ComboBoxInternalAction("xml", GroomIcons.fileXml),
            ComboBoxInternalAction("html", GroomIcons.fileHtml),
            ComboBoxInternalAction("plainText", GroomIcons.fileText)
        )
    }

    inner class ComboBoxInternalAction(val name: String, val icon: Icon) : IconWithTextAction(name, "", icon) {
        override fun actionPerformed(e: AnActionEvent) {
            e.getData(CompositeEditorDataKey.COMPOSITE_EDITOR_KEY)!!.fireEditorLanguageChanged(name, icon)
        }
    }
}