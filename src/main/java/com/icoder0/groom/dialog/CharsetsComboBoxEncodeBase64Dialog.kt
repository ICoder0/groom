package com.icoder0.groom.dialog

import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.layout.selected

class CharsetsComboBoxEncodeBase64Dialog : CharsetsComboBoxDialog("Encode Base64 Options") {
    var isLf = true
    var isCr = false
    var isCrlf = false

    override fun Panel.additionalRows() {
        @Suppress("DialogTitleCapitalization")
        buttonsGroup(title = "Convert input", indent = true) {
            row {
                radioButton("LF - \\n").bindSelected({ isLf }, { isLf = it}).applyToComponent { isSelected = true }
            }
            row {
                radioButton("CR - \\r").bindSelected({ isCr }, { isCr = it})
            }
            row {
                radioButton("CRLF - \\r\\n").bindSelected({ isCrlf }, { isCrlf = it})
            }
        }
    }
}