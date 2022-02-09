package com.icoder0.groom.ui

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DataProvider
import javax.swing.Icon

/**
 * @author bofa1ex
 * @since  2021/5/25
 */
interface CompositeEditorDataKey : DataProvider {

    fun getEditorVisible() : Boolean

    fun fireEditorLanguageChanged(language: String, icon: Icon)

    override fun getData(dataId: String): Any? {
        if (COMPOSITE_EDITOR_KEY.`is`(dataId)) {
            return this
        }
        if (IS_EDITOR_VISIBLE_KEY.`is`(dataId)){
            return getEditorVisible()
        }
        return null
    }

    companion object {
        @JvmStatic
        val COMPOSITE_EDITOR_KEY: DataKey<CompositeEditorDataKey> = DataKey.create("COMPOSITE_EDITOR_KEY")

        @JvmStatic
        val SELECTED_NAME: DataKey<String> = DataKey.create("SELECTED_NAME")

        @JvmStatic
        val SELECTED_ICON: DataKey<Icon> = DataKey.create("SELECTED_ICON")

        @JvmStatic
        val IS_EDITOR_VISIBLE_KEY: DataKey<Boolean> = DataKey.create("IS_EDITOR_VISIBLE_KEY")
    }
}