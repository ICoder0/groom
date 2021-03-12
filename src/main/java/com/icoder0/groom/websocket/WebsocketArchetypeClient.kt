package com.icoder0.groom.websocket

import com.icoder0.groom.panel.WsPanel
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*

/**
 * @author bofa1ex
 * @since  2021/2/20
 */
class WebsocketArchetypeClient(serverUri: URI?, var wspanel: WsPanel) : WebSocketClient(serverUri) {

    override fun onOpen(handShakeData: ServerHandshake) {
        // do nothing.
    }

    override fun onMessage(message: String) {
        wspanel.messageCallback(WebsocketConstant.inboundType, message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        wspanel.fireDisconnectCallback()
    }

    override fun onError(ex: Exception) {
       ex.printStackTrace()
    }

    companion object {
        private var singleton: WebsocketArchetypeClient? = null

        fun start(url: String, wspanel: WsPanel): WebsocketArchetypeClient? {
            if (Objects.nonNull(singleton)) {
                singleton!!.close()
            }
            singleton = WebsocketArchetypeClient(URI.create(url), wspanel)
            return singleton
        }
    }
}