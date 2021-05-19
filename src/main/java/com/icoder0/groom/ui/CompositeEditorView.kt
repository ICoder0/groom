package com.icoder0.groom.ui

import com.icoder0.groom.component.EditorManager
import com.icoder0.groom.component.EditorManager.EditorManagerInternal.disposePanel
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBUI.Panels.simplePanel

/**
 * @author bofa1ex
 * @since  2021/3/12
 */
class CompositeEditorView(project: Project, toolWindow: ToolWindowEx) : GroomToolWindowPanel(project, toolWindow) {

    override fun initUI(): CompositeEditorView {
        setContent(panel {
            row {
                cell(isFullWidth = true) {
                    editorWrapperPanel(grow, pushY).applyToComponent {
                        addToCenter(editor.component)
                    }
                }
                right {
                    languageComboBox().applyToComponent {
                        addActionListener {
                            val oldText: String = this@CompositeEditorView.editor.document.text
                            editorWrapperPanel.remove(this@CompositeEditorView.editor.component)
                            this@CompositeEditorView.editor = EditorManager.getEditor(project, this@CompositeEditorView, this.selectedItem as String)
                            WriteCommandAction.runWriteCommandAction(project) {
                                this@CompositeEditorView.editor.document.setText(oldText)
                            }
                            editorWrapperPanel.add(this@CompositeEditorView.editor.component)
                            this@CompositeEditorView.editorWrapperPanel.updateUI()
                        }
                    }
                }
            }
        })
        return this
    }

    override fun dispose() {
        super.dispose()
        disposePanel(project, this)
    }

    var editor = EditorManager.getEditor(project, this, "json")

    var languageComboBox = ComboBox(arrayOf("json", "xml", "html", "plainText"))

    var editorWrapperPanel = simplePanel()
}
