package com.orikit

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.orikitx.OriKit

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // OriKit初始化
        OriKit.install(this)

    }
}
