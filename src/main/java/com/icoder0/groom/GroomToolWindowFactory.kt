package com.icoder0.groom

import com.icoder0.groom.component.EditorManager
import com.icoder0.groom.i18n.GroomBundle
import com.icoder0.groom.ui.CompositeEditorView
import com.icoder0.groom.ui.GroomToolWindowPanel
import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.ToolWindowTabRenameActionBase
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.actions.IconWithTextAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.ui.LayeredIcon
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import icons.GroomIcons


/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class GroomToolWindowFactory : ToolWindowFactory, UserDataHolderBase() {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) = with(toolWindow as ToolWindowEx) {
        EditorManager.init(project)
        setTabDoubleClickActions(listOf(ToolWindowTabRenameActionBase(toolWindow.id, GroomBundle.message("action.GroomView.doubleclick.text.rename.tab"))))
        setTabActions(object : DefaultActionGroup("Create new tab", true) {
            init {
                templatePresentation.icon = AllIcons.General.Add
                templatePresentation.isEnabledAndVisible = true
            }
        }.apply {
            add(object : IconWithTextAction("Websocket", "", GroomIcons.Socket) {
                override fun actionPerformed(e: AnActionEvent) {
                    with(WebsocketClientView(project, toolWindow).initUI()) {
                        with(ContentFactory.SERVICE.getInstance().createContent(
                                this, "Uname", false
                        ).apply {
                            icon = GroomIcons.Socket
                            putUserData<Boolean>(ToolWindow.SHOW_CONTENT_ICON, java.lang.Boolean.TRUE)
                        }) {
                            contentManager.addContent(this)
                            contentManager.setSelectedContent(this)
                        }
                    }
                }
            })
            add(object : IconWithTextAction("Editor", "", GroomIcons.Editor) {
                override fun actionPerformed(e: AnActionEvent) {
                    with(CompositeEditorView(project, toolWindow).initUI()) {
                        with(ContentFactory.SERVICE.getInstance().createContent(
                                this, "Uname", false
                        ).apply {
                            icon = GroomIcons.Editor
                            putUserData<Boolean>(ToolWindow.SHOW_CONTENT_ICON, java.lang.Boolean.TRUE)
                        }) {
                            contentManager.addContent(this)
                            contentManager.setSelectedContent(this)
                        }
                    }
                }
            })
        })
        with(CompositeEditorView(project, toolWindow).initUI()) {
            contentManager.addContent(ContentFactory.SERVICE.getInstance().createContent(
                    this, "Uname", false
            ).apply {
                icon = GroomIcons.Editor
                putUserData<Boolean>(ToolWindow.SHOW_CONTENT_ICON, java.lang.Boolean.TRUE)
            })
        }
        toolWindow.addContentManagerListener(object : ContentManagerListener {
            override fun contentRemoveQuery(event: ContentManagerEvent) {
                if (event.content.icon is LayeredIcon) {
                    if (Messages.showOkCancelDialog(
                                    "Process Is Running, Make sure to dispose it.",
                                    "",
                                    "Terminate",
                                    "Cancel",
                                    GroomIcons.Logo32x) == Messages.CANCEL) {
                        contentManager.addContent(event.content, event.index)
                        event.consume()
                    }
                }
            }
        })
    }
}