package com.orikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orikitx.OriKit
import com.orikitx.view.RbToast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //测试消息接收
        testMsgLink()

    }

    //测试消息接收
    private fun testMsgLink() {
        OriKit.setMsgObserve {
            RbToast.create().show(it)
        }
    }

}