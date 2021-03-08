package com.icoder0.groom.renderer

import java.awt.Component
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

/**
 * @author bofa1ex
 * @since 2021/3/7
 */
class IconRendererEx : DefaultTableCellRenderer.UIResource() {
    public override fun setValue(value: Any) {
        icon = if (value is Icon) value else null
    }

    override fun getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        if (isSelected && table.selectedColumn == column) {
            super.setForeground(table.selectionForeground)
            super.setBackground(table.selectionBackground)
        } else {
            super.setForeground(table.foreground)
            super.setBackground(table.background)
        }
        font = table.font
        setValue(value)
        return this
    }

    init {
        horizontalAlignment = JLabel.CENTER
    }
}