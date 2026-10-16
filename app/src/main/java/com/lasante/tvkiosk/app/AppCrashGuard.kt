package com.lasante.tvkiosk.app

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Captura crashes Java/Kotlin no manejados y deja rastro en logcat + archivo local
 * para diagnosticar tablets que “abren y cierran” sin USB.
 */
object AppCrashGuard {
    private const val TAG = "LaSanteCrash"
    private const val DIR = "crashes"
    private const val FILE = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { persist(appContext, thread, throwable) }
            previous?.uncaughtException(thread, throwable)
        }
        logBootFingerprint(appContext)
    }

    fun lastCrashText(context: Context): String? {
        val file = File(File(context.filesDir, DIR), FILE)
        return if (file.isFile) file.readText() else null
    }

    private fun logBootFingerprint(context: Context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        Log.i(
            TAG,
            "boot model=${Build.MODEL} manufacturer=${Build.MANUFACTURER} " +
                "sdk=${Build.VERSION.SDK_INT} abi=${Build.SUPPORTED_ABIS.joinToString()} " +
                "memoryClass=${am.memoryClass} largeMemoryClass=${am.largeMemoryClass} " +
                "lowRam=${am.isLowRamDevice} " +
                "gles=0x${Integer.toHexString(am.deviceConfigurationInfo.reqGlEsVersion)} " +
                "filamentNative=${FilamentNativeSupport.available}",
        )
        FilamentNativeSupport.error?.let { Log.e(TAG, "filamentNativeError=$it") }
    }

    private fun persist(context: Context, thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val body = buildString {
            appendLine("time=$stamp")
            appendLine("thread=${thread.name}")
            appendLine("model=${Build.MODEL}")
            appendLine("manufacturer=${Build.MANUFACTURER}")
            appendLine("device=${Build.DEVICE}")
            appendLine("sdk=${Build.VERSION.SDK_INT}")
            appendLine("abis=${Build.SUPPORTED_ABIS.joinToString()}")
            appendLine("filamentNative=${FilamentNativeSupport.available}")
            appendLine("filamentError=${FilamentNativeSupport.error}")
            appendLine("---")
            append(sw.toString())
        }
        Log.e(TAG, body)
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        File(dir, FILE).writeText(body)
    }
}
