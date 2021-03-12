package com.icoder0.groom.panel

import com.icoder0.groom.component.EditorManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import com.intellij.util.ui.components.BorderLayoutPanel

/**
 * @author bofa1ex
 * @since  2021/3/12
 */
class EditorPanel(project: Project) : IPanel{

    override fun getComponent(): DialogPanel {
        return panel{
            row {
                cell(isFullWidth = true, init = {
                    editorWrapperPanel(grow)
                })
            }
            row {
                right {
                    BorderLayoutPanel().addToRight(languageComboBox)()
                }
            }
        }
    }

    /* websocket request 编辑器 */
    private var editor = EditorManager.getEditor(this,"json")

    private var languageComboBox = ComboBox(arrayOf("json", "xml", "html", "plainText")).apply {
        this.addActionListener {
            val oldText: String = this@EditorPanel.editor.document.text
            editorWrapperPanel.remove(this@EditorPanel.editor.component)
            this@EditorPanel.editor = EditorManager.getEditor(this@EditorPanel, this.selectedItem as String)
            // Make the document change in the context of a write action.
            WriteCommandAction.runWriteCommandAction(project) {
                this@EditorPanel.editor.document.replaceString(
                        0, this@EditorPanel.editor.document.textLength, oldText
                )
            }
            editorWrapperPanel.add(this@EditorPanel.editor.component)
        }
    }

    private var editorWrapperPanel = BorderLayoutPanel().apply {
        addToCenter(editor.component)
    }
}
