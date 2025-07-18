package com.orikitx.appinfo

import android.util.Log
import com.orikitx.FILTER_CONTENT
import com.orikitx.TAG

class ThreadUtil {
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