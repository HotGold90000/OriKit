package com.orikitx.logs

import android.util.Log

class OriLog {
}

private const val TAG = "Testlog"

fun String?.XDLog(tag: String = TAG) {
    this?.let {
        Log.d(tag, it)
    }
}