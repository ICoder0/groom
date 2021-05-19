package com.icoder0.groom.action

import com.icoder0.groom.component.NotificationManager
import com.icoder0.groom.ui.WebsocketClientView
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFrame
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * @author bofa1ex
 * @since  2021/5/6
 */
class WebsocketRunWithRerunAction : AnAction("", "asdasd", AllIcons.Actions.StartDebugger) {
    override fun update(e: AnActionEvent) {
        e.presentation.text = "Run/Rerun websocket client"
        val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        if (websocketClientView?.wsClient == null) {
            e.presentation.icon = AllIcons.Actions.StartDebugger
            return
        }
        when (websocketClientView.wsClient?.isOpen) {
            true -> e.presentation.icon = AllIcons.Actions.Restart
            false -> e.presentation.icon = AllIcons.Actions.StartDebugger
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val websocketClientView = e.getData(WebsocketClientView.WEBSOCKET_VIEW_KEY)
        if (websocketClientView?.wsClient == null) {
            WebsocketRunTask(e.project, websocketClientView).queue()
            return
        }
        when (websocketClientView.wsClient!!.isOpen) {
            true -> WebsocketRerunTask(e.project, websocketClientView).queue()
            false -> WebsocketRunTask(e.project, websocketClientView).queue()
        }
    }

    inner class WebsocketRunTask(project: Project?,
                                 val websocketClientView: WebsocketClientView?
    ) : Task.Backgroundable(project, "Websocket connecting", true) {
        init {
            cancelText = "cancel websocket connecting"
        }

        var taskFuture: Future<WebSocket>? = null

        override fun run(indicator: ProgressIndicator) {
            if (websocketClientView?.wsClientAddress == null || websocketClientView.wsClientAddress?.isEmpty()!!) {
                NotificationManager.notify(project, NotificationType.WARNING,
                        "Websocket RunConfiguration/RequestURL is Empty." +
                                "\tTry to create/fill it."
                )
                return
            }
            indicator.text = "Try to connect websocket..."
            indicator.isIndeterminate = true
            websocketClientView.fireWebsocketPreConnect()
            val newWebsocketClient = WebsocketClientView.webSocketFactory.createSocket(URI.create(websocketClientView.wsClientAddress!!)).apply {
                addListener(object : WebSocketAdapter() {
                    override fun onTextMessage(websocket: WebSocket?, text: String?) {
                        websocketClientView.fireMessageChanged(0, text)
                    }

                    override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
                        if (closedByServer) {
                            NotificationManager.notify(project, NotificationType.ERROR, "closed by server internal error")
                        }
                        websocketClientView.fireWebsocketDisconnected()
                    }
                })
            }
            try {
                taskFuture = newWebsocketClient?.connect(Executors.newSingleThreadExecutor())
                taskFuture?.get(5, TimeUnit.SECONDS)
                websocketClientView.fireWebsocketConnected(newWebsocketClient)
            } catch (e: ProcessCanceledException) {
                NotificationManager.notify(project, NotificationType.ERROR, "Cancel Task Manually: %s".format(e.message))
            } catch (e: Exception) {
                NotificationManager.notify(project, NotificationType.ERROR, e.message)
            }
        }

        override fun onCancel() {
            taskFuture?.cancel(true)
        }
    }

    open inner class WebsocketRerunTask(project: Project?,
                                        val websocketClientView: WebsocketClientView
    ) : Task.Backgroundable(project, "Websocket reloading", true) {
        init {
            cancelText = "cancel websocket reconnecting"
        }

        var taskFuture: Future<WebSocket>? = null

        override fun onCancel() {
            taskFuture?.cancel(true)
        }

        override fun run(indicator: ProgressIndicator) {
            try {
                indicator.text = "Try to reconnect websocket..."
                indicator.isIndeterminate = true
                websocketClientView.fireWebsocketPreConnect()
                websocketClientView.wsClient?.disconnect(0)
                val recreateWsClient = websocketClientView.wsClient?.recreate()
                taskFuture = recreateWsClient?.connect(Executors.newSingleThreadExecutor())
                taskFuture?.get(5, TimeUnit.SECONDS)
                websocketClientView.fireWebsocketConnected(recreateWsClient)
            } catch (e: ProcessCanceledException) {
                NotificationManager.notify(project, NotificationType.ERROR, "Cancel Task Manually: %s".format(e.message))
            } catch (e: Exception) {
                NotificationManager.notify(project, NotificationType.ERROR, e.message)
            }
        }
    }
}