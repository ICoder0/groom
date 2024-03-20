package com.icoder0.groom.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.panel.PanelBuilder
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import java.nio.charset.Charset
import javax.swing.JComponent

open class CharsetsComboBoxDialog(titleName: String) : DialogWrapper(true) {
    fun getCharset(): String = charsetComboBoxModel.selected.toString()
    private var charsetComboBoxModel = CollectionComboBoxModel(listOf(
        "UTF-8",
        "ASCII",
        "CP1256",
        "ISO-8859-1",
        "ISO-8859-2",
        "ISO-8859-6",
        "ISO-8859-15",
        "Windows-1252"))

    protected open fun com.intellij.ui.dsl.builder.Panel.additionalRows() {}
    open fun compose(): com.intellij.ui.dsl.builder.Panel.() -> Unit {
        return {
            row("Choose charset") {
                comboBox(charsetComboBoxModel).applyToComponent {
                    isOpaque = true
                    isEditable = true
                    selectedItem = "UTF-8"
                    val default = foreground
                    val textField = editor.editorComponent.asSafely<javax.swing.JTextField>()
                    textField!!.document.addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(p0: javax.swing.event.DocumentEvent) {
                            try {
                                Charset.forName(textField.text.trim())
                                textField.foreground = default
                            } catch (e: Exception) {
                                textField.foreground = JBColor.RED
                            }
                        }
                    })
                }
            }
            // Allow subclasses to add more rows
            additionalRows()
        }
    }

    override fun createCenterPanel(): JComponent = panel(compose())

    init {
        this.init()
        this.title = titleName
    }
}