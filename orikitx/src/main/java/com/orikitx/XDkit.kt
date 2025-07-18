package com.orikitx

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.marginLeft
import androidx.lifecycle.MutableLiveData
import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Executors
//import kotlin.reflect.full.declaredMemberProperties
//import kotlin.reflect.jvm.isAccessible

/**
 * implementation 'org.nanohttpd:nanohttpd-websocket:2.3.1'
 * implementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
 */

const val TAG = "Testlog"
const val DELAY_TIME = 500L
const val FILTER_CONTENT = "room"

class XDkit {
    companion object {

        @SuppressLint("StaticFieldLeak")
        var tv: TextView? = null
//        @SuppressLint("StaticFieldLeak")
//        var containerView : FrameLayout? = null

        @JvmStatic
        fun initTool(activity: Activity) {
            val actWeakRef = WeakReference<Activity>(activity)
            kotlin.runCatching {
                val curActiviey = actWeakRef.get()!!

//                val RlParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                val RlParams = RelativeLayout.LayoutParams(dp2px(100f), dp2px(50f))
                RlParams.addRule(RelativeLayout.ALIGN_PARENT_START)
                RlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                RlParams.topMargin = dp2px(15f)

                val RL = RelativeLayout(activity).apply {
                    this.x = dp2px(250f).toFloat()
                    this.y = dp2px(50f).toFloat()
                    this.setBackgroundColor(Color.BLUE)
                    this.id = View.generateViewId()
                    this.layoutParams = RlParams
                }

                tv = TextView(curActiviey).apply {
                    val layoutParams =  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    layoutParams.topMargin = dp2px(15f) //设置布局属性：view的上边的外部留出10px的距离。（注意这里不是dp）
                    this.layoutParams = layoutParams
                    this.setPadding(dp2px(5f), dp2px(5f), dp2px(5f), dp2px(5f))
                    this.setBackgroundColor(Color.RED)  //设置纯红色背景
                    this.setTextColor(Color.GRAY)
                    this.text = "FloatTool"
                    this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    val tvId = View.generateViewId()
                    this.id = tvId  //设置view的id，使用这个方法设置的id不能与现存的所有的view的id重合，且也不能设置为0，这里设置以从1开始累加的方法设置id虽然不出错，但会有重合的风险，具体解决方案请看这篇帖子：https://blog.csdn.net/s13383754499/article/details/81383466
                }
                RL.addView(tv)
                val decorViewContainer = curActiviey.window.decorView as FrameLayout
                decorViewContainer.addView(RL)
            }.onFailure {
                it.message?.logXDkit()
            }

        }


        private var webSocketServer: MyWebSocketServer? = null
        private var webSocketCallback: ((String) -> Unit)? = null

        /**
         * vivo S17的logcat常常出现打印异常 尼玛! 我还要在这里实现一个log打印输出控制台
         */
        fun sendLogMsg(msgToSend:String) {
            Executors.newSingleThreadExecutor().submit {
                //Note:send()操作必须放在子线程，否则报错：android.os.NetworkOnMainThreadException
                webSocketServer?.socketInstance?.send("Testlot: ${msgToSend}")
            }
        }

        fun setMsgCallback(callback : (String) -> Unit) {
            webSocketCallback = callback
        }



        @JvmStatic
        fun startWebSocketServer() {
            if (webSocketServer == null) {
                webSocketServer = MyWebSocketServer(PORT) // 启动 WebSocket 服务器
            }

            try {
                webSocketServer?.start(SOCKET_TIMEOUT) //到达超时时候后报错：java.net.SocketTimeoutException: Read timed out
                webSocketServer?.newMsg?.observeForever {
                    webSocketCallback?.invoke(it)
                }
                Log.d(TAG, "WebSocket Server started")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "WebSocket Server failed to start: ${e.message}")
            }
            printIpAddress()
        }

        // 获取并输出 IP 地址
        private fun printIpAddress() {
            getApplicationByReflect()?.baseContext?.let {
                val ipAddress = getIPAddress(it)
                Log.d(TAG, "IP Address: $ipAddress")
            }
        }

        fun getIPAddress(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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


    }
}

private val mHandler = Handler(Looper.getMainLooper())

fun postMainThreadXDkit(actionTast: Runnable){
    mHandler.post(actionTast)
}


/**
 * print String
 */
fun String.logXDkit() {
    Log.d(TAG, "$TAG: ${this}")
}

/**
 * print stack of method
 */
fun printStackXDkit() {
    Thread.currentThread().stackTrace.forEachIndexed { index, stackTraceElement ->
        if (stackTraceElement.toString().contains(FILTER_CONTENT)) {
            Log.d(TAG, "$TAG: $stackTraceElement")
        }
    }
}

/**
 * display toast of String
 */
fun String.toastXDkit() {
    Toast.makeText(getApplicationByReflect()?.baseContext,"$TAG: $this}",Toast.LENGTH_SHORT).show()
}

/**
 * print size info of target View
 */
fun View.getAllSizesXDkit(extraInfo: Boolean = false) {
    try {
        this?.postDelayed({
            printInfos(this, extraInfo)
        }, DELAY_TIME)
    } catch (e : Exception) {
        Log.d(TAG, "$TAG: Run Error >>> e = ${e.message}")
    }
}

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

private fun printInfos(curView : View, extraInfo: Boolean) {
    /**
     * Parent View
     */
    curView.parent?.let { parentView ->
        if (parentView is View) {
            Log.d(TAG, "$TAG: [Parent]-------------------- Class : ${parentView.javaClass.simpleName} --------------------")
            Log.d(TAG, "$TAG:     {x, y} : [${px2dp(parentView.x)}, ${px2dp(parentView.y)}]")
            Log.d(TAG, "$TAG:     {width, height} : [${px2dp(parentView.width.toFloat())}, ${px2dp(parentView.height.toFloat())}]")
            Log.d(TAG, "$TAG:     Padding > {left, top, right, bottom} : [${px2dp(parentView.paddingLeft.toFloat())}, ${px2dp(parentView.paddingTop.toFloat())}, ${px2dp(parentView.paddingRight.toFloat())}, ${px2dp(parentView.paddingBottom.toFloat())}]")
            Log.d(TAG, "$TAG:     Margin  > {left, top, right, bottom} : [${px2dp(parentView.marginLeft.toFloat())}, ${px2dp(parentView.marginLeft.toFloat())}, ${px2dp(parentView.marginLeft.toFloat())}, ${px2dp(parentView.marginLeft.toFloat())}]")
        }
    }

    /**
     * Current View
     */
    Log.d(TAG, "$TAG: [Current]------------------- Class : ${curView.javaClass.simpleName} --------------------")
    Log.d(TAG, "$TAG:     {x, y} : [${px2dp(curView.x)}, ${px2dp(curView.y)}]")
    Log.d(TAG, "$TAG:     {width, height} : [${px2dp(curView.width.toFloat())}, ${px2dp(curView.height.toFloat())}]")
    Log.d(TAG, "$TAG:     Padding > {left, top, right, bottom} : [${px2dp(curView.paddingLeft.toFloat())}, ${px2dp(curView.paddingTop.toFloat())}, ${px2dp(curView.paddingRight.toFloat())}, ${px2dp(curView.paddingBottom.toFloat())}]")
    Log.d(TAG, "$TAG:     Margin  > {left, top, right, bottom} : [${px2dp(curView.marginLeft.toFloat())}, ${px2dp(curView.marginLeft.toFloat())}, ${px2dp(curView.marginLeft.toFloat())}, ${px2dp(curView.marginLeft.toFloat())}]")

    /**
     * Children View
     */
    if (curView is ViewGroup) {
        curView.children.forEachIndexed { index, childView ->
            Log.d(TAG, "$TAG: [Child, Index : ${index}]----------- Class : ${childView.javaClass.simpleName} --------------------")
            Log.d(TAG, "$TAG:     {x, y} : [${px2dp(childView.x)}, ${px2dp(childView.y)}]")
            Log.d(TAG, "$TAG:     {width, height} : [${px2dp(childView.width.toFloat())}, ${px2dp(childView.height.toFloat())}]")
            Log.d(TAG, "$TAG:     Padding > {left, top, right, bottom} : [${px2dp(childView.paddingLeft.toFloat())}, ${px2dp(childView.paddingTop.toFloat())}, ${px2dp(childView.paddingRight.toFloat())}, ${px2dp(childView.paddingBottom.toFloat())}]")
            Log.d(TAG, "$TAG:     Margin  > {left, top, right, bottom} : [${px2dp(childView.marginLeft.toFloat())}, ${px2dp(childView.marginLeft.toFloat())}, ${px2dp(childView.marginLeft.toFloat())}, ${px2dp(childView.marginLeft.toFloat())}]")
        }
    }

    /**
     * Extra View
     */
    if (extraInfo) {
        Log.d(TAG, "$TAG: [Extras]------------------------------------------------------------------------")
        Log.d(
            TAG, "$TAG:     Screen > {width, height}:[${px2dp(getScreenWidth().toFloat())}, ${
                px2dp(
                    getScreenHeight().toFloat())
            }]")
        Log.d(TAG, "$TAG:     StatusBarHeight:${getStatusBarHeightXDkit()}")
    }
}


private fun getAppName(): String? {
    val applicationInfo = getContext()?.applicationInfo ?: return null
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) {
        applicationInfo.nonLocalizedLabel.toString()
    } else {
        getContext()?.getString(stringId)
    }
}


/**
 * 获取状态栏高度
 */
private fun getStatusBarHeightXDkit(): Int {
    val resources = Resources.getSystem()
    var result = 0 //获取状态栏高度的资源id
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

private fun getScreenWidth(): Int {
    val wm = getApplicationByReflect()?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        wm.defaultDisplay.getRealSize(point)
    } else {
        wm.defaultDisplay.getSize(point)
    }
    return point.x
}


private fun getScreenHeight(): Int {
    val wm = getApplicationByReflect()?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        wm.defaultDisplay.getRealSize(point)
    } else {
        wm.defaultDisplay.getSize(point)
    }
    return point.y
}

private fun px2dp(pxValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

private fun dp2px(dpValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}


fun Int?.px2dpXDkit(): Int = innerPx2dp(this)
fun Float?.px2dpXDkit(): Int = innerPx2dp(this)
fun Double?.px2dpXDkit(): Int = innerPx2dp(this)
fun Long?.px2dpXDkit(): Int = innerPx2dp(this)

private fun innerPx2dp(pxValue: Any?): Int {
    if (pxValue is Int || pxValue is Float || pxValue is Double|| pxValue is Long) {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue.toString().toDouble() / scale + 0.5f).toInt()
    } else {
        return -1
    }
}


private fun isDebug() :Boolean {
    val flag = getApplicationByReflect()?.applicationInfo?.flags ?: 0
    val isDebuggable = (flag and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (isDebuggable) {
        // 当前是debug环境
        return true
    } else {
        // 当前是release环境
        return false
    }
}


private fun getContext() :Context? {
    return getApplicationByReflect()?.baseContext
}

private fun getApplicationByReflect(): Application? {
    try {
        @SuppressLint("PrivateApi")
        val activityThread = Class.forName("android.app.ActivityThread")
        val thread = activityThread.getMethod("currentActivityThread").invoke(null)
        val app = activityThread.getMethod("getApplication").invoke(thread) ?: throw NullPointerException("u should init first")
        return app as Application
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }
    throw NullPointerException("u should init first")
}



class MyWebSocketServer(port: Int) : NanoWSD(port) {

    lateinit var socketInstance: MyWebSocket

    val newMsg = MutableLiveData<String>()

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        socketInstance = MyWebSocket(handshake) { msg ->
            newMsg.value = msg
        }
        return socketInstance
    }

    class MyWebSocket(handshakeRequest: IHTTPSession, val callback:(String) -> Unit) : WebSocket(handshakeRequest) {

        val mHandler:Handler by lazy {
            Handler(Looper.getMainLooper())
        }

        @Throws(IOException::class)
        override fun onOpen() {
            Log.d("Testlog", "WebSocket Opened")
            send("Hi 新东方 连接建立成功")
        }

        override fun onClose(code: WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
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


private const val SOCKET_TIMEOUT = 500 * 1000
private const val PORT = 8080