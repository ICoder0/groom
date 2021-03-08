package com.icoder0.groom.renderer

import com.intellij.openapi.editor.EditorFactory
import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

/**
 * @author bofa1ex
 * @since 2021/3/5
 */
class EditorExTableCellEditor : AbstractTableCellEditor() {
    private val editor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""))
    private val editorOriginalCellHeight: Int

    override fun isCellEditable(e: EventObject): Boolean {
        return e is MouseEvent && e.clickCount == 2
    }

    override fun getTableCellEditorComponent(table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int): Component {
        editor.document.setText(value.toString())
        if (isSelected) {
            table.setRowHeight(row, 10 * editorOriginalCellHeight)
        }
        return editor.component
    }

    override fun getCellEditorValue(): Any {
        return editor.document.text
    }

    init {
        editor.settings.additionalLinesCount = 3
        editor.settings.additionalColumnsCount = 0
        editor.settings.isLineMarkerAreaShown = false
        editor.settings.isAdditionalPageAtBottom = false
        editor.settings.isRightMarginShown = false
        editorOriginalCellHeight = editor.component.getFontMetrics(editor.component.font).height
    }
}