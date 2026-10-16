package com.lasante.tvkiosk.app

import android.app.ActivityManager
import android.content.Context
import android.util.Log

/**
 * Preflight de arranque: GLES 3.0 + carga de .so de Filament.
 * Evita entrar a rememberEngine() en tablets que van a tumbar el proceso.
 */
object FilamentNativeSupport {
    private const val TAG = "FilamentNative"
    private const val GLES_3 = 0x00030000

    @Volatile
    var available: Boolean = false
        private set

    @Volatile
    var error: String? = null
        private set

    @Volatile
    var glesVersion: Int = 0
        private set

    fun evaluate(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        glesVersion = am.deviceConfigurationInfo.reqGlEsVersion
        if (glesVersion < GLES_3) {
            available = false
            error =
                "OpenGL ES 3.0 requerido (dispositivo=0x${Integer.toHexString(glesVersion)})"
            Log.e(TAG, error!!)
            return false
        }

        val loadError = runCatching {
            // Orden: core → utils → gltfio (dependencias nativas de SceneView/Filament).
            System.loadLibrary("filament-jni")
            System.loadLibrary("filament-utils-jni")
            System.loadLibrary("gltfio-jni")
        }.exceptionOrNull()

        if (loadError != null) {
            available = false
            error = loadError.message ?: loadError.javaClass.simpleName
            Log.e(TAG, "loadLibrary failed", loadError)
            return false
        }

        available = true
        error = null
        Log.i(TAG, "native OK gles=0x${Integer.toHexString(glesVersion)}")
        return true
    }

    fun userMessage(): String =
        error
            ?: "Este dispositivo no puede renderizar la vitrina 3D. " +
            "Puedes seguir navegando el catálogo por unidades."
}
