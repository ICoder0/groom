package com.icoder0.groom.panel

import com.intellij.openapi.ui.DialogPanel

/**
 * @author bofa1ex
 * @since  2021/3/12
 */
interface IPanel {

    fun getComponent(): DialogPanel;
}