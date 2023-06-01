package com.test.vapdemo

import android.os.Handler
import android.os.Looper

/**
 * Created by tailin on 2018/11/19.
 */
object HandlerGetter {
    @JvmStatic
    val mainHandler: Handler = Handler(Looper.getMainLooper())

    @JvmStatic
    @JvmOverloads
    fun runOnUIThread(runnable: Runnable, delay: Long? = null) {
        if (delay == null) {
            mainHandler.post(runnable)
        } else {
            mainHandler.postDelayed(runnable, delay)
        }
    }

}