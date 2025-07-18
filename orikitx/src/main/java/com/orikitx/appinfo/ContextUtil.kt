package com.orikitx.appinfo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import java.lang.reflect.InvocationTargetException


fun getOriContext(): Context? {
    return getApplicationByReflect()?.baseContext
}

fun getApplicationByReflect(): Application? {
    try {
        @SuppressLint("PrivateApi")
        val activityThread = Class.forName("android.app.ActivityThread")
        val thread = activityThread.getMethod("currentActivityThread").invoke(null)
        val app =
            activityThread.getMethod("getApplication").invoke(thread) ?: throw NullPointerException(
                "u should init first"
            )
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


fun getAppName(): String? {
    val applicationInfo = getOriContext()?.applicationInfo ?: return null
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) {
        applicationInfo.nonLocalizedLabel.toString()
    } else {
        getOriContext()?.getString(stringId)
    }
}

fun isDebug(): Boolean {
    val flag = getApplicationByReflect()?.applicationInfo?.flags ?: 0
    val isDebuggable = (flag and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    return isDebuggable
}