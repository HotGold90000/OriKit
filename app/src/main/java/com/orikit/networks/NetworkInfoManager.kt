package com.orikit.networks

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import androidx.fragment.app.FragmentActivity
import java.net.NetworkInterface

class NetworkInfoManager(private val context: Context) {

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    data class NetworkInfo(
        val ipAddress: String,
        val wifiName: String,
        val macAddress: String,
        val networkType: String,
        val isOnline: Boolean
    )

    fun getNetworkInfo(): NetworkInfo {
        return NetworkInfo(
            ipAddress = getLocalIpAddress(),
            wifiName = getWifiName(),
            macAddress = getMacAddressCompat(),
            networkType = getNetworkTypeCompat(),
            isOnline = getNetworkTypeCompat() != "无网络连接"
        )
    }

    fun getDeviceInfo(): Pair<String, String> {
        return Pair(Build.MODEL, Build.VERSION.RELEASE)
    }

    private fun getLocalIpAddress(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            } else {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo.ipAddress.toString()
            }
        } catch (e: Exception) {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val addrs = intf.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && !addr.isLinkLocalAddress && addr.isSiteLocalAddress) {
                            return addr.hostAddress
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            "无法获取"
        }
    }

    private fun getWifiName(): String {
        return try {
            val info = wifiManager.connectionInfo
            info.ssid.replace("\"", "").takeIf { it.isNotEmpty() } ?: "未连接WiFi"
        } catch (e: Exception) {
            "获取WiFi名称出错"
        }
    }

    private fun getMacAddressCompat(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiManager.connectionInfo.macAddress ?: "无法获取"
            } else {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo.macAddress ?: "无法获取"
            }
        } catch (e: Exception) {
            "受系统限制"
        }
    }

    private fun getNetworkTypeCompat(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val caps = connectivityManager.getNetworkCapabilities(network)

                when {
                    caps == null -> "无网络连接"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动数据"
                    else -> "其他网络类型"
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                when {
                    networkInfo == null -> "无网络连接"
                    networkInfo.type == ConnectivityManager.TYPE_WIFI -> "WiFi"
                    networkInfo.type == ConnectivityManager.TYPE_MOBILE -> "移动数据"
                    else -> "其他网络类型"
                }
            }
        } catch (e: Exception) {
            "无法检测网络类型"
        }
    }


    fun shareNetworkInfo(fragmentActivity: FragmentActivity) {
        val (model, version) = getDeviceInfo()
        val networkInfo = getNetworkInfo()

        val shareText = """
            设备信息:
            型号: $model 
            系统版本: $version 
            
            网络信息:
            IP地址: ${networkInfo.ipAddress}
            WiFi名称: ${networkInfo.wifiName}
            MAC地址: ${networkInfo.macAddress}
            网络类型: ${networkInfo.networkType}
        """.trimIndent()

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
//        startActivity(android.content.Intent.createChooser(shareIntent, "分享网络信息"))
        fragmentActivity.startActivity(android.content.Intent.createChooser(shareIntent, "分享网络信息"))
    }


}
