package com.icoder0.groom.component

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.LanguageTextField.DocumentCreator
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.util.function.Supplier
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

/**
 * @author bofa1ex
 * @since  2021/3/5
 */
open class EditorManager {
    companion object EditorManagerInternal {
        val DEFAULT_CREATOR: DocumentCreator = SimpleDocumentCreator()

        val editorPanelMap: MutableMap<JPanel, MutableMap<String, Editor>> = HashMap()

        val editorDisplayMap: MutableMap<String, Supplier<Editor>> = HashMap()

        val editorFactory = EditorFactory.getInstance()!!

        fun getEditor(panel: JPanel, display: String): Editor {
            return editorPanelMap[panel]!![display]!!
        }

        fun initPanel(panel: JPanel){
            editorPanelMap.putIfAbsent(panel, mutableMapOf(
                    Pair("json", editorDisplayMap["json"]!!.get()),
                    Pair("plainText", editorDisplayMap["plainText"]!!.get()),
                    Pair("html", editorDisplayMap["html"]!!.get()),
                    Pair("xml", editorDisplayMap["xml"]!!.get()),
            ))
        }

        fun disposePanel(panel: JPanel){
            editorPanelMap[panel]?.forEach { _, editor ->
                EditorFactory.getInstance().releaseEditor(editor)
            }
            editorPanelMap[panel]?.clear()
        }

        fun init(project: Project?) {
            editorDisplayMap["json"] = Supplier {
                editorFactory.createEditor(DEFAULT_CREATOR.createDocument("", JsonLanguage.INSTANCE, project), project, JsonFileType.INSTANCE, false).apply {
                    this.settings.apply {
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
                        isCaretRowShown = false
                        isRightMarginShown = false
                        isAdditionalPageAtBottom = false
                        isLineMarkerAreaShown = false
                    }
                }
            }
            editorDisplayMap["xml"] = Supplier {
                editorFactory.createEditor(DEFAULT_CREATOR.createDocument("", XMLLanguage.INSTANCE, project), project, XmlFileType.INSTANCE, false).apply {
                    this.settings.apply {
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