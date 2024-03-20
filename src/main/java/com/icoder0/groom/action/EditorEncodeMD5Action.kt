package com.icoder0.groom.action

import com.icoder0.groom.dialog.CharsetsComboBoxMD5Dialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import org.apache.commons.lang.StringUtils
import org.bouncycastle.util.encoders.Hex
import java.nio.charset.Charset
import java.security.MessageDigest


/**
 * @author bofa1ex
 * @since 2021/3/13
 */
class EditorEncodeMD5Action : AnAction() {
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
        val dialog = CharsetsComboBoxMD5Dialog()
        var replace = editor.selectionModel.selectedText!!
        if (dialog.showAndGet()) {
            val charset  = Charset.forName(dialog.getCharset())
            replace = Hex.toHexString(MessageDigest.getInstance("MD5").digest(document.text.toByteArray(charset)))
            if (dialog.is32BitUpper) {
                replace = StringUtils.upperCase(replace)
            }
            if (dialog.is32BitLower) {
                replace = StringUtils.lowerCase(replace)
            }
            if (dialog.is16BitUpper) {
                replace = StringUtils.upperCase(replace).substring(9, 24)
            }
            if (dialog.is16BitLower) {
                replace = StringUtils.lowerCase(replace).substring(9, 24)
            }
        }

        if (dialog.isOK) {
            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(start, end, "\"" + replace + "\"")
            }
        }

        // De-select the text range that was just replaced
        primaryCaret.removeSelection()
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