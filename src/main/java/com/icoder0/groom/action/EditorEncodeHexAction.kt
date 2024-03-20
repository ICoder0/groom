package com.icoder0.groom.action

import com.icoder0.groom.dialog.CharsetsComboBoxUpperDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import org.apache.commons.lang.StringUtils
import org.bouncycastle.util.encoders.Hex
import java.nio.charset.Charset


/**
 * @author bofa1ex
 * @since 2021/3/13
 */
class EditorEncodeHexAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    /**
     * Replaces the run of text selected by the primary caret with a fixed string.
     *
     * @param e Event related to this action
     */
    override fun actionPerformed(e: AnActionEvent) {
        // Get all the required data from data keys
        // Editor and Project were verified in update(), so they are not null.
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document
        // Work off of the primary caret to get the selection info
        val primaryCaret = editor.caretModel.primaryCaret
        val start = primaryCaret.selectionStart
        val end = primaryCaret.selectionEnd
        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        val dialog = CharsetsComboBoxUpperDialog("Encode Hex Options")
        var replace = editor.selectionModel.selectedText!!
        if (dialog.showAndGet()) {
            val charset = Charset.forName(dialog.getCharset())
            replace = Hex.toHexString(replace.toByteArray(charset))
            if (dialog.isUpper) {
                replace = StringUtils.upperCase(replace)
            }

            replace = zfill(replace)
        }

        if (dialog.isOK) {
            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(start, end, "\"" + replace + "\"")
            }
        }

        // De-select the text range that was just replaced
        primaryCaret.removeSelection()
    }

    fun zfill(original: String): String {
        var input = original
        if (original.length % 2 != 0) {
            input = StringUtils.leftPad(input, input.length + 1, '0')
        }
        return input
    }

    /**
     * Sets visibility and enables this action menu item if:
     *
     *  * a project is open
     *  * an editor is active
     *  * some characters are selected
     *
     *
     * @param e Event related to this action
     */
    override fun update(e: AnActionEvent) {
        // Get required data keys
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        // Set visibility and enable only in case of existing project and editor and if a selection exists
        e.presentation.isEnabledAndVisible = project != null && editor != null && editor.selectionModel.hasSelection()
    }
}