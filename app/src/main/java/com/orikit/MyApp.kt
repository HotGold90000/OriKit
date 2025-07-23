package com.orikit

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.orikitx.OriKit
import java.lang.reflect.InvocationTargetException

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // OriKit初始化
        OriKit.install(this)
    }

    //使用反射实现初始化
    private fun installOriKit() {
        try {
            // 使用反射加载OriKit类并调用install方法, install添加了@JvmStatic修饰
            val oriKitClass = Class.forName("com.orikitx.OriKit")
            val installMethod = oriKitClass.getMethod("install", Application::class.java)
            installMethod.invoke(null, this)

            //解决方案一
            // 1. 加载类
//            val oriKitClass = Class.forName("com.orikitx.OriKit")
//            // 2. 获取单例实例（INSTANCE）
//            val instance = oriKitClass.getDeclaredField("INSTANCE").get(null)
//            // 3. 获取 install 方法
//            val installMethod = oriKitClass.getMethod("install", Application::class.java)
//            // 4. 调用方法（传入 INSTANCE 作为 receiver）
//            installMethod.invoke(instance, this)

            //解决方案二
//            val oriKit = Class.forName("com.orikitx.OriKit")
//                .kotlin.objectInstance // 直接获取 Kotlin object 的实例
//
//            val installMethod = oriKit!!::class.members.first { it.name == "install" }
//            installMethod.call(oriKit, this)
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is ClassNotFoundException -> "OriKit class not found"
                is NoSuchMethodException -> "install method not found"
                is IllegalAccessException -> "cannot access method"
                is InvocationTargetException -> "method threw exception: ${e.cause?.message}"
                else -> "error: ${e.message}"
            }
            Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        // 成功提示
        Toast.makeText(this, "OriKit init success", Toast.LENGTH_SHORT).show()
    }

}
