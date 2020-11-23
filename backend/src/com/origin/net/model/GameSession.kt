package com.origin.net.model

import com.origin.entity.Account
import com.origin.net.WSServer
import org.java_websocket.WebSocket

class GameSession(private val connect: WebSocket?, val remoteAddr: String) {
    var account: Account? = null

    fun send(channel: String?, data: Any?) {
        if (connect != null && connect.isOpen) {
            val response = WSResponse()
            response.id = 0
            response.data = data
            response.channel = channel
            connect.send(WSServer.gsonSerialize.toJson(response))
        }
    }

    fun sendPing(data: String?) {
        if (connect != null && connect.isOpen) {
            connect.send(data)
        }
    }
}