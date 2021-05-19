package com.icoder0.groom.action

import com.icoder0.groom.component.NotificationManager
import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketException

/**
 * @author bofa1ex
 * @since  2021/5/6
 */
class WebsocketStopAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        WebsocketStopTask(e.project, websocketClientView?.wsClient).queue()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = "Stop websocket client"
        val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        e.presentation.isEnabled = websocketClientView?.wsClient?.isOpen ?: false
    }

    inner class WebsocketStopTask(project: Project?, var websocketClient: WebSocket?) : Task.Backgroundable(project, "Websocket disconnecting", false) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text = "Try to disconnect websocket..."
            indicator.isIndeterminate = true
            try {
                websocketClient?.disconnect(0)
            } catch (e: Exception) {
                NotificationManager.notifyError(project, e.message)
            }
        }
    }
}