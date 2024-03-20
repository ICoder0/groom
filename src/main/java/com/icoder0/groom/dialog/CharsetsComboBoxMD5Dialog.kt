package com.icoder0.groom.dialog

import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected

class CharsetsComboBoxMD5Dialog : CharsetsComboBoxDialog("Encode MD5 Options") {
    var is32BitUpper = true
    var is32BitLower = false
    var is16BitUpper = false
    var is16BitLower = false

    override fun Panel.additionalRows() {
        buttonsGroup(title = "Choose Option", indent = true) {
            row {
                radioButton("32 bit(Upper)").bindSelected({ is32BitUpper }, { is32BitUpper = it }).applyToComponent { isSelected = true }
            }
            row {
                radioButton("32 bit(Lower)").bindSelected({ is32BitLower }, { is32BitLower = it })
            }
            row {
                radioButton("16 bit(Upper)").bindSelected({ is16BitUpper }, { is16BitUpper = it })
            }
            row {
                radioButton("16 bit(Lower)").bindSelected({ is16BitLower }, { is16BitLower = it })
            }
        }
    }
}