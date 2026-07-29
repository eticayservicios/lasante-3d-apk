package com.lasante.tvkiosk.ui.screens.intro

import android.util.Log
import com.lasante.tvkiosk.BuildConfig

internal object VitrinaDebugLog {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun e(tag: String, message: String, error: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (error != null) Log.e(tag, message, error) else Log.e(tag, message)
        }
    }
}
