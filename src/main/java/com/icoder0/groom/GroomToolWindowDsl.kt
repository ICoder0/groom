package com.icoder0.groom

import com.icoder0.groom.panel.EditorPanel
import com.icoder0.groom.panel.WsPanel
import com.intellij.openapi.project.Project
import com.intellij.ui.TabbedPaneImpl
import com.intellij.ui.layout.panel
import javax.swing.*
import javax.swing.SwingConstants.TOP

/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class GroomToolWindowDsl(project: Project) {

    private val wsPanel = WsPanel(project)

    private val editorPanel = EditorPanel(project)

    fun getMainPanel(): JComponent {
        val mainPane = TabbedPaneImpl(TOP)
        mainPane.addTab("wsClient", wsPanel.getComponent())
        mainPane.addTab("editor", editorPanel.getComponent())
        mainPane.addTab("reverse", panel {})
        return mainPane
    }
}