package com.icoder0.groom.component

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField.DocumentCreator
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.util.function.Supplier
import javax.swing.JPanel

/**
 * @author bofa1ex
 * @since  2021/3/5
 */
open class EditorManager {
    companion object EditorManagerInternal {
        val DEFAULT_CREATOR: DocumentCreator = SimpleDocumentCreator()

        val editorProjectMap: MutableMap<Project, MutableMap<JPanel, MutableMap<String, Editor>>> = HashMap()

        val editorDisplayMap: MutableMap<String, Supplier<Editor>> = HashMap()

        val editorFactory = EditorFactory.getInstance()!!

        fun getEditor(project: Project, panel: JPanel, display: String): Editor {
            return editorProjectMap[project]!![panel]!![display]!!
        }

        fun initPanel(project: Project, panel: JPanel) {
            kotlin.run {
                for (projectEx in editorProjectMap.keys) {
                    if (projectEx.isDisposed){
                        editorProjectMap.remove(projectEx)
                    }
                }
            }
            editorProjectMap.compute(project) { _, item ->
                if (item == null){
                    return@compute mutableMapOf(
                            Pair(panel, mutableMapOf(
                                    Pair("json", editorDisplayMap["json"]!!.get()),
                                    Pair("plainText", editorDisplayMap["plainText"]!!.get()),
                                    Pair("html", editorDisplayMap["html"]!!.get()),
                                    Pair("xml", editorDisplayMap["xml"]!!.get()),
                            ))
                    )
                }
                item.putIfAbsent(panel, mutableMapOf(
                    Pair("json", editorDisplayMap["json"]!!.get()),
                    Pair("plainText", editorDisplayMap["plainText"]!!.get()),
                    Pair("html", editorDisplayMap["html"]!!.get()),
                    Pair("xml", editorDisplayMap["xml"]!!.get()),
                ))
                return@compute item
            }
        }

        fun disposePanel(project: Project, panel: JPanel) {
            editorProjectMap[project]?.get(panel)?.forEach{ (_, editor) ->
                EditorFactory.getInstance().releaseEditor(editor)
            }
            editorProjectMap[project]?.get(panel)?.clear()
            if (project.isDisposed) editorProjectMap[project]?.clear()
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