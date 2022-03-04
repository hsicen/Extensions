package com.hsicen.extension.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * @author: hsicen
 * @date: 2022/3/4 16:35
 * @email: codinghuang@163.com
 * description: 应用生命周期处理
 */
object AppLife {
    private var started = 0
    private var resumed = 0
    private var paused = 0
    private var stopped = 0

    fun isAppVisible() = started > stopped

    fun isAppForeground() = resumed > paused

    fun Application.registerLifecycle() {
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
                ++started
            }

            override fun onActivityResumed(activity: Activity) {
                ++resumed
            }

            override fun onActivityPaused(activity: Activity) {
                ++paused
            }

            override fun onActivityStopped(activity: Activity) {
                ++stopped
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }
}