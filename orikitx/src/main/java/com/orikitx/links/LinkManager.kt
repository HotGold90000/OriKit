package com.orikitx.links

import android.app.Application
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.orikitx.logs.XDLog
import com.orikitx.view.RbToast
import java.io.IOException
import java.util.concurrent.Executors

class LinkManager constructor(val application: Application) {

    private var webSocketServer: OriWebSocketServer? = null
    private var webSocketCallback: ((String) -> Unit)? = null

    //    @JvmStatic
    fun initServer() {
        if (webSocketServer == null) {
            webSocketServer = OriWebSocketServer(PORT) // 启动 WebSocket 服务器
        }

        try {
            webSocketServer?.start(SOCKET_TIMEOUT) //到达超时时候后报错：java.net.SocketTimeoutException: Read timed out
            webSocketServer?.newMsg?.observeForever {
                webSocketCallback?.invoke(it)
            }
            "WebSocket Server started".XDLog()
        } catch (e: IOException) {
//            e.printStackTrace()
            "WebSocket Server failed to start: ${e.message}".XDLog()
            RbToast.create().show("WebSocket Server failed to start: ${e.message}")
        }
        printIpAddress()
    }


    /**
     * vivo S17的logcat常常出现打印异常 尼玛! 我还要在这里实现一个log打印输出控制台
     */
    fun sendMsg(msgToSend: String) {
        Executors.newSingleThreadExecutor().submit {
            //Note:send()操作必须放在子线程，否则报错：android.os.NetworkOnMainThreadException
            webSocketServer?.socketInstance?.send("Testlot: ${msgToSend}")
        }
    }

    fun setMsgObserve(callback: (String) -> Unit) {
        webSocketCallback = callback
    }

    private fun getIPAddress(): String {
        val wifiManager =
            application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }

    // 获取并输出 IP 地址
    private fun printIpAddress() {
        "IP Address: ${getIPAddress()}".XDLog()
    }
    
}

private const val SOCKET_TIMEOUT = 500 * 1000
private const val PORT = 8080