package com.icoder0.groom.ui

import com.icoder0.groom.component.EditorManager
import com.icoder0.groom.component.EditorManager.EditorManagerInternal.disposePanel
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.ui.layout.*
import com.intellij.util.ui.JBUI.Panels.simplePanel
import icons.GroomIcons
import javax.swing.Icon

/**
 * @author bofa1ex
 * @since  2021/3/12
 */
class CompositeEditorView(project: Project, toolWindow: ToolWindowEx) : GroomToolWindowPanel(project, toolWindow),
    CompositeEditorDataKey {

    override fun initUI(): CompositeEditorView {
        with(ActionManager.getInstance()) {
            setContent(panel {
                row {
                    cell(isFullWidth = true) {
                        editorWrapperPanel(grow, pushY).applyToComponent {
                            addToCenter(editor.component)
                        }
                    }
                }
                row {
                    right {
                        createActionToolbar(
                            "CompositeEditorViewMainToolbar",
                            getAction("CompositeEditorView.MainToolbar") as ActionGroup,
                            true
                        ).apply {
                            setTargetComponent(this@CompositeEditorView)
                            setMinimumButtonSize(ActionToolbar.NAVBAR_MINIMUM_BUTTON_SIZE)
                        }.component()
                    }
                }
            })
        }
        return this
    }

    override fun dispose() {
        super.dispose()
        disposePanel(project, this)
    }

    override fun getData(dataId: String): Any? {
        var data = super<GroomToolWindowPanel>.getData(dataId)
        if (data != null) {
            return data
        }
        data = super<CompositeEditorDataKey>.getData(dataId)
        if (data != null){
            return data
        }
        if (CompositeEditorDataKey.SELECTED_NAME.`is`(dataId)) {
            return selectedName
        }
        if (CompositeEditorDataKey.SELECTED_ICON.`is`(dataId)) {
            return selectedIcon
        }
        return null
    }

    var selectedName: String = "json"

    var selectedIcon: Icon = GroomIcons.fileJson

    var editor = EditorManager.getEditor(project, this, selectedName)

    var editorWrapperPanel = simplePanel()

    override fun fireEditorLanguageChanged(language: String, icon: Icon) {
        selectedIcon = icon; selectedName = language
        val oldText: String = editor.document.text
        editorWrapperPanel.remove(editor.component)
        editor = EditorManager.getEditor(project, this, language)
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.setText(oldText)
        }
        editorWrapperPanel.add(editor.component)
        editorWrapperPanel.updateUI()
    }
}
