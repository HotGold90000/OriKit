package com.orikitx.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.marginLeft
import com.orikitx.DELAY_TIME
import com.orikitx.TAG
import com.orikitx.appinfo.getApplicationByReflect
import com.orikitx.dp2pxOri
import com.orikitx.getGlobalHandler
import com.orikitx.logs.XDLog
import com.orikitx.px2dpOri
import java.lang.ref.WeakReference

class OriView {
    fun initOriViewTool(activity: Activity) {
        val actWeakRef = WeakReference<Activity>(activity)
        kotlin.runCatching {
            val curActiviey = actWeakRef.get()!!

//                val RlParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            val RlParams = RelativeLayout.LayoutParams(dp2pxOri(100f), dp2pxOri(50f))
            RlParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            RlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            RlParams.topMargin = dp2pxOri(15f)

            val RL = RelativeLayout(activity).apply {
                this.x = dp2pxOri(250f).toFloat()
                this.y = dp2pxOri(50f).toFloat()
                this.setBackgroundColor(Color.BLUE)
                this.id = View.generateViewId()
                this.layoutParams = RlParams
            }

            val tv = TextView(curActiviey).apply {
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                layoutParams.topMargin = dp2pxOri(15f) //设置布局属性：view的上边的外部留出10px的距离。（注意这里不是dp）
                this.layoutParams = layoutParams
                this.setPadding(dp2pxOri(5f), dp2pxOri(5f), dp2pxOri(5f), dp2pxOri(5f))
                this.setBackgroundColor(Color.RED)  //设置纯红色背景
                this.setTextColor(Color.GRAY)
                this.text = "FloatTool"
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                val tvId = View.generateViewId()
                this.id =
                    tvId  //设置view的id，使用这个方法设置的id不能与现存的所有的view的id重合，且也不能设置为0，这里设置以从1开始累加的方法设置id虽然不出错，但会有重合的风险，具体解决方案请看这篇帖子：https://blog.csdn.net/s13383754499/article/details/81383466
            }
            RL.addView(tv)
            val decorViewContainer = curActiviey.window.decorView as FrameLayout
            decorViewContainer.addView(RL)
        }.onFailure {
            it.message?.XDLog()
        }

    }
    
}


/**
 * print size info of target View
 */
fun View.getAllSizesXDkit(extraInfo: Boolean = false) {
    try {
        getGlobalHandler().postDelayed({
            printInfos(this, extraInfo)
        }, DELAY_TIME)
    } catch (e: Exception) {
        Log.d(TAG, "$TAG: Run Error >>> e = ${e.message}")
    }
}

private fun printInfos(curView: View, extraInfo: Boolean) {
    /**
     * Parent View
     */
    curView.parent?.let { parentView ->
        if (parentView is View) {
            Log.d(
                TAG,
                "$TAG: [Parent]-------------------- Class : ${parentView.javaClass.simpleName} --------------------"
            )
            Log.d(TAG, "$TAG:     {x, y} : [${px2dpOri(parentView.x)}, ${px2dpOri(parentView.y)}]")
            Log.d(
                TAG,
                "$TAG:     {width, height} : [${px2dpOri(parentView.width.toFloat())}, ${
                    px2dpOri(parentView.height.toFloat())
                }]"
            )
            Log.d(
                TAG,
                "$TAG:     Padding > {left, top, right, bottom} : [${px2dpOri(parentView.paddingLeft.toFloat())}, ${
                    px2dpOri(parentView.paddingTop.toFloat())
                }, ${px2dpOri(parentView.paddingRight.toFloat())}, ${px2dpOri(parentView.paddingBottom.toFloat())}]"
            )
            Log.d(
                TAG,
                "$TAG:     Margin  > {left, top, right, bottom} : [${px2dpOri(parentView.marginLeft.toFloat())}, ${
                    px2dpOri(parentView.marginLeft.toFloat())
                }, ${px2dpOri(parentView.marginLeft.toFloat())}, ${px2dpOri(parentView.marginLeft.toFloat())}]"
            )
        }
    }

    /**
     * Current View
     */
    Log.d(
        TAG,
        "$TAG: [Current]------------------- Class : ${curView.javaClass.simpleName} --------------------"
    )
    Log.d(TAG, "$TAG:     {x, y} : [${px2dpOri(curView.x)}, ${px2dpOri(curView.y)}]")
    Log.d(
        TAG,
        "$TAG:     {width, height} : [${px2dpOri(curView.width.toFloat())}, ${px2dpOri(curView.height.toFloat())}]"
    )
    Log.d(
        TAG,
        "$TAG:     Padding > {left, top, right, bottom} : [${px2dpOri(curView.paddingLeft.toFloat())}, ${
            px2dpOri(curView.paddingTop.toFloat())
        }, ${px2dpOri(curView.paddingRight.toFloat())}, ${px2dpOri(curView.paddingBottom.toFloat())}]"
    )
    Log.d(
        TAG,
        "$TAG:     Margin  > {left, top, right, bottom} : [${px2dpOri(curView.marginLeft.toFloat())}, ${
            px2dpOri(curView.marginLeft.toFloat())
        }, ${px2dpOri(curView.marginLeft.toFloat())}, ${px2dpOri(curView.marginLeft.toFloat())}]"
    )

    /**
     * Children View
     */
    if (curView is ViewGroup) {
        curView.children.forEachIndexed { index, childView ->
            Log.d(
                TAG,
                "$TAG: [Child, Index : ${index}]----------- Class : ${childView.javaClass.simpleName} --------------------"
            )
            Log.d(TAG, "$TAG:     {x, y} : [${px2dpOri(childView.x)}, ${px2dpOri(childView.y)}]")
            Log.d(
                TAG,
                "$TAG:     {width, height} : [${px2dpOri(childView.width.toFloat())}, ${px2dpOri(childView.height.toFloat())}]"
            )
            Log.d(
                TAG,
                "$TAG:     Padding > {left, top, right, bottom} : [${px2dpOri(childView.paddingLeft.toFloat())}, ${
                    px2dpOri(childView.paddingTop.toFloat())
                }, ${px2dpOri(childView.paddingRight.toFloat())}, ${px2dpOri(childView.paddingBottom.toFloat())}]"
            )
            Log.d(
                TAG,
                "$TAG:     Margin  > {left, top, right, bottom} : [${px2dpOri(childView.marginLeft.toFloat())}, ${
                    px2dpOri(childView.marginLeft.toFloat())
                }, ${px2dpOri(childView.marginLeft.toFloat())}, ${px2dpOri(childView.marginLeft.toFloat())}]"
            )
        }
    }

    /**
     * Extra View
     */
    if (extraInfo) {
        Log.d(
            TAG,
            "$TAG: [Extras]------------------------------------------------------------------------"
        )
        Log.d(
            TAG, "$TAG:     Screen > {width, height}:[${px2dpOri(getScreenWidth().toFloat())}, ${
                px2dpOri(
                    getScreenHeight().toFloat()
                )
            }]"
        )
        Log.d(TAG, "$TAG:     StatusBarHeight:${getStatusBarHeightXDkit()}")
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





//    companion object {
//        @JvmStatic
//        fun initOriViewTool(activity: Activity) {
//            val actWeakRef = WeakReference<Activity>(activity)
//            kotlin.runCatching {
//                val curActiviey = actWeakRef.get()!!
//
////                val RlParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//                val RlParams = RelativeLayout.LayoutParams(dp2pxOri(100f), dp2pxOri(50f))
//                RlParams.addRule(RelativeLayout.ALIGN_PARENT_START)
//                RlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//                RlParams.topMargin = dp2pxOri(15f)
//
//                val RL = RelativeLayout(activity).apply {
//                    this.x = dp2pxOri(250f).toFloat()
//                    this.y = dp2pxOri(50f).toFloat()
//                    this.setBackgroundColor(Color.BLUE)
//                    this.id = View.generateViewId()
//                    this.layoutParams = RlParams
//                }
//
//                val tv = TextView(curActiviey).apply {
//                    val layoutParams = RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.WRAP_CONTENT,
//                        RelativeLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//                    layoutParams.topMargin = dp2pxOri(15f) //设置布局属性：view的上边的外部留出10px的距离。（注意这里不是dp）
//                    this.layoutParams = layoutParams
//                    this.setPadding(dp2pxOri(5f), dp2pxOri(5f), dp2pxOri(5f), dp2pxOri(5f))
//                    this.setBackgroundColor(Color.RED)  //设置纯红色背景
//                    this.setTextColor(Color.GRAY)
//                    this.text = "FloatTool"
//                    this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
//                    val tvId = View.generateViewId()
//                    this.id =
//                        tvId  //设置view的id，使用这个方法设置的id不能与现存的所有的view的id重合，且也不能设置为0，这里设置以从1开始累加的方法设置id虽然不出错，但会有重合的风险，具体解决方案请看这篇帖子：https://blog.csdn.net/s13383754499/article/details/81383466
//                }
//                RL.addView(tv)
//                val decorViewContainer = curActiviey.window.decorView as FrameLayout
//                decorViewContainer.addView(RL)
//            }.onFailure {
//                it.message?.logXDkit()
//            }
//
//        }
//    }