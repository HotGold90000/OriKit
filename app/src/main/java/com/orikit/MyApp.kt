package com.orikit

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

// 分离观察者逻辑
class AppLifecycleObserver : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
//        Log.d("Testlog", "App进入前台 (Application级别)")
    }

    override fun onStop(owner: LifecycleOwner) {
//        Log.d("Testlog", "App退到后台 (Application级别)")
    }
}
