package com.icoder0.groom.util

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.util.PopupUtil

import java.awt.datatransfer.StringSelection


/**
 * @author bofa1ex
 * @since  2021/5/21
 */
class IdeUtils {
    companion object {
        fun copyToClipboard(content: String?) {
            val selection = StringSelection(content)
            CopyPasteManager.getInstance().setContents(selection)
        }

        fun notify(message: String, type: MessageType) {
            PopupUtil.showBalloonForActiveFrame(message, type);
        }
    }
}