package com.icoder0.groom.action

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.PsiDocumentManager
import org.apache.commons.lang3.StringUtils

/**
 * @author bofa1ex
 * @since 2021/5/21
 */
class EditorJsonUniqueAction : DumbAwareAction(){
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document
        // Work off of the primary caret to get the selection info
        val primaryCaret = editor.caretModel.primaryCaret
        val start = primaryCaret.selectionStart
        val end = primaryCaret.selectionEnd
        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, StringUtils.replaceEach(
                    editor.selectionModel.selectedText, arrayOf("\n", "\t", "\r", " "), arrayOf("", "", "", "")
            ))
        }
        // De-select the text range that was just replaced
        primaryCaret.removeSelection()
    }

    override fun update(e: AnActionEvent) {
        // Get required data keys
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val document = editor?.document
        // Set visibility and enable only in case of existing project and editor and if a selection exists
        e.presentation.isEnabledAndVisible = project != null && editor != null && document != null && editor.selectionModel.hasSelection()
                && PsiDocumentManager.getInstance(project).getPsiFile(document) is JsonFile
    }

}