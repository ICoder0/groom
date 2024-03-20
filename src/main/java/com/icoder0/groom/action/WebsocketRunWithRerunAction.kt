package com.icoder0.groom.action

import com.icoder0.groom.ui.WebsocketClientView
import com.icoder0.groom.util.IdeUtils
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
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
            null, false -> e.presentation.icon = AllIcons.Actions.StartDebugger
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
                IdeUtils.notify("Websocket RunConfiguration/RequestURL is Empty." +
                        "\tTry to create/fill it.", MessageType.WARNING)
                return
            }
            indicator.text = "Try to connect websocket..."
            indicator.isIndeterminate = true
            websocketClientView.fireWebsocketPreConnect()
            val newWebsocketClient = createWsClient(websocketClientView)
            try {
                taskFuture = newWebsocketClient?.connect(Executors.newSingleThreadExecutor())
                taskFuture?.get(5, TimeUnit.SECONDS)
                websocketClientView.fireWebsocketConnected(newWebsocketClient)
            } catch (e: ProcessCanceledException) {
                IdeUtils.notify("Cancel Task Manually: %s".format(e.message), MessageType.ERROR)
            } catch (e: Exception) {
                IdeUtils.notify(e.message ?: e::javaClass.name, MessageType.ERROR)
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
                val recreateWsClient = createWsClient(websocketClientView)
                taskFuture = recreateWsClient?.connect(Executors.newSingleThreadExecutor())
                taskFuture?.get(5, TimeUnit.SECONDS)
                websocketClientView.fireWebsocketConnected(recreateWsClient)
            } catch (e: ProcessCanceledException) {
                IdeUtils.notify("Cancel Task Manually: %s".format(e.message), MessageType.ERROR)
            } catch (e: Exception) {
                IdeUtils.notify(e.message ?: e::javaClass.name, MessageType.ERROR)
            }
        }
    }

    fun createWsClient(websocketClientView: WebsocketClientView): WebSocket? {
        return WebsocketClientView.webSocketFactory.createSocket(URI.create(websocketClientView.wsClientAddress!!)).apply {
            addListener(object : WebSocketAdapter() {
                override fun onTextMessage(websocket: WebSocket?, text: String?) {
                    websocketClientView.fireMessageChanged(0, text)
                }

                override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
                    if (closedByServer) {
                        IdeUtils.notify("closed by server internal error", MessageType.ERROR)
                    }
                    websocketClientView.fireWebsocketDisconnected()
                }
            })
        }
    }
}