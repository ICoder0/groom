package com.icoder0.groom.ui.renderer

import org.apache.commons.lang.StringUtils
import java.awt.Component
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.table.TableCellRenderer

/**
 * @author bofa1ex
 * @since 2021/3/6
 */
class TextFieldTableCellRenderer : TableCellRenderer {
    override fun getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        val text = StringUtils.replace(value as String, "\n", "↵")
        val comp = JTextField(text)
        if (isSelected && table.selectedColumn == column) {
            comp.foreground = table.selectionForeground
            comp.background = table.selectionBackground
        } else {
            comp.foreground = table.foreground
            comp.background = table.background
        }
        comp.font = table.font
        if (table.editingRow == row && table.editingColumn == column) {
            return comp
        }
        table.setRowHeight(row, table.rowHeight)
        return comp
    }
}