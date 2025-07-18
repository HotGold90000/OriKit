package com.orikitx.links

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import fi.iki.elonen.NanoWSD
import java.io.IOException

class OriWebSocketServer(port: Int) : NanoWSD(port) {

    lateinit var socketInstance: MyWebSocket

    val newMsg = MutableLiveData<String>()

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        socketInstance = MyWebSocket(handshake) { msg ->
            newMsg.value = msg
        }
        return socketInstance
    }

    class MyWebSocket(handshakeRequest: IHTTPSession, val callback: (String) -> Unit) :
        WebSocket(handshakeRequest) {

        val mHandler: Handler by lazy {
            Handler(Looper.getMainLooper())
        }

        @Throws(IOException::class)
        override fun onOpen() {
            Log.d("Testlog", "WebSocket Opened")
            send("Hi 新东方 连接建立成功")
        }

        override fun onClose(
            code: WebSocketFrame.CloseCode,
            reason: String,
            initiatedByRemote: Boolean
        ) {
            Log.d("Testlog", "WebSocket Closed: $reason")
        }

        @Throws(IOException::class)
        override fun onMessage(message: WebSocketFrame) {
            Log.d("Testlog", "Message Received: ${message.textPayload}")
            send("Hi 已收到 -> ${message.textPayload}")

            mHandler.post {
                callback.invoke(message.textPayload)
            }

        }

        override fun onPong(pong: WebSocketFrame) {
            // Handle pong
        }

        override fun onException(exception: IOException) {
            exception.printStackTrace()
        }
    }
}