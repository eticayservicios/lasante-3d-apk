package com.lasante.tvkiosk.ui.screens.intro

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Immutable
import io.github.sceneview.RenderQuality

/** Perfil de carga según dispositivo — evita confundir tablet landscape con TV 42". */
@Immutable
internal data class VitrinaDeviceProfile(
    val isAndroidTv: Boolean,
    val isTabletLandscape: Boolean,
    val isLowRamDevice: Boolean,
    val isEmulator: Boolean,
)

/** Retrasos de prefetch y calidad Filament según perfil — reduce OOM en tablet táctil y emulador TV. */
internal object VitrinaDeviceLoadPolicy {
    fun resolve(context: Context, configuration: Configuration): VitrinaDeviceProfile {
        val isEmulator = isLikelyEmulator()
        val isTabletLandscape = isTabletLandscape(configuration)
        val isAndroidTv = isAndroidTv(context, configuration, isTabletLandscape)
        val isLowRam = isLowRamDevice(context)
        return VitrinaDeviceProfile(
            isAndroidTv = isAndroidTv,
            isTabletLandscape = isTabletLandscape,
            isLowRamDevice = isLowRam,
            isEmulator = isEmulator,
        )
    }

    /**
     * TV real: [Configuration.UI_MODE_TYPE_TELEVISION].
     * Tablets grandes (p. ej. tablet táctil 961×600 dp) comparten ancho con TV 42" — se excluyen por touch.
     */
    fun isAndroidTv(context: Context, configuration: Configuration): Boolean {
        val isTabletLandscape = isTabletLandscape(configuration)
        return isAndroidTv(context, configuration, isTabletLandscape)
    }

    private fun isAndroidTv(
        context: Context,
        configuration: Configuration,
        isTabletLandscape: Boolean,
    ): Boolean {
        if ((configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) ==
            Configuration.UI_MODE_TYPE_TELEVISION
        ) {
            return true
        }
        if (isTabletLandscape && hasTouchscreen(context)) return false
        if (isTabletLandscape) return false

        val maxW = maxOf(configuration.screenWidthDp, configuration.screenHeightDp)
        val maxH = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
        val isPhoneLandscape = maxW > maxH && maxH < 520
        if (isPhoneLandscape) return false
        return maxW >= 880 && maxH >= 480
    }

    fun isTabletLandscape(configuration: Configuration): Boolean {
        val maxW = maxOf(configuration.screenWidthDp, configuration.screenHeightDp)
        val maxH = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
        return maxW > maxH && maxW >= 640 && maxH >= 400
    }

    fun isLowRamDevice(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.isLowRamDevice || am.memoryClass <= 192
    }

    fun isLikelyEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true)

    fun videoPrefetchDelayMs(profile: VitrinaDeviceProfile): Long? = when {
        profile.isEmulator -> null
        profile.isAndroidTv -> 90_000L
        profile.isTabletLandscape && profile.isLowRamDevice -> 120_000L
        profile.isTabletLandscape -> 60_000L
        // Phone / Redmi: esperar más tras el GLB base (menos pico de RAM).
        profile.isLowRamDevice -> 60_000L
        else -> 45_000L
    }

    /** Screen saver: en phone espera GLB base + delay largo; TV/tablet un poco antes. */
    fun screenSaverPrefetchDelayMs(profile: VitrinaDeviceProfile): Long? = when {
        profile.isEmulator -> null
        profile.isAndroidTv -> 30_000L
        profile.isTabletLandscape && profile.isLowRamDevice -> 25_000L
        profile.isTabletLandscape -> 15_000L
        profile.isLowRamDevice -> 40_000L
        else -> 25_000L
    }

    fun institutionalVideoPrefetchDelayMs(profile: VitrinaDeviceProfile): Long? =
        videoPrefetchDelayMs(profile)

    /**
     * Calidad Filament:
     * - Emulador TV66/4K: [RenderQuality.Performance] (DRS) para evitar ANR en x86.
     * - Emulador TV1080 / Television_1080 (TV_REGULAR): [RenderQuality.Default] —
     *   Performance+DRS se ve muy pixelado en 1137×711.
     * - Dispositivos reales (tablet táctil incluido): Default sin DRS; la RAM se mitiga
     *   con prefetch retrasado, no bajando resolución del GLB.
     */
    fun filamentRenderQuality(context: Context, layoutMetrics: IntroLayoutMetrics): RenderQuality =
        when {
            isLikelyEmulator() && layoutMetrics.isTv66 -> RenderQuality.Performance
            else -> RenderQuality.Default
        }

    private fun hasTouchscreen(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
}
