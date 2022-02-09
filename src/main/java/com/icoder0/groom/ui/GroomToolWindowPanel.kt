package com.icoder0.groom.ui

import com.icoder0.groom.component.EditorManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ex.ToolWindowEx

/**
 * @author bofa1ex
 * @since  2021/4/17
 */
@SuppressWarnings("LeakingThisInConstructor")
abstract class GroomToolWindowPanel(val project: Project, val toolWindow: ToolWindowEx) : SimpleToolWindowPanel(true), Disposable {

    init {
        EditorManager.initPanel(project, this)
    }

    override fun dispose() {
        removeAll()
        removeNotify()
    }

    abstract fun initUI(): GroomToolWindowPanel
}