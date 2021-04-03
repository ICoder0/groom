package com.icoder0.groom.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.withSelectedBinding
import com.intellij.util.castSafelyTo
import org.apache.commons.lang.StringUtils
import org.bouncycastle.util.encoders.Hex
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.event.DocumentEvent


/**
 * @author bofa1ex
 * @since 2021/3/13
 */
class EditorEncodeMD5Action : AnAction() {
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
        val dialog = EncodeMD5Dialog()
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
        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, replace)
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


    class EncodeMD5Dialog : DialogWrapper(true) {
        var is32BitUpper = false
        var is32BitLower = false
        var is16BitUpper = false
        var is16BitLower = false


        fun getCharset(): String {
            return charsetComboBox.editor.editorComponent.castSafelyTo<JTextField>()!!.text.trim()
        }

        val charsetComboBox = ComboBox(arrayOf("UTF-8", "ASCII", "CP1256", "ISO-8859-1", "ISO-8859-2", "ISO-8859-6", "ISO-8859-15", "Windows-1252")).apply {
            isOpaque = true
            isEditable = true
            selectedItem = "UTF-8"
            val default = foreground
            val textField = editor.editorComponent.castSafelyTo<JTextField>()
            textField!!.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    try {
                        Charset.forName(textField.text.trim())
                        textField.foreground = default
                    } catch (e: Exception) {
                        textField.foreground = JBColor.RED
                    }
                }
            })
        }

        override fun createCenterPanel(): JComponent? {
            return panel {
                row("Choose charset") {
                    charsetComboBox()
                }
                row {
                    buttonGroup("Choose Option") {
                        row {
                            radioButton("32 bit(Upper)").withSelectedBinding(
                                    PropertyBinding({ is32BitUpper }, { is32BitUpper = it })
                            ).applyToComponent {
                                isSelected = true
                            }
                        }
                        row {
                            radioButton("32 bit(Lower)").withSelectedBinding(
                                    PropertyBinding({ is32BitLower }, { is32BitLower = it })
                            )
                        }
                        row {
                            radioButton("16 bit(Upper)").withSelectedBinding(
                                    PropertyBinding({ is16BitUpper }, { is16BitUpper = it })
                            )
                        }
                        row {
                            radioButton("16 bit(Lower)").withSelectedBinding(
                                    PropertyBinding({ is16BitLower }, { is16BitLower = it })
                            )
                        }
                    }
                }
            }
        }

        init {
            init()
            title = "Encode MD5 Options"
        }

    }
}