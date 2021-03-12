package com.icoder0.groom.component

import com.icoder0.groom.panel.IPanel
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField.DocumentCreator
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.util.*
import java.util.function.Supplier
import kotlin.collections.HashMap

/**
 * @author bofa1ex
 * @since  2021/3/5
 */
open class EditorManager {
    companion object EditorManagerInternal {
        val DEFAULT_CREATOR: DocumentCreator = SimpleDocumentCreator()

        val editorPanelMap: MutableMap<IPanel, MutableMap<String, Editor>> = HashMap()

        val editorDisplayMap: MutableMap<String, Supplier<Editor>> = HashMap()

        val editorFactory = EditorFactory.getInstance()

        fun getEditor(iPanel: IPanel, display: String): Editor {
            val editorMap = editorPanelMap.computeIfAbsent(iPanel) { t -> HashMap() }
            return editorMap.computeIfAbsent(display) { t -> editorDisplayMap[display]!!.get() }
        }

        fun init(project: Project?) {
            editorDisplayMap["json"] = Supplier {
                editorFactory.createEditor(DEFAULT_CREATOR.createDocument("", JsonLanguage.INSTANCE, project), project, JsonFileType.INSTANCE, false).apply {
                    this.settings.apply {
                        additionalLinesCount = 45
                        additionalColumnsCount = 0
                        isCaretRowShown = false
                        isRightMarginShown = false
                        isAdditionalPageAtBottom = false
                        isLineMarkerAreaShown = false
                    }
                }
            }
            editorDisplayMap["plainText"] = Supplier {
                editorFactory.createEditor(editorFactory.createDocument("")).apply {
                    this.settings.apply {
                        additionalLinesCount = 45
                        additionalColumnsCount = 0
                        isCaretRowShown = false
                        isRightMarginShown = false
                        isAdditionalPageAtBottom = false
                        isLineMarkerAreaShown = false
                    }
                }
            }
            editorDisplayMap["xml"] = Supplier {
                editorFactory.createEditor(DEFAULT_CREATOR.createDocument("", XMLLanguage.INSTANCE, project), project, XmlFileType.INSTANCE, false).apply {
                    this.settings.setTabSize(4)
                    this.settings.apply {
                        additionalLinesCount = 45
                        additionalColumnsCount = 0
                        isCaretRowShown = false
                        isRightMarginShown = false
                        isAdditionalPageAtBottom = false
                        isLineMarkerAreaShown = false
                    }
                }
            }

            editorDisplayMap["html"] = Supplier {
                editorFactory.createEditor(DEFAULT_CREATOR.createDocument("", HTMLLanguage.INSTANCE, project), project, HtmlFileType.INSTANCE, false).apply {
                    this.settings.apply {
                        setTabSize(4)
                        additionalLinesCount = 45
                        additionalColumnsCount = 0
                        isCaretRowShown = false
                        isRightMarginShown = false
                        isAdditionalPageAtBottom = false
                        isLineMarkerAreaShown = false
                    }
                }
            }
        }
    }
}