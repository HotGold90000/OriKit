package com.orikitx.view

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.orikitx.appinfo.ActivityStack
import java.lang.ref.WeakReference


class RbToast private constructor() {

    private var toastView: View? = null
    private var config = Config()

    companion object {
        private var sApplication: Application? = null
        private var mActivityStack: ActivityStack? = null
        private var instance: WeakReference<RbToast>? = null

        fun init(application: Application) {
            sApplication = application
            mActivityStack = ActivityStack.register(application)
        }

        @JvmStatic
        fun create(): RbToast {
            val existing = instance?.get()
            return existing ?: RbToast().also {
                instance = WeakReference(it)
            }.apply {
                config = Config() // 每次新建都重置Toast配置
            }
        }
    }

    @JvmOverloads
    fun show(
        content: String,
        @DrawableRes icon: Int = config.iconRes,
        isSingleLine: Boolean = config.isSingleLine,
        maxLines: Int = config.maxLines,
        @ColorInt backgroundColor: Int = config.bgColor,
        gravity: Int = config.gravity,
        @ColorInt textColor: Int = config.textColor,
        textSizeSp: Float = config.textSizeSp,
        durationMs: Int = config.durationMs,
        cornerRadius: Float = config.cornerRadius,
        horizontalPadding: Float = config.horizontalPadding,
        verticalPadding: Float = config.verticalPadding
    ) {
        cancel()

        val msg = Message.obtain()
        msg.what = TYPE_SHOW
        msg.obj = Config(
            content = content,
            iconRes = icon,
            isSingleLine = isSingleLine,
            maxLines = maxLines,
            bgColor = backgroundColor,
            gravity = gravity,
            textColor = textColor,
            textSizeSp = textSizeSp,
            durationMs = durationMs,
            cornerRadius = cornerRadius,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding
        )
        handler.sendMessage(msg)
    }

    @JvmOverloads
    private fun showTask(
        content: String,
        @DrawableRes icon: Int = config.iconRes,
        isSingleLine: Boolean = config.isSingleLine,
        maxLines: Int = config.maxLines,
        @ColorInt backgroundColor: Int = config.bgColor,
        gravity: Int = config.gravity,
        @ColorInt textColor: Int = config.textColor,
        textSizeSp: Float = config.textSizeSp,
        durationMs: Int = config.durationMs,
        cornerRadius: Float = config.cornerRadius,
        horizontalPadding: Float = config.horizontalPadding,
        verticalPadding: Float = config.verticalPadding
    ) {
        cancel()
        val resumedActivity = mActivityStack?.foregroundActivity
        if (resumedActivity != null) {
            val rootView = resumedActivity.findViewById<ViewGroup>(android.R.id.content)
            toastView = buildToastView(
                resumedActivity.applicationContext, // 使用Application Context
                content = content,
                icon = icon,
                isSingleLine = isSingleLine,
                maxLines = maxLines,
                bgColor = backgroundColor,
                textColor = textColor,
                textSizeSp = textSizeSp,
                cornerRadius = cornerRadius,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding
            ).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.gravity = gravity
                    setMargins(20f.dp2px(), 0, 20f.dp2px(), 0)
                }
            }
            rootView.addView(toastView)
            scheduleDismiss(durationMs)
        } else {
            sApplication?.baseContext?.let {
                Toast.makeText(it, content, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildToastView(
        context: Context,
        content: String,
        @DrawableRes icon: Int,
        isSingleLine: Boolean,
        maxLines: Int,
        @ColorInt bgColor: Int,
        @ColorInt textColor: Int,
        textSizeSp: Float,
        cornerRadius: Float,
        horizontalPadding: Float,
        verticalPadding: Float
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = createBackgroundDrawable(bgColor, cornerRadius.dp2px().toFloat())
            val horizontalPx = (horizontalPadding.dp2px()).toInt()
            val verticalPx = (verticalPadding.dp2px()).toInt()
            setPadding(horizontalPx, verticalPx, horizontalPx, verticalPx)

            if (icon != 0) {
                addView(ImageView(context).apply {
                    setImageResource(icon)
                    layoutParams = LinearLayout.LayoutParams(16.dp2px(), 16.dp2px()).apply {
                        marginEnd = 6.dp2px()
                    }
                })
            }

            addView(TextView(context).apply {
                this.text = content
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)

                if (isSingleLine) {
                    setMaxLines(1)
                } else {
                    setMaxLines(maxLines)
                }
                ellipsize = TextUtils.TruncateAt.END

                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    weight = 1f
                }
            })
        }
    }

    private fun createBackgroundDrawable(@ColorInt color: Int, cornerRadiusPx: Float): Drawable {
        return GradientDrawable().apply {
            this.cornerRadius = cornerRadiusPx
            setColor(color)
        }
    }

    private fun scheduleDismiss(delay: Int) {
        handler.postDelayed({ dismiss() }, delay.toLong())
    }

    fun dismiss() {
        removeToast()
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        removeToast()
    }

    private fun removeToast() {
        toastView?.let {
            it.animate()?.cancel()
            try {
                (it.parent as? ViewGroup)?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        toastView = null
    }

    // 链式配置方法
    fun setIcon(@DrawableRes iconRes: Int) = apply { config.iconRes = iconRes }
    fun setSingleLine(singleLine: Boolean) = apply { config.isSingleLine = singleLine }
    fun setMaxLines(maxLines: Int) = apply { config.maxLines = maxLines }
    fun setBackgroundColor(@ColorInt color: Int) = apply { config.bgColor = color }
    fun setTextColor(@ColorInt color: Int) = apply { config.textColor = color }
    fun setTextSize(sp: Float) = apply { config.textSizeSp = sp }
    fun setGravity(gravity: Int) = apply { config.gravity = gravity }
    fun setDuration(ms: Int) = apply { config.durationMs = ms }
    fun setCornerRadius(radiusDp: Float) = apply { config.cornerRadius = radiusDp }
    fun setHorizontalPadding(paddingDp: Float) = apply { config.horizontalPadding = paddingDp }
    fun setVerticalPadding(paddingDp: Float) = apply { config.verticalPadding = paddingDp }

    private data class Config(
        var content: String = "",
        @DrawableRes var iconRes: Int = 0,
        var isSingleLine: Boolean = false,
        var maxLines: Int = 2,
        @ColorInt var bgColor: Int = 0XF20D0E10.toInt(),
        var gravity: Int = Gravity.CENTER,
        @ColorInt var textColor: Int = 0XFFBFC2C3.toInt(),
        var textSizeSp: Float = 14f,
        var durationMs: Int = 2000,
        var cornerRadius: Float = 10f,
        var horizontalPadding: Float = 20f,
        var verticalPadding: Float = 16f
    )

    private class SafeHandler(
        looper: Looper,
        private val toast: WeakReference<RbToast>
    ) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val toastInstance = toast.get() ?: return
            val configInstance = msg.obj as? Config ?: return
            when (msg.what) {
                TYPE_SHOW -> {
                    try {
                        toastInstance.showTask(
                            content = configInstance.content,
                            icon = configInstance.iconRes,
                            isSingleLine = configInstance.isSingleLine,
                            maxLines = configInstance.maxLines,
                            backgroundColor = configInstance.bgColor,
                            textColor = configInstance.textColor,
                            textSizeSp = configInstance.textSizeSp,
                            cornerRadius = configInstance.cornerRadius,
                            horizontalPadding = configInstance.horizontalPadding,
                            verticalPadding = configInstance.verticalPadding
                        )
                    } catch (e: Exception) {
//                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private val handler = SafeHandler(Looper.getMainLooper(), WeakReference(this))
}

private fun Float.dp2px(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

private fun Int.dp2px(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

private const val TYPE_SHOW = 1