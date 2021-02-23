package com.icoder0.groom

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class GroomToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(GroomToolWindowDsl(project).getMainPanel(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}