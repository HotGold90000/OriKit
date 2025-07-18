package com.orikitx.appinfo

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.util.ArrayDeque
import java.util.Stack
import java.util.concurrent.CopyOnWriteArrayList


class MonitorForeAndBackTracker : Application.ActivityLifecycleCallbacks {

    private val activities = ArrayDeque<Activity>()

    fun getActivities() = activities

    private val activityStack = Stack<Activity>()

    interface Listener {
        fun onBecameForeground()
        fun onBecameBackground()
    }

    var isForeground = false
        private set
    private var paused = true
    private val handler = Handler()
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()
    private var check: Runnable? = null
    val isBackground: Boolean
        get() = !isForeground

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    override fun onActivityResumed(activity: Activity) {
        activities.offer(activity)
        paused = false
        val wasBackground = !isForeground
        isForeground = true
        if (check != null) {
            handler.removeCallbacks(check!!)
        }
        if (wasBackground) {
            for (l in listeners) {
                try {
                    l.onBecameForeground()
                } catch (exc: Exception) {
                    Log.e(TAG, "Listener threw exception!", exc)
                }
            }
        } else {
            Log.i(TAG, "still foreground")
        }
    }

    override fun onActivityPaused(activity: Activity) {
        activities.poll()
        paused = true
        if (check != null) {
            handler.removeCallbacks(check!!)
        }
        handler.postDelayed(Runnable {
            if (isForeground && paused) {
                isForeground = false
                for (l in listeners) {
                    try {
                        l.onBecameBackground()
//                        Logger.e(TAG + activities.size)
                    } catch (exc: Exception) {
//                        Logger.e(
//                            TAG,
//                            "Listener threw exception!"
//                        )
                        activities.poll()
//                        Logger.e(TAG + activities.size)
                    }
                }
            } else {
//                Logger.i("still foreground")
            }
        }.also { check = it }, CHECK_DELAY)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityStack.add(activity)
//        if (BuildConfig.DEBUG && !Config.isInit()) {
//            if (activity is SplashActivity || activity is WelcomeActivity) {
//                return
//            }
//            finishAllActivity()
//            WelcomeActivity.start(activity)
//        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
    }

    fun getActivityStack(): Stack<Activity>? {
        return activityStack
    }

    companion object {
        const val CHECK_DELAY: Long = 500
        val TAG = MonitorForeAndBackTracker::class.java.name
        private var instance: MonitorForeAndBackTracker? = null

        /**
         * Its not strictly necessary to use this method - _usually_ invoking
         * get with a Context gives us a path to retrieve the Application and
         * initialise, but sometimes (e.g. in test harness) the ApplicationContext
         * is != the Application, and the docs make no guarantees.
         * @param application
         * @return an initialised Foreground instance
         */
        fun init(application: Application): MonitorForeAndBackTracker? {
            if (instance == null) {
                instance = MonitorForeAndBackTracker()
                application.registerActivityLifecycleCallbacks(instance)
            }
            return instance
        }

        operator fun get(application: Application): MonitorForeAndBackTracker? {
            if (instance == null) {
                init(application)
            }
            return instance
        }

        operator fun get(ctx: Context): MonitorForeAndBackTracker? {
            if (instance == null) {
                val appCtx = ctx.applicationContext
                if (appCtx is Application) {
                    init(appCtx)
                } else {
                    throw IllegalStateException(
                        "Foreground is not initialised and " +
                                "cannot obtain the Application object"
                    )
                }
            }
            return instance
        }

        fun get(): MonitorForeAndBackTracker? {
            checkNotNull(instance) {
                "Foreground is not initialised - invoke " +
                        "at least once with parameterised init/get"
            }
            return instance
        }

        fun forceFinishAllActivity() {
            if (instance!!.getActivityStack() != null) {
                var i = 0
                val size = instance!!.getActivityStack()!!.size
                while (i < size) {
                    instance!!.getActivityStack()!![i].finish()
                    i++
                }
                instance!!.getActivityStack()!!.clear()
            }
//            XDFRoomPrivacyProtocolDialog.loginExhibition = false
//            XDFRoomSPUtils.put(Constants.FIRST_HAVE_EJECT, false)
        }
    }
}