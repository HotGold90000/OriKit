package com.orikitx

import android.app.Application
import com.orikitx.links.LinkManager
import com.orikitx.view.RbToast

object OriKit {

    private var mApplication: Application? = null
    private var linkManager: LinkManager? = null

    @JvmStatic
    fun install(app: Application) {
        mApplication = app
        mApplication?.let {
            linkManager = LinkManager(it).apply {
                initServer()
            }
            RbToast.init(it)
        }
    }

    fun setMsgObserve(callback: (String) -> Unit) {
        linkManager?.setMsgObserve(callback)
    }

    fun sendMsg(msgToSend: String) {
        linkManager?.sendMsg(msgToSend)
    }
    /**
     * 获取设备IP地址
     * @return String
     */
    fun getDeviceIp(): String {
        return linkManager?.getIPAddress() ?: "ip address not available"
    }

}

const val TAG = "Testlog"
const val DELAY_TIME = 500L
const val FILTER_CONTENT = "room"

//import kotlin.reflect.full.declaredMemberProperties
//import kotlin.reflect.jvm.isAccessible

/**
 * implementation 'org.nanohttpd:nanohttpd-websocket:2.3.1'
 * implementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
 */



/**
 * print all fields of instance
 */
//fun Any.logFieldsXDkit() {
//    try {
//        //修复异常：getFiledsInfoReflect执行耗时过长，影响app执行，所以放到子线程执行;
//        // 而且因为这里不是报错，所以不会走到catch
//        Executors.newSingleThreadExecutor().submit {
//            getFiledsInfoReflect(this)
//        }
//    } catch (e : Exception) {
//        Log.d(TAG, "$TAG: logFieldsXDkit >>> ErrorMsg = ${e.message}")
//    }
//}
//
//private fun getFiledsInfoReflect(targetInstance:Any) {
//    val kClass = targetInstance::class
//    kClass.declaredMemberProperties.forEach { property ->
//        if (!property.isAccessible) {
//            property.isAccessible = true // 确保可以访问私有字段
//        }
//        val propertyName = property.name
//        val propertyValue = property.call(targetInstance)
//        Log.d(TAG, "$TAG: ${kClass.simpleName}: $propertyName = $propertyValue")
//    }
//}

