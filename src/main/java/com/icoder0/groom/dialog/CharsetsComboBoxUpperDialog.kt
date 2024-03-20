package com.icoder0.groom.dialog

import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected

open class CharsetsComboBoxUpperDialog(titleName: String) : CharsetsComboBoxDialog(titleName) {
    var isUpper = true

    override fun Panel.additionalRows() {
        buttonsGroup {
            row {
                radioButton("Upper").bindSelected({ isUpper }, { isUpper = it }).applyToComponent { isSelected = true }
                radioButton("Lower").bindSelected({ !isUpper }, { isUpper = !it })
            }
        }
    }
}