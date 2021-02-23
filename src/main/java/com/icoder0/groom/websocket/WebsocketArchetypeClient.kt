package com.icoder0.groom.websocket

import com.icoder0.groom.GroomToolWindowDsl
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.rd.createLifetime
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.layout.panel
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.awt.Event
import java.awt.Point
import java.awt.Rectangle
import java.net.URI
import java.util.*

/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class WebsocketArchetypeClient(serverUri: URI?, var groomToolWindowDsl: GroomToolWindowDsl) : WebSocketClient(serverUri) {

    override fun onOpen(handShakeData: ServerHandshake) {
        // do nothing.
    }

    override fun onMessage(message: String) {
        groomToolWindowDsl.messageCallback(WebsocketConstant.inboundType, message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        groomToolWindowDsl.fireDisconnectCallback()
    }

    override fun onError(ex: Exception) {
       ex.printStackTrace()
    }

    companion object {
        private var singleton: WebsocketArchetypeClient? = null

        fun start(url: String, groomToolWindowDsl: GroomToolWindowDsl): WebsocketArchetypeClient? {
            if (Objects.nonNull(singleton)) {
                singleton!!.close()
            }
            singleton = WebsocketArchetypeClient(URI.create(url), groomToolWindowDsl)
            return singleton
        }
    }
}