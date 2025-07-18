package com.orikitx

import android.content.res.Resources
import android.os.Handler
import android.os.Looper

//class Utils {
//}

private val mGlobalHandler = Handler(Looper.getMainLooper())

fun getGlobalHandler() = mGlobalHandler


fun postMainThreadXDkit(actionTast: Runnable) {
    mGlobalHandler.post(actionTast)
}

fun px2dpOri(pxValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

fun dp2pxOri(dpValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Int?.px2dpOri(): Int = innerPx2dp(this)
fun Float?.px2dpOri(): Int = innerPx2dp(this)
fun Double?.px2dpOri(): Int = innerPx2dp(this)
fun Long?.px2dpOri(): Int = innerPx2dp(this)

private fun innerPx2dp(pxValue: Any?): Int {
    if (pxValue is Int || pxValue is Float || pxValue is Double || pxValue is Long) {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue.toString().toDouble() / scale + 0.5f).toInt()
    } else {
        return -1
    }
}